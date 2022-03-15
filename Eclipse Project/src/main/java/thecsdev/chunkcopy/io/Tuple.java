package thecsdev.chunkcopy.io;

import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

/**
 * Java does not have a {@link Tuple}
 * so I had to make one.
 */
public class Tuple<X, Y>
{
	public final X Item1;
	public final Y Item2;

	public Tuple(X item1, Y item2)
	{
		this.Item1 = item1;
		this.Item2 = item2;
	}
	
	public static Tuple<World, ChunkPos> from(ServerCommandSource src)
	{
		Entity e = src.getEntity();
		if(e == null) throw new IllegalStateException("Source is not an entity.");
		return new Tuple<World, ChunkPos>(e.getWorld(), e.getChunkPos());
	}
}