package com.fResult.com.fResult.socialApp

import kotlinx.coroutines.CoroutineScope

data class UserProfile(
  val id: String,
  val name: String,
  val age: Int,
)

class UserProfileViewModel(
  private val userRepository: UserRepository,
  private val coroutineScope: CoroutineScope,
) {
  // the state of the UI
  // the current user profile
  private var profile: UserProfile? = null

  // loading state (true/false)
  private var loading = false
}

interface UserRepository {
  suspend fun fetchProfile(userId: String): UserProfile?

  suspend fun updateProfile(userProfile: UserProfile): Boolean
}
