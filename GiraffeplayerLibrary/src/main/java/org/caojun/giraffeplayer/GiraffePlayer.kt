package org.caojun.giraffeplayer

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
//import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.os.Process
import android.transition.ChangeBounds
import android.transition.Transition
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.Surface
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.FrameLayout
import android.widget.MediaController
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.HashMap
import tv.danmaku.ijk.media.player.AndroidMediaPlayer
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import tv.danmaku.ijk.media.player.IjkTimedText
import tv.danmaku.ijk.media.player.misc.ITrackInfo


/**
 * Created by tcking on 2017
 */

class GiraffePlayer private constructor(context: Context, val videoInfo: VideoInfo) : MediaController.MediaPlayerControl {
    private val internalPlaybackThread: HandlerThread
//    private val intentFilter = IntentFilter(ACTION)

    private var currentBufferPercentage = 0
    private val canPause = true
    private var canSeekBackward = true
    private var canSeekForward = true
    private var audioSessionId: Int = 0
    private var seekWhenPrepared: Int = 0

    /**
     * get current player state
     *
     * @return state
     */
    var currentState = STATE_IDLE
        private set
    private var targetState = STATE_IDLE
    private var uri: Uri? = null
    private var headers: Map<String, String>? = null
    private val context: Context = context.applicationContext

    private var mediaPlayer: IMediaPlayer? = null
    @Volatile
    var isReleased: Boolean = false
        private set
    private var handler: Handler? = null
    private val uiHandler = Handler(Looper.getMainLooper())
    private val proxyListener: ProxyPlayerListener

    fun getOuterListener(): ProxyPlayerListener {
        return proxyListener
    }

    @Volatile
    private var startPosition = -1
    private var mute = false
    private var displayBoxRef: WeakReference<out ViewGroup>? = null
    private var ignoreOrientation = -100

    private var displayModel = DISPLAY_NORMAL
    private var lastDisplayModel = displayModel
    private val boxContainerRef: WeakReference<out ViewGroup>?


    private val isInPlaybackState: Boolean
        get() = mediaPlayer != null &&
                currentState != STATE_ERROR &&
                currentState != STATE_IDLE &&
                currentState != STATE_PREPARING

    var playerListener: PlayerListener
        get() = this.proxyListener.outerListener!!
        set(playerListener) {
            this.proxyListener.outerListener = playerListener
        }

    val proxyPlayerListener: PlayerListener
        get() = this.proxyListener

    val currentDisplay: ScalableTextureView?
        get() {
            if (displayBoxRef != null) {
                val box = displayBoxRef!!.get()
                if (box != null) {
                    return box.findViewById<View>(R.id.player_display) as ScalableTextureView
                }
            }
            return null
        }


    private val activity: Activity?
        get() {
            val videoView = PlayerManager.instance.getVideoView(videoInfo)
            return if (videoView != null) {
                videoView.context as Activity
            } else null
        }

    val trackInfo: Array<ITrackInfo?>
        get() = if (mediaPlayer == null || isReleased) {
            arrayOfNulls(0)
        } else mediaPlayer!!.trackInfo

