package shordinger.ModWrapper.migration.wrapper.minecraft.client.renderer.entity;

import net.minecraft.entity.item.EntityMinecartMobSpawner;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderMinecartMobSpawner extends RenderMinecart<EntityMinecartMobSpawner> {

    public RenderMinecartMobSpawner(RenderManager renderManagerIn) {
        super(renderManagerIn);
    }
}
