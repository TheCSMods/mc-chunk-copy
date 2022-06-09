package thecsdev.chunkcopy.server;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import thecsdev.chunkcopy.ChunkCopy;
import thecsdev.chunkcopy.command.ChunkCopyCommand;
import thecsdev.chunkcopy.server.command.ChunkCopyServerCommand;

/**
 * The {@link ChunkCopy} initializer for dedicated servers.
 */
public final class ChunkCopyServer extends ChunkCopy implements DedicatedServerModInitializer
{
	// ==================================================
	/**
	 * The server-side version of the {@link ChunkCopyCommand}.
	 */
	private ChunkCopyServerCommand Command = null;
	// ==================================================
	@Override
	public void onInitializeServer()
	{
		//register the command
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, enviroment) ->
		{
			Command = new ChunkCopyServerCommand();
			Command.register(dispatcher, registryAccess);
		});
	}
	// --------------------------------------------------
	@Override
	public @Nullable ChunkCopyCommand<?> getCommand() { return Command; }
	// ==================================================
}
