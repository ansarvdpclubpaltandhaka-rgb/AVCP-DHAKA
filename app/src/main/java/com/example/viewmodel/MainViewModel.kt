package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.AppConstants
import com.example.core.UiState
import com.example.data.DemoDataProvider
import com.example.data.OrgRepository
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: OrgRepository,
    val currentUid: String,
    val isAuthenticated: Boolean,
    val isAdmin: Boolean,
    val isSuperAdmin: Boolean,
    val currentUserName: String = "",
    val currentUserPhone: String = ""
) : ViewModel() {

    private val localDirectory = MutableStateFlow(DemoDataProvider.demoDirectory)
    private val localMembers = MutableStateFlow(DemoDataProvider.demoMembers)
    private val localVolunteers = MutableStateFlow(DemoDataProvider.demoVolunteers)
    private val localPositions = MutableStateFlow(DemoDataProvider.demoPositions)
    private val localServices = MutableStateFlow(DemoDataProvider.demoServices)
    private val localActivities = MutableStateFlow(DemoDataProvider.demoActivities)
    private val localNotices = MutableStateFlow(DemoDataProvider.demoNotices)
    private val localEvents = MutableStateFlow(DemoDataProvider.demoEvents)
    private val localBloodRequests = MutableStateFlow(DemoDataProvider.demoBloodRequests)
    private val localAllowances = MutableStateFlow(DemoDataProvider.demoAllowances)
    private val localOrgConfig = MutableStateFlow(DemoDataProvider.demoOrganizationConfig)

    private val localContributions = MutableStateFlow<List<ContributionRecord>>(emptyList())
    private val localDonations = MutableStateFlow<List<DonationRecord>>(emptyList())
    private val localExpenses = MutableStateFlow<List<ExpenseRecord>>(emptyList())
    private val localServiceRequests = MutableStateFlow<List<ServiceRequest>>(emptyList())
    private val localMedicalCases = MutableStateFlow<List<MedicalCase>>(emptyList())
    private val localConversations = MutableStateFlow<List<ConversationRecord>>(emptyList())
    private val localNotifications = MutableStateFlow<List<NotificationRecord>>(emptyList())

    val orgConfig: StateFlow<OrganizationConfig?> = localOrgConfig.asStateFlow()

    private fun isOwnDirectoryEntry(entry: DirectoryEntry): Boolean =
        isAuthenticated && (
            (currentUid.isNotBlank() && (entry.linkedUid == currentUid || entry.id == currentUid)) ||
                (currentUserName.isNotBlank() && entry.name.equals(currentUserName.trim(), ignoreCase = true))
            )

    private fun isOwnAllowanceRecord(record: AllowanceRecord): Boolean {
        if (!isAuthenticated) return false
        val normalizedCurrentPhone = AppConstants.normalizePhone(currentUserPhone)
        val normalizedRecordPhone = AppConstants.normalizePhone(record.memberPhone)
        return (currentUid.isNotBlank() && record.memberUid == currentUid) ||
            (normalizedCurrentPhone.isNotBlank() && normalizedRecordPhone == normalizedCurrentPhone) ||
            (currentUserName.isNotBlank() && record.memberName.equals(currentUserName.trim(), ignoreCase = true))
    }

    val directoryState: StateFlow<UiState<List<DirectoryEntry>>> =
        if (isAdmin) {
            localDirectory.map<List<DirectoryEntry>, UiState<List<DirectoryEntry>>> { UiState.Success(it) }
                .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Success(DemoDataProvider.demoDirectory))
        } else if (isAuthenticated) {
            localDirectory.map<List<DirectoryEntry>, UiState<List<DirectoryEntry>>> { list ->
                UiState.Success(list.filter { isOwnDirectoryEntry(it) })
            }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Success(emptyList()))
        } else {
            MutableStateFlow(UiState.Success(emptyList()))
        }

    val positionsState: StateFlow<UiState<List<PositionRecord>>> =
        if (isAdmin) {
            localPositions.map<List<PositionRecord>, UiState<List<PositionRecord>>> { list ->
                UiState.Success(list.sortedBy { it.rankOrder })
            }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Success(DemoDataProvider.demoPositions))
        } else {
            MutableStateFlow(UiState.Success(emptyList()))
        }

    val servicesState: StateFlow<UiState<List<ServiceCategory>>> =
        localServices.map<List<ServiceCategory>, UiState<List<ServiceCategory>>> { list ->
            UiState.Success(list.sortedBy { it.sortOrder })
        }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Success(DemoDataProvider.demoServices))

    val bloodRequestsState: StateFlow<UiState<List<BloodRequest>>> =
        if (isAdmin) {
            localBloodRequests.map<List<BloodRequest>, UiState<List<BloodRequest>>> { UiState.Success(it) }
                .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Success(DemoDataProvider.demoBloodRequests))
        } else {
            MutableStateFlow(UiState.Success(emptyList()))
        }

    val activitiesState: StateFlow<UiState<List<ActivityRecord>>> =
        localActivities.map<List<ActivityRecord>, UiState<List<ActivityRecord>>> { UiState.Success(it) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Success(DemoDataProvider.demoActivities))

    val noticesState: StateFlow<UiState<List<NoticeRecord>>> =
        localNotices.map<List<NoticeRecord>, UiState<List<NoticeRecord>>> { list ->
            UiState.Success(list.sortedByDescending { it.pinned })
        }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Success(DemoDataProvider.demoNotices))

    val eventsState: StateFlow<UiState<List<OrgEventRecord>>> =
        localEvents.map<List<OrgEventRecord>, UiState<List<OrgEventRecord>>> { UiState.Success(it) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Success(DemoDataProvider.demoEvents))

    val allowancesState: StateFlow<UiState<List<AllowanceRecord>>> =
        if (isAdmin) {
            localAllowances.map<List<AllowanceRecord>, UiState<List<AllowanceRecord>>> { UiState.Success(it) }
                .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Success(DemoDataProvider.demoAllowances))
        } else if (isAuthenticated) {
            localAllowances.map<List<AllowanceRecord>, UiState<List<AllowanceRecord>>> { list ->
                UiState.Success(list.filter { isOwnAllowanceRecord(it) })
            }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Success(emptyList()))
        } else {
            MutableStateFlow(UiState.Success(emptyList()))
        }

    val myServiceRequestsState: StateFlow<UiState<List<ServiceRequest>>> =
        localServiceRequests.map<List<ServiceRequest>, UiState<List<ServiceRequest>>> { UiState.Success(it) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Success(emptyList()))

    val myMedicalCasesState: StateFlow<UiState<List<MedicalCase>>> =
        localMedicalCases.map<List<MedicalCase>, UiState<List<MedicalCase>>> { UiState.Success(it) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Success(emptyList()))

    val myContributionsState: StateFlow<UiState<List<ContributionRecord>>> =
        localContributions.map<List<ContributionRecord>, UiState<List<ContributionRecord>>> { UiState.Success(it) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Success(emptyList()))

    val conversationsState: StateFlow<UiState<List<ConversationRecord>>> =
        localConversations.map<List<ConversationRecord>, UiState<List<ConversationRecord>>> { UiState.Success(it) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Success(emptyList()))

    val notificationsState: StateFlow<UiState<List<NotificationRecord>>> =
        localNotifications.map<List<NotificationRecord>, UiState<List<NotificationRecord>>> { UiState.Success(it) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Success(emptyList()))

    val adminMembersState: StateFlow<UiState<List<MemberRecord>>> =
        if (isAdmin) {
            localMembers.map<List<MemberRecord>, UiState<List<MemberRecord>>> { UiState.Success(it) }
                .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Success(DemoDataProvider.demoMembers))
        } else {
            MutableStateFlow(UiState.Success(emptyList()))
        }

    val adminVolunteersState: StateFlow<UiState<List<VolunteerRecord>>> =
        if (isAdmin) {
            localVolunteers.map<List<VolunteerRecord>, UiState<List<VolunteerRecord>>> { UiState.Success(it) }
                .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Success(DemoDataProvider.demoVolunteers))
        } else {
            MutableStateFlow(UiState.Success(emptyList()))
        }

    val adminServiceRequestsState: StateFlow<UiState<List<ServiceRequest>>> = myServiceRequestsState
    val adminMedicalCasesState: StateFlow<UiState<List<MedicalCase>>> = myMedicalCasesState
    val adminContributionsState: StateFlow<UiState<List<ContributionRecord>>> = myContributionsState
    val adminDonationsState: StateFlow<UiState<List<DonationRecord>>> =
        localDonations.map<List<DonationRecord>, UiState<List<DonationRecord>>> { UiState.Success(it) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Success(emptyList()))
    val adminExpensesState: StateFlow<UiState<List<ExpenseRecord>>> =
        localExpenses.map<List<ExpenseRecord>, UiState<List<ExpenseRecord>>> { UiState.Success(it) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Success(emptyList()))
    val adminUsersState: StateFlow<UiState<List<UserProfile>>> = MutableStateFlow(UiState.Success(emptyList()))

    val financialSummary: StateFlow<FinancialSummary> =
        if (isAdmin) {
            combine(localContributions, localDonations, localExpenses) { cList, dList, eList ->
                FinancialSummary(
                    totalContributions = cList.sumOf { it.paidAmount },
                    totalDonations = dList.sumOf { it.amount },
                    totalExpenses = eList.sumOf { it.amount }
                )
            }.stateIn(viewModelScope, SharingStarted.Eagerly, FinancialSummary())
        } else {
            MutableStateFlow(FinancialSummary())
        }

    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeMessagesState: StateFlow<UiState<List<ChatMessageRecord>>> =
        MutableStateFlow(UiState.Success(emptyList()))

    private val _actionFeedback = MutableStateFlow<String?>(null)
    val actionFeedback: StateFlow<String?> = _actionFeedback.asStateFlow()

    fun clearFeedback() {
        _actionFeedback.value = null
    }

    private fun requireAdmin(): Boolean {
        if (!isAdmin) {
            _actionFeedback.value = "শুধুমাত্র অ্যাডমিন এই কাজটি করতে পারবেন (View Only)"
            return false
        }
        return true
    }

    fun syncRegisteredMembers(accounts: List<com.example.data.RegisteredMemberAccount>) {
        if (accounts.isEmpty()) return
        val rejectedIds = accounts
            .filter { it.status.equals(AppConstants.STATUS_REJECTED, ignoreCase = true) }
            .map { it.uid }
            .toSet()
        val visibleAccounts = accounts.filterNot { it.uid in rejectedIds }

        val accountRecords = visibleAccounts.map { it.toMemberRecord() }
        val accountMap = accountRecords.associateBy { it.id }

        val updatedExistingMembers = localMembers.value
            .filterNot { it.id in rejectedIds }
            .map { existing -> accountMap[existing.id] ?: existing }
        val existingIds = updatedExistingMembers.map { it.id }.toSet()
        val newlyAddedMembers = accountRecords.filter { it.id !in existingIds }
        localMembers.value = newlyAddedMembers + updatedExistingMembers

        val dirRecords = accountRecords.map { m ->
            DirectoryEntry(
                id = m.id,
                linkedUid = m.linkedUid,
                organizationId = m.organizationId,
                name = m.name,
                photoUrl = m.photoUrl,
                bloodGroup = m.bloodGroup,
                position = m.position,
                platoon = m.platoon,
                trade = m.trade,
                joiningDate = m.joiningDate,
                status = m.status
            )
        }
        val dirMap = dirRecords.associateBy { it.id }
        val updatedExistingDir = localDirectory.value
            .filterNot { it.id in rejectedIds }
            .map { existing -> dirMap[existing.id] ?: existing }
        val existingDirIds = updatedExistingDir.map { it.id }.toSet()
        val newlyAddedDir = dirRecords.filter { it.id !in existingDirIds }
        localDirectory.value = newlyAddedDir + updatedExistingDir
    }

    fun approveMemberWithTrade(memberId: String, trade: String) {
        if (!requireAdmin()) return
        val cleanTrade = trade.trim()
        if (cleanTrade !in AppConstants.MEMBER_TRADES) {
            _actionFeedback.value = "বৈধ Trade নির্বাচন করুন"
            return
        }
        localMembers.value = localMembers.value.map { m ->
            if (m.id == memberId) {
                m.copy(
                    status = AppConstants.STATUS_APPROVED,
                    trade = cleanTrade,
                    joiningDate = if (m.joiningDate == "অনুমোদন অপেক্ষমাণ" || m.joiningDate.isBlank()) "অনুমোদিত সদস্য" else m.joiningDate
                )
            } else m
        }
        localDirectory.value = localDirectory.value.map { d ->
            if (d.id == memberId) {
                d.copy(
                    status = AppConstants.STATUS_APPROVED,
                    trade = cleanTrade
                )
            } else d
        }
        _actionFeedback.value = "সদস্য অনুমোদিত হয়েছে এবং Trade ($cleanTrade) সেট করা হয়েছে"
    }

    fun rejectMember(memberId: String) {
        if (!requireAdmin()) return
        localMembers.value = localMembers.value.filterNot { it.id == memberId }
        localDirectory.value = localDirectory.value.filterNot { it.id == memberId }
        _actionFeedback.value = "সদস্যের আবেদন বাতিল (Reject) করা হয়েছে"
    }

    fun toggleAllowancePaidStatus(allowanceId: String) {
        if (!requireAdmin()) return
        localAllowances.value = localAllowances.value.map { record ->
            if (record.id == allowanceId) record.copy(paid = !record.paid) else record
        }
        _actionFeedback.value = "ভাতার পেমেন্ট স্ট্যাটাস হালনাগাদ হয়েছে"
    }

    fun updateAllowanceRecord(
        allowanceId: String,
        memberName: String,
        areaOrDuty: String,
        date: String,
        dailyAmount: Long,
        monthlyTotal: Long,
        paid: Boolean
    ) {
        if (!requireAdmin()) return
        localAllowances.value = localAllowances.value.map { record ->
            if (record.id == allowanceId) {
                record.copy(
                    memberName = memberName.trim(),
                    areaOrDuty = areaOrDuty.trim(),
                    date = date.trim(),
                    dailyAmount = dailyAmount.coerceAtLeast(0L),
                    monthlyTotal = monthlyTotal.coerceAtLeast(0L),
                    paid = paid
                )
            } else record
        }
        _actionFeedback.value = "ভাতার তথ্য সফলভাবে হালনাগাদ হয়েছে"
    }

    fun openConversation(conversationId: String) {
        _activeConversationId.value = conversationId
    }

    fun startChatWith(
        currentUser: UserProfile,
        otherUid: String,
        otherName: String,
        otherPhotoUrl: String,
        onReady: (String) -> Unit
    ) {
        _actionFeedback.value = "সরাসরি যোগাযোগের জন্য সদস্যের মোবাইল নম্বরে কল করুন"
    }

    fun sendMessage(
        conversationId: String,
        participantIds: List<String>,
        senderName: String,
        text: String,
        imageUrl: String
    ) { }

    fun markMessagesSeen(conversationId: String, messages: List<ChatMessageRecord>) { }

    fun markNotificationRead(id: String) {
        localNotifications.value = localNotifications.value.map {
            if (it.id == id && currentUid !in it.readBy) it.copy(readBy = it.readBy + currentUid) else it
        }
    }

    fun submitServiceRequest(
        requesterName: String,
        serviceId: String,
        serviceTitle: String,
        details: String,
        location: String,
        contactPhone: String,
        urgency: String
    ) {
        val item = ServiceRequest(
            id = "sr_${System.currentTimeMillis()}",
            organizationId = AppConstants.DEFAULT_ORG_ID,
            userId = currentUid,
            requesterName = requesterName.ifBlank { "সদস্য" },
            serviceId = serviceId,
            serviceTitle = serviceTitle,
            details = details,
            location = location,
            contactPhone = contactPhone,
            urgency = urgency,
            status = "pending"
        )
        localServiceRequests.value = listOf(item) + localServiceRequests.value
        _actionFeedback.value = "সেবার আবেদন জমা হয়েছে"
    }

    fun submitMedicalCase(
        patientName: String,
        conditionSummary: String,
        hospitalName: String,
        estimatedCost: Double,
        contactPhone: String
    ) {
        val item = MedicalCase(
            id = "mc_${System.currentTimeMillis()}",
            organizationId = AppConstants.DEFAULT_ORG_ID,
            userId = currentUid,
            patientName = patientName,
            conditionSummary = conditionSummary,
            hospitalName = hospitalName,
            estimatedCost = estimatedCost,
            contactPhone = contactPhone,
            status = "pending"
        )
        localMedicalCases.value = listOf(item) + localMedicalCases.value
        _actionFeedback.value = "চিকিৎসা সহায়তার আবেদন জমা হয়েছে"
    }

    fun createBloodRequest(
        patientName: String,
        bloodGroup: String,
        hospital: String,
        location: String,
        requiredDate: String,
        units: Long,
        contact: String,
        urgency: String,
        notes: String
    ) {
        val item = BloodRequest(
            id = "br_${System.currentTimeMillis()}",
            organizationId = AppConstants.DEFAULT_ORG_ID,
            createdByUid = currentUid,
            patientName = patientName,
            bloodGroup = bloodGroup,
            hospital = hospital,
            location = location,
            requiredDate = requiredDate,
            units = units,
            contact = contact,
            urgency = urgency,
            status = "open",
            notes = notes
        )
        localBloodRequests.value = listOf(item) + localBloodRequests.value
        _actionFeedback.value = "রক্তের আবেদন যুক্ত হয়েছে"
    }

    fun updateBloodRequestStatus(requestId: String, newStatus: String) {
        if (!requireAdmin()) return
        localBloodRequests.value = localBloodRequests.value.map {
            if (it.id == requestId) it.copy(status = newStatus) else it
        }
        _actionFeedback.value = "রক্তের আবেদনের অবস্থা হালনাগাদ হয়েছে"
    }

    fun deleteBloodRequest(requestId: String) {
        if (!requireAdmin()) return
        localBloodRequests.value = localBloodRequests.value.filterNot { it.id == requestId }
        _actionFeedback.value = "রক্তের আবেদন মুছে ফেলা হয়েছে"
    }

    fun saveServiceCategory(
        existingId: String?,
        title: String,
        description: String,
        iconKey: String,
        active: Boolean,
        sortOrder: Long
    ) {
        if (!requireAdmin()) return
        val id = existingId?.takeIf { it.isNotBlank() } ?: "srv_${System.currentTimeMillis()}"
        val item = ServiceCategory(
            id = id, organizationId = AppConstants.DEFAULT_ORG_ID,
            title = title.trim(), description = description.trim(),
            iconKey = iconKey, active = active, sortOrder = sortOrder
        )
        localServices.value = if (existingId.isNullOrBlank()) {
            localServices.value + item
        } else {
            localServices.value.map { if (it.id == id) item else it }
        }
        _actionFeedback.value = "সেবার ধরন সংরক্ষিত হয়েছে"
    }

    fun deleteServiceCategory(id: String) {
        if (!requireAdmin()) return
        localServices.value = localServices.value.filterNot { it.id == id }
        _actionFeedback.value = "সেবার ধরন মুছে ফেলা হয়েছে"
    }

    fun saveMember(
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
        lastDonation: String,
        platoon: String = "",
        trade: String = ""
    ) {
        if (!requireAdmin()) return
        val id = existingId?.takeIf { it.isNotBlank() } ?: "mem_${System.currentTimeMillis()}"
        val member = MemberRecord(
            id = id, organizationId = AppConstants.DEFAULT_ORG_ID,
            linkedUid = linkedUid.ifBlank { id }, name = name.trim(), photoUrl = photoUrl.trim(),
            phone = phone.trim(), email = email.trim(), bloodGroup = bloodGroup.trim(),
            position = position.trim(), platoon = platoon.trim(), trade = trade.trim(),
            joiningDate = joiningDate.trim(), address = address.trim(),
            status = status, bloodDonor = bloodDonor, donorAvailable = donorAvailable,
            donationCount = donationCount, lastDonation = lastDonation.trim()
        )
        val dir = DirectoryEntry(
            id = id, linkedUid = member.linkedUid, organizationId = AppConstants.DEFAULT_ORG_ID,
            name = member.name, photoUrl = member.photoUrl, bloodGroup = member.bloodGroup,
            position = member.position, platoon = member.platoon, trade = member.trade,
            joiningDate = member.joiningDate, status = member.status,
            volunteer = false, bloodDonor = bloodDonor, donorAvailable = donorAvailable,
            donationCount = donationCount, lastDonation = member.lastDonation
        )
        if (existingId.isNullOrBlank()) {
            localMembers.value = listOf(member) + localMembers.value
            localDirectory.value = listOf(dir) + localDirectory.value
        } else {
            localMembers.value = localMembers.value.map { if (it.id == id) member else it }
            localDirectory.value = localDirectory.value.map { if (it.id == id) dir else it }
        }
        viewModelScope.launch {
            runCatching {
                repository.saveMember(
                    existingId, linkedUid, name, photoUrl, phone, email, bloodGroup,
                    position, joiningDate, address, status, bloodDonor, donorAvailable, donationCount, lastDonation
                )
            }
        }
        _actionFeedback.value = "সদস্যের তথ্য সংরক্ষিত হয়েছে"
    }

    fun toggleMemberStatus(memberId: String, newStatus: String) {
        if (!requireAdmin()) return
        localMembers.value = localMembers.value.map { if (it.id == memberId) it.copy(status = newStatus) else it }
        localDirectory.value = localDirectory.value.map { if (it.id == memberId) it.copy(status = newStatus) else it }
        _actionFeedback.value = "সদস্যের অবস্থা পরিবর্তন হয়েছে"
    }

    fun deleteMember(memberId: String) {
        if (!requireAdmin()) return
        localMembers.value = localMembers.value.filterNot { it.id == memberId }
        localDirectory.value = localDirectory.value.filterNot { it.id == memberId }
        viewModelScope.launch { runCatching { repository.deleteMember(memberId) } }
        _actionFeedback.value = "সদস্য মুছে ফেলা হয়েছে"
    }

    fun saveVolunteer(
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
    ) {
        if (!requireAdmin()) return
        val id = existingId?.takeIf { it.isNotBlank() } ?: "vol_${System.currentTimeMillis()}"
        val vol = VolunteerRecord(
            id = id, organizationId = AppConstants.DEFAULT_ORG_ID, linkedUid = linkedUid,
            name = name.trim(), photoUrl = photoUrl.trim(), phone = phone.trim(),
            skills = skills.trim(), availability = availability.trim(), bloodGroup = bloodGroup.trim(),
            joiningDate = joiningDate.trim(), status = status
        )
        localVolunteers.value = if (existingId.isNullOrBlank()) {
            listOf(vol) + localVolunteers.value
        } else {
            localVolunteers.value.map { if (it.id == id) vol else it }
        }
        _actionFeedback.value = "স্বেচ্ছাসেবকের তথ্য সংরক্ষিত হয়েছে"
    }

    fun toggleVolunteerStatus(volunteerId: String, newStatus: String) {
        if (!requireAdmin()) return
        localVolunteers.value = localVolunteers.value.map { if (it.id == volunteerId) it.copy(status = newStatus) else it }
        _actionFeedback.value = "স্বেচ্ছাসেবকের অবস্থা পরিবর্তন হয়েছে"
    }

    fun deleteVolunteer(volunteerId: String) {
        if (!requireAdmin()) return
        localVolunteers.value = localVolunteers.value.filterNot { it.id == volunteerId }
        _actionFeedback.value = "স্বেচ্ছাসেবক মুছে ফেলা হয়েছে"
    }

    fun savePosition(
        existingId: String?,
        title: String,
        rankOrder: Long,
        holderName: String,
        holderPhotoUrl: String,
        description: String
    ) {
        if (!requireAdmin()) return
        val id = existingId?.takeIf { it.isNotBlank() } ?: "pos_${System.currentTimeMillis()}"
        val pos = PositionRecord(
            id = id, organizationId = AppConstants.DEFAULT_ORG_ID,
            title = title.trim(), rankOrder = rankOrder,
            holderName = holderName.trim(), holderPhotoUrl = holderPhotoUrl.trim(),
            description = description.trim()
        )
        localPositions.value = if (existingId.isNullOrBlank()) {
            localPositions.value + pos
        } else {
            localPositions.value.map { if (it.id == id) pos else it }
        }
        _actionFeedback.value = "সাংগঠনিক পদবি সংরক্ষিত হয়েছে"
    }

    fun deletePosition(id: String) {
        if (!requireAdmin()) return
        localPositions.value = localPositions.value.filterNot { it.id == id }
        _actionFeedback.value = "পদবি মুছে ফেলা হয়েছে"
    }

    fun saveActivity(
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
    ) {
        if (!requireAdmin()) return
        val id = existingId?.takeIf { it.isNotBlank() } ?: "act_${System.currentTimeMillis()}"
        val item = ActivityRecord(
            id = id, organizationId = AppConstants.DEFAULT_ORG_ID,
            title = title.trim(), description = description.trim(), category = category.trim(),
            imageUrl = imageUrl.trim(), date = date.trim(), location = location.trim(),
            participants = participants, beneficiaries = beneficiaries, budget = budget, status = status
        )
        localActivities.value = if (existingId.isNullOrBlank()) {
            listOf(item) + localActivities.value
        } else {
            localActivities.value.map { if (it.id == id) item else it }
        }
        _actionFeedback.value = "মানবিক কার্যক্রম সংরক্ষিত হয়েছে"
    }

    fun deleteActivity(id: String) {
        if (!requireAdmin()) return
        localActivities.value = localActivities.value.filterNot { it.id == id }
        _actionFeedback.value = "কার্যক্রম মুছে ফেলা হয়েছে"
    }

    fun addContribution(
        memberUid: String,
        memberName: String,
        expectedAmount: Double,
        paidAmount: Double,
        month: String,
        paymentDate: String,
        paymentMethod: String
    ) {
        if (!requireAdmin()) return
        val item = ContributionRecord(
            id = "con_${System.currentTimeMillis()}", organizationId = AppConstants.DEFAULT_ORG_ID,
            memberUid = memberUid, memberName = memberName.trim(),
            expectedAmount = expectedAmount, paidAmount = paidAmount,
            dueAmount = (expectedAmount - paidAmount).coerceAtLeast(0.0),
            month = month.trim(), paymentDate = paymentDate.trim(), paymentMethod = paymentMethod
        )
        localContributions.value = listOf(item) + localContributions.value
        _actionFeedback.value = "সদস্যের চাঁদা/অবদান সংরক্ষিত হয়েছে"
    }

    fun deleteContribution(id: String) {
        if (!requireAdmin()) return
        localContributions.value = localContributions.value.filterNot { it.id == id }
        _actionFeedback.value = "চাঁদার রেকর্ড মুছে ফেলা হয়েছে"
    }

    fun addDonation(
        donorName: String,
        amount: Double,
        purpose: String,
        donationDate: String,
        paymentMethod: String
    ) {
        if (!requireAdmin()) return
        val item = DonationRecord(
            id = "don_${System.currentTimeMillis()}", organizationId = AppConstants.DEFAULT_ORG_ID,
            donorName = donorName.trim(), amount = amount, purpose = purpose.trim(),
            donationDate = donationDate.trim(), paymentMethod = paymentMethod
        )
        localDonations.value = listOf(item) + localDonations.value
        _actionFeedback.value = "অনুদান সংরক্ষিত হয়েছে"
    }

    fun deleteDonation(id: String) {
        if (!requireAdmin()) return
        localDonations.value = localDonations.value.filterNot { it.id == id }
        _actionFeedback.value = "অনুদান মুছে ফেলা হয়েছে"
    }

    fun addExpense(
        title: String,
        category: String,
        amount: Double,
        expenseDate: String,
        notes: String
    ) {
        if (!requireAdmin()) return
        val item = ExpenseRecord(
            id = "exp_${System.currentTimeMillis()}", organizationId = AppConstants.DEFAULT_ORG_ID,
            title = title.trim(), category = category.trim(), amount = amount,
            expenseDate = expenseDate.trim(), notes = notes.trim()
        )
        localExpenses.value = listOf(item) + localExpenses.value
        _actionFeedback.value = "ব্যয় সংরক্ষিত হয়েছে"
    }

    fun deleteExpense(id: String) {
        if (!requireAdmin()) return
        localExpenses.value = localExpenses.value.filterNot { it.id == id }
        _actionFeedback.value = "ব্যয় মুছে ফেলা হয়েছে"
    }

    fun createNotice(title: String, body: String, noticeType: String, pinned: Boolean) {
        if (!requireAdmin()) return
        val item = NoticeRecord(
            id = "not_${System.currentTimeMillis()}", organizationId = AppConstants.DEFAULT_ORG_ID,
            title = title.trim(), body = body.trim(), noticeType = noticeType,
            pinned = pinned, createdByUid = currentUid
        )
        localNotices.value = listOf(item) + localNotices.value
        viewModelScope.launch { runCatching { repository.createNotice(title, body, noticeType, pinned) } }
        _actionFeedback.value = "নোটিশ প্রকাশিত হয়েছে"
    }

    fun deleteNotice(id: String) {
        if (!requireAdmin()) return
        localNotices.value = localNotices.value.filterNot { it.id == id }
        viewModelScope.launch { runCatching { repository.deleteNotice(id) } }
        _actionFeedback.value = "নোটিশ মুছে ফেলা হয়েছে"
    }

    fun createEvent(
        title: String,
        description: String,
        eventType: String,
        eventDate: String,
        eventTime: String,
        location: String
    ) {
        if (!requireAdmin()) return
        val item = OrgEventRecord(
            id = "ev_${System.currentTimeMillis()}", organizationId = AppConstants.DEFAULT_ORG_ID,
            title = title.trim(), description = description.trim(), eventType = eventType,
            eventDate = eventDate.trim(), eventTime = eventTime.trim(), location = location.trim(),
            createdByUid = currentUid
        )
        localEvents.value = listOf(item) + localEvents.value
        _actionFeedback.value = "ইভেন্ট/সভা সংরক্ষিত হয়েছে"
    }

    fun deleteEvent(id: String) {
        if (!requireAdmin()) return
        localEvents.value = localEvents.value.filterNot { it.id == id }
        _actionFeedback.value = "ইভেন্ট মুছে ফেলা হয়েছে"
    }

    fun updateServiceRequestStatus(request: ServiceRequest, newStatus: String, adminNote: String) {
        if (!requireAdmin()) return
        localServiceRequests.value = localServiceRequests.value.map {
            if (it.id == request.id) it.copy(status = newStatus, adminNote = adminNote) else it
        }
        _actionFeedback.value = "সেবা আবেদন হালনাগাদ হয়েছে"
    }

    fun updateMedicalCaseStatus(caseItem: MedicalCase, newStatus: String) {
        if (!requireAdmin()) return
        localMedicalCases.value = localMedicalCases.value.map {
            if (it.id == caseItem.id) it.copy(status = newStatus) else it
        }
        _actionFeedback.value = "চিকিৎসা আবেদন হালনাগাদ হয়েছে"
    }

    fun updateUserRoleAndPermissions(
        targetUid: String,
        role: String,
        permissions: List<String>,
        status: String,
        positionTitle: String
    ) {
        _actionFeedback.value = "স্থানীয় অ্যাডমিন মোডে ভূমিকা আপডেট প্রযোজ্য নয়"
    }

    fun saveOrganizationConfig(
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
    ) {
        if (!requireAdmin()) return
        localOrgConfig.value = localOrgConfig.value.copy(
            name = name.trim(), tagline = tagline.trim(), clubName = clubName.trim(),
            address = address.trim(), phone = phone.trim(), email = email.trim(),
            imageUploadProvider = imageUploadProvider,
            cloudinaryCloudName = cloudinaryCloudName.trim(),
            cloudinaryUploadPreset = cloudinaryUploadPreset.trim(),
            customUploadUrl = customUploadUrl.trim()
        )
        _actionFeedback.value = "সংগঠন সেটিংস সংরক্ষিত হয়েছে"
    }
}
