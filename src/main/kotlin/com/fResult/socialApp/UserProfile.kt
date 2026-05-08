package com.fResult.com.fResult.socialApp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserProfile(
  val id: String,
  val name: String,
  val age: Int,
)

class UserProfileViewModel(
  private val userRepository: UserRepository,
  private val coroutineScope: CoroutineScope,
) {
  // the states of the UI
  // the current user profile
  private val _profile = MutableStateFlow<UserProfile?>(null) // writable thread safe variable
  val profile = _profile.asStateFlow() // read-only

  // loading state (true/false)
  private val _loading = MutableStateFlow(false)
  val loading = _loading.asStateFlow()

  fun loadUserProfile(userId: String) =
    coroutineScope.launch {
      _loading.value = true // assignment is thread-safe
      try {
        _profile.value = userRepository.fetchProfile(userId)
      } finally {
        _loading.value = false
      }
    }
}

interface UserRepository {
  suspend fun fetchProfile(userId: String): UserProfile?

  suspend fun updateProfile(userProfile: UserProfile): Boolean
}
