package com.detech.whenami;

public class Door
{
	public static final int ENTRANCE = 0; 
	public static final int EXIT = 1; 
	
	public final int mX;
	public final int mY;
	public final int mDir;
	public final int mType;
	
	public Door ( final int x, final int y, final int dir, final int type )
	{
		mX=x;
		mY=y;
		mDir=dir;
		mType=type;
	}
}
