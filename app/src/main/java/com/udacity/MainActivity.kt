package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.udacity.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var downloadID: Long = 0
    private var downloadStatus: DOWNLOAD_STATUS = DOWNLOAD_STATUS.FAIL
    private lateinit var selectedUrl: String

    private val NOTIFICATION_ID = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        createNotificationChannel()

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        binding.contentMain.downloadRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            selectedUrl = when (checkedId) {
                R.id.radio_glide -> GLIDE_URL
                R.id.radio_load_app -> LOAD_APP_URL
                R.id.radio_retrofit -> RETROFIT_URL
                else -> LOAD_APP_URL
            }
        }

        binding.contentMain.customButton.setOnClickListener {
            if (this::selectedUrl.isInitialized) {
                val loadingButton = it as LoadingButton
                loadingButton.onChangeButtonState(ButtonState.Loading)
                download()
            } else {
                Toast.makeText(this, getString(R.string.no_selection_toast_msg), Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            if (downloadID == id) {
                downloadStatus = DOWNLOAD_STATUS.SUCCESS
                Toast.makeText(context, "Download Completed", Toast.LENGTH_SHORT).show()
                binding.contentMain.customButton.onChangeButtonState(ButtonState.Completed)
                sendNotification()
            }
        }
    }

    private fun sendNotification() {
        // Create an intent for the notification click action
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        // Create the notification
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_description))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Show the notification
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun createNotificationChannel() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { setShowBadge(false) }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.notification_description)

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun download() {
        val request =
            DownloadManager.Request(Uri.parse(selectedUrl))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "/repository.zip"
                )
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.

        val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadID))
        if (cursor.moveToFirst()) {
            when (cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) {
                DownloadManager.STATUS_FAILED -> {
                    downloadStatus = DOWNLOAD_STATUS.FAIL
                    binding.contentMain.customButton.onChangeButtonState(ButtonState.Completed)
                }
                DownloadManager.STATUS_SUCCESSFUL -> {
                    downloadStatus = DOWNLOAD_STATUS.SUCCESS
                }
            }
        }
    }

    companion object {
        private enum class DOWNLOAD_STATUS {
            FAIL,
            SUCCESS
        }
        private const val GLIDE_URL = "https://github.com/square/retrofit"
        private const val LOAD_APP_URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val RETROFIT_URL = "https://github.com/square/retrofit"
        private const val CHANNEL_ID = "channelId"
        private const val CHANNEL_NAME = "loadAppChannel"
    }
}