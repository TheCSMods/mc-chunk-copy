package thecsdev.chunkcopy.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import thecsdev.chunkcopy.ChunkCopy;

/**
 * The {@link ChunkCopy} command.
 */
public abstract class ChunkCopyCommand<CS extends CommandSource>
{
	// ==================================================
	protected static final IntegerArgumentType ChunkDistArg = IntegerArgumentType.integer(1, 8);
	// ==================================================
	public void register(CommandDispatcher<CS> dispatcher)
	{
		dispatcher.register(literal(getCommandName()).requires(src -> canCopy(src) || canPaste(src))
				//copying
				.then(literal("copy").requires(arg -> canCopy(arg))
						.then(argument("fileName", StringArgumentType.word())
								.executes(arg -> exec_copy_fileName(arg))
								.then(argument("chunkDistance", ChunkDistArg)
										.executes(arg -> exec_copy_fileName_chunkDistance(arg)))))
				
				//pasting
				.then(literal("paste").requires(arg -> canPaste(arg))
						.then(argument("fileName", StringArgumentType.word())
								.executes(arg -> exec_paste_fileName(arg))
								.then(argument("chunkDistance", ChunkDistArg)
										.executes(arg -> exec_paste_fileName_chunkDistance(arg)))))
				
				//filling
				.then(literal("fill").requires(arg -> canPaste(arg))
						.then(argument("chunkDistance", ChunkDistArg)
								.then(argument("blockState", BlockStateArgumentType.blockState())
										.executes(arg -> exec_fill_chunkDistance_block(arg)))))
				
				//clearing
				.then(literal("clear").requires(arg -> canPaste(arg))
						.then(argument("chunkDistance", ChunkDistArg)
								.executes(arg -> exec_clear_chunkDistance(arg))))
				);
	}
	// --------------------------------------------------
	private int exec_copy_fileName(CommandContext<CS> cs)
	{
		copy(cs.getSource(), cs.getArgument("fileName", String.class), 8);
		return 1;
	}
	
	private int exec_copy_fileName_chunkDistance(CommandContext<CS> cs)
	{
		copy(cs.getSource(), cs.getArgument("fileName", String.class), cs.getArgument("chunkDistance", Integer.class));
		return 1;
	}
	// ---------------
	private int exec_paste_fileName(CommandContext<CS> cs)
	{
		paste(cs.getSource(), cs.getArgument("fileName", String.class), 8);
		return 1;
	}
	
	private int exec_paste_fileName_chunkDistance(CommandContext<CS> cs)
	{
		paste(cs.getSource(), cs.getArgument("fileName", String.class), cs.getArgument("chunkDistance", Integer.class));
		return 1;
	}
	// ---------------
	private int exec_fill_chunkDistance_block(CommandContext<CS> cs)
	{
		fill(cs.getSource(), cs.getArgument("chunkDistance", Integer.class),
				cs.getArgument("blockState", BlockStateArgument.class).getBlockState());
		return 1;
	}
	// ---------------
	private int exec_clear_chunkDistance(CommandContext<CS> cs)
	{
		fill(cs.getSource(), cs.getArgument("chunkDistance", Integer.class),
				Blocks.AIR.getDefaultState());
		return 1;
	}
	// ==================================================
	/**
	 * The name of the command. <b>Must be constant!</b>
	 */
	public abstract String getCommandName();
	// --------------------------------------------------
	/**
	 * Returns true if a {@link CommandSource} can
	 * execute copying commands.
	 * @param commandSource The command executor.
	 */
	protected abstract boolean canCopy(CS commandSource);
	
	/**
	 * Returns true if a {@link CommandSource} can
	 * execute pasting commands (ex. paste, fill, clear).
	 * @param commandSource The command executor.
	 */
	protected abstract boolean canPaste(CS commandSource);
	// --------------------------------------------------
	protected abstract void copy(CS commandSource, String fileName, int chunkDistance);
	protected abstract void paste(CS commandSource, String fileName, int chunkDistance);
	protected abstract void fill(CS commandSource, int chunkDistance, BlockState block);
	protected final void clear(CS commandSource, int chunkDistance)
		{ fill(commandSource, chunkDistance, Blocks.AIR.getDefaultState()); }
	// ==================================================
	/**
	 * Creates a literal argument builder.
	 *
	 * @param name the literal name
	 * @return the created argument builder
	 */
	public LiteralArgumentBuilder<CS> literal(String name) { return LiteralArgumentBuilder.literal(name); }
	// --------------------------------------------------
	/**
	 * Creates a required argument builder.
	 *
	 * @param name the name of the argument
	 * @param type the type of the argument
	 * @param <CS>  the type of the parsed argument value
	 * @return the created argument builder
	 */
	public <ARG> RequiredArgumentBuilder<CS, ARG> argument(String name, ArgumentType<ARG> type)
	{
		return RequiredArgumentBuilder.argument(name, type);
	}
	// ==================================================
	protected static String getExceptionMessage(Throwable e)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(e.getClass().getCanonicalName() + ": " + e.getMessage() + "\n");
		for (StackTraceElement ste : e.getStackTrace())
		{
			if(!ste.getClassName().contains("thecsdev")) continue;
			sb.append(ste.toString() + "\n");
			break;
		}
		return sb.toString().trim();
	}
	// ==================================================
}
