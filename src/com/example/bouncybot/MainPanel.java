package com.example.bouncybot;
/**
 * 
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * @author impaler
 * This is the main surface that handles the ontouch events and draws
 * the image to the screen.
 */
public class MainPanel extends SurfaceView implements
		SurfaceHolder.Callback {

	private static final String TAG = MainPanel.class.getSimpleName();
	
	private MainThread thread;
	private Player p;

	public MainPanel(Context context) {
		super(context);
		// adding the callback (this) to the surface holder to intercept events
		getHolder().addCallback(this);

		// create p and load bitmap
		p = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.fobo0), 50, 50);
		
		// create the game loop thread
		thread = new MainThread(getHolder(), this);
		
		// make the GamePanel focusable so it can handle events
		setFocusable(true);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// at this point the surface is created and
		// we can safely start the game loop
		thread.setRunning(true);
		thread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "Surface is being destroyed");
		// tell the thread to shut down and wait for it to finish
		// this is a clean shutdown
		boolean retry = true;
		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
				// try again shutting down the thread
			}
		}
		Log.d(TAG, "Thread was shut down cleanly");
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// delegating event handling to the Player
			p.handleActionDown((int)event.getX(), (int)event.getY());
			
			// check if in the lower part of the screen we exit
			if (event.getY() > getHeight() - 50) {
				thread.setRunning(false);
				((Activity)getContext()).finish();
			} else {
				Log.d(TAG, "Coords: x=" + event.getX() + ",y=" + event.getY());
			}
		} if (event.getAction() == MotionEvent.ACTION_MOVE) {
			// the gestures
			if (p.isTouched()) {
				// the Player was picked up and is being dragged
				p.setX((int)event.getX());
				p.setY((int)event.getY());
			}
		} if (event.getAction() == MotionEvent.ACTION_UP) {
			// touch was released
			if (p.isTouched()) {
				p.setTouched(false);
			}
		}
		return true;
	}

	public void render(Canvas canvas) {
		canvas.drawColor(Color.BLACK);
		p.draw(canvas);
	}

	/**
	 * This is the game update method. It iterates through all the objects
	 * and calls their update method if they have one or calls specific
	 * engine's update method.
	 */
	public void update() {
		// check collision with right wall if heading right
		if (p.getSpeed().getxDirection() == Speed.DIRECTION_RIGHT
				&& p.getX() + p.getBitmap().getWidth() / 2 >= getWidth()) {
			p.getSpeed().toggleXDirection();
		}
		// check collision with left wall if heading left
		if (p.getSpeed().getxDirection() == Speed.DIRECTION_LEFT
				&& p.getX() - p.getBitmap().getWidth() / 2 <= 0) {
			p.getSpeed().toggleXDirection();
		}
		// check collision with bottom wall if heading down
		if (p.getSpeed().getyDirection() == Speed.DIRECTION_DOWN
				&& p.getY() + p.getBitmap().getHeight() / 2 >= getHeight()) {
			p.getSpeed().toggleYDirection();
		}
		// check collision with top wall if heading up
		if (p.getSpeed().getyDirection() == Speed.DIRECTION_UP
				&& p.getY() - p.getBitmap().getHeight() / 2 <= 0) {
			p.getSpeed().toggleYDirection();
		}
		// Update the lone Player
		p.update();
	}

}
