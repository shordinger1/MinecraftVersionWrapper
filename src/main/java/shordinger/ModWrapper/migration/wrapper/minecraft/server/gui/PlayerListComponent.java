package shordinger.ModWrapper.migration.wrapper.minecraft.server.gui;

import java.util.Vector;

import javax.swing.JList;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ITickable;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.SERVER)
public class PlayerListComponent extends JList implements ITickable {

    private final MinecraftServer server;
    private int ticks;

    public PlayerListComponent(MinecraftServer server) {
        this.server = server;
        server.registerTickable(this);
    }

    /**
     * Like the old updateEntity(), except more generic.
     */
    public void update() {
        if (this.ticks++ % 20 == 0) {
            Vector<String> vector = new Vector<String>();

            for (int i = 0; i < this.server.getPlayerList()
                .getPlayers()
                .size(); ++i) {
                vector.add(
                    ((EntityPlayerMP) this.server.getPlayerList()
                        .getPlayers()
                        .get(i)).getName());
            }

            this.setListData(vector);
        }
    }
}
