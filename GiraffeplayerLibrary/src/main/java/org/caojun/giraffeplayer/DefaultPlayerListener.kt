package org.caojun.giraffeplayer

import tv.danmaku.ijk.media.player.IjkTimedText

/**
 * Created by tcking on 2017
 */

open class DefaultPlayerListener : PlayerListener {

    override fun onPrepared(giraffePlayer: GiraffePlayer) {

    }

    override fun onBufferingUpdate(giraffePlayer: GiraffePlayer, percent: Int) {

    }

    override fun onInfo(giraffePlayer: GiraffePlayer, what: Int, extra: Int): Boolean {
        return true
    }

    override fun onCompletion(giraffePlayer: GiraffePlayer) {

    }

    override fun onSeekComplete(giraffePlayer: GiraffePlayer) {

    }

    override fun onError(giraffePlayer: GiraffePlayer, what: Int, extra: Int): Boolean {
        return true
    }

    override fun onPause(giraffePlayer: GiraffePlayer) {

    }

    override fun onRelease(giraffePlayer: GiraffePlayer) {

    }

    override fun onStart(giraffePlayer: GiraffePlayer) {

    }

    override fun onTargetStateChange(oldState: Int, newState: Int) {

    }

    override fun onCurrentStateChange(oldState: Int, newState: Int) {

    }

    override fun onDisplayModelChange(oldModel: Int, newModel: Int) {

    }

    override fun onPreparing(giraffePlayer: GiraffePlayer) {

    }

    override fun onTimedText(giraffePlayer: GiraffePlayer, text: IjkTimedText?) {

    }

    override fun onLazyLoadError(giraffePlayer: GiraffePlayer, message: String) {

    }

    override fun onLazyLoadProgress(giraffePlayer: GiraffePlayer, progress: Int) {

    }

    companion object {

        val INSTANCE = DefaultPlayerListener()
    }
}
