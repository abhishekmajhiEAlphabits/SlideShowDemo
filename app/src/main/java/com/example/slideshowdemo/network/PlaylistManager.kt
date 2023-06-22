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
import com.example.slideshowdemo.utils.AppPreferences
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
    private var staticDurations = longArrayOf(4000, 3000, 3000, 3000, 3000, 8000, 2000)

    private val _fileDescriptorData = MutableLiveData<String>()
    val fileDescriptorData: LiveData<String> = _fileDescriptorData


    fun getPlayListData() {
        try {
            var i = 0
            var localScreenCode =
                AppPreferences(context).retrieveValueByKey("LOCAL_SCREEN_CODE", "NA")
            ApiClient.client().create(ApiInterface::class.java)
                .getPlayList(localScreenCode).enqueue(object : Callback<List<PlaylistData>> {
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onResponse(
                        call: Call<List<PlaylistData>>,
                        response: Response<List<PlaylistData>>
                    ) {
                        if (response.isSuccessful) {
                            Toast.makeText(context, "" + response, Toast.LENGTH_LONG).show()

                            if (response.body() != null) {
                                Log.d(TAG, "${response.body()}")

                                if (response.body()!!.size != null) {
                                    AppPreferences(context).saveKeyValue(
                                        response.body()!!.size.toString(),
                                        "$localScreenCode-PLAYLIST_SIZE"
                                    )
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

                                            AppPreferences(context).saveKeyValue(
                                                id.toString(),
                                                "$localScreenCode-$i-ID"
                                            )
                                            AppPreferences(context).saveKeyValue(
                                                contentType.toString(),
                                                "$localScreenCode-$i-CONTENT_TYPE"
                                            )
                                            AppPreferences(context).saveKeyValue(
                                                slideContentUrl,
                                                "$localScreenCode-$i-CONTENT_URL"
                                            )
                                            AppPreferences(context).saveKeyValue(
                                                autoReplayVideo.toString(),
                                                "$localScreenCode-$i-AUTO_REPLAY"
                                            )
                                            AppPreferences(context).saveKeyValue(
                                                interval.toString(),
                                                "$localScreenCode-$i-INTERVAL"
                                            )
                                            if (fromDate != null) {
                                                AppPreferences(context).saveKeyValue(
                                                    fromDate,
                                                    "$localScreenCode-$i-FROM_DATE"
                                                )
                                            }
                                            if (toDate != null) {
                                                AppPreferences(context).saveKeyValue(
                                                    toDate,
                                                    "$localScreenCode-$i-TO_DATE"
                                                )
                                            }
                                            if (fromTime != null) {
                                                AppPreferences(context).saveKeyValue(
                                                    fromTime,
                                                    "$localScreenCode-$i-FROM_TIME"
                                                )
                                            }
                                            if (toTime != null) {
                                                AppPreferences(context).saveKeyValue(
                                                    toTime,
                                                    "$localScreenCode-$i-TO_TIME"
                                                )
                                            }
                                            AppPreferences(context).saveKeyValue(
                                                days.toString(),
                                                "$$localScreenCode-$i-DAYS"
                                            )

                                        }
                                        i++
                                        Log.d(TAG, "${response.body()!!.size}")
                                    }
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
                        getFileUri()
                        readData()
                    }

                    override fun onFailure(call: Call<List<PlaylistData>>, t: Throwable) {
                        getFileUri()
                        readData()
                        Log.d(TAG, "$t")
                    }
                })
        } catch (e: Exception) {
            Log.d(TAG, "$e")
        }

    }

    private fun getFileUri() {
        try {
            var localScreenCode =
                AppPreferences(context).retrieveValueByKey("LOCAL_SCREEN_CODE", "NA")
            var externalScreenCode = "C22G66"
            val responseSize =
                AppPreferences(context).retrieveValueByKey("$localScreenCode-PLAYLIST_SIZE", "6")
            for (i in 0 until responseSize.toInt()) {
                var id = AppPreferences(context).retrieveValueByKey("$localScreenCode-$i-ID", "0")
                var contentType =
                    AppPreferences(context).retrieveValueByKey(
                        "$localScreenCode-$i-CONTENT_TYPE",
                        "2"
                    )
                var slideContentUrl =
                    AppPreferences(context).retrieveValueByKey(
                        "$localScreenCode-$i-CONTENT_URL",
                        "NA"
                    )
                if (slideContentUrl != "NA") {
                    val uri: Uri = Uri.parse(slideContentUrl)
                    val filename = slideContentUrl.substring(slideContentUrl.length - 5)
                    Log.d(TAG, "file uri :: $uri :: $filename :: $filename")
                    Log.d(TAG, "file uri :: $contentType :: $id :: $slideContentUrl")
                    var firstRun = false
                    if (firstRun) {
                        downloadMedia(uri, id.toInt(), contentType.toInt(), filename)
                        Log.d(TAG, "file downloading.. :: $filename")
                    } else {
                        if (localScreenCode != externalScreenCode) {
                            //code to delete files for the previous screen code because if
                            //the filename of the two screen codes may be same the new files
                            //will be downloaded with like eg:- 100_1,100_2
                            if (!fileExistInStorage(filename)) {
                                downloadMedia(uri, id.toInt(), contentType.toInt(), filename)
                                Log.d(TAG, "file downloading.. :: $filename")
                            } else {
                                Log.d(TAG, "file already exists :: $filename")
                            }
                        } else {
                            if (!fileExistInStorage(filename)) {
                                downloadMedia(uri, id.toInt(), contentType.toInt(), filename)
                                Log.d(TAG, "file downloading.. :: $filename")
                            } else {
                                Log.d(TAG, "file already exists :: $filename")
                            }
                        }
                    }
                }
            }
            playlistSize = mediaSourceUrls.size
            Log.d(TAG, "$playlistSize")
        } catch (e: Exception) {
            Log.d(TAG, "$e")
        }
    }

    private fun downloadMedia(uri: Uri, fileId: Int, contentType: Int, filename: String) {
        try {
            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(uri)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

            request.setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_DOWNLOADS,
                "$filename"
            )
            Log.d(TAG, "files image/video fileDir :: $fileId")


            val reference = downloadManager.enqueue(request)
            val query = DownloadManager.Query()

            query.setFilterById(reference)
            val cursor: Cursor = downloadManager.query(query)
        } catch (e: Exception) {
            Log.d(TAG, "$e")
        }
    }

    private fun readFromStorage(fileId: Int, contentType: Int, interval: Int, filename: String) {
        try {
            var filePath =
                "${context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)}/$filename"
            var file = File(filePath)
            if (file.exists()) {
                fileDescriptors.add(FileDescriptors(fileId, contentType, filePath, true, interval))
            } else {
                fileDescriptors.add(FileDescriptors(fileId, contentType, filePath, false, interval))
            }
            Log.d(TAG, "read image/video :: $filePath :: ${file.exists()}")

            _fileDescriptorData.postValue("file descriptor data updated")
        } catch (e: Exception) {
            Log.d(TAG, "$e")
        }
    }

    fun getDownloadedFilePath(): ArrayList<FileDescriptors> {
        Log.d("abhi", "descripss :: $fileDescriptors")
        readData()
        return fileDescriptors
    }

    private fun readData() {
        try {
            var localScreenCode =
                AppPreferences(context).retrieveValueByKey("LOCAL_SCREEN_CODE", "NA")
            fileDescriptors.clear()
            val responseSize = AppPreferences(context).retrieveValueByKey("PLAYLIST_SIZE", "6")
            for (i in 0 until responseSize.toInt()) {
                var id = AppPreferences(context).retrieveValueByKey("$localScreenCode-$i-ID", "0")
                var contentType =
                    AppPreferences(context).retrieveValueByKey(
                        "$localScreenCode-$i-CONTENT_TYPE",
                        "2"
                    )
                var slideContentUrl =
                    AppPreferences(context).retrieveValueByKey(
                        "$localScreenCode-$i-CONTENT_URL",
                        "NA"
                    )
                if (slideContentUrl != "NA") {
                    var interval =
                        AppPreferences(context).retrieveValueByKey(
                            "$localScreenCode-$i-INTERVAL",
                            "NA"
                        )
                    val filename = slideContentUrl.substring(slideContentUrl.length - 5)
                    readFromStorage(id.toInt(), contentType.toInt(), interval.toInt(), filename)
                    Log.d("abhi", "readData")
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "$e")
        }
    }

    private fun fileExistInStorage(filename: String): Boolean {
        var filePath = "${context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)}/$filename"
        var file = File(filePath)
        return file.exists()
    }

    fun getSlideDurations(): LongArray {
        Log.d("abhi", "slideDurationFile :: ${fileDescriptors.size}")
        if (fileDescriptors.size != null && fileDescriptors.size != 0) {
            var durationsArray = LongArray(fileDescriptors.size)
            var i = 0
            fileDescriptors.forEach {
                durationsArray[i] = it.interval.toLong()
                i++
            }
            return durationsArray
        } else {
            return staticDurations
        }
    }

}