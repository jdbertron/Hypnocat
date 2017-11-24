package com.bertron.hypnocat;

import com.bertron.hypnocat.BirdFlightView.AnimationThread;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

public class BirdSprite {

	private AnimationThread ov;
	double xSpeed,ySpeed;
	double x,y;
	int height,width;
	Bitmap b;
	int currentFrame = 0;
	long flightSpeed = 100;	// ms between frames
	long created = System.currentTimeMillis();

	public BirdSprite(AnimationThread animationThread, Bitmap blob, int x, int y, double xSpeed, double ySpeed, int flightSpeed) {
		b = blob;
		ov = animationThread;
		height = b.getHeight();
		width = b.getWidth() /16;
		this.xSpeed = xSpeed;
		this.ySpeed = ySpeed;
		this.x = x;
		this.y = y;
		this.currentFrame = ((int) x* (int) xSpeed)%16;	// some variation on how birds sync flight
		this.flightSpeed = flightSpeed;
	}

	public void onDraw(Canvas canvas) {
		int xoffset = width*currentFrame;	// Offset into the sprite sheet
		Rect src = new Rect(xoffset,0,xoffset+width, height);
		Rect dst = new Rect((int)x,(int)y,(int)x+width,(int)y+height);
		canvas.drawBitmap(b, src, dst, null);
	}
	
	public void setFrame(long time) {
		long diff = time-created;
		long frameinc = diff/flightSpeed;
		currentFrame = (int) ((frameinc)%16);
		//Log.w(this.getClass().getName(), "Time:" + time + " Frame : "+ currentFrame);
	}

	public void setCurrentFrame(int currentFrame) {
		this.currentFrame = currentFrame;
	}

	public void setFlightSpeed(int flightSpeed) {
		this.flightSpeed = flightSpeed;
	}
	
}
