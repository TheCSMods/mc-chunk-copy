package thecsdev.chunkcopy.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.WorldChunk;
import thecsdev.chunkcopy.ChunkCopy;
import thecsdev.chunkcopy.api.data.ChunkData;
import thecsdev.chunkcopy.api.data.block.CDBFillBlocks;
import thecsdev.chunkcopy.api.io.IOUtils;

/**
 * The main API implementation for the {@link ChunkCopy} mod.<br/>
 */
public final class ChunkCopyAPI
{
	// ==================================================
	public static final String FILE_EXTENSION = ".bin";
	// ==================================================
	/**
	 * Returns the directory where all copied
	 * chunk save files are stored.
	 */
	public static File getSaveFilesDirectory()
	{
		return new File(ChunkCopy.getModSavesDirectory().getAbsolutePath() + "/savedChunks/");
	}
	
	/**
	 * Returns the directory path to a save file.
	 * @param fileName - The name of the save file.
	 */
	public static File getSaveFileDirectory(String fileName)
	{
		fileName = fileName.replaceFirst("\\/.*", ""); //discard alternate pattern
		return new File(getSaveFilesDirectory().getAbsolutePath() + "/" + fileName + "/");
	}
	// --------------------------------------------------
	/**
	 * Returns the save file for a specific world chunk.
	 * @throws IOException For invalid fileName syntax.
	 * @param world The world where the chunk is located.
	 * @param chunkPos The {@link ChunkPos} of the chunk in the world.
	 * @param fileName The directory where the chunk is or will be saved to.
	 */
	public static File getChunkSaveFile(World world, ChunkPos chunkPos, String fileName) throws IOException
	{
		//get world id
		String worldIdNamespace = null;
		String worldIdPath = null;
		
		//regex syntax check
		IOException e = new IOException("Invalid fileName syntax.");
		if(!fileName.matches("[a-zA-Z0-9_\\/]*")) throw e; //handle regular fileName
		
		if(fileName.contains("/")) //handle custom dimension definition
		{
			//regex check
			if(!fileName.matches("[a-zA-Z0-9_]{1,}\\/[a-zA-Z0-9_]{1,}\\/[a-zA-Z0-9_]{1,}")) throw e;
			
			//split and assign values
			String[] fnS = fileName.split("\\/");
			worldIdNamespace = fnS[1];
			worldIdPath = fnS[2];
		}
		else
		{
			worldIdNamespace = world.getRegistryKey().getValue().getNamespace();
			worldIdPath = world.getRegistryKey().getValue().getPath();
		}
		
		//construct path
		String a = getSaveFileDirectory(fileName).getAbsolutePath() + "/";
		String b = worldIdNamespace + "/" + worldIdPath + "/";
		String c = chunkPos.x + "_" + chunkPos.z + FILE_EXTENSION;
		String chunkFileStr = a + b + c;
		
		//return file
		return new File(chunkFileStr);
	}
	// ==================================================
	/**
	 * Copies chunk data using {@link #copyChunkData(World, ChunkPos, boolean)}
	 * and then saves it to it's save file.
	 * @param world The world where the chunk is located.
	 * @param chunkPos The {@link ChunkPos} of the chunk in the world.
	 * @param fileName The directory where the chunk data will be saved to.
	 * @throws IOException If an {@link IOException} occurs while saving.
	 */
	public static void saveChunkDataIO(World world, ChunkPos chunkPos, String fileName) throws IOException
	{
		//make sure the chunk isn't null or empty
		WorldChunk chunk = world.getChunk(chunkPos.x, chunkPos.z);
		if(chunk == null || (chunk instanceof EmptyChunk)) return;
		
		//prepare the file
		File file = getChunkSaveFile(world, chunkPos, fileName);
		file.getParentFile().mkdirs();
		
		//write
		byte[] chunkData = copyChunkData(world, chunkPos, true);
		FileUtils.writeByteArrayToFile(file, chunkData);
	}
	// --------------------------------------------------
	/**
	 * Loads chunk data from a chunk's save file and then pastes it
	 * using {@link #pasteChunkData(byte[], ServerWorld, ChunkPos, boolean)}.
	 * @param world The world where the chunk is located.
	 * @param chunkPos The {@link ChunkPos} of the chunk in the world.
	 * @param fileName The directory where the chunk data will be loaded from.
	 * @return True if the chunk save file exists and was loaded.
	 * @throws IOException If an {@link IOException} occurs while loading.
	 */
	public static boolean loadChunkDataIO(ServerWorld world, ChunkPos chunkPos, String fileName) throws IOException
	{
		return loadChunkDataIO(world, chunkPos, fileName, true);
	}
	
