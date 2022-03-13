package thecsdev.chunkcopy.io;

import java.io.File;
import java.util.ArrayList;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.commons.io.FileUtils;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.text.TranslatableText;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;

import thecsdev.chunkcopy.ChunkCopy;
import thecsdev.chunkcopy.ChunkNotLoadedException;
import thecsdev.chunkcopy.Tuple;

//RIFF chunk IDs:
//0x01 - For blocks
//0x02 - For block entities
//0x03 - For entities (too complicated to implement, i'm not gonna bother)
//
//RIFF chunk format:
//[VarInt id][VarInt data_length][byte[] data]
/**
 * Provides utility methods for the {@link ChunkCopy} mod.
 */
public final class CCUtils
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
	 * @param chunkDistance Kind of like render distance, except it defines
	 * how close the chunk has to be in order to be returned. Ranges from 0 to 8.
	 * Higher values cause lots of lag.
	 */
	public static ArrayList<Tuple<World, ChunkPos>> getLoadedChunksInRange(World world, int chunkDistance)
	{
		//define the resulting list
		ArrayList<Tuple<World, ChunkPos>> result = new ArrayList<>();
		if(world == null) return result;
		
		//define stuff
		MinecraftClient MC = ChunkCopy.MC;
		int rndDist = (int)MC.worldRenderer.getViewDistance() / 2; //slice in half cuz we're using radius
		int minDist = 0;
		int maxDist = Math.min(rndDist, 8);
		if(chunkDistance < minDist) chunkDistance = minDist;
		else if(chunkDistance > maxDist) chunkDistance = maxDist;
		
		Chunk playerChunk = world.getWorldChunk(MC.player.getChunkPos().getBlockPos(0, 0, 0));
		
		//add chunks
		if(chunkDistance < 1) { /*nothing*/ }
		else if(chunkDistance == 1) { result.add(new Tuple<World, ChunkPos>(world, playerChunk.getPos())); }
		else if(chunkDistance > 1)
		{
			for(int chunkX = playerChunk.getPos().x - chunkDistance; chunkX < playerChunk.getPos().x + chunkDistance; chunkX++)
			{
				for(int chunkZ = playerChunk.getPos().z - chunkDistance; chunkZ < playerChunk.getPos().z + chunkDistance; chunkZ++)
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
	 */
	public static byte[] copyChunkData(Chunk chunk) throws IOException
	{
		//define
		ByteArrayOutputStream chunkBytes = new ByteArrayOutputStream();
		
		//write
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
		else throw new IOException("Unexpected byte: " + riffChunkID);
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
	public static void saveLoadedChunksIO(String fileName, int chunkDistance) throws IOException
	{
		//define stuff
		MinecraftClient MC = ChunkCopy.MC;
		Chunk playerChunk = MC.world.getWorldChunk(MC.player.getChunkPos().getBlockPos(0, 0, 0));
		
		int rndDist = (int)MC.worldRenderer.getViewDistance() / 2; //slice in half cuz we're using radius
		int minDist = 0;
		int maxDist = Math.min(rndDist, 8);
		if(chunkDistance < minDist) chunkDistance = minDist;
		else if(chunkDistance > maxDist) chunkDistance = maxDist;
		
		//iterate and save chunks
		if(chunkDistance > 1)
		{
			for(int chunkX = playerChunk.getPos().x - rndDist; chunkX < playerChunk.getPos().x + rndDist; chunkX++)
			{
				for(int chunkZ = playerChunk.getPos().z - rndDist; chunkZ < playerChunk.getPos().z + rndDist; chunkZ++)
				{
					//check if loaded
					if(!MC.world.isChunkLoaded(chunkX, chunkZ)) continue;
					
					//copy chunk
					saveLoadedChunkIO(MC.world, new ChunkPos(chunkX, chunkZ), fileName);
				}
			}
		}
		else saveLoadedChunkIO(MC.world, playerChunk.getPos(), fileName);
		
		//feedback
		ChunkCopy.printChat(new TranslatableText("thecsdev.chunkcopy.copiedchunks")
				.getString().replace("{$fileName}", fileName));
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
	public static void saveLoadedChunkIO(World world, ChunkPos chunkPos, String fileName) throws IOException
	{
		//check if loaded
		if(!world.isChunkLoaded(chunkPos.x, chunkPos.z))
			return;
		
		//get chunk
		Chunk chunk = world.getChunk(chunkPos.getBlockPos(0, 0, 0));
		
		//get chunk file path
		String worldIdNamespace = world.getRegistryKey().getValue().getNamespace();
		String worldIdPath = world.getRegistryKey().getValue().getPath();
		
		String chunkFileStr = getSaveFileDirStr(fileName) +
				"/" + worldIdNamespace + "/" + worldIdPath + "/" +
				chunkPos.x + "_" + chunkPos.z + FILE_EXTENSION;
		File chunkFile = new File(chunkFileStr);
		
		//copy chunk data to the file
		chunkFile.getParentFile().mkdirs();
		FileUtils.writeByteArrayToFile(chunkFile, CCStreamUtils.gzipCompressBytes(copyChunkData(chunk)));
	}
	// ==================================================
	/**
	 * Loads chunk data to currently loaded chunks from their save files.
	 * @param fileName The name of the directory where the chunk data will be stored to.
	 * @param chunkDistance Kind of like render distance, except it defines
	 * how close the chunk has to be in order to be loaded. Ranges from 0 to 8.
	 * Higher values cause lots of lag.
	 */
	public static boolean loadLoadedChunksIO(String fileName, int chunkDistance) throws IOException
	{
		//check if singleplayer
		MinecraftClient MC = ChunkCopy.MC;
		if(!MC.isInSingleplayer())
		{
			ChunkCopy.printChatT("thecsdev.chunkcopy.requiresingleplayer");
			return false;
		}
		
		//check if the save file exists
		else if(!new File(getSaveFileDirStr(fileName)).exists())
		{
			ChunkCopy.printChat(new TranslatableText("thecsdev.chunkcopy.pastefilenotfound")
					.getString().replace("{$fileName}", fileName));
			return false;
		}
		
		//load chunks
		World world = MC.getServer().getWorld(MC.world.getRegistryKey());
		ArrayList<Tuple<World, ChunkPos>> chunks = getLoadedChunksInRange(world, chunkDistance);
		for (Tuple<World, ChunkPos> chunk : chunks)
		{
			loadLoadedChunkIO(fileName, chunk.Item1, chunk.Item2);
		}
		
		//feedback
		ChunkCopy.printChat(new TranslatableText("thecsdev.chunkcopy.pastedchunks")
				.getString().replace("{$fileName}", fileName));
		
		//reload renderer to avoid some blocks not being rendered
		if(MC.worldRenderer != null) MC.worldRenderer.reload();
		
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
	public static boolean loadLoadedChunkIO(String fileName, World world, ChunkPos chunkPos) throws IOException
	{
		//check if loaded
		if(!world.isChunkLoaded(chunkPos.x, chunkPos.z))
			return false;
		
		//check
		MinecraftClient MC = ChunkCopy.MC;
		if(!MC.isInSingleplayer()) { return false; }
		
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
		
		//send chunk data packet
		CCChunkUtils.sendChunkDataS2CPacket(world, chunkPos);
		
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
	public static boolean fillLoadedChunks(int chunkDistance, BlockState state)
	{
		//check if singleplayer
		MinecraftClient MC = ChunkCopy.MC;
		if(!MC.isInSingleplayer())
		{
			ChunkCopy.printChatT("thecsdev.chunkcopy.requiresingleplayer");
			return false;
		}
		
		//clear chunks
		World world = MC.getServer().getWorld(MC.world.getRegistryKey());
		ArrayList<Tuple<World, ChunkPos>> chunks = getLoadedChunksInRange(world, chunkDistance);
		for (Tuple<World, ChunkPos> chunk : chunks)
		{
			fillLoadedChunk(chunk.Item1, chunk.Item2, state);
		}
		
		//feedback
		ChunkCopy.printChat(new TranslatableText("thecsdev.chunkcopy.filledchunks")
				.getString().replace("{$blockName}", state.getBlock().getName().getString()));
		
		//reload renderer to avoid some blocks not being rendered
		if(MC.worldRenderer != null) MC.worldRenderer.reload();
		
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
		
		//send chunk data packet
		CCChunkUtils.sendChunkDataS2CPacket(world, chunkPos);
		
		//return
		return true;
	}
	// ==================================================
}
