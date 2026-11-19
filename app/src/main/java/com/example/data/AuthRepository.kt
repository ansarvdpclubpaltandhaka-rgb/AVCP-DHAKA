package com.example.data

import android.content.Context
import com.example.core.AppConstants
import com.example.model.MemberRecord
import com.example.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class RegisteredMemberAccount(
    val uid: String,
    val name: String,
    val phone: String,
    val normalizedPhone: String,
    val platoon: String,
    val trade: String = "",
    val status: String = AppConstants.STATUS_PENDING,
    val passwordHash: String,
    val createdAtMillis: Long
) {
    val isApprovedWithTrade: Boolean
        get() = AppConstants.isApprovedMemberStatus(status) && trade.isNotBlank()

    fun toUserProfile(): UserProfile = UserProfile(
        id = uid,
        uid = uid,
        name = name,
        email = "",
        phone = phone,
        photoURL = "",
        organizationId = AppConstants.DEFAULT_ORG_ID,
        role = AppConstants.ROLE_MEMBER,
        permissions = emptyList(),
        status = status,
        bloodGroup = "",
        positionTitle = "সদস্য",
        platoon = platoon,
        trade = trade
    )

    fun toMemberRecord(): MemberRecord = MemberRecord(
        id = uid,
        organizationId = AppConstants.DEFAULT_ORG_ID,
        linkedUid = uid,
        name = name,
        phone = phone,
        email = "",
        bloodGroup = "",
        position = "সদস্য",
        platoon = platoon,
        trade = trade,
        joiningDate = if (status == AppConstants.STATUS_PENDING) "অনুমোদন অপেক্ষমাণ" else "অনুমোদিত সদস্য",
        address = platoon,
        status = status
    )
}

class AuthRepository(context: Context? = null) {

    private val prefs = context?.applicationContext
        ?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _registeredAccounts = MutableStateFlow(loadAccounts())
    val registeredAccountsFlow: StateFlow<List<RegisteredMemberAccount>> = _registeredAccounts.asStateFlow()

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUserFlow: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    fun signInAdmin(email: String, password: String): Result<UserProfile> {
        return if (AppConstants.verifyAdminCredentials(email, password)) {
            val adminProfile = UserProfile(
                id = AppConstants.SUPER_ADMIN_UID,
                uid = AppConstants.SUPER_ADMIN_UID,
                name = "সুপার অ্যাডমিন",
                email = AppConstants.ADMIN_EMAIL,
                phone = "",
                photoURL = "",
                organizationId = AppConstants.DEFAULT_ORG_ID,
                role = AppConstants.ROLE_SUPER_ADMIN,
                permissions = AppConstants.ALL_PERMISSIONS.map { it.first },
                status = "active",
                bloodGroup = "O+",
                positionTitle = "সুপার অ্যাডমিন",
                platoon = "কেন্দ্রীয় পরিচালনা",
                trade = "দলনেতা"
            )
            _currentUser.value = adminProfile
            Result.success(adminProfile)
        } else {
            Result.failure(IllegalArgumentException("অ্যাডমিন ইমেইল বা পাসওয়ার্ড সঠিক নয়।"))
        }
    }

    fun registerMember(
        name: String,
        phone: String,
        platoon: String,
        password: String
    ): Result<RegisteredMemberAccount> {
        val cleanName = name.trim()
        val cleanPhone = phone.trim()
        val cleanPlatoon = platoon.trim()
        val normalizedPhone = AppConstants.normalizePhone(cleanPhone)

        if (cleanName.isBlank()) {
            return Result.failure(IllegalArgumentException("অনুগ্রহ করে আপনার পূর্ণ নাম লিখুন।"))
        }
        if (normalizedPhone.length < 6) {
            return Result.failure(IllegalArgumentException("সঠিক মোবাইল নম্বর লিখুন।"))
        }
        if (cleanPlatoon.isBlank()) {
            return Result.failure(IllegalArgumentException("আপনার প্লাটুনের নাম লিখুন।"))
        }
        if (password.length < 4) {
            return Result.failure(IllegalArgumentException("পাসওয়ার্ড কমপক্ষে ৪ অক্ষরের হতে হবে।"))
        }

        val currentAccounts = _registeredAccounts.value
        val existingAccount = currentAccounts.firstOrNull { it.normalizedPhone == normalizedPhone }
        if (existingAccount != null && existingAccount.status != AppConstants.STATUS_REJECTED) {
            val dupMsg = if (existingAccount.isApprovedWithTrade) {
                "এই মোবাইল নম্বর দিয়ে ইতিমধ্যে নিবন্ধন করা হয়েছে। অনুগ্রহ করে লগইন করুন।"
            } else {
                AppConstants.MSG_MEMBER_PENDING
            }
            return Result.failure(IllegalStateException(dupMsg))
        }

        val newAccount = RegisteredMemberAccount(
            uid = "member_${normalizedPhone}",
            name = cleanName,
            phone = cleanPhone,
            normalizedPhone = normalizedPhone,
            platoon = cleanPlatoon,
            trade = "", // Kept empty during registration; Admin sets Trade on approval
            status = AppConstants.STATUS_PENDING,
            passwordHash = AppConstants.hashPassword(password),
            createdAtMillis = System.currentTimeMillis()
        )
        val remaining = currentAccounts.filterNot { it.normalizedPhone == normalizedPhone }
        val updated = listOf(newAccount) + remaining
        _registeredAccounts.value = updated
        saveAccounts(updated)
        return Result.success(newAccount)
    }

