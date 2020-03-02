package org.caojun.library.udpsocket

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.udpsocket_activity_server.*
import org.caojun.library.R
import org.caojun.udpsocket.BaseData
import org.caojun.udpsocket.JsonUtils
import org.caojun.udpsocket.SocketServerManager
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ServerActivity : AppCompatActivity() {

    private val alClient = ArrayList<String>()
    private var aaClient: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.udpsocket_activity_server)

        val socketServerManager = SocketServerManager(object : SocketServerManager.Listener {
            override fun onReceive(data: BaseData) {
                runOnUiThread {
                    addData(data, "Receive")
                }
            }

            override fun onOffline(data: BaseData) {
                runOnUiThread {
                    addData(data, "Offline")
                }
            }
        })
    }

    private fun addData(data: BaseData, tag: String) {
        val timeStamp = SimpleDateFormat("HH:mm:ss.SSS").format(Date())
        val msg = "[$tag][$timeStamp]${JsonUtils.toJSONString(data)}"
        alClient.add(msg)
        if (aaClient == null) {
            aaClient = ArrayAdapter(
                    this,
                    android.R.layout.simple_list_item_1, alClient
            )
            lvClient.adapter = aaClient
        } else {
            aaClient?.notifyDataSetChanged()
            if (aaClient != null) {
                lvClient.smoothScrollToPosition(aaClient!!.count - 1)
            }
        }
    }
}