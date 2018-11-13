package org.caojun.giraffeplayer

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.view.ScrollingView
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.AbsListView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ScrollView

/**
 * Created by tcking on 2017
 */

class VideoView : FrameLayout {


    var mediaController: MediaController? = null
        private set
    var playerListener: PlayerListener? = null
    var container: ViewGroup? = null
        private set

    var videoInfo = VideoInfo.createFromDefault()
        private set

    private var activity: Activity? = null

    val player: GiraffePlayer
        get() {
            if (videoInfo.getUri() == null) {
                throw RuntimeException("player uri is null")
            }
            return PlayerManager.instance.getPlayer(this)
        }

    /**
     * is current active player (in list controllerView there are many players)
     * @return boolean
     */
    val isCurrentActivePlayer: Boolean
        get() = PlayerManager.instance.isCurrentPlayer(videoInfo.fingerprint)

    val coverView: ImageView
        get() = findViewById(R.id.app_video_cover)

    fun setPlayerListener(playerListener: PlayerListener): VideoView {
        this.playerListener = playerListener
        return this
    }

    fun videoInfo(videoInfo: VideoInfo): VideoView {
        if (this.videoInfo.getUri() != null && this.videoInfo.getUri() != videoInfo.getUri()) {
            PlayerManager.instance.releaseByFingerprint(this.videoInfo.fingerprint)
        }
        this.videoInfo = videoInfo
        return this
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context)
    }

    private fun init(context: Context) {
        activity = context as Activity
        container = FrameLayout(context)
        addView(container, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        initMediaController()
        setBackgroundColor(videoInfo.getBgColor())
    }


    private fun initMediaController() {
        mediaController = DefaultMediaController(context)
        mediaController!!.bind(this)
    }


    fun setFingerprint(fingerprint: Any): VideoView {
        videoInfo.setFingerprint(fingerprint)
        return this
    }

    fun setVideoPath(uri: String): VideoView {
        videoInfo.setUri(Uri.parse(uri))
        return this
    }

    /**
     * is video controllerView in 'list' controllerView
     * @return
     */
    fun inListView(): Boolean {
        var vp: ViewParent? = parent
        while (vp != null) {
            if (vp is AbsListView || vp is ScrollingView || vp is ScrollView) {
                return true
            }
            vp = vp.parent
        }
        return false
    }
}
