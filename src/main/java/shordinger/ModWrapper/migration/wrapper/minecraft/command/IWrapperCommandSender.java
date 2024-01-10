package shordinger.ModWrapper.migration.wrapper.minecraft.command;

import javax.annotation.Nullable;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import shordinger.ModWrapper.migration.wrapper.minecraft.entity.Entity;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.Vec3d;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.text.ITextComponent;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.text.TextComponentString;
import shordinger.ModWrapper.migration.wrapper.minecraft.world.World;

public interface IWrapperCommandSender extends ICommandSender {

    /**
     * Get the name of this object. For players this returns their username
     */
    String getName();

    default ITextComponent getDisplayName() {
        return new TextComponentString(this.getName());
    }

    default void sendMessage(ITextComponent component) {
    }

    /**
     * Returns {@code true} if the CommandSender is allowed to execute the command, {@code false} if not
     */
    default boolean canUseCommand(int permLevel, String commandName) {
        return canCommandSenderUseCommand(permLevel, commandName);
    }

    default BlockPos getPosition() {
        return BlockPos.ORIGIN;
    }

    default Vec3d getPositionVector() {
        return Vec3d.ZERO;
    }

    /**
     * Get the world, if available. <b>{@code null} is not allowed!</b> If you are not an entity in the world, return
     * the overworld
     */
    World getEntityWorld();

    @Nullable
    default Entity getCommandSenderEntity() {
        return null;
    }

    default boolean sendCommandFeedback() {
        return false;
    }

    default void setCommandStat(CommandResultStats.Type type, int amount) {
    }

    /**
     * Get the Minecraft server instance
     */
    @Nullable
    MinecraftServer getServer();
}
