package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.security.KeyManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Fruit Ninja Auto Slicer", appName)
  }

  @Test
  fun `verify key generation and validation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val deviceId = KeyManager.getDeviceId(context)
    val validKey = KeyManager.generateKeyForDevice(deviceId)

    assertTrue("Valid key should pass", KeyManager.isKeyValid(context, validKey))
    assertTrue("Master VIP key should pass", KeyManager.isKeyValid(context, "SHIBLU-VIP-2026"))
  }
}
