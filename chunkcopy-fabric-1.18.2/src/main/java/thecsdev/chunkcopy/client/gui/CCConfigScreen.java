package thecsdev.chunkcopy.client.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.TranslatableText;

public final class CCConfigScreen extends Screen
{
	// ==================================================
	protected CCConfigScreen() { super(new TranslatableText("chunkcopy.title")); }
	// --------------------------------------------------
	@Override public boolean shouldPause() { return true; }
	@Override public boolean shouldCloseOnEsc() { return true; }
	// ==================================================
}