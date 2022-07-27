package thecsdev.chunkcopy.api.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import thecsdev.chunkcopy.ChunkCopy;
import thecsdev.chunkcopy.api.data.block.CDBBlocksLegacy;
import thecsdev.chunkcopy.api.data.block.CDBEntitiesLegacy;
import thecsdev.chunkcopy.api.data.block.CDBEntityBlocksLegacy;
import thecsdev.chunkcopy.api.io.IOUtils;

/**
 * Contains copied data about a chunk.<br/>
 * Please see also {@link ChunkDataIO}.<br/>
 */
public final class ChunkData implements ChunkDataIO
{
	// ==================================================
	/**
	 * Contains all {@link ChunkDataBlock}s that store
	 * information about a chunk.
	 */
	public final HashSet<ChunkDataBlock> ChunkDataBlocks = new HashSet<>();
	// ==================================================
	@Override
	public void copyData(World world, ChunkPos chunkPos)
	{
		//clear old data
		ChunkDataBlocks.clear();
		
		//iterate all chunk data block types
		for (Class<? extends ChunkDataBlock> cdbType : ChunkDataBlockTypes)
			try
			{
				//create instance of the block type
				ChunkDataBlock cdb = cdbType.getConstructor().newInstance();
				
				//copy data and add it
				cdb.copyData(world, chunkPos);
				ChunkDataBlocks.add(cdb);
			}
			catch (Exception e) { /*TODO - HANDLE EXCEPTION*/ e.printStackTrace(); }
	}
	// --------------------------------------------------
	@Override
	public void pasteData(ServerWorld world, ChunkPos chunkPos) { pasteData(world, chunkPos, true); }
	public void pasteData(ServerWorld world, ChunkPos chunkPos, boolean updateClients)
	{
		world.getServer().execute(() ->
		{
			//iterate all chunk data blocks
			for (ChunkDataBlock chunkDataBlock : ChunkDataBlocks)
				//paste each chunk data block
				try
				{
					chunkDataBlock.pasteData(world, chunkPos);
					if(updateClients) chunkDataBlock.updateClients(world, chunkPos);
				}
				catch(Exception e) { /*TODO - HANDLE EXCEPTION*/ }
		});
	}
	// ==================================================
	@Override
	public void readData(InputStream stream) throws IOException
	{
		//clear old data
		ChunkDataBlocks.clear();
		
		//read and handle the mod id prefix
		byte[] modIdBytes = ChunkCopy.ModID.getBytes("ASCII");
		if(Arrays.compare(modIdBytes, stream.readNBytes(modIdBytes.length)) != 0)
		{
			stream.reset();
			readData_legacy(stream);
			return;
		}
		
		//read file version
		int fileVersion = IOUtils.readVarInt(stream);
		if(fileVersion != ChunkCopy.FileVersion)
		{
			throw new IOException("Unable to read and paste chunk data because it was saved using an "
					+ "incompatible file or game version. Please use that version to paste chunk data.");
		}
		
		//read chunk data blocks
		ByteArrayInputStream chunkDataStream = new ByteArrayInputStream(IOUtils.readByteArray(stream));
		while(chunkDataStream.available() > 0)
		{
			//read block length and data
			byte[] cdbBytes = IOUtils.readByteArray(chunkDataStream);
			ByteArrayInputStream cdbStream = new ByteArrayInputStream(cdbBytes);
			
			//read block id and construct it
			String cdbId = IOUtils.readString(cdbStream);
			ChunkDataBlock cdb = ChunkDataBlock.fromId(cdbId);
			if(cdb == null) { cdbStream.close(); continue; }
			
			//read data and then add it
			try { cdb.readData(cdbStream); } catch(IOException e) { /*TODO - HANDLE EXCEPTION*/ }
			ChunkDataBlocks.add(cdb);
			cdbStream.close();
		}
		chunkDataStream.close();
	}
	// -----------------
	private void readData_legacy(InputStream stream) throws IOException
	{
		//keep reading more blocks until all blocks are read
		while(stream.available() > 0)
		{
			//get chunk id and data
			int chunkId = IOUtils.readVarInt(stream);
			byte[] chunkData = IOUtils.readByteArray(stream);
			
			//define the chunk data block based on the id
			ChunkDataBlock cdb = null;
			if(chunkId == 1) cdb = new CDBBlocksLegacy();
			else if(chunkId == 2) cdb = new CDBEntityBlocksLegacy();
			else if(chunkId == 3) cdb = new CDBEntitiesLegacy();
			else continue;
			
			//read the chunk data
			ByteArrayInputStream chunkStream = new ByteArrayInputStream(chunkData);
			cdb.readData(chunkStream);
			chunkStream.close();
			
			//add the chunk data block
			ChunkDataBlocks.add(cdb);
		}
	}
	// --------------------------------------------------
	// RIFF Format: [String modIdPrefix][ChunkDataBlock[] array]
	// ChunkDataBlock RIFF Format: [VarInt length][[String id][byte[] data]]
	@Override
	public void writeData(OutputStream stream) throws IOException
	{
		//write the mod id prefix (used to spot legacy save files)
		stream.write(ChunkCopy.ModID.getBytes("ASCII"));
		
		//write file version
		IOUtils.writeVarInt(stream, ChunkCopy.FileVersion);
		
		//iterate all chunk data blocks and save each one
		ByteArrayOutputStream chunkDataStream = new ByteArrayOutputStream();
		for (ChunkDataBlock cdb : ChunkDataBlocks)
		{
			//get the identifier, and
			//get bytes of the chunk data block
			byte[] cdbIdBytes = cdb.getIdentifierByteArray();
			byte[] cdbBytes = new byte[0];
			try { cdbBytes = cdb.toByteArray(); } catch (IOException e) { /*TODO - HANDLE EXCEPTION*/ }
			
			//write the (chunk data block) data
			IOUtils.writeVarInt(chunkDataStream, cdbIdBytes.length + cdbBytes.length); //LENGTH
			chunkDataStream.write(cdbIdBytes);                                         //ID
			chunkDataStream.write(cdbBytes);                                           //DATA
		}
		IOUtils.writeByteArray(stream, chunkDataStream.toByteArray());
		chunkDataStream.close();
	}
	// ==================================================
	/**
	 * These are the registered {@link ChunkDataBlock} types
	 * that will be used to copy and paste world chunk data.
	 */
	protected static final HashSet<Class<? extends ChunkDataBlock>> ChunkDataBlockTypes = new HashSet<>();
	// --------------------------------------------------
	/**
	 * Registers a specific type of {@link ChunkDataBlock}. Once registered,
	 * the {@link ChunkDataBlock} type will be used to copy and paste world chunk data.
	 * @param type The {@link ChunkDataBlock} class to register.
	 * @return True if all conditions were met and the type was registered.
	 * See {@link ChunkDataBlock}.
	 */
	public static <T extends ChunkDataBlock> boolean registerChunkDataBlockType(Class<T> type)
	{
		//define log message
		String log = "Registering chunk data block '" + type.getSimpleName() + "': %s, %s.";
		
		//check conditions
		boolean a = type.isAnnotationPresent(ChunkDataBlockID.class);
		boolean b = IOUtils.classHasParameterlessConstructor(type);
		boolean result = a && b;
		
		if(result) ChunkDataBlockTypes.add(type);
		
		//log and return
		ChunkCopy.LOGGER.info(String.format(log, Boolean.toString(a), Boolean.toString(b)));
		return result;
	}
	
