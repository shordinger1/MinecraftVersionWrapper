package shordinger.ModWrapper.migration.wrapper.minecraft.client.renderer;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface ItemMeshDefinition {

    ModelResourceLocation getModelLocation(ItemStack stack);
}
