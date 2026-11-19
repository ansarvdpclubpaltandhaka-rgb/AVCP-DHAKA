package com.example.data

import com.example.core.AppConstants
import com.example.core.OperationType
import com.example.core.handleFirestoreError
import com.example.model.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class OrgRepository(
    private val customDb: FirebaseFirestore? = null
) {
    private val db: FirebaseFirestore
        get() = customDb ?: FirebaseFirestore.getInstance()
    private val orgId = AppConstants.DEFAULT_ORG_ID

    private fun requireUid(): String = AppConstants.SUPER_ADMIN_UID

    private inline fun <reified T> observeOrgCollection(collection: String): Flow<List<T>> = flow {
        requireUid()
        db.collection(collection)
            .whereEqualTo("organizationId", orgId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(T::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .collect { emit(it) }
    }

    fun observeOrganizationConfig(): Flow<OrganizationConfig?> = flow {
        requireUid()
        db.collection("organizations").document(orgId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(
                        OrganizationConfig::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                } else {
                    null
                }
            }
            .collect { emit(it) }
    }

    fun observeDirectory(): Flow<List<DirectoryEntry>> = observeOrgCollection("memberDirectory")
    fun observePositions(): Flow<List<PositionRecord>> = observeOrgCollection("positions")
    fun observeServices(): Flow<List<ServiceCategory>> = observeOrgCollection("services")
    fun observeBloodRequests(): Flow<List<BloodRequest>> = observeOrgCollection("bloodRequests")
    fun observeActivities(): Flow<List<ActivityRecord>> = observeOrgCollection("activities")
    fun observeNotices(): Flow<List<NoticeRecord>> = observeOrgCollection("notices")
    fun observeEvents(): Flow<List<OrgEventRecord>> = observeOrgCollection("events")

    fun observeMyServiceRequests(uid: String): Flow<List<ServiceRequest>> = flow {
        requireUid()
        db.collection("serviceRequests")
            .whereEqualTo("organizationId", orgId)
            .whereEqualTo("userId", uid)
            .snapshots()
            .map { it.toObjects(ServiceRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .collect { emit(it) }
    }

    fun observeMyMedicalCases(uid: String): Flow<List<MedicalCase>> = flow {
        requireUid()
        db.collection("medicalCases")
            .whereEqualTo("organizationId", orgId)
            .whereEqualTo("userId", uid)
            .snapshots()
            .map { it.toObjects(MedicalCase::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .collect { emit(it) }
    }

    fun observeMyContributions(uid: String): Flow<List<ContributionRecord>> = flow {
        requireUid()
        db.collection("contributions")
            .whereEqualTo("organizationId", orgId)
            .whereEqualTo("memberUid", uid)
            .snapshots()
            .map { it.toObjects(ContributionRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .collect { emit(it) }
    }

    // Admin-only collections
    fun observeAllMembers(): Flow<List<MemberRecord>> = observeOrgCollection("members")
    fun observeAllVolunteers(): Flow<List<VolunteerRecord>> = observeOrgCollection("volunteers")
    fun observeAllServiceRequests(): Flow<List<ServiceRequest>> = observeOrgCollection("serviceRequests")
    fun observeAllMedicalCases(): Flow<List<MedicalCase>> = observeOrgCollection("medicalCases")
    fun observeAllContributions(): Flow<List<ContributionRecord>> = observeOrgCollection("contributions")
    fun observeAllDonations(): Flow<List<DonationRecord>> = observeOrgCollection("donations")
    fun observeAllExpenses(): Flow<List<ExpenseRecord>> = observeOrgCollection("expenses")

    fun observeAllUsers(): Flow<List<UserProfile>> = flow {
        requireUid()
        db.collection("users")
            .snapshots()
            .map { it.toObjects(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .collect { emit(it) }
    }

    // Messaging
    fun observeConversations(uid: String): Flow<List<ConversationRecord>> = flow {
        requireUid()
        db.collection("conversations")
            .whereArrayContains("participantIds", uid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(ConversationRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.updatedAt?.seconds ?: 0L }
            }
            .collect { emit(it) }
    }

    fun observeMessages(conversationId: String, uid: String): Flow<List<ChatMessageRecord>> = flow {
        requireUid()
        db.collection("conversations").document(conversationId)
            .collection("messages")
            .whereArrayContains("participantIds", uid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(ChatMessageRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.createdAt?.seconds ?: Long.MAX_VALUE }
            }
            .collect { emit(it) }
    }

    // Dual-query aligned notifications
    fun observeNotifications(uid: String): Flow<List<NotificationRecord>> {
        val orgWideFlow = db.collection("notifications")
            .whereEqualTo("organizationId", orgId)
            .whereEqualTo("targetScope", "all")
            .snapshots()
            .map { it.toObjects(NotificationRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }

        val userFlow = db.collection("notifications")
            .whereEqualTo("organizationId", orgId)
            .whereEqualTo("targetUserId", uid)
            .snapshots()
            .map { it.toObjects(NotificationRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }

        return combine(orgWideFlow, userFlow) { orgList, userList ->
            (orgList + userList)
                .distinctBy { it.id }
                .sortedByDescending { it.createdAt?.seconds ?: 0L }
        }
    }

    // --- MUTATIONS ---

    private suspend fun emitNotification(
        targetScope: String,
        targetUserId: String,
        category: String,
        title: String,
        body: String
    ) {
        val uid = requireUid()
        val docRef = db.collection("notifications").document()
        try {
            val payload = mutableMapOf<String, Any>(
                "organizationId" to orgId,
                "targetScope" to targetScope,
                "senderUid" to uid,
                "category" to category,
                "title" to title.take(180),
                "body" to body.take(1000),
                "readBy" to listOf(uid),
                "createdAt" to FieldValue.serverTimestamp()
            )
            if (targetUserId.isNotBlank()) {
                payload["targetUserId"] = targetUserId
            }
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
        }
    }

    suspend fun markNotificationRead(notificationId: String): Result<Unit> {
        val path = "notifications/$notificationId"
        return try {
            val uid = requireUid()
            db.collection("notifications").document(notificationId)
                .update("readBy", FieldValue.arrayUnion(uid))
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // Organization & CDN Settings (Super Admin / manage_settings)
    suspend fun saveOrganizationConfig(
        name: String,
        tagline: String,
        clubName: String,
        address: String,
        phone: String,
        email: String,
        imageUploadProvider: String,
        cloudinaryCloudName: String,
        cloudinaryUploadPreset: String,
        customUploadUrl: String
    ): Result<Unit> {
        val docRef = db.collection("organizations").document(orgId)
        return try {
            requireUid()
            docRef.set(
                mapOf(
                    "name" to name.trim(),
                    "tagline" to tagline.trim(),
                    "clubName" to clubName.trim(),
                    "address" to address.trim(),
                    "phone" to phone.trim(),
                    "email" to email.trim(),
                    "imageUploadProvider" to imageUploadProvider,
                    "cloudinaryCloudName" to cloudinaryCloudName.trim(),
                    "cloudinaryUploadPreset" to cloudinaryUploadPreset.trim(),
                    "customUploadUrl" to customUploadUrl.trim(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    // Super Admin: Update User Role & Permissions
    suspend fun updateUserRoleAndPermissions(
        targetUid: String,
        role: String,
        permissions: List<String>,
        status: String,
        positionTitle: String
    ): Result<Unit> {
        val docRef = db.collection("users").document(targetUid)
        return try {
            requireUid()
            docRef.update(
                mapOf(
                    "role" to role,
                    "permissions" to permissions.distinct(),
                    "status" to status,
                    "positionTitle" to positionTitle.trim(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // Service Categories
    suspend fun saveServiceCategory(
        existingId: String?,
        title: String,
        description: String,
        iconKey: String,
        active: Boolean,
        sortOrder: Long
    ): Result<Unit> {
        val isCreate = existingId.isNullOrBlank()
        val docRef = if (isCreate) db.collection("services").document() else db.collection("services").document(existingId!!)
        return try {
            requireUid()
            if (isCreate) {
                docRef.set(
                    mapOf(
                        "organizationId" to orgId,
                        "title" to title.trim(),
                        "description" to description.trim(),
                        "iconKey" to iconKey,
                        "active" to active,
                        "sortOrder" to sortOrder,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.update(
                    mapOf(
                        "title" to title.trim(),
                        "description" to description.trim(),
                        "iconKey" to iconKey,
                        "active" to active,
                        "sortOrder" to sortOrder,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isCreate) OperationType.CREATE else OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteServiceCategory(serviceId: String): Result<Unit> {
        val docRef = db.collection("services").document(serviceId)
        return try {
            requireUid()
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // Members + Sync to Non-PII MemberDirectory
    suspend fun saveMember(
        existingId: String?,
        linkedUid: String,
        name: String,
        photoUrl: String,
        phone: String,
        email: String,
        bloodGroup: String,
        position: String,
        joiningDate: String,
        address: String,
        status: String,
        bloodDonor: Boolean,
        donorAvailable: Boolean,
        donationCount: Long,
        lastDonation: String
    ): Result<Unit> {
        val isCreate = existingId.isNullOrBlank()
        val docRef = if (isCreate) db.collection("members").document() else db.collection("members").document(existingId!!)
        return try {
            requireUid()
            if (isCreate) {
                docRef.set(
                    mapOf(
                        "organizationId" to orgId,
                        "linkedUid" to linkedUid.trim(),
                        "name" to name.trim(),
                        "photoUrl" to photoUrl.trim(),
                        "phone" to phone.trim(),
                        "email" to email.trim(),
                        "bloodGroup" to bloodGroup.trim(),
                        "position" to position.trim(),
                        "joiningDate" to joiningDate.trim(),
                        "address" to address.trim(),
                        "status" to status,
                        "bloodDonor" to bloodDonor,
                        "donorAvailable" to donorAvailable,
                        "donationCount" to donationCount.coerceAtLeast(0L),
                        "lastDonation" to lastDonation.trim(),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.update(
                    mapOf(
                        "linkedUid" to linkedUid.trim(),
                        "name" to name.trim(),
                        "photoUrl" to photoUrl.trim(),
                        "phone" to phone.trim(),
                        "email" to email.trim(),
                        "bloodGroup" to bloodGroup.trim(),
                        "position" to position.trim(),
                        "joiningDate" to joiningDate.trim(),
                        "address" to address.trim(),
                        "status" to status,
                        "bloodDonor" to bloodDonor,
                        "donorAvailable" to donorAvailable,
                        "donationCount" to donationCount.coerceAtLeast(0L),
                        "lastDonation" to lastDonation.trim(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            // Sync safe non-PII card
            val dirId = linkedUid.trim().ifBlank { "mem_${docRef.id}" }
            db.collection("memberDirectory").document(dirId).set(
                mapOf(
                    "linkedUid" to linkedUid.trim(),
                    "organizationId" to orgId,
                    "name" to name.trim(),
                    "photoUrl" to photoUrl.trim(),
                    "bloodGroup" to bloodGroup.trim(),
                    "position" to position.trim(),
                    "joiningDate" to joiningDate.trim(),
                    "status" to status,
                    "volunteer" to false,
                    "skills" to "",
                    "availability" to "",
                    "bloodDonor" to bloodDonor,
                    "donorAvailable" to donorAvailable,
                    "donationCount" to donationCount.coerceAtLeast(0L),
                    "lastDonation" to lastDonation.trim(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isCreate) OperationType.CREATE else OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun toggleMemberStatus(memberId: String, newStatus: String): Result<Unit> {
        val docRef = db.collection("members").document(memberId)
        return try {
            requireUid()
            docRef.update(mapOf("status" to newStatus, "updatedAt" to FieldValue.serverTimestamp())).await()
            val dirRef = db.collection("memberDirectory").document("mem_$memberId")
            if (dirRef.get().await().exists()) {
                dirRef.update(mapOf("status" to newStatus, "updatedAt" to FieldValue.serverTimestamp())).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteMember(memberId: String): Result<Unit> {
        val docRef = db.collection("members").document(memberId)
        return try {
            requireUid()
            docRef.delete().await()
            db.collection("memberDirectory").document("mem_$memberId").delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // Volunteers
    suspend fun saveVolunteer(
        existingId: String?,
        linkedUid: String,
        name: String,
        photoUrl: String,
        phone: String,
        skills: String,
        availability: String,
        bloodGroup: String,
        joiningDate: String,
        status: String
    ): Result<Unit> {
        val isCreate = existingId.isNullOrBlank()
        val docRef = if (isCreate) db.collection("volunteers").document() else db.collection("volunteers").document(existingId!!)
        return try {
            requireUid()
            if (isCreate) {
                docRef.set(
                    mapOf(
                        "organizationId" to orgId,
                        "linkedUid" to linkedUid.trim(),
                        "name" to name.trim(),
                        "photoUrl" to photoUrl.trim(),
                        "phone" to phone.trim(),
                        "skills" to skills.trim(),
                        "availability" to availability.trim(),
                        "bloodGroup" to bloodGroup.trim(),
                        "joiningDate" to joiningDate.trim(),
                        "status" to status,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.update(
                    mapOf(
                        "linkedUid" to linkedUid.trim(),
                        "name" to name.trim(),
                        "photoUrl" to photoUrl.trim(),
                        "phone" to phone.trim(),
                        "skills" to skills.trim(),
                        "availability" to availability.trim(),
                        "bloodGroup" to bloodGroup.trim(),
                        "joiningDate" to joiningDate.trim(),
                        "status" to status,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            val dirId = linkedUid.trim().ifBlank { "vol_${docRef.id}" }
            db.collection("memberDirectory").document(dirId).set(
                mapOf(
                    "linkedUid" to linkedUid.trim(),
                    "organizationId" to orgId,
                    "name" to name.trim(),
                    "photoUrl" to photoUrl.trim(),
                    "bloodGroup" to bloodGroup.trim(),
                    "position" to "স্বেচ্ছাসেবক",
                    "joiningDate" to joiningDate.trim(),
                    "status" to status,
                    "volunteer" to true,
                    "skills" to skills.trim(),
                    "availability" to availability.trim(),
                    "bloodDonor" to false,
                    "donorAvailable" to false,
                    "donationCount" to 0L,
                    "lastDonation" to "",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isCreate) OperationType.CREATE else OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun toggleVolunteerStatus(volunteerId: String, newStatus: String): Result<Unit> {
        val docRef = db.collection("volunteers").document(volunteerId)
        return try {
            requireUid()
            docRef.update(mapOf("status" to newStatus, "updatedAt" to FieldValue.serverTimestamp())).await()
            val dirRef = db.collection("memberDirectory").document("vol_$volunteerId")
            if (dirRef.get().await().exists()) {
                dirRef.update(mapOf("status" to newStatus, "updatedAt" to FieldValue.serverTimestamp())).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteVolunteer(volunteerId: String): Result<Unit> {
        val docRef = db.collection("volunteers").document(volunteerId)
        return try {
            requireUid()
            docRef.delete().await()
            db.collection("memberDirectory").document("vol_$volunteerId").delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // Positions
    suspend fun savePosition(
        existingId: String?,
        title: String,
        rankOrder: Long,
        holderName: String,
        holderPhotoUrl: String,
        description: String
    ): Result<Unit> {
        val isCreate = existingId.isNullOrBlank()
        val docRef = if (isCreate) db.collection("positions").document() else db.collection("positions").document(existingId!!)
        return try {
            requireUid()
            docRef.set(
                mapOf(
                    "organizationId" to orgId,
                    "title" to title.trim(),
                    "rankOrder" to rankOrder,
                    "holderName" to holderName.trim(),
                    "holderPhotoUrl" to holderPhotoUrl.trim(),
                    "description" to description.trim(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deletePosition(positionId: String): Result<Unit> {
        val docRef = db.collection("positions").document(positionId)
        return try {
            requireUid()
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // Service Requests & Medical Cases
    suspend fun submitServiceRequest(
        requesterName: String,
        serviceId: String,
        serviceTitle: String,
        details: String,
        location: String,
        contactPhone: String,
        urgency: String
    ): Result<Unit> {
        val docRef = db.collection("serviceRequests").document()
        return try {
            val uid = requireUid()
            docRef.set(
                mapOf(
                    "organizationId" to orgId,
                    "userId" to uid,
                    "requesterName" to requesterName.trim(),
                    "serviceId" to serviceId,
                    "serviceTitle" to serviceTitle.trim(),
                    "details" to details.trim(),
                    "location" to location.trim(),
                    "contactPhone" to contactPhone.trim(),
                    "urgency" to urgency,
                    "status" to "pending",
                    "adminNote" to "",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateServiceRequestStatus(
        request: ServiceRequest,
        newStatus: String,
        adminNote: String
    ): Result<Unit> {
        val docRef = db.collection("serviceRequests").document(request.id)
        return try {
            requireUid()
            docRef.update(
                mapOf(
                    "status" to newStatus,
                    "adminNote" to adminNote.trim(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            emitNotification(
                targetScope = "user",
                targetUserId = request.userId,
                category = "service_update",
                title = "সেবা আবেদনের আপডেট: ${request.serviceTitle}",
                body = "অবস্থা: $newStatus। ${adminNote.trim()}"
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun submitMedicalCase(
        patientName: String,
        conditionSummary: String,
        hospitalName: String,
        estimatedCost: Double,
        contactPhone: String
    ): Result<Unit> {
        val docRef = db.collection("medicalCases").document()
        return try {
            val uid = requireUid()
            docRef.set(
                mapOf(
                    "organizationId" to orgId,
                    "userId" to uid,
                    "patientName" to patientName.trim(),
                    "conditionSummary" to conditionSummary.trim(),
                    "hospitalName" to hospitalName.trim(),
                    "estimatedCost" to estimatedCost.coerceAtLeast(0.0),
                    "contactPhone" to contactPhone.trim(),
                    "status" to "pending",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateMedicalCaseStatus(caseItem: MedicalCase, newStatus: String): Result<Unit> {
        val docRef = db.collection("medicalCases").document(caseItem.id)
        return try {
            requireUid()
            docRef.update(
                mapOf(
                    "status" to newStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            emitNotification(
                targetScope = "user",
                targetUserId = caseItem.userId,
                category = "service_update",
                title = "চিকিৎসা সহায়তার আপডেট",
                body = "রোগী ${caseItem.patientName}-এর আবেদন অবস্থা: $newStatus"
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // Blood Requests
    suspend fun createBloodRequest(
        patientName: String,
        bloodGroup: String,
        hospital: String,
        location: String,
        requiredDate: String,
        units: Long,
        contact: String,
        urgency: String,
        notes: String
    ): Result<Unit> {
        val docRef = db.collection("bloodRequests").document()
        return try {
            val uid = requireUid()
            docRef.set(
                mapOf(
                    "organizationId" to orgId,
                    "createdByUid" to uid,
                    "patientName" to patientName.trim(),
                    "bloodGroup" to bloodGroup.trim(),
                    "hospital" to hospital.trim(),
                    "location" to location.trim(),
                    "requiredDate" to requiredDate.trim(),
                    "units" to units.coerceIn(1L, 20L),
                    "contact" to contact.trim(),
                    "urgency" to urgency,
                    "status" to "open",
                    "notes" to notes.trim(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            emitNotification(
                targetScope = "all",
                targetUserId = "",
                category = "blood_request",
                title = "জরুরি রক্তের প্রয়োজন ($bloodGroup)",
                body = "রোগী: $patientName, হাসপাতাল: $hospital ($units ব্যাগ, তারিখ: $requiredDate)"
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateBloodRequestStatus(requestId: String, newStatus: String): Result<Unit> {
        val docRef = db.collection("bloodRequests").document(requestId)
        return try {
            requireUid()
            docRef.update(
                mapOf(
                    "status" to newStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteBloodRequest(requestId: String): Result<Unit> {
        val docRef = db.collection("bloodRequests").document(requestId)
        return try {
            requireUid()
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // Activities
    suspend fun saveActivity(
        existingId: String?,
        title: String,
        description: String,
        category: String,
        imageUrl: String,
        date: String,
        location: String,
        participants: Long,
        beneficiaries: Long,
        budget: Double,
        status: String
    ): Result<Unit> {
        val isCreate = existingId.isNullOrBlank()
        val docRef = if (isCreate) db.collection("activities").document() else db.collection("activities").document(existingId!!)
        return try {
            requireUid()
            if (isCreate) {
                docRef.set(
                    mapOf(
                        "organizationId" to orgId,
                        "title" to title.trim(),
                        "description" to description.trim(),
                        "category" to category.trim(),
                        "imageUrl" to imageUrl.trim(),
                        "date" to date.trim(),
                        "location" to location.trim(),
                        "participants" to participants.coerceAtLeast(0L),
                        "beneficiaries" to beneficiaries.coerceAtLeast(0L),
                        "budget" to budget.coerceAtLeast(0.0),
                        "status" to status,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.update(
                    mapOf(
                        "title" to title.trim(),
                        "description" to description.trim(),
                        "category" to category.trim(),
                        "imageUrl" to imageUrl.trim(),
                        "date" to date.trim(),
                        "location" to location.trim(),
                        "participants" to participants.coerceAtLeast(0L),
                        "beneficiaries" to beneficiaries.coerceAtLeast(0L),
                        "budget" to budget.coerceAtLeast(0.0),
                        "status" to status,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isCreate) OperationType.CREATE else OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteActivity(activityId: String): Result<Unit> {
        val docRef = db.collection("activities").document(activityId)
        return try {
            requireUid()
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // Finance: Contributions, Donations, Expenses
    suspend fun addContribution(
        memberUid: String,
        memberName: String,
        expectedAmount: Double,
        paidAmount: Double,
        month: String,
        paymentDate: String,
        paymentMethod: String
    ): Result<Unit> {
        val docRef = db.collection("contributions").document()
        return try {
            requireUid()
            val due = (expectedAmount - paidAmount).coerceAtLeast(0.0)
            docRef.set(
                mapOf(
                    "organizationId" to orgId,
                    "memberUid" to memberUid.trim(),
                    "memberName" to memberName.trim(),
                    "expectedAmount" to expectedAmount.coerceAtLeast(0.0),
                    "paidAmount" to paidAmount.coerceAtLeast(0.0),
                    "dueAmount" to due,
                    "month" to month.trim(),
                    "paymentDate" to paymentDate.trim(),
                    "paymentMethod" to paymentMethod.trim(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            if (memberUid.isNotBlank()) {
                emitNotification(
                    targetScope = "user",
                    targetUserId = memberUid.trim(),
                    category = "payment_update",
                    title = "মাসিক চাঁদা/অর্থ প্রাপ্তি ($month)",
                    body = "পরিশোধিত: ৳${paidAmount.toLong()}, বকেয়া: ৳${due.toLong()} ($paymentMethod)"
                )
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteContribution(id: String): Result<Unit> {
        val docRef = db.collection("contributions").document(id)
        return try {
            requireUid()
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun addDonation(
        donorName: String,
        amount: Double,
        purpose: String,
        donationDate: String,
        paymentMethod: String
    ): Result<Unit> {
        val docRef = db.collection("donations").document()
        return try {
            requireUid()
            docRef.set(
                mapOf(
                    "organizationId" to orgId,
                    "donorName" to donorName.trim(),
                    "amount" to amount.coerceAtLeast(0.01),
                    "purpose" to purpose.trim(),
                    "donationDate" to donationDate.trim(),
                    "paymentMethod" to paymentMethod.trim(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteDonation(id: String): Result<Unit> {
        val docRef = db.collection("donations").document(id)
        return try {
            requireUid()
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun addExpense(
        title: String,
        category: String,
        amount: Double,
        expenseDate: String,
        notes: String
    ): Result<Unit> {
        val docRef = db.collection("expenses").document()
        return try {
            requireUid()
            docRef.set(
                mapOf(
                    "organizationId" to orgId,
                    "title" to title.trim(),
                    "category" to category.trim(),
                    "amount" to amount.coerceAtLeast(0.01),
                    "expenseDate" to expenseDate.trim(),
                    "notes" to notes.trim(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteExpense(id: String): Result<Unit> {
        val docRef = db.collection("expenses").document(id)
        return try {
            requireUid()
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // Notices & Events
    suspend fun createNotice(
        title: String,
        body: String,
        noticeType: String,
        pinned: Boolean
    ): Result<Unit> {
        val docRef = db.collection("notices").document()
        return try {
            val uid = requireUid()
            docRef.set(
                mapOf(
                    "organizationId" to orgId,
                    "title" to title.trim(),
                    "body" to body.trim(),
                    "noticeType" to noticeType,
                    "pinned" to pinned,
                    "createdByUid" to uid,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            val cat = if (noticeType == "announcement") "announcement" else "notice"
            emitNotification(
                targetScope = "all",
                targetUserId = "",
                category = cat,
                title = title.trim(),
                body = body.trim().take(200)
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteNotice(id: String): Result<Unit> {
        val docRef = db.collection("notices").document(id)
        return try {
            requireUid()
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun createEvent(
        title: String,
        description: String,
        eventType: String,
        eventDate: String,
        eventTime: String,
        location: String
    ): Result<Unit> {
        val docRef = db.collection("events").document()
        return try {
            val uid = requireUid()
            docRef.set(
                mapOf(
                    "organizationId" to orgId,
                    "title" to title.trim(),
                    "description" to description.trim(),
                    "eventType" to eventType,
                    "eventDate" to eventDate.trim(),
                    "eventTime" to eventTime.trim(),
                    "location" to location.trim(),
                    "createdByUid" to uid,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            emitNotification(
                targetScope = "all",
                targetUserId = "",
                category = "event",
                title = "নতুন ইভেন্ট/সভা: ${title.trim()}",
                body = "তারিখ: $eventDate $eventTime | স্থান: $location"
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteEvent(id: String): Result<Unit> {
        val docRef = db.collection("events").document(id)
        return try {
            requireUid()
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // 1-to-1 Messaging
    suspend fun getOrCreateConversation(
        currentUser: UserProfile,
        otherUid: String,
        otherName: String,
        otherPhotoUrl: String
    ): Result<String> {
        val uid = currentUser.uid
        if (uid == otherUid) return Result.failure(IllegalArgumentException("নিজের সাথে চ্যাট করা যাবে না"))
        val sortedIds = listOf(uid, otherUid).sorted()
        val convId = "${sortedIds[0]}_${sortedIds[1]}"
        val docRef = db.collection("conversations").document(convId)
        return try {
            val snap = docRef.get().await()
            if (!snap.exists()) {
                docRef.set(
                    mapOf(
                        "organizationId" to orgId,
                        "participantIds" to sortedIds,
                        "participantNames" to mapOf(uid to currentUser.name, otherUid to otherName),
                        "participantPhotos" to mapOf(uid to currentUser.photoURL, otherUid to otherPhotoUrl),
                        "lastMessageText" to "",
                        "lastSenderId" to uid,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(convId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun sendMessage(
        conversationId: String,
        participantIds: List<String>,
        senderName: String,
        text: String,
        imageUrl: String
    ): Result<Unit> {
        val convRef = db.collection("conversations").document(conversationId)
        val msgRef = convRef.collection("messages").document()
        return try {
            val uid = requireUid()
            val trimmedText = text.trim()
            val trimmedImg = imageUrl.trim()
            require(trimmedText.isNotEmpty() || trimmedImg.isNotEmpty()) { "বার্তা বা ছবি দিন" }

            val msgPayload = mutableMapOf<String, Any>(
                "senderId" to uid,
                "participantIds" to participantIds,
                "seen" to false,
                "createdAt" to FieldValue.serverTimestamp()
            )
            if (trimmedText.isNotEmpty()) msgPayload["text"] = trimmedText
            if (trimmedImg.isNotEmpty()) msgPayload["imageUrl"] = trimmedImg

            msgRef.set(msgPayload).await()

            val preview = trimmedText.ifBlank { "ছবি পাঠানো হয়েছে" }
            convRef.update(
                mapOf(
                    "lastMessageText" to preview.take(1000),
                    "lastSenderId" to uid,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            val otherUid = participantIds.firstOrNull { it != uid }.orEmpty()
            if (otherUid.isNotBlank()) {
                emitNotification(
                    targetScope = "user",
                    targetUserId = otherUid,
                    category = "message",
                    title = "নতুন বার্তা: $senderName",
                    body = preview.take(200)
                )
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, msgRef.path)
            Result.failure(e)
        }
    }

    suspend fun markMessagesSeen(conversationId: String, messages: List<ChatMessageRecord>, currentUid: String) {
        val unread = messages.filter { !it.seen && it.senderId != currentUid && it.id.isNotBlank() }
        if (unread.isEmpty()) return
        for (msg in unread) {
            val docRef = db.collection("conversations").document(conversationId)
                .collection("messages").document(msg.id)
            try {
                docRef.update(
                    mapOf(
                        "seen" to true,
                        "seenAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            }
        }
    }
}
