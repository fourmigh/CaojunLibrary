package org.caojun.giraffeplayer

import android.content.Intent
import android.os.Bundle
import android.view.View

/**
 * Created by tcking on 2017
 */

class PlayerActivity : BasePlayerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.giraffe_player_activity)
        val intent = intent
        if (intent == null) {
            finish()
            return
        }
        val videoInfo = intent.getParcelableExtra<VideoInfo>("__video_info__")
        if (videoInfo == null) {
            finish()
            return
        }
        PlayerManager.instance.releaseByFingerprint(videoInfo.fingerprint)
        val videoView = findViewById<VideoView>(R.id.video_view)
        videoView.videoInfo(videoInfo)
        PlayerManager.instance.getPlayer(videoView)?.start()
    }

}
