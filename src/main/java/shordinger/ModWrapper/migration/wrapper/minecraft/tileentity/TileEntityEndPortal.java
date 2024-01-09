package shordinger.ModWrapper.migration.wrapper.minecraft.tileentity;

import net.minecraft.util.EnumFacing;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityEndPortal extends TileEntity {

    @SideOnly(Side.CLIENT)
    public boolean shouldRenderFace(EnumFacing p_184313_1_) {
        return p_184313_1_ == EnumFacing.UP;
    }
}
