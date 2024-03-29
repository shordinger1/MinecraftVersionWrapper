package shordinger.ModWrapper.migration.wrapper.minecraft.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.init.SoundEvents;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class WrapperItemEgg extends WrapperItem {

    public WrapperItemEgg() {
        this.maxStackSize = 16;
        this.setCreativeTab(CreativeTabs.MATERIALS);
    }

    /**
     * Called when the equipped item is right clicked.
     */
    public ActionResult<TempItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        TempItemStack itemstack = playerIn.getHeldItem(handIn);

        if (!playerIn.capabilities.isCreativeMode) {
            itemstack.shrink(1);
        }

        worldIn.playSound(
            (EntityPlayer) null,
            playerIn.posX,
            playerIn.posY,
            playerIn.posZ,
            SoundEvents.ENTITY_EGG_THROW,
            SoundCategory.PLAYERS,
            0.5F,
            0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

        if (!worldIn.isRemote) {
            EntityEgg entityegg = new EntityEgg(worldIn, playerIn);
            entityegg.shoot(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, 1.5F, 1.0F);
            worldIn.spawnEntity(entityegg);
        }

        playerIn.addStat(StatList.getObjectUseStats(this));
        return new ActionResult<TempItemStack>(EnumActionResult.SUCCESS, itemstack);
    }
}
