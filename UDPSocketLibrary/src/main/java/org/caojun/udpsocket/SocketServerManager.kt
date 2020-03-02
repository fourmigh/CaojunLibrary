package org.caojun.udpsocket

import org.jetbrains.anko.doAsync
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.util.*

class SocketServerManager(private val listener: Listener) {

    interface Listener {
        fun onReceive(data: BaseData)
        fun onOffline(data: BaseData)
    }

    private var serverSocket = ServerSocket(Const.SOCKET_PORT)
    private var isRunning = true
    private val socketClients = Hashtable<Long, ConnectThread>()

    init {

        doAsync {

            try {
                var socket: Socket? = null
                //等待连接，每建立一个连接，就新建一个线程
                while (isRunning) {
                    socket = serverSocket.accept()//等待一个客户端的连接，在连接之前，此方法是阻塞的
                    val ct = ConnectThread(socket, listener)
                    ct.start()
                    Thread.sleep(50)
                }

                socket?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun sendMessage(baseData: BaseData): Boolean {
        val socketClient = socketClients[baseData.uuidClient] ?: return false
        socketClient.sendMessage(baseData)
        return true
    }

    internal inner class ConnectThread(private val socket: Socket, private val listener: Listener) : Thread() {

        var baseData = BaseData()
        private var isRunning = true
        private var dos: DataOutputStream? = null

        override fun run() {
            try {
                val dis = DataInputStream(socket.getInputStream())
                dos = DataOutputStream(socket.getOutputStream())
                while (isRunning) {
                    val data = dis.readUTF()
                    baseData = JsonUtils.fromJson(data, BaseData::class.java) ?: continue
                    if (!socketClients.containsKey(baseData.uuidClient)) {
                        socketClients[baseData.uuidClient] = this
                    }
                    synchronized(this@SocketServerManager) {
                        listener.onReceive(baseData)
                    }
                }

                dis.close()
                dos?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            socketClients.remove(baseData.uuidClient)
            synchronized(this@SocketServerManager) {
                listener.onOffline(baseData)
            }
        }

        fun doDestroy() {
            isRunning = false
        }

        fun sendMessage(baseData: BaseData) {
            synchronized(this@SocketServerManager) {
                val msg = JsonUtils.toJSONString(baseData)
                dos?.writeUTF(msg)
                dos?.flush()
            }
        }
    }
}