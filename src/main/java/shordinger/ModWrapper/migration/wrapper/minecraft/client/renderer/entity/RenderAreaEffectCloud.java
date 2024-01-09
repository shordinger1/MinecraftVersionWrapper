package shordinger.ModWrapper.migration.wrapper.minecraft.client.renderer.entity;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderAreaEffectCloud extends Render<EntityAreaEffectCloud> {

    public RenderAreaEffectCloud(RenderManager manager) {
        super(manager);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    @Nullable
    protected ResourceLocation getEntityTexture(EntityAreaEffectCloud entity) {
        return null;
    }
}
