package org.caojun.giraffeplayer

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import java.lang.ref.WeakReference
import java.util.WeakHashMap
import java.util.concurrent.ConcurrentHashMap


/**
 * Created by tcking on 2017
 */

class PlayerManager {
    @Volatile
    private var currentPlayerFingerprint: String? = null
    private var activityLifecycleCallbacks: Application.ActivityLifecycleCallbacks? = null
    private var topActivityRef: WeakReference<Activity>? = null

    /**
     * default config for all
     */
    val defaultVideoInfo = VideoInfo()

    private val videoViewsRef = WeakHashMap<String, VideoView>()
    private val playersRef = ConcurrentHashMap<String, GiraffePlayer?>()
    private val activity2playersRef = WeakHashMap<Context, String>()

    var currentPlayer: GiraffePlayer?
        get() = if (currentPlayerFingerprint == null) null else playersRef[currentPlayerFingerprint!!]
        set(giraffePlayer) {
            val videoInfo = giraffePlayer?.videoInfo
            val fingerprint = videoInfo?.fingerprint
            if (!isCurrentPlayer(fingerprint)) {
                try {
                    releaseCurrent()
                    currentPlayerFingerprint = fingerprint
                } catch (e: Exception) {
                    e.printStackTrace()
                    throw RuntimeException(e)
                }

            }
        }

    val topActivity: Activity
        get() = topActivityRef!!.get()!!

    private fun createPlayer(videoView: VideoView): GiraffePlayer {
        val videoInfo = videoView.videoInfo
        //        log(videoInfo.getFingerprint(), "createPlayer");
        videoViewsRef[videoInfo.fingerprint] = videoView
        registerActivityLifecycleCallbacks((videoView.context as Activity).application)
        val player = GiraffePlayer.createPlayer(videoView.context, videoInfo)
        playersRef[videoInfo.fingerprint] = player
        activity2playersRef[videoView.context] = videoInfo.fingerprint
        if (topActivityRef == null || topActivityRef!!.get() == null) {
            topActivityRef = WeakReference(videoView.context as Activity)
        }
        return player
    }

    @Synchronized
    private fun registerActivityLifecycleCallbacks(context: Application) {
        if (activityLifecycleCallbacks != null) {
            return
        }
        activityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

            override fun onActivityStarted(activity: Activity) {

            }

            override fun onActivityResumed(activity: Activity) {
                val currentPlayer = getPlayerByFingerprint(activity2playersRef[activity])
                currentPlayer?.onActivityResumed()
                topActivityRef = WeakReference(activity)
            }

            override fun onActivityPaused(activity: Activity) {
                val currentPlayer = getPlayerByFingerprint(activity2playersRef[activity])
                currentPlayer?.onActivityPaused()
                if (topActivityRef != null && topActivityRef!!.get() == activity) {
                    topActivityRef?.clear()
                }
            }

            override fun onActivityStopped(activity: Activity) {

            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

            }

            override fun onActivityDestroyed(activity: Activity) {
                val currentPlayer = currentPlayer
                currentPlayer?.onActivityDestroyed()
                activity2playersRef.remove(activity)
            }
        }
        context.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
    }

    fun releaseCurrent() {
        //        log(currentPlayerFingerprint, "releaseCurrent");
        val currentPlayer = currentPlayer
        if (currentPlayer != null) {
//            if (currentPlayer.proxyPlayerListener != null) {
                currentPlayer.proxyPlayerListener.onCompletion(currentPlayer)
//            }
            currentPlayer.release()
        }
        currentPlayerFingerprint = null
    }


    fun isCurrentPlayer(fingerprint: String?): Boolean {
        return fingerprint != null && fingerprint == this.currentPlayerFingerprint
    }

    fun onConfigurationChanged(newConfig: Configuration) {
        val currentPlayer = currentPlayer
        currentPlayer?.onConfigurationChanged(newConfig)
    }

    fun onBackPressed(): Boolean {
        val currentPlayer = currentPlayer
        return currentPlayer?.onBackPressed() ?: false
    }

    fun getVideoView(videoInfo: VideoInfo): VideoView {
        return videoViewsRef[videoInfo.fingerprint]!!
    }

    fun getPlayer(videoView: VideoView): GiraffePlayer {
        val videoInfo = videoView.videoInfo
        var player: GiraffePlayer? = playersRef[videoInfo.fingerprint]
        if (player == null) {
            player = createPlayer(videoView)
        }
        return player
    }

    fun getPlayerByFingerprint(fingerprint: String?): GiraffePlayer? {
        return if (fingerprint == null) {
            null
        } else playersRef[fingerprint]
    }

    fun releaseByFingerprint(fingerprint: String): PlayerManager {
        val player = playersRef[fingerprint]
        player?.release()
        return this
    }

    fun removePlayer(fingerprint: String) {
        playersRef.remove(fingerprint)
    }

    companion object {

//        @JvmField
        private val instance = PlayerManager()

        fun getInstance(): PlayerManager {
            return instance
        }
    }
}
