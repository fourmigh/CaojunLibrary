package org.caojun.citypicker.model

import android.support.annotation.IntDef
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

class LocateState {

    companion object {
        const val LOCATING = 123
        const val SUCCESS = 132
        const val FAILURE = 321
    }

    @IntDef(SUCCESS, FAILURE)
    @Retention(RetentionPolicy.SOURCE)
    annotation class State
}