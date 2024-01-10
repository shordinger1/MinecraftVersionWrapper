package shordinger.ModWrapper.migration.wrapper.minecraft.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.BlockDispenser;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class WrapperItemShield extends WrapperItem {

    public WrapperItemShield() {
        this.maxStackSize = 1;
        this.setCreativeTab(CreativeTabs.COMBAT);
        this.setMaxDamage(336);
        this.addPropertyOverride(new ResourceLocation("blocking"), new IItemPropertyGetter() {

            @SideOnly(Side.CLIENT)
            public float apply(TempItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
                return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F
                    : 0.0F;
            }
        });
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, WrapperItemArmor.DISPENSER_BEHAVIOR);
    }

    public String getItemStackDisplayName(TempItemStack stack) {
        if (stack.getSubCompound("BlockEntityTag") != null) {
            EnumDyeColor enumdyecolor = TileEntityBanner.getColor(stack);
            return I18n.translateToLocal("item.shield." + enumdyecolor.getUnlocalizedName() + ".name");
        } else {
            return I18n.translateToLocal("item.shield.name");
        }
    }

    /**
     * allows items to add custom lines of information to the mouseover description
     */
    @SideOnly(Side.CLIENT)
    public void addInformation(TempItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        WrapperItemBanner.appendHoverTextFromTileEntityTag(stack, tooltip);
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    public EnumAction getItemUseAction(TempItemStack stack) {
        return EnumAction.BLOCK;
    }

    /**
     * How long it takes to use or consume an item
     */
    public int getMaxItemUseDuration(TempItemStack stack) {
        return 72000;
    }

    /**
     * Called when the equipped item is right clicked.
     */
    public ActionResult<TempItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        TempItemStack itemstack = playerIn.getHeldItem(handIn);
        playerIn.setActiveHand(handIn);
        return new ActionResult<TempItemStack>(EnumActionResult.SUCCESS, itemstack);
    }

    /**
     * Return whether this item is repairable in an anvil.
     *
     * @param toRepair the {@code ItemStack} being repaired
     * @param repair   the {@code ItemStack} being used to perform the repair
     */
    public boolean getIsRepairable(TempItemStack toRepair, TempItemStack repair) {
        return repair.getItem() == WrapperItem.getItemFromBlock(Blocks.PLANKS) ? false : super.getIsRepairable(toRepair, repair);
    }
}
