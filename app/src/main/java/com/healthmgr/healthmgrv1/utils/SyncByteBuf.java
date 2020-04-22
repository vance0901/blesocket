package com.healthmgr.healthmgrv1.utils;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;

/**
 * Created by wzl on 2017/12/9.
 */

public class SyncByteBuf {

    private static final int MAX_LEN    =   1024 * 1024 * 1;

    private PipedInputStream in;
    private PipedOutputStream out;

    public SyncByteBuf() {
        try {
            this.in = new PipedInputStream(1024 * 1024);
            this.out = new PipedOutputStream(this.in);
        } catch (Exception ex) {
            Log.e("", "pipe buf error..", ex);
        }
    }

    public void addByte(byte [] b, int offset, int count) throws IOException {
        this.out.write(b, offset, count);
    }

    public void addByte(byte b) throws IOException {
        this.out.write(b);
    }

    public int readBytes(byte [] b) throws IOException {
//        if (this.in.available() > 22050 * 10) {
////            Log.e("#######", ">>>>>>>>>>>>>>>>>>>  this.in.available() : " + this.in.available());
//            this.in.skip(22050 * 10);
//        }
        return this.in.read(b);
    }

    public byte readByte() throws IOException {
        if (this.in.available() > 22050 * 10) {
            Log.e("#######", ">>>>>>>>>>>>>>>>>>> 22 this.in.available() : " + this.in.available());
//            this.in.skip(22050 * 10);
        }
        int v = this.in.read();
        return (byte) (v & 0x00FF);
    }

    public int getDeep() throws IOException {
        return this.in.available();
    }

}
