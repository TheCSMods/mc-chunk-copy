package thecsdev.chunkcopy.client.command;

import java.util.ArrayList;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkPos;
import thecsdev.chunkcopy.api.AutoChunkCopy;
import thecsdev.chunkcopy.api.ChunkCopyAPI;
import thecsdev.chunkcopy.api.ChunkCopyUtils;
import thecsdev.chunkcopy.command.ChunkCopyCommand;

public final class ChunkCopyClientCommand extends ChunkCopyCommand<FabricClientCommandSource>
{
	// ==================================================
	@Override
	public String getCommandName() { return "chunkcopy"; }
	// --------------------------------------------------
	@Override
	protected boolean canChunkCopy(FabricClientCommandSource cs) { return true; }
	// --------------------------------------------------
	@Override
	protected boolean canCopy(FabricClientCommandSource cs) { return true; }
	// --------------------------------------------------
	@Override
	protected boolean canPaste(FabricClientCommandSource cs)
	{
		if(!cs.hasPermissionLevel(4)) return false;
		else if(!net.minecraft.client.MinecraftClient.getInstance().isInSingleplayer()) return false;
		else return true;
	}
	// --------------------------------------------------
	@Override
	protected boolean canConfig(FabricClientCommandSource cs) { return true; }
	// ==================================================
	@Override
	protected void execMain(FabricClientCommandSource cs)
	{
		cs.sendFeedback(Text.translatable("chunkcopy.feedback.syntax.copypaste"));
	}
	// --------------------------------------------------
	@Override
	protected void copy(FabricClientCommandSource commandSource, String fileName, int chunkDistance)
	{
		copy(commandSource, fileName, chunkDistance, true);
	}
	
	public void copy(FabricClientCommandSource commandSource, String fileName, int chunkDistance, boolean sendFeedback)
	{
		//obtain chunks
		ClientWorld world = commandSource.getWorld();
		ChunkPos chunkPos = commandSource.getPlayer().getChunkPos();
		ArrayList<ChunkPos> loadedChunks = ChunkCopyUtils.getNearbyLoadedChunks(world, chunkPos, chunkDistance);
		
		//copy chunks
		try { for (ChunkPos cp : loadedChunks) ChunkCopyAPI.saveChunkDataIO(world, cp, fileName); }
		catch (Exception e) { handleException(commandSource, e); return; }
		
		//send feedback
		if(sendFeedback)
		{
			commandSource.sendFeedback(Text.translatable("chunkcopy.feedback.copied", fileName));
		}
	}
	// --------------------------------------------------
	@Override
	protected void paste(FabricClientCommandSource commandSource, String fileName, int chunkDistance)
	{
		//check if in singleplayer
		MinecraftClient mc = MinecraftClient.getInstance();
		if(!requireSingleplayer(commandSource)) return;
		
		//check if file exists
		if(!ChunkCopyAPI.getSaveFileDirectory(fileName).exists())
		{
			commandSource.sendFeedback(Text.translatable("chunkcopy.feedback.paste_file_not_found",
					new Object[] { fileName }));
			return;
		}
		
		//obtain chunks
		ServerWorld world = mc.getServer().getWorld(mc.world.getRegistryKey());
		ChunkPos chunkPos = commandSource.getPlayer().getChunkPos();
		ArrayList<ChunkPos> loadedChunks = ChunkCopyUtils.getNearbyLoadedChunks(world, chunkPos, chunkDistance);
		
		//paste chunks
		try { for (ChunkPos cp : loadedChunks) ChunkCopyAPI.loadChunkDataIO(world, cp, fileName); }
		catch (Exception e) { handleException(commandSource, e); return; }
		
		//send feedback
		commandSource.sendFeedback(Text.translatable("chunkcopy.feedback.pasted", fileName));
	}
	// --------------------------------------------------
	@Override
	protected void fill(FabricClientCommandSource commandSource, int chunkDistance, BlockState block)
	{
		MinecraftClient mc = MinecraftClient.getInstance();
		if(!requireSingleplayer(commandSource)) return;
		
		//obtain chunks
		ServerWorld world = mc.getServer().getWorld(mc.world.getRegistryKey());
		ChunkPos chunkPos = commandSource.getPlayer().getChunkPos();
		ArrayList<ChunkPos> loadedChunks = ChunkCopyUtils.getNearbyLoadedChunks(world, chunkPos, chunkDistance);
		
		//fill chunks
		try { for (ChunkPos cp : loadedChunks) ChunkCopyAPI.fillChunkBlocks(world, cp, block); }
		catch (Exception e) { handleException(commandSource, e); return; }
		
		//send feedback
		String bn = block.getBlock().getName().getString();
		commandSource.sendFeedback(Text.translatable("chunkcopy.feedback.filled", bn));
	}
	// --------------------------------------------------
	@Override
	protected void autoCopyStart(FabricClientCommandSource commandSource, String fileName)
	{
		copy(commandSource, fileName, 8, false);
		AutoChunkCopy.start(fileName);
		
		//send feedback
		commandSource.sendFeedback(Text.translatable("chunkcopy.feedback.autocopy_start", fileName));
	}
	
	@Override
	protected void autoCopyStop(FabricClientCommandSource commandSource)
	{
		AutoChunkCopy.stop();
		
		//send feedback
		commandSource.sendFeedback(Text.translatable("chunkcopy.feedback.autocopy_stop"));
	}
	// ==================================================
	/**
	 * Returns false and sends feedback
	 * if not in singleplayer.
	 */
	private boolean requireSingleplayer(FabricClientCommandSource cmdSrc)
	{
		//check singleplayer
		MinecraftClient mc = MinecraftClient.getInstance();
		if(!mc.isInSingleplayer())
		{
			cmdSrc.sendFeedback(Text.translatable("chunkcopy.feedback.require_singleplayer"));
			return false;
		}
		return true;
	}
	// --------------------------------------------------
	/**
	 * Sends exception feedback.
	 */
	private void handleException(FabricClientCommandSource source, Exception e)
	{
		source.sendFeedback(Text.translatable("chunkcopy.feedback.command_exception",
				new Object[] { "\n" + getExceptionMessage(e) }));
	}
	// ==================================================
}
