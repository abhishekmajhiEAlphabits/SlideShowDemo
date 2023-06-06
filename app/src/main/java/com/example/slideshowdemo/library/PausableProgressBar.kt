package com.example.slideshowdemo.library

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation
import android.view.animation.Transformation
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.AttrRes
import com.example.slideshowdemo.R

class PausableProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    /***
     * progress
     */
    private val DEFAULT_PROGRESS_DURATION = 2000

    private var frontProgressView: View? = null
    private var maxProgressView: View? = null

    private var animation: PausableScaleAnimation? = null
    private var duration = DEFAULT_PROGRESS_DURATION.toLong()
    private var callback: Callback? = null

    interface Callback {
        fun onStartProgress()
        fun onFinishProgress()
    }

//    fun PausableProgressBar(context: Context) {
//        PausableProgressBar(context, null)
//    }
//
//    fun PausableProgressBar(context: Context, attrs: AttributeSet?) {
//        PausableProgressBar(context, attrs, 0)
//    }

    //
//    fun PausableProgressBar(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) {
//        super(context, attrs, defStyleAttr)
//        LayoutInflater.from(context).inflate(R.layout.pausable_progress, this)
//        frontProgressView = findViewById(R.id.front_progress)
//        maxProgressView = findViewById(R.id.max_progress) // work around
//    }

//    constructor(context: Context) {
//        PausableProgressBar(context, null)
//    }

    init {
        LayoutInflater.from(context).inflate(R.layout.pausable_progress, this)
        frontProgressView = findViewById(R.id.front_progress)
        maxProgressView = findViewById(R.id.max_progress) // work around
    }

    fun setDuration(duration: Long) {
        this.duration = duration
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    fun setMax() {
        finishProgress(true)
    }

    fun setMin() {
        finishProgress(false)
    }

    fun setMinWithoutCallback() {
//        maxProgressView.setBackgroundResource(R.color.progress_secondary);
        maxProgressView!!.visibility = VISIBLE
        if (animation != null) {
            animation!!.setAnimationListener(null)
            animation!!.cancel()
        }
    }

    fun setMaxWithoutCallback() {
//        maxProgressView.setBackgroundResource(R.color.progress_max_active);

//        maxProgressView.setVisibility(VISIBLE);
        maxProgressView!!.visibility = GONE
        if (animation != null) {
            animation!!.setAnimationListener(null)
            animation!!.cancel()
        }
    }

    private fun finishProgress(isMax: Boolean) {
//        if (isMax) maxProgressView.setBackgroundResource(R.color.progress_max_active);
//        maxProgressView.setVisibility(isMax ? VISIBLE : GONE);
        maxProgressView!!.visibility = if (isMax) GONE else GONE
        if (animation != null) {
            animation!!.setAnimationListener(null)
            animation!!.cancel()
            if (callback != null) {
                callback!!.onFinishProgress()
            }
        }
    }

    fun startProgress() {
        maxProgressView!!.visibility = GONE
        animation = PausableScaleAnimation(
            0f, 1f, 1f, 1f, Animation.ABSOLUTE, 0f, Animation.RELATIVE_TO_SELF, 0f
        )
        animation!!.duration = duration
        animation!!.interpolator = LinearInterpolator()
        animation!!.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
//                frontProgressView.setVisibility(View.VISIBLE);
                frontProgressView!!.visibility = GONE
                if (callback != null) callback!!.onStartProgress()
                val sec = duration.toString()
                Toast.makeText(
                    context,
                    "Slide Time : " + sec.toString() + " milliseconds",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                if (callback != null) callback!!.onFinishProgress()
            }
        })
        animation!!.fillAfter = true
        frontProgressView!!.startAnimation(animation)
    }

    fun pauseProgress() {
        if (animation != null) {
            animation!!.pause()
        }
    }

    fun resumeProgress() {
        if (animation != null) {
            animation!!.resume()
        }
    }

    fun clear() {
        if (animation != null) {
            animation!!.setAnimationListener(null)
            animation!!.cancel()
            animation = null
        }
    }

    private class PausableScaleAnimation constructor(
        fromX: Float, toX: Float, fromY: Float,
        toY: Float, pivotXType: Int, pivotXValue: Float, pivotYType: Int,
        pivotYValue: Float
    ) :
        ScaleAnimation(
            fromX, toX, fromY, toY, pivotXType, pivotXValue, pivotYType,
            pivotYValue
        ) {
        private var mElapsedAtPause: Long = 0
        private var mPaused = false
        override fun getTransformation(
            currentTime: Long,
            outTransformation: Transformation,
            scale: Float
        ): Boolean {
            if (mPaused && mElapsedAtPause == 0L) {
                mElapsedAtPause = currentTime - startTime
            }
            if (mPaused) {
                startTime = currentTime - mElapsedAtPause
            }
            return super.getTransformation(currentTime, outTransformation, scale)
        }

        /***
         * pause animation
         */
        fun pause() {
            if (mPaused) return
            mElapsedAtPause = 0
            mPaused = true
        }

        /***
         * resume animation
         */
        fun resume() {
            mPaused = false
        }
    }
}