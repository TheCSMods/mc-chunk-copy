package thecsdev.chunkcopy.api.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.minecraft.util.Identifier;

/**
 * The unique {@link Identifier} of the {@link ChunkDataBlock}.
 * This unique ID is used to keep track of and to differentiate
 * {@link ChunkDataBlock}s and their data.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ChunkDataBlockID
{
	/**
	 * Usually the mod ID.
	 */
	String namespace();
	
	/**
	 * Usually the name of the block.
	 */
	String path();
}