    /**
     * is mute
     *
     * @return true if mute
     */
    val isMute: Boolean
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                return audioManager.isStreamMute(AudioManager.STREAM_MUSIC)
            } else {
                return mute
            }
        }

    /**
     * @return is looping play
     */
    val isLooping: Boolean
        get() = if (mediaPlayer != null && !isReleased) {
            mediaPlayer!!.isLooping
        } else false

    fun getDisplayModel(): Int {
        return displayModel
    }


    private fun proxyListener(): ProxyPlayerListener {
        return proxyListener
    }


    init {
        //        log("new GiraffePlayer");
        val videoView = PlayerManager.instance.getVideoView(videoInfo)
        boxContainerRef = WeakReference<ViewGroup>(videoView?.container)
        boxContainerRef.get()?.setBackgroundColor(videoInfo.getBgColor())
        this.proxyListener = ProxyPlayerListener(videoInfo)
        internalPlaybackThread = HandlerThread("GiraffePlayerInternal:Handler", Process.THREAD_PRIORITY_AUDIO)
        internalPlaybackThread.start()
        handler = Handler(internalPlaybackThread.looper, Handler.Callback { msg ->
            //init mediaPlayer before any actions
            //                log("handleMessage:" + msg.what);
            if (msg.what == MSG_CTRL_RELEASE) {
                if (!isReleased) {
                    handler?.removeCallbacks(null)
                    currentState(STATE_RELEASE)
                    doRelease(msg.obj as String)
                }
                return@Callback true
            }
            if (mediaPlayer == null || isReleased) {
                handler?.removeCallbacks(null)
                try {
                    init(true)
                    handler?.sendMessage(Message.obtain(msg))
                } catch (e: UnsatisfiedLinkError) {
                    //                        log("UnsatisfiedLinkError:" + e);
                    currentState(STATE_LAZYLOADING)
                    //                        LazyLoadManager.Load(context, videoInfo.getFingerprint(), Message.obtain(msg));
                }

                return@Callback true
            }
            when (msg.what) {
                MSG_CTRL_PLAYING -> if (currentState == STATE_ERROR) {
                    handler?.sendEmptyMessage(MSG_CTRL_RETRY)
                } else if (isInPlaybackState) {
                    if (canSeekForward) {
                        if (currentState == STATE_PLAYBACK_COMPLETED) {
                            startPosition = 0
                        }
                        if (startPosition >= 0) {
                            mediaPlayer!!.seekTo(startPosition.toLong())
                            startPosition = -1
                        }
                    }
                    mediaPlayer!!.start()
                    currentState(STATE_PLAYING)
                }
                MSG_CTRL_PAUSE -> {
                    mediaPlayer!!.pause()
                    currentState(STATE_PAUSED)
                }
                MSG_CTRL_SEEK -> {
                    if (canSeekForward) {
                        val position = msg.obj as Int
                        mediaPlayer!!.seekTo(position.toLong())
                    }
                }
                MSG_CTRL_SELECT_TRACK -> {
                    val track = msg.obj as Int
                    if (mediaPlayer is IjkMediaPlayer) {
                        (mediaPlayer as IjkMediaPlayer).selectTrack(track)
                    } else if (mediaPlayer is AndroidMediaPlayer) {
                        (mediaPlayer as AndroidMediaPlayer).internalMediaPlayer.selectTrack(track)
                    }
                }
                MSG_CTRL_DESELECT_TRACK -> {
                    val deselectTrack = msg.obj as Int
                    if (mediaPlayer is IjkMediaPlayer) {
                        (mediaPlayer as IjkMediaPlayer).deselectTrack(deselectTrack)
                    } else if (mediaPlayer is AndroidMediaPlayer) {
                        (mediaPlayer as AndroidMediaPlayer).internalMediaPlayer.deselectTrack(deselectTrack)
                    }
                }
                MSG_SET_DISPLAY -> if (msg.obj == null) {
                    mediaPlayer!!.setDisplay(null)
                } else if (msg.obj is SurfaceTexture) {
                    mediaPlayer!!.setSurface(Surface(msg.obj as SurfaceTexture))
                } else if (msg.obj is SurfaceView) {
                    mediaPlayer!!.setDisplay((msg.obj as SurfaceView).holder)
                }
                MSG_CTRL_RETRY -> {
                    init(false)
                    handler?.sendEmptyMessage(MSG_CTRL_PLAYING)
                }
                MSG_CTRL_SET_VOLUME -> {
                    val pram = msg.obj as Map<String, Float>
                    mediaPlayer!!.setVolume(pram["left"]!!, pram["right"]!!)
                }
            }
            true
        })
        PlayerManager.instance.currentPlayer = this
    }


    override fun start() {
        if (currentState == STATE_PLAYBACK_COMPLETED && !canSeekForward) {
            releaseMediaPlayer()
        }
        targetState(STATE_PLAYING)
        handler?.sendEmptyMessage(MSG_CTRL_PLAYING)
        proxyListener().onStart(this)
    }

    private fun targetState(newState: Int) {
        val oldTargetState = targetState
        targetState = newState
        if (oldTargetState != newState) {
            uiHandler.post { proxyListener().onTargetStateChange(oldTargetState, newState) }
        }
    }

    private fun currentState(newState: Int) {
        val oldCurrentState = currentState
        currentState = newState
        if (oldCurrentState != newState) {
            uiHandler.post { proxyListener().onCurrentStateChange(oldCurrentState, newState) }
        }
    }

    override fun pause() {
        targetState(STATE_PAUSED)
        handler?.sendEmptyMessage(MSG_CTRL_PAUSE)
        proxyListener().onPause(this)
    }

    override fun getDuration(): Int {
        return if (mediaPlayer == null) {
            0
        } else mediaPlayer!!.duration.toInt()
    }

    override fun getCurrentPosition(): Int {
        return if (mediaPlayer == null) {
            0
        } else mediaPlayer!!.currentPosition.toInt()
    }

    override fun seekTo(pos: Int) {
        handler?.obtainMessage(MSG_CTRL_SEEK, pos)?.sendToTarget()
    }

    override fun isPlaying(): Boolean {
        //mediaPlayer.isPlaying()
        return currentState == STATE_PLAYING
    }

    override fun getBufferPercentage(): Int {
        return currentBufferPercentage
    }

    override fun canPause(): Boolean {
        return canPause
    }

    override fun canSeekBackward(): Boolean {
        return canSeekBackward
    }

    override fun canSeekForward(): Boolean {
        return canSeekForward
    }

    override fun getAudioSessionId(): Int {
        if (audioSessionId == 0) {
            audioSessionId = mediaPlayer!!.audioSessionId
        }
        return audioSessionId
    }


    /**
     * Sets video path.
     *
     * @param path the path of the video.
     */
    @Throws(IOException::class)
    private fun setVideoPath(path: String): GiraffePlayer {
        return setVideoURI(Uri.parse(path))
    }

    /**
     * Sets video URI using specific headers.
     *
     * @param uri     the URI of the video.
     * @param headers the headers for the URI request.
     * Note that the cross domain redirection is allowed by default, but that can be
     * changed with key/value pairs through the headers parameter with
     * "android-allow-cross-domain-redirect" as the key and "0" or "1" as the value
     * to disallow or allow cross domain redirection.
     */
    @Throws(IOException::class)
    private fun setVideoURI(uri: Uri, headers: Map<String, String>? = null): GiraffePlayer {
        this.uri = uri
        this.headers = headers
        seekWhenPrepared = 0
        return this
    }

    private fun init(createDisplay: Boolean) {
        //        log("init createDisplay:" + createDisplay);
        uiHandler.post { proxyListener().onPreparing(this@GiraffePlayer) }
        releaseMediaPlayer()
        mediaPlayer = createMediaPlayer()
        if (mediaPlayer is IjkMediaPlayer) {
            IjkMediaPlayer.native_setLogLevel(if (nativeDebug) IjkMediaPlayer.IJK_LOG_DEBUG else IjkMediaPlayer.IJK_LOG_ERROR)
        }
        setOptions()
        isReleased = false
        mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer!!.isLooping = videoInfo.isLooping
        mediaPlayer!!.setOnPreparedListener {
            val live = mediaPlayer!!.duration == 0L
            canSeekBackward = !live
            canSeekForward = !live
            currentState(STATE_PREPARED)
            proxyListener().onPrepared(this@GiraffePlayer)
            if (targetState == STATE_PLAYING) {
                handler?.sendEmptyMessage(MSG_CTRL_PLAYING)
            }
        }
        initInternalListener()
        if (createDisplay) {
            val videoView = PlayerManager.instance.getVideoView(videoInfo)
            if (videoView.container != null) {
                createDisplay(videoView.container!!)
            }
        }
        try {
            uri = videoInfo.getUri()
            mediaPlayer?.setDataSource(context, uri, headers)
            currentState(STATE_PREPARING)
            mediaPlayer?.prepareAsync()
        } catch (e: IOException) {
            currentState(STATE_ERROR)
            e.printStackTrace()
            uiHandler.post { proxyListener().onError(this@GiraffePlayer, 0, 0) }
        }

    }

    private fun createMediaPlayer(): IMediaPlayer {
        return if (VideoInfo.PLAYER_IMPL_SYSTEM == videoInfo.getPlayerImpl()) {
            AndroidMediaPlayer()
        } else IjkMediaPlayer(Looper.getMainLooper())
    }

    private fun setOptions() {
        if (mediaPlayer is IjkMediaPlayer && videoInfo.options.size > 0) {
            val ijkMediaPlayer = mediaPlayer as IjkMediaPlayer?
            for (option in videoInfo.options) {
                if (option.value is String) {
                    ijkMediaPlayer!!.setOption(option.category, option.name, option.value)
                } else if (option.value is Long) {
                    ijkMediaPlayer!!.setOption(option.category, option.name, option.value)
                }
            }
        }
    }

    private fun initInternalListener() {
        //proxyListener fire on main thread
        mediaPlayer?.setOnBufferingUpdateListener { iMediaPlayer, percent ->
            currentBufferPercentage = percent
            proxyListener().onBufferingUpdate(this@GiraffePlayer, percent)
        }
        mediaPlayer?.setOnInfoListener { iMediaPlayer, what, extra ->
            //https://developer.android.com/reference/android/media/MediaPlayer.OnInfoListener.html
            if (what == IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED) {
                val currentDisplay = currentDisplay
                if (currentDisplay != null) {
                    currentDisplay.rotation = extra.toFloat()
                }
            }
            proxyListener().onInfo(this@GiraffePlayer, what, extra)
        }
        mediaPlayer?.setOnCompletionListener {
            currentState(STATE_PLAYBACK_COMPLETED)
            proxyListener().onCompletion(this@GiraffePlayer)
        }
        mediaPlayer?.setOnErrorListener { iMediaPlayer, what, extra ->
            currentState(STATE_ERROR)
            val b = proxyListener().onError(this@GiraffePlayer, what, extra)
            val retryInterval = videoInfo.getRetryInterval()
            if (retryInterval > 0) {
                //                    log("replay delay " + retryInterval + " seconds");
                handler?.sendEmptyMessageDelayed(MSG_CTRL_RETRY, (retryInterval * 1000).toLong())
            }
            b
        }
        mediaPlayer?.setOnSeekCompleteListener { proxyListener().onSeekComplete(this@GiraffePlayer) }
        mediaPlayer?.setOnVideoSizeChangedListener { mp, width, height, sarNum, sarDen ->
            //                if (debug) {
            //                    log("onVideoSizeChanged:width:" + width + ",height:" + height);
            //                }
            val videoWidth = mp.videoWidth
            val videoHeight = mp.videoHeight
            //                int videoSarNum = mp.getVideoSarNum();
            //                int videoSarDen = mp.getVideoSarDen();
            if (videoWidth != 0 && videoHeight != 0) {
                val currentDisplay = currentDisplay
                if (currentDisplay != null) {
                    val scalableDisplay = currentDisplay as ScalableDisplay?
                    scalableDisplay!!.setVideoSize(videoWidth, videoHeight)
                }
            }
        }
        mediaPlayer?.setOnTimedTextListener { mp, text -> proxyListener().onTimedText(this@GiraffePlayer, text) }
    }

    private fun bindDisplay(textureView: TextureView): GiraffePlayer {
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            private var surface: SurfaceTexture? = null

            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                //                log("onSurfaceTextureAvailable");
                if (this.surface == null) {
                    handler?.obtainMessage(MSG_SET_DISPLAY, surface)?.sendToTarget()
                    this.surface = surface
                } else {
                    textureView.surfaceTexture = this.surface
                }
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {

            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                //                log("onSurfaceTextureDestroyed");
                return false//全屏时会发生view的移动，会触发此回调，必须为false（true表示系统负责销毁，此view将不再可用）
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

            }
        }
        return this
    }

    /**
     * create video display controllerView
     *
     * @param container
     */
    fun createDisplay(container: ViewGroup): GiraffePlayer {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            uiHandler.post { doCreateDisplay(container) }
        } else {
            doCreateDisplay(container)
        }
        return this
    }

    private fun doCreateDisplay(container: ViewGroup) {
        //        log("doCreateDisplay");
        isolateDisplayBox()
        val displayBox = FrameLayout(container.context)
        displayBox.id = R.id.player_display_box
        displayBox.setBackgroundColor(videoInfo.getBgColor())
        val lp = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
        )
        val textureView = ScalableTextureView(container.context)
        textureView.setAspectRatio(videoInfo.getAspectRatio())
        textureView.id = R.id.player_display
        displayBox.addView(textureView, FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
        ))
        container.addView(displayBox, 0, lp)
        bindDisplay(textureView)
        displayBoxRef = WeakReference(displayBox)
    }

    /**
     * isolate display box from parent
     *
     * @return
     */
    private fun isolateDisplayBoxContainer(): GiraffePlayer {
        if (boxContainerRef != null) {
            val box = boxContainerRef.get()
            removeFromParent(box)
        }
        return this
    }

    /**
     * isolate display box from parent
     *
     * @return
     */
    private fun isolateDisplayBox(): GiraffePlayer {
        if (displayBoxRef != null) {
            val box = displayBoxRef!!.get()
            removeFromParent(box)
        }
        return this
    }

    private fun doRelease(fingerprint: String) {
        if (isReleased) {
            return
        }
        //        log("doRelease");
        PlayerManager.instance.removePlayer(fingerprint)
        //1. quit handler thread
        internalPlaybackThread.quit()
        //2. remove display group
        releaseDisplayBox()
        releaseMediaPlayer()
        isReleased = true
    }

    private fun releaseMediaPlayer() {
        if (mediaPlayer != null) {
            //            log("releaseMediaPlayer");
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }

    fun release() {
        //        log("try release");
        val fingerprint = videoInfo.fingerprint
        PlayerManager.instance.removePlayer(fingerprint)
        proxyListener().onRelease(this)
        handler?.obtainMessage(MSG_CTRL_RELEASE, fingerprint)?.sendToTarget()
    }

    private fun releaseDisplayBox() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            doReleaseDisplayBox()
        } else {
            uiHandler.post { doReleaseDisplayBox() }
        }
    }

    private fun doReleaseDisplayBox() {
        //        log("doReleaseDisplayBox");
        val currentDisplay = currentDisplay
        if (currentDisplay != null) {
            currentDisplay.surfaceTextureListener = null
        }
        isolateDisplayBox()
    }

    fun togglePlayOrPause() {
        if (isPlaying) {
            pause()
        } else {
            start()
        }
    }

    /**
     * @return
     */
    fun toggleFullScreen(): GiraffePlayer {
        if (displayModel == DISPLAY_NORMAL) {
            setDisplayModel(DISPLAY_FULL_WINDOW)
        } else {
            setDisplayModel(DISPLAY_NORMAL)
        }
        return this
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun setDisplayModel(targetDisplayModel: Int): GiraffePlayer {
        if (targetDisplayModel == displayModel) {
            return this
        }

        //if no display box container,nothing can do
        if (boxContainerRef == null || boxContainerRef.get() == null) {
            return this
        }
        lastDisplayModel = displayModel

        if (targetDisplayModel == DISPLAY_FULL_WINDOW) {
            val activity = activity ?: return this

            //orientation & action bar
            val uiHelper = UIHelper.with(activity)
            if (videoInfo.isPortraitWhenFullScreen()) {
                uiHelper.requestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                ignoreOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            uiHelper.showActionBar(false).fullScreen(true)
            val activityBox = activity.findViewById<ViewGroup>(android.R.id.content)

            animateIntoContainerAndThen(activityBox, object : VideoViewAnimationListener() {

                public override fun onStart(src: ViewGroup, target: ViewGroup) {
                    removeFloatContainer()
                }

                public override fun onEnd(src: ViewGroup, target: ViewGroup) {
                    proxyListener().onDisplayModelChange(displayModel, DISPLAY_FULL_WINDOW)
                    displayModel = DISPLAY_FULL_WINDOW
                }

            })


        } else if (targetDisplayModel == DISPLAY_NORMAL) {
            val activity = activity ?: return this
            val videoView = PlayerManager.instance.getVideoView(videoInfo) ?: return this
//change orientation & action bar
            val uiHelper = UIHelper.with(activity)
            if (videoInfo.isPortraitWhenFullScreen()) {
                uiHelper.requestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                ignoreOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            uiHelper.showActionBar(true).fullScreen(false)


            animateIntoContainerAndThen(videoView, object : VideoViewAnimationListener() {

                public override fun onStart(src: ViewGroup, target: ViewGroup) {
                    removeFloatContainer()
                }

                public override fun onEnd(src: ViewGroup, target: ViewGroup) {
                    proxyListener().onDisplayModelChange(displayModel, DISPLAY_NORMAL)
                    displayModel = DISPLAY_NORMAL
                }

            })
        } else if (targetDisplayModel == DISPLAY_FLOAT) {
            val activity = activity ?: return this

            //change orientation & action bar
            val uiHelper = UIHelper.with(activity)
            if (videoInfo.isPortraitWhenFullScreen()) {
                uiHelper.requestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                ignoreOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            uiHelper.showActionBar(true).fullScreen(false)

            val floatBox = createFloatBox()
            floatBox.visibility = View.INVISIBLE
            animateIntoContainerAndThen(floatBox, object : VideoViewAnimationListener() {
                override fun onEnd(src: ViewGroup, target: ViewGroup) {
                    floatBox.visibility = View.VISIBLE
                    proxyListener().onDisplayModelChange(displayModel, DISPLAY_FLOAT)
                    displayModel = DISPLAY_FLOAT
                }
            })

        }
        return this
    }

    internal fun doMessage(message: Message): GiraffePlayer {
        handler?.sendMessage(message)
        return this
    }


    internal fun lazyLoadProgress(progress: Int) {
        uiHandler.post { proxyListener().onLazyLoadProgress(this@GiraffePlayer, progress) }
    }

    fun lazyLoadError(message: String) {
        uiHandler.post { proxyListener().onLazyLoadError(this@GiraffePlayer, message) }
    }

    internal open inner class VideoViewAnimationListener {
        internal open fun onStart(src: ViewGroup, target: ViewGroup) {}

        internal open fun onEnd(src: ViewGroup, target: ViewGroup) {}
    }

    @SuppressLint("NewApi")
    private fun animateIntoContainerAndThen(container: ViewGroup, listener: VideoViewAnimationListener) {
        val displayBoxContainer = boxContainerRef!!.get()

        val usingAnim = usingAnim()

        if (!usingAnim) {//no animation
            listener.onStart(displayBoxContainer!!, container)
            if (displayBoxContainer.parent != container) {
                isolateDisplayBoxContainer()
                container.addView(displayBoxContainer, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
            }
            listener.onEnd(displayBoxContainer, container)
            return
        }

        val activity = activity ?: return

        //这里用post确保在调用此函数之前的ui操作都已经ok
        uiHandler.post {
            val activityBox = activity.findViewById<View>(android.R.id.content) as ViewGroup


            val targetXY = intArrayOf(0, 0)
            val activityBoxXY = intArrayOf(0, 0)

            //set src LayoutParams
            activityBox.getLocationInWindow(activityBoxXY)


            if (displayBoxContainer?.parent != activityBox) {
                val srcXY = intArrayOf(0, 0)
                val srcLayoutParams = FrameLayout.LayoutParams(displayBoxContainer!!.width, displayBoxContainer.height)
                displayBoxContainer?.getLocationInWindow(srcXY)
                srcLayoutParams.leftMargin = srcXY[0] - activityBoxXY[0]
                srcLayoutParams.topMargin = srcXY[1] - activityBoxXY[1]
                isolateDisplayBoxContainer()
                activityBox.addView(displayBoxContainer, srcLayoutParams)
            }

            //2.set target LayoutParams
            val targetLayoutParams = FrameLayout.LayoutParams(container.layoutParams)
            container.getLocationInWindow(targetXY)
            targetLayoutParams.leftMargin = targetXY[0] - activityBoxXY[0]
            targetLayoutParams.topMargin = targetXY[1] - activityBoxXY[1]


            val transition = ChangeBounds()
            transition.startDelay = 200
            transition.addListener(object : Transition.TransitionListener {

                private fun afterTransition() {
                    //fire listener
                    if (displayBoxContainer?.parent != container) {
                        isolateDisplayBoxContainer()
                        container.addView(displayBoxContainer, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
                    }
                    listener.onEnd(displayBoxContainer!!, container)
                }

                override fun onTransitionStart(transition: Transition) {

                }


                override fun onTransitionEnd(transition: Transition) {
                    afterTransition()
                }

                override fun onTransitionCancel(transition: Transition) {
                    afterTransition()
                }

                override fun onTransitionPause(transition: Transition) {

                }

                override fun onTransitionResume(transition: Transition) {

                }
            })

            //                    must put the action to queue so the beginDelayedTransition can take effect
            uiHandler.post {
                listener.onStart(displayBoxContainer!!, container)
                TransitionManager.beginDelayedTransition(displayBoxContainer, transition)
                displayBoxContainer.layoutParams = targetLayoutParams
            }
        }


    }


    private fun createFloatBox(): ViewGroup {
        removeFloatContainer()
        val topActivity = PlayerManager.instance.topActivity
        val topActivityBox = topActivity.findViewById<View>(android.R.id.content) as ViewGroup
        val floatBox = LayoutInflater.from(topActivity.application).inflate(R.layout.giraffe_float_box, null) as ViewGroup
        floatBox.setBackgroundColor(videoInfo.getBgColor())

        val floatBoxParams = FrameLayout.LayoutParams(VideoInfo.floatView_width, VideoInfo.floatView_height)
        if (VideoInfo.floatView_x == Integer.MAX_VALUE.toFloat() || VideoInfo.floatView_y == Integer.MAX_VALUE.toFloat()) {
            floatBoxParams.gravity = Gravity.BOTTOM or Gravity.END
            floatBoxParams.bottomMargin = 20
            floatBoxParams.rightMargin = 20
        } else {
            floatBoxParams.gravity = Gravity.TOP or Gravity.START
            floatBoxParams.leftMargin = VideoInfo.floatView_x.toInt()
            floatBoxParams.topMargin = VideoInfo.floatView_y.toInt()
        }
        topActivityBox.addView(floatBox, floatBoxParams)

        floatBox.setOnTouchListener(object : View.OnTouchListener {
            internal var ry: Float = 0.toFloat()
            internal var oy: Float = 0.toFloat()

            internal var rx: Float = 0.toFloat()
            internal var ox: Float = 0.toFloat()

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                // 获取相对屏幕的坐标，即以屏幕左上角为原点
                //                System.out.println("MotionEvent:action:"+event.getAction()+",raw:["+event.getRawX()+","+event.getRawY()+"],xy["+event.getX()+","+event.getY()+"]");

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        ry = event.rawY
                        oy = v.translationY

                        rx = event.rawX
                        ox = v.translationX
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val y = oy + event.rawY - ry
                        if (y > 0) {
                            //                            y = 0;
                        }
                        v.translationY = y

                        val x = ox + event.rawX - rx
                        if (x > 0) {
                            //                            x = 0;
                        }
                        v.translationX = x
                    }
                }
                return true
            }
        })
        return floatBox
    }

    private fun removeFloatContainer() {
        val activity = activity
        if (activity != null) {
            val floatBox = activity.findViewById<View>(R.id.player_display_float_box)
            if (floatBox != null) {
                VideoInfo.floatView_x = floatBox.x
                VideoInfo.floatView_y = floatBox.y
            }
            removeFromParent(floatBox)
        }
    }

    private fun removeFromParent(view: View?) {
        if (view != null) {
            val parent = view.parent
            if (parent != null) {
                (parent as ViewGroup).removeView(view)
            }
        }
    }


    private fun usingAnim(): Boolean {
        return videoInfo.isFullScreenAnimation() && !videoInfo.isPortraitWhenFullScreen() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
    }

    fun onConfigurationChanged(newConfig: Configuration): GiraffePlayer {
        //        log("onConfigurationChanged");
        if (ignoreOrientation == newConfig.orientation) {
            //            log("onConfigurationChanged ignore");
            return this
        }
        if (videoInfo.isPortraitWhenFullScreen()) {
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                setDisplayModel(lastDisplayModel)
            } else {
                setDisplayModel(DISPLAY_FULL_WINDOW)
            }
        }
        return this
    }

    fun onBackPressed(): Boolean {
        //        log("onBackPressed");
        if (displayModel == DISPLAY_FULL_WINDOW) {
            setDisplayModel(lastDisplayModel)
            return true
        }
        return false
    }

    fun onActivityResumed() {
        //        log("onActivityResumed");
        if (targetState == STATE_PLAYING) {
            start()
        } else if (targetState == STATE_PAUSED) {
            if (canSeekForward && startPosition >= 0) {
                seekTo(startPosition)
            }
        }

        //        if (targetState == STATE_PLAYING) {
        //            start();
        //        }
    }

    fun onActivityPaused() {
        //        log("onActivityPaused");
        if (mediaPlayer == null) {
            return
        }
        if (targetState == STATE_PLAYING
                || currentState == STATE_PLAYING
                || targetState == STATE_PAUSED
                || currentState == STATE_PAUSED) {

            startPosition = mediaPlayer!!.currentPosition.toInt()
            releaseMediaPlayer()
        }
    }

    fun onActivityDestroyed() {
        //        log("onActivityDestroyed");
        release()
    }

    fun stop() {
        release()
    }

    fun aspectRatio(aspectRatio: Int) {
        //        log("aspectRatio:" + aspectRatio);
        videoInfo.setAspectRatio(aspectRatio)
        val display = currentDisplay
        display?.setAspectRatio(aspectRatio)
    }

    fun getSelectedTrack(trackType: Int): Int {
        if (mediaPlayer == null || isReleased) {
            return -1
        }
        if (mediaPlayer is IjkMediaPlayer) {
            return (mediaPlayer as IjkMediaPlayer).getSelectedTrack(trackType)
        } else if (mediaPlayer is AndroidMediaPlayer) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return (mediaPlayer as AndroidMediaPlayer).internalMediaPlayer.getSelectedTrack(trackType)
            }
        }
        return -1
    }

    fun selectTrack(track: Int): GiraffePlayer {
        if (mediaPlayer == null || isReleased) {
            return this
        }
        handler?.removeMessages(MSG_CTRL_SELECT_TRACK)
        handler?.obtainMessage(MSG_CTRL_SELECT_TRACK, track)?.sendToTarget()
        return this
    }

    fun deselectTrack(selectedTrack: Int): GiraffePlayer {
        if (mediaPlayer == null || isReleased) {
            return this
        }
        handler?.removeMessages(MSG_CTRL_DESELECT_TRACK)
        handler?.obtainMessage(MSG_CTRL_DESELECT_TRACK, selectedTrack)?.sendToTarget()
        return this
    }


    /**
     * set volume
     *
     * @param left  [0,1]
     * @param right [0,1]
     * @return GiraffePlayer
     */
    fun setVolume(left: Float, right: Float): GiraffePlayer {
        if (mediaPlayer == null || isReleased) {
            return this
        }
        val pram = HashMap<String, Float>()
        pram["left"] = left
        pram["right"] = right
        handler?.removeMessages(MSG_CTRL_SET_VOLUME)
        handler?.obtainMessage(MSG_CTRL_SET_VOLUME, pram)?.sendToTarget()
        return this
    }

    /**
     * set mute
     *
     * @param mute
     * @return GiraffePlayer
     */
    fun setMute(mute: Boolean): GiraffePlayer {
        this.mute = mute
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true)
        return this
    }

    /**
     * set looping play
     *
     * @param looping
     * @return
     */
    fun setLooping(looping: Boolean): GiraffePlayer {
        if (mediaPlayer != null && !isReleased) {
            mediaPlayer!!.isLooping = looping
        }
        return this
    }

    companion object {
        val TAG = "GiraffePlayer"
        val ACTION = "tcking.github.com.giraffeplayer2.action"
        var debug = false
        var nativeDebug = false
        // Internal messages
        private val MSG_CTRL_PLAYING = 1

        private val MSG_CTRL_PAUSE = 2
        private val MSG_CTRL_SEEK = 3
        private val MSG_CTRL_RELEASE = 4
        private val MSG_CTRL_RETRY = 5
        private val MSG_CTRL_SELECT_TRACK = 6
        private val MSG_CTRL_DESELECT_TRACK = 7
        private val MSG_CTRL_SET_VOLUME = 8


        private val MSG_SET_DISPLAY = 12


        // all possible internal states
        val STATE_ERROR = -1
        val STATE_IDLE = 0
        val STATE_PREPARING = 1
        val STATE_PREPARED = 2
        val STATE_PLAYING = 3
        val STATE_PAUSED = 4
        val STATE_PLAYBACK_COMPLETED = 5
        val STATE_RELEASE = 6
        val STATE_LAZYLOADING = 7

        val DISPLAY_NORMAL = 0
        val DISPLAY_FULL_WINDOW = 1
        val DISPLAY_FLOAT = 2

        fun createPlayer(context: Context, videoInfo: VideoInfo): GiraffePlayer {
            return GiraffePlayer(context, videoInfo)
        }

        fun play(context: Context, videoInfo: VideoInfo) {
            val intent = Intent(context, PlayerActivity::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            intent.putExtra("__video_info__", videoInfo)
            PlayerManager.instance.releaseCurrent()
            context.startActivity(intent)
        }
    }

}
/**
 * Sets video URI.
 *
 * @param uri the URI of the video.
 */
