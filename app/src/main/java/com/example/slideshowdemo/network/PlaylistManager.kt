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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.slideshowdemo.model.FileDescriptors
import com.example.slideshowdemo.model.PlaylistData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*
import javax.inject.Singleton
import kotlin.collections.ArrayList

@Singleton
class PlaylistManager(context: Context) {
    private var TAG = "abhi"
    val context = context
    private var mediaSourceUrls = ArrayList<PlaylistData>()
    private var playlistSize: Int? = null
    private var fileDescriptors = ArrayList<FileDescriptors>()

    private val _fileDescriptorData = MutableLiveData<String>()
    val fileDescriptorData: LiveData<String> = _fileDescriptorData


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
        }
        playlistSize = mediaSourceUrls.size
        Log.d(TAG, "$playlistSize")
    }

    private fun downloadMedia(uri: Uri, fileId: Int, contentType: Int) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(uri)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        if (contentType == 2) {
//            val fileDir: String = "${context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)}"
//            val fileDir: String = "/DownloadTestFolder"
            request.setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_DOWNLOADS,
                "$fileId.JPG"
            )
            Log.d(TAG, "files image fileDir :: $fileId")
        }

        if (contentType == 3) {
//            val fileDir: String = "${context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)}"
            request.setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_DOWNLOADS,
                "$fileId.mp4"
            )
            Log.d(TAG, "files video fileDir :: $fileId")
        }


        val reference = downloadManager.enqueue(request)
        val query = DownloadManager.Query()

        query.setFilterById(reference)
        val cursor: Cursor = downloadManager.query(query)
    }

    private fun readFromStorage(fileId: Int, contentType: Int, interval: Int) {
        if (contentType == 2) {
            var filePath =
                "${context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)}/$fileId.JPG"
//            val path = "${context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath}/$fileId.JPG"
            var file = File(filePath)
            if (file.exists()) {
                fileDescriptors.add(FileDescriptors(fileId, contentType, filePath, true, interval))
            } else {
                fileDescriptors.add(FileDescriptors(fileId, contentType, filePath, false, interval))
            }
            Log.d(TAG, "read image :: $filePath :: ${file.exists()}")
        }

        if (contentType == 3) {
            var filePath =
                "${context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)}/$fileId.mp4"
            var file = File(filePath)
            if (file.exists()) {
                fileDescriptors.add(FileDescriptors(fileId, contentType, filePath, true, interval))
            } else {
                fileDescriptors.add(FileDescriptors(fileId, contentType, filePath, false, interval))
            }
            Log.d(TAG, "read video :: $filePath :: ${file.exists()}")
        }

        _fileDescriptorData.postValue("file descriptor data updated")

    }

    fun getDownloadedFilePath(): ArrayList<FileDescriptors> {
        Log.d("abhi", "descripss :: $fileDescriptors")
        readData()
        return fileDescriptors
    }

    private fun readData() {
        fileDescriptors.clear()
        mediaSourceUrls.forEach {
            readFromStorage(it.id, it.contentType, it.interval)
            Log.d("abhi", "readData")
        }
    }

}