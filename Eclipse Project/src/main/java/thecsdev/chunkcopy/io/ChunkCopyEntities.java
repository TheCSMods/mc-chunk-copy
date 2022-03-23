package thecsdev.chunkcopy.io;

import java.util.ArrayList;

import net.minecraft.entity.EntityType;
import net.minecraft.util.registry.Registry;

public class ChunkCopyEntities
{
	// ==================================================
	/**
	 * Returns a list of all registered entity types in the
	 * Minecraft's {@link Registry#ENTITY_TYPE} registry.
	 * @param allOfThem Do you want all of them? It's not safe.
	 */
	public static EntityType<?>[] getAllEntityTypes()
	{
		//or just take all of them i guess
		ArrayList<EntityType<?>> result = new ArrayList<>();
		Registry.ENTITY_TYPE.getEntries().forEach(i -> result.add(i.getValue()));
		return result.toArray(new EntityType<?>[result.size()]);
	}
	// ==================================================
}
