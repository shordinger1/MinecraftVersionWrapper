package shordinger.ModWrapper.migration.wrapper.minecraft.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.RayTraceResult;

public class WrapperItemLilyPad extends WrapperItemColored {

    public WrapperItemLilyPad(Block block) {
        super(block, false);
    }

    /**
     * Called when the equipped item is right clicked.
     */
    public ActionResult<TempItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        TempItemStack itemstack = playerIn.getHeldItem(handIn);
        RayTraceResult raytraceresult = this.rayTrace(worldIn, playerIn, true);

        if (raytraceresult == null) {
            return new ActionResult<TempItemStack>(EnumActionResult.PASS, itemstack);
        } else {
            if (raytraceresult.typeOfHit == RayTraceResult.Type.BLOCK) {
                BlockPos blockpos = raytraceresult.getBlockPos();

                if (!worldIn.isBlockModifiable(playerIn, blockpos) || !playerIn
                    .canPlayerEdit(blockpos.offset(raytraceresult.sideHit), raytraceresult.sideHit, itemstack)) {
                    return new ActionResult<TempItemStack>(EnumActionResult.FAIL, itemstack);
                }

                BlockPos blockpos1 = blockpos.up();
                IWrapperBlockState iblockstate = worldIn.getBlockState(blockpos);

                if (iblockstate.getMaterial() == Material.WATER
                    && ((Integer) iblockstate.getValue(BlockLiquid.LEVEL)).intValue() == 0
                    && worldIn.isAirBlock(blockpos1)) {
                    // special case for handling block placement with water lilies
                    net.minecraftforge.common.util.BlockSnapshot blocksnapshot = net.minecraftforge.common.util.BlockSnapshot
                        .getBlockSnapshot(worldIn, blockpos1);
                    worldIn.setBlockState(blockpos1, Blocks.WATERLILY.getDefaultState());
                    if (net.minecraftforge.event.ForgeEventFactory
                        .onPlayerBlockPlace(playerIn, blocksnapshot, net.minecraft.util.EnumFacing.UP, handIn)
                        .isCanceled()) {
                        blocksnapshot.restore(true, false);
                        return new ActionResult<TempItemStack>(EnumActionResult.FAIL, itemstack);
                    }

                    worldIn.setBlockState(blockpos1, Blocks.WATERLILY.getDefaultState(), 11);

                    if (playerIn instanceof EntityPlayerMP) {
                        CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) playerIn, blockpos1, itemstack);
                    }

                    if (!playerIn.capabilities.isCreativeMode) {
                        itemstack.shrink(1);
                    }

                    playerIn.addStat(StatList.getObjectUseStats(this));
                    worldIn.playSound(
                        playerIn,
                        blockpos,
                        SoundEvents.BLOCK_WATERLILY_PLACE,
                        SoundCategory.BLOCKS,
                        1.0F,
                        1.0F);
                    return new ActionResult<TempItemStack>(EnumActionResult.SUCCESS, itemstack);
                }
            }

            return new ActionResult<TempItemStack>(EnumActionResult.FAIL, itemstack);
        }
    }
}
