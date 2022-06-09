package thecsdev.chunkcopy.api.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.BoolArgumentType;

import net.minecraft.util.Identifier;
import thecsdev.chunkcopy.ChunkCopy;

/**
 * Manages the mod's configuration.<br/>
 * <b>STILL UNDER CONSTRUCTION...</b>
 */
public final class ChunkCopyConfig
{
	// ==================================================
	public static final ConfigKey<Boolean> PASTE_ENTITIES = new ConfigKey<Boolean>(ChunkCopy.getModID(), "paste_entities", BoolArgumentType.bool());
	// ==================================================
	/**
	 * The main {@link Properties} object that holds all of the mod's properties.
	 */
	private static final Properties PROPERTIES = new Properties();
	// --------------------------------------------------
	/**
	 * Contains the property keys for the {@link ChunkCopyConfig}.
	 */
	public static final HashSet<ConfigKey<?>> KEYS = new HashSet<ConfigKey<?>>();
	// ==================================================
	/**
	 * Returns the file where the {@link ChunkCopyConfig} is saved and loaded.
	 */
	public static File getPropertiesFile()
	{
		return new File(ChunkCopy.getRunDirectory().getAbsolutePath() +
				"/config/" + ChunkCopy.ModID + ".properties");
	}
	// --------------------------------------------------
	/**
	 * Saves the config properties to it's save file.
	 */
	public static boolean saveConfig()
	{
		FileOutputStream fos = null;
		try
		{
			File file = getPropertiesFile();
			file.getParentFile().mkdirs();
			file.createNewFile();
			
			fos = new FileOutputStream(file);
			PROPERTIES.store(fos, ChunkCopy.ModName + " config");
			
			return true;
		}
		catch (IOException e) { return false; }
		finally
		{
			if(fos != null)
				try { fos.close(); } catch(Exception e) {}
		}
	}
	// --------------------------------------------------
	/**
	 * Loads the config properties from it's save file.
	 */
	public static boolean loadConfig()
	{
		FileInputStream fis = null;
		try
		{
			File file = getPropertiesFile();
			file.getParentFile().mkdirs();
			file.createNewFile();
			
			fis = new FileInputStream(file);
			PROPERTIES.load(fis);
			
			return true;
		}
		catch (IOException e) { return false; }
		finally
		{
			if(fis != null)
				try { fis.close(); } catch(Exception e) {}
		}
	}
	// --------------------------------------------------
	/**
	 * Returns a {@link ConfigKey} from the {@link #KEYS}
	 * list using it's name (aka {@link Identifier}).
	 */
	@Nullable
	public static ConfigKey<?> getKeyByName(Identifier keyName)
	{
		try { return KEYS.stream().filter(i -> i.keyName.compareTo(keyName) == 0).findFirst().get(); }
		catch(Exception e) { return null; }
	}
	// ==================================================
	/**
	 * Gets the value of a property.
	 * @param key The property key.
	 * @param defaultValue The value that will be returned if the key
	 * isn't listed or the value is undefined.
	 */
	public static <T> T get(ConfigKey<T> key, T defaultValue)
	{
		try
		{
			String value = PROPERTIES.getProperty(key.keyName.toString());
			StringReader sr = new StringReader(value);
			return key.argumentType.parse(sr);
		}
		catch(Exception e) { return defaultValue; }
	}
	
	/**
	 * Sets the value of a property.
	 * @param key The property key.
	 * @param value The value that the property will be set to.
	 */
	public static <T> void set(ConfigKey<T> key, T value)
	{
		PROPERTIES.setProperty(key.keyName.toString(), key.valueToString(value));
	}
	// ==================================================
}