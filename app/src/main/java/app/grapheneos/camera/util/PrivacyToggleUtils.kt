package app.grapheneos.camera.util

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.SensorPrivacyManager
import android.os.Build
import androidx.annotation.RequiresApi

private const val MICROPHONE_OPS: String = "android:record_audio"
private const val CAMERA_OPS: String = "android:camera"

@RequiresApi(Build.VERSION_CODES.S)
class PrivacyToggleUtils(private val context: Context) {

    private val appOpsManager by lazy {
        context.getSystemService(AppOpsManager::class.java)
    }
    private val sensorPrivacyManager by lazy {
        context.getSystemService(SensorPrivacyManager::class.java)
    }
    private val microphoneToggleSupported by lazy {
        sensorPrivacyManager.supportsSensorToggle(
            SensorPrivacyManager.Sensors.MICROPHONE
        )
    }
    private val cameraToggleSupported by lazy {
        sensorPrivacyManager.supportsSensorToggle(
            SensorPrivacyManager.Sensors.CAMERA
        )
    }

    private fun isPermissionGranted(permission: String) =
        context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    private fun microphoneAllowed() =
        isPermissionGranted(Manifest.permission.RECORD_AUDIO)

    private fun cameraAllowed() =
        isPermissionGranted(Manifest.permission.CAMERA)

    fun isMicrophoneToggleOff(): Boolean {
        if (!microphoneToggleSupported || !microphoneAllowed()) return false

        val state = appOpsManager.unsafeCheckOpNoThrow(
            MICROPHONE_OPS,
            android.os.Process.myUid(),
            context.packageName,
        )
        return state == AppOpsManager.MODE_IGNORED
    }

    fun opsMicrophoneAllowed() = opsAllowed(MICROPHONE_OPS)
    fun opsCameraAllowed() = opsAllowed(CAMERA_OPS)

    private fun opsAllowed(ops: String) = appOpsManager.unsafeCheckOpNoThrow(
        ops,
        android.os.Process.myUid(),
        context.packageName
    ) == AppOpsManager.MODE_ALLOWED

    fun showMicrophoneUnblockDialog() = showOpsUnblockDialog(MICROPHONE_OPS)

    fun showCameraUnblockDialog() = showOpsUnblockDialog(CAMERA_OPS)

    private fun showOpsUnblockDialog(
        ops: String,
        attributionTag: String = "",
        message: String = ""
    ) {
        appOpsManager.noteOpNoThrow(
            ops,
            android.os.Process.myUid(),
            context.packageName,
            attributionTag,
            message
        )
    }
}