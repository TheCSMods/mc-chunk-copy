package thecsdev.chunkcopy.command.argument;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import thecsdev.chunkcopy.api.ChunkCopyAPI;

/**
 * An {@link ArgumentType}&lt;{@link String}&gt; that will suggest picking
 * from a list of copied chunks.
 */
public final class CopiedChunksArgumentType implements ArgumentType<String>
{
	// ==================================================
	public static CopiedChunksArgumentType forCopying() { return new CopiedChunksArgumentType(false); }
	public static CopiedChunksArgumentType forPasting() { return new CopiedChunksArgumentType(true); }
	// ==================================================
	public final boolean showDimensions;
	protected CopiedChunksArgumentType(boolean showDimensions) { this.showDimensions = showDimensions; }
	// ==================================================
	@Override
	public String parse(StringReader reader) throws CommandSyntaxException
	{
		if(!showDimensions) return reader.readUnquotedString();
		else return reader.readString();
	}
	// --------------------------------------------------
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
	{
		try
		{
			//iterate existing directories and list them
			File sfd = ChunkCopyAPI.getSaveFilesDirectory();
			if(sfd.exists())
			for (File dir : sfd.listFiles())
			{
				//skip files
				if(!dir.isDirectory()) continue;
				
				//suggest directory
				if(!showDimensions) builder.suggest(dir.getName());
				else builder.suggest("\"" + dir.getName() + "\"");
				
				//suggest dimensions
				if(showDimensions)
					for(File subDir : dir.listFiles())
					{
						//skip files
						if(!subDir.isDirectory()) continue;
						
						for(File subSubDir : subDir.listFiles())
						{
							//skip files
							if(!subSubDir.isDirectory()) continue;
							
							//suggest directory
							builder.suggest("\"" + dir.getName() + "/" + subDir.getName() + "/" + subSubDir.getName() + "\"");
						}
					}
			}
		}
		catch (Exception e) {}
		
		//return
		return builder.buildFuture();
	}
	// ==================================================
}
