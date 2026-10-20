package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.protocol.BlueAndMeConstants
import com.example.protocol.ReflectionMessage
import com.example.protocol.SlipFrameCodec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
    assertEquals("BlueAndMe Connect", appName)
  }

  @Test
  fun `verify slip framing and reflection message roundtrip`() {
    val codec = SlipFrameCodec()
    val originalMsg = ReflectionMessage(
      iid = BlueAndMeConstants.IID.POSITIONING_SENSOR,
      fid = BlueAndMeConstants.FID_SENSOR.SUBSCRIBE,
      payload = byteArrayOf(0x64, 0x00, 0x00, 0x00) // 100 ms
    )

    val serialized = ReflectionMessage.serialize(originalMsg.iid, originalMsg.fid, originalMsg.payload)
    val encoded = codec.encode(serialized)

    var parsedMsg: ReflectionMessage? = null
    codec.feed(encoded, encoded.size) { frame ->
      parsedMsg = ReflectionMessage.parse(frame)
    }

    assertNotNull(parsedMsg)
    assertEquals(originalMsg.iid, parsedMsg?.iid)
    assertEquals(originalMsg.fid, parsedMsg?.fid)
    assertEquals(4, parsedMsg?.payload?.size)
  }
}
