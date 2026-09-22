package com.example.termux

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import java.util.concurrent.atomic.AtomicInteger

/**
 * Android -> Termux integration layer.
 *
 * Auto-storage uses Termux as an optional local Linux backend.
 *
 * The APK remains fully functional without Termux.
 */
class TermuxBridge(
    private val context: Context
) {

    companion object {
        const val TERMUX_PACKAGE = "com.termux"

        const val RUN_COMMAND_SERVICE =
            "com.termux.app.RunCommandService"

        const val ACTION_RUN_COMMAND =
            "com.termux.RUN_COMMAND"

        const val EXTRA_COMMAND_PATH =
            "com.termux.RUN_COMMAND_PATH"

        const val EXTRA_ARGUMENTS =
            "com.termux.RUN_COMMAND_ARGUMENTS"

        const val EXTRA_WORKDIR =
            "com.termux.RUN_COMMAND_WORKDIR"

        const val EXTRA_BACKGROUND =
            "com.termux.RUN_COMMAND_BACKGROUND"

        const val EXTRA_SESSION_ACTION =
            "com.termux.RUN_COMMAND_SESSION_ACTION"

        const val EXTRA_PENDING_INTENT =
            "com.termux.RUN_COMMAND_PENDING_INTENT"

        const val EXTRA_STDIN =
            "com.termux.RUN_COMMAND_STDIN"

        const val EXTRA_RESULT_BUNDLE =
            "com.termux.RUN_COMMAND_RESULT_BUNDLE"

        const val RESULT_EXIT_CODE =
            "com.termux.service.EXTRA_PLUGIN_RESULT_BUNDLE_EXIT_CODE"

        const val RESULT_STDOUT =
            "com.termux.service.EXTRA_PLUGIN_RESULT_BUNDLE_STDOUT"

        const val RESULT_STDERR =
            "com.termux.service.EXTRA_PLUGIN_RESULT_BUNDLE_STDERR"

        const val RESULT_ERR =
            "com.termux.service.EXTRA_PLUGIN_RESULT_BUNDLE_ERR"

        const val RESULT_ERRMSG =
            "com.termux.service.EXTRA_PLUGIN_RESULT_BUNDLE_ERRMSG"

        private const val HOME =
            "/data/data/com.termux/files/home"

        private const val PREFIX =
            "/data/data/com.termux/files/usr"

        private val executionCounter =
            AtomicInteger(1000)
    }

    data class CommandResult(
        val exitCode: Int,
        val stdout: String,
        val stderr: String,
        val errorCode: Int = 0,
        val errorMessage: String = ""
    ) {
        val success: Boolean
            get() = exitCode == 0 && errorCode == 0
    }

    fun isInstalled(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    TERMUX_PACKAGE,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    TERMUX_PACKAGE,
                    0
                )
            }

            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun runCommand(
        commandPath: String,
        arguments: Array<String> = emptyArray(),
        workDir: String = HOME,
        background: Boolean = true,
        stdin: String? = null
    ): Boolean {

        if (!isInstalled()) {
            return false
        }

        val intent = Intent().apply {
            setClassName(
                TERMUX_PACKAGE,
                RUN_COMMAND_SERVICE
            )

            action = ACTION_RUN_COMMAND

            putExtra(
                EXTRA_COMMAND_PATH,
                commandPath
            )

            putExtra(
                EXTRA_ARGUMENTS,
                arguments
            )

            putExtra(
                EXTRA_WORKDIR,
                workDir
            )

            putExtra(
                EXTRA_BACKGROUND,
                background
            )

            putExtra(
                EXTRA_SESSION_ACTION,
                "0"
            )

            if (stdin != null) {
                putExtra(
                    EXTRA_STDIN,
                    stdin
                )
            }
        }

        return try {
            context.startService(intent)
            true
        } catch (_: SecurityException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    fun runCommandWithResult(
        commandPath: String,
        arguments: Array<String> = emptyArray(),
        workDir: String = HOME,
        background: Boolean = true,
        stdin: String? = null
    ): Boolean {

        if (!isInstalled()) {
            return false
        }

        val executionId =
            executionCounter.incrementAndGet()

        val receiverIntent =
            Intent(
                context,
                TermuxCommandReceiver::class.java
            ).apply {
                putExtra(
                    TermuxCommandReceiver.EXTRA_EXECUTION_ID,
                    executionId
                )
            }

        val pendingIntentFlags =
            PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_MUTABLE
                } else {
                    0
                }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                executionId,
                receiverIntent,
                pendingIntentFlags
            )

        val intent = Intent().apply {
            setClassName(
                TERMUX_PACKAGE,
                RUN_COMMAND_SERVICE
            )

            action = ACTION_RUN_COMMAND

            putExtra(
                EXTRA_COMMAND_PATH,
                commandPath
            )

            putExtra(
                EXTRA_ARGUMENTS,
                arguments
            )

            putExtra(
                EXTRA_WORKDIR,
                workDir
            )

            putExtra(
                EXTRA_BACKGROUND,
                background
            )

            putExtra(
                EXTRA_SESSION_ACTION,
                "0"
            )

            putExtra(
                EXTRA_PENDING_INTENT,
                pendingIntent
            )

            if (stdin != null) {
                putExtra(
                    EXTRA_STDIN,
                    stdin
                )
            }
        }

        return try {
            context.startService(intent)
            true
        } catch (_: SecurityException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Request Termux shared-storage setup.
     *
     * This does not bypass Android permissions.
     */
    fun setupStorage(): Boolean {
        return runCommand(
            commandPath =
                "$PREFIX/bin/termux-setup-storage",
            arguments = emptyArray(),
            workDir = HOME,
            background = false
        )
    }

    /**
     * Check whether ~/storage/shared exists.
     */
    fun checkStorageSetup(): Boolean {
        return runCommandWithResult(
            commandPath = "$PREFIX/bin/test",
            arguments = arrayOf(
                "-d",
                "$HOME/storage/shared"
            ),
            workDir = HOME,
            background = true
        )
    }

    fun sharedStoragePath(): String {
        return "$HOME/storage/shared"
    }

    fun androidSharedStoragePath(): String {
        return "/storage/emulated/0"
    }

    /**
     * Reserved for the future persistent Auto-storage daemon.
     *
     * Do not require this for the current application to work.
     */
    fun startAutoStorageDaemon(): Boolean {
        return runCommand(
            commandPath =
                "$HOME/auto-storage/start-daemon.sh",
            arguments = emptyArray(),
            workDir = "$HOME/auto-storage",
            background = true
        )
    }

    fun stopAutoStorageDaemon(): Boolean {
        return runCommand(
            commandPath =
                "$HOME/auto-storage/stop-daemon.sh",
            arguments = emptyArray(),
            workDir = "$HOME/auto-storage",
            background = true
        )
    }
}
