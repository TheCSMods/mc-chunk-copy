package thecsdev.chunkcopy.server;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import thecsdev.chunkcopy.ChunkCopy;

@Deprecated
@Environment(EnvType.SERVER)
public class ChunkCopyServer implements DedicatedServerModInitializer
{
	// ==================================================
	protected static MinecraftServer Server = null;
	// ==================================================
	@Override
	public void onInitializeServer()
	{
		//init
		ChunkCopy.initEnviroment(EnvType.SERVER);
		
		//register handlers
		ServerLifecycleEvents.SERVER_STARTED.register(s -> Server = s);
		ServerLifecycleEvents.SERVER_STOPPED.register(s -> { if(s == Server) Server = null; });
		CommandRegistrationCallback.EVENT.register(new ChunkCopyCommandS()::register);
	}
	// ==================================================
	@Nullable
	public static MinecraftServer getServer() { return Server; }
	// ==================================================
}
