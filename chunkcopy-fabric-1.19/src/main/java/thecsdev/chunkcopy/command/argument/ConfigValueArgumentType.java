package thecsdev.chunkcopy.command.argument;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.util.Identifier;
import thecsdev.chunkcopy.api.config.ChunkCopyConfig;
import thecsdev.chunkcopy.api.config.ConfigKey;

/**
 * An {@link ArgumentType}&lt;{@link String}&gt; that will suggest picking
 * appropriate values for {@link ConfigKeyArgumentType}.
 */
@Deprecated
public final class ConfigValueArgumentType implements ArgumentType<String>
{
	// ==================================================
	protected ConfigValueArgumentType() {}
	public static ConfigValueArgumentType configStringValue() { return new ConfigValueArgumentType(); }
	// ==================================================
	@Override
	public String parse(StringReader reader) throws CommandSyntaxException { return reader.readString(); }
	// --------------------------------------------------
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
	{
		try
		{
			Identifier keyId = context.getArgument("key", Identifier.class);
			if(keyId == null) throw new Exception();
			
			ConfigKey<?> key = ChunkCopyConfig.getKeyByName(keyId);
			return key.argumentType.listSuggestions(context, builder);
		}
		catch(Exception e) { return ArgumentType.super.listSuggestions(context, builder); }
	}
	// ==================================================
}
