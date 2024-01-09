package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import java.util.Random;

import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.world.World;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public interface IGrowable {

    /**
     * Whether this IGrowable can grow
     */
    boolean canGrow(World worldIn, BlockPos pos, IWrapperBlockState state, boolean isClient);

    boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IWrapperBlockState state);

    void grow(World worldIn, Random rand, BlockPos pos, IWrapperBlockState state);
}
