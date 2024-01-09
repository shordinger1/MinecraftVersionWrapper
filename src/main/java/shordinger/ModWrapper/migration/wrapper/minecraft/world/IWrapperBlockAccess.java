package shordinger.ModWrapper.migration.wrapper.minecraft.world;

import javax.annotation.Nullable;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shordinger.ModWrapper.migration.wrapper.minecraft.block.state.IWrapperBlockState;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;
import shordinger.ModWrapper.migration.wrapper.minecraft.world.biome.Biome;

public interface IWrapperBlockAccess extends IBlockAccess {

    @Nullable
    TileEntity getTileEntity(BlockPos pos);

    @SideOnly(Side.CLIENT)
    int getCombinedLight(BlockPos pos, int lightValue);

    IWrapperBlockState getBlockState(BlockPos pos);

    /**
     * Checks to see if an air block exists at the provided location. Note that this only checks to see if the blocks
     * material is set to air, meaning it is possible for non-vanilla blocks to still pass this check.
     */
    boolean isAirBlock(BlockPos pos);

    @SideOnly(Side.CLIENT)
    Biome getBiome(BlockPos pos);

    int getStrongPower(BlockPos pos, EnumFacing direction);

    @SideOnly(Side.CLIENT)
    WrapperWorldType getWorldType();

    /**
     * FORGE: isSideSolid, pulled up from {@link WrapperWorld}
     *
     * @param pos      Position
     * @param side     Side
     * @param _default default return value
     * @return if the block is solid on the side
     */
    boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default);
}
