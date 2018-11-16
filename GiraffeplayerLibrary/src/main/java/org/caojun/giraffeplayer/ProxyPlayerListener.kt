package org.caojun.giraffeplayer

import tv.danmaku.ijk.media.player.IjkTimedText


/**
 * Created by tcking on 2017
 */

class ProxyPlayerListener(private val videoInfo: VideoInfo) : PlayerListener {

    var outerListener: PlayerListener? = null

    private fun outerListener(): PlayerListener? {
        if (outerListener != null) {
            return outerListener
        }
        val videoView = PlayerManager.getInstance().getVideoView(videoInfo)
        return if (videoView.playerListener != null) {
            videoView.playerListener
        } else DefaultPlayerListener.INSTANCE
    }

    private fun listener(): PlayerListener {
        val videoView = PlayerManager.getInstance().getVideoView(videoInfo)
        return if (videoView.mediaController != null) {
            videoView.mediaController!!
        } else DefaultPlayerListener.INSTANCE
    }

    override fun onPrepared(giraffePlayer: GiraffePlayer) {
        listener().onPrepared(giraffePlayer)
        outerListener()?.onPrepared(giraffePlayer)
    }

    override fun onBufferingUpdate(giraffePlayer: GiraffePlayer, percent: Int) {
        listener().onBufferingUpdate(giraffePlayer, percent)
        outerListener()?.onBufferingUpdate(giraffePlayer, percent)
    }

    override fun onInfo(giraffePlayer: GiraffePlayer, what: Int, extra: Int): Boolean {
        listener().onInfo(giraffePlayer, what, extra)
        return outerListener()?.onInfo(giraffePlayer, what, extra)?:false
    }

    override fun onCompletion(giraffePlayer: GiraffePlayer) {
        listener().onCompletion(giraffePlayer)
        outerListener()?.onCompletion(giraffePlayer)
    }

    override fun onSeekComplete(giraffePlayer: GiraffePlayer) {
        listener().onSeekComplete(giraffePlayer)
        outerListener()?.onSeekComplete(giraffePlayer)

    }

    override fun onError(giraffePlayer: GiraffePlayer, what: Int, extra: Int): Boolean {
        listener().onError(giraffePlayer, what, extra)
        return outerListener()?.onError(giraffePlayer, what, extra)?:false
    }

    override fun onPause(giraffePlayer: GiraffePlayer) {
        listener().onPause(giraffePlayer)
        outerListener()?.onPause(giraffePlayer)
    }

    override fun onRelease(giraffePlayer: GiraffePlayer) {
        listener().onRelease(giraffePlayer)
        outerListener()?.onRelease(giraffePlayer)

    }

    override fun onStart(giraffePlayer: GiraffePlayer) {
        listener().onStart(giraffePlayer)
        outerListener()?.onStart(giraffePlayer)
    }

    override fun onTargetStateChange(oldState: Int, newState: Int) {
        listener().onTargetStateChange(oldState, newState)
        outerListener()?.onTargetStateChange(oldState, newState)
    }

    override fun onCurrentStateChange(oldState: Int, newState: Int) {
        listener().onCurrentStateChange(oldState, newState)
        outerListener()?.onCurrentStateChange(oldState, newState)
    }

    override fun onDisplayModelChange(oldModel: Int, newModel: Int) {
        listener().onDisplayModelChange(oldModel, newModel)
        outerListener()?.onDisplayModelChange(oldModel, newModel)
    }

    override fun onPreparing(giraffePlayer: GiraffePlayer) {
        listener().onPreparing(giraffePlayer)
        outerListener()?.onPreparing(giraffePlayer)
    }

    override fun onTimedText(giraffePlayer: GiraffePlayer, text: IjkTimedText?) {
        listener().onTimedText(giraffePlayer, text)
        outerListener()?.onTimedText(giraffePlayer, text)
    }

    override fun onLazyLoadProgress(giraffePlayer: GiraffePlayer, progress: Int) {
        listener().onLazyLoadProgress(giraffePlayer, progress)
        outerListener()?.onLazyLoadProgress(giraffePlayer, progress)
    }

    override fun onLazyLoadError(giraffePlayer: GiraffePlayer, message: String) {
        listener().onLazyLoadError(giraffePlayer, message)
        outerListener()?.onLazyLoadError(giraffePlayer, message)
    }
}
