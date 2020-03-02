package org.caojun.udpsocket

import org.jetbrains.anko.doAsync

class UDPClient(var sendData: BaseData, val listener: Listener, private val interval: Long = 1000) {

    interface Listener {
        fun onReceive(data: BaseData)
        //收到服务端的响应报文，分配了uuidServer
        fun onConnectServer(data: BaseData, ip: String)
    }

    private var udpManager: UDPManager
    private var isRunning = true

    init {
        udpManager = UDPManager(object : UDPManager.Listener {
            override fun onReceive(data: BaseData, ip: String) {
                synchronized(this@UDPClient) {
                    if (sendData.uuidClient == data.uuidClient) {

                        if (sendData.uuidServer == BaseData.UUID_INIT && data.uuidServer != BaseData.UUID_INIT) {
                            listener.onConnectServer(data, ip)
                            return@synchronized
                        }
                        listener.onReceive(data)
                    }
                }
            }
        })

        doAsync {
            while (isRunning) {
                Thread.sleep(interval)
                sendMessage(sendData)
            }

            udpManager.destroy()
        }
    }

    fun destroy() {
        isRunning = false
    }

    fun sendMessage(data: BaseData) {
        synchronized(this) {
            sendData = data
            udpManager.sendMessage(data)
        }
    }
}