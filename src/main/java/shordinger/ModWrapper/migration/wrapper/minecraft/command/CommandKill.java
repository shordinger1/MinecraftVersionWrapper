package shordinger.ModWrapper.migration.wrapper.minecraft.command;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class CommandKill extends CommandBase {

    /**
     * Gets the name of the command
     */
    public String getName() {
        return "kill";
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel() {
        return 2;
    }

    /**
     * Gets the usage string for the command.
     */
    public String getUsage(IWrapperCommandSender sender) {
        return "commands.kill.usage";
    }

    /**
     * Callback for when the command is executed
     */
    public void execute(MinecraftServer server, IWrapperCommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            EntityPlayer entityplayer = getCommandSenderAsPlayer(sender);
            entityplayer.onKillCommand();
            notifyCommandListener(
                sender,
                this,
                "commands.kill.successful",
                new Object[] { entityplayer.getDisplayName() });
        } else {
            Entity entity = getEntity(server, sender, args[0]);
            entity.onKillCommand();
            notifyCommandListener(sender, this, "commands.kill.successful", new Object[] { entity.getDisplayName() });
        }
    }

    /**
     * Return whether the specified command parameter index is a username parameter.
     */
    public boolean isUsernameIndex(String[] args, int index) {
        return index == 0;
    }

    /**
     * Get a list of options for when the user presses the TAB key
     */
    public List<String> getTabCompletions(MinecraftServer server, IWrapperCommandSender sender, String[] args,
                                          @Nullable BlockPos targetPos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames())
            : Collections.emptyList();
    }
}
