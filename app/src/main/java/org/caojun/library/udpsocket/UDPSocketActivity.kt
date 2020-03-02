package org.caojun.library.udpsocket

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.udpsocket_activity_main.*
import org.caojun.library.R
import org.jetbrains.anko.startActivity

class UDPSocketActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.udpsocket_activity_main)

        btnServer.setOnClickListener {
            startActivity<ServerActivity>()
        }

        btnClient.setOnClickListener {
            startActivity<ClientActivity>()
        }
    }
}