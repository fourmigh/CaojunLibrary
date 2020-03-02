package org.caojun.udpsocket

import org.jetbrains.anko.doAsync
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

class SocketClientManager(var clientData: BaseData, listener: Listener) {

    interface Listener {
        fun onReceive(data: BaseData)
    }

    private var connectThread: ConnectThread? = null

    init {
        doAsync {

            val udpClient = UDPClient(clientData, object : UDPClient.Listener {
                override fun onReceive(data: BaseData) {
                    clientData = data
                    listener.onReceive(data)
                }

                override fun onConnectServer(data: BaseData, ip: String) {
                    val socket = Socket(ip, Const.SOCKET_PORT)
                    connectThread = ConnectThread(socket, listener)
                    connectThread?.start()
                    clientData = data
                }
            })

            while (clientData.uuidServer == BaseData.UUID_INIT) {

                udpClient.sendMessage(clientData)
                Thread.sleep(1000)
            }

            udpClient.destroy()
        }
    }

    internal inner class ConnectThread(private val socket: Socket, private val listener: Listener) : Thread() {

        private var isRunning = true
        private var dos: DataOutputStream? = null

        override fun run() {
            try {
                val dis = DataInputStream(socket.getInputStream())
                dos = DataOutputStream(socket.getOutputStream())
                while (isRunning) {
                    val data = dis.readUTF()
                    val baseData = JsonUtils.fromJson(data, BaseData::class.java) ?: continue
                    synchronized(this) {
                        listener.onReceive(baseData)
                    }
                }

                dis.close()
                dos?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun doDestroy() {
            isRunning = false
        }

        fun sendMessage(baseData: BaseData) {
            synchronized(this) {
                val msg = JsonUtils.toJSONString(baseData)
                dos?.writeUTF(msg)
                dos?.flush()
            }
        }
    }

    fun sendMessage(data: BaseData): Boolean {
        if (connectThread == null) {
            return false
        }
        connectThread?.sendMessage(data)
        return true
    }
}