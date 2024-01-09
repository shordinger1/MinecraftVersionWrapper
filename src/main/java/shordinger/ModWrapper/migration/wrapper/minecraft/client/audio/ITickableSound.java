package shordinger.ModWrapper.migration.wrapper.minecraft.client.audio;

import net.minecraft.util.ITickable;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface ITickableSound extends ISound, ITickable {

    boolean isDonePlaying();
}
