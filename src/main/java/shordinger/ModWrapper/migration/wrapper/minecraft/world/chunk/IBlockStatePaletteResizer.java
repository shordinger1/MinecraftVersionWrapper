package shordinger.ModWrapper.migration.wrapper.minecraft.world.chunk;

import net.minecraft.block.state.IWrapperBlockState;

interface IWrapperBlockStatePaletteResizer {

    int onResize(int bits, IWrapperBlockState state);
}
