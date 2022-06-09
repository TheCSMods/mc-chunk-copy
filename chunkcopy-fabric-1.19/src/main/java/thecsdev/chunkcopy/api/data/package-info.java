/**
 * <b>Important: Please wrap API calls to {@link ChunkDataBlock}s in a
 * {@link MinecraftServer#execute(Runnable)} to avoid concurrency issues
 * (crashes) that occur while copying and pasting chunk data
 * using {@link ChunkDataBlock}s.</b><br/>
 * <br/>
 * Basically, Minecraft is a multi-threaded game. While copying and pasting chunk data,
 * it is extremely likely that another Minecraft's thread will attempt to access
 * {@link PalettedContainer}s, blocks, entities, and other places where block and entity data are stored,
 * while the {@link ChunkDataBlock}s are copying and pasting chunks. This always results in concurrent
 * modification problems, which cause the game to crash every time.<br/>
 * <br/>
 * To resolve this issue, simply use the {@link MinecraftServer#execute(Runnable)} method
 * provided by the game itself to avoid such issues.</br>
 * <br/>
 * Example:<br/>
 * <pre>
 * {@code
 * ChunkDataBlock chunkData = ...;
 * ServerWorld    world     = ...;
 * ChunkPos       chunkPos  = ...;
 * world.getServer().execute(() -> chunkData.pasteChunkData(world, chunkPos));
 * }
 * </pre>
 * The {@link ChunkCopyAPI} and {@link ChunkData} already take care of this for you, so there is
 * no need to use {@link MinecraftServer#execute(Runnable)} when calling those methods.<br/>
 * <br/>
 */
package thecsdev.chunkcopy.api.data;