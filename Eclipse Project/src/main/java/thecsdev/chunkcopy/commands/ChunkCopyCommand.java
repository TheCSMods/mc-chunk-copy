package thecsdev.chunkcopy.commands;

import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.text.TranslatableText;
import thecsdev.chunkcopy.ChunkCopy;
import thecsdev.chunkcopy.io.CCUtils;

import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.argument;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

public final class ChunkCopyCommand
{
	// ==================================================
	public static int PERMISSION_LEVEL = 0;
	// ==================================================
	public void register(CommandDispatcher<FabricClientCommandSource> dispatcher)
	{
		dispatcher.register(literal("chunkcopy").requires(src -> src.hasPermissionLevel(PERMISSION_LEVEL))
				.then(literal("copy")
						.then(argument("fileName", StringArgumentType.word())
								.executes(arg -> exec_copy_fn(arg))
								.then(argument("chunkDistance", IntegerArgumentType.integer(0, 8))
										.executes(arg -> exec_copy_fn_cd(arg)))))
				//pasting is only available in single player and if cheats are on
				.then(literal("paste").requires(src -> src.hasPermissionLevel(4) && ChunkCopy.MC.isInSingleplayer())
						.then(argument("fileName", StringArgumentType.word())
								.executes(arg -> exec_paste_fn(arg))
								.then(argument("chunkDistance", IntegerArgumentType.integer(0, 8))
										.executes(arg -> exec_paste_fn_cd(arg))))));
	}
	// ==================================================
	private static int exec_copy_fn(CommandContext<FabricClientCommandSource> arg)
	{
		copy(arg.getArgument("fileName", String.class), (int) ChunkCopy.MC.worldRenderer.getViewDistance() / 2);
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
		paste(arg.getArgument("fileName", String.class), (int) ChunkCopy.MC.worldRenderer.getViewDistance() / 2);
		return 1;
	}
	// --------------------------------------------------
	private static int exec_paste_fn_cd(CommandContext<FabricClientCommandSource> arg)
	{
		paste(arg.getArgument("fileName", String.class), arg.getArgument("chunkDistance", Integer.class));
		return 1;
	}
	// ==================================================
	private static void copy(String fileName, int chunkDist)
	{
		try { CCUtils.saveLoadedChunksIO(fileName, chunkDist); }
		catch(Exception e)
		{
			String message = getExceptionMessage(e);
			ChunkCopy.printChat(new TranslatableText("thecsdev.chunkcopy.exception")
					.getString().replace("{$message}", "\n" + message + "\n"));
		}
	}
	// --------------------------------------------------
	private static void paste(String fileName, int chunkDist)
	{
		try { CCUtils.loadLoadedChunksIO(fileName, chunkDist); }
		catch(Exception e)
		{
			String message = getExceptionMessage(e);
			ChunkCopy.printChat(new TranslatableText("thecsdev.chunkcopy.exception")
					.getString().replace("{$message}", "\n" + message + "\n"));
		}
	}
	// --------------------------------------------------
	private static String getExceptionMessage(Throwable e)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(e.getClass().getCanonicalName() + ": " + e.getMessage() + "\n");
		for (StackTraceElement ste : e.getStackTrace())
		{
			if(!ste.getClassName().contains("thecsdev")) continue;
			sb.append(ste.toString() + "\n");
		}
		return sb.toString().trim();
	}
	// ==================================================
}
