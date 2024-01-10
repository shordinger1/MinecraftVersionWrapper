package shordinger.ModWrapper.migration.wrapper.minecraft.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class WrapperItemEmptyMap extends WrapperItemMapBase {

    protected WrapperItemEmptyMap() {
        this.setCreativeTab(CreativeTabs.MISC);
    }

    /**
     * Called when the equipped item is right clicked.
     */
    public ActionResult<TempItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        TempItemStack itemstack = WrapperItemMap.setupNewMap(worldIn, playerIn.posX, playerIn.posZ, (byte) 0, true, false);
        TempItemStack itemstack1 = playerIn.getHeldItem(handIn);
        itemstack1.shrink(1);

        if (itemstack1.isEmpty()) {
            return new ActionResult<TempItemStack>(EnumActionResult.SUCCESS, itemstack);
        } else {
            if (!playerIn.inventory.addItemStackToInventory(itemstack.copy())) {
                playerIn.dropItem(itemstack, false);
            }

            playerIn.addStat(StatList.getObjectUseStats(this));
            return new ActionResult<TempItemStack>(EnumActionResult.SUCCESS, itemstack1);
        }
    }
}
