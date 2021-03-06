package org.caojun.giraffeplayer

import android.content.res.Configuration
import android.support.v7.app.AppCompatActivity

/**
 * Created by tcking on 2017
 */

abstract class BasePlayerActivity : AppCompatActivity() {
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        PlayerManager.getInstance().onConfigurationChanged(newConfig)
    }

    override fun onBackPressed() {
        if (PlayerManager.getInstance().onBackPressed()) {
            return
        }
        super.onBackPressed()
    }
}
