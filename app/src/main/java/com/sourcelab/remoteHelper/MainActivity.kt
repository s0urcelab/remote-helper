package com.sourcelab.remoteHelper

import android.os.Bundle
import android.os.Handler

import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import android.view.View
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        setupView()
    }

    private fun setupView() {
        on.setOnClickListener(wakeupHandler)
        off.setOnClickListener(shutdownHandler)
        fab.setOnClickListener(saveBtnHandler)
        mac.setText(AppPreferences.getVal("wol_mac"))
        wolHost.setText(AppPreferences.getVal("wol_host"))
        wolPort.setText(AppPreferences.getVal("wol_port"))
        sshHost.setText(AppPreferences.getVal("ssh_host"))
        sshPort.setText(AppPreferences.getVal("ssh_port"))
        username.setText(AppPreferences.getVal("ssh_name"))
        passwd.setText(AppPreferences.getVal("ssh_pw"))
        sshCmd.setText(AppPreferences.getVal("ssh_cmd"))
    }

    private var saveBtnHandler = View.OnClickListener { view ->

        // mac地址
        val macString = mac.text.trim().toString()
        val macReg = Regex("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})\$")
//        if(macString.isEmpty()){
//            Toast.makeText(this,"请输入${mac.hint}",Toast.LENGTH_SHORT).show()
//            return@OnClickListener
//        }

        if(macString.isNotEmpty() && !macReg.matches(macString)){
            Toast.makeText(this,"MAC地址格式不正确，参考00-33-AE-4F-2D-1A或00:33:AE:4F:2D:1A",Toast.LENGTH_LONG).show()
            mac.setText("")
            return@OnClickListener
        }

        AppPreferences.save("ssh_host", sshHost.text.trim().toString())
        AppPreferences.save("ssh_port", sshPort.text.trim().toString())
        AppPreferences.save("ssh_name", username.text.trim().toString())
        AppPreferences.save("ssh_pw", passwd.text.trim().toString())
        AppPreferences.save("ssh_cmd", sshCmd.text.trim().toString())

        AppPreferences.save("wol_mac", macString)
        AppPreferences.save("wol_host", wolHost.text.trim().toString())
        AppPreferences.save("wol_port", wolPort.text.trim().toString())

        Snackbar.make(view, "保存成功！", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
    }

    private var wakeupHandler = View.OnClickListener {
        val mac = AppPreferences.getVal("wol_mac")
        val host = AppPreferences.getVal("wol_host")
        val port = AppPreferences.getVal("wol_port")

        val uiHandler = Handler()


        if (mac == null || host == null || port == null || mac.isEmpty() || host.isEmpty() || port.isEmpty()) {
            Toast.makeText(this, "WOL相关参数不完整，请先长按快捷设置按钮进入APP设置", Toast.LENGTH_SHORT)
                .show()
        } else {
            val shortMac = mac.replace("-", "").replace(":", "")
            AsyncUtils.doAsync {
                val result = Common.wakeUp(host, shortMac, port.toInt())
                uiHandler.post {
                    Toast.makeText(this, if (result) "唤醒主机：${host} port:${port}" else "大概是网络错误", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }

    }

    private var shutdownHandler = View.OnClickListener {
        val cmd = AppPreferences.getVal("ssh_cmd")
        val host = AppPreferences.getVal("ssh_host")
        val name = AppPreferences.getVal("ssh_name")
        val pw = AppPreferences.getVal("ssh_pw")
        val port = AppPreferences.getVal("ssh_port")

        val uiHandler = Handler()

        if (cmd == null || host == null || name == null || pw == null || cmd.isEmpty() || host.isEmpty() || name.isEmpty() || pw.isEmpty()) {
            Toast.makeText(this, "SSH相关参数不完整，请先长按快捷设置按钮进入APP设置", Toast.LENGTH_SHORT)
                .show()
        } else {
            AsyncUtils.doAsync {
                val output = Common.executeRemoteCommand(name, pw, host, cmd, if (port == null || port.isEmpty()) 22 else port.toInt())
                uiHandler.post {
                    Toast.makeText(this, if (output.isEmpty()) "命令已执行" else output, Toast.LENGTH_LONG)
                        .show()
                }
            }
        }

    }
}
