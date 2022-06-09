package thecsdev.chunkcopy.api.data.block;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import thecsdev.chunkcopy.ChunkCopy;
import thecsdev.chunkcopy.api.data.ChunkDataBlock;
import thecsdev.chunkcopy.api.data.ChunkDataBlockID;
import thecsdev.chunkcopy.api.io.IOUtils;
import thecsdev.chunkcopy.api.io.Tuple;

/**
 * A {@link ChunkDataBlock} that contains information about world {@link ChunkSection}s.
 */
@ChunkDataBlockID(namespace = ChunkCopy.ModID, path = "chunk_sections")
public class CDBChunkSections extends ChunkDataBlock
{
	// ==================================================
	/**
	 * Stores {@link ChunkSection} data. The {@link Integer} is
	 * {@link ChunkSection#getYOffset()}, and the {@link PacketByteBuf}
	 * is the {@link ChunkSection} data.
	 */
	public final ArrayList<Tuple<Integer, PacketByteBuf>> ChunkSectionData = new ArrayList<Tuple<Integer, PacketByteBuf>>();
	// ==================================================
	@Override
	public void copyData(World world, ChunkPos chunkPos)
	{
		//clear old data
		ChunkSectionData.clear();
		
		Chunk chunk = world.getChunk(chunkPos.x, chunkPos.z);
		for (ChunkSection chunkSection : chunk.getSectionArray())
		{
			PacketByteBuf pbb = PacketByteBufs.create();
			chunkSection.toPacket(pbb);
			ChunkSectionData.add(new Tuple<Integer, PacketByteBuf>(chunkSection.getYOffset(), pbb));
		}
	}
	// --------------------------------------------------
	@Override
	public void pasteData(ServerWorld world, ChunkPos chunkPos)
	{
		Chunk chunk = world.getChunk(chunkPos.x, chunkPos.z);
		for (Tuple<Integer, PacketByteBuf> pbb : ChunkSectionData)
		{
			ChunkSection cs = chunk.getSection(chunk.getSectionIndex(pbb.Item1));
			cs.fromPacket(pbb.Item2);
		}
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
		//clear old data
		ChunkSectionData.clear();
		
		//keep reading until there is nothing left to read
		while(stream.available() > 0)
		{
			int len = IOUtils.readVarInt(stream);
			byte[] pbbBytes = stream.readNBytes(len);
			
			ByteArrayInputStream pbbStream = new ByteArrayInputStream(pbbBytes);
			int offsetY = IOUtils.readVarInt(pbbStream);
			PacketByteBuf pbb = PacketByteBufs.copy(Unpooled.copiedBuffer(IOUtils.readByteArray(pbbStream)));
			pbbStream.close();
			
			ChunkSectionData.add(new Tuple<Integer, PacketByteBuf>(offsetY, pbb));
		}
	}
	// --------------------------------------------------
	@Override
	public void writeData(OutputStream stream) throws IOException
	{
		//iterate all chunk sections
		for (Tuple<Integer, PacketByteBuf> pbb : ChunkSectionData)
		{
			//handle a separate stream for each tuple
			ByteArrayOutputStream pbbStream = new ByteArrayOutputStream();
			byte[] pbbCsBytes = pbb.Item2.getWrittenBytes();
			
			IOUtils.writeVarInt(pbbStream, pbb.Item1);
			IOUtils.writeByteArray(pbbStream, pbbCsBytes);
			
			//write each stream
			byte[] pbbBytes = pbbStream.toByteArray();
			IOUtils.writeByteArray(stream, pbbBytes);
			pbbStream.close();
		}
	}
	// ==================================================
}
