package thecsdev.chunkcopy.client.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import thecsdev.chunkcopy.api.AutoChunkCopy;
import thecsdev.chunkcopy.api.ChunkCopyAPI;

@Mixin(targets = "net/minecraft/client/world/ClientChunkManager$ClientChunkMap")
public abstract class ClientChunkMapMixin
{
	// ==================================================
	private ClientChunkManager chunkCopy_chunkManager;
	// ==================================================
	@Inject(method = "<init>", at = @At("TAIL"))
	private void construct(ClientChunkManager clientChunkManager, int radius, CallbackInfo ci)
	{
		chunkCopy_chunkManager = clientChunkManager;
	}
	
	@Inject(method = "set", at = @At("TAIL"))
	protected void set(int index, @Nullable WorldChunk chunk, CallbackInfo callback)
	{
		//check if auto-copy is copying
		if(!AutoChunkCopy.isCopying()) return;
		
		//ignore null chunks
		if(chunk == null) return;
		
		//save chunk
		try
		{
			World world = (World) chunkCopy_chunkManager.getWorld();
			ChunkPos chunkPos = chunk.getPos();
			String fileName = AutoChunkCopy.getFileName();
			
			ChunkCopyAPI.saveChunkDataIO(world, chunkPos, fileName);
		}
		catch (Throwable e) {}
	}
	// ==================================================
}
