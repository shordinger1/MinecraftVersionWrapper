package shordinger.ModWrapper.migration.wrapper.minecraft.client.gui.spectator;

import net.minecraft.util.text.ITextComponent;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface ISpectatorMenuObject {

    void selectItem(SpectatorMenu menu);

    ITextComponent getSpectatorName();

    void renderIcon(float brightness, int alpha);

    boolean isEnabled();
}
