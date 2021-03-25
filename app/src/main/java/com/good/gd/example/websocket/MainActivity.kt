/**
 * Copyright (c) 2020 BlackBerry Limited. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.good.gd.example.websocket

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.good.gd.GDAndroid
import com.good.gd.GDStateListener
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URI

class MainActivity : AppCompatActivity(), GDStateListener {

    val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            if (intent != null) {
                response.setText(intent.getStringExtra(RESPONSE_STRING))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        GDAndroid.getInstance().activityInit(this)
    }

    private fun initViews() {
        val uri = URI("ws://gs-gems-qa09.gems.sw.rim.net:8082/GaslampService/ws");
        val webSocketClient: SampleWebSocketClient =
            SampleWebSocketClient(
                this,
                uri,
                SampleWebSocketClient.getHttpHeaders(
                    this
                )
            )
        webSocketClient.connect();
        send.setOnClickListener {
            if (webSocketClient.isOpen) {
                webSocketClient.sendRequest(txtRequest.text.toString())
            } else {
                webSocketClient.reconnect();
            }
        }
    }


    override fun onLocked() {

    }

    override fun onWiped() {
    }

    override fun onUpdateConfig(p0: MutableMap<String, Any>?) {
    }

    override fun onUpdateServices() {
    }

    override fun onAuthorized() {
        initViews()
    }

    override fun onUpdateEntitlements() {
    }

    override fun onUpdatePolicy(p0: MutableMap<String, Any>?) {
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadCastReceiver, IntentFilter(BROADCAST_MESSAGE_RECEIVED_WEBSOCKET))
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadCastReceiver)
    }

    companion object {
        @kotlin.jvm.JvmField
        val RESPONSE_STRING = "RESPONSE"
        const val BROADCAST_MESSAGE_RECEIVED_WEBSOCKET = "messageReceivedFromWebSocket"
    }
}
