package shordinger.ModWrapper.migration.wrapper.minecraft.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.world.World;

public class WrapperItemSpectralArrow extends WrapperItemArrow {

    public EntityArrow createArrow(World worldIn, TempItemStack stack, EntityLivingBase shooter) {
        return new EntitySpectralArrow(worldIn, shooter);
    }
}
