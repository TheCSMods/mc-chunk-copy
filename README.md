## Chunk Copy
Chunk Copy is a Fabric Minecraft mod used for downloading multiplayer worlds by copying their chunks and pasting them into singleplayer worlds. This mod adds a client-side command `/chunkcopy` that is used to copy and paste world chunks. All copied world chunks are stored in the `mods/chunkcopy` directory.

## How to use the mod?
This mod adds a client-side command `/chunkcopy` that is used to copy and paste chunk data. Chunks can be copied at any time in both singleplayer and multiplayer worlds. Pasting chunks, on the other hand, can only be done in singleplayer worlds that have cheats enabled.<br/>
<br/>
<i><b>Please be careful when executing the mod commands, as they cannot be undone. This is why cheats are required to be enabled before pasting, to avoid accidentally damaging casual survival worlds.</i></b><br/>
<br/>
#### Command syntaxes:
* /chunkcopy (copy/paste) &lt;file_name&gt; [chunk_distance]
* /chunkcopy fill &lt;chunk_distance&gt; &lt;block&gt;
* /chunkcopy clear &lt;chunk_distance&gt;
<br/>
<ins><b>file_name</b></ins> - This is the name of the directory where the chunk data will be saved to and loaded from. All copied chunk data is stored in `[.minecraft]/mods/chunkcopy/[file_name]`.<br/>
<ins><b>chunk_distance</b></ins> - This is kind of like render distance, as it defines how close the chunk has to be in order to be copied/pasted. Set this to `0` or `1` to copy or paste a single chunk in which the player is located in. Avoid using high values and high render distances as they will cause lots of lag and may even freeze and crash the game.<br/>
<ins><b>block</b></ins> - The desired block type.<br/>
<br/>
<b>To copy chunks</b>, log in on the desired Minecraft server, and go to the area that you wish to copy. Make sure that the chunks you wish to copy are loaded client-side. Then execute the copying command (ex. `/chunkcopy copy Test`).<br/>
<b>To paste chunks</b>, go to a singleplayer world with cheats enabled (it is preferred that you create a new empty world with the void preset), then go to the exact same coordinates of where the chunks are located on the server, and execute the pasting command (ex. `/chunkcopy paste Test`).

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
