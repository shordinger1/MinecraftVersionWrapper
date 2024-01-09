package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import java.util.Random;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.MathHelper;

public class WrapperBlockFrostedIce extends WrapperBlockIce {

    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 3);

    public WrapperBlockFrostedIce() {
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(AGE, Integer.valueOf(0)));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        return ((Integer) state.getValue(AGE)).intValue();
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IWrapperBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
            .withProperty(AGE, Integer.valueOf(MathHelper.clamp(meta, 0, 3)));
    }

    public void updateTick(World worldIn, BlockPos pos, IWrapperBlockState state, Random rand) {
        if ((rand.nextInt(3) == 0 || this.countNeighbors(worldIn, pos) < 4) && worldIn.getLightFromNeighbors(pos)
            > 11 - ((Integer) state.getValue(AGE)).intValue() - state.getLightOpacity()) {
            this.slightlyMelt(worldIn, pos, state, rand, true);
        } else {
            worldIn.scheduleUpdate(pos, this, MathHelper.getInt(rand, 20, 40));
        }
    }

    /**
     * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
     * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
     * block, etc.
     */
    public void neighborChanged(IWrapperBlockState state, World worldIn, BlockPos pos, WrapperBlock wrapperBlockIn,
        BlockPos fromPos) {
        if (wrapperBlockIn == this) {
            int i = this.countNeighbors(worldIn, pos);

            if (i < 2) {
                this.turnIntoWater(worldIn, pos);
            }
        }
    }

    private int countNeighbors(World worldIn, BlockPos pos) {
        int i = 0;

        for (EnumFacing enumfacing : EnumFacing.values()) {
            if (worldIn.getBlockState(pos.offset(enumfacing))
                .getBlock() == this) {
                ++i;

                if (i >= 4) {
                    return i;
                }
            }
        }

        return i;
    }

    protected void slightlyMelt(World worldIn, BlockPos pos, IWrapperBlockState state, Random rand,
        boolean meltNeighbors) {
        int i = ((Integer) state.getValue(AGE)).intValue();

        if (i < 3) {
            worldIn.setBlockState(pos, state.withProperty(AGE, Integer.valueOf(i + 1)), 2);
            worldIn.scheduleUpdate(pos, this, MathHelper.getInt(rand, 20, 40));
        } else {
            this.turnIntoWater(worldIn, pos);

            if (meltNeighbors) {
                for (EnumFacing enumfacing : EnumFacing.values()) {
                    BlockPos blockpos = pos.offset(enumfacing);
                    IWrapperBlockState iblockstate = worldIn.getBlockState(blockpos);

                    if (iblockstate.getBlock() == this) {
                        this.slightlyMelt(worldIn, blockpos, iblockstate, rand, false);
                    }
                }
            }
        }
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { AGE });
    }

    public ItemStack getItem(World worldIn, BlockPos pos, IWrapperBlockState state) {
        return ItemStack.EMPTY;
    }
}
