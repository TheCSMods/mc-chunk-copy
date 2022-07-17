package thecsdev.chunkcopy.api.data.block;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import thecsdev.chunkcopy.ChunkCopy;
import thecsdev.chunkcopy.api.data.ChunkDataBlock;
import thecsdev.chunkcopy.api.data.ChunkDataBlockID;
import thecsdev.chunkcopy.api.io.IOUtils;

/**
 * A {@link ChunkDataBlock} that contains no information about world chunk blocks.
 * It is only used to fill chunks.
 */
@ChunkDataBlockID(namespace = ChunkCopy.ModID, path = "fill_blocks")
public class CDBFillBlocks extends ChunkDataBlock
{
	// ==================================================
	public BlockState state = Blocks.AIR.getDefaultState();
	// ==================================================
	@Override
	public void copyData(World world, ChunkPos chunkPos) {}
	// --------------------------------------------------
	@Override
	public void pasteData(ServerWorld world, ChunkPos chunkPos)
	{
		WorldChunk chunk = world.getChunk(chunkPos.x, chunkPos.z);
		int chunkWidthX = Math.abs(chunkPos.getEndX() - chunkPos.getStartX());
		int chunkWidthZ = Math.abs(chunkPos.getEndZ() - chunkPos.getStartZ());
		
		//iterate all block IDs
		int x = 0, y = chunk.getBottomY(), z = 0;
		
		while (y < chunk.getTopY() + 1)
		{
			//place block
			try
			{
				ChunkSection toChunkSection = chunk.getSection(chunk.getSectionIndex(y));
				toChunkSection.getBlockStateContainer().set(x, y & 0xF, z, state);
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
		//use CDBBlocksLegacy's method of updating clients
		new CDBBlocksLegacy().updateClients(world, chunkPos);
	}
	// ==================================================
	@Override
	public void readData(InputStream stream) throws IOException
	{
		state = Block.getStateFromRawId(IOUtils.readVarInt(stream));
	}
	// --------------------------------------------------
	@Override
	public void writeData(OutputStream stream) throws IOException
	{
		IOUtils.writeVarInt(stream, Block.getRawIdFromState(state));
	}
	// ==================================================
}
