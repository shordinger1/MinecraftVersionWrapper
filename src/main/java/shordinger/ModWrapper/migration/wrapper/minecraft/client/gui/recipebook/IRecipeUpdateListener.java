package shordinger.ModWrapper.migration.wrapper.minecraft.client.gui.recipebook;

import java.util.List;

import net.minecraft.item.crafting.IRecipe;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IRecipeUpdateListener {

    void recipesShown(List<IRecipe> recipes);
}
