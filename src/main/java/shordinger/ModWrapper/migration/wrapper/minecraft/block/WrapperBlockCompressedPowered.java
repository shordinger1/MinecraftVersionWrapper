package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WrapperBlockCompressedPowered extends WrapperBlock {

    public WrapperBlockCompressedPowered(Material materialIn, MapColor color) {
        super(materialIn, color);
    }

    /**
     * Can this block provide power. Only wire currently seems to have this change based on its state.
     */
    public boolean canProvidePower(IWrapperBlockState state) {
        return true;
    }

    public int getWeakPower(IWrapperBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return 15;
    }
}
