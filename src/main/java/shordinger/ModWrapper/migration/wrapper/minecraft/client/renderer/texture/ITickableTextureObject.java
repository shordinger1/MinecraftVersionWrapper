package shordinger.ModWrapper.migration.wrapper.minecraft.client.renderer.texture;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface ITickableTextureObject extends ITextureObject, ITickable {
}
