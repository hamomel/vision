package com.hamomel.vision.camerascreen.presintation

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.hamomel.vision.R

class NeedPermissionsDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.need_permission_dialog_title)
            .setMessage(R.string.need_permission_dialog_message)
            .setCancelable(false)
            .setPositiveButton(R.string.need_permission_dialog_positive_button) { _, _ ->
                (parentFragment as? PermissionDialogActionsHandler)?.onPositiveClick()
            }
            .setNegativeButton(R.string.need_permission_dialog_negative_button) { _, _ ->
                (parentFragment as? PermissionDialogActionsHandler)?.onNegativeClick()
            }
            .create().apply {
                setCanceledOnTouchOutside(false)
            }
    }
}

interface PermissionDialogActionsHandler {
    fun onPositiveClick()
    fun onNegativeClick()
}