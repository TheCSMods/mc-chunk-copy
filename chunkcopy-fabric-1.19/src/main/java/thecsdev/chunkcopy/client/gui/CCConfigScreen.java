package thecsdev.chunkcopy.client.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class CCConfigScreen extends Screen
{
	// ==================================================
	protected CCConfigScreen() { super(Text.translatable("chunkcopy.title")); }
	// --------------------------------------------------
	@Override public boolean shouldPause() { return true; }
	@Override public boolean shouldCloseOnEsc() { return true; }
	// ==================================================
}