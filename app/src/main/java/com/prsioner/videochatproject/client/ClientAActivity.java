package com.prsioner.videochatproject.client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.prsioner.videochatproject.DecodePlayerLiveH265;
import com.prsioner.videochatproject.R;
import com.prsioner.videochatproject.SocketCallBack;
import com.prsioner.videochatproject.server.ServerLiveSocket;
import com.prsioner.videochatproject.widget.LocalSurfaceView;

import java.io.IOException;

/**
 * 客户端页面
 */
public class ClientAActivity extends AppCompatActivity implements SocketCallBack {


    SurfaceView remoteSurfaceView;
    LocalSurfaceView localSurfaceView;
    DecodePlayerLiveH265 decodePlayerLiveH265;
    Surface surface;
    Button callButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_a);

        initView();
    }

    private void initView(){
        remoteSurfaceView = findViewById(R.id.remoteSurfaceView);
        localSurfaceView = findViewById(R.id.localSurfaceView);
        callButton = findViewById(R.id.btn_call);

        remoteSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                surface = surfaceHolder.getSurface();
                decodePlayerLiveH265 = new DecodePlayerLiveH265();
                decodePlayerLiveH265.initDecoder(surface);
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

            }
        });

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                localSurfaceView.startCapture(ClientAActivity.this,0);
            }
        });

    }

    //接受到另一端的数据，进行解码渲染
    @Override
    public void callBack(byte[] data) {
        if(decodePlayerLiveH265 !=null ){
            decodePlayerLiveH265.callBack(data);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        try {
//            localSurfaceView.onRelease();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException exception) {
//            exception.printStackTrace();
//        }
    }
}



















