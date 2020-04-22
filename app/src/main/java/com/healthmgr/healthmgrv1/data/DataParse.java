package com.healthmgr.healthmgrv1.data;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.healthmgr.healthmgrv1.common.MyApplication;
import com.healthmgr.healthmgrv1.utils.AudioUtils;
import com.healthmgr.healthmgrv1.utils.SyncByteBuf;

/**
 * 处理协议解析的类   用于 解析蓝牙设备传回的数据
 */
public class DataParse implements Runnable{

    private static DataParse ins = new DataParse();

    private final String TAG = getClass().getName();

    private SyncByteBuf recvData = new SyncByteBuf(); //  蓝牙传回的数据实际上放在这个缓存里面了

    private LinkedBlockingQueue<Integer> ecgWaveDataList = new LinkedBlockingQueue<Integer>();

    //    private ByteBuffer ecgAudioBuf = ByteBuffer.allocate(8300 * 10);
    private ByteBuffer ecgAudioBuf = ByteBuffer.allocate(4000 * 10);

    private int MAX_ECGWAVE_DATA_LENGTH         =   100;

    //  各种标志
    private final int ECGWAVE_CHECK  =   0x0080;
    private final int ECGWAVE_FLAG      =   0x0000;

    private final static Map<Integer, String> heartrateStateUnusualMap = new HashMap<Integer, String>() {
        {
            put(Unusual_1, "心率正常");
            put(Unusual_2, "心率过快");
            put(Unusual_3, "心率过慢");
            put(Unusual_4, "心率异常4");
            put(Unusual_5, "心率异常5");
            put(Unusual_6, "心率异常6");
            put(Unusual_7, "心率异常7");
            put(Unusual_8, "心率异常8");
        }
    };
    private final static int Unusual_1  =   0x0000;
    private final static int Unusual_2  =   0x0001;
    private final static int Unusual_3  =   0x0002;
    private final static int Unusual_4  =   0x0003;
    private final static int Unusual_5  =   0x0004;
    private final static int Unusual_6  =   0x0005;
    private final static int Unusual_7  =   0x0006;
    private final static int Unusual_8  =   0x0007;
    public String heartRateState = "正常";
    public int  heartRateVal = 70;


    public static final String ECG_WAVE_DATA_LISTENER     =   "ecgWaveDataListener";

    private Map<String, List<DataChangeListener>> listenerList = Collections.synchronizedMap(new HashMap<String, List<DataChangeListener>>());

    static {    //  在app启动时  会用改代码启动一个线程来解析数据
        Executors.newSingleThreadExecutor().execute(DataParse.getInstance());  //  会触发另外一个线程中执行下面的 run 函数
    }

    public static DataParse getInstance() {
        return ins;
    }

