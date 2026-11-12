package com.example

import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Court Timer", appName)
    }

    @Test
    fun `verify no audio or video recording permissions exist`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )
        val permissions = packageInfo.requestedPermissions?.toList() ?: emptyList()

        assertFalse("App must not request RECORD_AUDIO", permissions.contains("android.permission.RECORD_AUDIO"))
        assertFalse("App must not request CAMERA", permissions.contains("android.permission.CAMERA"))
        assertFalse("App must not request CAPTURE_AUDIO_OUTPUT", permissions.contains("android.permission.CAPTURE_AUDIO_OUTPUT"))
        assertFalse("App must not request CAPTURE_VIDEO_OUTPUT", permissions.contains("android.permission.CAPTURE_VIDEO_OUTPUT"))
    }
}
