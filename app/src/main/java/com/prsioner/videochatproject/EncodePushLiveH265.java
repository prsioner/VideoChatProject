package com.prsioner.videochatproject;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import com.prsioner.videochatproject.client.ClientLiveSocket;
import com.prsioner.videochatproject.server.ServerLiveSocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @ClassName: EncodecPushLiveH265
 * @Author: qinglin
 * @CreateDate: 2021/2/27 17:50
 * @Description: 画面数据(YUV) 编码成H265 ，然后进行socket 传输
 */


public class EncodePushLiveH265 {
    
    public final int LOGIN_CLIENT_TYPE = 0;
    public final int LOGIN_SERVER_TYPE = 1;
    private final int type;
    
    private static final String TAG = "David";
    private MediaCodec mediaCodec;
    int width=1080;
    int height=1920;
    //    传输 过去
    private ServerLiveSocket serverLiveSocket;
    private ClientLiveSocket clientLiveSocket;
    //    nv21转换成nv12的数据
    byte[] nv12;
    //    旋转之后的yuv数据
    byte[] yuv;
    public static final int NAL_I = 19;
    public static final int NAL_VPS = 32;
    private byte[] vps_sps_pps_buf;
    int frameIndex;
    public EncodePushLiveH265(SocketCallBack socketCallback, int width, int height,int type) {
        this.type = type;
        if(type ==LOGIN_CLIENT_TYPE){
            clientLiveSocket = new ClientLiveSocket(socketCallback);
            clientLiveSocket.start();
        }else if(type==LOGIN_SERVER_TYPE){
            this.serverLiveSocket  = new ServerLiveSocket(socketCallback );
        // 建立链接
            serverLiveSocket.start();
        }else {
            throw new IllegalArgumentException("参数错误");
        }
        
        this.width = width;
        this.height = height;
    }
    //    startLive  MActivity
    public void startLive() {
//        实例化编码器
        //创建对应编码器
        try {
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC);

            //预览图宽和高是经过旋转的，原始画面是横着的宽高，所以在编码时原宽高就需要对调一下
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, height, width);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 1080 * 1920);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5); //IDR帧刷新时间
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mediaCodec.start();
//            周五 总的 新的知识点
            int bufferLength = width*height*3/2;
            nv12 = new byte[bufferLength];
            yuv = new byte[bufferLength];
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    ///摄像头调用
    public int encodeFrame(byte[] input) {
//        旋转
//        nv21-nv12
        nv12=YuvUtils.nv21toNV12(input);
//        旋转
        YuvUtils.portraitData2Raw(nv12, yuv, width, height);

        int inputBufferIndex = mediaCodec.dequeueInputBuffer(100000);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex);
            inputBuffer.clear();
            inputBuffer.put(yuv);
            //编码时加入时间戳
            long presentationTimeUs = computePresentationTime(frameIndex);
            mediaCodec.queueInputBuffer(inputBufferIndex, 0, yuv.length, presentationTimeUs, 0);
            frameIndex ++;
        }
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 100000);
        while (outputBufferIndex >= 0) {
            ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);
            dealFrame(outputBuffer, bufferInfo);
            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);

        }
        return 0;
    }

    private long computePresentationTime(long frameIndex) {
        //132 为偏移量，可以自定义，保证PTS 不从0开始，因为编码器初始化需要时间，视频里时间单位是微秒，而帧率是15 所以每一帧播放的时间戳是frameIndex*1000000/15
        return 132 + frameIndex * 1000000 / 15;
    }
    private void dealFrame(ByteBuffer bb, MediaCodec.BufferInfo bufferInfo) {
        int offset = 4;
        if (bb.get(2) == 0x01) {
            offset = 3;
        }
        int type = (bb.get(offset) & 0x7E) >> 1;
        if (type == NAL_VPS) {
            vps_sps_pps_buf = new byte[bufferInfo.size];
            bb.get(vps_sps_pps_buf);
        } else if (type == NAL_I) {
            final byte[] bytes = new byte[bufferInfo.size];
            bb.get(bytes);
            byte[] newBuf = new byte[vps_sps_pps_buf.length + bytes.length];
            System.arraycopy(vps_sps_pps_buf, 0, newBuf, 0, vps_sps_pps_buf.length);
            System.arraycopy(bytes, 0, newBuf, vps_sps_pps_buf.length, bytes.length);
            sendData(newBuf);
        } else {
            final byte[] bytes = new byte[bufferInfo.size];
            bb.get(bytes);
            sendData(bytes);
            Log.v(TAG, "视频数据  " + Arrays.toString(bytes));
        }
    }
    
    private void sendData(byte[] data){
        if(type == LOGIN_CLIENT_TYPE){
            clientLiveSocket.sendData(data);
        }else if(type == LOGIN_SERVER_TYPE){
            serverLiveSocket.sendData(data);
        }
    }
}

