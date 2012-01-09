/**
 * 
 */
package com.detech.whenami;

import java.util.ArrayList;

import org.anddev.andengine.engine.camera.BoundCamera;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLayer;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLoader;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLoader.ITMXTilePropertiesListener;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXProperties;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTile;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTileProperty;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTiledMap;
import org.anddev.andengine.entity.layer.tiled.tmx.util.exception.TMXLoadException;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.util.Debug;
import org.anddev.andengine.util.SmartList;

/**
 * @author Pete
 *
 */
public class Level extends Scene
{
	// MEMBERS:
	private World				mWorld;
	private String				mName;
	
	// LAYERS:
	private TMXTiledMap			mMap = null;
	private TMXLayer			mPhysics = null;
	private TMXLayer			mDoors = null;
	private SmartList<Door>		mEntranceList;
	private SmartList<Door>		mExitList;
	private TMXLayer			mInteraction = null;

	/////////////////////////////////////////////////
	
	public Level ( final World world, final String name, final float scaleX, final float scaleY, final String mapPath )
	{
		mWorld = world;
		mName = name;
		
		// Set up the Scene.
		setBackground( new ColorBackground(0.09804f, 0.6274f, 0.8784f) );
		setScale( scaleX, scaleY );
		
		loadMap( mapPath );
	}
	
	private void loadMap(String mapPath)
	{
		try
		{
			final World world = mWorld;
			mEntranceList = new SmartList<Door>();
			mExitList = new SmartList<Door>();
			
			final TMXLoader tmxLoader = new TMXLoader(world.getActivity(), world.getEngine().getTextureManager(), TextureOptions.BILINEAR_PREMULTIPLYALPHA, new ITMXTilePropertiesListener()
			{
				public void onTMXTileWithPropertiesCreated(final TMXTiledMap pTMXTiledMap, final TMXLayer pTMXLayer, final TMXTile pTMXTile, final TMXProperties<TMXTileProperty> pTMXTileProperties)
				{
					if(pTMXTileProperties.containsTMXProperty("door", "entrance"))
					{
						Level.this.mEntranceList.add( new Door( pTMXTile.getTileColumn(), pTMXTile.getTileRow(), World.DIR_NONE, Door.ENTRANCE ) );
					}
					else if(pTMXTileProperties.containsTMXProperty("door", "exit"))
					{
						Level.this.mExitList.add( new Door( pTMXTile.getTileColumn(), pTMXTile.getTileRow(), World.DIR_NONE, Door.EXIT ) );
					}
				}
			});

			final TMXTiledMap map = tmxLoader.loadFromAsset( mWorld.getActivity(), mapPath );
			setMap(map);
		}
		catch (final TMXLoadException tmxle)
		{
			Debug.e(tmxle);
		}
	}

	private void setMap( final TMXTiledMap map )
	{
		mMap = map; 
		
		// Create the Level map.
		final ArrayList<TMXLayer> layers = map.getTMXLayers();
		for( TMXLayer layer : layers )
		{
			if( layer.getName().equalsIgnoreCase("physics") )
			{
				// Set the Physics layer.
				mPhysics = layer;
			}
			else if( layer.getName().equalsIgnoreCase("doors") )
			{
				// Set the Doors layer.
				mDoors = layer;
			}
			else if( layer.getName().equalsIgnoreCase("interactions") )
			{
				// Set the Interaction layer.
				mInteraction = layer;
			}
			else
			{
				// Add a Visual Layer.
				super.attachChild(layer);
			}
		}
		
		// Make the camera not exceed the bounds of the TMXEntity.
		final int layerCount = layers.size();
		if( layerCount > 0 )
		{
			final TMXLayer layer0 = map.getTMXLayers().get(0);
			final BoundCamera cam = mWorld.getCamera();
			cam.setBounds( 0, layer0.getWidth(), 0, layer0.getHeight() );
			cam.setBoundsEnabled(true);
		}
	}
	
	public void addCharacter( final Character character, final int x, final int y )
	{
		addCharacter( character );
		character.teleport( x, y );
	}

	public void addCharacter( final Character character )
	{
		// Add Character to the Level.
		super.attachChild(character);
	}

	public boolean canMove( final int fromX, final int fromY, final int toX, final int toY, final int dir )
	{
		final TMXLayer phys = mPhysics;
		if( phys != null )
		{
			switch(dir)
			{
			case World.DIR_LEFT:
				if( toX < 0 )
					return false;
				break;

			case World.DIR_RIGHT:
				final int width = phys.getTileColumns();
				if( toX >= width )
					return false;
				break;

			case World.DIR_UP:
				if( toY < 0 )
					return false;
				break;

			case World.DIR_DOWN:
				final int height = phys.getTileRows();
				if( toY >= height )
					return false;
				break;
			}
			final TMXTile tile = phys.getTMXTile(toX, toY);
			if( tile != null )
			{
				final int id = tile.getGlobalTileID();
				return id==0;
			}
			return true;
		}
		else
		{
			if( toX < 0 && toX-fromX < 0 )
			{
				return false;
			}
			if( toY < 0 && toY-fromY < 0 )
			{
				return false;
			}
		}
		return true;
	}

	public void handleTileEntered( final int x, final int y )
	{
		final TMXLayer doors = mDoors;
		if( doors != null )
		{
			final TMXTile doorTile = doors.getTMXTile( x, y );
			final int doorTileID = doorTile.getGlobalTileID();
			if( doorTileID != 0 )
			{
				// EXIT LEVEL!
				
			}
		}
	}


	public void enterViaDoor ( Character character )
	{
		final SmartList<Door> doors = mEntranceList;
		if( doors != null )
		{
			final int size = doors.size();
			if( size>0 )
			{
				final Door door = doors.get(0);
				final int x = door.mX;
				final int y = door.mY;
				character.teleport(x,y);
			}
		}
	}
	
	
	


}
