package thecsdev.chunkcopy.api.data.block;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.BitSet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import thecsdev.chunkcopy.ChunkCopy;
import thecsdev.chunkcopy.api.data.ChunkDataBlock;
import thecsdev.chunkcopy.api.data.ChunkDataBlockID;
import thecsdev.chunkcopy.api.io.IOUtils;

/**
 * A legacy {@link ChunkDataBlock} that contains information about world chunk blocks.<br/>
 * <br/>
 * <b>Obsolete because of how im-performant (slow) it is. Use {@link CDBChunkSections} instead.</b>
 */
@ChunkDataBlockID(namespace = ChunkCopy.ModID, path = "blocks_legacy")
public class CDBBlocksLegacy extends ChunkDataBlock
{
	// ==================================================
	/**
	 * Defines the bottom Y level of the chunk.
	 * This is the Y level of bedrock.
	 */
	public int StartY = 0;
	
	/**
	 * The array of copied blocks.
	 */
	public final ArrayList<Integer> BlockIDs = new ArrayList<Integer>();
	// ==================================================
	@Deprecated(forRemoval = true, since = "v2.0.0")
	@Override
	public void copyData(World world, ChunkPos chunkPos)
	{
		WorldChunk chunk = world.getChunk(chunkPos.x, chunkPos.z);
		int chunkWidthX = Math.abs(chunkPos.getEndX() - chunkPos.getStartX());
		int chunkWidthZ = Math.abs(chunkPos.getEndZ() - chunkPos.getStartZ());
		
		//Start Y
		StartY = chunk.getBottomY();
		
		//Block IDs
		BlockIDs.clear();
		int x = 0, y = chunk.getBottomY(), z = 0;
		while(y < chunk.getTopY() + 1)
		{
			//handle block
			BlockPos blockPos = chunk.getPos().getBlockPos(x, y, z);
			BlockState blockState = chunk.getBlockState(blockPos);
			BlockIDs.add(Block.getRawIdFromState(blockState));
			
			//increment
			x++;
			if(x > chunkWidthX)
			{
				z++; x = 0;
				if(z > chunkWidthZ) { y++; z = 0; }
			}
		}
	}
	// --------------------------------------------------
	@Deprecated(forRemoval = true, since = "v2.0.0")
	@Override
	public void pasteData(ServerWorld world, ChunkPos chunkPos)
	{
		WorldChunk chunk = world.getChunk(chunkPos.x, chunkPos.z);
		int chunkWidthX = Math.abs(chunkPos.getEndX() - chunkPos.getStartX());
		int chunkWidthZ = Math.abs(chunkPos.getEndZ() - chunkPos.getStartZ());
		
		//iterate all block IDs
		int x = 0, y = StartY, z = 0;
		for (int blockID : BlockIDs)
		{
			//break if too high
			if(y > chunk.getTopY()) break;
			
			//handle block
			try
			{
				do
				{
					//ignore blocks below bedrock and above build limit
					if(y < chunk.getBottomY() || y > chunk.getTopY())
						break;
					
					//get state, section, and local coords
					BlockState state = Block.getStateFromRawId(blockID);
					if(state.hasBlockEntity()) break;
					
					ChunkSection toChunkSection = chunk.getSection(chunk.getSectionIndex(y));
					toChunkSection.setBlockState(x, y & 0xF, z, state);
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
		chunk.setNeedsSaving(true);
	}
	// --------------------------------------------------
	@Override
	public void updateClients(ServerWorld world, ChunkPos chunkPos)
	{
		ChunkDataS2CPacket chunkData = makeMeAChunkDataPacketPls(world, chunkPos);
		world.getPlayers().forEach(p -> p.networkHandler.sendPacket(chunkData));
	}
	// ==================================================
	@Override
	public void readData(InputStream stream) throws IOException
	{
		//read StartY
		StartY = IOUtils.readVarInt(stream);
		
		//read blocks
		BlockIDs.clear();
		while(stream.available() > 0)
		{
			try { BlockIDs.add(IOUtils.readVarInt(stream)); }
			catch(IOException e) {}
		}
	}
	// --------------------------------------------------
	@Override
	public void writeData(OutputStream stream) throws IOException
	{
		//write StartY
		IOUtils.writeVarInt(stream, StartY);
		
		//write blocks
		for (Integer blockId : BlockIDs) { IOUtils.writeVarInt(stream, blockId); }
	}
	// ==================================================
	//i didn't know what else to name this method so whatever
	private static ChunkDataS2CPacket makeMeAChunkDataPacketPls(World world, ChunkPos chunkPos)
	{
		WorldChunk wchunk = world.getChunk(chunkPos.x, chunkPos.z);
		LightingProvider lp = world.getLightingProvider();
		BitSet skyBits = new BitSet(0);
		BitSet blockBits = new BitSet(0);
		return new ChunkDataS2CPacket(wchunk, lp, skyBits, blockBits, true);
	}
	// ==================================================
}
