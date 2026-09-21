package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.StorageStats
import com.example.ui.components.StorageDashboardCard
import com.example.ui.theme.MyApplicationTheme
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
  fun app_ui_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        StorageDashboardCard(
          stats = StorageStats(
            totalFiles = 12,
            totalBytes = 44040192L,
            duplicateCount = 3,
            duplicateSavingsBytes = 12582912L,
            renamedCount = 4,
            pendingRenameCount = 3,
            organizedCount = 8,
            taggedCount = 10
          ),
          storageFreedTotal = 3145728L,
          onBatchRenameClick = {},
          onCleanDuplicatesClick = {},
          onOrganizeAllClick = {},
          onScanClick = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/app_ui.png")
  }
}

