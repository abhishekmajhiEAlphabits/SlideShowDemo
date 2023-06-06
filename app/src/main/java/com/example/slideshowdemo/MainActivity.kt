package com.example.slideshowdemo

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import android.widget.VideoView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.example.slideshowdemo.library.StoriesProgressView
import com.example.slideshowdemo.model.FileDescriptors
import com.example.slideshowdemo.model.Slide
import com.example.slideshowdemo.network.PlaylistManager
import com.example.slideshowdemo.receivers.DownloadsReceiver


class MainActivity : AppCompatActivity(), StoriesProgressView.StoriesListener {


    private val PROGRESS_COUNT = 6

    private lateinit var storiesProgressView: StoriesProgressView
    private var image: ImageView? = null
    private var video: VideoView? = null

    private var counter = 0

    private var i = 2
//    private var durations2 = longArrayOf()

    private lateinit var downloadsReceiver: DownloadsReceiver
    private lateinit var fileDescriptors: ArrayList<FileDescriptors>
    private lateinit var playlistManager: PlaylistManager

    private val path =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .getPath() + "/sample.mp4";
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

        try {
            downloadsReceiver = DownloadsReceiver()
            registerDownloadReceiver()

            //api call
            playlistManager = PlaylistManager(applicationContext)
            playlistManager.getPlayListData()

            //init fileDescriptors arrayList
            fileDescriptors = ArrayList<FileDescriptors>()
            fileDescriptors.clear()

//            playlistApiCall()
            Log.d("abhi", "descripMain :: $fileDescriptors")

            if (fileDescriptors.isNotEmpty()) {
                //for video
                if (fileDescriptors[0].contentType == 3 && fileDescriptors[0].isFileExist) {
                    val videoUri = Uri.parse(fileDescriptors[0].slideFilePath)
                    video!!.setVideoURI(videoUri)
                    video!!.setOnPreparedListener {
                        // mediaPlayer.start()
                        video!!.start()
                    }
                } else if (fileDescriptors[0].contentType == 3 && !fileDescriptors[0].isFileExist) {
                    video!!.visibility = View.GONE
                    image!!.setImageResource(R.drawable.ic_launcher_background)
                }

                //for image
                else if (fileDescriptors[0].contentType == 2 && fileDescriptors[0].isFileExist) {
                    val imageUri = Uri.parse(fileDescriptors[0].slideFilePath)
                    Log.d("abhi", "image uri :: $imageUri")
                    image!!.setImageURI(imageUri)
                } else if (fileDescriptors[0].contentType == 2 && !fileDescriptors[0].isFileExist) {
                    image!!.setImageResource(R.drawable.ic_launcher_background)
                } else {
                    Log.d("abhi", "something wrong")
                }


                // bind reverse view
                var reverse = findViewById<View>(R.id.reverse)
                reverse.setOnClickListener(
                    object : View.OnClickListener {
                        override fun onClick(v: View) {
                            storiesProgressView.reverse()
                        }
                    })
                reverse.setOnTouchListener(onTouchListener)


                // bind skip view
                var skip = findViewById<View>(R.id.skip)
                skip.setOnClickListener(
                    object : View.OnClickListener {
                        override fun onClick(v: View) {
                            storiesProgressView.skip()
                        }
                    })
                skip.setOnTouchListener(onTouchListener)
            } else {
                Log.d("abhi", "descripElse :: $fileDescriptors")
            }
        } catch (e: Exception) {
            Log.d("abhi", "error in onCreate :: $e")
        }

        storiesProgressView = findViewById<StoriesProgressView>(R.id.stories)
        storiesProgressView.setStoriesCount(PROGRESS_COUNT)
//        storiesProgressView.setStoryDuration(3000L);
        // or
        //        storiesProgressView.setStoryDuration(3000L);
        // or
        storiesProgressView.setStoriesCountWithDurations(durations)
        storiesProgressView.setStoriesListener(this)
//        storiesProgressView.startStories();
        //        storiesProgressView.startStories();
        counter = 0
        storiesProgressView.startStories(counter)

        image = findViewById<ImageView>(R.id.image)
        video = findViewById<VideoView>(R.id.video)

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val params = video!!.layoutParams as FrameLayout.LayoutParams
        params.width = metrics.widthPixels
        params.height = metrics.heightPixels
        params.leftMargin = 0
        video!!.layoutParams = params

