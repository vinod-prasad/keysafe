package com.appnest.safe.credentials.vault.domain

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AppLockManager : DefaultLifecycleObserver {
    private val _isLocked = MutableStateFlow(true) // Start locked by default
    val isLocked: StateFlow<Boolean> = _isLocked

    private var backgroundTimestamp: Long = 0L
    private var idleTimeoutMillis: Long = 5 * 60 * 1000L // Default 5 mins

    fun updateIdleTimeout(mins: Int) {
        idleTimeoutMillis = mins * 60 * 1000L
    }

    fun unlock() {
        _isLocked.value = false
    }

    fun forceLock() {
        _isLocked.value = true
    }

    fun attachToProcessLifecycle() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        // App comes to foreground
        if (backgroundTimestamp > 0) {
            val idleTime = System.currentTimeMillis() - backgroundTimestamp
            if (idleTime > idleTimeoutMillis) {
                forceLock()
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        // App goes to background
        backgroundTimestamp = System.currentTimeMillis()
    }
}