    public DataParse() {
        //
        listenerList.put(ECG_WAVE_DATA_LISTENER, Collections.synchronizedList(new ArrayList<DataChangeListener>()));
        ecgAudioBuf.position(0);

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Log.e("###", ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   now " + totalCount + ", buf deep is " + recvData.getDeep());
                        Thread.sleep(1000L * 1);
                    } catch (Throwable ex) {
                        Log.i("###", "", ex);
                    }
                }
            }
        });
    }

    public void addDataChangeListener(String type, DataChangeListener listener) {
        List<DataChangeListener> list = listenerList.get(type);
        if (list == null) {
            throw new IllegalArgumentException("Unknown type [" + type + "].");
        }
        list.add(listener);
    }

    private int totalCount = 0;

    public void add(byte[] buf, int bufSize) {
        try {
//            StringBuilder sb = new StringBuilder();
//            for (int i = 0; i < buf.length; i++) {
//                String s = Integer.toHexString(buf[i]);
//                sb.append(s.length() >=2 ? s.substring(s.length() - 2) : s).append(' ');
//            }
//            Log.e("###", ">>>>>>>>>  datas " + sb );
            recvData.addByte(buf, 0, bufSize);
            totalCount += bufSize;
        } catch (Exception ex) {
            Log.e("", "", ex);
        }
    }

    public List<Integer> getCurrentEcgDataList() {
        Integer [] buf = ecgWaveDataList.toArray(new Integer[0]);
        List<Integer> list = new ArrayList<Integer>(Arrays.asList(buf));
        return list;
    }


    //  会被上面触发执行 这就是线程的主函数
    @Override
    public void run() {
        try {
            boolean catchFile = false;// g改成Ture 可以用于录音成一个文件
            File file = new File("/sdcard/Download/test01.wav");// 注意该文件没有文件头 只有数据
            if (file.exists()) {
//                file.delete();
            }
            BufferedOutputStream out = null;
            if (catchFile) {
                out = new BufferedOutputStream(new FileOutputStream(file));
            }
            int dataCount = 0;
            long begin = System.currentTimeMillis();
            while (true) {// 这个死循环处理所有的数据
                byte a = recvData.readByte();// 从接收的缓存里面把数据一个个字节取出来处理
                if ((a & ECGWAVE_CHECK)== ECGWAVE_FLAG){   // 检测波形数据
                    //  心电波形，取后7位
                    int val = (int) ( a & 0x007F );
//                    Log.d("run","run ecgwave ==="+a+"    val");
                    if (catchFile) {// 如果录音，这里开始写文件到存储器
                        if (dataCount > 8300 * 60) {        //  注意 按照8300的采样率  这里只写了 60秒 数据

                            long cost = System.currentTimeMillis() - begin;
                            out.flush();
                            out.close();
                            Log.i("", "receive " + dataCount + " cost " + cost + " millis.");
                            break;
                        } else {
                            out.write(new byte [] { (byte) ((val / 256) & 0x00FF), (byte) (val & 0x00FF) });
                            dataCount++;
                            if ((dataCount % 10000) == 0 ) { //  每次获取1000个数据 输出一个调试信息日志数据
                                Log.e("#######", ">>>>>>>>>>>>>>>>>>>>received datacount " + dataCount);
                            }
                        }
                    }
                    notiffyEcgWaveData(val);// 通过一种 称为观察者模式 的方式  把数据发出去 给 所有关注它的该的对象
//                    if (ecgWaveDataList.size() > MAX_ECGWAVE_DATA_LENGTH) {
//                        ecgWaveDataList.drainTo(new ArrayList<Integer>(), ecgWaveDataList.size() - MAX_ECGWAVE_DATA_LENGTH);
//                    }
                } else {
                    //心率
                    int val = (a & 0x00FF ) & 0x007F ;
                    String heartrateStateDescr = heartrateStateUnusualMap.get(val);
                    Log.d("run","HeartRate_CHECK ==="+a+"    val");
                    if( heartrateStateDescr == null ){
                        //  心率   心率数据
                        int heartRateVal = val + 32;
                        Intent intent = new Intent("heartRateVal");
                        intent.putExtra("heartRateVal",heartRateVal);
                        LocalBroadcastManager.getInstance(MyApplication.mContext).sendBroadcast(intent);
                    } else {
                        //  心率异常
                        Intent intent = new Intent("heartRateState");
                        intent.putExtra("heartRateState", heartrateStateDescr);
                        LocalBroadcastManager.getInstance(MyApplication.mContext).sendBroadcast(intent);
                    }
                }
            }
            Log.i("", "Data parser loop exit....");
        } catch (Throwable th) {
            Log.e("", "", th);
        }
    }


    private void notiffyEcgWaveData(int val) {//  找到所有  ECG_WAVE_DATA_LISTENER  的数据 把数据通知出去
        List<DataChangeListener> list = listenerList.get(ECG_WAVE_DATA_LISTENER);
        for (DataChangeListener dcl : list) {
            dcl.addData(new Integer(val));
        }
    }

}