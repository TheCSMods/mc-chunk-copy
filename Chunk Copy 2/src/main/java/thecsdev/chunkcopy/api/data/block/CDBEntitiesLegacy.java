package thecsdev.chunkcopy.api.data.block;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import thecsdev.chunkcopy.ChunkCopy;
import thecsdev.chunkcopy.api.ChunkCopyUtils;
import thecsdev.chunkcopy.api.data.ChunkDataBlock;
import thecsdev.chunkcopy.api.data.ChunkDataBlockID;
import thecsdev.chunkcopy.api.io.IOUtils;

/**
 * A {@link ChunkDataBlock} that contains information about world chunk entities.
 */
@ChunkDataBlockID(namespace = ChunkCopy.ModID, path = "entities_legacy")
public class CDBEntitiesLegacy extends ChunkDataBlock
{
	// ==================================================
	public final ArrayList<NbtCompound> EntityNBTs = new ArrayList<NbtCompound>();
	// ==================================================
	@Override
	public void copyData(World world, ChunkPos chunkPos)
	{
		//clear old data
		EntityNBTs.clear();
		
		//iterate entities
		for (Entity entity : ChunkCopyUtils.getEntitiesInChunk(world, chunkPos))
		{
			//create nbt for each entity
			NbtCompound eNbt = new NbtCompound();
			
			//write entity data to the nbt
			eNbt.putString("id", EntityType.getId(entity.getType()).toString());
			eNbt = entity.writeNbt(eNbt);
			
			//add the nbt to EntityNBTs
			EntityNBTs.add(eNbt);
		}
	}
	// --------------------------------------------------
	@Override
	public void pasteData(ServerWorld world, ChunkPos chunkPos)
	{
		//clear existing entities in the chunk
		for (Entity entity : ChunkCopyUtils.getEntitiesInChunk(world, chunkPos))
		{
			if(!entity.isPlayer())
				entity.discard();
		}
		
		//iterate NBTs
		for (NbtCompound eNbt : EntityNBTs)
			try
			{
				//create entity from nbt
				Entity entity = EntityType.getEntityFromNbt(eNbt, world).get();
				
				//discard old entity if there is one and if it isn't already removed
				Entity oldEntity = world.getEntity(entity.getUuid());
				if(oldEntity != null && !oldEntity.isRemoved()) oldEntity.discard();
				
				//spawn entity
				world.spawnEntity(entity);
			}
			catch (Exception e) {}
	}
	// --------------------------------------------------
	@Override
	public void updateClients(ServerWorld world, ChunkPos chunkPos)
	{
		//no need to do this, the game will do this for us.
	}
	// ==================================================
	@Override
	public void readData(InputStream stream) throws IOException
	{
		//clear old data
		EntityNBTs.clear();
		
		//keep reading until there is nothing left to read
		while(stream.available() > 0)
			try
			{
				String nbtString = IOUtils.readString(stream);
				NbtCompound eNbt = NbtHelper.fromNbtProviderString(nbtString);
				EntityNBTs.add(eNbt);
			}
			catch (CommandSyntaxException e) { throw new IOException("Invalid or corrupted Entity NBT data."); }
			catch (Exception e) { break; }
	}
	// --------------------------------------------------
	@Override
	public void writeData(OutputStream stream) throws IOException
	{
		//iterate all entity NBTs
		for (NbtCompound eNbt : EntityNBTs)
			//write entity nbt
			IOUtils.writeString(stream, NbtHelper.toNbtProviderString(eNbt));
	}
	// ==================================================
}
