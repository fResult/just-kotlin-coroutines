package com.fResult.socialApp

import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

class UserProfileViewModelTest {
  // coroutine dispatcher
  private val testDispatcher = StandardTestDispatcher()
  private val testScope = TestScope(testDispatcher)

  // code under test
  private val fakeUserRepo =
    object : UserRepository {
      val profiles =
        mutableMapOf(
          "1" to UserProfile("1", "Korn", 99),
          "2" to UserProfile("2", "Batman", 34),
        )

      override suspend fun fetchProfile(userId: String): UserProfile? {
        delay(1.seconds)
        return profiles[userId]
      }

      override suspend fun updateProfile(userProfile: UserProfile): Boolean {
        delay(500.milliseconds)
        if (userProfile.id !in profiles) return false

        profiles[userProfile.id] = userProfile
        return true
      }
    }
  private val viewModel = UserProfileViewModel(fakeUserRepo, testScope)

  @OptIn(ExperimentalCoroutinesApi::class)
  @BeforeEach
  fun setup() {
    Dispatchers.setMain(testDispatcher)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @AfterEach
  fun tearDown() {
    Dispatchers.resetMain()
  }
}
