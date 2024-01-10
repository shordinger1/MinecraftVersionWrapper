package shordinger.ModWrapper.migration.wrapper.minecraft.dispenser;

import net.minecraft.dispenser.IBlockSource;

import shordinger.ModWrapper.migration.wrapper.minecraft.block.state.IBlockState;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public interface IWrapperBlockSource extends ILocatableSource, IBlockSource {


    BlockPos getBlockPos();

    /**
     * Gets the block state of this position and returns it.
     *
     * @return Block state in this position
     */
    IBlockState getBlockState();
//
//    <T extends TileEntity> T getBlockTileEntity();
}
