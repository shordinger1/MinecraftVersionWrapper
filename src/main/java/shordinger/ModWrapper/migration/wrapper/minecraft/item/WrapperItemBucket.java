package shordinger.ModWrapper.migration.wrapper.minecraft.item;

import javax.annotation.Nullable;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.RayTraceResult;

public class WrapperItemBucket extends WrapperItem {

    /** field for checking if the bucket has been filled. */
    private final Block containedBlock;

    public WrapperItemBucket(Block containedBlockIn) {
        this.maxStackSize = 1;
        this.containedBlock = containedBlockIn;
        this.setCreativeTab(CreativeTabs.MISC);
    }

    /**
     * Called when the equipped item is right clicked.
     */
    public ActionResult<TempItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        boolean flag = this.containedBlock == Blocks.AIR;
        TempItemStack itemstack = playerIn.getHeldItem(handIn);
        RayTraceResult raytraceresult = this.rayTrace(worldIn, playerIn, flag);
        ActionResult<TempItemStack> ret = net.minecraftforge.event.ForgeEventFactory
            .onBucketUse(playerIn, worldIn, itemstack, raytraceresult);
        if (ret != null) return ret;

        if (raytraceresult == null) {
            return new ActionResult<TempItemStack>(EnumActionResult.PASS, itemstack);
        } else if (raytraceresult.typeOfHit != RayTraceResult.Type.BLOCK) {
            return new ActionResult<TempItemStack>(EnumActionResult.PASS, itemstack);
        } else {
            BlockPos blockpos = raytraceresult.getBlockPos();

            if (!worldIn.isBlockModifiable(playerIn, blockpos)) {
                return new ActionResult<TempItemStack>(EnumActionResult.FAIL, itemstack);
            } else if (flag) {
                if (!playerIn
                    .canPlayerEdit(blockpos.offset(raytraceresult.sideHit), raytraceresult.sideHit, itemstack)) {
                    return new ActionResult<TempItemStack>(EnumActionResult.FAIL, itemstack);
                } else {
                    IWrapperBlockState iblockstate = worldIn.getBlockState(blockpos);
                    Material material = iblockstate.getMaterial();

                    if (material == Material.WATER
                        && ((Integer) iblockstate.getValue(BlockLiquid.LEVEL)).intValue() == 0) {
                        worldIn.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 11);
                        playerIn.addStat(StatList.getObjectUseStats(this));
                        playerIn.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
                        return new ActionResult<TempItemStack>(
                            EnumActionResult.SUCCESS,
                            this.fillBucket(itemstack, playerIn, Items.WATER_BUCKET));
                    } else if (material == Material.LAVA
                        && ((Integer) iblockstate.getValue(BlockLiquid.LEVEL)).intValue() == 0) {
                            playerIn.playSound(SoundEvents.ITEM_BUCKET_FILL_LAVA, 1.0F, 1.0F);
                            worldIn.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 11);
                            playerIn.addStat(StatList.getObjectUseStats(this));
                            return new ActionResult<TempItemStack>(
                                EnumActionResult.SUCCESS,
                                this.fillBucket(itemstack, playerIn, Items.LAVA_BUCKET));
                        } else {
                            return new ActionResult<TempItemStack>(EnumActionResult.FAIL, itemstack);
                        }
                }
            } else {
                boolean flag1 = worldIn.getBlockState(blockpos)
                    .getBlock()
                    .isReplaceable(worldIn, blockpos);
                BlockPos blockpos1 = flag1 && raytraceresult.sideHit == EnumFacing.UP ? blockpos
                    : blockpos.offset(raytraceresult.sideHit);

                if (!playerIn.canPlayerEdit(blockpos1, raytraceresult.sideHit, itemstack)) {
                    return new ActionResult<TempItemStack>(EnumActionResult.FAIL, itemstack);
                } else if (this.tryPlaceContainedLiquid(playerIn, worldIn, blockpos1)) {
                    if (playerIn instanceof EntityPlayerMP) {
                        CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) playerIn, blockpos1, itemstack);
                    }

                    playerIn.addStat(StatList.getObjectUseStats(this));
                    return !playerIn.capabilities.isCreativeMode
                        ? new ActionResult(EnumActionResult.SUCCESS, new TempItemStack(Items.BUCKET))
                        : new ActionResult(EnumActionResult.SUCCESS, itemstack);
                } else {
                    return new ActionResult<TempItemStack>(EnumActionResult.FAIL, itemstack);
                }
            }
        }
    }

    private TempItemStack fillBucket(TempItemStack emptyBuckets, EntityPlayer player, WrapperItem fullBucket) {
        if (player.capabilities.isCreativeMode) {
            return emptyBuckets;
        } else {
            emptyBuckets.shrink(1);

            if (emptyBuckets.isEmpty()) {
                return new TempItemStack(fullBucket);
            } else {
                if (!player.inventory.addItemStackToInventory(new TempItemStack(fullBucket))) {
                    player.dropItem(new TempItemStack(fullBucket), false);
                }

                return emptyBuckets;
            }
        }
    }

    public boolean tryPlaceContainedLiquid(@Nullable EntityPlayer player, World worldIn, BlockPos posIn) {
        if (this.containedBlock == Blocks.AIR) {
            return false;
        } else {
            IWrapperBlockState iblockstate = worldIn.getBlockState(posIn);
            Material material = iblockstate.getMaterial();
            boolean flag = !material.isSolid();
            boolean flag1 = iblockstate.getBlock()
                .isReplaceable(worldIn, posIn);

            if (!worldIn.isAirBlock(posIn) && !flag && !flag1) {
                return false;
            } else {
                if (worldIn.provider.doesWaterVaporize() && this.containedBlock == Blocks.FLOWING_WATER) {
                    int l = posIn.getX();
                    int i = posIn.getY();
                    int j = posIn.getZ();
                    worldIn.playSound(
                        player,
                        posIn,
                        SoundEvents.BLOCK_FIRE_EXTINGUISH,
                        SoundCategory.BLOCKS,
                        0.5F,
                        2.6F + (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.8F);

                    for (int k = 0; k < 8; ++k) {
                        worldIn.spawnParticle(
                            EnumParticleTypes.SMOKE_LARGE,
                            (double) l + Math.random(),
                            (double) i + Math.random(),
                            (double) j + Math.random(),
                            0.0D,
                            0.0D,
                            0.0D);
                    }
                } else {
                    if (!worldIn.isRemote && (flag || flag1) && !material.isLiquid()) {
                        worldIn.destroyBlock(posIn, true);
                    }

                    SoundEvent soundevent = this.containedBlock == Blocks.FLOWING_LAVA
                        ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA
                        : SoundEvents.ITEM_BUCKET_EMPTY;
                    worldIn.playSound(player, posIn, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    worldIn.setBlockState(posIn, this.containedBlock.getDefaultState(), 11);
                }

                return true;
            }
        }
    }

    @Override
    public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(TempItemStack stack,
                                                                                       @Nullable net.minecraft.nbt.NBTTagCompound nbt) {
        if (this.getClass() == WrapperItemBucket.class) {
            return new net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper(stack);
        } else {
            return super.initCapabilities(stack, nbt);
        }
    }
}
