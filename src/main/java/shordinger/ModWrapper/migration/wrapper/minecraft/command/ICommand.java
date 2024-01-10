package shordinger.ModWrapper.migration.wrapper.minecraft.command;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.server.MinecraftServer;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public interface ICommand extends net.minecraft.command.ICommand {

    /**
     * Gets the name of the command
     */
    String getName();

    /**
     * Gets the usage string for the command.
     */
    String getUsage(IWrapperCommandSender sender);

    /**
     * Get a list of aliases for this command. <b>Never return null!</b>
     */
    List<String> getAliases();

    /**
     * Callback for when the command is executed
     */
    void execute(MinecraftServer server, IWrapperCommandSender sender, String[] args) throws CommandException;

    /**
     * Check if the given ICommandSender has permission to execute this command
     */
    boolean checkPermission(MinecraftServer server, IWrapperCommandSender sender);

    /**
     * Get a list of options for when the user presses the TAB key
     */
    List<String> getTabCompletions(MinecraftServer server, IWrapperCommandSender sender, String[] args,
                                   @Nullable BlockPos targetPos);

    /**
     * Return whether the specified command parameter index is a username parameter.
     */
    boolean isUsernameIndex(String[] args, int index);
}
