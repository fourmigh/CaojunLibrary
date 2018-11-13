package org.caojun.giraffeplayer


/**
 * Created by tcking on 2017
 */

interface MediaController : PlayerListener {
    /**
     * bind this media controller to video controllerView
     */
    fun bind(videoView: VideoView)
}
