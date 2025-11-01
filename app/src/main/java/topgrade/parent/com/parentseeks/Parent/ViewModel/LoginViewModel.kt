package topgrade.parent.com.parentseeks.Parent.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import topgrade.parent.com.parentseeks.Parent.Repository.ConsolidatedUserRepository

class LoginViewModel(private val userRepository: ConsolidatedUserRepository) : ViewModel() {
    private val _loginState = MutableLiveData<ConsolidatedUserRepository.LoginResult>()
    val loginState: LiveData<ConsolidatedUserRepository.LoginResult> = _loginState

    fun login(email: String, password: String, campusId: String, fcmToken: String, userType: String) {
        _loginState.value = ConsolidatedUserRepository.LoginResult.Loading
        viewModelScope.launch {
            val result = userRepository.login(email, password, campusId, fcmToken, userType)
            _loginState.value = result
        }
    }
} 