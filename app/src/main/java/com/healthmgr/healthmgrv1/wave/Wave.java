package com.healthmgr.healthmgrv1.wave;

import java.util.concurrent.LinkedBlockingQueue;


import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

public class Wave 
{
	private SurfaceView                  mSurfaceView;
	private SurfaceHolder                mSurfaceHolder;
	private LinkedBlockingQueue<Integer> mWaveQueue;
	private Thread                       mWaveThread;
	private DrawRunnable                 mWaveRunnable;
	
	private boolean IsStoped = false;
	
	public Wave(SurfaceView surfaceView, WaveParse waveParse) {
		this.mSurfaceView = surfaceView;
		
		mSurfaceView.setZOrderOnTop(true);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
		
		mWaveQueue   =  new LinkedBlockingQueue<Integer>(1024);
		mWaveRunnable = new DrawRunnable(mWaveQueue,mSurfaceView,mSurfaceHolder,waveParse);
		mWaveThread = new Thread(mWaveRunnable);
		mWaveThread.start();
		
		mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
			
			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				// TODO Auto-generated method stub
				stop();

			}
			
			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				// TODO Auto-generated method stub
				start();
			}
			
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width,
					int height) {
				// TODO Auto-generated method stub
				//清除残留的痕迹
				Canvas mCanvas = holder.lockCanvas();
				mCanvas.drawColor(0x000000);
				holder.unlockCanvasAndPost(mCanvas);
			}
		});
	}
	
	
	protected void stop() {
		// TODO Auto-generated method stub
		mWaveQueue.clear();
		IsStoped = true;
	}


	protected void start() {
		// TODO Auto-generated method stub
		IsStoped = false;
	}


	public void add(Integer dat)
	{
		if(IsStoped) return;
		try 
		{
			mWaveQueue.put(dat);
			//Log.d("WAVE_QUEUE","add"+dat);
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
		//Log.i("-----WAVE_QUEUE-----","::::::"+mWaveQueue.size());
	}


	public void setVisibility(boolean b) {
		// TODO Auto-generated method stub
		if(b)
		{
			 mSurfaceView.setVisibility(View.VISIBLE);
		}
		else {
			 mSurfaceView.setVisibility(View.GONE);
		}
		
	}
}