package com.sourcelab.remoteHelper

import android.service.quicksettings.TileService
import android.widget.Toast
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.io.IOException
import android.content.Intent
import android.service.quicksettings.Tile
import android.os.Handler

class TileWakeup: TileService(){

    /**
     * 构造magic魔术包
     * @param mac    mac
     * @return 包文
     */
    private fun generateMagicPackage(mac: String) : String {
        var magicString = ""
        val COMMON_HEAD = "FFFFFFFFFFFF"
        for (i in 0..15) {
            magicString += mac
        }
        return COMMON_HEAD + magicString
    }

    /**
     * 网络唤醒
     * @param host        主机地址
     * @param mac        mac地址
     * @param port       端口
     */
    private fun wakeUp(host: String, mac: String, port: Int) : Boolean {
        //构建magic魔术包
        val magicPacage = generateMagicPackage(mac)
        val MPBinary = HexUtils.hexStringToBytes(magicPacage)
        try {
            val socket = DatagramSocket()
            val address = InetAddress.getByName(host)
            val packet = DatagramPacket(MPBinary, MPBinary.size, address, port)
            //发送唤醒包
            socket.send(packet)
            socket.close()
        } catch (e: IOException) {
//            e.printStackTrace()
            return false
        }
        return true
    }

    override fun onClick() {
        super.onClick()
        // Called when the user click the tile
        if (qsTile.state == Tile.STATE_INACTIVE) {
            qsTile.state = Tile.STATE_ACTIVE
            qsTile.updateTile()

            val mac = AppPreferences.getVal("wol_mac")
            val host = AppPreferences.getVal("wol_host")
            val port = AppPreferences.getVal("wol_port")

            val uiHandler = Handler()

            // 收起通知栏
            sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
            if (mac == null || host == null || port == null || mac.isEmpty() || host.isEmpty() || port.isEmpty()) {
                Toast.makeText(this@TileWakeup, "WOL相关参数不完整，请先长按快捷设置按钮进入APP设置", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val shortMac = mac.replace("-", "").replace(":", "")
                AsyncUtils.doAsync {
                    val result = wakeUp(host, shortMac, port.toInt())
                    uiHandler.post {
                        Toast.makeText(this@TileWakeup, if (result) "唤醒主机：${host} port:${port}" else "大概是网络错误", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }

            uiHandler.postDelayed({
                qsTile.state = Tile.STATE_INACTIVE
                qsTile.updateTile()
            }, 3000)
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        qsTile.state = Tile.STATE_INACTIVE
        qsTile.updateTile()
        // Called when the Tile becomes visible
    }
}