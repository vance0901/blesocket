package com.healthmgr.healthmgrv1.utils;

import android.app.Application;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.net.rtp.AudioCodec;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.healthmgr.healthmgrv1.MainActivity;
import com.healthmgr.healthmgrv1.data.DataChangeListener;
import com.healthmgr.healthmgrv1.data.DataParse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by wzl on 2017/12/8.
 */

public class AudioUtils {

    File sdcard = Environment.getExternalStorageDirectory();       //获取存储，需要加上权限。

    private static AudioUtils single = new AudioUtils();

    Handler handler = new Handler();

    public static AudioUtils me() {
        return single;
    }

    private int channel = 4;
    private int bits = 16;
    private AudioTrack audioTrack;

    private SyncByteBuf audioData = null;

    private static final int sampleRateHz = 8440;

    public void startPlay() {
//        handler.post(new Runnable() {
////        Executors.newSingleThreadExecutor().execute(new Runnable() {
//            @Override
//            public void run() {
                try {
                    Log.i("", "begin play audio..");
                    int minBufSize = AudioTrack.getMinBufferSize(
                            sampleRateHz,
                            AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_8BIT);
                    audioTrack = new AudioTrack(    //  准备好一个播放的音轨
                            AudioManager.STREAM_MUSIC,  //音轨的播放在系统的 铃声 多媒体 通知 系统等   这几个中哪里播放出来
                            sampleRateHz,   //  采样频率
                            AudioFormat.CHANNEL_OUT_MONO,//  单、双声道，这里用的是单声道
                            AudioFormat.ENCODING_PCM_8BIT,// 声音数据是 16 位的
                            minBufSize,  // 音轨的内部缓存大小 影响好像不大
                            AudioTrack.MODE_STREAM  // 一定要用流模式
                    );
//                    audioTrack.setVolume(1.0f); // 设置音量 1.0 是最大音量 可以是小数

                    /**
                     * 下面一段是 张涵试图改变系统音量的代码 但是好像没凑效
                     */
//                    AudioManager audioManager = (AudioManager) MainActivity.mContext.getSystemService(Context.AUDIO_SERVICE);
//                    int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
//                    audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM
//                            , AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND) ;
//                    int current = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
//                    audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
//                    audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 50, AudioManager.FLAG_PLAY_SOUND);


                    audioTrack.play();// 通知音轨准备播放 这里实际上还没有开始出声

                    Log.i("", "create track success..");
                } catch (Throwable th) {
                    Log.e("", "", th);
                }
//            }
//        });
    }

    private void playOnce() {
        try {
            final byte [] thisBuf = new byte[ 1024 * 8 * 16 ];
//            Arrays.fill(thisBuf, (byte) 0);
//            for (int i = 0; i < 300; i += 2) {
//                int vv = (int) (Math.asin(Math.PI * 2 * i / 300)) * 4096 * 4;
//                thisBuf[i] = (byte) (vv & 0x00FF);
//                thisBuf[i + 1] = (byte) ((vv / 256) & 0x00FF);
//            }
//            audioTrack.write(thisBuf, 0, 300 );
//            audioTrack.flush();
//            if (1 == 1) {
//                return;
//            }
            final int readCount = audioData.readBytes(thisBuf);
            Log.e("########", ">>>>>>>>>>>>>> count " + readCount);
//            byte [] tmpBuf = fillBytes(thisBuf, readCount);
            if (readCount > 0) {
//                audioTrack.write(tmpBuf, 0, sampleRateHz );
                long t2 = System.currentTimeMillis();
                audioTrack.write(thisBuf, 0, readCount );
                long t3 = System.currentTimeMillis();
                if (t3 - t2 > 100) {
                    Log.e("###", ">>>>>>>>>   play once soct time " + (t3 - t2) + " ms.");
                }
//                audioTrack.flush();
            }
        } catch (Exception ex) {
            Log.e("", "", ex);
        }
    }

    private byte [] lastAudioVal = new byte [] { 0, 0 };

    private byte [] fillBytes(byte [] src, int srcLen) {
        byte [] tmpBuf = new byte [sampleRateHz];
        for (int i = 0; i < tmpBuf.length - 1; i++) {
            int j = (int) (1L * i * srcLen * 1.0 / tmpBuf.length );
            //j = j / 2 * 2;
            tmpBuf[i] = src[j];
        }
//        tmpBuf[0] = (byte) ((lastAudioVal[0] + tmpBuf[0]) / 2);
//        tmpBuf[1] = (byte) ((lastAudioVal[1] + tmpBuf[1]) / 2);
//        lastAudioVal[0] = tmpBuf[tmpBuf.length - 2];
//        lastAudioVal[1] = tmpBuf[tmpBuf.length - 1];
        return tmpBuf;
    }

//    public void putAudioDatas(byte b1, byte b2) {
//        try {
//            audioData.addByte(b1);
//            audioData.addByte(b2);
//        } catch (Exception ex) {
//            Log.e("", "", ex);
//        }
//    }

