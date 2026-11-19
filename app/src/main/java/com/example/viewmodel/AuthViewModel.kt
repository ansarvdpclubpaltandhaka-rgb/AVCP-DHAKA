package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.UiState
import com.example.data.AuthRepository
import com.example.data.RegisteredMemberAccount
import com.example.model.UserProfile
import kotlinx.coroutines.flow.*

sealed interface AuthSessionState {
    data object Unauthenticated : AuthSessionState
    data class Authenticated(val user: UserProfile) : AuthSessionState
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application.applicationContext)

    val registeredAccounts: StateFlow<List<RegisteredMemberAccount>> =
        authRepository.registeredAccountsFlow

    val sessionState: StateFlow<AuthSessionState> = authRepository.currentUserFlow
        .map { user ->
            if (user == null) AuthSessionState.Unauthenticated
            else AuthSessionState.Authenticated(user)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = AuthSessionState.Unauthenticated
        )

    val userProfileState: StateFlow<UiState<UserProfile?>> = authRepository.currentUserFlow
        .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Success(null)
        )

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _authMessage = MutableStateFlow<String?>(null)
    val authMessage: StateFlow<String?> = _authMessage.asStateFlow()

    private val _isSuccessMessage = MutableStateFlow(false)
    val isSuccessMessage: StateFlow<Boolean> = _isSuccessMessage.asStateFlow()

    private val _loginSucceededEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val loginSucceededEvent: SharedFlow<Unit> = _loginSucceededEvent.asSharedFlow()

    private val _memberRegisteredEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val memberRegisteredEvent: SharedFlow<String> = _memberRegisteredEvent.asSharedFlow()

    fun clearMessage() {
        _authMessage.value = null
        _isSuccessMessage.value = false
    }

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _isSuccessMessage.value = false
            _authMessage.value = "অ্যাডমিন ইমেইল এবং পাসওয়ার্ড লিখুন"
            return
        }
        _authMessage.value = null
        _isSuccessMessage.value = false
        val res = authRepository.signInAdmin(email, password)
        res.onSuccess {
            _loginSucceededEvent.tryEmit(Unit)
        }.onFailure { e ->
            _authMessage.value = e.localizedMessage ?: "অ্যাডমিন ইমেইল বা পাসওয়ার্ড সঠিক নয়।"
        }
    }

    fun registerMember(name: String, phone: String, platoon: String, password: String) {
        _authMessage.value = null
        _isSuccessMessage.value = false
        val res = authRepository.registerMember(name, phone, platoon, password)
        res.onSuccess { account ->
            _isSuccessMessage.value = true
            _authMessage.value = com.example.core.AppConstants.MSG_MEMBER_PENDING
            _memberRegisteredEvent.tryEmit(account.phone)
        }.onFailure { e ->
            _isSuccessMessage.value = false
            _authMessage.value = e.localizedMessage ?: "নিবন্ধন সম্পন্ন করা যায়নি।"
        }
    }

    fun approveMemberAndSetTrade(uidOrId: String, trade: String) {
        val res = authRepository.approveMemberAndSetTrade(uidOrId, trade)
        _isSuccessMessage.value = res.isSuccess
        _authMessage.value = res.fold(
            onSuccess = { "${it.name} অনুমোদিত হয়েছে (Trade: ${it.trade})" },
            onFailure = { it.localizedMessage ?: "অনুমোদন ব্যর্থ হয়েছে" }
        )
    }

    fun rejectMember(uidOrId: String) {
        val res = authRepository.rejectMember(uidOrId)
        _isSuccessMessage.value = res.isSuccess
        _authMessage.value = res.fold(
            onSuccess = { "${it.name}-এর আবেদন বাতিল (Reject) করা হয়েছে" },
            onFailure = { it.localizedMessage ?: "বাতিল করা যায়নি" }
        )
    }

    fun removeRegisteredMember(uidOrId: String) {
        authRepository.removeRegisteredMember(uidOrId)
    }

    fun signInMember(phone: String, password: String) {
        _authMessage.value = null
        _isSuccessMessage.value = false
        val res = authRepository.signInMember(phone, password)
        res.onSuccess {
            _loginSucceededEvent.tryEmit(Unit)
        }.onFailure { e ->
            _isSuccessMessage.value = false
            _authMessage.value = e.localizedMessage ?: "মোবাইল নম্বর বা পাসওয়ার্ড সঠিক নয়।"
        }
    }

    fun updateOwnProfile(uid: String, name: String, phone: String, photoUrl: String, bloodGroup: String) {
        val res = authRepository.updateOwnProfile(name, phone, photoUrl, bloodGroup)
        _isSuccessMessage.value = res.isSuccess
        _authMessage.value = res.fold(
            onSuccess = { "প্রোফাইল সফলভাবে হালনাগাদ হয়েছে" },
            onFailure = { it.localizedMessage ?: "প্রোফাইল হালনাগাদ ব্যর্থ হয়েছে" }
        )
    }

    fun signOut() {
        authRepository.signOut()
        _isSuccessMessage.value = true
        _authMessage.value = "লগআউট সম্পন্ন হয়েছে"
    }
}
