package thecsdev.chunkcopy.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.math.NumberUtils;

import net.minecraft.util.Identifier;
import thecsdev.chunkcopy.ChunkCopy;

/**
 * Mod config for {@link ChunkCopy}.<br/>
 * <b>Notice: Still under construction! Will be available after v2.0.0<b/>
 */
public final class ChunkCopyConfig
{
	// ==================================================
	/**
	 * Stores the {@link Properties} for the {@link ChunkCopy} mod.
	 */
	private static final Properties PROPERTIES = new Properties();
	// ==================================================
	/**
	 * Returns the {@link File} where the {@link #PROPERTIES}
	 * are being saved and loaded.
	 */
	public static File getConfigFile()
	{
		return new File(ChunkCopy.getRunDirectory().getAbsolutePath() +
				"/config/" + ChunkCopy.ModID + ".properties");
	}
	// --------------------------------------------------
	/**
	 * Saves the {@link #PROPERTIES} to the mod config file.
	 * @return True if no {@link IOException}s were thrown while saving.
	 */
	public static boolean saveConfig()
	{
		try
		{
			//create file
			File cf = getConfigFile();
			cf.getParentFile().mkdirs();
			cf.createNewFile();
			
			//stream
			FileOutputStream fos = new FileOutputStream(cf);
			PROPERTIES.store(fos, ChunkCopy.ModID + " properties.");
			fos.close();
			
			//return
			return true;
		}
		catch(Exception e) { return false; }
	}
	// --------------------------------------------------
	/**
	 * Loads the {@link #PROPERTIES} from the mod config file.
	 * @return True if no {@link IOException}s were thrown while loading.
	 */
	public static boolean loadConfig()
	{
		try
		{
			//create file
			File cf = getConfigFile();
			if(!cf.exists()) return false;
			
			//stream
			FileInputStream fos = new FileInputStream(cf);
			PROPERTIES.load(fos);
			fos.close();
			
			//return
			return true;
		}
		catch(Exception e) { return false; }
	}
	// ==================================================
	private static Identifier toPId(String property) { return new Identifier(ChunkCopy.ModID, property); }
	// --------------------------------------------------
	public static String get(Identifier property) { return get(property, null); }
	public static String get(Identifier property, String defaultValue)
	{
		return PROPERTIES.getProperty(property.toString(), defaultValue);
	}
	
	public static void set(Identifier property, String value)
	{
		PROPERTIES.setProperty(property.toString(), value);
	}
	// ==================================================
	public static boolean getPasteEntities()
	{
		return Boolean.parseBoolean(get(toPId("pasteEntities"), "true"));
	}
	
	public static void setPasteEntitie(boolean arg0)
	{
		set(toPId("pasteEntities"), Boolean.toString(arg0));
	}
	// --------------------------------------------------
	public static int getEntityCap()
	{
		int cap = Math.abs(NumberUtils.toInt(get(toPId("entityCap")), 15));
		if(cap > 15) cap = 15;
		return cap;
	}
	
	public static void setEntityCap(int arg0)
	{
		set(toPId("entityCap"), Integer.toString(arg0));
	}
	// ==================================================
}
