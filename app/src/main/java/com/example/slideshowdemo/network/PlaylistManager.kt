package com.example.slideshowdemo.network

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.slideshowdemo.model.FileDescriptors
import com.example.slideshowdemo.model.PlaylistData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


class PlaylistManager(context: Context) {
    private var TAG = "abhi"
    val context = context
    private var mediaSourceUrls = ArrayList<PlaylistData>()
    private var playlistSize: Int? = null
    private var fileDescriptors = ArrayList<FileDescriptors>()

    fun getPlayListData() {
        var localScreenCode = "C22G66"
        ApiClient.client().create(ApiInterface::class.java)
            .getPlayList(localScreenCode).enqueue(object : Callback<List<PlaylistData>> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(
                    call: Call<List<PlaylistData>>,
                    response: Response<List<PlaylistData>>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "" + response, Toast.LENGTH_LONG).show();

                        if (response.body() != null) {
                            Log.d(TAG, "${response.body()}")

                            if (response.body()!!.size != null) {
                                mediaSourceUrls.clear()
                                for (data in response.body()!!) {
                                    var id = data.id
                                    var contentType = data.contentType
                                    var slideContentUrl = data.slideContentUrl
                                    var autoReplayVideo = data.autoReplayVideo
                                    var interval = data.interval
                                    var fromDate = data.fromDate
                                    var toDate = data.toDate
                                    var fromTime = data.fromTime
                                    var toTime = data.toTime
                                    var days = data.days

                                    if (id != null && contentType != null && slideContentUrl != null && interval != null) {
                                        mediaSourceUrls.add(
                                            PlaylistData(
                                                id,
                                                contentType,
                                                slideContentUrl,
                                                autoReplayVideo,
                                                interval,
                                                fromDate,
                                                toDate,
                                                fromTime,
                                                toTime,
                                                days
                                            )
                                        )
                                    }
                                    Log.d(TAG, "${response.body()!!.size}")
                                }
                                getFileUri()
                            }


//                            if(response.body().)

                        }

                    } else {
                        Toast.makeText(
                            context,
                            "Failed api call",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d(TAG, "Failed")
                    }
                }

                override fun onFailure(call: Call<List<PlaylistData>>, t: Throwable) {
                    Log.d(TAG, "$t")
                }
            })
    }

    private fun getFileUri() {
        mediaSourceUrls.forEach {
            val uri: Uri = Uri.parse(it.slideContentUrl)
            Log.d(TAG, "$uri")
            downloadMedia(uri, it.id, it.contentType)
            readFromStorage(uri, it.id, it.contentType)
        }
        playlistSize = mediaSourceUrls.size
        Log.d(TAG, "$playlistSize")
    }

    private fun downloadMedia(uri: Uri, fileId: Int, contentType: Int) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(uri)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        if (contentType == 2) {
            val fileDir: String = "${context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)}"
//            val fileDir: String = "/DownloadTestFolder"
            request.setDestinationInExternalPublicDir(fileDir, "$fileId.JPG")
            Log.d(TAG, "files image $fileDir :: $fileId")
        }

        if (contentType == 3) {
            val fileDir: String = "${context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)}"
            request.setDestinationInExternalPublicDir(fileDir, "$fileId.mp4")
            Log.d(TAG, "files video $fileDir :: $fileId")
        }


        val reference = downloadManager.enqueue(request)
        val query = DownloadManager.Query()

        query.setFilterById(reference)
        val cursor: Cursor = downloadManager.query(query)
    }

    private fun readFromStorage(uri: Uri, fileId: Int, contentType: Int) {
        fileDescriptors.clear()
        if (contentType == 2) {
            var file = "${context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)}/$fileId.JPG"
            Log.d(TAG, "read image :: $file")
            fileDescriptors.add(FileDescriptors(fileId, contentType, file))
        }

        if (contentType == 3) {
            var file = "${context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)}/$fileId.mp4"
            Log.d(TAG, "read video :: $file")
            fileDescriptors.add(FileDescriptors(fileId, contentType, file))
        }

    }

    fun getDownloadedFilePath(): ArrayList<FileDescriptors> {
        return fileDescriptors
    }

}