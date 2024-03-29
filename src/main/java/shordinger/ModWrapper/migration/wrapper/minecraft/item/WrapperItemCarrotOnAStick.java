package shordinger.ModWrapper.migration.wrapper.minecraft.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class WrapperItemCarrotOnAStick extends WrapperItem {

    public WrapperItemCarrotOnAStick() {
        this.setCreativeTab(CreativeTabs.TRANSPORTATION);
        this.setMaxStackSize(1);
        this.setMaxDamage(25);
    }

    /**
     * Returns True is the item is renderer in full 3D when hold.
     */
    @SideOnly(Side.CLIENT)
    public boolean isFull3D() {
        return true;
    }

    /**
     * Returns true if this item should be rotated by 180 degrees around the Y axis when being held in an entities
     * hands.
     */
    @SideOnly(Side.CLIENT)
    public boolean shouldRotateAroundWhenRendering() {
        return true;
    }

    /**
     * Called when the equipped item is right clicked.
     */
    public ActionResult<TempItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        TempItemStack itemstack = playerIn.getHeldItem(handIn);

        if (worldIn.isRemote) {
            return new ActionResult<TempItemStack>(EnumActionResult.PASS, itemstack);
        } else {
            if (playerIn.isRiding() && playerIn.getRidingEntity() instanceof EntityPig) {
                EntityPig entitypig = (EntityPig) playerIn.getRidingEntity();

                if (itemstack.getMaxDamage() - itemstack.getMetadata() >= 7 && entitypig.boost()) {
                    itemstack.damageItem(7, playerIn);

                    if (itemstack.isEmpty()) {
                        TempItemStack itemstack1 = new TempItemStack(Items.FISHING_ROD);
                        itemstack1.setTagCompound(itemstack.getTagCompound());
                        return new ActionResult<TempItemStack>(EnumActionResult.SUCCESS, itemstack1);
                    }

                    return new ActionResult<TempItemStack>(EnumActionResult.SUCCESS, itemstack);
                }
            }

            playerIn.addStat(StatList.getObjectUseStats(this));
            return new ActionResult<TempItemStack>(EnumActionResult.PASS, itemstack);
        }
    }
}
