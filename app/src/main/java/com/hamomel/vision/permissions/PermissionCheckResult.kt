package com.hamomel.vision.permissions

/**
 * @author Роман Зотов on 16.12.2021
 */
sealed interface PermissionCheckResult

object Granted : PermissionCheckResult

object Denied : PermissionCheckResult

object ShouldShowRationale : PermissionCheckResult
