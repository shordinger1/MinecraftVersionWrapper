package shordinger.ModWrapper.migration.wrapper.minecraft.network.play.server;

import java.io.IOException;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SPacketTimeUpdate implements Packet<INetHandlerPlayClient> {

    private long totalWorldTime;
    private long worldTime;

    public SPacketTimeUpdate() {}

    public SPacketTimeUpdate(long totalWorldTimeIn, long worldTimeIn, boolean doDaylightCycle) {
        this.totalWorldTime = totalWorldTimeIn;
        this.worldTime = worldTimeIn;

        if (!doDaylightCycle) {
            this.worldTime = -this.worldTime;

            if (this.worldTime == 0L) {
                this.worldTime = -1L;
            }
        }
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.totalWorldTime = buf.readLong();
        this.worldTime = buf.readLong();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeLong(this.totalWorldTime);
        buf.writeLong(this.worldTime);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleTimeUpdate(this);
    }

    @SideOnly(Side.CLIENT)
    public long getTotalWorldTime() {
        return this.totalWorldTime;
    }

    @SideOnly(Side.CLIENT)
    public long getWorldTime() {
        return this.worldTime;
    }
}
