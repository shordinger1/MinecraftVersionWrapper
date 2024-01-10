package shordinger.ModWrapper.migration.wrapper.minecraft.item;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.world.World;

public class WrapperItemMapBase extends WrapperItem {

    /**
     * false for all Items except sub-classes of ItemMapBase
     */
    public boolean isMap() {
        return true;
    }

    @Nullable
    public Packet<?> createMapDataPacket(TempItemStack stack, World worldIn, EntityPlayer player) {
        return null;
    }
}
