package com.hamomel.vision.permissions

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.ref.WeakReference
import kotlin.coroutines.resume

/**
 * @author Роман Зотов on 16.12.2021
 */
class PermissionChecker(
    private val isVersionGreaterThanM: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
) {

    private var activity: WeakReference<ComponentActivity>? = null

    /**
     * Call this method in Activity's in onStart()
     */
    fun attach(activity: ComponentActivity) {
        this.activity = WeakReference(activity)
    }

    /**
     * Call this in Activity's onStop
     */
    fun detach() {
        activity?.clear()
        activity = null
    }

    /**
     * Checks permission and asks if needed. Suspends until user answers.
     * @return [Granted] if permission is granted
     * @return [Denied] if permission is denied and showing permission rationale is not required
     * @return [ShouldShowRationale] if showing permission rationale is required
     */
    suspend fun checkAndRequestIfNeeded(permission: String): PermissionCheckResult =
        suspendCancellableCoroutine { continuation ->
            val activity = activity?.get() ?: throw IllegalStateException("PermissionChecker is not attached to activity")

            val isGranted = ContextCompat.checkSelfPermission(activity, permission)

            when {
                isGranted == PackageManager.PERMISSION_GRANTED -> {
                    continuation.resumeIfActive(Granted)
                }
                shouldShowPermissionsRationale(permission) -> {
                    continuation.resumeIfActive(ShouldShowRationale)
                }
                else -> {
                    val launcher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                        continuation.resumeIfActive(
                            if (granted) Granted else Denied
                        )
                    }

                    launcher.launch(permission)
                }
            }
        }

    private fun <T> CancellableContinuation<T>.resumeIfActive(result: T) {
        if (isActive) resume(result)
    }

    @SuppressLint("NewApi")
    private fun shouldShowPermissionsRationale(permission: String): Boolean =
        if (isVersionGreaterThanM) {
            activity?.get()?.shouldShowRequestPermissionRationale(permission)
                ?: throw IllegalStateException("PermissionChecker is not attached to activity")
        } else {
            false
        }
}


