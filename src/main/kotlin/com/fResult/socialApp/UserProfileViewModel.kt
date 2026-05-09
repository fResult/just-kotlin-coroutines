package com.fResult.socialApp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

  fun updateUserProfile(
    name: String,
    age: Int,
  ) = coroutineScope.launch {
    _profile.value?.let { currentProfile ->
      _loading.value = true
      try {
        val userToUpdate = currentProfile.copy(name = name, age = age)
        val updatedProfileSuccessfully = userRepository.updateProfile(userToUpdate)
        if (updatedProfileSuccessfully) {
          _profile.value = userToUpdate
        }
      } finally {
        _loading.value = false
      }
    }
  }
}
