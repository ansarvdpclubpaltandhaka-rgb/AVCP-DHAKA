const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const SUPER_ADMIN_UID = "GpfMc9GLItN0NRDoeHW9OnCPNre2";
const ADMIN_UID = "admin_user_001";
const ALICE_UID = "alice_member_001";
const BOB_UID = "bob_member_002";
const ORG_ID = "ansar_vdp_paltan_dhaka";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) await testEnv.cleanup();
});

beforeEach(async () => {
  if (testEnv) await testEnv.clearFirestore();
});

async function seedAdmin(perms = ["manage_members", "manage_services", "manage_activities", "manage_blood", "manage_finance", "manage_notices", "manage_requests"]) {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    const past = new Date(Date.now() - 10000);
    await db.collection("users").doc(ADMIN_UID).set({
      uid: ADMIN_UID,
      name: "Admin User",
      email: "admin@example.com",
      phone: "01700000000",
      organizationId: ORG_ID,
      role: "admin",
      permissions: perms,
      status: "active",
      createdAt: past,
      updatedAt: past,
    });
  });
}

test("Unauthenticated user cannot read or write any collection", async () => {
  const db = testEnv.unauthenticatedContext().firestore();
  await assertFails(db.collection("services").where("organizationId", "==", ORG_ID).get());
  await assertFails(db.collection("users").doc(ALICE_UID).get());
  await assertFails(db.collection("bloodRequests").where("organizationId", "==", ORG_ID).get());
});

