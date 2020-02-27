package org.caojun.arouter

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.annotation.Interceptor
import com.alibaba.android.arouter.facade.callback.InterceptorCallback
import com.alibaba.android.arouter.facade.template.IInterceptor
import org.jetbrains.anko.startActivity

@Interceptor(priority = 7)
class LoginInterceptor : IInterceptor {

    private var context: Context? = null

    companion object {
        var isLogin = false
        var loginActivity: Class<*>? = null
        private lateinit var callback: InterceptorCallback
        private lateinit var postcard: Postcard

        fun loginSuccess(activity: Activity) {
            isLogin = true
            callback.onContinue(postcard)
            activity.setResult(Activity.RESULT_OK)
            activity.finish()
        }

        fun gotoLogin(context: Context) {
            val intent = Intent(context, loginActivity)
            context.startActivity(intent)
        }
    }

    override fun process(postcard: Postcard, callback: InterceptorCallback) {
        LoginInterceptor.postcard = postcard
        LoginInterceptor.callback = callback
        if (context != null && loginActivity != null && postcard.path.contains("/login/") && !isLogin) {
            gotoLogin(context!!)
            return
        }
        callback.onContinue(postcard)
    }

    override fun init(context: Context?) {
        this.context = context
    }
}