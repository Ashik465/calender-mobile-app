package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.KokoroViewModel
import com.example.ui.DashboardScreen
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun dashboard_render_test() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = KokoroViewModel(application)

    composeTestRule.setContent { 
      MyApplicationTheme(animeTheme = "MIDNIGHT_BASS", darkTheme = true) { 
        DashboardScreen(viewModel = viewModel)
      } 
    }

    composeTestRule.waitForIdle()

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/dashboard.png")
  }
}
