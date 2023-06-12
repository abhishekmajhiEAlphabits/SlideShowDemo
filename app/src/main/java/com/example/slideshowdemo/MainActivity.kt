package com.example.slideshowdemo

import android.app.DownloadManager
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.example.slideshowdemo.adapters.DemoInfiniteAdapter
import com.example.slideshowdemo.loopingviewpager.LoopingViewPager
import com.example.slideshowdemo.model.FileDescriptors
import com.example.slideshowdemo.model.Slide
import com.example.slideshowdemo.network.PlaylistManager
import com.example.slideshowdemo.receivers.DownloadsReceiver


class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: LoopingViewPager
    private var adapter: DemoInfiniteAdapter? = null

    private var counter = 0

    private var i = 2

    private lateinit var downloadsReceiver: DownloadsReceiver
    private lateinit var fileDescriptors: ArrayList<FileDescriptors>
    private lateinit var playlistManager: PlaylistManager

    private val path =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .getPath() + "/sample.mp4"
//    var path = "android.resource://jp.shts.android.storyprogressbar/raw/sample"

    private val slides: Array<Slide> = arrayOf<Slide>(
        Slide(0, path),
        Slide(R.drawable.ic_launcher_background, "0"),
        Slide(0, path),
        Slide(R.drawable.ic_launcher_background, "0"),
        Slide(0, path),
        Slide(R.drawable.ic_launcher_background, "0")
    )

//    private val resources = intArrayOf(
//        R.drawable.sportscar,
//        R.drawable.sample2,
//        R.drawable.sportscar,
//        R.drawable.sample2,
//        R.drawable.sportscar,
//        R.drawable.sample2
//    )

    private val durations = longArrayOf(
        10, 2000, 4000, 4000, 4000, 4000, 4000, 5000
    )

    var pressTime = 0L
    var limit = 500L

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
        viewPager = findViewById(R.id.viewpager)

        try {
            getStoragePermission()

            //api call
            Thread(Runnable {
                kotlin.run {
                    playlistManager = PlaylistManager(this)
                    playlistManager.getPlayListData()
                }
            }).start()

            //init fileDescriptors arrayList
            fileDescriptors = ArrayList<FileDescriptors>()
            fileDescriptors.clear()

            fileDescriptors.add(FileDescriptors(100,2,"android.resource://com.example.slideshowdemo/raw/loading",true,10))
            fileDescriptors.add(FileDescriptors(100,2,"android.resource://com.example.slideshowdemo/raw/loading",true,10))
//            fileDescriptors.add(FileDescriptors(100,2,"android.resource://com.example.slideshowdemo/raw/sample",true,10))
//            fileDescriptors.add(FileDescriptors(100,2,"hgh",true,10))
//            fileDescriptors.add(FileDescriptors(100,2,"hgh",true,10))
//            fileDescriptors.add(FileDescriptors(100,2,"hgh",true,10))
//            fileDescriptors.add(FileDescriptors(100,2,"hgh",true,10))

            adapter = DemoInfiniteAdapter(fileDescriptors, true)
            viewPager.adapter = adapter

            downloadsReceiver = DownloadsReceiver()
            registerDownloadReceiver()

            Log.d("abhi", "descripMain :: $fileDescriptors")

            setupObservers()
        } catch (e: Exception) {
            Log.d("abhi", "error in onCreate :: $e")
        }
    }

    private fun createDummyItems(): java.util.ArrayList<Int> {
        val items = java.util.ArrayList<Int>()
        items.add(0, 1)
        items.add(1, 2)
        items.add(2, 3)
        items.add(3, 4)
        items.add(4, 5)
        items.add(5, 6)
        items.add(6, 0)

        return items
    }

    private fun setupObservers() {
        downloadsReceiver.downloadState.observe(this, Observer {
            Log.d("abhi", "inside observer")
            getMediaFilePaths()
            adapter!!.setFileDescriptors(fileDescriptors)
//            viewPager.reset()
        })
        playlistManager.fileDescriptorData.observe(this, Observer {
            Log.d("abhi", "inside file observer")
            getMediaFilePaths()
            adapter!!.setFileDescriptors(fileDescriptors)
//            viewPager.reset()
        })
    }

    private fun registerDownloadReceiver() {
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        registerReceiver(downloadsReceiver, filter)
    }

    private fun getMediaFilePaths() {
        fileDescriptors = playlistManager.getDownloadedFilePath()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getStoragePermission(): Boolean {
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.d("abhi", "Permission is granted")
            //File write logic here
            return true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1234
            )
            return false
        }
    }


    override fun onDestroy() {
        unregisterReceiver(downloadsReceiver)
        super.onDestroy()
    }

}