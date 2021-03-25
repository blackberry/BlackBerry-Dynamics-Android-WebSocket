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

package com.good.gd.example.websocket;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.protocols.IProtocol;
import org.java_websocket.util.WSLog;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.SSLSession;

/**
 * SampleWebSocketClient is a WebSocket Client. we instantiate a websocket client connection.
 * The callback methods, onOpen, onClose and onError from
 * WebSocket Client inform the status of the websocket connection.
 */
public class SampleWebSocketClient extends WebSocketClient {

    private boolean canSendRequest = false;
    private Context context;

    public SampleWebSocketClient(Context context,
                                 URI serverURI,
                                 Map<String, String> httpHeaders) {

        // Open the websocket connection using proper headers.
        super(serverURI, httpHeaders);
        this.context = context;
    }

    @NonNull
    public static Map<String, String> getHttpHeaders(Context context) {
        Map<String, String> httpHeaders = new HashMap<>();

        String sequenceNumber = "-1";

        String deviceType = "Android";
        String localeIdentifier = Locale.getDefault().toString();
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy HH:mm:ss Z", Locale.US);

        httpHeaders.put("Accept-Language", localeIdentifier);
        httpHeaders.put("Date", dateFormat.format(new Date()));
        httpHeaders.put("X-User-Agent", deviceType);
        httpHeaders.put("X-Notify", "true");
        httpHeaders.put("sequence", sequenceNumber);
        httpHeaders.put("X-GC-DeviceToken", Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID));
        httpHeaders.put("x-good-background",
                Boolean.toString(false));
        String gdAuthToken = GDState.sharedInstance().getGdAuthToken();
        httpHeaders.put("x-good-gd-authtoken", gdAuthToken);
        return httpHeaders;
    }

    public void sendRequest(String request) {
        // Send the request through websocket.
        if (canSendRequest) {
            send(request);
        }
    }

    public void stop() {
        // Close the webSocket connection.
        super.close();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        canSendRequest = true;
    }

    @Override
    public void onMessage(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                handleMessageReceivedFromWebSocket(message);
            }
        });
    }

    private void handleMessageReceivedFromWebSocket(String message) {
        Intent intent = new Intent(MainActivity.BROADCAST_MESSAGE_RECEIVED_WEBSOCKET);
        intent.putExtra(MainActivity.RESPONSE_STRING, message);
        if (context != null) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        WSLog.d(this, "onClose invoked");
        canSendRequest = false;
    }

    @Override
    public void onError(Exception ex) {
        WSLog.d(this, ex, "onError: ex = " + ex);
        canSendRequest = false;
    }

    @Override
    public boolean hasSSLSupport() {
        return false;
    }

    @Override
    public SSLSession getSSLSession() throws IllegalArgumentException {
        return null;
    }

    @Override
    public IProtocol getProtocol() {
        return null;
    }
}
