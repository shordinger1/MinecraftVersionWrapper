package shordinger.ModWrapper.migration.wrapper.minecraft.client.resources;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreenResourcePacks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ResourcePackListEntryDefault extends ResourcePackListEntryServer {

    public ResourcePackListEntryDefault(GuiScreenResourcePacks resourcePacksGUIIn) {
        super(
            resourcePacksGUIIn,
            Minecraft.getMinecraft()
                .getResourcePackRepository().rprDefaultResourcePack);
    }

    protected String getResourcePackName() {
        return "Default";
    }

    public boolean isServerPack() {
        return false;
    }
}
