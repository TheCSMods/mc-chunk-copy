package thecsdev.chunkcopy.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import thecsdev.chunkcopy.ChunkCopy;
import thecsdev.chunkcopy.io.CCUtils;
import thecsdev.chunkcopy.io.Tuple;

import static net.minecraft.server.command.CommandManager.literal;
import static thecsdev.chunkcopy.ChunkCopy.getExceptionMessage;
import static net.minecraft.server.command.CommandManager.argument;

@Environment(EnvType.SERVER)
public final class ChunkCopyCommandS
{
	// ==================================================
	public static int PERMISSION_LEVEL = 4;
	// ==================================================
	public void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated)
	{
		//get root node
		RootCommandNode<ServerCommandSource> rootNode = dispatcher.getRoot();
		
		//chunk copy root node
		LiteralCommandNode<ServerCommandSource> ccRootNode =
			literal("chunkcopysrv").requires(src -> src.hasPermissionLevel(PERMISSION_LEVEL))
				.then(literal("copy")
						.then(argument("fileName", StringArgumentType.word())
								.executes(arg -> exec_copy_fn(arg))
								.then(argument("chunkDistance", IntegerArgumentType.integer(1, 8))
										.executes(arg -> exec_copy_fn_cd(arg)))))
				.then(literal("paste")
						.then(argument("fileName", StringArgumentType.word())
								.executes(arg -> exec_paste_fn(arg))
								.then(argument("chunkDistance", IntegerArgumentType.integer(1, 8))
										.executes(arg -> exec_paste_fn_cd(arg)))))
				.then(literal("clear")
						.then(argument("chunkDistance", IntegerArgumentType.integer(1, 8))
								.executes(arg -> exec_clear_cd(arg))))
				.then(literal("fill")
						.then(argument("chunkDistance", IntegerArgumentType.integer(1, 8))
								.then(argument("blockState", BlockStateArgumentType.blockState())
										.executes(arg -> exec_fill_cd_state(arg)))))
				.build();
		
		//add child
		rootNode.addChild(ccRootNode);
	}
	// ==================================================
	private static int exec_copy_fn(CommandContext<ServerCommandSource> arg)
	{
		copy(arg.getArgument("fileName", String.class), 8, arg.getSource());
		return 1;
	}
	// --------------------------------------------------
	private static int exec_copy_fn_cd(CommandContext<ServerCommandSource> arg)
	{
		copy(arg.getArgument("fileName", String.class), arg.getArgument("chunkDistance", Integer.class), arg.getSource());
		return 1;
	}
	// --------------------------------------------------
	private static int exec_paste_fn(CommandContext<ServerCommandSource> arg)
	{
		paste(arg.getArgument("fileName", String.class), 8, arg.getSource());
		return 1;
	}
	// --------------------------------------------------
	private static int exec_paste_fn_cd(CommandContext<ServerCommandSource> arg)
	{
		paste(arg.getArgument("fileName", String.class), arg.getArgument("chunkDistance", Integer.class), arg.getSource());
		return 1;
	}
	// --------------------------------------------------
	private static int exec_clear_cd(CommandContext<ServerCommandSource> arg)
	{
		clear(arg.getArgument("chunkDistance", Integer.class), arg.getSource());
		return 1;
	}
	// --------------------------------------------------
	private static int exec_fill_cd_state(CommandContext<ServerCommandSource> arg)
	{
		BlockStateArgument bsa = arg.getArgument("blockState", BlockStateArgument.class);
		fill(arg.getArgument("chunkDistance", Integer.class), bsa.getBlockState(), arg.getSource());
		return 1;
	}
	// ==================================================
	public static void copy(String fileName, int chunkDist, ServerCommandSource src)
	{
		try
		{
			CCUtils.saveLoadedChunksIO(fileName, chunkDist, Tuple.from(src));
			feedback("Copied chunk data to '{$fileName}'.".replace("{$fileName}", fileName), src);
		}
		catch(Exception e) { handleException(e, src); }
	}
	// --------------------------------------------------
	public static void paste(String fileName, int chunkDist, ServerCommandSource src)
	{
		try
		{
			CCUtils.loadLoadedChunksIO(fileName, chunkDist, Tuple.from(src));
			feedback("Pasted chunk data from '{$fileName}'.".replace("{$fileName}", fileName), src);
		}
		catch(Exception e) { handleException(e, src); }
	}
	// --------------------------------------------------
	public static void clear(int chunkDist, ServerCommandSource src)
	{
		try
		{
			BlockState state = Blocks.AIR.getDefaultState();
			CCUtils.fillLoadedChunks(chunkDist, state, Tuple.from(src));
			
			feedback("Filled chunk blocks with '{$blockName}'."
					.replace("{$blockName}", state.getBlock().getName().getString()), src);
		}
		catch(Exception e) { handleException(e, src); }
	}
	// --------------------------------------------------
	public static void fill(int chunkDist, BlockState state, ServerCommandSource src)
	{
		try
		{
			CCUtils.fillLoadedChunks(chunkDist, state, Tuple.from(src));
			
			feedback("Filled chunk blocks with '{$blockName}'."
					.replace("{$blockName}", state.getBlock().getName().getString()), src);
		}
		catch(Exception e) { handleException(e, src); }
	}
	// ==================================================
	private static void handleException(Exception e, ServerCommandSource src)
	{
		String message = getExceptionMessage(e);
		feedback("An Exception was thrown during the operation: {$message}"
				.replace("{$message}", "\n" + message + "\n"), src);
	}
	// --------------------------------------------------
	private static void feedback(String text, ServerCommandSource src)
	{
		text = "[" + ChunkCopy.ModName + "] " + text;
		src.sendFeedback(new LiteralText(text), false);
	}
	// ==================================================
}
