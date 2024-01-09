package shordinger.ModWrapper.migration.wrapper.minecraft.server.dedicated;

import net.minecraft.command.ICommandSender;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.SERVER)
public class PendingCommand {

    /** The command string. */
    public final String command;
    public final ICommandSender sender;

    public PendingCommand(String input, ICommandSender sender) {
        this.command = input;
        this.sender = sender;
    }
}
