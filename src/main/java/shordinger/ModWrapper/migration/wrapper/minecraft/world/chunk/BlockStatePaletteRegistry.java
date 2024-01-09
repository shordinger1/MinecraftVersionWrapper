package shordinger.ModWrapper.migration.wrapper.minecraft.world.chunk;

import net.minecraft.block.Block;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.PacketBuffer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockStatePaletteRegistry implements IWrapperBlockStatePalette {

    public int idFor(IWrapperBlockState state) {
        int i = Block.BLOCK_STATE_IDS.get(state);
        return i == -1 ? 0 : i;
    }

    /**
     * Gets the block state by the palette id.
     */
    public IWrapperBlockState getBlockState(int indexKey) {
        IWrapperBlockState iblockstate = Block.BLOCK_STATE_IDS.getByValue(indexKey);
        return iblockstate == null ? Blocks.AIR.getDefaultState() : iblockstate;
    }

    @SideOnly(Side.CLIENT)
    public void read(PacketBuffer buf) {
        buf.readVarInt();
    }

    public void write(PacketBuffer buf) {
        buf.writeVarInt(0);
    }

    public int getSerializedSize() {
        return PacketBuffer.getVarIntSize(0);
    }
}
