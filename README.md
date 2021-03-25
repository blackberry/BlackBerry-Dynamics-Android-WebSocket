# BlackBerry Dynamics WebSocket library for Android

BlackBerry Dynamics WebSocket is Java library based on Java WebSocket and GDSocket. It provides secure WebSocket client support and conforms to the WebSocket (RFC 6455 and RFC 7692) Java client library for Android.

## Prerequisites

- BlackBerry Dynamics SDK for Android, check environment requirements [here](https://docs.blackberry.com/en/development-tools/blackberry-dynamics-sdk-android/).

For details on BlackBerry Dynamics please see https://www.blackberry.com/dynamics

## Features
- Nonblocking.
- Simple delegate pattern design.
- TLS/WSS support.

## Getting Started

### Integrate Blackberry Dynamics SDK
Follow these steps to integrate the BlackBerry Dynamics SDK:

https://developers.blackberry.com/us/en/resources/get-started/blackberry-dynamics-getting-started.html?platform=android#step-1

### Write your own WebSocket Client
The `org.java_websocket.client.WebSocketClient` abstract class can connect to valid WebSocket servers. The constructor expects a valid `ws://` URI to connect to. 

Important events `onOpen`, `onClose`, `onMessage` and `onError` get fired throughout the life of the WebSocketClient, and must be implemented in your subclass.

### Example

Once the library is imported, open a connection to your WebSocket server. 
```
WebSocketClient client = new SampleWebSocketClient(new URI("ws://localhost:8082"));
client.connect();
```
Note that socket is probably best provided as a property, so your delegate can stick around. Additionally, you can specify protocols if needed.
s

After establishing a connection, the following delegate methods are required to be implemented.
- `onOpen` - WebSocket is ready to read/write data.
- `onMessage` - Receive data in the onMessage method. It will contain a response or notification from the server.
- `onClose` - WebSocket has been closed. To send data to the socket it will be necessary to reconnect.
- `onError` - Caught an exception during reading/writing to the socket.


#### onOpen
```
@Override
public void onOpen(ServerHandshake handshakedata) {
    Log.i(TAG, "Socket Opened");
}
```

#### onMessage
```
@Override
public void onMessage(final String message) {
    Log.i(TAG, "Received data from the server");        
}
```

#### onClose
```
@Override
public void onClose(int code, String reason, boolean remote) {
    Log.d(this, "onClose invoked");
}
```

#### send
`webSocketClient.send(text) // write text over the socket!`

#### isOpen
Returns if the socket is connected or not.
```
if (webSocketClient.isOpen) {
    // Can write data to the socket.
}
```
#### close
Close the WebSocket connection.

`webSocketClient.close();`


## TLS/WSS Support
This library supports WebSocket over SSL/TLS (WSS). 

#### Certificates
A valid TLS certificate is required. 

If you are using a self-signed certificate, note that browsers will refuse the connection and not prompt the user to accept the certificate. The following article explains a technique which may be helpful to force the browser to accept a self signed certificate during testing. ( https://bugzilla.mozilla.org/show_bug.cgi?id=594502 ). 

Additionally the Java environment option `-Djavax.net.debug=all` can help to find problems with the certificate.

#### Limitations
- Accepting WS and WSS connections at the same time via the same WebSocket server instance is not currently supported. 
- Firefox does not allow multiple connections to the same WSS server if the server uses a different port than the default port (443). This is specifically relevant for Android.
