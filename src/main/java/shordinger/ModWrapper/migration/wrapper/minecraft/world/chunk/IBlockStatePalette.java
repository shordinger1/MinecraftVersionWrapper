package shordinger.ModWrapper.migration.wrapper.minecraft.world.chunk;

import javax.annotation.Nullable;

import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.network.PacketBuffer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IWrapperBlockStatePalette {

    int idFor(IWrapperBlockState state);

    /**
     * Gets the block state by the palette id.
     */
    @Nullable
    IWrapperBlockState getBlockState(int indexKey);

    @SideOnly(Side.CLIENT)
    void read(PacketBuffer buf);

    void write(PacketBuffer buf);

    int getSerializedSize();
}
