package shordinger.ModWrapper.migration.wrapper.minecraft.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionUtils;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

import com.google.common.base.Predicate;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.RayTraceResult;

public class WrapperItemGlassBottle extends WrapperItem {

    public WrapperItemGlassBottle() {
        this.setCreativeTab(CreativeTabs.BREWING);
    }

    /**
     * Called when the equipped item is right clicked.
     */
    public ActionResult<TempItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        List<EntityAreaEffectCloud> list = worldIn.<EntityAreaEffectCloud>getEntitiesWithinAABB(
            EntityAreaEffectCloud.class,
            playerIn.getEntityBoundingBox()
                .grow(2.0D),
            new Predicate<EntityAreaEffectCloud>() {

                public boolean apply(@Nullable EntityAreaEffectCloud p_apply_1_) {
                    return p_apply_1_ != null && p_apply_1_.isEntityAlive()
                        && p_apply_1_.getOwner() instanceof EntityDragon;
                }
            });
        TempItemStack itemstack = playerIn.getHeldItem(handIn);

        if (!list.isEmpty()) {
            EntityAreaEffectCloud entityareaeffectcloud = list.get(0);
            entityareaeffectcloud.setRadius(entityareaeffectcloud.getRadius() - 0.5F);
            worldIn.playSound(
                (EntityPlayer) null,
                playerIn.posX,
                playerIn.posY,
                playerIn.posZ,
                SoundEvents.ITEM_BOTTLE_FILL_DRAGONBREATH,
                SoundCategory.NEUTRAL,
                1.0F,
                1.0F);
            return new ActionResult(
                EnumActionResult.SUCCESS,
                this.turnBottleIntoItem(itemstack, playerIn, new TempItemStack(Items.DRAGON_BREATH)));
        } else {
            RayTraceResult raytraceresult = this.rayTrace(worldIn, playerIn, true);

            if (raytraceresult == null) {
                return new ActionResult(EnumActionResult.PASS, itemstack);
            } else {
                if (raytraceresult.typeOfHit == RayTraceResult.Type.BLOCK) {
                    BlockPos blockpos = raytraceresult.getBlockPos();

                    if (!worldIn.isBlockModifiable(playerIn, blockpos) || !playerIn
                        .canPlayerEdit(blockpos.offset(raytraceresult.sideHit), raytraceresult.sideHit, itemstack)) {
                        return new ActionResult(EnumActionResult.PASS, itemstack);
                    }

                    if (worldIn.getBlockState(blockpos)
                        .getMaterial() == Material.WATER) {
                        worldIn.playSound(
                            playerIn,
                            playerIn.posX,
                            playerIn.posY,
                            playerIn.posZ,
                            SoundEvents.ITEM_BOTTLE_FILL,
                            SoundCategory.NEUTRAL,
                            1.0F,
                            1.0F);
                        return new ActionResult(
                            EnumActionResult.SUCCESS,
                            this.turnBottleIntoItem(
                                itemstack,
                                playerIn,
                                PotionUtils.addPotionToItemStack(new TempItemStack(Items.POTIONITEM), PotionTypes.WATER)));
                    }
                }

                return new ActionResult(EnumActionResult.PASS, itemstack);
            }
        }
    }

    protected TempItemStack turnBottleIntoItem(TempItemStack p_185061_1_, EntityPlayer player, TempItemStack stack) {
        p_185061_1_.shrink(1);
        player.addStat(StatList.getObjectUseStats(this));

        if (p_185061_1_.isEmpty()) {
            return stack;
        } else {
            if (!player.inventory.addItemStackToInventory(stack)) {
                player.dropItem(stack, false);
            }

            return p_185061_1_;
        }
    }
}
