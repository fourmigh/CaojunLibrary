package org.caojun.giraffeplayer

import tv.danmaku.ijk.media.player.IjkTimedText

/**
 * Created by tcking on 2017
 */

interface PlayerListener {

    fun onPrepared(giraffePlayer: GiraffePlayer)

    /**
     * Called to update status in buffering a media stream received through progressive HTTP download.
     * @param giraffePlayer
     * @param percent nt: the percentage (0-100) of the content that has been buffered or played thus far
     */
    fun onBufferingUpdate(giraffePlayer: GiraffePlayer, percent: Int)

    fun onInfo(giraffePlayer: GiraffePlayer, what: Int, extra: Int): Boolean

    fun onCompletion(giraffePlayer: GiraffePlayer)

    fun onSeekComplete(giraffePlayer: GiraffePlayer)

    fun onError(giraffePlayer: GiraffePlayer, what: Int, extra: Int): Boolean

    fun onPause(giraffePlayer: GiraffePlayer)

    fun onRelease(giraffePlayer: GiraffePlayer)

    fun onStart(giraffePlayer: GiraffePlayer)

    fun onTargetStateChange(oldState: Int, newState: Int)

    fun onCurrentStateChange(oldState: Int, newState: Int)

    fun onDisplayModelChange(oldModel: Int, newModel: Int)

    fun onPreparing(giraffePlayer: GiraffePlayer)

    /**
     * render subtitle
     * @param giraffePlayer
     * @param text
     */
    fun onTimedText(giraffePlayer: GiraffePlayer, text: IjkTimedText?)

    fun onLazyLoadProgress(giraffePlayer: GiraffePlayer, progress: Int)

    fun onLazyLoadError(giraffePlayer: GiraffePlayer, message: String)
}
