package com.bertron.hypnocat;

import java.util.ListIterator;
import java.util.Random;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class BirdFlightView extends SurfaceView implements	SurfaceHolder.Callback {

	private AnimationThread mAnimationThread = null;

	private int screenWidth;
	private int screenHeight;

	private SoundPool mSoundPool; // plays sound effects
	private SparseIntArray mSoundMap; // maps IDs to SoundPool

	// Paint variables used when drawing each bird
	private Paint birdPaint;
	private Paint bkgPaint;
	private float lineWidth = 1.0f;
	Random rnd = new Random(System.currentTimeMillis());

	private boolean surfaceCreated = false;
	private boolean isPaused = true;

	public boolean isPaused() {
		return isPaused;
	}

	public void setPaused(boolean isPaused) {
		this.isPaused = isPaused;
	}

	public boolean isSurfaceCreated() {
		return surfaceCreated;
	}

	public BirdFlightView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// initialize SoundPool to play the app's three sound effects
		mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);

		// create Map of sounds and pre-load sounds
		mSoundMap = new SparseIntArray(); // create new HashMap
		mSoundMap.put(0, mSoundPool.load(context, R.raw.bird1, 1));
		mSoundMap.put(1, mSoundPool.load(context, R.raw.bird2, 1));
		mSoundMap.put(2, mSoundPool.load(context, R.raw.bird3, 1));
		mSoundMap.put(3, mSoundPool.load(context, R.raw.bird4, 1));
		mSoundMap.put(4, mSoundPool.load(context, R.raw.bird5, 1));
		mSoundMap.put(5, mSoundPool.load(context, R.raw.bird6, 1));
		mSoundMap.put(6, mSoundPool.load(context, R.raw.bird7, 1));
		mSoundMap.put(7, mSoundPool.load(context, R.raw.bird8, 1));
		mSoundMap.put(8, mSoundPool.load(context, R.raw.bird9, 1));
		mSoundMap.put(9, mSoundPool.load(context, R.raw.birda, 1));
		mSoundMap.put(10, mSoundPool.load(context, R.raw.birdb, 1));
		mSoundMap.put(11, mSoundPool.load(context, R.raw.birdc, 1));

		// construct Paints these are configured in method onSizeChanged
		birdPaint = new Paint(); // Paint for drawing text
		bkgPaint = new Paint();


		// // register our interest in hearing about changes to our surface
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);

		// create thread only; it's started in surfaceCreated()
		mAnimationThread = new AnimationThread(holder, context);

		setFocusable(true);
	}

	public AnimationThread getThread() {
		return mAnimationThread;
	}

