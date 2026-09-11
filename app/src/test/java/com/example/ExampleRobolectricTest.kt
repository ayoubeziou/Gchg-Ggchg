package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("DeepSeek", appName)
  }

  @Test
  fun `test session and message insertion in database`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = androidx.room.Room.inMemoryDatabaseBuilder(
      context,
      com.example.data.local.AppDatabase::class.java
    ).allowMainThreadQueries().build()

    val dao = db.chatDao()
    val session = com.example.data.local.entity.ChatSessionEntity(
      id = "test-session-1",
      title = "DeepThink Discussion"
    )
    dao.insertSession(session)

    val message = com.example.data.local.entity.ChatMessageEntity(
      id = "msg-1",
      sessionId = "test-session-1",
      role = "user",
      content = "حل المسألة المنطقية",
      isDeepThink = true,
      isWebSearch = false
    )
    dao.insertMessage(message)

    val messages = dao.getMessagesListForSession("test-session-1")
    assertEquals(1, messages.size)
    assertEquals("حل المسألة المنطقية", messages[0].content)
    assertEquals(true, messages[0].isDeepThink)

    db.close()
  }
}
