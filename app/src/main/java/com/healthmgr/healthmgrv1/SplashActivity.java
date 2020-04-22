package com.healthmgr.healthmgrv1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;


public class SplashActivity extends Activity{
	
	private final long SPLASH_LENGTH = 2000;  
    Handler handler = new Handler();
    Runnable runnable;

    
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);
        
        runnable = new Runnable() {  
            @Override  
            public void run() {  
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);  
                finish();
            }  
        };
        
        handler.postDelayed(runnable, SPLASH_LENGTH);
    }  
    
    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		handler.removeCallbacks(runnable);
	}
}