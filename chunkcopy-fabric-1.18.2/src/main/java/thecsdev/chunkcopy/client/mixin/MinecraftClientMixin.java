package thecsdev.chunkcopy.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import thecsdev.chunkcopy.api.AutoChunkCopy;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin
{
	// ==================================================
	//@Accessor("currentScreen") public abstract Screen getCurrentScreen();
	//@Accessor("isConnectedToServer") public abstract boolean isConnectedToServer();
	// --------------------------------------------------
	/**
	 * Stop {@link AutoChunkCopy} when disconnecting.
	 */
	@Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("RETURN"))
	public void disconnect(Screen screen, CallbackInfo callback)
	{
		AutoChunkCopy.stop();
	}
	// ==================================================
}
