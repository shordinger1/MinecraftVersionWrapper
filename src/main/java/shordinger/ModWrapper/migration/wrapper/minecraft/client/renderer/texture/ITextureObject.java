package shordinger.ModWrapper.migration.wrapper.minecraft.client.renderer.texture;

import java.io.IOException;

import net.minecraft.client.resources.IResourceManager;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface ITextureObject {

    void setBlurMipmap(boolean blurIn, boolean mipmapIn);

    void restoreLastBlurMipmap();

    void loadTexture(IResourceManager resourceManager) throws IOException;

    int getGlTextureId();
}
