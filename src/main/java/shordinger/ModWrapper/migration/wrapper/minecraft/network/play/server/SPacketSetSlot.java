package shordinger.ModWrapper.migration.wrapper.minecraft.network.play.server;

import java.io.IOException;

import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SPacketSetSlot implements Packet<INetHandlerPlayClient> {

    private int windowId;
    private int slot;
    private ItemStack item = ItemStack.EMPTY;

    public SPacketSetSlot() {}

    public SPacketSetSlot(int windowIdIn, int slotIn, ItemStack itemIn) {
        this.windowId = windowIdIn;
        this.slot = slotIn;
        this.item = itemIn.copy();
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleSetSlot(this);
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.windowId = buf.readByte();
        this.slot = buf.readShort();
        this.item = buf.readItemStack();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeByte(this.windowId);
        buf.writeShort(this.slot);
        buf.writeItemStack(this.item);
    }

    @SideOnly(Side.CLIENT)
    public int getWindowId() {
        return this.windowId;
    }

    @SideOnly(Side.CLIENT)
    public int getSlot() {
        return this.slot;
    }

    @SideOnly(Side.CLIENT)
    public ItemStack getStack() {
        return this.item;
    }
}
