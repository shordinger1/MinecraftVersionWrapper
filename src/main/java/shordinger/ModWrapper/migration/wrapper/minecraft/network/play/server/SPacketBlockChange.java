package shordinger.ModWrapper.migration.wrapper.minecraft.network.play.server;

import java.io.IOException;

import net.minecraft.block.Block;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class SPacketBlockChange implements Packet<INetHandlerPlayClient> {

    private BlockPos blockPosition;
    public IWrapperBlockState blockState;

    public SPacketBlockChange() {}

    public SPacketBlockChange(World worldIn, BlockPos posIn) {
        this.blockPosition = posIn;
        this.blockState = worldIn.getBlockState(posIn);
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.blockPosition = buf.readBlockPos();
        this.blockState = Block.BLOCK_STATE_IDS.getByValue(buf.readVarInt());
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeBlockPos(this.blockPosition);
        buf.writeVarInt(Block.BLOCK_STATE_IDS.get(this.blockState));
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleBlockChange(this);
    }

    @SideOnly(Side.CLIENT)
    public IWrapperBlockState getBlockState() {
        return this.blockState;
    }

    @SideOnly(Side.CLIENT)
    public BlockPos getBlockPosition() {
        return this.blockPosition;
    }
}
