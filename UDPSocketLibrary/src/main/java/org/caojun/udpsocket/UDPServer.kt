package org.caojun.udpsocket

class UDPServer {

    private var udpManager: UDPManager

    init {
        udpManager = UDPManager(object : UDPManager.Listener {
            override fun onReceive(data: BaseData, ip: String) {
                if (data.uuidServer == BaseData.UUID_INIT) {
                    //服务端给客户端分配uuid
                    data.uuidServer = System.currentTimeMillis()
                }
                sendMessage(data)
            }
        }, true)
    }

    fun sendMessage(data: BaseData) {
        udpManager.sendMessage(data)
    }
}