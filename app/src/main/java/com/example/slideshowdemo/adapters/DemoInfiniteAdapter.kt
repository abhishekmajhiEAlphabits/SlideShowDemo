package com.example.slideshowdemo.adapters

import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import com.example.slideshowdemo.R
import com.example.slideshowdemo.loopingviewpager.LoopingPagerAdapter
import com.example.slideshowdemo.model.FileDescriptors
import kotlin.collections.ArrayList


class DemoInfiniteAdapter(
    itemList: ArrayList<FileDescriptors>,
    isInfinite: Boolean
) : LoopingPagerAdapter<Int>(itemList, isInfinite) {


    override fun getItemViewType(listPosition: Int): Int {
//        return if (itemList!![listPosition].contentType == 3) VIEW_TYPE_SPECIAL else VIEW_TYPE_NORMAL
//        return VIEW_TYPE_NORMAL
        return if (itemList!![listPosition].contentType == 3) 3
        else 2
    }

    override fun inflateView(
        viewType: Int,
        container: ViewGroup,
        listPosition: Int
    ): View {
//        return if (viewType == VIEW_TYPE_SPECIAL) LayoutInflater.from(
//            container.context
//        ).inflate(
//            R.layout.item_special,
//            container,
//            false
//        ) else LayoutInflater.from(container.context)
//            .inflate(R.layout.item_page, container, false)
//
        return LayoutInflater.from(container.context)
            .inflate(R.layout.item_page, container, false)
    }

    override fun bindView(
        convertView: View,
        listPosition: Int,
        viewType: Int
    ) {
        var videoView: VideoView? = null
        var imageView: ImageView? = null
        videoView = convertView.findViewById<View>(R.id.video) as VideoView
        imageView = convertView.findViewById<View>(R.id.image) as ImageView
        val description = convertView.findViewById<TextView>(R.id.description)
//        val loading = convertView.findViewById<View>(R.id.loadingTxt)
//        if (listPosition == 1) {
//            Log.d("abhi", "list :: $listPosition")
//        } else {
//            Log.d("abhi", "list :: $listPosition")
//        }
        if (viewType == 3) {
            Log.d("abhi", "listIf :: $listPosition :: ${itemList!![listPosition].slideFilePath}")
//            loading.visibility = View.GONE
            imageView.visibility = View.GONE
            if (itemList!![listPosition].slideFilePath != null && itemList!![listPosition].isFileExist) {
                videoView.visibility = View.VISIBLE
                videoView.setVideoURI(Uri.parse(itemList!![listPosition].slideFilePath))
//                videoView.setVideoURI(Uri.parse("android.resource://com.example.slideshowdemo/raw/sample"))
//                videoView.start()
                videoView.setOnPreparedListener {
//                    it.start()
                    videoView!!.start()
                }
            } else {
                videoView.stopPlayback()
                videoView.visibility = View.GONE
                imageView.visibility = View.VISIBLE
                imageView.setImageResource(R.drawable.loading)
//                loading.visibility = View.VISIBLE
            }
        } else if (viewType == 2) {
            Log.d("abhi", "listElseIf :: $listPosition")
            videoView.stopPlayback()
            videoView.visibility = View.GONE
//            loading.visibility = View.GONE
            imageView.visibility = View.VISIBLE
            if (itemList!![listPosition].slideFilePath != null && itemList!![listPosition].isFileExist) {
                imageView.setImageURI(Uri.parse(itemList!![listPosition].slideFilePath))
            } else {
//                imageView.visibility = View.GONE
//                loading.visibility = View.VISIBLE
                imageView.visibility = View.VISIBLE
                imageView.setImageResource(R.drawable.loading)
            }
//
//            convertView.findViewById<View>(R.id.image)
//                .setBackgroundColor(
//                    convertView.context.resources.getColor(
//                        getBackgroundColor(
//                            listPosition
//                        )
//                    )
//                )

        } else {
            Log.d("abhi", "listElse :: $listPosition")
            videoView = convertView.findViewById<View>(R.id.video) as VideoView
            videoView!!.stopPlayback()
            videoView!!.visibility = View.GONE
            imageView.visibility = View.VISIBLE
            imageView.setImageResource(R.drawable.loading)
//            loading.visibility = View.VISIBLE
//            convertView.findViewById<View>(R.id.image)
//                .setBackgroundColor(
//                    convertView.context.resources.getColor(
//                        getBackgroundColor(
//                            listPosition
//                        )
//                    )
//                )
        }
        Log.d("abhi", "descripFiledeabh :: $itemList")
//        val description = convertView.findViewById<TextView>(R.id.description)
//        description.text = itemList?.get(listPosition).toString()
    }

    private fun getBackgroundColor(number: Int): Int {
        return when (number) {
            0 -> android.R.color.holo_red_light
            1 -> android.R.color.holo_orange_light
            2 -> android.R.color.holo_green_light
            3 -> android.R.color.holo_blue_light
            4 -> android.R.color.holo_purple
            5 -> android.R.color.black
            else -> android.R.color.black
        }
    }

    fun setFileDescriptors(fileDescriptors: ArrayList<FileDescriptors>) {
        if (this.itemList!!.size != 6) {
            this.itemList = fileDescriptors
            notifyDataSetChanged()
        }
    }

    companion object {
        private const val VIEW_TYPE_NORMAL = 100
        private const val VIEW_TYPE_SPECIAL = 101
    }
}