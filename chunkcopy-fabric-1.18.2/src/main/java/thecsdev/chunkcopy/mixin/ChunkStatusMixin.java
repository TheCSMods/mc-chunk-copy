package thecsdev.chunkcopy.mixin;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.datafixers.util.Either;

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import thecsdev.chunkcopy.api.AutoChunkCopy;
import thecsdev.chunkcopy.api.ChunkCopyAPI;

@Mixin(ChunkStatus.class)
public abstract class ChunkStatusMixin
{
	@Inject(method = "runGenerationTask", at = @At("RETURN"))
	public void nounusedchunks_onRunGenerationTask(Executor executor, ServerWorld world, ChunkGenerator generator, StructureManager structureManager, ServerLightingProvider lightingProvider, Function<Chunk, CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> fullChunkConverter, List<Chunk> chunks, boolean bl, CallbackInfoReturnable<Either<Chunk, ChunkHolder.Unloaded>> callback)
	{
		//check if auto-copy is pasting
		if(!AutoChunkCopy.isPasting()) return;
		
		//turn the action into a Runnable task
		Runnable task = () ->
		{
			try
			{
				//get and null check the return value
				@SuppressWarnings("unchecked") //ik what i'm doing
				CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> rValue = (CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>)(Object)callback.getReturnValue();
				if(rValue == null || !rValue.isDone()) return; //do not remove the isDone check, weird deadlocks occur when you do
				
				//get and null check the chunk
				final Chunk chunk = rValue.get().left().orElse(null);
				if(chunk == null) return;
				
				//paste data into the chunk
				final String fileName = AutoChunkCopy.getFileName();
				ChunkCopyAPI.loadChunkDataIO(world, chunk.getPos(), fileName);
			}
			catch(Exception exc) {}
		};
		
		//let the server execute the task once it is able to
		world.getServer().execute(task);
	}
}