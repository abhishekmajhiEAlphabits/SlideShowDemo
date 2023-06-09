package com.example.slideshowdemo.adapters

import android.content.Context
import com.example.slideshowdemo.loopingviewpager.LoopingViewPager
import com.example.slideshowdemo.network.PlaylistManager
import javax.inject.Singleton

@Singleton
class FilePopulator(context: Context) {
    private var context = context
    private lateinit var playlistManager: PlaylistManager
    private lateinit var viewPager: LoopingViewPager
    private var adapter: DemoInfiniteAdapter? = null

    fun initFilePopulator(){
//        initAdapter()
        playlistApiCall()
    }


    private fun playlistApiCall() {
        //api call
        Thread(Runnable {
            kotlin.run {
                playlistManager = PlaylistManager(context)
                playlistManager.getPlayListData()
            }
        }).start()
    }



}
