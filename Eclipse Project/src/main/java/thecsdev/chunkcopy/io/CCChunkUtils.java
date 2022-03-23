package thecsdev.chunkcopy.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;

import thecsdev.chunkcopy.ChunkCopy;

/**
 * Provides utility methods related to chunk copying/pasting for the {@link ChunkCopy} mod.
 */
public final class CCChunkUtils
{
	// ==================================================
	/**
	 * Returns a global-space box containing the whole chunk.
	 */
	public static Box getChunkBoxGlobal(World world, ChunkPos chunkPos)
	{
		//calculate
		Chunk chunk = world.getChunk(chunkPos.getBlockPos(0, 0, 0));
		int chunkWidthX = Math.abs(chunkPos.getEndX() - chunkPos.getStartX());
		int chunkWidthZ = Math.abs(chunkPos.getEndZ() - chunkPos.getStartZ());
		Box chunkBox = new Box(
				chunkPos.getBlockPos(0, chunk.getBottomY(), 0),
				chunkPos.getBlockPos(chunkWidthX, chunk.getTopY(), chunkWidthZ));
		
		//return
		return chunkBox;
	}
	// ==================================================
	/**
	 * Iterates all block states in the chunk and adds their
	 * IDs to an array which is then returned.<br/>
	 * This method is like copying chunk blocks to an integer array.<br/>
	 * This method does not add any extra chunk info to the array,
	 * only the blocks that are within the chunk.<br/>
	 * <b>The startY used when copying blocks is chunk.getBottomY()</b>.
	 */
	public static int[] chunkToBlockIDs(Chunk chunk)
	{
		//define stuff
		ArrayList<Integer> blockIDs = new ArrayList<>();
		ChunkPos cp = chunk.getPos();
		int chunkWidthX = Math.abs(cp.getEndX() - cp.getStartX());
		int chunkWidthZ = Math.abs(cp.getEndZ() - cp.getStartZ());
		
		//iterate all blocks in the chunk
		int x = 0, y = chunk.getBottomY(), z = 0;
		while(y < chunk.getTopY() + 1)
		{
			//handle block
			BlockPos blockPos = chunk.getPos().getBlockPos(x, y, z);
			BlockState blockState = chunk.getBlockState(blockPos);
			blockIDs.add(Block.getRawIdFromState(blockState));
			
			//increment
			x++;
			if(x > chunkWidthX)
			{
				z++; x = 0;
				if(z > chunkWidthZ) { y++; z = 0; }
			}
		}
		
		//return
		return blockIDs.stream().mapToInt(i->i).toArray();
	}
	// --------------------------------------------------
	/**
	 * Iterates all block states from the integer array, and
	 * applies them to the chunk.<br/>
	 * In other words, this is like pasting chunk blocks to a chunk.
	 * @param blockIDs The block ID array.
	 * @param startY The starting Y level of the blockIDs. Usually toChunk.getBottomY();
	 * @throws ChunkNotLoadedException 
	 */
	public static void blockIDsToChunk(int[] blockIDs, World world, ChunkPos chunkPos, int startY)
	throws ChunkNotLoadedException
	{
		//check if chunk loaded
		if(!world.isChunkLoaded(chunkPos.x, chunkPos.z))
			throw new ChunkNotLoadedException(world, chunkPos);
		
		//define stuff
		Chunk toChunk = world.getChunk(chunkPos.getBlockPos(0, 0, 0));
		int chunkWidthX = Math.abs(chunkPos.getEndX() - chunkPos.getStartX());
		int chunkWidthZ = Math.abs(chunkPos.getEndZ() - chunkPos.getStartZ());
		
		int x = 0, y = startY, z = 0;
		
		//iterate all block IDs
		for (int blockID : blockIDs)
		{
			//break if too high
			if(y > toChunk.getTopY()) break;
			
			//handle block
			try
			{
				do
				{
					//ignore blocks below bedrock and above build limit
					if(y < toChunk.getBottomY() || y > toChunk.getTopY())
						break;
					
					//get state, section, and local coords
					BlockState state = Block.getStateFromRawId(blockID);
					
					ChunkSection toChunkSection = toChunk.getSection(toChunk.getSectionIndex(y));
				    int localX = x, localY = y & 0xF, localZ = z;
					toChunkSection.setBlockState(localX, localY, localZ, state);
				}
				while(false);
			}
			catch (Exception e) { break; }
			
			//increment
			x++;
			if(x > chunkWidthX)
			{
				z++; x = 0;
				if(z > chunkWidthZ) { y++; z = 0; }
			}
		}
		
		//mark as needs saving
		if(!world.isClient) toChunk.setShouldSave(true);
	}
	// ==================================================
	/**
	 * Saves all entity block data from a chunk to a byte array.<br/>
	 * RIFF format:  [(entity_block), (entity_block)...]<br/>
	 * Entity block RIFF format: <b>[length][</b> [x][y][z][blockId][nbt_data_length][nbt_data] <b>]</b>
	 */
	public static byte[] chunkEntityBlocksToBytes(Chunk chunk) throws IOException
	{
		//create stream
		ByteArrayOutputStream chunkBytes = new ByteArrayOutputStream();
		
		//iterate entity blocks
		for (BlockPos eBlockPos : chunk.getBlockEntityPositions())
		{
			//define eBlock stuff
			ByteArrayOutputStream eBlockBytes = new ByteArrayOutputStream();
			BlockState eBlockState = chunk.getBlockState(eBlockPos);
			BlockEntity eBlock = chunk.getBlockEntity(eBlockPos);
			
			//write position data
			CCStreamUtils.writeVarInt(eBlockBytes, eBlockPos.getX());
			CCStreamUtils.writeVarInt(eBlockBytes, eBlockPos.getY());
			CCStreamUtils.writeVarInt(eBlockBytes, eBlockPos.getZ());
			
			//write block id
			CCStreamUtils.writeVarInt(eBlockBytes, Block.getRawIdFromState(eBlockState));
			
			//write nbt data
			{
				NbtCompound eNBT = eBlock.createNbtWithIdentifyingData();
				byte[] eNBTBytes = NbtHelper.toNbtProviderString(eNBT).getBytes("UTF-8");
				CCStreamUtils.writeVarInt(eBlockBytes, eNBTBytes.length);
				eBlockBytes.write(eNBTBytes);
			}
			
			//write all data to chunkBytes
			eBlockBytes.close();
			CCStreamUtils.writeVarInt(chunkBytes, eBlockBytes.size());
			chunkBytes.write(eBlockBytes.toByteArray());
		}
		
		//return bytes
		chunkBytes.close();
		byte[] result = chunkBytes.toByteArray();
		return result;
	}
	// --------------------------------------------------
	/**
	 * Loads all entity block data from a byte array to a chunk.<br/>
	 * See there RIFF format here: {@link #chunkEntityBlocksToBytes(Chunk)}.
	 * @throws IOException If an IO exception occurs.
	 * @throws ChunkNotLoadedException 
	 * @throws CommandSyntaxException When an entity block NBT data is invalid. 
	 */
	public static void bytesToChunkEntityBlocks(byte[] bytes, World world, ChunkPos chunkPos)
	throws IOException, ChunkNotLoadedException
	{
		//check if chunk loaded
		if(!world.isChunkLoaded(chunkPos.x, chunkPos.z))
			throw new ChunkNotLoadedException(world, chunkPos);
		
		//create stream
		ByteArrayInputStream chunkBytes = new ByteArrayInputStream(bytes);
		
		//read entity blocks
		Chunk toChunk = world.getWorldChunk(chunkPos.getBlockPos(0, 0, 0));
		while(chunkBytes.available() > 0)
		{
			//read entity block bytes
			int eBlockBytesLen = CCStreamUtils.readVarInt(chunkBytes);
			ByteArrayInputStream eBlockBytes = new ByteArrayInputStream(chunkBytes.readNBytes(eBlockBytesLen));
			
			//process entity block bytes
			{
				//get block pos and block state
				BlockPos blockPos = new BlockPos(
						CCStreamUtils.readVarInt(eBlockBytes),
						CCStreamUtils.readVarInt(eBlockBytes),
						CCStreamUtils.readVarInt(eBlockBytes));
				BlockState blockState = Block.getStateFromRawId(CCStreamUtils.readVarInt(eBlockBytes));
				
				//get block nbt data
				int eBlockNbtLen = CCStreamUtils.readVarInt(eBlockBytes);
				String eBlockNbtRaw = new String(eBlockBytes.readNBytes(eBlockNbtLen), "UTF-8");
				NbtCompound blockNbt;
				try { blockNbt = NbtHelper.fromNbtProviderString(eBlockNbtRaw); }
				catch (CommandSyntaxException e) { throw new IOException("Invalid or corrupt chunk NBT data."); }
				
				//apply
				if(!blockState.isAir()) //the check is done now so as to let the stream NBT data get handled
					toChunk.setBlockEntity(BlockEntity.createFromNbt(blockPos, blockState, blockNbt));
			}
			
			//close
			eBlockBytes.close();
		}
		
		//close and end
		chunkBytes.close();
	}
	// ==================================================
	public static byte[] chunkEntitiesToBytes(World world, ChunkPos chunkPos)
	throws IOException, ChunkNotLoadedException
	{
		//create stream
		ByteArrayOutputStream chunkBytes = new ByteArrayOutputStream();
		
		//get chunk box
		Box chunkBox = getChunkBoxGlobal(world, chunkPos);
		
		//iterate all entity types and write them down
		for (EntityType<?> entityType : ChunkCopyEntities.getAllEntityTypes())
		{
			for (Entity entity : world.getEntitiesByType(entityType, chunkBox, (arg) -> true))
			{
				//create a stream for each entity
				ByteArrayOutputStream eBytes = new ByteArrayOutputStream();
				
				//write entity data to an NBT Compound
				NbtCompound eNbt = new NbtCompound();
				eNbt.putString("id", EntityType.getId(entityType).toString());
				eNbt = entity.writeNbt(eNbt);
				
				//write the NBT data to the stream, and write the stream to chunk data
				eBytes.write(NbtHelper.toNbtProviderString(eNbt).getBytes("UTF-8"));
				CCStreamUtils.writeVarInt(chunkBytes, eBytes.size());
				chunkBytes.write(eBytes.toByteArray());
				eBytes.close();
			}
		}
		
		//return bytes
		chunkBytes.close();
		byte[] result = chunkBytes.toByteArray();
		return result;
	}
	// --------------------------------------------------
	public static void bytesToChunkEntities(byte[] bytes, World world, ChunkPos chunkPos)
	throws IOException, ChunkNotLoadedException
	{
		//check if chunk loaded
		if(!world.isChunkLoaded(chunkPos.x, chunkPos.z))
			throw new ChunkNotLoadedException(world, chunkPos);
		
		//create stream and prepare
		ByteArrayInputStream chunkBytes = new ByteArrayInputStream(bytes);
		
		//read entity blocks
		while(chunkBytes.available() > 0)
		{
			//read entity block bytes
			int eBytesLen = CCStreamUtils.readVarInt(chunkBytes);
			ByteArrayInputStream entityBytes = new ByteArrayInputStream(chunkBytes.readNBytes(eBytesLen));
			
			//process entity bytes
			try
			{
				//write entity data to an NBT Compound
				NbtCompound eNbt = NbtHelper.fromNbtProviderString(new String(entityBytes.readAllBytes(), "UTF-8"));
				
				//spawn entity
				try
				{
					//spawn entity
					Entity entity = EntityType.getEntityFromNbt(eNbt, world).get();
					world.spawnEntity(entity);
				}
				catch (Exception e) { /*eh, who cares if it fails ig...*/ }
			}
			catch (CommandSyntaxException e) { throw new IOException("Invalid or corrupt chunk NBT data."); }
			
			//close
			entityBytes.close();
		}
	}
	// ==================================================
	/**
	 * Sets all blocks in a chunk to the specified block state.
	 * @throws ChunkNotLoadedException
	 */
	public static void fillChunkBlocks(World world, ChunkPos chunkPos, BlockState state) throws ChunkNotLoadedException
	{
		//check if chunk loaded
		if(!world.isChunkLoaded(chunkPos.x, chunkPos.z))
			throw new ChunkNotLoadedException(world, chunkPos);
		
		//calculate stuff
		Chunk chunk = world.getChunk(chunkPos.getBlockPos(0, 0, 0));
		
		int chunkWidthX = Math.abs(chunkPos.getEndX() - chunkPos.getStartX());
		int chunkWidthZ = Math.abs(chunkPos.getEndZ() - chunkPos.getStartZ());
		
		//iterate all blocks in the chunk and set them all to air
		int x = 0, y = chunk.getBottomY(), z = 0;
		while(y < chunk.getTopY() + 1)
		{
			try
			{
				//set block
				ChunkSection toChunkSection = chunk.getSection(chunk.getSectionIndex(y));
			    int localX = x, localY = y & 0xF, localZ = z;
				toChunkSection.setBlockState(localX, localY, localZ, state);
				
				//increment
				x++;
				if(x > chunkWidthX)
				{
					z++; x = 0;
					if(z > chunkWidthZ) { y++; z = 0; }
				}
			}
			catch (Exception e) { break; }
		}
		
		//mark as should save
		chunk.setShouldSave(true);
	}
	// ==================================================
}