	/**
	 * Same as {@link #loadChunkDataIO(ServerWorld, ChunkPos, String)}.
	 * @param updateClients Should the {@link ServerWorld} clients be updated on the pasted changes that were made?
	 */
	public static boolean loadChunkDataIO(ServerWorld world, ChunkPos chunkPos, String fileName, boolean updateClients) throws IOException
	{
		//make sure the chunk isn't null or empty
		WorldChunk chunk = world.getChunk(chunkPos.x, chunkPos.z);
		if(chunk == null || (chunk instanceof EmptyChunk)) return false;
		
		//get the file
		File file = getChunkSaveFile(world, chunkPos, fileName);
		if(!file.exists()) return false;
		
		//read
		byte[] chunkData = FileUtils.readFileToByteArray(file);
		pasteChunkData(chunkData, world, chunkPos, updateClients);
		return true;
	}
	// ==================================================
	/**
	 * Copies all chunk data from a world chunk to {@link ChunkData}.
	 * @param world The world where the chunk is located.
	 * @param chunkPos The {@link ChunkPos} of the chunk in the world.
	 * @param useCompression Whether or not to compress the bytes using gZip compression.
	 * @throws IOException If an IOException occurs while writing data to the byte array.
	 */
	public static byte[] copyChunkData(World world, ChunkPos chunkPos, boolean useCompression)
	throws IOException
	{
		ChunkData chunkData = new ChunkData();
		chunkData.copyData(world, chunkPos);
		
		byte[] bytes = chunkData.toByteArray();
		if(useCompression) bytes = IOUtils.gzipCompressBytes(bytes);
		return bytes;
	}
	// --------------------------------------------------
	/**
	 * Pastes all chunk data from {@link ChunkData} to a world chunk.
	 * @param data The chunk data to paste.
	 * @param world The world where the chunk is located.
	 * @param chunkPos The {@link ChunkPos} of the chunk in the world.
	 * @param isCompressed Whether or not the bytes were compressed using gZip compression.
	 * @throws IOException If an IOException occurs while reading data from the byte array.
	 */
	public static void pasteChunkData(byte[] data, ServerWorld world, ChunkPos chunkPos, boolean isCompressed) throws IOException
	{
		pasteChunkData(data, world, chunkPos, isCompressed, true);
	}
	
	/**
	 * Same as {@link #pasteChunkData(byte[], ServerWorld, ChunkPos, boolean)}.
	 * @param updateClients Should the {@link ServerWorld} clients be updated on the pasted changes that were made?
	 */
	public static void pasteChunkData(byte[] data, ServerWorld world, ChunkPos chunkPos, boolean isCompressed, boolean updateClients) throws IOException
	{
		if(isCompressed) data = IOUtils.gzipDecompressBytes(data);
		
		ByteArrayInputStream stream = new ByteArrayInputStream(data);
		ChunkData chunkData = new ChunkData();
		chunkData.readData(stream);
		stream.close();
		chunkData.pasteData(world, chunkPos, updateClients);
	}
	// --------------------------------------------------
	/**
	 * Fills an entire world chunk with a specific {@link BlockState}.
	 */
	public static void fillChunkBlocks(ServerWorld world, ChunkPos chunkPos, BlockState block)
	{
		CDBFillBlocks f = new CDBFillBlocks();
		f.state = block;
		f.pasteData(world, chunkPos);
		f.updateClients(world, chunkPos);
	}
	
	/**
	 * Same as {@link #fillChunkBlocks(ServerWorld, ChunkPos, BlockState)},
	 * but the {@link BlockState} is {@link Blocks#AIR}.
	 */
	public static void clearChunkBlocks(ServerWorld world, ChunkPos chunkPos)
	{
		fillChunkBlocks(world, chunkPos, Blocks.AIR.getDefaultState());
	}
	// ==================================================
}
 