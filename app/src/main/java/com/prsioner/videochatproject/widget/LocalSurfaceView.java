package com.prsioner.videochatproject.widget;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.prsioner.videochatproject.EncodePushLiveH265;
import com.prsioner.videochatproject.SocketCallBack;
import com.prsioner.videochatproject.server.ServerLiveSocket;

import java.io.IOException;

/**
 * @ClassName: LocalSurfaceView
 * @Author: qinglin
 * @CreateDate: 2021/2/27 17:35
 * @Description: 本地画面: 不仅要拿到摄像头画面来预览，在socket连接后，还要把
 * 画面数据编码成h265 码流进行socket 传输
 */

public class LocalSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private Camera.Size size;
    private Camera mCamera;
    EncodePushLiveH265 encodecPushLiveH265;
    public LocalSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        startPreview();
    }
    byte[] buffer;
    private void startPreview() {
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
//        流程
        Camera.Parameters parameters = mCamera.getParameters();
//尺寸
        size = parameters.getPreviewSize();

        try {
            mCamera.setPreviewDisplay(getHolder());
//            横着
            mCamera.setDisplayOrientation(90);
            buffer = new byte[size.width * size.height * 3 / 2];
            mCamera.addCallbackBuffer(buffer);
            mCamera.setPreviewCallbackWithBuffer(this);
//            输出数据怎么办
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }
    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }
    //接受到  MainActivity调用
    public void startCapture(SocketCallBack socketCallback,int type){
        encodecPushLiveH265 = new EncodePushLiveH265(socketCallback,size.width, size.height,type);
        encodecPushLiveH265.startLive();

    }
    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
//        获取到摄像头的原始数据yuv
//        开始    视频通话
        if (encodecPushLiveH265 != null) {
            encodecPushLiveH265.encodeFrame(bytes);
        }

        mCamera.addCallbackBuffer(bytes);
    }
}
