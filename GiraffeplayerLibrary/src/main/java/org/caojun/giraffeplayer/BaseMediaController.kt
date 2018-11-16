package org.caojun.giraffeplayer

import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

import com.github.tcking.viewquery.ViewQuery

/**
 * base of media controller
 * Created by tcking on 2017.
 */

abstract class BaseMediaController(protected val context: Context) : DefaultPlayerListener(), MediaController, Handler.Callback {
    protected val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    protected var `$`: ViewQuery? = null

//    protected var handler: Handler? = null
    protected val handler = Handler(Looper.getMainLooper(), this)
    protected var videoView: VideoView? = null
//    protected var controllerView: View? = null

//    init {
//        handler = Handler(Looper.getMainLooper(), this)
//    }


    override fun bind(videoView: VideoView) {
        this.videoView = videoView
        val controllerView = makeControllerView()
        `$` = ViewQuery(controllerView)
        initView(controllerView)
        val layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        this.videoView?.container?.addView(controllerView, layoutParams)
    }

    protected abstract fun makeControllerView(): View

    protected abstract fun initView(view: View)

    companion object {

        val MESSAGE_SHOW_PROGRESS = 1
        val MESSAGE_FADE_OUT = 2
        val MESSAGE_SEEK_NEW_POSITION = 3
        val MESSAGE_HIDE_CENTER_BOX = 4
        val MESSAGE_RESTART_PLAY = 5

        val DefaultTimeout = 3 * 1000
    }

}
