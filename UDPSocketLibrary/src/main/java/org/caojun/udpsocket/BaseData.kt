package org.caojun.udpsocket

class BaseData {

    companion object {
        const val UUID_INIT = 0L
    }

    //由服务端分配后，客户端再发起socket连接
    var uuidServer = UUID_INIT
    //由客户端自行分配的uuid
    val uuidClient = System.currentTimeMillis()
    var clientTimer = 0

    fun copy(data: BaseData) {
        this.uuidServer = data.uuidServer
    }
}