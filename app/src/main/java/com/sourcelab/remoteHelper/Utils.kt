package com.sourcelab.remoteHelper

import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.*

object HexUtils {

    /**
     * 十六进制String转换成Byte[]
     * @param hexString the hex string
     * *
     * @return byte[]
     */
    fun hexStringToBytes(hexString: String): ByteArray {
        var hexString = hexString
        if (hexString == "") {
            return ByteArray(0)
        }
        hexString = hexString.toUpperCase()
        val length = hexString.length / 2
        val hexChars = hexString.toCharArray()
        val d = ByteArray(length)
        for (i in 0..length - 1) {
            val pos = i * 2
            d[i] = (charToByte(hexChars[pos]).toInt() shl 4 or charToByte(hexChars[pos + 1]).toInt()).toByte()
        }
        return d
    }

    /**
     * Convert char to byte
     * @param c char
     * *
     * @return byte
     */
    private fun charToByte(c: Char): Byte {

        return "0123456789ABCDEF".indexOf(c).toByte()
    }
}

object AsyncUtils {
    class doAsync(val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
        init {
            execute()
        }

        override fun doInBackground(vararg params: Void?): Void? {
            handler()
            return null
        }
    }
}

object AppPreferences {
    private const val NAME = "com.sourcelab.remoteHelper"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences

    // list of app specific preferences
    private val IS_FIRST_RUN_PREF = Pair("is_first_run", false)

    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, MODE)
    }

    /**
     * SharedPreferences extension function, so we won't need to call edit() and apply()
     * ourselves on every SharedPreferences operation.
     */
    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    var firstRun: Boolean
        // custom getter to get a preference of a desired type, with a predefined default value
        get() = preferences.getBoolean(IS_FIRST_RUN_PREF.first, IS_FIRST_RUN_PREF.second)

        // custom setter to save a preference back to preferences file
        set(value) = preferences.edit {
            it.putBoolean(IS_FIRST_RUN_PREF.first, value)
        }

    fun save(KEY_NAME: String, value: String) {
        preferences.edit {
            it.putString(KEY_NAME, value)
        }
    }

    fun getVal(KEY_NAME: String): String? {
        return preferences.getString(KEY_NAME, "")
    }
}

object Common {
    /**
     * 构造magic魔术包
     * @param mac    mac
     * @return 包文
     */
    fun generateMagicPackage(mac: String) : String {
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
    fun wakeUp(host: String, mac: String, port: Int) : Boolean {
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

    fun executeRemoteCommand(username: String,
                             password: String,
                             hostname: String,
                             cmd: String,
                             port: Int): String {

        try {
            val jsch = JSch()
            val session = jsch.getSession(username, hostname, port)
            session.setPassword(password)

            // Avoid asking for key confirmation.
            val properties = Properties()
            properties.put("StrictHostKeyChecking", "no")
            session.setConfig(properties)

            session.connect()

            // Create SSH Channel.
            val sshChannel = session.openChannel("exec") as ChannelExec
            val outputStream = ByteArrayOutputStream()
            sshChannel.outputStream = outputStream

            // Execute command.
            sshChannel.setCommand(cmd)
            sshChannel.connect()

            // Sleep needed in order to wait long enough to get result back.
            Thread.sleep(1_000)
            sshChannel.disconnect()

            session.disconnect()

            return outputStream.toString()

        } catch (e: JSchException) {
//            e.printStackTrace()
            return e.message.toString()
        }
    }
}