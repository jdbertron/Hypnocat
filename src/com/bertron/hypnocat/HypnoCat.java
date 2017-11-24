package com.bertron.hypnocat;

import com.bertron.hypnocat.BirdFlightView.AnimationThread;

import android.media.AudioManager;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;

public class HypnoCat extends Activity {

	private GestureDetector gestureDetector;
	private BirdFlightView birdFlightView;
	private AnimationThread animationThread;
	
	// Listens for touch events sent to the GestureDetector
	SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {
		// called when the user double taps the screen
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			birdFlightView.doubleTap(e);
			return true; // the event was handled
		}
	};
	   

	@Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        Eula.show(this);
        setContentView(R.layout.bird_flight);	// inflate layout
        
        birdFlightView = (BirdFlightView) findViewById(R.id.bird_flight_view);
        animationThread = birdFlightView.getThread();
        
        // Initialize the GestureDetector
        gestureDetector = new GestureDetector(this, gestureListener);
        
        Log.w(this.getClass().getName(), "onCreate()");
        // allow volume keys to set game volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }	
	
    @Override
	protected void onStart() {
		super.onStart();
		Log.w(this.getClass().getName(), "onStart()");
	}
    
    // when the app is pushed to the background, pause it
    @Override
    public void onPause()
    {
       super.onPause(); 
       Log.w(this.getClass().getName(), "onPause()");
       birdFlightView.setPaused(true);
    }
    

    @Override
	protected void onResume() {
		super.onResume();
		Log.w(this.getClass().getName(), "onResume()");
		birdFlightView.setPaused(false);
	}

     
	@Override
	protected void onStop() {
		super.onStop();
		Log.w(this.getClass().getName(), "onStop()");
	}

	@Override
	protected void onDestroy() {	// App is being destroyed
		super.onDestroy();
		Log.w(this.getClass().getName(), "onDestroy()");
		birdFlightView.endAnimation(); // terminates the animation
		birdFlightView.releaseResources();
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
	
   // Simple override called when the user touches the screen in this Activity
   @Override
   public boolean onTouchEvent(MotionEvent event)
   {
      // get integer representing the type of action which caused this event
      int action = event.getAction();

      // the user user touched the screen or dragged along the screen
      if (action == MotionEvent.ACTION_DOWN)
      {
    	  birdFlightView.scareBird(event); 
      } 

      // call the GestureDetector's onTouchEvent method
      return gestureDetector.onTouchEvent(event);
   }

//   @Override
//   public boolean onKeyDown(int keyCode, KeyEvent event) {
//    switch(keyCode){
//    case KeyEvent.KEYCODE_MENU:
//    	Log.w(this.getClass().getName(), "Menu");
//       return true;
//      case KeyEvent.KEYCODE_SEARCH:
//        Log.w(this.getClass().getName(), "Search");
//        return true;
//      case KeyEvent.KEYCODE_BACK:
//    	Log.w(this.getClass().getName(), "Back");
//        return false;
//      case KeyEvent.KEYCODE_VOLUME_UP:
//    	Log.w(this.getClass().getName(), "VolumeUp");
//        return false;
//      case KeyEvent.KEYCODE_VOLUME_DOWN:
//        Log.w(this.getClass().getName(), "VolumeDown");
//        return false;
//    }
//    return super.onKeyDown(keyCode, event);
//   }
}
