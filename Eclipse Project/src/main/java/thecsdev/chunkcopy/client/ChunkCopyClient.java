package thecsdev.chunkcopy.client;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import thecsdev.chunkcopy.ChunkCopy;

@Environment(EnvType.CLIENT)
public class ChunkCopyClient implements ClientModInitializer
{
	// ==================================================
	protected static MinecraftClient Client;
	// ==================================================
	@Override
	public void onInitializeClient()
	{
		//init
		ChunkCopy.initEnviroment(EnvType.CLIENT);
		
		//reg
		Client = MinecraftClient.getInstance();
		new ChunkCopyCommandC().register(ClientCommandManager.DISPATCHER);
	}
	// ==================================================
	@Nullable
	public static MinecraftClient getClient() { return Client; }
	// ==================================================
	public static void printChatT(String translationKey) { printChat(new TranslatableText(translationKey)); }
	// --------------------------------------------------
	public static void printChat(Text text) { printChat(text.getString()); }
	// --------------------------------------------------
	public static void printChat(String rawText)
	{
		rawText = "["+ChunkCopy.ModName+"] " + rawText;
		Client.inGameHud.getChatHud().addMessage(new LiteralText(rawText));
		ChunkCopy.LOGGER.info(rawText);
	}
	// ==================================================
}
