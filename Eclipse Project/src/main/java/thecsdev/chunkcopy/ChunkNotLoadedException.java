package thecsdev.chunkcopy;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

/**
 * An {@link Exception} thrown when attempting to copy or
 * paste data to/from a chunk that isn't loaded.
 */
public final class ChunkNotLoadedException extends Exception
{
	// ==================================================
	private static final long serialVersionUID = -185372379145360932L;
	// --------------------------------------------------
	public final World world;
	public final ChunkPos chunkPos;
	// ==================================================
	public ChunkNotLoadedException(World world, ChunkPos chunkPos)
	{
		//call super
		super(createMessage(world, chunkPos));
		
		//define final variables
		this.world = world;
		this.chunkPos = chunkPos;
	}
	// ==================================================
	/**
	 * Creates an exception message for {@link ChunkNotLoadedException}s.
	 */
	private static String createMessage(World world, ChunkPos chunkPos)
	{
		//world id and chunk pos as strings
		String worldId = world.getRegistryKey().getValue().toString();
		String chunkPosS = "[" + chunkPos.x + " " + chunkPos.z + "]";
		
		//message
		return "The chunk at " + chunkPosS + " in " + worldId + " is not loaded.";
	}
	// ==================================================
}
