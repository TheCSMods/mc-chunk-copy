package thecsdev.chunkcopy.api;

import java.util.ArrayList;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import thecsdev.chunkcopy.mixin.WorldMixin;

/**
 * Provides utility methods for the {@link ChunkCopyAPI}.
 */
public final class ChunkCopyUtils
{

	// ==================================================
	/**
	 * Returns an {@link ArrayList} of {@link ChunkPos}itions
	 * of nearby loaded chunks.
	 * @param world The world where the chunks are located.
	 * @param chunkPos The central chunk around which to check for loaded chunks.
	 * @param chunkDistance The maximum distance of loaded chunks.
	 */
	public static ArrayList<ChunkPos> getNearbyLoadedChunks(World world, ChunkPos chunkPos, int chunkDistance)
	{
		//clamp chunkDistance
		if(chunkDistance < 1) chunkDistance = 1;
		else if(chunkDistance > 8) chunkDistance = 8;
		
		//define list
		ArrayList<ChunkPos> result = new ArrayList<>();
		
		//add chunks
		if(chunkDistance == 1) { result.add(chunkPos); }
		else if(chunkDistance > 1)
		{
			chunkDistance--;
			for(int chunkX = chunkPos.x - chunkDistance; chunkX < chunkPos.x + chunkDistance; chunkX++)
			{
				for(int chunkZ = chunkPos.z - chunkDistance; chunkZ < chunkPos.z + chunkDistance; chunkZ++)
				{
					//check if loaded
					if(!world.isChunkLoaded(chunkX, chunkZ)) continue;
					
					//add chunk to the list
					result.add(new ChunkPos(chunkX, chunkZ));
				}
			}
		}
		
		//return
		return result;
	}
	// --------------------------------------------------
	/**
	 * Returns a {@link Box} that contains a whole world chunk.
	 * @param world The world where the chunk is located.
	 * @param chunkPos The {@link ChunkPos} of the chunk in the world.
	 */
	public static Box getChunkBox(World world, ChunkPos chunkPos)
	{
		//calculate
		Chunk chunk = world.getChunk(chunkPos.getBlockPos(0, 0, 0));
		int chunkWidthX = Math.abs(chunkPos.getEndX() - chunkPos.getStartX());
		int chunkWidthZ = Math.abs(chunkPos.getEndZ() - chunkPos.getStartZ());
		Box chunkBox = new Box(
				chunkPos.getBlockPos(0, chunk.getBottomY(), 0),
				chunkPos.getBlockPos(chunkWidthX, chunk.getTopY(), chunkWidthZ));
		
		//return
		return chunkBox;
	}
	// --------------------------------------------------
	/**
	 * Returns a list of all {@link Entity}-s present in a world chunk.
	 * @param world The world where the chunk is located.
	 * @param chunkPos The {@link ChunkPos} of the chunk in the world.
	 */
	public static ArrayList<Entity> getEntitiesInChunk(World world, ChunkPos chunkPos)
	{
		//define list and chunk box
		ArrayList<Entity> result = new ArrayList<>();
		Box chunkBox = getChunkBox(world, chunkPos);
		
		//get and add entities
		((WorldMixin)world).getEntityLookup().forEachIntersects(chunkBox, e -> result.add(e));
		
		//return list
		return result;
	}
	// ==================================================
}
