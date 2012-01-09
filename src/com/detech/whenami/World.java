package com.detech.whenami;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.BoundCamera;
import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl.IOnScreenControlListener;
import org.anddev.andengine.engine.camera.hud.controls.DigitalOnScreenControl;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.SmartList;

public class World implements IUpdateHandler
{
	// DEBUG
	public static final boolean DEBUG		= false;

	// DIRECTIONS:
	public static final int DIR_NONE		= -1;
	public static final int DIR_DOWN		= 0;
	public static final int DIR_LEFT		= 1;
	public static final int DIR_RIGHT		= 2;
	public static final int DIR_UP			= 3;
	public static final int getDirX ( final int dir ) { return (dir==DIR_LEFT)?-1:(dir==DIR_RIGHT)?1:0; }
	public static final int getDirY ( final int dir ) { return (dir==DIR_UP)?-1:(dir==DIR_DOWN)?1:0; }

	public static final float GRID_SIZE = 32.0f;
	public static final float getPosFromTileX ( final int x ) { return x*GRID_SIZE; }
	public static final float getPosFromTileY ( final int y ) { return y*GRID_SIZE; }

	private static final String CHARACTER_TEXTURE_PATHS[] = { "char/Player.png","char/Actor1.png","char/Actor2.png","char/Actor3.png","char/Animal.png","char/Evil.png","char/Monster.png","char/People1.png","char/People2.png","char/People3.png","char/People4.png","char/People5.png","char/Spiritual.png" };
	private static final int CHARACTER_ATLAS_WIDTH = 2048;
	private static final int CHARACTER_ATLAS_HEIGHT = 2048;
	private static final int CHARACTER_SHEET_WIDTH = 384;
	private static final int CHARACTER_SHEET_HEIGHT = 256;
	private static final int CHARACTER_SHEETS_PER_ATLAS_ROW = CHARACTER_ATLAS_WIDTH/CHARACTER_SHEET_WIDTH;
	private static final int CHARACTERS_PER_SHEET = 8;
	private static final int CHARACTERS_PER_SHEET_ROW = 4;
	private static final int CHARACTERS_PER_SHEET_COL = 2;
	private static final int CHARACTER_FRAMES_PER_CHARACTER_X = 3;
	private static final int CHARACTER_FRAMES_PER_CHARACTER_Y = 4;
	private static final int CHARACTER_FRAMES_PER_SHEET_ROW = CHARACTER_FRAMES_PER_CHARACTER_X*CHARACTERS_PER_SHEET_ROW;
	private static final int CHARACTER_FRAMES_PER_SHEET_COL = CHARACTER_FRAMES_PER_CHARACTER_Y*CHARACTERS_PER_SHEET_COL;

	// ENGINE:
	private BaseGameActivity			mActivity;
	private Engine						mEngine;
	private BoundCamera					mCamera;
	
	// CONTROLS:
	private DigitalOnScreenControl		mDPad;
	private float						mDPadControlsX;
	private float						mDPadControlsY;
	private int							mDPadDir;
	
	// SYSTEM TEXTURES:
	private BitmapTextureAtlas			mSystemTextureAtlas;
	private TextureRegion				mDPadBaseTexture;
	private TextureRegion				mDPadStickTexture;

	// CHARACTER TEXTURE ATLAS:
	private BitmapTextureAtlas			mCharacterTextureAtlas;
	
	// CHARACTERs:
	private SmartList<PlayerCharacter>	mPlayerCharacters;
	
	// ACCESSORS:
	public final BaseGameActivity getActivity ( ) { return mActivity; }
	public final Engine getEngine ( ) { return mEngine; }
	public final BoundCamera getCamera ( ) { return mCamera; }
	public final float getDPadInputX ( ) { return mDPadControlsX; }
	public final float getDPadInputY ( ) { return mDPadControlsY; }

	
	// METHODS:
	public World( final BaseGameActivity activity, final Engine engine, final BoundCamera cam )
	{
		mEngine = engine;
		mActivity = activity;
		mCamera = cam;
		
		mDPadDir = DIR_NONE;
		mPlayerCharacters = new SmartList<PlayerCharacter>();
		
		// Set Logger.
		mEngine.registerUpdateHandler(new FPSLogger());
		mEngine.registerUpdateHandler(this);
	}
	
