package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
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
    private var downloadStatus: DownloadStatus = DownloadStatus.FAIL
    private lateinit var selectedUrl: URL

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
                R.id.radio_glide -> URL.GLIDE
                R.id.radio_load_app -> URL.LOAD_APP
                R.id.radio_retrofit -> URL.RETROFIT
                else -> URL.LOAD_APP
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
                downloadStatus = DownloadStatus.SUCCESS
                Toast.makeText(context, "Download Completed", Toast.LENGTH_SHORT).show()
                binding.contentMain.customButton.onChangeButtonState(ButtonState.Completed)
                sendNotification()
            }
        }
    }

    private fun sendNotification() {
        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create an intent for the notification click action
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("fileName", selectedUrl.title)
        intent.putExtra("status", downloadStatus.status)

        // Create a back stack for the intent
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addParentStack(DetailActivity::class.java)
        stackBuilder.addNextIntent(intent)

        // Get the pending intent from the stack builder
        pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        action = NotificationCompat.Action(
            R.drawable.ic_assistant_black_24dp,
            getString(R.string.notification_button),
            pendingIntent
        )

        // Create the notification
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(selectedUrl.title)
            .setContentText(selectedUrl.content)
            .setContentIntent(pendingIntent)
            .addAction(action)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Show the notification
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
            DownloadManager.Request(Uri.parse(selectedUrl.url))
                .setTitle(selectedUrl.title)
                .setDescription(selectedUrl.content)
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
                    downloadStatus = DownloadStatus.FAIL
                    binding.contentMain.customButton.onChangeButtonState(ButtonState.Completed)
                }
                DownloadManager.STATUS_SUCCESSFUL -> {
                    downloadStatus = DownloadStatus.SUCCESS
                }
            }
        }
    }

    companion object {
        private enum class DownloadStatus(val status: String) {
            FAIL("Fail"),
            SUCCESS("Success")
        }

        private enum class URL(val url: String, val title: String, val content: String) {
            GLIDE(
                "https://github.com/bumptech/glide/archive/master.zip",
                "Glide: Image Loading Library By BumpTech",
                "Glide repository is downloaded"
            ),
            LOAD_APP(
                "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip",
                "Udacity: Android Kotlin Nanodegree",
                "The Project 3 repository is downloaded"
            ),
            RETROFIT(
                "https://github.com/square/retrofit/archive/master.zip",
                "Retrofit: Type-safe HTTP client by Square, Inc",
                "Retrofit repository is downloaded"
            ),
        }

        private const val CHANNEL_ID = "channelId"
        private const val CHANNEL_NAME = "loadAppChannel"
    }
}