package com.detech.whenami;

public class PlayerCharacter extends Character
{
	public PlayerCharacter( final float fSpeed )
	{
		super(fSpeed);
		
	}
	
	
	public void handleInput ( final int dir )
	{
		if( getState() == CharacterState.IDLE )
		{
			switch(dir)
			{
			case World.DIR_NONE:
				super.setAnimation(Character.ANIM_IDLE);
				break;
	
			case World.DIR_LEFT:
			case World.DIR_RIGHT:
			case World.DIR_UP:
			case World.DIR_DOWN:
				super.requestWalk(dir);
				setAnimationAndDir( Character.ANIM_WALK, dir );
				break;
			}
		}
	}

}
