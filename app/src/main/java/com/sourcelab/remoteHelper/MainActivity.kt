package com.sourcelab.remoteHelper

import android.os.Bundle

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

//        if(wolHost.text.trim().isEmpty()){
//            Toast.makeText(this,"请输入${wolHost.hint}",Toast.LENGTH_SHORT).show()
//            return@OnClickListener
//        }
//
//        if(wolPort.text.trim().isEmpty()){
//            Toast.makeText(this,"请输入${wolPort.hint}",Toast.LENGTH_SHORT).show()
//            return@OnClickListener
//        }
//
//        if(sshHost.text.trim().isEmpty()){
//            Toast.makeText(this,"请输入${sshHost.hint}",Toast.LENGTH_SHORT).show()
//            return@OnClickListener
//        }
//
//        if(sshPort.text.trim().isEmpty()){
//            Toast.makeText(this,"请输入${sshPort.hint}",Toast.LENGTH_SHORT).show()
//            return@OnClickListener
//        }
//
//        if(username.text.trim().isEmpty()){
//            Toast.makeText(this,"请输入${username.hint}",Toast.LENGTH_SHORT).show()
//            return@OnClickListener
//        }
//
//        if(passwd.text.trim().isEmpty()){
//            Toast.makeText(this,"请输入${passwd.hint}",Toast.LENGTH_SHORT).show()
//            return@OnClickListener
//        }
//
//        if(sshCmd.text.trim().isEmpty()){
//            Toast.makeText(this,"请输入${sshCmd.hint}",Toast.LENGTH_SHORT).show()
//            return@OnClickListener
//        }
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
}
