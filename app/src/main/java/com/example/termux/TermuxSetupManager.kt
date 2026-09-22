package com.example.termux

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/**
 * High-level Termux integration state.
 */
class TermuxSetupManager(
    context: Context
) {

    private val appContext =
        context.applicationContext

    private val bridge =
        TermuxBridge(appContext)

    fun isTermuxInstalled(): Boolean {
        return bridge.isInstalled()
    }

    fun initializeStorage(): Boolean {
        return bridge.setupStorage()
    }

    fun startDaemon(): Boolean {
        return bridge.startAutoStorageDaemon()
    }

    fun stopDaemon(): Boolean {
        return bridge.stopAutoStorageDaemon()
    }

    fun openTermux(): Boolean {
        return try {

            val launchIntent =
                appContext.packageManager
                    .getLaunchIntentForPackage(
                        TermuxBridge.TERMUX_PACKAGE
                    )

            if (launchIntent != null) {

                launchIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )

                appContext.startActivity(
                    launchIntent
                )

                true

            } else {
                false
            }

        } catch (_: Exception) {
            false
        }
    }

    fun openTermuxAppInfo(): Boolean {
        return try {

            val intent =
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse(
                        "package:${TermuxBridge.TERMUX_PACKAGE}"
                    )
                )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            appContext.startActivity(
                intent
            )

            true

        } catch (_: Exception) {
            false
        }
    }
}
