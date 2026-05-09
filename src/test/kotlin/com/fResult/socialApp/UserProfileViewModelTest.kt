package com.fResult.socialApp

import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
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

  @BeforeEach
  fun setup() {
    Dispatchers.setMain(testDispatcher)
  }

  @AfterEach
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `load user profile should update user profile and loading status`() =
    testScope.runTest {
      val userId = "1"

      // can run coroutines
      viewModel.loadUserProfile(userId)
      runCurrent() // runs all pending tasks in the coroutine displatcher
      assertTrue(viewModel.loading.value) // true at this point
      assertNull(viewModel.profile.value) // no profile loaded yet

      advanceTimeBy(1.seconds) // moves the internal clock of the dispatcher
      runCurrent() // the coroutine is finished

      assertFalse(viewModel.loading.value) // the screen has finished loading
      assertEquals(userId, viewModel.profile.value?.id)
    }

  @Test
  fun `updateUserProfile should modify user profile and loading status`() {
    val name = "KornZilla"
    val age = 999

    testScope.runTest {
      viewModel.loadUserProfile("1")
      advanceTimeBy(1.seconds)

      viewModel.updateUserProfile(name, age)
      runCurrent()

      assertTrue(viewModel.loading.value)

      advanceTimeBy(500.milliseconds)
      runCurrent()

      assertFalse(viewModel.loading.value)
      assertEquals(name, viewModel.profile.value?.name)
      assertEquals(age, viewModel.profile.value?.age)
    }
  }
}