    public void startMain() {
        audioData = new SyncByteBuf();
                    Executors.newSingleThreadExecutor().execute(new Runnable() {
                        @Override
                        public void run() {
                            while(true) {
                                try {
                                    playOnce();
                                    Thread.sleep(1000 * 1);
                                } catch (Throwable th) {
                                    Log.e("", "", th);
                                }
                            }
                        }
                    });
    }

    public void listenRealVoice() {//  向协议解释器注册 该程序关注 ECG_WAVE_DATA_LISTENER 类型的数据
        DataParse.getInstance().addDataChangeListener(DataParse.ECG_WAVE_DATA_LISTENER, new DataChangeListener() {
            @Override
            public String getType() {
                return DataParse.ECG_WAVE_DATA_LISTENER;
            }

            public void addData(final Integer val) {// 每当一个数据来的时候 这个方法会被回调
                        try {
                            int v = val.intValue();
                            v = (v & 0x00FFFF) * 4;// 声音放大4倍 因为是12位的数据 用16位音轨放出来
                            if (audioData == null) {
                                audioTrack.write(new byte[]{ (byte) (v & 0x00FF) }, 0, 1);
                                // 把数据写到音轨上去  就会被播放出来了     注意 高位和低位 的数据顺序
                            } else {
                                audioData.addByte(new byte[]{ (byte) (v & 0x00FF) }, 0, 1);
                            }
                        } catch (Throwable ex) {
                            Log.e("", "", ex);
                        }
            }
        });
    }

    /**
     * 用录音文件的数据  录音数据
     */
    public void createSimulationDatas() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    createSimulationDatasFromFile();//  读文件的线程
                } catch (Throwable th) {
                    Log.e("", "", th);
                }
            }
        });
    }

    private void createSimulationDatasFromFile() throws Exception {
        byte [] buffer = new byte[1024 * 1024 * 1];// 准备  1MB的缓存用于读文件
        File f = new File("/sdcard/Download/test01-2.wav");
        FileInputStream fin = new FileInputStream(f);
        int readCount = fin.read(buffer);// 实际读到 readCount 个字节 放在 buffer 中
        fin.close();
/**
 * 下面是读取标准的 WAV  文件 的
 */
//        int fmtLen = 0;
//        fmtLen = buffer[0x13];
//        fmtLen = 256 * fmtLen + buffer[0x12];
//        fmtLen = 256 * fmtLen + buffer[0x11];
//        fmtLen = 256 * fmtLen + buffer[0x10];

        int dataStart = 0x3E;

        int pcmlen=0;
        pcmlen+=buffer[dataStart + 0x04 + 0x03];
        pcmlen=pcmlen*256+buffer[dataStart + 0x04 + 0x02];
        pcmlen=pcmlen*256+buffer[dataStart + 0x04 + 0x01];
        pcmlen=pcmlen*256+buffer[dataStart + 0x04 + 0x00];

        channel=buffer[0x17];
        channel=channel*256+buffer[0x16];

        bits=buffer[0x23];
        bits=bits*256+buffer[0x22];

//        for (int i = 0; i < pcmlen; i++) {
//            audioData.offer(new Byte(buffer[dataStart + 0x08 + i]));
//        }
/**
 * 上面是读取标准的 WAV  文件 的
 */
        for (int i = 0; i < readCount - 1; ) {  // 这个循环 用于更换 高地位顺序
            byte b1 = buffer[i];
            if (b1 % 2 == 0) {
                b1 = (byte) (b1 & 0x0F);
            }
            byte b2 = buffer[i + 1];
            audioData.addByte(b2); //换好顺序的 数据 写入  audioData  缓存中
            audioData.addByte(b1);

            i++;
            i++;
        }

        byte [] thisBuf = new byte[1024 * 1024 * 1];
        int cc = audioData.readBytes(thisBuf); //  从缓存里把录音文件数据取出来
        audioTrack.write(thisBuf, 0, cc);   // 把露营文件播放出来
        audioTrack.flush(); // 音轨的缓存需要刷新一下

        Log.i("", "finished create simulation datas.");
    }

}
