package thecsdev.chunkcopy.command.argument;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.util.Identifier;
import thecsdev.chunkcopy.api.config.ChunkCopyConfig;
import thecsdev.chunkcopy.api.config.ConfigKey;

/**
 * An {@link ArgumentType}&lt;{@link Identifier}&gt; that will suggest picking
 * a property from a list of {@link ChunkCopyConfig#KEYS}.
 */
public final class ConfigKeyArgumentType implements ArgumentType<Identifier>
{
	// ==================================================
	private final static IdentifierArgumentType IAT = IdentifierArgumentType.identifier();
	// ==================================================
	protected ConfigKeyArgumentType() {}
	public static ConfigKeyArgumentType configKeyId() { return new ConfigKeyArgumentType(); }
	// ==================================================
	@Override
	public Identifier parse(StringReader reader) throws CommandSyntaxException { return IAT.parse(reader); }
	// --------------------------------------------------
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
	{
		//suggest properties
		for (ConfigKey<?> configKey : ChunkCopyConfig.KEYS)
			builder.suggest(configKey.keyName.toString());
		
		//return build
		return builder.buildFuture();
	}
	// ==================================================
}
