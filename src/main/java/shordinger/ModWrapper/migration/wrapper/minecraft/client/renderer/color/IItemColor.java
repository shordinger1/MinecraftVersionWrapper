package shordinger.ModWrapper.migration.wrapper.minecraft.client.renderer.color;

import net.minecraft.item.ItemStack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IItemColor {

    int colorMultiplier(ItemStack stack, int tintIndex);
}
