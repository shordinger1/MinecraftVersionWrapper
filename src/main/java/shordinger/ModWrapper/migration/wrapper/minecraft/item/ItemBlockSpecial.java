package shordinger.ModWrapper.migration.wrapper.minecraft.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class ItemBlockSpecial extends Item {

    private final Block block;

    public ItemBlockSpecial(Block block) {
        this.block = block;
    }

    /**
     * Called when a Block is right-clicked with this Item
     */
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
        EnumFacing facing, float hitX, float hitY, float hitZ) {
        IWrapperBlockState iblockstate = worldIn.getBlockState(pos);
        Block block = iblockstate.getBlock();

        if (block == Blocks.SNOW_LAYER && ((Integer) iblockstate.getValue(BlockSnow.LAYERS)).intValue() < 1) {
            facing = EnumFacing.UP;
        } else if (!block.isReplaceable(worldIn, pos)) {
            pos = pos.offset(facing);
        }

        ItemStack itemstack = player.getHeldItem(hand);

        if (!itemstack.isEmpty() && player.canPlayerEdit(pos, facing, itemstack)
            && worldIn.mayPlace(this.block, pos, false, facing, player)) {
            IWrapperBlockState iblockstate1 = this.block
                .getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, 0, player, hand);

            if (!worldIn.setBlockState(pos, iblockstate1, 11)) {
                return EnumActionResult.FAIL;
            } else {
                iblockstate1 = worldIn.getBlockState(pos);

                if (iblockstate1.getBlock() == this.block) {
                    ItemBlock.setTileEntityNBT(worldIn, player, pos, itemstack);
                    iblockstate1.getBlock()
                        .onBlockPlacedBy(worldIn, pos, iblockstate1, player, itemstack);

                    if (player instanceof EntityPlayerMP) {
                        CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, pos, itemstack);
                    }
                }

                SoundType soundtype = iblockstate1.getBlock()
                    .getSoundType(iblockstate1, worldIn, pos, player);
                worldIn.playSound(
                    player,
                    pos,
                    soundtype.getPlaceSound(),
                    SoundCategory.BLOCKS,
                    (soundtype.getVolume() + 1.0F) / 2.0F,
                    soundtype.getPitch() * 0.8F);
                itemstack.shrink(1);
                return EnumActionResult.SUCCESS;
            }
        } else {
            return EnumActionResult.FAIL;
        }
    }

    public Block getBlock() {
        return this.getBlockRaw() == null ? null : this.getBlockRaw().delegate.get();
    }

    private Block getBlockRaw() {
        return this.block;
    }
}
