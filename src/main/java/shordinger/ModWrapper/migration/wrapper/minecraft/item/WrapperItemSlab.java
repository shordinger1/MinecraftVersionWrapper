package shordinger.ModWrapper.migration.wrapper.minecraft.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.AxisAlignedBB;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WrapperItemSlab extends WrapperItemBlock {

    private final BlockSlab singleSlab;
    private final BlockSlab doubleSlab;

    public WrapperItemSlab(Block block, BlockSlab singleSlab, BlockSlab doubleSlab) {
        super(block);
        this.singleSlab = singleSlab;
        this.doubleSlab = doubleSlab;
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    /**
     * Converts the given ItemStack damage value into a metadata value to be placed in the world when this Item is
     * placed as a Block (mostly used with ItemBlocks).
     */
    public int getMetadata(int damage) {
        return damage;
    }

    /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     */
    public String getUnlocalizedName(TempItemStack stack) {
        return this.singleSlab.getUnlocalizedName(stack.getMetadata());
    }

    /**
     * Called when a Block is right-clicked with this Item
     */
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
        EnumFacing facing, float hitX, float hitY, float hitZ) {
        TempItemStack itemstack = player.getHeldItem(hand);

        if (!itemstack.isEmpty() && player.canPlayerEdit(pos.offset(facing), facing, itemstack)) {
            Comparable<?> comparable = this.singleSlab.getTypeForItem(itemstack);
            IWrapperBlockState iblockstate = worldIn.getBlockState(pos);

            if (iblockstate.getBlock() == this.singleSlab) {
                IProperty<?> iproperty = this.singleSlab.getVariantProperty();
                Comparable<?> comparable1 = iblockstate.getValue(iproperty);
                BlockSlab.EnumBlockHalf blockslab$enumblockhalf = (BlockSlab.EnumBlockHalf) iblockstate
                    .getValue(BlockSlab.HALF);

                if ((facing == EnumFacing.UP && blockslab$enumblockhalf == BlockSlab.EnumBlockHalf.BOTTOM
                    || facing == EnumFacing.DOWN && blockslab$enumblockhalf == BlockSlab.EnumBlockHalf.TOP)
                    && comparable1 == comparable) {
                    IWrapperBlockState iblockstate1 = this.makeState(iproperty, comparable1);
                    AxisAlignedBB axisalignedbb = iblockstate1.getCollisionBoundingBox(worldIn, pos);

                    if (axisalignedbb != Block.NULL_AABB && worldIn.checkNoEntityCollision(axisalignedbb.offset(pos))
                        && worldIn.setBlockState(pos, iblockstate1, 11)) {
                        SoundType soundtype = this.doubleSlab.getSoundType(iblockstate1, worldIn, pos, player);
                        worldIn.playSound(
                            player,
                            pos,
                            soundtype.getPlaceSound(),
                            SoundCategory.BLOCKS,
                            (soundtype.getVolume() + 1.0F) / 2.0F,
                            soundtype.getPitch() * 0.8F);
                        itemstack.shrink(1);

                        if (player instanceof EntityPlayerMP) {
                            CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, pos, itemstack);
                        }
                    }

                    return EnumActionResult.SUCCESS;
                }
            }

            return this.tryPlace(player, itemstack, worldIn, pos.offset(facing), comparable) ? EnumActionResult.SUCCESS
                : super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
        } else {
            return EnumActionResult.FAIL;
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side, EntityPlayer player,
        TempItemStack stack) {
        BlockPos blockpos = pos;
        IProperty<?> iproperty = this.singleSlab.getVariantProperty();
        Comparable<?> comparable = this.singleSlab.getTypeForItem(stack);
        IWrapperBlockState iblockstate = worldIn.getBlockState(pos);

        if (iblockstate.getBlock() == this.singleSlab) {
            boolean flag = iblockstate.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.TOP;

            if ((side == EnumFacing.UP && !flag || side == EnumFacing.DOWN && flag)
                && comparable == iblockstate.getValue(iproperty)) {
                return true;
            }
        }

        pos = pos.offset(side);
        IWrapperBlockState iblockstate1 = worldIn.getBlockState(pos);
        return iblockstate1.getBlock() == this.singleSlab && comparable == iblockstate1.getValue(iproperty) ? true
            : super.canPlaceBlockOnSide(worldIn, blockpos, side, player, stack);
    }

    private boolean tryPlace(EntityPlayer player, TempItemStack stack, World worldIn, BlockPos pos, Object itemSlabType) {
        IWrapperBlockState iblockstate = worldIn.getBlockState(pos);

        if (iblockstate.getBlock() == this.singleSlab) {
            Comparable<?> comparable = iblockstate.getValue(this.singleSlab.getVariantProperty());

            if (comparable == itemSlabType) {
                IWrapperBlockState iblockstate1 = this.makeState(this.singleSlab.getVariantProperty(), comparable);
                AxisAlignedBB axisalignedbb = iblockstate1.getCollisionBoundingBox(worldIn, pos);

                if (axisalignedbb != Block.NULL_AABB && worldIn.checkNoEntityCollision(axisalignedbb.offset(pos))
                    && worldIn.setBlockState(pos, iblockstate1, 11)) {
                    SoundType soundtype = this.doubleSlab.getSoundType(iblockstate1, worldIn, pos, player);
                    worldIn.playSound(
                        player,
                        pos,
                        soundtype.getPlaceSound(),
                        SoundCategory.BLOCKS,
                        (soundtype.getVolume() + 1.0F) / 2.0F,
                        soundtype.getPitch() * 0.8F);
                    stack.shrink(1);
                }

                return true;
            }
        }

        return false;
    }

    protected <T extends Comparable<T>> IWrapperBlockState makeState(IProperty<T> p_185055_1_,
        Comparable<?> p_185055_2_) {
        return this.doubleSlab.getDefaultState()
            .withProperty(p_185055_1_, (T) p_185055_2_);
    }
}