        setupObservers()
        getStoragePermission()
    }

    private fun setupObservers() {
        downloadsReceiver.downloadState.observe(this, Observer {
            Log.d("abhi", "inside observer")
            getMediaFilePaths()
        })
    }

    private fun registerDownloadReceiver() {
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        registerReceiver(downloadsReceiver, filter)
    }

    private fun getMediaFilePaths() {
        Thread(Runnable {
            kotlin.run {
//                playlistManager.getDownloadedFilePath().forEach {
//                    fileDescriptors.add(it)
//                }
                fileDescriptors = playlistManager.getDownloadedFilePath()
            }
        }).start()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getStoragePermission(): Boolean {
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.d("abhi", "Permission is granted");
            //File write logic here
            return true;
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1234
            );
            return false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListener = OnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                pressTime = System.currentTimeMillis()
                storiesProgressView.pause()
                return@OnTouchListener false
            }
            MotionEvent.ACTION_UP -> {
                val now = System.currentTimeMillis()
                storiesProgressView.resume()
                return@OnTouchListener limit < now - pressTime
            }
        }
        false
    }

    override fun onNext() {
        try {
            var size = fileDescriptors.size + 2
            var durations2: LongArray = LongArray(size)
            durations2[0] = 10
            durations2[1] = 2000
            durations2[i] = fileDescriptors[i - 2].interval.toLong() * 1000
//            durations2[i] = 2000
            i++
            storiesProgressView.setStoriesCountWithDurations(durations2)
            playSlideShow()
        } catch (e: Exception) {
            Log.d("abhi", "error :: $e")
        }
        Log.d("abhi", "descripNext :: $fileDescriptors")
    }

    override fun onPrev() {
        if (counter - 1 < 0) return
        //        image.setImageResource(resources[--counter]);
        reverseSlideShow()
    }

    override fun onComplete() {}

    override fun onDestroy() {
        // Very important !
        storiesProgressView!!.destroy()
        unregisterReceiver(downloadsReceiver)
        super.onDestroy()
    }

    private fun playSlideShow() {
        Log.d("abhi", "descripPlay :: $fileDescriptors")
        //for video
        if (fileDescriptors[counter].contentType === 3 && fileDescriptors[counter].isFileExist) {
            try {
                image!!.visibility = View.GONE
                video!!.visibility = View.VISIBLE
                val videoUri = Uri.parse(fileDescriptors[counter].slideFilePath)
                Log.d("abhi", "uri : $videoUri")
                video!!.setVideoURI(videoUri)
                video!!.setOnPreparedListener {
                    // mediaPlayer.start();
                    video!!.start()
                    val animSlide = AnimationUtils.loadAnimation(
                        applicationContext,
                        R.anim.slide
                    )
                    video!!.startAnimation(animSlide)
                }
            } catch (e: Exception) {
                Log.d("abhi", e.toString())
            }
        } else if (fileDescriptors[counter].contentType == 3 && !fileDescriptors[counter].isFileExist) {
            video!!.visibility = View.GONE
            Log.d("abhi", "no video")
            image!!.setImageResource(R.drawable.ic_launcher_background)
        }

        //for image
        else if (fileDescriptors[counter].contentType == 2 && fileDescriptors[counter].isFileExist) {
            video!!.stopPlayback()
            video!!.clearAnimation()
            video!!.visibility = View.GONE
            // Animation animation = new ScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f, Animation.RELATIVE_TO_SELF,1.0f, Animation.RELATIVE_TO_SELF, 0.5f);
            image!!.visibility = View.VISIBLE


            val imageUri = Uri.parse(fileDescriptors[counter].slideFilePath)
            Log.d("abhi", "image uri :: $imageUri")
            image!!.setImageURI(imageUri)


            // Load the animation like this
            val animSlide = AnimationUtils.loadAnimation(
                applicationContext, R.anim.slide
            )
            image!!.startAnimation(animSlide)
        } else if (fileDescriptors[counter].contentType === 2 && !fileDescriptors[counter].isFileExist) {
            image!!.visibility = View.VISIBLE
            Toast.makeText(applicationContext, "Please wait...", Toast.LENGTH_SHORT).show()
            Log.d("abhi", "no image")
            image!!.setImageResource(R.drawable.ic_launcher_background)
        } else {
            Log.d("abhi", "something wrong")
        }
        counter++
    }

    private fun reverseSlideShow() {
        if (slides[--counter].imgRes === 0) {
            try {
                image!!.visibility = View.GONE
                video!!.visibility = View.VISIBLE
                val videoUri = Uri.parse(fileDescriptors[counter].slideFilePath)
                Log.d("abhi", "uri : $videoUri")
                video!!.setVideoURI(videoUri)
                video!!.setOnPreparedListener {
//                    mediaPlayer.start();
                    video!!.start()
                }
            } catch (e: Exception) {
                Log.d("abhi", e.toString())
            }
        }
        if (fileDescriptors[++counter].contentType === 2) {
            video!!.visibility = View.GONE
            image!!.visibility = View.VISIBLE
            image!!.setImageResource(R.drawable.ic_launcher_background)
            Log.d("abhi", "imgRes : " + slides[counter].imgRes)
        }
    }

}