	public void LoadResources()
	{
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		//////////////////////////////////////////////////////////
		// Create the System Texture Atlas.
		mSystemTextureAtlas = new BitmapTextureAtlas( 256, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA );

		// Load the System Textures.
		mDPadBaseTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset( mSystemTextureAtlas, mActivity, "system/dpad_base.png", 0, 0 );
		mDPadStickTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset( mSystemTextureAtlas, mActivity, "system/dpad_stick.png", 128, 0 );
		mEngine.getTextureManager().loadTexture( mSystemTextureAtlas );
		
		
		//////////////////////////////////////////////////////////
		// Create the Character Texture Atlas. (5x8 CharacterSheets) (Each sheet contains 4x2 characters) (Each Character contains 3x4 frames) (Each frame is 16x16 pixels)
		mCharacterTextureAtlas = new BitmapTextureAtlas( CHARACTER_ATLAS_WIDTH, CHARACTER_ATLAS_HEIGHT, TextureOptions.NEAREST_PREMULTIPLYALPHA );

	}

	
	public Scene LoadScene()
	{
		// Create Level.
		final Level level = new Level( this, "POOOOS!", 1.0f, 1.0f, "town.tmx" );

		// Create the Controls.
		float distFromCorner = 40.0f;
		mDPad = new DigitalOnScreenControl( distFromCorner, mCamera.getHeight()-mDPadBaseTexture.getHeight()-distFromCorner, mCamera, mDPadBaseTexture, mDPadStickTexture, 0.1f, 
			new IOnScreenControlListener()
			{
				public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float pValueX, final float pValueY)
				{
					mDPadControlsX = pValueX;
					mDPadControlsY = pValueY;
					mDPadDir = ( pValueX > 0.25f ) ? DIR_RIGHT : ( pValueX < -0.25f ) ? DIR_LEFT : ( pValueY > 0.25f ) ? DIR_DOWN : ( pValueY < -0.25f ) ? DIR_UP : DIR_NONE ;
				}
			}
		)
		{
			protected void onUpdateControlKnob(final float pRelativeX, final float pRelativeY)
			{
				final float fDeadZone = 0.25f;
				super.onUpdateControlKnob(Math.abs(pRelativeX)<fDeadZone?0.0f:pRelativeX, Math.abs(pRelativeY)<fDeadZone?0.0f:pRelativeY);
			}
		};
		mDPad.getControlBase().setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		mDPad.getControlBase().setAlpha(0.5f);
		mDPad.getControlBase().setScaleCenter(0, 128);
		mDPad.getControlBase().setScale(0.8f);
		mDPad.getControlKnob().setScale(0.8f);
		mDPad.setAllowDiagonal(false);
		mDPad.refreshControlKnobPosition();
		level.setChildScene(mDPad);

		
		
