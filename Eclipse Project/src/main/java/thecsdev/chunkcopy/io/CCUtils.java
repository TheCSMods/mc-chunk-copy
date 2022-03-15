package thecsdev.chunkcopy.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.BitSet;

import org.apache.commons.io.FileUtils;

import net.fabricmc.api.EnvType;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;

import thecsdev.chunkcopy.ChunkCopy;

//RIFF chunk IDs:
//0x01 - For blocks
//0x02 - For block entities
//0x03 - For entities (too complicated to implement, i'm not gonna bother)
//
//RIFF chunk format:
//[VarInt id][VarInt data_length][byte[] data]

/**
 * Contains a set of methods that are used by both
 * the client and the server version of the mod.
 */
public class CCUtils
{
	// ==================================================
	public static final String FILE_EXTENSION = ".bin";
	// ==================================================
	/**
	 * Returns the file path to a save file.
	 * @param fileName - The name of the save file.
	 */
	public static String getSaveFileDirStr(String fileName)
	{
		return ChunkCopy.getModDirectory().getAbsolutePath() +
				"/savedChunks/" + fileName + "/";
	}
	// --------------------------------------------------
	/**
	 * Returns a list of loaded chunks around the local player
	 * in a specified world.
	 * @param world The world where the chunks are located.
	 * @param centerChunk The central point in the world.
	 * @param chunkDistance Kind of like render distance, except it defines
	 * how close the chunk has to be in order to be returned. Ranges from 0 to 8.
	 * Higher values cause lots of lag.
	 */
	public static ArrayList<Tuple<World, ChunkPos>> getLoadedChunksInRange(Tuple<World, ChunkPos> center, int chunkDistance)
	{
		World world = center.Item1;
		ChunkPos chunkPos = center.Item2;
		
		//define the resulting list
		ArrayList<Tuple<World, ChunkPos>> result = new ArrayList<>();
		if(world == null) return result;
		
		//define stuff
		if(chunkDistance < 0) chunkDistance = 0;
		else if(chunkDistance > 8) chunkDistance = 8;
		
		//add chunks
		if(chunkDistance == 1) { result.add(new Tuple<World, ChunkPos>(world, chunkPos)); }
		else if(chunkDistance > 1)
		{
			for(int chunkX = chunkPos.x - chunkDistance; chunkX < chunkPos.x + chunkDistance; chunkX++)
			{
				for(int chunkZ = chunkPos.z - chunkDistance; chunkZ < chunkPos.z + chunkDistance; chunkZ++)
				{
					//check if loaded
					if(!world.isChunkLoaded(chunkX, chunkZ)) continue;
					
					//add chunk to the list
					result.add(new Tuple<World, ChunkPos>(world, new ChunkPos(chunkX, chunkZ)));
				}
			}
		}
		
		//return the list
		return result;
	}
	// ==================================================
	/**
	 * Copies all chunk data to a byte array.
	 * @throws ChunkNotLoadedException 
	 */
	public static byte[] copyChunkData(World world, ChunkPos chunkPos) throws IOException, ChunkNotLoadedException
	{
		//define
		ByteArrayOutputStream chunkBytes = new ByteArrayOutputStream();
		Chunk chunk = world.getChunk(chunkPos.getBlockPos(0, 0, 0));
		
		//write data
		writeChunkData_blocks(chunkBytes, chunk);
		writeChunkData_blockEntities(chunkBytes, chunk);
		
		//return
		chunkBytes.close();
		return chunkBytes.toByteArray();
	}
	// --------------------------------------------------
	/** [VarInt id] [VarInt length] [(VarInt startY)(VarInt[] blocks)] */
	private static void writeChunkData_blocks(OutputStream stream, Chunk chunk) throws IOException
	{
		//gather info
		int[] blockIDs = CCChunkUtils.chunkToBlockIDs(chunk);
		
		ByteArrayOutputStream blockBytes = new ByteArrayOutputStream();
		CCStreamUtils.writeVarInt(blockBytes, chunk.getBottomY()); //write start Y
		for (int blockID : blockIDs) { CCStreamUtils.writeVarInt(blockBytes, blockID); } //write blocks
		blockBytes.close();
		
		//write
		stream.write(0x01);                                   //CHUNK ID
		CCStreamUtils.writeVarInt(stream, blockBytes.size()); //CHUNK SIZE
		stream.write(blockBytes.toByteArray());               //CHUNK DATA
	}
	// --------------------------------------------------
	/** [VarInt id] [VarInt length] [byte[] entity_block_data] */
	private static void writeChunkData_blockEntities(OutputStream stream, Chunk chunk) throws IOException
	{
		//gather info
		byte[] blockBytes = CCChunkUtils.chunkEntityBlocksToBytes(chunk);
		
		//write
		stream.write(0x02);                                   //CHUNK ID
		CCStreamUtils.writeVarInt(stream, blockBytes.length); //CHUNK SIZE
		stream.write(blockBytes);                             //CHUNK DATA
	}
	// --------------------------------------------------
	/** [VarInt id] [VarInt length] [byte[] entity_data] */
	private static void writeChunkData_entities(OutputStream stream, World world, ChunkPos chunkPos)
	throws IOException, ChunkNotLoadedException
	{
		//gather info
		byte[] entityBytes = CCChunkUtils.chunkEntitiesToBytes(world, chunkPos);
		
		//write
		stream.write(0x03);                                    //CHUNK ID
		CCStreamUtils.writeVarInt(stream, entityBytes.length); //CHUNK SIZE
		stream.write(entityBytes);                             //CHUNK DATA
	}
	// ==================================================
	/**
	 * Pastes all chunk data from a byte array to a chunk.
	 * @throws ChunkNotLoadedException 
	 */
	public static void pasteChunkData(World world, ChunkPos chunkPos, byte[] chunkData)
	throws IOException, ChunkNotLoadedException
	{
		ByteArrayInputStream chunkBytesIn = new ByteArrayInputStream(chunkData);
		while(chunkBytesIn.available() > 1) readChunkDataBlock(world, chunkPos, chunkBytesIn);
		chunkBytesIn.close();
	}
	// --------------------------------------------------
	private static void readChunkDataBlock(World world, ChunkPos chunkPos, InputStream stream)
	throws IOException, ChunkNotLoadedException
	{
		//read riff chunk id
		int riffChunkID = stream.read();
		if(riffChunkID < 0) return;
		
		//depending on the id of the next riff chunk, read Minecraft world chunk data
		//ChunkCopy.printChat("Reading chunk ID: " + riffChunkID  + ", remaining: " + stream.available());
		if(riffChunkID == 0x01) readChunkDataBlock_blocks(world, chunkPos, stream);
		else if(riffChunkID == 0x02) readChunkDataBlock_blockEntities(world, chunkPos, stream);
		else readChunkDataBlock_skipBlock(stream);
	}
	// --------------------------------------------------
	private static void readChunkDataBlock_skipBlock(InputStream stream) throws IOException
	{
		//get size (length)
		int riffChunkLength = CCStreamUtils.readVarInt(stream);
		if(riffChunkLength < 1) { return; }
		
		//skip bytes
		stream.skipNBytes(riffChunkLength);
	}
	// --------------------------------------------------
	private static void readChunkDataBlock_blocks(World world, ChunkPos chunkPos, InputStream stream)
	throws IOException, ChunkNotLoadedException
	{
		//get size (length)
		int riffChunkLength = CCStreamUtils.readVarInt(stream);
		if(riffChunkLength < 1) { return; }
		
		//get raw bytes
		byte[] rawChunkBytes = stream.readNBytes(riffChunkLength);
		
		//convert raw bytes to integers
		ByteArrayInputStream rawChunkBytesIn = new ByteArrayInputStream(rawChunkBytes);
		int startY = CCStreamUtils.readVarInt(rawChunkBytesIn); //read start Y
		ArrayList<Integer> blockIDs = new ArrayList<Integer>(); //ˇ read blocks
		while(rawChunkBytesIn.available() > 0) blockIDs.add(CCStreamUtils.readVarInt(rawChunkBytesIn));
		rawChunkBytesIn.close();
		
		//load integers
		int[] blockIDsArr = blockIDs.stream().mapToInt(i->i).toArray();
		CCChunkUtils.blockIDsToChunk(blockIDsArr, world, chunkPos, startY);
	}
	// --------------------------------------------------
	private static void readChunkDataBlock_blockEntities(World world, ChunkPos chunkPos, InputStream stream)
	throws IOException, ChunkNotLoadedException
	{
		//get size (length)
		int riffChunkLength = CCStreamUtils.readVarInt(stream);
		if(riffChunkLength < 1) { return; }
		
		//get bytes
		byte[] chunkBytes = stream.readNBytes(riffChunkLength);
		
		//load bytes
		CCChunkUtils.bytesToChunkEntityBlocks(chunkBytes, world, chunkPos);
	}
	// ==================================================
	/**
	 * Saves currently loaded chunks to a save file.
	 * @param fileName The name of the directory where the chunk data will be stored to.
	 * @param chunkDistance Kind of like render distance, except it defines
	 * how close the chunk has to be in order to be saved. Ranges from 0 to 8.
	 * Higher values cause lots of lag.
	 * @throws IOException 
	 */
	public static void saveLoadedChunksIO(String fileName, int chunkDistance, Tuple<World, ChunkPos> center) throws IOException
	{
		//iterate and save chunks
		ArrayList<Tuple<World, ChunkPos>> chunks = getLoadedChunksInRange(center, chunkDistance);
		for (Tuple<World, ChunkPos> chunk : chunks)
		{
			saveLoadedChunkIO(chunk, fileName);
		}
	}
	//---------------------------------------------------
	/**
	 * Saves a single chunk to a save file. The written bytes are compressed.
	 * @param world The world where the chunk is located.
	 * @param chunkPos The position of the chunk in the world.
	 * @param fileName The name of the directory where the chunk data will be stored to.
	 * @throws IOException If this method fails to write chunk data to the save file
	 * or if the chunk is currently unloaded.
	 */
	public static boolean saveLoadedChunkIO(Tuple<World, ChunkPos> chunk, String fileName) throws IOException
	{
		World world = chunk.Item1;
		ChunkPos chunkPos = chunk.Item2;
		
		//check if loaded
		if(!world.isChunkLoaded(chunkPos.x, chunkPos.z))
			return false;
		
		//get chunk file path
		String worldIdNamespace = world.getRegistryKey().getValue().getNamespace();
		String worldIdPath = world.getRegistryKey().getValue().getPath();
		
		String chunkFileStr = getSaveFileDirStr(fileName) +
				"/" + worldIdNamespace + "/" + worldIdPath + "/" +
				chunkPos.x + "_" + chunkPos.z + FILE_EXTENSION;
		File chunkFile = new File(chunkFileStr);
		
		//copy chunk data to the file
		try
		{
			chunkFile.getParentFile().mkdirs();
			byte[] chunkBytes = CCStreamUtils.gzipCompressBytes(copyChunkData(world, chunkPos));
			FileUtils.writeByteArrayToFile(chunkFile, chunkBytes);
		}
		catch (ChunkNotLoadedException e) { return false; }
		
		//return
		return true;
	}
	// ==================================================
	/**
	 * Loads chunk data to currently loaded chunks from their save files.
	 * @param fileName The name of the directory where the chunk data will be stored to.
	 * @param chunkDistance Kind of like render distance, except it defines
	 * how close the chunk has to be in order to be loaded. Ranges from 0 to 8.
	 * Higher values cause lots of lag.
	 */
	public static boolean loadLoadedChunksIO(String fileName, int chunkDistance, Tuple<World, ChunkPos> center) throws IOException
	{
		//check if the save file exists
		if(!new File(getSaveFileDirStr(fileName)).exists())
		{
			thecsdev.chunkcopy.client.ChunkCopyClient.printChat(new TranslatableText("thecsdev.chunkcopy.pastefilenotfound")
					.getString().replace("{$fileName}", fileName));
			return false;
		}
		
		//load chunks
		ArrayList<Tuple<World, ChunkPos>> chunks = getLoadedChunksInRange(center, chunkDistance);
		for (Tuple<World, ChunkPos> chunk : chunks)
		{
			loadLoadedChunkIO(fileName, chunk);
		}
		
		//reload renderer client-side to avoid some blocks not being rendered
		refreshClientSide(center, chunkDistance);
		refreshServerSide(center, chunkDistance);
		
		//return
		return true;
	}
	// --------------------------------------------------
	/**
	 * Loads chunk data to a single currently loaded chunk from it's save file.
	 * @param fileName The name of the directory where the chunk data will be stored to.
	 * @param world The world in which the chunk is located.
	 * @param chunkPos The position of the chunk in the world.
	 */
	public static boolean loadLoadedChunkIO(String fileName, Tuple<World, ChunkPos> chunk) throws IOException
	{
		World world = chunk.Item1;
		ChunkPos chunkPos = chunk.Item2;
		
		//check if loaded
		if(!world.isChunkLoaded(chunkPos.x, chunkPos.z))
			return false;
		
		//get chunk file path
		String worldIdNamespace = world.getRegistryKey().getValue().getNamespace();
		String worldIdPath = world.getRegistryKey().getValue().getPath();
		
		String chunkFileStr = getSaveFileDirStr(fileName) +
				"/" + worldIdNamespace + "/" + worldIdPath + "/" +
				chunkPos.x + "_" + chunkPos.z + FILE_EXTENSION;
		File chunkFile = new File(chunkFileStr);
		if(!chunkFile.exists()) return false;
		
		//read chunk data from the file
		byte[] chunkBytes = CCStreamUtils.gzipDecompressBytes(FileUtils.readFileToByteArray(chunkFile));
		
		//paste chunk data
		try { pasteChunkData(world, chunkPos, chunkBytes); }
		catch (ChunkNotLoadedException e) { return false; }
		
		//return
		return true;
	}
	// ==================================================
	/**
	 * Fills all blocks in currently loaded chunks with the specified block state.
	 * @param chunkDistance Kind of like render distance, except it defines
	 * how close the chunk has to be in order to be filled. Ranges from 0 to 8.
	 * Higher values cause lots of lag.
	 * @param state The block state to fill the chunks with.
	 */
	public static boolean fillLoadedChunks(int chunkDistance, BlockState state, Tuple<World, ChunkPos> center)
	{
		//clear chunks
		ArrayList<Tuple<World, ChunkPos>> chunks = getLoadedChunksInRange(center, chunkDistance);
		for (Tuple<World, ChunkPos> chunk : chunks)
		{
			fillLoadedChunk(chunk.Item1, chunk.Item2, state);
		}
		
		//reload renderer to avoid some blocks not being rendered
		refreshClientSide(center, chunkDistance);
		refreshServerSide(center, chunkDistance);
		
		//return
		return true;
	}
	// --------------------------------------------------
	/**
	 * Fills all blocks in a currently loaded chunk with the specified block state.
	 * @param world The world in which the chunk is located.
	 * @param chunkPos The position of the chunk in the world.
	 * @param state The block state to fill the chunk with.
	 */
	public static boolean fillLoadedChunk(World world, ChunkPos chunkPos, BlockState state)
	{		
		//clear
		try { CCChunkUtils.fillChunkBlocks(world, chunkPos, state); }
		catch (ChunkNotLoadedException e) { return false; }
		
		//return
		return true;
	}
	// ==================================================
	private static void refreshClientSide(Tuple<World, ChunkPos> center, int chunkDistance)
	{
		//--- I avoided importing client and server side packages just in case. ---
		//check env.
		if(ChunkCopy.getEnviroment() != EnvType.CLIENT) return;
		net.minecraft.client.MinecraftClient MC = thecsdev.chunkcopy.client.ChunkCopyClient.getClient();
		if(!MC.isInSingleplayer()) return;
		
		//iterate chunks
		for(Tuple<World, ChunkPos> chunk : getLoadedChunksInRange(center, chunkDistance))
		{
			ChunkDataS2CPacket cd = makeMeAChunkDataPacketPls(chunk);
			MC.getServer().getPlayerManager().sendToAll(cd);
		}
		
		//reload renderer
		if(MC.worldRenderer != null) MC.worldRenderer.reload();
	}
	// --------------------------------------------------
	private static void refreshServerSide(Tuple<World, ChunkPos> center, int chunkDistance)
	{
		//--- I avoided importing client and server side packages just in case. ---
		//check env.
		if(ChunkCopy.getEnviroment() != EnvType.SERVER) return;
		
		//define stuff
		net.minecraft.server.MinecraftServer srv = thecsdev.chunkcopy.server.ChunkCopyServer.getServer();
		net.minecraft.server.world.ServerWorld srvW = srv.getWorld(center.Item1.getRegistryKey());
		
		//refresh players
		for(Tuple<World, ChunkPos> chunk : getLoadedChunksInRange(center, chunkDistance))
		{
			ChunkDataS2CPacket chunkData = makeMeAChunkDataPacketPls(chunk);
			srvW.getPlayers().forEach(p -> p.networkHandler.sendPacket(chunkData));
		}
	}
	// --------------------------------------------------
	//i didn't know what else to name this method so whatever
	private static ChunkDataS2CPacket makeMeAChunkDataPacketPls(Tuple<World, ChunkPos> chunk)
	{
		WorldChunk wchunk = chunk.Item1.getChunk(chunk.Item2.x, chunk.Item2.z);
		LightingProvider lp = chunk.Item1.getLightingProvider();
		BitSet skyBits = new BitSet(0);
		BitSet blockBits = new BitSet(0);
		return new ChunkDataS2CPacket(wchunk, lp, skyBits, blockBits, true);
	}
	// ==================================================
}
