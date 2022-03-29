//Edit: never-mind, i took care of it myself. should be safe now.

/*
 * <b>Important: Please wrap API calls in a {@link MinecraftServer#execute(Runnable)}
 * to avoid concurrency issues (crashes) that occur while copying and pasting chunk data.</b><br/>
 * <br/>
 * Basically, Minecraft is a multi-threaded game. While copying and pasting chunk data,
 * it is extremely likely that another Minecraft's thread will attempt to access
 * {@link PalettedContainer}s, blocks, entities, and other places where block and entity data are stored,
 * while the {@link ChunkCopyAPI} is copying and pasting chunks. This always results in concurrent
 * modification problems, which cause the game to crash every time.<br/>
 * <br/>
 * To resolve this issue, simply use the {@link MinecraftServer#execute(Runnable)} method
 * provided by the game itself to avoid such issues.</br>
 * </br>
 * Example 1:<br/>
 * <pre>
 * {@code
 * byte[]      chunkData = ...;
 * ServerWorld world     = ...;
 * ChunkPos    chunkPos  = ...;
 * world.getServer().execute(() -> ChunkCopyAPI.pasteChunkData(data, world, chunkPos, true));
 * }
 * </pre>
 * Example 2:<br/>
 * <pre>
 * {@code
 * ChunkData chunkData = ...;
 * MinecraftServer srv = ...;
 * srv.execute(() -> chunkData.pasteData(world, chunkPos));
 * }
 * </pre>
 */
package thecsdev.chunkcopy.api;