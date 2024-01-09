package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WrapperBlockBreakable extends WrapperBlock {

    private final boolean ignoreSimilarity;

    protected WrapperBlockBreakable(Material materialIn, boolean ignoreSimilarityIn) {
        this(materialIn, ignoreSimilarityIn, materialIn.getMaterialMapColor());
    }

    protected WrapperBlockBreakable(Material materialIn, boolean ignoreSimilarityIn, MapColor mapColorIn) {
        super(materialIn, mapColorIn);
        this.ignoreSimilarity = ignoreSimilarityIn;
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
    public boolean isOpaqueCube(IWrapperBlockState state) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IWrapperBlockState blockState, IBlockAccess blockAccess, BlockPos pos,
        EnumFacing side) {
        IWrapperBlockState iblockstate = blockAccess.getBlockState(pos.offset(side));
        WrapperBlock wrapperBlock = iblockstate.getBlock();

        if (this == Blocks.GLASS || this == Blocks.STAINED_GLASS) {
            if (blockState != iblockstate) {
                return true;
            }

            if (wrapperBlock == this) {
                return false;
            }
        }

        return !this.ignoreSimilarity && wrapperBlock == this ? false
            : super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }
}
