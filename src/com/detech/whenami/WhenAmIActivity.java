package com.detech.whenami;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.BoundCamera;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.ui.activity.BaseGameActivity;

public class WhenAmIActivity extends BaseGameActivity
{

	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	// ===========================================================
	// Fields
	// ===========================================================

	private World mWorld;

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	public Engine onLoadEngine()
	{
		final BoundCamera cam = new BoundCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		Engine engine = new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), cam ));
		mWorld = new World( this, engine, cam );
		return engine;
	}

	public void onLoadResources()
	{
		mWorld.LoadResources();
	}

	public Scene onLoadScene()
	{
		return mWorld.LoadScene();
	}

	public void onLoadComplete()
	{

	}

	// ===========================================================
	// Methods
	// ===========================================================
	 
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}