package shordinger.ModWrapper.migration.wrapper.minecraft.command;

public interface ICommandListener {

    /**
     * Send an informative message to the server operators
     */
    void notifyListener(IWrapperCommandSender sender, ICommand command, int flags, String translationKey,
                        Object... translationArgs);
}