		// Create the Character(s).
		if( !DEBUG )
		{
			PlayerCharacter player = createPlayerCharacter( 9 );
			player.teleport( 0, 0 );
			player.setVisible(false);
			level.addCharacter( player );
			level.enterViaDoor(player);
			mPlayerCharacters.add( player );
			mCamera.setChaseEntity(player);
		}
		else
		{
			final int w = 8;
			final int h = 8;
			for( int i=0; i<h; i++ )
			{
				for( int j=0; j<w; j++ )
				{
					final int index = (i*w)+j;
					PlayerCharacter player = createPlayerCharacter( index );
					player.setVisible(false);
					level.addCharacter( player );
					mPlayerCharacters.add( player );
				}
			}
		}
		return level;
	}


	public void onUpdate(float pSecondsElapsed)
	{
		// Handle Controls.
		final int dir = mDPadDir;
		
		final int playerCount = mPlayerCharacters.size();
		for(int i=playerCount-1; i>=0; i--)
		{
			final PlayerCharacter player = mPlayerCharacters.get(i);
			player.handleInput( dir );
			player.handleMove( pSecondsElapsed );
			player.setVisible(true);
		}
	}
	
	public void reset()
	{
		
	}

	private final Character createCharacter( final int characterID )
	{
		Character character = new Character( 3.0f );
		prepCharacter( character, characterID );
		return character;
	}

	private final PlayerCharacter createPlayerCharacter( final int characterID )
	{
		PlayerCharacter character = new PlayerCharacter( 3.0f );
		prepCharacter( character, characterID );
		return character;
	}
	
	private final void prepCharacter( final Character character, final int characterID )
	{
		character.createAnimatedSprite( createTextureRegion(characterID) );

		final int localCharID = characterID%CHARACTERS_PER_SHEET;
		final int localCharX = localCharID%CHARACTERS_PER_SHEET_ROW;
		final int localCharY = localCharID/CHARACTERS_PER_SHEET_ROW;
		final long walkAnimDelay = 400;

		int first = ((localCharY*CHARACTER_FRAMES_PER_CHARACTER_Y)*CHARACTER_FRAMES_PER_SHEET_ROW)+(localCharX*CHARACTER_FRAMES_PER_CHARACTER_X);
		character.addDirectionAnimation( World.DIR_DOWN, Character.ANIM_IDLE,	new SpriteAnimation( new long[]{1000},														new int[]{first+1},								 0 ) );
		character.addDirectionAnimation( World.DIR_DOWN, Character.ANIM_WALK,	new SpriteAnimation( new long[]{walkAnimDelay,walkAnimDelay,walkAnimDelay,walkAnimDelay},	new int[]{first+0,first+1,first+2,first+1},		-1 ) );
		first+=CHARACTER_FRAMES_PER_SHEET_ROW;
		character.addDirectionAnimation( World.DIR_LEFT, Character.ANIM_IDLE,	new SpriteAnimation( new long[]{1000},														new int[]{first+1},								 0 ) );
		character.addDirectionAnimation( World.DIR_LEFT, Character.ANIM_WALK,	new SpriteAnimation( new long[]{walkAnimDelay,walkAnimDelay,walkAnimDelay,walkAnimDelay},	new int[]{first+0,first+1,first+2,first+1},		-1 ) );
		first+=CHARACTER_FRAMES_PER_SHEET_ROW;
		character.addDirectionAnimation( World.DIR_RIGHT, Character.ANIM_IDLE,	new SpriteAnimation( new long[]{1000},														new int[]{first+1},								 0 ) );
		character.addDirectionAnimation( World.DIR_RIGHT, Character.ANIM_WALK,	new SpriteAnimation( new long[]{walkAnimDelay,walkAnimDelay,walkAnimDelay,walkAnimDelay},	new int[]{first+0,first+1,first+2,first+1},		-1 ) );
		first+=CHARACTER_FRAMES_PER_SHEET_ROW;
		character.addDirectionAnimation( World.DIR_UP, Character.ANIM_IDLE,		new SpriteAnimation( new long[]{1000},														new int[]{first+1},								 0 ) );
		character.addDirectionAnimation( World.DIR_UP, Character.ANIM_WALK,		new SpriteAnimation( new long[]{walkAnimDelay,walkAnimDelay,walkAnimDelay,walkAnimDelay},	new int[]{first+0,first+1,first+2,first+1},		-1 ) );
	}
	
	private final TiledTextureRegion createTextureRegion( final int characterID )
	{
		final int sheet = characterID/CHARACTERS_PER_SHEET;
		mEngine.getTextureManager().loadTexture( mCharacterTextureAtlas );
		return BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset( 
				mCharacterTextureAtlas, mActivity, CHARACTER_TEXTURE_PATHS[sheet],
				CHARACTER_SHEET_WIDTH*(sheet%CHARACTER_SHEETS_PER_ATLAS_ROW),
				CHARACTER_SHEET_HEIGHT*(sheet/CHARACTER_SHEETS_PER_ATLAS_ROW),
				CHARACTER_FRAMES_PER_SHEET_ROW, CHARACTER_FRAMES_PER_SHEET_COL );
	}




}