test("Member registration enforces role=member and blocks privilege escalation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const past = new Date(Date.now() - 5000);

  // Self-assigning admin must fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      uid: ALICE_UID,
      name: "Alice",
      email: "alice@example.com",
      phone: "01711111111",
      organizationId: ORG_ID,
      role: "admin",
      permissions: ["manage_finance"],
      status: "active",
      createdAt: past,
      updatedAt: past,
    })
  );

  // Valid member registration succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      uid: ALICE_UID,
      name: "Alice",
      email: "alice@example.com",
      phone: "01711111111",
      organizationId: ORG_ID,
      role: "member",
      permissions: [],
      status: "active",
      createdAt: past,
      updatedAt: past,
    })
  );

  // Member cannot escalate own role on update
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      role: "super_admin",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Member can update own allowed profile fields
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      name: "Alice Rahman",
      bloodGroup: "O+",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Super Admin UID can bootstrap and manage Admin roles/permissions", async () => {
  const superDb = testEnv.authenticatedContext(SUPER_ADMIN_UID).firestore();
  const past = new Date(Date.now() - 5000);

  await assertSucceeds(
    superDb.collection("users").doc(SUPER_ADMIN_UID).set({
      uid: SUPER_ADMIN_UID,
      name: "Super Admin",
      email: "super@example.com",
      phone: "01700000001",
      organizationId: ORG_ID,
      role: "super_admin",
      permissions: ["manage_members", "manage_services", "manage_activities", "manage_blood", "manage_finance", "manage_notices", "manage_requests", "manage_settings"],
      status: "active",
      createdAt: past,
      updatedAt: past,
    })
  );

  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("users").doc(ALICE_UID).set({
      uid: ALICE_UID,
      name: "Alice",
      email: "alice@example.com",
      phone: "01711111111",
      organizationId: ORG_ID,
      role: "member",
      permissions: [],
      status: "active",
      createdAt: past,
      updatedAt: past,
    });
  });

  await assertSucceeds(
    superDb.collection("users").doc(ALICE_UID).update({
      role: "admin",
      permissions: ["manage_members", "manage_notices"],
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("PII and Finance isolation: members cannot read members/donations/expenses or other users", async () => {
  await seedAdmin();
  const past = new Date(Date.now() - 5000);
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("users").doc(BOB_UID).set({
      uid: BOB_UID, name: "Bob", email: "bob@example.com", phone: "01800000000",
      organizationId: ORG_ID, role: "member", permissions: [], status: "active",
      createdAt: past, updatedAt: past,
    });
    await db.collection("members").doc("mem_1").set({
      organizationId: ORG_ID, name: "Bob", phone: "01800000000", bloodGroup: "A+",
      position: "Member", status: "active", createdAt: past, updatedAt: past,
    });
    await db.collection("donations").doc("don_1").set({
      organizationId: ORG_ID, donorName: "Donor", amount: 5000, purpose: "Relief",
      donationDate: "2026-11-19", paymentMethod: "bKash", createdAt: past, updatedAt: past,
    });
    await db.collection("contributions").doc("con_bob").set({
      organizationId: ORG_ID, memberUid: BOB_UID, memberName: "Bob",
      expectedAmount: 500, paidAmount: 500, dueAmount: 0, month: "2026-11",
      paymentDate: "2026-11-10", paymentMethod: "Cash", createdAt: past, updatedAt: past,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const adminDb = testEnv.authenticatedContext(ADMIN_UID).firestore();

  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(aliceDb.collection("members").where("organizationId", "==", ORG_ID).get());
  await assertFails(aliceDb.collection("donations").where("organizationId", "==", ORG_ID).get());
  await assertFails(aliceDb.collection("contributions").where("organizationId", "==", ORG_ID).get());

  // Bob can query his own contributions
  await assertSucceeds(
    bobDb.collection("contributions").where("organizationId", "==", ORG_ID).where("memberUid", "==", BOB_UID).get()
  );

  // Admin can query members and donations
  await assertSucceeds(adminDb.collection("members").where("organizationId", "==", ORG_ID).get());
  await assertSucceeds(adminDb.collection("donations").where("organizationId", "==", ORG_ID).get());
});

test("Messaging and Notifications isolation", async () => {
  const past = new Date(Date.now() - 5000);
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const adminDb = testEnv.authenticatedContext(ADMIN_UID).firestore();
  const convId = `${ALICE_UID}_${BOB_UID}`;

  await assertSucceeds(
    aliceDb.collection("conversations").doc(convId).set({
      organizationId: ORG_ID,
      participantIds: [ALICE_UID, BOB_UID],
      participantNames: { [ALICE_UID]: "Alice", [BOB_UID]: "Bob" },
      lastMessageText: "Assalamu Alaikum",
      lastSenderId: ALICE_UID,
      updatedAt: past,
    })
  );

  await assertSucceeds(
    aliceDb.collection("conversations").doc(convId).collection("messages").doc("msg_1").set({
      senderId: ALICE_UID,
      participantIds: [ALICE_UID, BOB_UID],
      text: "Assalamu Alaikum",
      seen: false,
      createdAt: past,
    })
  );

  // Bob can read conversation and messages
  await assertSucceeds(bobDb.collection("conversations").where("participantIds", "array-contains", BOB_UID).get());
  await assertSucceeds(
    bobDb.collection("conversations").doc(convId).collection("messages").where("participantIds", "array-contains", BOB_UID).get()
  );

  // Third party cannot read Alice-Bob conversation or messages
  await assertFails(adminDb.collection("conversations").doc(convId).get());
  await assertFails(
    adminDb.collection("conversations").doc(convId).collection("messages").get()
  );
  await assertFails(
    adminDb.collection("conversations").doc(convId).collection("messages").where("participantIds", "array-contains", ALICE_UID).get()
  );

  // Notifications dual-query pattern
  await assertSucceeds(
    aliceDb.collection("notifications").where("organizationId", "==", ORG_ID).where("targetScope", "==", "all").get()
  );
  await assertSucceeds(
    aliceDb.collection("notifications").where("organizationId", "==", ORG_ID).where("targetUserId", "==", ALICE_UID).get()
  );
  await assertFails(
    aliceDb.collection("notifications").where("organizationId", "==", ORG_ID).get()
  );
});

test("Admin email ansarvdpclubpaltandhaka@gmail.com gets Super Admin and normal members are View-Only on members/allowances/notices", async () => {
  const past = new Date(Date.now() - 5000);
  const adminByEmailDb = testEnv.authenticatedContext("email_admin_uid", {
    email: "ansarvdpclubpaltandhaka@gmail.com",
  }).firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "alice@example.com",
  }).firestore();

  // Admin email can create own super_admin profile
  await assertSucceeds(
    adminByEmailDb.collection("users").doc("email_admin_uid").set({
      uid: "email_admin_uid",
      name: "Paltan Admin",
      email: "ansarvdpclubpaltandhaka@gmail.com",
      phone: "01700000000",
      organizationId: ORG_ID,
      role: "super_admin",
      permissions: ["manage_members", "manage_finance", "manage_notices"],
      status: "active",
      createdAt: past,
      updatedAt: past,
    })
  );

  // Admin email can write members, allowances, and notices
  await assertSucceeds(
    adminByEmailDb.collection("members").doc("m_test").set({
      organizationId: ORG_ID, name: "Member 1", phone: "01712345678",
      bloodGroup: "O+", position: "Member", status: "active",
      createdAt: past, updatedAt: past,
    })
  );
  await assertSucceeds(
    adminByEmailDb.collection("allowances").doc("alw_test").set({
      organizationId: ORG_ID, memberName: "Member 1",
      dailyAmount: 500, monthlyTotal: 10000, paid: true,
    })
  );
  await assertSucceeds(
    adminByEmailDb.collection("notices").doc("n_test").set({
      organizationId: ORG_ID, title: "Meeting", body: "Tomorrow at 4 PM",
      noticeType: "notice", pinned: true, createdByUid: "email_admin_uid",
      createdAt: past, updatedAt: past,
    })
  );

  // Normal member cannot add/delete members, edit allowances, or send notices
  await assertFails(
    aliceDb.collection("members").doc("m_fail").set({
      organizationId: ORG_ID, name: "Fake", phone: "017",
      bloodGroup: "A+", position: "Member", status: "active",
      createdAt: past, updatedAt: past,
    })
  );
  await assertFails(aliceDb.collection("members").doc("m_test").delete());
  await assertFails(aliceDb.collection("allowances").doc("alw_test").update({ paid: false }));
  await assertFails(
    aliceDb.collection("notices").doc("n_fail").set({
      organizationId: ORG_ID, title: "Spam", body: "Test",
      noticeType: "notice", pinned: false, createdByUid: ALICE_UID,
      createdAt: past, updatedAt: past,
    })
  );

  // Normal member CAN read allowances and notices
  await assertSucceeds(aliceDb.collection("allowances").doc("alw_test").get());
  await assertSucceeds(aliceDb.collection("notices").doc("n_test").get());
});
