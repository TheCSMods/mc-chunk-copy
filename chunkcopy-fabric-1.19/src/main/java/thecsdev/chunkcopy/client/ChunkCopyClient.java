package thecsdev.chunkcopy.client;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import thecsdev.chunkcopy.ChunkCopy;
import thecsdev.chunkcopy.client.command.ChunkCopyClientCommand;
import thecsdev.chunkcopy.command.ChunkCopyCommand;

/**
 * The {@link ChunkCopy} initializer for clients.
 */
public final class ChunkCopyClient extends ChunkCopy implements ClientModInitializer
{
	// ==================================================
	/**
	 * The client-side version of the {@link ChunkCopyCommand}.
	 */
	private ChunkCopyClientCommand Command = null;
	// ==================================================
	@Override
	public void onInitializeClient()
	{
		//register the command
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
		{
			Command = new ChunkCopyClientCommand();
			Command.register(ClientCommandManager.getActiveDispatcher(), registryAccess);
		});
	}
	// --------------------------------------------------
	@Override
	public @Nullable ChunkCopyCommand<?> getCommand() { return Command; }
	// ==================================================
}
