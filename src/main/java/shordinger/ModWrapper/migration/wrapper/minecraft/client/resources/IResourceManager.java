package shordinger.ModWrapper.migration.wrapper.minecraft.client.resources;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IResourceManager {

    Set<String> getResourceDomains();

    IResource getResource(ResourceLocation location) throws IOException;

    /**
     * Gets all versions of the resource identified by {@code location}. The list is ordered by resource pack priority
     * from lowest to highest.
     */
    List<IResource> getAllResources(ResourceLocation location) throws IOException;
}
