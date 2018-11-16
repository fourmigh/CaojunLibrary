package org.caojun.giraffeplayer

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Message
import android.text.TextUtils
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.SeekBar
import java.util.Locale
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkTimedText

/**
 * media controller for ListView or RecyclerView
 * Created by tcking on 2017
 */

class DefaultMediaController(context: Context) : BaseMediaController(context) {

    private var newPosition: Long = -1
    private var isShowing: Boolean = false
    private var isDragging: Boolean = false

    private var instantSeeking: Boolean = false
    private var seekBar: SeekBar? = null

    private var volume = -1
    private val MaxVolume: Int = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)


    private var brightness: Float = 0.toFloat()
    private var status = STATUS_IDLE
    private var displayModel = GiraffePlayer.DISPLAY_NORMAL

    private val seekListener: SeekBar.OnSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (!fromUser)
                return
            if (!videoView!!.isCurrentActivePlayer) {
                return
            }
            `$`?.id(R.id.app_video_status)?.gone()//移动时隐藏掉状态image
            val player = videoView!!.player
            val newPosition = (player.duration * (progress * 1.0 / 1000)).toInt()
            val time = generateTime(newPosition.toLong())
            if (instantSeeking) {
                player.seekTo(newPosition)

            }
            `$`?.id(R.id.app_video_currentTime)?.text(time)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            isDragging = true
            show(3600000)
            handler.removeMessages(BaseMediaController.MESSAGE_SHOW_PROGRESS)
            if (instantSeeking) {
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true)
            }
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            if (!videoView!!.isCurrentActivePlayer) {
                return
            }
            val player = videoView!!.player
            if (!instantSeeking) {
                player.seekTo((player.duration * (seekBar.progress * 1.0 / 1000)).toInt())
            }
            show(DefaultTimeout)
            handler.removeMessages(BaseMediaController.MESSAGE_SHOW_PROGRESS)
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false)
            isDragging = false
            handler.sendEmptyMessageDelayed(BaseMediaController.MESSAGE_SHOW_PROGRESS, 1000)
        }
    }

    private val onClickListener: View.OnClickListener = View.OnClickListener { v ->
        val player = videoView!!.player
        if (v.id == R.id.app_video_fullscreen) {
            player.toggleFullScreen()
        } else if (v.id == R.id.app_video_play) {
            //                if (player.isPlaying()) {
            //                    player.pause();
            //                } else {
            //                    player.start();
            //                }
            player.togglePlayOrPause()
        } else if (v.id == R.id.app_video_replay_icon) {
            player.seekTo(0)
            player.start()
            //                videoView.seekTo(0);
            //                videoView.start();
            //                doPauseResume();
        } else if (v.id == R.id.app_video_finish) {
            if (!player.onBackPressed()) {
                (videoView?.context as Activity).finish()
            }
        } else if (v.id == R.id.app_video_float_close) {
            player.stop()
            player.setDisplayModel(GiraffePlayer.DISPLAY_NORMAL)
        } else if (v.id == R.id.app_video_float_full) {
            player.setDisplayModel(GiraffePlayer.DISPLAY_FULL_WINDOW)
        }
    }

    private val isRtl: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL
        } else false

    private fun generateTime(time: Long): String {
        val totalSeconds = (time / 1000).toInt()
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        return if (hours > 0) String.format("%02d:%02d:%02d", hours, minutes, seconds) else String.format("%02d:%02d", minutes, seconds)
    }

    private fun updatePausePlay() {
        if (videoView!!.isCurrentActivePlayer) {
            val playing = videoView!!.player.isPlaying
            if (playing) {
                `$`?.id(R.id.app_video_play)?.image(R.drawable.ic_stop)
            } else {
                `$`?.id(R.id.app_video_play)?.image(R.drawable.ic_play)
            }
        } else {
            `$`?.id(R.id.app_video_play)?.image(R.drawable.ic_play)
            `$`?.id(R.id.app_video_currentTime)?.text("")
            `$`?.id(R.id.app_video_endTime)?.text("")
        }
    }


    private fun setProgress(): Long {
        if (isDragging) {
            return 0
        }
        //check player is active
        val currentPlayer = videoView!!.isCurrentActivePlayer
        if (!currentPlayer) {
            seekBar?.progress = 0
            return 0
        }

        //check player is ready
        val player = videoView!!.player
        val currentState = player.currentState
        if (currentState == GiraffePlayer.STATE_IDLE ||
                currentState == GiraffePlayer.STATE_PREPARING ||
                currentState == GiraffePlayer.STATE_ERROR) {
            return 0
        }

        val position = player.currentPosition.toLong()
        val duration = player.duration

        if (seekBar != null) {
            if (duration > 0) {
                val pos = 1000L * position / duration
                seekBar?.progress = pos.toInt()
            }
            val percent = player.bufferPercentage
            seekBar?.secondaryProgress = percent * 10
        }

        `$`?.id(R.id.app_video_currentTime)?.text(generateTime(position))
        if (duration == 0) {//live stream
            `$`?.id(R.id.app_video_endTime)?.text(R.string.giraffe_player_live)
        } else {
            `$`?.id(R.id.app_video_endTime)?.text(generateTime(duration.toLong()))
        }
        return position
    }


    protected fun show(timeout: Int) {
        if (!isShowing) {
            if (videoView!!.videoInfo.isShowTopBar() || displayModel == GiraffePlayer.DISPLAY_FULL_WINDOW) {
                `$`?.id(R.id.app_video_top_box)?.visible()
                `$`?.id(R.id.app_video_title)?.text(videoView!!.videoInfo.getTitle())
            } else {
                `$`?.id(R.id.app_video_top_box)?.gone()
            }
            showBottomControl(true)
            isShowing = true
        }
        updatePausePlay()
        handler.sendEmptyMessage(BaseMediaController.MESSAGE_SHOW_PROGRESS)
        handler.removeMessages(BaseMediaController.MESSAGE_FADE_OUT)
        if (timeout != 0) {
            handler.sendMessageDelayed(handler.obtainMessage(BaseMediaController.MESSAGE_FADE_OUT), timeout.toLong())
        }

    }


    private fun showBottomControl(show: Boolean) {
        var show = show
        if (displayModel == GiraffePlayer.DISPLAY_FLOAT) {
            show = false
        }
        //        $.id(R.id.app_video_play).visibility(show ? View.VISIBLE : View.GONE);
        //        $.id(R.id.app_video_currentTime).visibility(show ? View.VISIBLE : View.GONE);
        //        $.id(R.id.app_video_endTime).visibility(show ? View.VISIBLE : View.GONE);
        //        $.id(R.id.app_video_seekBar).visibility(show ? View.VISIBLE : View.GONE);
        //        $.id(R.id.app_video_fullscreen).visibility(show ? View.VISIBLE : View.GONE);
        `$`?.id(R.id.app_video_bottom_box)?.visibility(if (show) View.VISIBLE else View.GONE)

    }

    private fun hide(force: Boolean) {
        if (force || isShowing) {
            handler.removeMessages(BaseMediaController.MESSAGE_SHOW_PROGRESS)
            showBottomControl(false)
            `$`?.id(R.id.app_video_top_box)?.gone()
            //            $.id(R.id.app_video_fullscreen).invisible();
            isShowing = false
        }

    }

    override fun makeControllerView(): View {
        return LayoutInflater.from(context).inflate(R.layout.giraffe_media_controller, videoView, false)
    }

    override fun initView(view: View) {
        seekBar = `$`?.id(R.id.app_video_seekBar)?.view()
        seekBar?.max = 1000
        seekBar?.setOnSeekBarChangeListener(seekListener)
        `$`?.id(R.id.app_video_play)?.clicked(onClickListener)?.imageView()?.rotation = (if (isRtl) 180 else 0).toFloat()
        `$`?.id(R.id.app_video_fullscreen)?.clicked(onClickListener)
        `$`?.id(R.id.app_video_finish)?.clicked(onClickListener)?.imageView()?.rotation = (if (isRtl) 180 else 0).toFloat()
        `$`?.id(R.id.app_video_replay_icon)?.clicked(onClickListener)?.imageView()?.rotation = (if (isRtl) 180 else 0).toFloat()
        `$`?.id(R.id.app_video_float_close)?.clicked(onClickListener)
        `$`?.id(R.id.app_video_float_full)?.clicked(onClickListener)


        val gestureDetector = GestureDetector(context, createGestureListener())
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        view.setOnTouchListener(View.OnTouchListener { v, event ->
            if (displayModel == GiraffePlayer.DISPLAY_FLOAT) {
                return@OnTouchListener false
            }

            if (gestureDetector.onTouchEvent(event)) {
                return@OnTouchListener true
            }

            // 处理手势结束
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_OUTSIDE -> endGesture()
            }
            true
        })
    }

    private fun createGestureListener(): GestureDetector.OnGestureListener {
        return PlayerGestureListener()
    }

    //    public class LiteGestureListener extends GestureDetector.SimpleOnGestureListener {
    //        @Override
    //        public boolean onSingleTapUp(MotionEvent e) {
    //            boolean currentPlayer = videoView.isCurrentActivePlayer();
    //            if (!currentPlayer) {
    //                return true;
    //            }
    //            if (isShowing) {
    //                hide(false);
    //            } else {
    //                show(defaultTimeout);
    //            }
    //            return true;
    //        }
    //    }

    override fun handleMessage(msg: Message): Boolean {
        var msg = msg
        when (msg.what) {
            BaseMediaController.MESSAGE_FADE_OUT -> hide(false)
            BaseMediaController.MESSAGE_HIDE_CENTER_BOX -> {
                `$`?.id(R.id.app_video_volume_box)?.gone()
                `$`?.id(R.id.app_video_brightness_box)?.gone()
                `$`?.id(R.id.app_video_fastForward_box)?.gone()
            }
            BaseMediaController.MESSAGE_SEEK_NEW_POSITION -> if (newPosition >= 0) {
                videoView?.player?.seekTo(newPosition.toInt())
                newPosition = -1
            }
            BaseMediaController.MESSAGE_SHOW_PROGRESS -> {
                setProgress()
                if (!isDragging && isShowing) {
                    msg = handler.obtainMessage(BaseMediaController.MESSAGE_SHOW_PROGRESS)
                    handler.sendMessageDelayed(msg, 300)
                    updatePausePlay()
                }
            }
            BaseMediaController.MESSAGE_RESTART_PLAY -> {
            }
        }//                        play(url);
        return true
    }

    override fun onCompletion(giraffePlayer: GiraffePlayer) {
        statusChange(STATUS_COMPLETED)
    }

    override fun onRelease(giraffePlayer: GiraffePlayer) {
        handler.removeCallbacksAndMessages(null)

        `$`?.id(R.id.app_video_play)?.image(R.drawable.ic_play)
        `$`?.id(R.id.app_video_currentTime)?.text("")
        `$`?.id(R.id.app_video_endTime)?.text("")

        //1.set the cover view visible
        `$`?.id(R.id.app_video_cover)?.visible()
        //2.set current view as cover
        val videoInfo = videoView!!.videoInfo
        if (videoInfo.isCurrentVideoAsCover) {
            if (giraffePlayer.currentState != GiraffePlayer.STATE_ERROR) {
                val currentDisplay = giraffePlayer.currentDisplay
                if (currentDisplay != null) {
                    val imageView = `$`?.id(R.id.app_video_cover)?.imageView()
                    if (imageView != null) {
                        val aspectRatio = videoInfo.getAspectRatio()
                        when (aspectRatio) {
                            VideoInfo.AR_ASPECT_FILL_PARENT -> imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                            VideoInfo.AR_MATCH_PARENT -> imageView.scaleType = ImageView.ScaleType.FIT_XY
                            VideoInfo.AR_ASPECT_WRAP_CONTENT -> imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                            else -> imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                        }
                        imageView.setImageBitmap(currentDisplay.bitmap)
                    }
                }
            }
        }

    }

    override fun onStart(giraffePlayer: GiraffePlayer) {
        `$`?.id(R.id.app_video_replay)?.gone()
        show(DefaultTimeout)
    }


    private fun endGesture() {
        volume = -1
        brightness = -1f
        if (newPosition >= 0) {
            handler.removeMessages(BaseMediaController.MESSAGE_SEEK_NEW_POSITION)
            handler.sendEmptyMessage(BaseMediaController.MESSAGE_SEEK_NEW_POSITION)
        }
        handler.removeMessages(BaseMediaController.MESSAGE_HIDE_CENTER_BOX)
        handler.sendEmptyMessageDelayed(BaseMediaController.MESSAGE_HIDE_CENTER_BOX, 500)
    }

    inner class PlayerGestureListener : GestureDetector.SimpleOnGestureListener() {
        private var firstTouch: Boolean = false
        private var volumeControl: Boolean = false
        private var toSeek: Boolean = false

        /**
         * 双击
         */
        override fun onDoubleTap(e: MotionEvent): Boolean {
            //            Toast.makeText(context, "onDoubleTap", Toast.LENGTH_SHORT).show();
            val player = videoView?.player
            player?.togglePlayOrPause()
            return true
        }

        override fun onDown(e: MotionEvent): Boolean {
            firstTouch = true
            return true

        }

        /**
         * 滑动
         */
        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            //1. if not the active player,ignore
            val currentPlayer = videoView!!.isCurrentActivePlayer
            if (!currentPlayer) {
                return true
            }

            val oldX = e1.x
            val oldY = e1.y
            val deltaY = oldY - e2.y
            val deltaX = oldX - e2.x
            if (firstTouch) {
                toSeek = Math.abs(distanceX) >= Math.abs(distanceY)
                volumeControl = oldX > videoView!!.width * 0.5f
                firstTouch = false
            }
            val player = videoView!!.player
            if (toSeek) {
                if (player.canSeekForward()) {
                    onProgressSlide(-deltaX / videoView!!.width)
                }
            } else {
                //if player in list controllerView,ignore
                if (displayModel == GiraffePlayer.DISPLAY_NORMAL && videoView!!.inListView()) {
                    return true
                }
                val percent = deltaY / videoView!!.height
                if (volumeControl) {
                    onVolumeSlide(percent)
                } else {
                    onBrightnessSlide(percent)
                }
            }
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            if (isShowing) {
                hide(false)
            } else {
                show(DefaultTimeout)
            }
            return true
        }
    }


    /**
     * 滑动改变声音大小
     *
     * @param percent
     */
    private fun onVolumeSlide(percent: Float) {
        if (volume == -1) {
            volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            if (volume < 0)
                volume = 0
        }
        hide(true)

        var index = (percent * MaxVolume).toInt() + volume
        if (index > MaxVolume)
            index = MaxVolume
        else if (index < 0)
            index = 0

        // 变更声音
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0)

        // 变更进度条
        val i = (index * 1.0 / MaxVolume * 100).toInt()
        var s = i.toString() + "%"
        if (i == 0) {
            s = "off"
        }
        // 显示
        `$`?.id(R.id.app_video_volume_icon)?.image(if (i == 0) R.drawable.ic_volume_off else R.drawable.ic_volume_on)
        `$`?.id(R.id.app_video_brightness_box)?.gone()
        `$`?.id(R.id.app_video_volume_box)?.visible()
        `$`?.id(R.id.app_video_volume_box)?.visible()
        `$`?.id(R.id.app_video_volume)?.text(s)?.visible()
    }

    private fun onProgressSlide(percent: Float) {
        val player = videoView!!.player
        val position = player.currentPosition.toLong()
        val duration = player.duration.toLong()
        val deltaMax = Math.min((100 * 1000).toLong(), duration - position)
        var delta = (deltaMax * percent).toLong()
        if (isRtl) {
            delta *= -1
        }

        newPosition = delta + position
        if (newPosition > duration) {
            newPosition = duration
        } else if (newPosition <= 0) {
            newPosition = 0
            delta = -position
        }
        val showDelta = delta.toInt() / 1000
        if (showDelta != 0) {
            `$`?.id(R.id.app_video_fastForward_box)?.visible()
            val text = if (showDelta > 0) "+$showDelta" else "" + showDelta
            `$`?.id(R.id.app_video_fastForward)?.text(text + "s")
            `$`?.id(R.id.app_video_fastForward_target)?.text(generateTime(newPosition) + "/")
            `$`?.id(R.id.app_video_fastForward_all)?.text(generateTime(duration))
        }
        //        handler.sendEmptyMessage(MESSAGE_SEEK_NEW_POSITION);
    }

    /**
     * 滑动改变亮度
     *
     * @param percent
     */
    private fun onBrightnessSlide(percent: Float) {
        val window = (context as Activity).window
        if (brightness < 0) {
            brightness = window.attributes.screenBrightness
            if (brightness <= 0.00f) {
                brightness = 0.50f
            } else if (brightness < 0.01f) {
                brightness = 0.01f
            }
        }
        //        Log.d(this.getClass().getSimpleName(), "brightness:" + brightness + ",percent:" + percent);
        `$`?.id(R.id.app_video_brightness_box)?.visible()
        val lpa = window.attributes
        lpa.screenBrightness = brightness + percent
        if (lpa.screenBrightness > 1.0f) {
            lpa.screenBrightness = 1.0f
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f
        }
        `$`?.id(R.id.app_video_brightness)?.text((lpa.screenBrightness * 100).toInt().toString() + "%")
        window.attributes = lpa

    }

    override fun onInfo(giraffePlayer: GiraffePlayer, what: Int, extra: Int): Boolean {
        when (what) {
            IMediaPlayer.MEDIA_INFO_BUFFERING_START -> statusChange(STATUS_LOADING)
            IMediaPlayer.MEDIA_INFO_BUFFERING_END -> statusChange(STATUS_PLAYING)
            IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH -> {
            }
            IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> statusChange(STATUS_PLAYING)
        }//显示 下载速度
        //                        Toaster.show("download rate:" + extra);

        return true
    }

    override fun onCurrentStateChange(oldState: Int, newState: Int) {
        if (context is Activity) {
            if (newState == GiraffePlayer.STATE_LAZYLOADING) {
                `$`?.id(R.id.app_video_loading)?.gone()
                `$`?.id(R.id.app_video_status)?.visible()
                        ?.id(R.id.app_video_status_text)
                        ?.text(context.getString(R.string.giraffe_player_lazy_loading, 0))
            }
            if (newState == GiraffePlayer.STATE_PLAYING) {
                //set SCREEN_ON
                context.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                context.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    private fun statusChange(status: Int) {
        this.status = status

        when (status) {
            STATUS_LOADING -> {
                `$`?.id(R.id.app_video_loading)?.visible()
                `$`?.id(R.id.app_video_status)?.gone()
            }
            STATUS_PLAYING -> {
                `$`?.id(R.id.app_video_loading)?.gone()
                `$`?.id(R.id.app_video_status)?.gone()
            }
            STATUS_COMPLETED -> {
                handler.removeMessages(BaseMediaController.MESSAGE_SHOW_PROGRESS)
                showBottomControl(false)
                `$`?.id(R.id.app_video_replay)?.visible()
                `$`?.id(R.id.app_video_loading)?.gone()
                `$`?.id(R.id.app_video_status)?.gone()
            }
            STATUS_ERROR -> {
                `$`?.id(R.id.app_video_status)?.visible()?.id(R.id.app_video_status_text)?.text(R.string.small_problem)
                handler.removeMessages(BaseMediaController.MESSAGE_SHOW_PROGRESS)
                `$`?.id(R.id.app_video_loading)?.gone()
            }
        }
    }

    override fun onError(giraffePlayer: GiraffePlayer, what: Int, extra: Int): Boolean {
        statusChange(STATUS_ERROR)
        return true
    }

    override fun onPrepared(giraffePlayer: GiraffePlayer) {
        val live = giraffePlayer.duration == 0
        `$`?.id(R.id.app_video_seekBar)?.enabled(!live)
    }

    override fun onPreparing(giraffePlayer: GiraffePlayer) {
        statusChange(STATUS_LOADING)
    }

    override fun onDisplayModelChange(oldModel: Int, newModel: Int) {
        this.displayModel = newModel
        if (displayModel == GiraffePlayer.DISPLAY_FLOAT) {
            `$`?.id(R.id.app_video_float_close)?.visible()
            `$`?.id(R.id.app_video_float_full)?.visible()
            `$`?.id(R.id.app_video_bottom_box)?.gone()
        } else {
            `$`?.id(R.id.app_video_float_close)?.gone()
            `$`?.id(R.id.app_video_float_full)?.gone()
            `$`?.id(R.id.app_video_bottom_box)?.visible()
        }

        if (displayModel == GiraffePlayer.DISPLAY_FULL_WINDOW) {
            `$`?.id(R.id.app_video_fullscreen)?.image(R.drawable.ic_fullscreen_off)
            `$`?.id(R.id.app_video_float_full)?.image(R.drawable.ic_fullscreen_off)
        } else {
            `$`?.id(R.id.app_video_fullscreen)?.image(R.drawable.ic_fullscreen_on)
            `$`?.id(R.id.app_video_float_full)?.image(R.drawable.ic_fullscreen_on)
        }
        //        ((ViewGroup) controllerView.getParent()).removeView(controllerView);
        //        if (newModel == GiraffePlayer.DISPLAY_FULL_WINDOW) {
        //            ViewGroup top = (ViewGroup) ((Activity) videoView.getContext()).findViewById(android.R.id.content);
        //            top.addView(controllerView);
        //        } else if (newModel == GiraffePlayer.DISPLAY_NORMAL) {
        //            videoView.addView(controllerView);
        //        }
    }

    override fun onTargetStateChange(oldState: Int, newState: Int) {
        if (newState != GiraffePlayer.STATE_IDLE) {
            `$`?.id(R.id.app_video_cover)?.gone()
        }
    }


    override fun onTimedText(giraffePlayer: GiraffePlayer, text: IjkTimedText?) {
        if (text == null) {
            `$`?.id(R.id.app_video_subtitle)?.gone()
        } else {
            `$`?.id(R.id.app_video_subtitle)?.visible()?.text(text.text)
        }
    }

    override fun onLazyLoadProgress(giraffePlayer: GiraffePlayer, progress: Int) {
        `$`?.id(R.id.app_video_loading)?.gone()
        `$`?.id(R.id.app_video_status)?.visible()
        `$`?.id(R.id.app_video_status_text)
                ?.text(context.getString(R.string.giraffe_player_lazy_loading, progress))
    }

    override fun onLazyLoadError(giraffePlayer: GiraffePlayer, message: String) {
        `$`?.id(R.id.app_video_loading)?.gone()
        `$`?.id(R.id.app_video_status)?.visible()
        `$`?.id(R.id.app_video_status_text)
                ?.text(context.getString(R.string.giraffe_player_lazy_loading_error, message))
    }

    companion object {

        private val STATUS_ERROR = -1
        private val STATUS_IDLE = 0
        private val STATUS_LOADING = 1
        private val STATUS_PLAYING = 2
        private val STATUS_PAUSE = 3
        private val STATUS_COMPLETED = 4
    }
}
