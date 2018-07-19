package org.caojun.citypicker

import android.Manifest
import android.app.Activity
import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.annotation.StyleRes
import org.caojun.citypicker.adapter.OnPickListener
import org.caojun.citypicker.model.HotCity
import org.caojun.citypicker.model.LocateState
import org.caojun.citypicker.model.LocatedCity
import org.jetbrains.anko.doAsync
import android.location.LocationManager
import android.widget.TextView
import java.nio.file.Files.size
import android.R.attr.data
import android.location.Address
import android.location.Geocoder
import android.util.Log
import java.io.IOException


class CityPicker {

    companion object {
        private const val TAG = "CityPicker"
        private var mInstance: CityPicker? = null

        fun getInstance(): CityPicker {
            if (mInstance == null) {
                synchronized(CityPicker::class.java) {
                    if (mInstance == null) {
                        mInstance = CityPicker()
                    }
                }
            }
            return mInstance!!
        }
    }

    private var mFragmentManager: FragmentManager? = null
    private var mTargetFragment: Fragment? = null

    private var enableAnim: Boolean = false
    private var mAnimStyle: Int = 0
    private var mLocation: LocatedCity? = null
    private var mHotCities: MutableList<HotCity>? = null
    private var mOnPickListener: OnPickListener? = null

    fun setFragmentManager(fm: FragmentManager): CityPicker {
        this.mFragmentManager = fm
        return this
    }

    fun setTargetFragment(targetFragment: Fragment): CityPicker {
        this.mTargetFragment = targetFragment
        return this
    }

    /**
     * 设置动画效果
     * @param animStyle
     * @return
     */
    fun setAnimationStyle(@StyleRes animStyle: Int): CityPicker {
        this.mAnimStyle = animStyle
        return this
    }

    /**
     * 设置当前已经定位的城市
     * @param location
     * @return
     */
    fun setLocatedCity(location: LocatedCity): CityPicker {
        this.mLocation = location
        return this
    }

    fun setHotCities(data: MutableList<HotCity>): CityPicker {
        this.mHotCities = data
        return this
    }

    /**
     * 启用动画效果，默认为false
     * @param enable
     * @return
     */
    fun enableAnimation(enable: Boolean): CityPicker {
        this.enableAnim = enable
        return this
    }

    /**
     * 设置选择结果的监听器
     * @param listener
     * @return
     */
    fun setOnPickListener(listener: OnPickListener): CityPicker {
        this.mOnPickListener = listener
        return this
    }

    fun show() {
        if (mFragmentManager == null) {
            throw UnsupportedOperationException("CityPicker：method setFragmentManager() must be called.")
        }
        var ft = mFragmentManager!!.beginTransaction()
        val prev = mFragmentManager!!.findFragmentByTag(TAG)
        if (prev != null) {
            ft.remove(prev).commit()
            ft = mFragmentManager!!.beginTransaction()
        }
        ft.addToBackStack(null)
        val cityPickerFragment = CityPickerDialogFragment.newInstance(enableAnim)
        if (mLocation != null) {
            cityPickerFragment.setLocatedCity(mLocation!!)
        }
        if (mHotCities != null) {
            cityPickerFragment.setHotCities(mHotCities!!)
        }
        cityPickerFragment.setAnimationStyle(mAnimStyle)
        if (mOnPickListener != null) {
            cityPickerFragment.setOnPickListener(mOnPickListener!!)
        }
        if (mTargetFragment != null) {
            cityPickerFragment.setTargetFragment(mTargetFragment!!, 0)
        }
        cityPickerFragment.show(ft, TAG)
    }

    /**
     * 定位完成
     * @param location
     * @param state
     */
    fun locateComplete(location: LocatedCity?, @LocateState.State state: Int) {
        val fragment = mFragmentManager!!.findFragmentByTag(TAG) as CityPickerDialogFragment
        fragment.locationChanged(location, state)
    }

    /**
     * 定位
     */
    fun doLocate(activity: Activity) {
        doAsync {
            val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            PermissionUtils.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION, object : PermissionUtils.RequestPermissionListener {
                override fun onSuccess() {
                    try {
                        val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        if (location != null) {
                            val latitude = location.latitude // 经度
                            val longitude = location.longitude // 纬度

                            val ge = Geocoder(activity)
                            try {
                                val addList = ge.getFromLocation(latitude, longitude, 1)
                                val ad = addList[0]
                                locateComplete(LocatedCity(ad.locality, ad.adminArea), LocateState.SUCCESS)
                            } catch (e:Exception) {
                                e.printStackTrace()
                            }
                        }
                    } catch (e: SecurityException) {

                    } catch (e: Exception) {

                    }
                }

                override fun onFail() {
                    locateComplete(null, LocateState.FAILURE)
                }
            })

        }
    }
}