	/**
	 * Unregisters a specific type of {@link ChunkDataBlock}. Once unregistered,
	 * the {@link ChunkDataBlock} type will no longer be used to copy and paste world chunk data.
	 * @param type The {@link ChunkDataBlock} class to unregister.
	 * @return Check {@link HashSet#remove(Object)}.
	 */
	public static <T extends ChunkDataBlock> boolean unregisterChunkDataBlockType(Class<T> type)
	{
		return ChunkDataBlockTypes.remove(type);
	}
	// --------------------------------------------------
	/**
	 * Returns a registered {@link ChunkDataBlock} type by it's unique identifier.
	 * @param identifier The unique identifier of the {@link ChunkDataBlock} type.
	 */
	@Nullable
	public static Class<? extends ChunkDataBlock> getChunkDataBlockType(String identifier)
	{
		try
		{
			//get the ChunkDataBlock class
			Class<? extends ChunkDataBlock> cdbClass = ChunkData.ChunkDataBlockTypes.stream().filter(i ->
			{
				//get the ID of each block type
				ChunkDataBlockID iId = i.getAnnotation(ChunkDataBlockID.class);
				if(iId == null) return false;
				
				//only return the one that is looked for
				return (iId.namespace() + ":" + iId.path()).equals(identifier);
			})
			.findFirst().get();
			
			//return found class
			return cdbClass;
		}
		catch (Exception e) { return null; }
	}
	// ==================================================
}
