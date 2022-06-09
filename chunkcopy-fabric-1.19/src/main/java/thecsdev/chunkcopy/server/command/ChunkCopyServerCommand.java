package thecsdev.chunkcopy.server.command;

import java.util.ArrayList;

import net.minecraft.block.BlockState;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkPos;
import thecsdev.chunkcopy.api.ChunkCopyAPI;
import thecsdev.chunkcopy.api.ChunkCopyUtils;
import thecsdev.chunkcopy.command.ChunkCopyCommand;

public final class ChunkCopyServerCommand extends ChunkCopyCommand<ServerCommandSource>
{
	// ==================================================
	@Override
	public String getCommandName() { return "chunkcopysrv"; }
	// --------------------------------------------------
	@Override
	protected boolean canCopy(ServerCommandSource cs) { return isOpAndHuman(cs); }
	// --------------------------------------------------
	@Override
	protected boolean canPaste(ServerCommandSource cs) { return isOpAndHuman(cs); }
	// --------------------------------------------------
	@Override
	protected boolean canConfig(ServerCommandSource cs) { return cs.hasPermissionLevel(4); }
	// ==================================================
	protected void execMain(ServerCommandSource cs)
	{
		String feedback  = "[Chunk Copy] Only operator players can execute this command so "
				+ "that the mod can know where you wish to copy/paste chunks.";
		cs.sendFeedback(Text.literal(feedback), false);
	}
	// --------------------------------------------------
	@Override
	protected void copy(ServerCommandSource commandSource, String fileName, int chunkDistance)
	{
		try
		{
			//obtain chunks
			ServerWorld world = commandSource.getWorld();
			ChunkPos chunkPos = commandSource.getPlayer().getChunkPos();
			ArrayList<ChunkPos> loadedChunks = ChunkCopyUtils.getNearbyLoadedChunks(world, chunkPos, chunkDistance);
			
			//copy chunks
			for (ChunkPos cp : loadedChunks) ChunkCopyAPI.saveChunkDataIO(world, cp, fileName);
			
			//send feedback
			String feedback = String.format("[Chunk Copy] Copied chunk data to '%s'.", fileName);
			commandSource.sendFeedback(Text.literal(feedback), true);
		}
		catch (Exception e) { handleException(commandSource, e); return; }
	}
	// --------------------------------------------------
	@Override
	protected void paste(ServerCommandSource commandSource, String fileName, int chunkDistance)
	{
		//check if file exists
		if(!ChunkCopyAPI.getSaveFileDirectory(fileName).exists())
		{
			String feedback = String.format("[Chunk Copy] Unable to paste chunks from '%s', file not found.", fileName);
			commandSource.sendFeedback(Text.literal(feedback), true);
			return;
		}
		
		try
		{
			//obtain chunks
			ServerWorld world = commandSource.getWorld();
			ChunkPos chunkPos = commandSource.getPlayer().getChunkPos();
			ArrayList<ChunkPos> loadedChunks = ChunkCopyUtils.getNearbyLoadedChunks(world, chunkPos, chunkDistance);
			
			//paste chunks
			for (ChunkPos cp : loadedChunks) ChunkCopyAPI.loadChunkDataIO(world, cp, fileName);
			
			//send feedback
			String feedback = String.format("[Chunk Copy] Pasted chunk data from '%s'.", fileName);
			commandSource.sendFeedback(Text.literal(feedback), true);
		}
		catch (Exception e) { handleException(commandSource, e); return; }
	}
	// --------------------------------------------------
	@Override
	protected void fill(ServerCommandSource commandSource, int chunkDistance, BlockState block)
	{
		try
		{
			//obtain chunks
			ServerWorld world = commandSource.getWorld();
			ChunkPos chunkPos = commandSource.getPlayer().getChunkPos();
			ArrayList<ChunkPos> loadedChunks = ChunkCopyUtils.getNearbyLoadedChunks(world, chunkPos, chunkDistance);
			
			//fill chunks
			for (ChunkPos cp : loadedChunks) ChunkCopyAPI.fillChunkBlocks(world, cp, block);
			
			//send feedback
			String bn = block.getBlock().getName().getString();
			String feedback = String.format("[Chunk Copy] Filled chunk blocks with '%s'.", bn);
			commandSource.sendFeedback(Text.literal(feedback), true);
		}
		catch (Exception e) { handleException(commandSource, e); return; }
	}
	// --------------------------------------------------
	@Override
	protected void autoCopyStart(ServerCommandSource commandSource, String fileName) { autoCopyStop(commandSource); }
	
	@Override
	protected void autoCopyStop(ServerCommandSource commandSource)
	{
		//send feedback
		String feedback = "[Chunk Copy] Auto-copying is not available server-side.";
		commandSource.sendFeedback(Text.literal(feedback), false);
	}
	// ==================================================
	private static boolean isOpAndHuman(ServerCommandSource src)
	{
		try { return src.getPlayer().hasPermissionLevel(4); }
		catch (Exception e) { return false; }
	}
	// --------------------------------------------------
	/**
	 * Sends exception feedback.
	 */
	private void handleException(ServerCommandSource source, Exception e)
	{
		String feedback = String.format(
				"[Chunk Copy] An exception was thrown while executing the command: %s",
				"\n" + getExceptionMessage(e));
		source.sendFeedback(Text.literal(feedback), true);
	}
	// ==================================================
}
