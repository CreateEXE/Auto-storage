package com.example.termux

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log

/**
 * Receives results returned by Termux RUN_COMMAND.
 */
class TermuxCommandReceiver : BroadcastReceiver() {

    companion object {

        private const val TAG =
            "AutoStorage.Termux"

        const val EXTRA_EXECUTION_ID =
            "auto_storage_execution_id"

        const val EXTRA_EXIT_CODE =
            "com.termux.service.EXTRA_PLUGIN_RESULT_BUNDLE_EXIT_CODE"

        const val EXTRA_STDOUT =
            "com.termux.service.EXTRA_PLUGIN_RESULT_BUNDLE_STDOUT"

        const val EXTRA_STDERR =
            "com.termux.service.EXTRA_PLUGIN_RESULT_BUNDLE_STDERR"

        const val EXTRA_ERR =
            "com.termux.service.EXTRA_PLUGIN_RESULT_BUNDLE_ERR"

        const val EXTRA_ERRMSG =
            "com.termux.service.EXTRA_PLUGIN_RESULT_BUNDLE_ERRMSG"

        const val ACTION_COMMAND_RESULT =
            "com.example.termux.COMMAND_RESULT"
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        val executionId =
            intent.getIntExtra(
                EXTRA_EXECUTION_ID,
                -1
            )

        val resultBundle: Bundle? =
            intent.getBundleExtra(
                TermuxBridge.EXTRA_RESULT_BUNDLE
            )

        val exitCode =
            resultBundle?.getInt(
                EXTRA_EXIT_CODE,
                -1
            )
                ?: intent.getIntExtra(
                    EXTRA_EXIT_CODE,
                    -1
                )

        val stdout =
            resultBundle?.getString(
                EXTRA_STDOUT,
                ""
            )
                ?: intent.getStringExtra(
                    EXTRA_STDOUT
                )
                ?: ""

        val stderr =
            resultBundle?.getString(
                EXTRA_STDERR,
                ""
            )
                ?: intent.getStringExtra(
                    EXTRA_STDERR
                )
                ?: ""

        val errorCode =
            resultBundle?.getInt(
                EXTRA_ERR,
                0
            )
                ?: intent.getIntExtra(
                    EXTRA_ERR,
                    0
                )

        val errorMessage =
            resultBundle?.getString(
                EXTRA_ERRMSG,
                ""
            )
                ?: intent.getStringExtra(
                    EXTRA_ERRMSG
                )
                ?: ""

        Log.d(
            TAG,
            "Termux command result: id=$executionId exit=$exitCode error=$errorCode"
        )

        if (stdout.isNotBlank()) {
            Log.d(
                TAG,
                "stdout: $stdout"
            )
        }

        if (stderr.isNotBlank()) {
            Log.w(
                TAG,
                "stderr: $stderr"
            )
        }

        val resultIntent =
            Intent(ACTION_COMMAND_RESULT).apply {

                setPackage(
                    context.packageName
                )

                putExtra(
                    EXTRA_EXECUTION_ID,
                    executionId
                )

                putExtra(
                    EXTRA_EXIT_CODE,
                    exitCode
                )

                putExtra(
                    EXTRA_STDOUT,
                    stdout
                )

                putExtra(
                    EXTRA_STDERR,
                    stderr
                )

                putExtra(
                    EXTRA_ERR,
                    errorCode
                )

                putExtra(
                    EXTRA_ERRMSG,
                    errorMessage
                )
            }

        context.sendBroadcast(
            resultIntent
        )
    }
}
