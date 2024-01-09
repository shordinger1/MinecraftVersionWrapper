package shordinger.ModWrapper.migration.wrapper.minecraft.client.gui.spectator;

import java.util.List;

import net.minecraft.util.text.ITextComponent;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface ISpectatorMenuView {

    List<ISpectatorMenuObject> getItems();

    ITextComponent getPrompt();
}
