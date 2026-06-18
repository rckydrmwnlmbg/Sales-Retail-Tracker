package com.example

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView
import kotlin.system.exitProcess

class CrashActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val stackTrace = intent.getStringExtra("STACKTRACE") ?: "Unknown Error"
        
        val scrollView = ScrollView(this)
        val textView = TextView(this).apply {
            text = "App crashed!\n\n$stackTrace"
            setPadding(32, 32, 32, 32)
        }
        scrollView.addView(textView)
        setContentView(scrollView)
    }
}

fun setupCrashHandler(context: Context) {
    val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        val stackTrace = throwable.stackTraceToString()
        android.util.Log.e("CRASH_HANDLER", "App crashed", throwable)
        val intent = Intent(context, CrashActivity::class.java).apply {
            putExtra("STACKTRACE", stackTrace)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)
        exitProcess(1) // Kill current process
    }
}
