package thecsdev.chunkcopy.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
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
	 * Returns the file path to a save file.
	 * @param fileName - The name of the save file.
	 */
	public static File getSaveFileDirectory(String fileName)
	{
		return new File(ChunkCopy.getModSavesDirectory().getAbsolutePath() +
				"/savedChunks/" + fileName + "/");
	}
	// --------------------------------------------------
	/**
	 * Returns the save file for a specific world chunk.
	 */
	public static File getChunkSaveFile(World world, ChunkPos chunkPos, String fileName)
	{
		//get world id
		String worldIdNamespace = world.getRegistryKey().getValue().getNamespace();
		String worldIdPath = world.getRegistryKey().getValue().getPath();
		
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
		//get the file
		File file = getChunkSaveFile(world, chunkPos, fileName);
		if(!file.exists()) return false;
		
		//read
		byte[] chunkData = FileUtils.readFileToByteArray(getChunkSaveFile(world, chunkPos, fileName));
		pasteChunkData(chunkData, world, chunkPos, true);
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
	public static void pasteChunkData(byte[] data, ServerWorld world, ChunkPos chunkPos, boolean isCompressed)
	throws IOException
	{
		if(isCompressed) data = IOUtils.gzipDecompressBytes(data);
		
		ByteArrayInputStream stream = new ByteArrayInputStream(data);
		ChunkData chunkData = new ChunkData();
		chunkData.readData(stream);
		stream.close();
		chunkData.pasteData(world, chunkPos);
	}
	// --------------------------------------------------
	/**
	 * Fills an entire world chunk with a specific {@link BlockState}.
	 */
	public static void fillChunkBlocks(ServerWorld world, ChunkPos chunkPos, BlockState block)
	{
		CDBFillBlocks f = new CDBFillBlocks();
		f.BlockID = Block.getRawIdFromState(block);
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
 