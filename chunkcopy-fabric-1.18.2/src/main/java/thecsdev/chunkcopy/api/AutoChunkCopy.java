package thecsdev.chunkcopy.api;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import thecsdev.chunkcopy.ChunkCopy;

/**
 * This class holds the information about the
 * status of auto-copying chunks.
 */
public class AutoChunkCopy
{
	// ==================================================
	@Nullable
	private static String FileName = null;
	// ==================================================
	/**
	 * Returns the fileName of where the chunks are
	 * currently being auto-copied to. Returns null if
	 * auto-copying is currently not running.
	 */
	@Nullable
	public static String getFileName() { validate(); return FileName; }
	
	/**
	 * Returns true if auto-copying is currently running.
	 */
	public static boolean isRunning() { return validate() && !StringUtils.isAllBlank(FileName); }
	// --------------------------------------------------
	public static boolean start(String fileName)
	{
		//validate
		if(!validate() /*|| isRunning()*/) return false;
		
		//validate fileName with regex
		if(!fileName.matches("^[\\w\\-. ]+$")) return false;
		
		//start
		ChunkCopy.LOGGER.info("Started auto-copying chunks to '" + fileName + "'.");
		FileName = fileName;
		return true;
	}
	
	public static void stop()
	{
		//check if already stopped
		if(!isRunning()) return;
		
		//stop
		ChunkCopy.LOGGER.info("Stopped auto-copying chunks.");
		FileName = null;
	}
	// ==================================================
	public static boolean validate()
	{
		if(!(ChunkCopy.validateInstance() && ChunkCopy.isClient()))
		{
			FileName = null;
			return false;
		}
		return true;
	}
	// ==================================================
}
