package com.detech.whenami;

import org.anddev.andengine.entity.Entity;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;

public class Character extends Entity
{
	protected enum CharacterState
	{
		INVALID,
		IDLE,
		MOVING,
	}

	// ANIMATION TYPES
	public static final int ANIM_IDLE			= 0;
	public static final int ANIM_WALK			= 1;

	public static final int ANIM_DIR_TOTAL		= 2;
	public static final int ANIM_TOTAL			= ANIM_DIR_TOTAL*4;
	public static final int getDirAnimIndex( final int anim, final int dir ) { return (dir*ANIM_DIR_TOTAL)+anim; }

	// SPRITE AND ANIMATIONS
	private AnimatedSprite			mSprite;
	private final SpriteAnimation[]	mAnimations;
	private int						mAnimation;
	private int						mDirection;
	
	// TILE POS
	private int						mCX;
	private int						mCY;
	private int						mTX;
	private int						mTY;
	
	// STATE
	private CharacterState			mState;
	
	// STATS
	private float					mSpeed;
	private float					mInvAnimSpeed;

	
	// ACCESSORS:
	public final Level getLevel() { return (Level)getParent(); }
	protected final CharacterState getState() { return mState; }
	
	
	
	
	public Character ( final float fSpeed )
	{
		// Create the Animations.
		mAnimations = new SpriteAnimation[ANIM_TOTAL];
		mState = CharacterState.INVALID;
		mSpeed = fSpeed*World.GRID_SIZE;		// Grid blocks per second.
		mInvAnimSpeed = 1.0f/fSpeed;
		mDirection = World.DIR_DOWN;
		mAnimation = -1;
		mCX = mTX = 0;
		mCY = mTY = 0;
	}
	
	public void createAnimatedSprite ( final TiledTextureRegion texture )
	{
		// Create the Animated Sprite and add it to the scene.
		mSprite = new AnimatedSprite( 0.0f, 0.0f, texture );
		this.attachChild(mSprite);
	}
	
	public void addDirectionAnimation( final int dir, final int anim, final SpriteAnimation pAnim )
	{
		mAnimations[getDirAnimIndex(anim, dir)] = pAnim;
	}

	public void handleMove( final float dt )
	{
		final int cx = mCX;
		final int cy = mCY;

		final int tx = mTX;
		final int ty = mTY;

		if( tx==cx && ty==cy )
		{
			mState = CharacterState.IDLE;
		}
		else
		{
			mState = CharacterState.MOVING;
			final float fCurrentX = getX();
			final float fCurrentY = getY();
			final float fDestinationX = World.getPosFromTileX(tx);
			final float fDestinationY = World.getPosFromTileY(ty);
			final float fMaxDelta = mSpeed*dt;
			final float fDeltaX = Math.max( -fMaxDelta, Math.min( fMaxDelta, fDestinationX-fCurrentX ));
			final float fDeltaY = Math.max( -fMaxDelta, Math.min( fMaxDelta, fDestinationY-fCurrentY ));
			final float fNewX = fCurrentX+fDeltaX;
			final float fNewY = fCurrentY+fDeltaY;
			setPosition( fNewX, fNewY );

			if( Math.abs(fDeltaX)+Math.abs(fDeltaY) < Float.MIN_NORMAL )
			{
				mCX = tx;
				mCY = ty;
				mState = CharacterState.IDLE;
			}
		}
	}

	public void teleport( final int x, final int y )
	{
		// Set the Current and Target Tile positions.
		mCX = mTX = x;
		mCY = mTY = y;

		// Move the Actual Entity to the correct position.
		setPosition( mCX*World.GRID_SIZE, mCY*World.GRID_SIZE );
	}

	public void setDirection( final int dir )
	{
		setAnimationAndDir( mAnimation, dir );
	}

	public void setAnimation( final int animationIndex )
	{
		setAnimationAndDir( animationIndex, mDirection );
	}

	public void setAnimationAndDir( final int anim, final int dir )
	{
		final int currAnim = mAnimation;
		final int currDir = mDirection;
		if( currAnim != anim || currDir != dir )
		{
			if( anim>=0 && dir>=0 )
			{
				final int dirAnimIndex = getDirAnimIndex(anim,dir);
				final SpriteAnimation pAnim = mAnimations[dirAnimIndex];
				if( pAnim != null )
				{
					long durations[] = pAnim.mDurations.clone();
					for( int i=0; i<durations.length; i++ )
					{
						durations[i]*=mInvAnimSpeed;
					}
					
					final int frames[] = pAnim.mFrames;
					final int loops = pAnim.mLoops;
					mSprite.animate( durations, frames, loops );
					mAnimation = anim;
					mDirection = dir;
				}
			}
		}
	}

	public void requestWalk( final int dir )
	{
		Level pLevel = getLevel();
		if( pLevel != null )
		{
			if( mCX==mTX && mCY==mTY )
			{
				final int fromX = mCX;
				final int fromY = mCY;
				final int toX = fromX+World.getDirX(dir);
				final int toY = fromY+World.getDirY(dir);
				if( pLevel.canMove(fromX,fromY,toX,toY,dir) )
				{
					mTX = toX;
					mTY = toY;
				}
			}
		}
		
	}
	
	
}






