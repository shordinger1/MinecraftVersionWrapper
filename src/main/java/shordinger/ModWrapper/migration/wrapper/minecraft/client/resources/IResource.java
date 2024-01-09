package shordinger.ModWrapper.migration.wrapper.minecraft.client.resources;

import java.io.Closeable;
import java.io.InputStream;

import javax.annotation.Nullable;

import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IResource extends Closeable {

    ResourceLocation getResourceLocation();

    InputStream getInputStream();

    boolean hasMetadata();

    @Nullable
    <T extends IMetadataSection> T getMetadata(String sectionName);

    String getResourcePackName();
}
