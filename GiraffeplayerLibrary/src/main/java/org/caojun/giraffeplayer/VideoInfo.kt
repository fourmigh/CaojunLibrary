package org.caojun.giraffeplayer

import android.graphics.Color
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.ColorInt

import java.util.HashSet

/**
 * Created by tcking on 2017
 */

class VideoInfo : Parcelable {

    var options = HashSet<Option>()
    private var showTopBar = false
    private var uri: Uri? = null
    /**
     * A Fingerprint represent a player
     * @return setFingerprint
     */
    var fingerprint = Integer.toHexString(hashCode())
        private set
    private var portraitWhenFullScreen = true
    private var title: String? = null
    private var aspectRatio = AR_ASPECT_FIT_PARENT
    private var lastFingerprint: String? = null
    private var lastUri: Uri? = null
    private var retryInterval = 0
    private var bgColor = Color.DKGRAY
    private var playerImpl = PLAYER_IMPL_IJK
    private var fullScreenAnimation = true
    var isLooping = false
    /**
     * set current video as cover image when player released
     * @param currentVideoAsCover
     */
    var isCurrentVideoAsCover = true

    constructor(defaultVideoInfo: VideoInfo) {
        title = defaultVideoInfo.title
        portraitWhenFullScreen = defaultVideoInfo.portraitWhenFullScreen
        aspectRatio = defaultVideoInfo.aspectRatio
        for (op in defaultVideoInfo.options) {
            try {
                options.add(op.clone())
            } catch (e: CloneNotSupportedException) {
                e.printStackTrace()
            }

        }
        showTopBar = defaultVideoInfo.showTopBar
        retryInterval = defaultVideoInfo.retryInterval
        bgColor = defaultVideoInfo.bgColor
        playerImpl = defaultVideoInfo.playerImpl
        fullScreenAnimation = defaultVideoInfo.fullScreenAnimation
        isLooping = defaultVideoInfo.isLooping
        isCurrentVideoAsCover = defaultVideoInfo.isCurrentVideoAsCover
    }

    fun isFullScreenAnimation(): Boolean {
        return fullScreenAnimation
    }

    fun setFullScreenAnimation(fullScreenAnimation: Boolean): VideoInfo {
        this.fullScreenAnimation = fullScreenAnimation
        return this
    }

    fun getPlayerImpl(): String {
        return playerImpl
    }

    fun setPlayerImpl(playerImpl: String): VideoInfo {
        this.playerImpl = playerImpl
        return this
    }

    fun getBgColor(): Int {
        return bgColor
    }

    /**
     * player background color default is Color.DKGRAY
     * @param bgColor ColorInt
     * @return
     */
    fun setBgColor(@ColorInt bgColor: Int): VideoInfo {
        this.bgColor = bgColor
        return this
    }


    fun getRetryInterval(): Int {
        return retryInterval
    }

    /**
     * retry to play again interval (in second)
     * @param retryInterval interval in second <=0 will disable retry
     * @return VideoInfo
     */
    fun setRetryInterval(retryInterval: Int): VideoInfo {
        this.retryInterval = retryInterval
        return this
    }

    /**
     * add player init option
     * @param option option
     * @return VideoInfo
     */
    fun addOption(option: Option): VideoInfo {
        this.options.add(option)
        return this
    }

    fun isShowTopBar(): Boolean {
        return showTopBar
    }

    /**
     * show top bar(back arrow and title) when user tap the view
     * @param showTopBar true to show
     * @return VideoInfo
     */
    fun setShowTopBar(showTopBar: Boolean): VideoInfo {
        this.showTopBar = showTopBar
        return this
    }

    fun isPortraitWhenFullScreen(): Boolean {
        return portraitWhenFullScreen
    }

    /**
     * control Portrait when full screen
     * @param portraitWhenFullScreen true portrait when full screen
     * @return VideoInfo
     */
    fun setPortraitWhenFullScreen(portraitWhenFullScreen: Boolean): VideoInfo {
        this.portraitWhenFullScreen = portraitWhenFullScreen
        return this
    }

    fun getTitle(): String? {
        return title
    }

