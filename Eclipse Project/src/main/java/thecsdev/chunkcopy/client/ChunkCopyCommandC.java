package thecsdev.chunkcopy.client;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import thecsdev.chunkcopy.io.CCUtils;
import thecsdev.chunkcopy.io.Tuple;
import static thecsdev.chunkcopy.ChunkCopy.getExceptionMessage;

import java.util.function.Predicate;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

@Environment(EnvType.CLIENT)
public final class ChunkCopyCommandC
{
	// ==================================================
	public static int PERMISSION_LEVEL = 0;
	
	private final static Predicate<FabricClientCommandSource> SinglepOP =
			src -> src.hasPermissionLevel(4) && thecsdev.chunkcopy.client.ChunkCopyClient.Client.isInSingleplayer();
	// ==================================================
	public void register(CommandDispatcher<FabricClientCommandSource> dispatcher)
	{
		dispatcher.register(literal("chunkcopy").requires(src -> src.hasPermissionLevel(PERMISSION_LEVEL))
				.then(literal("copy")
						.then(argument("fileName", StringArgumentType.word())
								.executes(arg -> exec_copy_fn(arg))
								.then(argument("chunkDistance", IntegerArgumentType.integer(1, 8))
										.executes(arg -> exec_copy_fn_cd(arg)))))
				.then(literal("paste").requires(SinglepOP)
						.then(argument("fileName", StringArgumentType.word())
								.executes(arg -> exec_paste_fn(arg))
								.then(argument("chunkDistance", IntegerArgumentType.integer(1, 8))
										.executes(arg -> exec_paste_fn_cd(arg)))))
				.then(literal("clear").requires(SinglepOP)
						.then(argument("chunkDistance", IntegerArgumentType.integer(1, 8))
								.executes(arg -> exec_clear_cd(arg))))
				.then(literal("fill").requires(SinglepOP)
						.then(argument("chunkDistance", IntegerArgumentType.integer(1, 8))
								.then(argument("blockState", BlockStateArgumentType.blockState())
										.executes(arg -> exec_fill_cd_state(arg))))));
	}
	// ==================================================
	private static int exec_copy_fn(CommandContext<FabricClientCommandSource> arg)
	{
		copy(arg.getArgument("fileName", String.class), (int) ChunkCopyClient.Client.worldRenderer.getViewDistance() / 2);
		return 1;
	}
	// --------------------------------------------------
	private static int exec_copy_fn_cd(CommandContext<FabricClientCommandSource> arg)
	{
		copy(arg.getArgument("fileName", String.class), arg.getArgument("chunkDistance", Integer.class));
		return 1;
	}
	// --------------------------------------------------
	private static int exec_paste_fn(CommandContext<FabricClientCommandSource> arg)
	{
		paste(arg.getArgument("fileName", String.class), (int) ChunkCopyClient.Client.worldRenderer.getViewDistance() / 2);
		return 1;
	}
	// --------------------------------------------------
	private static int exec_paste_fn_cd(CommandContext<FabricClientCommandSource> arg)
	{
		paste(arg.getArgument("fileName", String.class), arg.getArgument("chunkDistance", Integer.class));
		return 1;
	}
	// --------------------------------------------------
	private static int exec_clear_cd(CommandContext<FabricClientCommandSource> arg)
	{
		clear(arg.getArgument("chunkDistance", Integer.class));
		return 1;
	}
	// --------------------------------------------------
	private static int exec_fill_cd_state(CommandContext<FabricClientCommandSource> arg)
	{
		BlockStateArgument bsa = arg.getArgument("blockState", BlockStateArgument.class);
		fill(arg.getArgument("chunkDistance", Integer.class), bsa.getBlockState());
		return 1;
	}
	// ==================================================
	public static void copy(String fileName, int chunkDist)
	{
		try
		{
			CCUtils.saveLoadedChunksIO(fileName, chunkDist, getClientChunk());
			
			//feedback
			ChunkCopyClient.printChat(new TranslatableText("thecsdev.chunkcopy.copiedchunks")
					.getString().replace("{$fileName}", fileName));
		}
		catch(Exception e) { handleException(e); }
	}
	// --------------------------------------------------
	public static void paste(String fileName, int chunkDist)
	{
		try
		{
			CCUtils.loadLoadedChunksIO(fileName, chunkDist, getClientChunk());
			
			//feedback
			ChunkCopyClient.printChat(new TranslatableText("thecsdev.chunkcopy.pastedchunks")
					.getString().replace("{$fileName}", fileName));
		}
		catch(Exception e) { handleException(e); }
	}
	// --------------------------------------------------
	public static void clear(int chunkDist)
	{
		try
		{
			BlockState state = Blocks.AIR.getDefaultState();
			CCUtils.fillLoadedChunks(chunkDist, state, getClientChunk());
			
			//feedback
			ChunkCopyClient.printChat(new TranslatableText("thecsdev.chunkcopy.filledchunks")
					.getString().replace("{$blockName}", state.getBlock().getName().getString()));
		}
		catch(Exception e) { handleException(e); }
	}
	// --------------------------------------------------
	public static void fill(int chunkDist, BlockState state)
	{
		try
		{
			CCUtils.fillLoadedChunks(chunkDist, state, getClientChunk());
			
			//feedback
			ChunkCopyClient.printChat(new TranslatableText("thecsdev.chunkcopy.filledchunks")
					.getString().replace("{$blockName}", state.getBlock().getName().getString()));
		}
		catch(Exception e) { handleException(e); }
	}
	// ==================================================
	private static Tuple<World, ChunkPos> getClientChunk()
	{
		MinecraftClient MC = ChunkCopyClient.getClient();
		return new Tuple<World, ChunkPos>(MC.getServer().getWorld(MC.world.getRegistryKey()), MC.player.getChunkPos());
	}
	// --------------------------------------------------
	public static void handleException(Exception e)
	{
		String message = getExceptionMessage(e);
		thecsdev.chunkcopy.client.ChunkCopyClient.printChat(new TranslatableText("thecsdev.chunkcopy.exception")
				.getString().replace("{$message}", "\n" + message + "\n"));
	}
	// ==================================================
}
