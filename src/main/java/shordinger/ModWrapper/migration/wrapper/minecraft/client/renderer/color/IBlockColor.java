package shordinger.ModWrapper.migration.wrapper.minecraft.client.renderer.color;

import javax.annotation.Nullable;

import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

@SideOnly(Side.CLIENT)
public interface IBlockColor {

    int colorMultiplier(IWrapperBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos,
        int tintIndex);
}
