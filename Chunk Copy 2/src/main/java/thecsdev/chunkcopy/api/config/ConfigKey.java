package thecsdev.chunkcopy.api.config;

import com.mojang.brigadier.arguments.ArgumentType;

import net.minecraft.util.Identifier;

/**
 * A property key for the {@link ChunkCopyConfig}.<br/>
 * Please put any custom {@link ConfigKey}s in {@link ChunkCopyConfig#KEYS},
 * otherwise they might not work properly.
 */
public /*non final*/ class ConfigKey<T>
{
	// ==================================================
	public final Identifier keyName;
	public final ArgumentType<T> argumentType;
	// ==================================================
	public ConfigKey(String namespace, String path, ArgumentType<T> argType)
	{
		this(new Identifier(namespace, path), argType);
	}
	
	public ConfigKey(Identifier name, ArgumentType<T> argType)
	{
		this.keyName = name;
		this.argumentType = argType;
	}
	// --------------------------------------------------
	@Override
	public final int hashCode()
	{
		int a = keyName.toString().hashCode();
		int b = argumentType.toString().hashCode();
		return a + b;
	}
	// --------------------------------------------------
	/**
	 * Override this to define how &ltT&gt should
	 * be converted to {@link String}.
	 * @param value The object being converted to string.
	 */
	public String valueToString(T value) { return value.toString(); }
	// ==================================================
}