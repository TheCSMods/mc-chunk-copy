package thecsdev.chunkcopy.server.command;

import java.util.ArrayList;

import net.minecraft.block.BlockState;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.ChunkPos;
import thecsdev.chunkcopy.api.AutoChunkCopy.ACCMode;
import thecsdev.chunkcopy.api.AutoChunkCopy;
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
		cs.sendFeedback(new LiteralText(feedback), false);
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
			int affectedChunks = 0;
			for (ChunkPos cp : loadedChunks)
			{
				ChunkCopyAPI.saveChunkDataIO(world, cp, fileName);
				affectedChunks++;
			}
			
			//send feedback
			String feedback = String.format("[Chunk Copy] Copied %d chunks to '%s'.", affectedChunks, fileName);
			commandSource.sendFeedback(new LiteralText(feedback), true);
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
			commandSource.sendFeedback(new LiteralText(feedback), true);
			return;
		}
		
		try
		{
			//obtain chunks
			ServerWorld world = commandSource.getWorld();
			ChunkPos chunkPos = commandSource.getPlayer().getChunkPos();
			ArrayList<ChunkPos> loadedChunks = ChunkCopyUtils.getNearbyLoadedChunks(world, chunkPos, chunkDistance);
			
			//paste chunks
			int affectedChunks = 0;
			for (ChunkPos cp : loadedChunks)
			{
				if(ChunkCopyAPI.loadChunkDataIO(world, cp, fileName))
					affectedChunks++;
			}
			
			//send feedback
			String feedback = String.format("[Chunk Copy] Pasted %d chunks from '%s'.", affectedChunks, fileName);
			commandSource.sendFeedback(new LiteralText(feedback), true);
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
			int affectedChunks = 0;
			for (ChunkPos cp : loadedChunks)
			{
				ChunkCopyAPI.fillChunkBlocks(world, cp, block);
				affectedChunks++;
			}
			
			//send feedback
			String bn = block.getBlock().getName().getString();
			String feedback = String.format("[Chunk Copy] Filled %d chunks with '%s'.", affectedChunks, bn);
			commandSource.sendFeedback(new LiteralText(feedback), true);
		}
		catch (Exception e) { handleException(commandSource, e); return; }
	}
	// --------------------------------------------------
	@Override
	protected void autoChunkCopyStart(ServerCommandSource commandSource, String fileName, ACCMode accMode)
	{
		autoChunkCopyStop(commandSource);
	}
	
	@Override
	protected void autoChunkCopyStop(ServerCommandSource commandSource)
	{
		//send feedback
		String feedback = "[Chunk Copy] AutoChunkCopy is not available server-side.";
		commandSource.sendFeedback(new LiteralText(feedback), false);
		AutoChunkCopy.stop();
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
		source.sendFeedback(new LiteralText(feedback), true);
	}
	// ==================================================
}
