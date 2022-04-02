package thecsdev.chunkcopy.api.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import thecsdev.chunkcopy.api.io.IOUtils;

/**<b>Important: Please see {@link thecsdev.chunkcopy.api.data}!</b><br/>
 * <br/>
 * A chunk data block contains certain information
 * about a world chunk.<br/>
 * <br/>
 * <b>Every chunk data block type MUST have the {@link ChunkDataBlockID} attribute,
 * and must have a public constructor with no parameters.</b><br/>
 * <br/>
 * Please see also {@link ChunkDataIO}.
 */
public abstract class ChunkDataBlock implements ChunkDataIO
{
	// ==================================================
	/**
	 * Constructs a {@link ChunkDataBlock} using it's
	 * unique {@link Identifier}.
	 * @param cdbId {@link ChunkDataBlock} {@link Identifier}.
	 */
	@Nullable
	public static ChunkDataBlock fromId(String cdbId)
	{
		try { return ChunkData.getChunkDataBlockType(cdbId).getConstructor().newInstance(); }
		catch (Exception e) { return null; }
	}
	// ==================================================
	/**
	 * Returns this {@link ChunkDataBlock}'s {@link ChunkDataBlockID}.
	 */
	public final String getIdentifier()
	{
		ChunkDataBlockID id = getClass().getAnnotation(ChunkDataBlockID.class);
		if(id == null) return "null:null";
		return id.namespace() + ":" + id.path();
	}
	// --------------------------------------------------
	/**
	 * Writes {@link #getIdentifier()} to a byte
	 * array and then returns the byte array.
	 * @throws IOException If something goes wrong while writing the identifier to a byte array.
	 */
	public final byte[] getIdentifierByteArray() throws IOException
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		IOUtils.writeString(stream, getIdentifier());
		byte[] bytes = stream.toByteArray();
		stream.close();
		return bytes;
	}
	// ==================================================
	/**
	 * When pasting data to chunks, it is done server-side.
	 * This means that clients do not get to know what is
	 * going on while data is being pasted. To fix this issue,
	 * {@link #updateClients(ServerWorld, ChunkPos)} notifies
	 * the clients about the changes that were made.<br/>
	 * <b>Do not call this inside of {@link #pasteData(ServerWorld, ChunkPos)}<b/>
	 * @param world The world where the chunk is located.
	 * @param chunkPos The {@link ChunkPos} of the chunk in the world.
	 */
	public abstract void updateClients(ServerWorld world, ChunkPos chunkPos);
	// ==================================================
}