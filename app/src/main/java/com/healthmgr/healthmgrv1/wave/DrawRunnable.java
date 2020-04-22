package com.healthmgr.healthmgrv1.wave;

import java.util.concurrent.LinkedBlockingQueue;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.healthmgr.healthmgrv1.MainActivity;
import com.healthmgr.healthmgrv1.R;


public class DrawRunnable implements Runnable{

	private Paint         mPaint;
	private int           WAVE_PADDING = 40;
	private int           STROKE_WIDTH = 3;
	private LinkedBlockingQueue<Integer> mQueue;
	private LinkedBlockingQueue<Integer> mQueueSurface;
	private int           mQueueSurfaceSize = 0;
	private int           mQueuemaxize = 1024;
	private SurfaceHolder mSurfaceHolder;
	private SurfaceView   mSurfaceView;
	private WaveParse     mWaveParas;
	
	private int           WAVE_SP_NUMBER = 1;
	
	
	public DrawRunnable(LinkedBlockingQueue<Integer> queue,
			SurfaceView surfaceView, SurfaceHolder surfaceHolder,
			WaveParse waveParas)
	{
		mPaint = new Paint();
		mPaint.setColor(Color.WHITE);
		mPaint.setStrokeWidth(STROKE_WIDTH);
		mPaint.setAntiAlias(true);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStyle(Paint.Style.STROKE);
		
		this.mQueue = queue;
		this.mSurfaceHolder = surfaceHolder;
		this.mSurfaceView = surfaceView;
		this.mWaveParas = waveParas;
		
	}


	@Override
	public void run() {
		// TODO Auto-generated method stub 

		mQueueSurface   =  new LinkedBlockingQueue<Integer>(mQueuemaxize);
		int    temp = 0;
	    Point  oldPoint = new Point(0, 0);
	    Point  newPoint = new Point(0, 0);
	    int[]  tempArray = new int[5];
	    int    counter   = 0;
	    Path   mPath     = new Path();
	    Path   mPath2     = new Path();
    	int surfaceflag = 0;
    	int surfacenumber = 0;
		int avr = 0x75;
		int maxVaule = 116;
		int minVaule = 116;

		int avr1 = 0;
		int avr2 = 0;
		int avr3 = 0;
		int avr4 = 0;
		int avr5 = 0;
		int avr6 = 0;
		int avr7 = 0;
		int avr8 = 0;
		int avr9 = 0;
		int avr10 = 0;
		int avr11 = 0;
		int avr12 = 0;
		int avr13 = 0;
		int avr14 = 0;
		int avr15 = 0;
		int avr16 = 0;
		
		//int extendVaule = 4;
		
		float extendVaule = 0;
		while(true)
		{
		    for(counter = 0; counter < mWaveParas.bufferCounter; counter++)
		    {
		    	try {
            		temp = mQueue.take();

        			//Log.d("WAVE_QUEUE","take"+temp);
            		
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	tempArray[counter] = temp;

		    	if(mQueueSurfaceSize>=mQueuemaxize)
		    	{
		    		int temp2 = mQueueSurface.poll();
			    	mQueueSurfaceSize--;
			    	//Log.d("WAVE_QUEUE", "pool"+temp2);
		    	}
		    	mQueueSurface.add(temp);
		    	mQueueSurfaceSize++;
		    }
		    surfacenumber =0;
		    
		    synchronized (this) {
	    		Canvas mCanvas = mSurfaceHolder.lockCanvas(new Rect(0,0,mSurfaceView.getWidth(),mSurfaceView.getHeight()));
			    if(mCanvas != null)
			    {
			    	//minVaule = maxVaule = mQueueSurface.peek();
			    	
			    	mCanvas.drawColor(MainActivity.mContext.getResources().getColor(R.color.emerald));
				    mPath.reset();
				    mPath2.reset();
				    oldPoint.x = (mSurfaceView.getWidth()-mWaveParas.xStep*mQueueSurfaceSize);
				    int all = 0;	
				    for (Integer y : mQueueSurface)  
					{
				    	if(surfaceflag==0)
				    	{
					    	newPoint.x = (oldPoint.x+mWaveParas.xStep*WAVE_SP_NUMBER);
					    	extendVaule = (float)(maxVaule-minVaule)/mSurfaceView.getHeight()/4;

			    		    //Log.d("WAVE_QUEUE", "extendVaule "+extendVaule+"maxVaule "+maxVaule+"minVaule " +minVaule);
			    		    
			            	//newPoint.y = mSurfaceView.getHeight()- WAVE_PADDING*2 - (int)(extendVaule*((y-avr)*4+avr)*(mSurfaceView.getHeight()- WAVE_PADDING*2));
			            	//newPoint.y = mSurfaceView.getHeight()- WAVE_PADDING*2 - (int)(0.004*((y-avr)*4+avr)*(mSurfaceView.getHeight()- WAVE_PADDING*2));
			            	newPoint.y = mSurfaceView.getHeight()- WAVE_PADDING*2 - (int)(0.003*((y-avr)*4+avr)*(mSurfaceView.getHeight()- WAVE_PADDING*2));
			            	//avr16 = avr15;
			            	//avr15 = avr14;
			            	//avr14 = avr13;
			            	//avr13 = avr12;
			            	//avr12 = avr11;
			            	//avr11 = avr10;
			            	//avr10 = avr9;
			            	//avr9 = avr8;
			            	/*
			            	avr8 = avr7;
			            	avr7 = avr6;
			            	avr6 = avr5;
			            	avr5 = avr4;
			            	avr4 = avr3;
			            	avr3 = avr2;
			            	avr2 = avr1;
			            	avr1 = newPoint.y;
			            	//newPoint.y = (avr1+avr2+avr3+avr4+avr5+avr6+avr7+avr8+avr9+avr10+avr11+avr12+avr13+avr14+avr15+avr16)/16;
			            	newPoint.y = (avr1+avr2+avr3+avr4+avr5+avr6+avr7+avr8)/8;
			            	*/
			            	
			            	if(newPoint.x>0)
			            	{
				    	        mPath.moveTo(oldPoint.x, oldPoint.y);
				    	        mPath.quadTo((newPoint.x+oldPoint.x)/2, (newPoint.y+oldPoint.y)/2, newPoint.x, newPoint.y);
				    		    surfacenumber++;
				    		    all = all+y;
				    		    
				    		    if(maxVaule<y)
				    		    	maxVaule = y;
				    		    if(minVaule>y)
				    		    	minVaule = y;
			            	}
			            	oldPoint.x = newPoint.x;
			    		    oldPoint.y = newPoint.y;
			    		    
				    	}
				    	surfaceflag=(surfaceflag+1)%WAVE_SP_NUMBER;
				    }
				    if(surfacenumber!=0)
				    {
				    	avr = (all/surfacenumber);
				    	//Log.d("WAVE_QUEUE", "avr = "+avr);
				    }
				    
				    surfaceflag = 0;
				    
				    mCanvas.drawPath(mPath, mPaint);
	    		    mCanvas.save();
	    		    //Log.d("WAVE_QUEUE", "SurfaceReflash"+mQueueSurfaceSize+"surfacenumber"+surfacenumber);
	    		    
	    		    mSurfaceHolder.unlockCanvasAndPost(mCanvas);
			    }
			}
		}
	}

	
}