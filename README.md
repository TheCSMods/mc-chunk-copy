## Chunk Copy
Chunk Copy is a Fabric Minecraft mod used for downloading multiplayer worlds by copying their chunks and pasting them into singleplayer worlds. This mod adds a client-side command `/chunkcopy` that is used to copy and paste world chunks. All copied world chunks are stored in the `mods/chunkcopy` directory.  
  
- **Warning:** This mod does not feature a way to undo and redo changes made to chunks. Please be careful as any wrong moves made cannot be undone.

## Command syntaxes:
- /chunkcopy (copy/paste) &lt;file_name&gt; [chunk_distance]
- /chunkcopy fill &lt;chunk_distance&gt; &lt;block&gt;
- /chunkcopy clear &lt;chunk_distance&gt;
  
<ins><b>file_name</b></ins> - This is the name of the directory where the chunk data will be saved to and loaded from. All copied chunks are stored in the <i>mods/chunkcopy/[file_name]/</i> directory.<br/>
<ins><b>chunk_distance</b></ins> - This is kind of like render distance. It defines how close the chunk has to be in order to be copied or pasted. Set this to `1` to copy or paste a single chunk in which you are located in. By default, this value depends on the render distance. Because large chunk distances cause massive lag and the game to freeze due to so many blocks being affected all at once, this value is cannot go over 8.<br/>
<ins><b>block</b></ins> - The desired block type.<br/>

## Some extra info
To copy chunks, log in on the desired Minecraft server, and go to the area that you wish to copy. Make sure that the chunks you wish to copy are loaded. Then execute the copying command (ex. /chunkcopy copy Test).  
  
To paste chunks, go to a singleplayer world that has cheats enabled (it is preferred that you create a new empty superflat world with the void preset), then go to the exact same coordinates of where the chunks you copied are located on the server, and execute the pasting command (ex. /chunkcopy paste Test).  
  
Please note that due to the way Minecraft works, it is impossible to write a client-side mod that is able to obtain and copy data such as worldgen settings (ie. the seed, world type, and so on) and contents of containers (such as chests, hoppers, and so on). Those things are handled server-side, which is why the client is not able to obtain such information. When it comes to containers such as chests, the only way to see their contents is to manually open them one by one. You may also experience lag spikes while copying and pasting chunks.  
  
Here is a short YouTube clip showing how the mod works (click the image):<br/>
[![Chunk Copy demonstration](https://img.youtube.com/vi/mg6rYM5OuMg/0.jpg)](https://www.youtube.com/watch?v=mg6rYM5OuMg)

## Some questions and answers

#### What exactly does this mod copy?
* Only the blocks. This mod does not copy biome data, worldgen data, entities, container contents (aka items in chests etc.), and so on.

#### Why can this mod not copy chest contents?
* Because it is impossible to do that. Container content (aka inventory) management is handled server-side. This means that the client does not have access to it, and can therefore not copy it. The only way of actually copying container contents (aka chests for ex.) would be to manually go through and open (right click) every single one of them and copy their contents that way.

#### Why can this mod not copy worldgen settings?
* Because that too is handled server-side, and is therefore impossible to do. So the only way of obtaining the world seed would be to execute the `/seed` command, which may require operator permissions.

#### Why can this mod not copy entities?
* While it is possible to implement this feature, I did not feel like doing it. After all, mobs walk around all over the place, so how can you know which ones were where when pasting the same chunks multiple times, and which mobs to kill and replace.

#### Where are the copied chunks stored?
* In this directory: `[.minecraft]/mods/chunkcopy/`
