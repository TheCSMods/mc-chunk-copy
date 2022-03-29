package thecsdev.chunkcopy.api.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

/**
 * Represents a {@link ChunkData} component that can
 * be copied from and pasted to world chunks.<br/>
 * <br/>
 * <b>Calling order for saving chunk data:</b><br/>
 * 1. {@link #copyData(World, ChunkPos)} then<br/>
 * 2. {@link #writeData(OutputStream)}<br/>
 * <br/>
 * <b>Calling order for loading chunk data:</b><br/>
 * 1. {@link #readData(InputStream)} then<br/>
 * 2. {@link #pasteData(ServerWorld, ChunkPos)}.
 */
public interface ChunkDataIO
{
	// ==================================================
	/**
	 * Copies data from a World chunk to this {@link ChunkDataIO}.<br/>
	 * Throwing exceptions here may have no effect.
	 * @param world The world where the chunk is located.
	 * @param chunkPos The {@link ChunkPos} of the chunk in the world.
	 */
	public abstract void copyData(World world, ChunkPos chunkPos);
	
	/**
	 * Pastes data from this {@link ChunkDataIO} to a world chunk.<br/>
	 * Throwing exceptions here may have no effect.
	 * @param world The world where the chunk is located.
	 * @param chunkPos The {@link ChunkPos} of the chunk in the world.
	 */
	public abstract void pasteData(ServerWorld world, ChunkPos chunkPos);
	// ==================================================
	/**
	 * Reads (loads) this {@link ChunkDataIO} from an {@link InputStream}.<br/>
	 * <b>This method will not read the total length of the data.</b>
	 * @param stream The {@link InputStream} that contains the data.
	 * @exception IOException If an IOException occurs while reading the data.
	 */
	public abstract void readData(InputStream stream) throws IOException;
	
	/**
	 * Writes (saves) this {@link ChunkDataIO} to an {@link OutputStream}.<br/>
	 * <b>This method will not write the total length of the data.</b>
	 * @param stream The {@link OutputStream} where the data will be stored.
	 * @exception IOException If an IOException occurs while writing the data.
	 */
	public abstract void writeData(OutputStream stream) throws IOException;
	// ==================================================
	/**
	 * Converts {@link #writeData(OutputStream)} to a byte array.
	 * @throws IOException See {@link #writeData(OutputStream)}.
	 */
	public default byte[] toByteArray() throws IOException
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		writeData(stream);
		byte[] bytes = stream.toByteArray();
		stream.close();
		return bytes;
	}
	// ==================================================
}