    fun approveMemberAndSetTrade(uidOrId: String, trade: String): Result<RegisteredMemberAccount> {
        val cleanTrade = trade.trim()
        if (cleanTrade !in AppConstants.MEMBER_TRADES) {
            return Result.failure(IllegalArgumentException("বৈধ Trade নির্বাচন করুন: ${AppConstants.MEMBER_TRADES.joinToString(", ")}"))
        }
        var updatedAccount: RegisteredMemberAccount? = null
        val updatedList = _registeredAccounts.value.map { acc ->
            if (acc.uid == uidOrId) {
                acc.copy(
                    trade = cleanTrade,
                    status = AppConstants.STATUS_APPROVED
                ).also { updatedAccount = it }
            } else acc
        }
        val target = updatedAccount
            ?: return Result.failure(IllegalArgumentException("নিবন্ধিত সদস্য খুঁজে পাওয়া যায়নি।"))
        _registeredAccounts.value = updatedList
        saveAccounts(updatedList)
        val current = _currentUser.value
        if (current != null && current.uid == target.uid) {
            _currentUser.value = target.toUserProfile()
        }
        return Result.success(target)
    }

    fun rejectMember(uidOrId: String): Result<RegisteredMemberAccount> {
        var rejectedAccount: RegisteredMemberAccount? = null
        val updatedList = _registeredAccounts.value.map { acc ->
            if (acc.uid == uidOrId) {
                acc.copy(
                    trade = "",
                    status = AppConstants.STATUS_REJECTED
                ).also { rejectedAccount = it }
            } else acc
        }
        val target = rejectedAccount
            ?: return Result.failure(IllegalArgumentException("নিবন্ধিত সদস্য খুঁজে পাওয়া যায়নি।"))
        _registeredAccounts.value = updatedList
        saveAccounts(updatedList)
        if (_currentUser.value?.uid == target.uid) {
            _currentUser.value = null
        }
        return Result.success(target)
    }

    fun removeRegisteredMember(uidOrId: String) {
        val updated = _registeredAccounts.value.filterNot { it.uid == uidOrId }
        if (updated.size != _registeredAccounts.value.size) {
            _registeredAccounts.value = updated
            saveAccounts(updated)
        }
    }

    fun signInMember(phone: String, password: String): Result<UserProfile> {
        val normalizedPhone = AppConstants.normalizePhone(phone)
        if (normalizedPhone.length < 6 || password.isBlank()) {
            return Result.failure(IllegalArgumentException("সঠিক মোবাইল নম্বর ও পাসওয়ার্ড লিখুন।"))
        }
        val account = _registeredAccounts.value.firstOrNull { it.normalizedPhone == normalizedPhone }
            ?: return Result.failure(IllegalArgumentException("এই মোবাইল নম্বরে কোনো সদস্য অ্যাকাউন্ট পাওয়া যায়নি। প্রথমে নিবন্ধন করুন।"))

        if (account.passwordHash != AppConstants.hashPassword(password)) {
            return Result.failure(IllegalArgumentException("মোবাইল নম্বর বা পাসওয়ার্ড সঠিক নয়।"))
        }

        if (account.status.equals(AppConstants.STATUS_REJECTED, ignoreCase = true)) {
            return Result.failure(IllegalStateException(AppConstants.MSG_MEMBER_REJECTED))
        }

        if (!account.isApprovedWithTrade) {
            return Result.failure(IllegalStateException(AppConstants.MSG_MEMBER_PENDING))
        }

        val profile = account.toUserProfile()
        _currentUser.value = profile
        return Result.success(profile)
    }

    fun updateOwnProfile(name: String, phone: String, photoUrl: String, bloodGroup: String): Result<Unit> {
        val current = _currentUser.value ?: return Result.failure(IllegalStateException("লগইন করা নেই"))
        _currentUser.value = current.copy(
            name = name.trim(),
            phone = phone.trim(),
            photoURL = photoUrl.trim(),
            bloodGroup = bloodGroup.trim()
        )
        return Result.success(Unit)
    }

    fun signOut() {
        _currentUser.value = null
    }

    private fun loadAccounts(): List<RegisteredMemberAccount> {
        val raw = prefs?.getString(KEY_MEMBERS_JSON, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(
                        RegisteredMemberAccount(
                            uid = obj.getString("uid"),
                            name = obj.getString("name"),
                            phone = obj.getString("phone"),
                            normalizedPhone = obj.getString("normalizedPhone"),
                            platoon = obj.getString("platoon"),
                            trade = obj.optString("trade", ""),
                            status = obj.optString("status", AppConstants.STATUS_PENDING),
                            passwordHash = obj.getString("passwordHash"),
                            createdAtMillis = obj.optLong("createdAtMillis", 0L)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun saveAccounts(accounts: List<RegisteredMemberAccount>) {
        val editor = prefs?.edit() ?: return
        val arr = JSONArray()
        accounts.forEach { acc ->
            arr.put(
                JSONObject().apply {
                    put("uid", acc.uid)
                    put("name", acc.name)
                    put("phone", acc.phone)
                    put("normalizedPhone", acc.normalizedPhone)
                    put("platoon", acc.platoon)
                    put("trade", acc.trade)
                    put("status", acc.status)
                    put("passwordHash", acc.passwordHash)
                    put("createdAtMillis", acc.createdAtMillis)
                }
            )
        }
        editor.putString(KEY_MEMBERS_JSON, arr.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "ansar_vdp_member_accounts"
        private const val KEY_MEMBERS_JSON = "members_json"
    }
}