    /**
     * video title
     * @param title title
     * @return VideoInfo
     */
    fun setTitle(title: String): VideoInfo {
        this.title = title
        return this
    }

    fun getAspectRatio(): Int {
        return aspectRatio
    }

    fun setAspectRatio(aspectRatio: Int): VideoInfo {
        this.aspectRatio = aspectRatio
        return this
    }

    constructor() {}

    constructor(uri: Uri) {
        this.uri = uri
    }

    protected constructor(`in`: Parcel) {
        fingerprint = `in`.readString()
        uri = `in`.readParcelable(Uri::class.java.classLoader)
        title = `in`.readString()
        portraitWhenFullScreen = `in`.readByte().toInt() != 0
        aspectRatio = `in`.readInt()
        lastFingerprint = `in`.readString()
        lastUri = `in`.readParcelable(Uri::class.java.classLoader)
        options = `in`.readSerializable() as HashSet<Option>
        showTopBar = `in`.readByte().toInt() != 0
        retryInterval = `in`.readInt()
        bgColor = `in`.readInt()
        playerImpl = `in`.readString()
        fullScreenAnimation = `in`.readByte().toInt() != 0
        isLooping = `in`.readByte().toInt() != 0
        isCurrentVideoAsCover = `in`.readByte().toInt() != 0
    }


    fun setFingerprint(fingerprint: Any): VideoInfo {
        val fp = "" + fingerprint//to string first
        if (lastFingerprint != null && lastFingerprint != fp) {
            //different from last setFingerprint, release last
            PlayerManager.instance.releaseByFingerprint(lastFingerprint!!)
        }
        this.fingerprint = fp
        lastFingerprint = this.fingerprint
        return this
    }

    fun getUri(): Uri? {
        return uri
    }

    /**
     * set video uri
     * @param uri uri
     * @return VideoInfo
     */
    fun setUri(uri: Uri): VideoInfo {
        if (lastUri != null && lastUri != uri) {
            //different from last uri, release last
            PlayerManager.instance.releaseByFingerprint(fingerprint)
        }
        this.uri = uri
        this.lastUri = this.uri
        return this
    }


    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(fingerprint)
        dest.writeParcelable(uri, flags)
        dest.writeString(title)
        dest.writeByte((if (portraitWhenFullScreen) 1 else 0).toByte())
        dest.writeInt(aspectRatio)
        dest.writeString(lastFingerprint)
        dest.writeParcelable(lastUri, flags)
        dest.writeSerializable(options)
        dest.writeByte((if (showTopBar) 1 else 0).toByte())
        dest.writeInt(retryInterval)
        dest.writeInt(bgColor)
        dest.writeString(playerImpl)
        dest.writeByte((if (fullScreenAnimation) 1 else 0).toByte())
        dest.writeByte((if (isLooping) 1 else 0).toByte())
        dest.writeByte((if (isCurrentVideoAsCover) 1 else 0).toByte())
    }

    companion object {
        val AR_ASPECT_FIT_PARENT = 0 // without clip
        val AR_ASPECT_FILL_PARENT = 1 // may clip
        val AR_ASPECT_WRAP_CONTENT = 2
        val AR_MATCH_PARENT = 3
        val AR_16_9_FIT_PARENT = 4
        val AR_4_3_FIT_PARENT = 5
        val PLAYER_IMPL_IJK = "ijk"
        val PLAYER_IMPL_SYSTEM = "system"

        var FloatView_Width = 400
        var FloatView_Height = 300

        var floatView_x = Integer.MAX_VALUE.toFloat() //max_value means unset
        var floatView_y = Integer.MAX_VALUE.toFloat()

        @JvmField
        val CREATOR: Parcelable.Creator<VideoInfo> = object : Parcelable.Creator<VideoInfo> {
            override fun createFromParcel(`in`: Parcel): VideoInfo {
                return VideoInfo(`in`)
            }

            override fun newArray(size: Int): Array<VideoInfo?> {
                return arrayOfNulls(size)
            }
        }

        fun createFromDefault(): VideoInfo {
            return VideoInfo(PlayerManager.instance.defaultVideoInfo)
        }
    }
}
