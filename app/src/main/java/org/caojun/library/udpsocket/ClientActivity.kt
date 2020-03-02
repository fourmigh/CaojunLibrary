package org.caojun.library.udpsocket

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.udpsocket_activity_client.*
import org.caojun.library.R
import org.caojun.udpsocket.BaseData
import org.caojun.udpsocket.JsonUtils
import org.caojun.udpsocket.SocketClientManager
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ClientActivity : AppCompatActivity() {

    private val alServer = ArrayList<String>()
    private var aaServer: ArrayAdapter<String>? = null
    private var clientData = BaseData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.udpsocket_activity_client)

        val socketClientManager = SocketClientManager(clientData, object : SocketClientManager.Listener {
            override fun onReceive(data: BaseData) {
                clientData = data
                runOnUiThread {
                    addData(data, "Receive")
                }
            }
        })

        btnSend.setOnClickListener {
            clientData.clientTimer ++
            socketClientManager.sendMessage(clientData)
        }
    }

    private fun addData(data: BaseData, tag: String) {
        val timeStamp = SimpleDateFormat("HH:mm:ss.SSS").format(Date())
        val msg = "[$tag][$timeStamp]${JsonUtils.toJSONString(data)}"
        alServer.add(msg)
        if (aaServer == null) {
            aaServer = ArrayAdapter(
                    this,
                    android.R.layout.simple_list_item_1, alServer
            )
            lvServer.adapter = aaServer
        } else {
            aaServer?.notifyDataSetChanged()
            if (aaServer != null) {
                lvServer.smoothScrollToPosition(aaServer!!.count - 1)
            }
        }
    }
}