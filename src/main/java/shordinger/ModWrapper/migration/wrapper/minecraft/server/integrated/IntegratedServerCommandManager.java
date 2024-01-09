package shordinger.ModWrapper.migration.wrapper.minecraft.server.integrated;

import net.minecraft.command.ServerCommandManager;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class IntegratedServerCommandManager extends ServerCommandManager {

    public IntegratedServerCommandManager(IntegratedServer server) {
        super(server);
    }
}
