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
	public enum ACCMode { Copying, Pasting }
	
	@Nullable
	private static String FileName = null;
	private static ACCMode AutoChunkCopyMode = null;
	// ==================================================
	/**
	 * Returns the fileName of where the chunks are
	 * currently being auto copied or pasted. Returns null if
	 * {@link AutoChunkCopy} is currently not running.
	 */
	@Nullable
	public static String getFileName() { return AutoChunkCopyMode != null ? FileName : null; }
	
	/**
	 * Returns true if auto-copying is currently running.
	 */
	private static boolean isRunning() { return validate() && AutoChunkCopyMode != null && !StringUtils.isAllBlank(FileName); }
	public static boolean isCopying() { return isRunning() && AutoChunkCopyMode == ACCMode.Copying; }
	public static boolean isPasting() { return isRunning() && AutoChunkCopyMode == ACCMode.Pasting; }
	// --------------------------------------------------
	public static boolean start(String fileName, ACCMode mode)
	{
		//validate instance
		if(!validate()) return false;
		
		//validate fileName with regex
		if(!fileName.matches("^[\\w\\-. ]+$")) return false;
		
		//start
		ChunkCopy.LOGGER.info("Started AutoChunkCopy; Mode: '" + mode.name() + "'; File: '" + fileName + "';");
		AutoChunkCopyMode = mode;
		FileName = fileName;
		return true;
	}
	
	public static void stop()
	{
		//log if running
		if(isRunning())
			ChunkCopy.LOGGER.info("Stopped AutoChunkCopy; Mode: '" + AutoChunkCopyMode.name() + "'; File: '" + FileName + "';");
		
		//stop
		AutoChunkCopyMode = null;
		FileName = null;
	}
	// ==================================================
	public static boolean validate()
	{
		if(!ChunkCopy.validateInstance() || !ChunkCopy.isClient())
		{
			FileName = null;
			return false;
		}
		return true;
	}
	// ==================================================
}
