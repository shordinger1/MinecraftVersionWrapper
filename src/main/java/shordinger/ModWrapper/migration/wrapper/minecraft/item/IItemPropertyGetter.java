package shordinger.ModWrapper.migration.wrapper.minecraft.item;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IItemPropertyGetter {

    @SideOnly(Side.CLIENT)
    float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn);
}
