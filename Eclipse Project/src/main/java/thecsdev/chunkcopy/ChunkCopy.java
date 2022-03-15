package thecsdev.chunkcopy;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.EnvType;
import thecsdev.chunkcopy.client.ChunkCopyClient;
import thecsdev.chunkcopy.server.ChunkCopyServer;

/**
 * The Fabric mod-loader entry point for this mod. 
 */
public final class ChunkCopy
{
	// ==================================================
	public static final Logger LOGGER = LoggerFactory.getLogger(getModID());
	// --------------------------------------------------
	public static final String ModName = "Chunk Copy";
	public static final String ModID   = "chunkcopy";
	// --------------------------------------------------
	private static EnvType Enviroment = null;
	// ==================================================
	public static File getModDirectory()
	{
		File runDir = getEnviroment() == EnvType.CLIENT ?
				ChunkCopyClient.getClient().runDirectory :
				ChunkCopyServer.getServer().getRunDirectory();
		
		return new File(runDir.getAbsolutePath() + "/mods/" + ModID + "/");				
	}
	// --------------------------------------------------
	public static String getModID() { return ModID; }
	// --------------------------------------------------
	public static boolean traceHasChunkCopy()
	{
		String pkg = ChunkCopy.class.getPackageName();
		for (StackTraceElement ste : Thread.currentThread().getStackTrace())
		{
			if(ste.getClassName().contains(pkg))
				return true;
		}
		return false;
	}
	// --------------------------------------------------
	public static EnvType getEnviroment () { return Enviroment; }
	// --------------------------------------------------
	public static String getExceptionMessage(Throwable e)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(e.getClass().getCanonicalName() + ": " + e.getMessage() + "\n");
		for (StackTraceElement ste : e.getStackTrace())
		{
			if(!ste.getClassName().contains("thecsdev")) continue;
			sb.append(ste.toString() + "\n");
		}
		return sb.toString().trim();
	}
	// ==================================================
	/**
	 * Do not call this. Used by chunkcopy to keep
	 * track of the side it is running on.
	 */
	public static void initEnviroment(EnvType env)
	{
		//check
		if(Enviroment != null)
			throw new RuntimeException("Failed to keep track of physical side.");
		
		//init
		Enviroment = env;
	}
	// ==================================================
}
