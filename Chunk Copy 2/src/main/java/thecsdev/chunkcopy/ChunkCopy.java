package thecsdev.chunkcopy;

import java.io.File;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.EnvType;
import thecsdev.chunkcopy.api.config.ChunkCopyConfig;
import thecsdev.chunkcopy.api.data.ChunkData;
import thecsdev.chunkcopy.api.data.block.CDBChunkSections;
import thecsdev.chunkcopy.api.data.block.CDBEntitiesLegacy;
import thecsdev.chunkcopy.api.data.block.CDBEntityBlocksLegacy;
import thecsdev.chunkcopy.client.ChunkCopyClient;
import thecsdev.chunkcopy.command.ChunkCopyCommand;
import thecsdev.chunkcopy.server.ChunkCopyServer;

/**
 * The main ChunkCopy class. This class
 * contains vital information about the mod.
 */
public abstract class ChunkCopy
{
	// ==================================================
	private static ChunkCopy Instance = null;
	// --------------------------------------------------
	public static final Logger LOGGER = LoggerFactory.getLogger(getModID());
	// --------------------------------------------------
	public static final String ModName     = "Chunk Copy";
	public static final String ModID       = "chunkcopy";
	public static final int    FileVersion = 757;
	// ==================================================
	public ChunkCopy()
	{
		//check if there already is an instance
		if(validateInstance())
			throw new RuntimeException("An instance of ChunkCopy already exists.");
		
		//define Instance
		Instance = this;
		
		//log stuff
		LOGGER.info("Initializing '" + getModName() + "' as '" + getClass().getSimpleName() + "'.");
		
		//register config keys and load the config -- work in progress
		ChunkCopyConfig.KEYS.add(ChunkCopyConfig.PASTE_ENTITIES);
		//ChunkCopyConfig.loadConfig();
		
		//handle API registrations
		ChunkData.registerChunkDataBlockType(CDBChunkSections.class);
		ChunkData.registerChunkDataBlockType(CDBEntityBlocksLegacy.class);
		ChunkData.registerChunkDataBlockType(CDBEntitiesLegacy.class);
	}
	// --------------------------------------------------
	public static String getModName() { return ModName; }
	public static String getModID() { return ModID; }
	@Nullable public static ChunkCopy getInstance() { return Instance; }
	// --------------------------------------------------
	/**
	 * Returns true if the {@link #Instance} is valid. This
	 * should always return true. If it doesn't, the mod
	 * probably hasn't been initialized yet.
	 */
	public static boolean validateInstance()
	{
		if(Instance != null && (Instance instanceof ChunkCopyClient || Instance instanceof ChunkCopyServer))
			return true;
		else return false;
	}
	// --------------------------------------------------
	/**
	 * Returns the currently running {@link EnvType}.
	 * @throws RuntimeException If the mod hasn't been initialized yet.
	 */
	public static EnvType getEnviroment()
	{
		//check if initialized
		if(!validateInstance())
			throw new RuntimeException("Uninitialized mod.");
		
		//return
		if(Instance instanceof ChunkCopyClient) return EnvType.CLIENT;
		else if(Instance instanceof ChunkCopyServer) return EnvType.SERVER;
		else throw new RuntimeException("If you are reading this, something went terribly wrong.");
	}
	// --------------------------------------------------
	/**
	 * Returns the directory where this mod
	 * saves and loads it's data.
	 */
	public static File getModSavesDirectory()
	{
		File runDir = getRunDirectory();
		return new File(runDir.getAbsolutePath() + "/mods/" + ModID + "/");				
	}
	// --------------------------------------------------
	/**
	 * Returns the directory where Minecraft
	 * is currently running.
	 */
	public static File getRunDirectory() { return new File(System.getProperty("user.dir")); }
	// ==================================================
	/**
	 * Returns the registered {@link ChunkCopyCommand}.
	 * Will return null if the mod hasn't been initialized yet.
	 */
	@Nullable
	public abstract ChunkCopyCommand<?> getCommand();
	// ==================================================
}
