package thecsdev.chunkcopy.api.data.block;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import thecsdev.chunkcopy.ChunkCopy;
import thecsdev.chunkcopy.api.data.ChunkDataBlock;
import thecsdev.chunkcopy.api.data.ChunkDataBlockID;
import thecsdev.chunkcopy.api.io.IOUtils;

/**
 * A {@link ChunkDataBlock} that contains information about world chunk entity blocks.
 */
@ChunkDataBlockID(namespace = ChunkCopy.ModID, path = "entity_blocks_legacy")
public class CDBEntityBlocksLegacy extends ChunkDataBlock
{
	// ==================================================
	public final ArrayList<CDBEntityBlock> BlockEntities = new ArrayList<CDBEntityBlock>();
	// ==================================================
	@Override
	public void copyData(World world, ChunkPos chunkPos)
	{
		//clear old data
		BlockEntities.clear();
		
		//paste new data
		WorldChunk chunk = world.getChunk(chunkPos.x, chunkPos.z);
		for (BlockPos eBlockPos : chunk.getBlockEntityPositions())
		{
			BlockState eBlockState = chunk.getBlockState(eBlockPos);
			BlockEntity eBlock = chunk.getBlockEntity(eBlockPos);
			
			CDBEntityBlock cdbBlock = new CDBEntityBlock();
			cdbBlock.x = eBlockPos.getX();
			cdbBlock.y = eBlockPos.getY();
			cdbBlock.z = eBlockPos.getZ();
			cdbBlock.blockId = Block.getRawIdFromState(eBlockState);
			cdbBlock.nbtData = eBlock.createNbtWithIdentifyingData();
			
			BlockEntities.add(cdbBlock);
		}
	}
	// --------------------------------------------------
	@Override
	public void pasteData(ServerWorld world, ChunkPos chunkPos)
	{
		//iterate entity blocks
		WorldChunk chunk = world.getChunk(chunkPos.x, chunkPos.z);
		for (CDBEntityBlock cdbBlock : BlockEntities)
		{
			BlockPos eBlockPos = new BlockPos(cdbBlock.x, cdbBlock.y, cdbBlock.z);
			BlockState eBlockState = Block.getStateFromRawId(cdbBlock.blockId);
			
			if(!eBlockState.isAir() && cdbBlock.nbtData != null)
			{
				chunk.setBlockState(eBlockPos, eBlockState, false);
				chunk.setBlockEntity(BlockEntity.createFromNbt(eBlockPos, eBlockState, cdbBlock.nbtData));
			}
		}
		
		//mark chunk as needs saving
		chunk.setNeedsSaving(true);
	}
	// --------------------------------------------------
	@Override
	public void updateClients(ServerWorld world, ChunkPos chunkPos)
	{
		//no need to do this, chunk.setBlockEntity(...) will do this for us.
	}
	// ==================================================
	@Override
	public void readData(InputStream stream) throws IOException
	{
		//clear old data
		BlockEntities.clear();
		
		//keep reading chunks
		while(stream.available() > 0)
		{
			int len = IOUtils.readVarInt(stream);
			byte[] bytes = stream.readNBytes(len);
			
			CDBEntityBlock cdbBlock = new CDBEntityBlock();
			cdbBlock.readByteArray(bytes);
			BlockEntities.add(cdbBlock);
		}
	}
	// --------------------------------------------------
	@Override
	public void writeData(OutputStream stream) throws IOException
	{
		//iterate each copied entity block
		for (CDBEntityBlock cdbBlock : BlockEntities)
		{
			//write
			byte[] bytes = cdbBlock.toByteArray();
			IOUtils.writeVarInt(stream, bytes.length);
			stream.write(bytes);
		}
	}
	// ==================================================
	/**
	 * A class that contains information
	 * about a copied entity block.
	 */
	public class CDBEntityBlock
	{
		// ---------------------------------
		public int x, y, z;
		public int blockId;
		public NbtCompound nbtData;
		// ---------------------------------
		public byte[] toByteArray() throws IOException
		{
			if(nbtData == null) nbtData = new NbtCompound();
			
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			IOUtils.writeVarInt(stream, x);
			IOUtils.writeVarInt(stream, y);
			IOUtils.writeVarInt(stream, z);
			IOUtils.writeVarInt(stream, blockId);
			IOUtils.writeString(stream, NbtHelper.toNbtProviderString(nbtData));
			
			byte[] bytes = stream.toByteArray();
			stream.close();
			return bytes;
		}
		// ---------------------------------
		public void readByteArray(byte[] bytes) throws IOException
		{
			ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
			x = IOUtils.readVarInt(stream);
			y = IOUtils.readVarInt(stream);
			z = IOUtils.readVarInt(stream);
			blockId = IOUtils.readVarInt(stream);
			try { nbtData = NbtHelper.fromNbtProviderString(IOUtils.readString(stream)); }
			catch(CommandSyntaxException e) { throw new IOException("Invalid or corrupted BlockEntity NBT data."); }
		}
		// ---------------------------------
	}
	// ==================================================
}
