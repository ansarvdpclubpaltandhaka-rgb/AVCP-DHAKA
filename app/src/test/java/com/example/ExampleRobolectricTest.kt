package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.AppConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("আনসার ভিডিপি ক্লাব পল্টন ঢাকা", appName)
  }

  @Test
  fun `super admin recognized by configured email and uid`() {
    assertTrue(AppConstants.isSuperAdminUser("any_uid", "ansarvdpclubpaltandhaka@gmail.com"))
    assertTrue(AppConstants.isSuperAdminUser("any_uid", "AnsarVDPClubPaltanDhaka@gmail.com"))
    assertTrue(AppConstants.isSuperAdminUser(AppConstants.SUPER_ADMIN_UID, "other@example.com"))
    assertFalse(AppConstants.isSuperAdminUser("normal_uid", "member@example.com"))
  }

  @Test
  fun `verifyAdminCredentials matches only configured admin email and password`() {
    assertTrue(AppConstants.verifyAdminCredentials("ansarvdpclubpaltandhaka@gmail.com", "Paltan@2026"))
    assertTrue(AppConstants.verifyAdminCredentials("  AnsarVDPClubPaltanDhaka@gmail.com ", "Paltan@2026"))
    assertFalse(AppConstants.verifyAdminCredentials("ansarvdpclubpaltandhaka@gmail.com", "wrong_pass"))
    assertFalse(AppConstants.verifyAdminCredentials("other@gmail.com", "Paltan@2026"))
  }

  @Test
  fun `member register creates pending account blocked from login until admin approves with one of 7 trades`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = com.example.data.AuthRepository(context)

    val expectedTrades = listOf(
      "পুরুষ টিডিপি",
      "মহিলা টিডিপি",
      "ড্রাইভার",
      "থানা আনসার",
      "দলনেতা",
      "দলনেত্রী",
      "সদস্য"
    )
    assertEquals(expectedTrades, AppConstants.MEMBER_TRADES)

    val regResult = repo.registerMember(
      name = "করিম উদ্দিন",
      phone = "01712345678",
      platoon = "পল্টন প্লাটুন-১",
      password = "pass1234"
    )
    assertTrue(regResult.isSuccess)
    val pendingAccount = regResult.getOrThrow()
    assertEquals("", pendingAccount.trade)
    assertEquals(AppConstants.STATUS_PENDING, pendingAccount.status)
    assertFalse(pendingAccount.isApprovedWithTrade)

    // Duplicate phone should fail
    val dupResult = repo.registerMember(
      name = "অন্য নাম",
      phone = "01712345678",
      platoon = "পল্টন প্লাটুন-২",
      password = "pass9999"
    )
    assertTrue(dupResult.isFailure)

    // Pending member CANNOT login yet and sees exact pending message
    val freshRepo = com.example.data.AuthRepository(context)
    val loginPendingResult = freshRepo.signInMember("01712345678", "pass1234")
    assertTrue(loginPendingResult.isFailure)
    assertEquals(AppConstants.MSG_MEMBER_PENDING, loginPendingResult.exceptionOrNull()?.message)

    // Wrong password still fails with auth error
    assertTrue(freshRepo.signInMember("01712345678", "wrong").isFailure)

    // Invalid trade should be rejected
    assertTrue(freshRepo.approveMemberAndSetTrade(pendingAccount.uid, "InvalidTrade").isFailure)

    // Admin approves and sets one of the 7 official Bengali Trades
    val approveResult = freshRepo.approveMemberAndSetTrade(pendingAccount.uid, "ড্রাইভার")
    assertTrue(approveResult.isSuccess)
    val approvedAccount = approveResult.getOrThrow()
    assertEquals("ড্রাইভার", approvedAccount.trade)
    assertEquals(AppConstants.STATUS_APPROVED, approvedAccount.status)
    assertTrue(approvedAccount.isApprovedWithTrade)

    // Reload repository and verify approved Member CAN now log in and see his Dashboard profile
    val verifiedRepo = com.example.data.AuthRepository(context)
    val loginApprovedResult = verifiedRepo.signInMember("01712345678", "pass1234")
    assertTrue(loginApprovedResult.isSuccess)
    val approvedProfile = loginApprovedResult.getOrThrow()
    assertEquals("করিম উদ্দিন", approvedProfile.name)
    assertEquals("01712345678", approvedProfile.phone)
    assertEquals("পল্টন প্লাটুন-১", approvedProfile.platoon)
    assertEquals("ড্রাইভার", approvedProfile.trade)
    assertEquals(AppConstants.STATUS_APPROVED, approvedProfile.status)
    assertEquals(AppConstants.ROLE_MEMBER, approvedProfile.role)
  }

  @Test
  fun `rejected member cannot log in and disappears from active list in MainViewModel`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
      com.google.firebase.FirebaseApp.initializeApp(context)
    }
    val repo = com.example.data.AuthRepository(context)
    val pending = repo.registerMember(
      name = "রাশেদ খান",
      phone = "01811223344",
      platoon = "পল্টন প্লাটুন-২",
      password = "pass1234"
    ).getOrThrow()

    val adminVm = com.example.viewmodel.MainViewModel(
      repository = com.example.data.OrgRepository(),
      currentUid = AppConstants.SUPER_ADMIN_UID,
      isAuthenticated = true,
      isAdmin = true,
      isSuperAdmin = true
    )
    adminVm.syncRegisteredMembers(listOf(pending))
    assertTrue((adminVm.adminMembersState.value as com.example.core.UiState.Success).data.any { it.id == pending.uid })

    val rejected = repo.rejectMember(pending.uid).getOrThrow()
    assertEquals(AppConstants.STATUS_REJECTED, rejected.status)
    adminVm.rejectMember(pending.uid)
    assertFalse((adminVm.adminMembersState.value as com.example.core.UiState.Success).data.any { it.id == pending.uid })

    val signInRejected = repo.signInMember("01811223344", "pass1234")
    assertTrue(signInRejected.isFailure)
    assertEquals(AppConstants.MSG_MEMBER_REJECTED, signInRejected.exceptionOrNull()?.message)
  }

  @Test
  fun `member cannot add delete member edit allowance or approve trade in MainViewModel`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
      com.google.firebase.FirebaseApp.initializeApp(context)
    }
    val vm = com.example.viewmodel.MainViewModel(
      repository = com.example.data.OrgRepository(),
      currentUid = "demo_1",
      isAuthenticated = true,
      isAdmin = false,
      isSuperAdmin = false,
      currentUserName = "মো. রুহুল আমিন",
      currentUserPhone = "01711000001"
    )
    val initialAllowances = (vm.allowancesState.value as com.example.core.UiState.Success).data
    assertEquals(1, initialAllowances.size)
    val target = initialAllowances.first()

    vm.saveMember(
      existingId = null, linkedUid = "", name = "নতুন", photoUrl = "",
      phone = "01800000000", email = "", bloodGroup = "O+", position = "সদস্য",
      joiningDate = "", address = "", status = "active", bloodDonor = false,
      donorAvailable = false, donationCount = 0L, lastDonation = ""
    )
    assertEquals("শুধুমাত্র অ্যাডমিন এই কাজটি করতে পারবেন (View Only)", vm.actionFeedback.value)

    vm.approveMemberWithTrade("m1", "থানা আনসার")
    assertEquals("শুধুমাত্র অ্যাডমিন এই কাজটি করতে পারবেন (View Only)", vm.actionFeedback.value)

    vm.rejectMember("m1")
    assertEquals("শুধুমাত্র অ্যাডমিন এই কাজটি করতে পারবেন (View Only)", vm.actionFeedback.value)

    vm.deleteMember("d1")
    assertEquals("শুধুমাত্র অ্যাডমিন এই কাজটি করতে পারবেন (View Only)", vm.actionFeedback.value)

    vm.updateAllowanceRecord(target.id, target.memberName, target.areaOrDuty, target.date, 9999L, 99999L, !target.paid)
    val afterAllowances = (vm.allowancesState.value as com.example.core.UiState.Success).data
    assertEquals(target.dailyAmount, afterAllowances.first { it.id == target.id }.dailyAmount)
  }

  @Test
  fun `privacy rule isolates member own profile attendance and allowance while admin sees all`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
      com.google.firebase.FirebaseApp.initializeApp(context)
    }

    // 1. Guest (unauthenticated): sees zero member list entries, zero member allowances, zero blood requests
    val guestVm = com.example.viewmodel.MainViewModel(
      repository = com.example.data.OrgRepository(),
      currentUid = "demo_guest",
      isAuthenticated = false,
      isAdmin = false,
      isSuperAdmin = false
    )
    assertTrue((guestVm.directoryState.value as com.example.core.UiState.Success).data.isEmpty())
    assertTrue((guestVm.allowancesState.value as com.example.core.UiState.Success).data.isEmpty())
    assertTrue((guestVm.adminMembersState.value as com.example.core.UiState.Success).data.isEmpty())
    assertTrue((guestVm.positionsState.value as com.example.core.UiState.Success).data.isEmpty())

    // 2. Approved Member ("মো. রুহুল আমিন"): sees ONLY own attendance entry and own allowance, NEVER other members
    val memberVm = com.example.viewmodel.MainViewModel(
      repository = com.example.data.OrgRepository(),
      currentUid = "demo_1",
      isAuthenticated = true,
      isAdmin = false,
      isSuperAdmin = false,
      currentUserName = "মো. রুহুল আমিন",
      currentUserPhone = "01711000001"
    )
    val memberDir = (memberVm.directoryState.value as com.example.core.UiState.Success).data
    assertEquals(1, memberDir.size)
    assertEquals("মো. রুহুল আমিন", memberDir.first().name)
    assertTrue((memberVm.adminMembersState.value as com.example.core.UiState.Success).data.isEmpty())
    assertTrue((memberVm.adminVolunteersState.value as com.example.core.UiState.Success).data.isEmpty())

    val memberAllowances = (memberVm.allowancesState.value as com.example.core.UiState.Success).data
    assertEquals(1, memberAllowances.size)
    assertEquals("মো. রুহুল আমিন", memberAllowances.first().memberName)

    // 3. Admin: sees ALL members list, ALL profiles, ALL attendance, ALL allowances
    val adminVm = com.example.viewmodel.MainViewModel(
      repository = com.example.data.OrgRepository(),
      currentUid = AppConstants.SUPER_ADMIN_UID,
      isAuthenticated = true,
      isAdmin = true,
      isSuperAdmin = true
    )
    assertEquals(com.example.data.DemoDataProvider.demoDirectory.size, (adminVm.directoryState.value as com.example.core.UiState.Success).data.size)
    assertEquals(com.example.data.DemoDataProvider.demoMembers.size, (adminVm.adminMembersState.value as com.example.core.UiState.Success).data.size)
    assertEquals(com.example.data.DemoDataProvider.demoAllowances.size, (adminVm.allowancesState.value as com.example.core.UiState.Success).data.size)
  }

  @Test
  fun `admin can approve pending member with trade in MainViewModel`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
      com.google.firebase.FirebaseApp.initializeApp(context)
    }
    val repo = com.example.data.AuthRepository(context)
    val account = repo.registerMember(
      name = "সোহেল রানা",
      phone = "01911223344",
      platoon = "পল্টন প্লাটুন-৩",
      password = "pass5678"
    ).getOrThrow()

    val adminVm = com.example.viewmodel.MainViewModel(
      repository = com.example.data.OrgRepository(),
      currentUid = AppConstants.SUPER_ADMIN_UID,
      isAuthenticated = true,
      isAdmin = true,
      isSuperAdmin = true
    )
    adminVm.syncRegisteredMembers(listOf(account))

    val beforeMembers = (adminVm.adminMembersState.value as com.example.core.UiState.Success).data
    val syncedPending = beforeMembers.first { it.id == account.uid }
    assertEquals(AppConstants.STATUS_PENDING, syncedPending.status)
    assertEquals("", syncedPending.trade)

    adminVm.approveMemberWithTrade(account.uid, "থানা আনসার")
    val afterMembers = (adminVm.adminMembersState.value as com.example.core.UiState.Success).data
    val syncedApproved = afterMembers.first { it.id == account.uid }
    assertEquals(AppConstants.STATUS_APPROVED, syncedApproved.status)
    assertEquals("থানা আনসার", syncedApproved.trade)
  }
}
