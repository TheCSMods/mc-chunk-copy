package thecsdev.chunkcopy;

import java.io.File;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import thecsdev.chunkcopy.commands.ChunkCopyCommand;

/**
 * The Fabric mod-loader entry point for this mod. 
 */
public final class ChunkCopy implements ClientModInitializer
{
	// ==================================================
	public static final MinecraftClient MC = MinecraftClient.getInstance();
	public static final Logger LOGGER = LoggerFactory.getLogger(getModID());
	// ------------------------------------------------------
	public static final String ModName = "Chunk Copy";
	public static final String ModID   = "chunkcopy";
	// ==================================================
	@Override
	public void onInitializeClient()
	{
		//register commands
		new ChunkCopyCommand().register(ClientCommandManager.DISPATCHER);
	}
	// ==================================================
	public static void printChat(Text text) { printChat(text.getString()); }
	
	public static void printChat(String rawText)
	{
		rawText = "["+ModName+"] " + rawText;
		MC.inGameHud.getChatHud().addMessage(new LiteralText(rawText));
		LOGGER.info(rawText);
	}
	
	public static void printChatT(String translationKey) { printChat(new TranslatableText(translationKey)); }
	// ==================================================
	public static File getModDirectory()
	{
		return new File(MC.runDirectory.getAbsolutePath() + "/mods/" + ModID + "/");
	}
	// --------------------------------------------------
	public static String getModID() { return ModID; }
	// --------------------------------------------------
	public static boolean traceHasChunkCopy()
	{
		String pkg = ChunkCopy.class.getPackageName();
		for (StackTraceElement ste : Thread.currentThread().getStackTrace())
		{
			if(ste.getClassName().contains(pkg))
				return true;
		}
		return false;
	}
	// ==================================================
}