//	/**
//	 * Standard window-focus override. Notice focus lost so we can pause on
//	 * focus lost. e.g. user switches to take a call.
//	 */
//	@Override
//	public void onWindowFocusChanged(boolean hasWindowFocus) {
//		if (!hasWindowFocus) {
//			if (mAnimationThread != null) 
//				mAnimationThread.setRunning(false);
//		}
//	}

	// called by surfaceChanged when the size of the SurfaceView changes,
	// and also when it is first added to the View hierarchy
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		screenWidth = w; // store the width
		screenHeight = h; // store the height

		// configure Paint objects
		birdPaint.setAntiAlias(true); // smoothes the text
		birdPaint.setStrokeWidth(lineWidth); // set line thickness
		birdPaint.setColor(Color.YELLOW);
		birdPaint.setTextSize(w / 30);
		birdPaint.setStyle(Paint.Style.FILL);

		bkgPaint.setColor(Color.BLACK); // set background color
	}

	// Respond to a user touch
	public void scareBird(MotionEvent event) {
		// get the location of the touch in this view
		Point touchPoint = new Point((int) event.getX(), (int) event.getY());

		if (mAnimationThread.isBird(touchPoint.x, touchPoint.y))
		{
			int sound = rnd.nextInt(12);
			mSoundPool.play(mSoundMap.get(sound), 1, 1, 1, 0, 1f); 
		}
	}

	// stops the game
	public void endAnimation() {
		boolean retry = true;
		mAnimationThread.setRunning(false);
		setPaused(false);
		while (retry) {
			try {
				mAnimationThread.join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}
		
		Log.w(this.getClass().getName(), "endAnimation()");
		mAnimationThread = null;
	}

	// releases resources; called by onDestroy method
	public void releaseResources() {
		mSoundPool.release(); // release all resources used by the SoundPool
		mSoundPool = null;
	}

	// called when surface changes size
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height) {
		mAnimationThread.setSurfaceSize(width, height);
	}

	// called when surface is first created
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		surfaceCreated  = true;
		if (!mAnimationThread.isRunning ) {
			mAnimationThread.setRunning(true);
			mAnimationThread.start();
		}
	}

	/*
	 * Callback invoked when the Surface has been destroyed and must no longer
	 * be touched. WARNING: after this method returns, the Surface/Canvas must
	 * never be touched again!
	 */
	public void surfaceDestroyed(SurfaceHolder holder) {
		// we have to tell thread to shut down & wait for it to finish, or else
		// it might touch the Surface after we return and explode
		surfaceCreated = false;

		Log.w(this.getClass().getName(), "surfaceDestroyed()");
	}

	public void doubleTap(MotionEvent e) {
		invalidate();
	}

	
	class AnimationThread extends Thread {

		public static final int STATE_PAUSE = 0;
		public static final int STATE_RUNNING = 1;
		/** The drawable to use as the background of the animation canvas */
		private SurfaceHolder mSurfaceHolder; // for manipulating canvas
		private boolean isRunning = false; // running by default
		private long mLastTime = System.currentTimeMillis()+100;
		private Vector<BirdSprite> sprites = new Vector<BirdSprite>();
		Bitmap blob1,blob2;
		private long mNextTime = 0;
		Random r = new Random(System.currentTimeMillis());
		double rate = 1.0/1000.0;	// Rate one per 1000 ms
		private long now;



		// initializes the surface holder
		public AnimationThread(SurfaceHolder holder, Context context) {
			mSurfaceHolder = holder;
			setName("AnimationThread");

			Resources res = context.getResources();
			// cache handles to our key sprites & other drawables
			blob1 = BitmapFactory.decodeResource(res, R.drawable.bird_flight1);
			blob2 = BitmapFactory.decodeResource(res, R.drawable.bird_flight2);
		}


		public void setSurfaceSize(int width, int height) {
			// synchronized to make sure these all change atomically
			synchronized (mSurfaceHolder) {
				// Start with 5 birds
				for (int i=0; i < 5;i++) {
					BirdSprite sprite = newBird(r);
					sprites.add(sprite);
				}
			}
		}
		 
		// Default birds
		public BirdSprite newBird(Random r) {
			int x = r.nextInt(200)-200;	// always start out of bounds
			int y = r.nextInt(50)+80;
			
			int n = r.nextInt(100)+1;
			int flightSpeed = 100/n+15;	// the less the faster
			double xSpeed = (2*n+500.0)/1000.0;	// pix/ms
			double ySpeed = -1*(r.nextInt(20)+80.0)/1000.0;
			Bitmap b;
			if (r.nextInt() %2 == 0) b= blob1;	else b= blob2; 
			//Log.w(this.getClass().getName(), "Sprite created: x:"+x+"   y:"+y+"   vx:"+xSpeed+"  vy:"+ySpeed+"  fs:"+flightSpeed);
			return new BirdSprite(this, b, x ,y ,xSpeed, ySpeed, flightSpeed);
		}
		
		// Generates the time for the next event following a random poisson process with rate lambda
		public double nextTime(Random r, double lambda) {
			double u = r.nextDouble();
			double nt = -1.0*Math.log(u)/lambda;
			return nt;
		}

		public synchronized void restoreState(Bundle savedState) {
			synchronized (mSurfaceHolder) {
				// mDifficulty = savedState.getInt(KEY_DIFFICULTY);
			}
		}

		public void setRunning(boolean running) {
			isRunning = running;
		}
		
		// controls the game loop
		@Override
		public void run() {
			
			// Wait for surface to become available
			while (!isSurfaceCreated() && isRunning) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			// Set the last Tick to right now
			mLastTime = System.currentTimeMillis();
			
			// main loop
			while (isRunning) {
				// Pause loop
				while (isPaused) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// Coming out of Pause, we don't want to jump ahead
					mLastTime = System.currentTimeMillis();
				}
				
				now = System.currentTimeMillis();
				
				Canvas c = null;
				try {
					if (isRunning) {	// still running
						c = mSurfaceHolder.lockCanvas(null);
						synchronized (mSurfaceHolder) {
							updatePhysics();
							drawGameElements(c);
						}
					}
				} 
				finally {
					// do this in a finally so that if an exception is thrown
					// during the above, we don't leave the Surface in an
					// inconsistent state
					if (c != null) {
						mSurfaceHolder.unlockCanvasAndPost(c);
					}
				}
			}
		}

		// Called by the animation thread, draws the game to the given Canvas
		public void drawGameElements(Canvas canvas) {
			// clear the background
			canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(),bkgPaint);
			//canvas.drawText(getResources().getString(R.string.app_name), 30, 50, birdPaint);
			for (BirdSprite b: sprites)
				b.onDraw(canvas); 
			//Log.w(this.getClass().getName(), "Canvas updated: "+ b.position.x);
		}

		void updatePhysics() {

			// Do nothing if mLastTime is in the future.
			// This allows the game-start to delay the start of the physics by 100ms or whatever.
			if (mLastTime > now)
				return;

			long elapsed = (now - mLastTime);

			// update the bird's position
			for (BirdSprite b: sprites) {
				double birdUpdateX = elapsed * b.xSpeed;
				double birdUpdateY = elapsed * b.ySpeed;				
				b.x += birdUpdateX;
				b.y += birdUpdateY;
				b.setFrame(now);
				//Log.w(this.getClass().getName(), "Sprite elapsed:"+elapsed+" x:"+b.x+"   y:"+b.y+"   vx:"+birdUpdateX+"  vy:"+birdUpdateY);
			}
			// Remove birds when they go out of frame
			ListIterator<BirdSprite> litr = sprites.listIterator(); 
			while(litr.hasNext()) {
			    BirdSprite b = litr.next(); 
			    if (b.x > screenWidth) {
					litr.remove();
			    }
			} 
			//Log.w(this.getClass().getName(), "Birds: "+ sprites.size());
			// Add new bird if necessary
			mNextTime -= elapsed;
			if (mNextTime < -1) {
				// Add bird
				sprites.add(newBird(r));
				// Figure out the next event
				mNextTime = (long) nextTime(r,rate);
			}
			mLastTime = now;
			//Log.w(this.getClass().getName(), "Physics updated");
		}

		public boolean isBird(int x, int y) {
			
			for (BirdSprite b: sprites) {
				if (x > b.x && y > b.y && x < (b.x+b.width) && y < (b.y+b.height)) {
					return true;
				}
			}
			return false;
		}
	}


}
