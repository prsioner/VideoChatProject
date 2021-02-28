package com.prsioner.videochatproject.server;

import android.util.Log;

import com.prsioner.videochatproject.SocketCallBack;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * @ClassName: ServerLiveSocket
 * @Author: qinglin
 * @CreateDate: 2021/2/27 17:55
 * @Description: 服务端
 */
public class ServerLiveSocket {

    private String TAG = ServerLiveSocket.class.getSimpleName();

    public SocketCallBack socketCallBack;
    private WebSocket webSocket;

    public ServerLiveSocket(SocketCallBack socketCallBack){
        this.socketCallBack =socketCallBack;
    }


    public void start(){
        webSocketServer.start();
    }

    public void close() throws IOException, InterruptedException {
        webSocket.close();
        webSocketServer.stop();
    }

    private WebSocketServer webSocketServer = new WebSocketServer(new InetSocketAddress(40001)) {
        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            Log.d(TAG,"webSocketServer onOpen()");
            webSocket = conn;
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            Log.d(TAG,"webSocketServer onClose()");

        }

        @Override
        public void onMessage(WebSocket conn, String message) {

        }

        @Override
        public void onMessage(WebSocket conn, ByteBuffer message) {
            Log.d(TAG,"onMessage --消息长度:"+message.remaining());
            byte[] buffers = new byte[message.remaining()];
            message.get(buffers);
            if(socketCallBack !=null){
                socketCallBack.callBack(buffers);

            }
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            Log.d(TAG,"webSocketServer onError:"+ex.toString());

        }

        @Override
        public void onStart() {

        }
    };

    /**
     * 服务端发送数据
     * @param bytes
     */
    public void sendData(byte[] bytes){
        if(webSocket !=null &&webSocket.isOpen()){
            webSocket.send(bytes);
        }
    }



}
