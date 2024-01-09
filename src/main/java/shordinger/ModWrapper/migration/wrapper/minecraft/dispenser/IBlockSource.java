package shordinger.ModWrapper.migration.wrapper.minecraft.dispenser;

import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.tileentity.TileEntity;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public interface IBlockSource extends ILocatableSource {

    double getX();

    double getY();

    double getZ();

    BlockPos getBlockPos();

    /**
     * Gets the block state of this position and returns it.
     * 
     * @return Block state in this position
     */
    IWrapperBlockState getBlockState();

    <T extends TileEntity> T getBlockTileEntity();
}
