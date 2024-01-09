package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.AxisAlignedBB;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WrapperBlockLilyPad extends WrapperBlockBush {

    protected static final AxisAlignedBB LILY_PAD_AABB = new AxisAlignedBB(
        0.0625D,
        0.0D,
        0.0625D,
        0.9375D,
        0.09375D,
        0.9375D);

    protected WrapperBlockLilyPad() {
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }

    public void addCollisionBoxToList(IWrapperBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox,
        List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        if (!(entityIn instanceof EntityBoat)) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, LILY_PAD_AABB);
        }
    }

    /**
     * Called When an Entity Collided with the Block
     */
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IWrapperBlockState state, Entity entityIn) {
        super.onEntityCollidedWithBlock(worldIn, pos, state, entityIn);

        if (entityIn instanceof EntityBoat) {
            worldIn.destroyBlock(new BlockPos(pos), true);
        }
    }

    public AxisAlignedBB getBoundingBox(IWrapperBlockState state, IBlockAccess source, BlockPos pos) {
        return LILY_PAD_AABB;
    }

    /**
     * Return true if the block can sustain a Bush
     */
    protected boolean canSustainBush(IWrapperBlockState state) {
        return state.getBlock() == Blocks.WATER || state.getMaterial() == Material.ICE;
    }

    public boolean canBlockStay(World worldIn, BlockPos pos, IWrapperBlockState state) {
        if (pos.getY() >= 0 && pos.getY() < 256) {
            IWrapperBlockState iblockstate = worldIn.getBlockState(pos.down());
            Material material = iblockstate.getMaterial();
            return material == Material.WATER
                && ((Integer) iblockstate.getValue(WrapperBlockLiquid.LEVEL)).intValue() == 0
                || material == Material.ICE;
        } else {
            return false;
        }
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        return 0;
    }
}
