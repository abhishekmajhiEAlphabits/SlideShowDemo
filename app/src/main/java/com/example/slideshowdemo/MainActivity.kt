package com.example.slideshowdemo

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
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
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.example.slideshowdemo.library.StoriesProgressView
import com.example.slideshowdemo.model.FileDescriptors
import com.example.slideshowdemo.model.Slide
import com.example.slideshowdemo.network.PlaylistManager

class MainActivity : AppCompatActivity(), StoriesProgressView.StoriesListener {


    private val PROGRESS_COUNT = 6

    private lateinit var storiesProgressView: StoriesProgressView
    private var image: ImageView? = null
    private var video: VideoView? = null

    private var counter = 0

    private lateinit var fileDescriptors: ArrayList<FileDescriptors>

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

//    private val durations = longArrayOf(
//        10000, 3000, 4000, 7000, 6000, 9000
//    )

    private val durations = longArrayOf(
        10000, 3000, 4000, 7000, 6000, 9000
    )

    var pressTime = 0L
    var limit = 500L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

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

        //api call
        val playlistManager = PlaylistManager(applicationContext)
        playlistManager.getPlayListData()

        fileDescriptors = ArrayList<FileDescriptors>()
        fileDescriptors.clear()

//        fileDescriptors.add(FileDescriptors(106,3,"/storage/emulated/0/Android/data/com.example.slideshowdemo/files/Download/sample.mp4"))
//       fileDescriptors = playlistManager.getDownloadedFilePath()
        playlistManager.getDownloadedFilePath().forEach {
            fileDescriptors.add(it)
        }
        Log.d("abhi", "descrip :: $fileDescriptors")

        if (fileDescriptors.isNotEmpty()) {


            if (fileDescriptors[counter].contentType == 3 && fileDescriptors[counter].isFileExist) {
                val videoUri = Uri.parse(fileDescriptors[0].slideFilePath)
                video!!.setVideoURI(videoUri)
                video!!.setOnPreparedListener {
                    // mediaPlayer.start()
                    video!!.start()
                }
            }
            if (fileDescriptors[counter].contentType == 3 && !fileDescriptors[counter].isFileExist) {
                video!!.visibility = View.GONE
                image!!.setImageResource(R.drawable.ic_launcher_background)
            }

            //for image
            if (fileDescriptors[counter].contentType == 2 && fileDescriptors[counter].isFileExist) {
                val imageUri = Uri.parse(fileDescriptors[0].slideFilePath)
                image!!.setImageURI(imageUri)
            }
            if (fileDescriptors[counter].contentType == 2 && !fileDescriptors[counter].isFileExist) {
                image!!.setImageResource(R.drawable.ic_launcher_background)
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
            Log.d("abhi", "descrip :: $fileDescriptors")
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
//        Log.d("abhi", resources[counter]);
//        image.setImageResource(resources[++counter]);
        playSlideShow()
        Log.d("abhi", "descrip :: $fileDescriptors")
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
        super.onDestroy()
    }

    private fun playSlideShow() {
        if (fileDescriptors[++counter].contentType === 3 && fileDescriptors[counter].isFileExist) {
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
        }
        if (fileDescriptors[counter].contentType == 3 && !fileDescriptors[counter].isFileExist) {
            video!!.visibility = View.GONE
            image!!.setImageResource(R.drawable.ic_launcher_background)
        }

        //for image
        if (fileDescriptors[++counter].contentType == 2 && fileDescriptors[counter].isFileExist) {
            video!!.stopPlayback()
            video!!.clearAnimation()
            video!!.visibility = View.GONE
            // Animation animation = new ScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f, Animation.RELATIVE_TO_SELF,1.0f, Animation.RELATIVE_TO_SELF, 0.5f);
            image!!.visibility = View.VISIBLE
            image!!.setImageResource(R.drawable.ic_launcher_background)
            Log.d("abhi", "imgRes : " + slides[counter].imgRes)
            // Load the animation like this
            val animSlide = AnimationUtils.loadAnimation(
                applicationContext, R.anim.slide
            )
            image!!.startAnimation(animSlide)
        }
        if (fileDescriptors[counter].contentType === 2 && !fileDescriptors[counter].isFileExist) {
            image!!.setImageResource(R.drawable.ic_launcher_background)
        }
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