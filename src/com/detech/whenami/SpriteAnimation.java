package com.detech.whenami;

public class SpriteAnimation
{
	public SpriteAnimation ( final long durations [], final int frames[], final int loops )
	{
		mDurations = durations;
		mFrames = frames;
		mLoops = loops;
	}
	
	public final long mDurations[];
	public final int mFrames[];
	public final int mLoops;
}
