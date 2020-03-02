package org.caojun.udpsocket

import org.jetbrains.anko.doAsync
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.charset.Charset

class UDPManager(listener: Listener, private val isServer: Boolean = false, timeout: Int = 5000) {

    interface Listener {
        fun onReceive(data: BaseData, ip: String)
    }

    private var receiveSocket: DatagramSocket
    private var sendSocket: DatagramSocket

    private var isRunning = true

    init {

        if (isServer) {
            sendSocket = DatagramSocket(Const.UDP_CLIENT_PORT)
            receiveSocket = DatagramSocket(Const.UDP_SERVER_PORT)
        } else {
            sendSocket = DatagramSocket(Const.UDP_SERVER_PORT)
            receiveSocket = DatagramSocket(Const.UDP_CLIENT_PORT)
        }

        receiveSocket.soTimeout = timeout
        sendSocket.broadcast = true

        doAsync {
            while (isRunning) {
                Thread.sleep(100)

                try {
                    val byte = ByteArray(1024)
                    val datagramPacket = DatagramPacket(byte, byte.size)
                    receiveSocket.receive(datagramPacket)
                    val json = String(
                            datagramPacket.data,
                            0,
                            datagramPacket.length,
                            Charset.forName("utf-8")
                    )
                    val data = JsonUtils.fromJson(json, BaseData::class.java) ?: continue
                    listener.onReceive(data, datagramPacket.address.hostAddress)
                } catch (e: Exception) {
                    continue
                }
            }
            sendSocket.close()
            receiveSocket.close()
        }
    }

    fun destroy() {
        isRunning = false
    }

    fun sendMessage(data: BaseData) {
        doAsync {
            doSendMessage(data)
        }
    }

    private fun doSendMessage(data: BaseData) {
        try {
            val message = JsonUtils.toJSONString(data)
            val byte = message.toByteArray()
            val datagramPacket = DatagramPacket(byte, byte.size)
            datagramPacket.address = InetAddress.getByName("255.255.255.255")
            datagramPacket.port = if (isServer) Const.UDP_CLIENT_PORT else Const.UDP_SERVER_PORT
            sendSocket.send(datagramPacket)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}