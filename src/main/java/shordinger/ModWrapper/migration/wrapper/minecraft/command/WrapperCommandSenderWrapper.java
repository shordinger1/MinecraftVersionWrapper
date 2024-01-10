package shordinger.ModWrapper.migration.wrapper.minecraft.command;

import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.Vec3d;

public class WrapperCommandSenderWrapper implements IWrapperCommandSender {

    private final IWrapperCommandSender delegate;
    @Nullable
    private final Vec3d positionVector;
    @Nullable
    private final BlockPos position;
    @Nullable
    private final Integer permissionLevel;
    @Nullable
    private final Entity entity;
    @Nullable
    private final Boolean sendCommandFeedback;

    public WrapperCommandSenderWrapper(IWrapperCommandSender delegateIn, @Nullable Vec3d positionVectorIn,
                                       @Nullable BlockPos positionIn, @Nullable Integer permissionLevelIn, @Nullable Entity entityIn,
                                       @Nullable Boolean sendCommandFeedbackIn) {
        this.delegate = delegateIn;
        this.positionVector = positionVectorIn;
        this.position = positionIn;
        this.permissionLevel = permissionLevelIn;
        this.entity = entityIn;
        this.sendCommandFeedback = sendCommandFeedbackIn;
    }

    public static WrapperCommandSenderWrapper create(IWrapperCommandSender sender) {
        return sender instanceof WrapperCommandSenderWrapper ? (WrapperCommandSenderWrapper) sender
            : new WrapperCommandSenderWrapper(
                sender,
                (Vec3d) null,
                (BlockPos) null,
                (Integer) null,
                (Entity) null,
                (Boolean) null);
    }

    public WrapperCommandSenderWrapper withEntity(Entity entityIn, Vec3d p_193997_2_) {
        return this.entity == entityIn && Objects.equals(this.positionVector, p_193997_2_) ? this
            : new WrapperCommandSenderWrapper(
                this.delegate,
                p_193997_2_,
                new BlockPos(p_193997_2_),
                this.permissionLevel,
                entityIn,
                this.sendCommandFeedback);
    }

    public WrapperCommandSenderWrapper withPermissionLevel(int level) {
        return this.permissionLevel != null && this.permissionLevel.intValue() <= level ? this
            : new WrapperCommandSenderWrapper(
                this.delegate,
                this.positionVector,
                this.position,
                level,
                this.entity,
                this.sendCommandFeedback);
    }

    public WrapperCommandSenderWrapper withSendCommandFeedback(boolean sendCommandFeedbackIn) {
        return this.sendCommandFeedback == null || this.sendCommandFeedback.booleanValue() && !sendCommandFeedbackIn
            ? new WrapperCommandSenderWrapper(
                this.delegate,
                this.positionVector,
                this.position,
                this.permissionLevel,
                this.entity,
                sendCommandFeedbackIn)
            : this;
    }

    public WrapperCommandSenderWrapper computePositionVector() {
        return this.positionVector != null ? this
            : new WrapperCommandSenderWrapper(
                this.delegate,
                this.getPositionVector(),
                this.getPosition(),
                this.permissionLevel,
                this.entity,
                this.sendCommandFeedback);
    }

    /**
     * Get the name of this object. For players this returns their username
     */
    public String getName() {
        return this.entity != null ? this.entity.getName() : this.delegate.getName();
    }

    /**
     * Get the formatted ChatComponent that will be used for the sender's username in chat
     */
    public ITextComponent getDisplayName() {
        return this.entity != null ? this.entity.getDisplayName() : this.delegate.getDisplayName();
    }

    /**
     * Send a chat message to the CommandSender
     */
    public void sendMessage(ITextComponent component) {
        if (this.sendCommandFeedback == null || this.sendCommandFeedback.booleanValue()) {
            this.delegate.sendMessage(component);
        }
    }

    /**
     * Returns {@code true} if the CommandSender is allowed to execute the command, {@code false} if not
     */
    public boolean canUseCommand(int permLevel, String commandName) {
        return this.permissionLevel != null && this.permissionLevel.intValue() < permLevel ? false
            : this.delegate.canUseCommand(permLevel, commandName);
    }

    /**
     * Get the position in the world. <b>{@code null} is not allowed!</b> If you are not an entity in the world, return
     * the coordinates 0, 0, 0
     */
    public BlockPos getPosition() {
        if (this.position != null) {
            return this.position;
        } else {
            return this.entity != null ? this.entity.getPosition() : this.delegate.getPosition();
        }
    }

    /**
     * Get the position vector. <b>{@code null} is not allowed!</b> If you are not an entity in the world, return 0.0D,
     * 0.0D, 0.0D
     */
    public Vec3d getPositionVector() {
        if (this.positionVector != null) {
            return this.positionVector;
        } else {
            return this.entity != null ? this.entity.getPositionVector() : this.delegate.getPositionVector();
        }
    }

    /**
     * Get the world, if available. <b>{@code null} is not allowed!</b> If you are not an entity in the world, return
     * the overworld
     */
    public World getEntityWorld() {
        return this.entity != null ? this.entity.getEntityWorld() : this.delegate.getEntityWorld();
    }

    /**
     * Returns the entity associated with the command sender. MAY BE NULL!
     */
    @Nullable
    public Entity getCommandSenderEntity() {
        return this.entity != null ? this.entity.getCommandSenderEntity() : this.delegate.getCommandSenderEntity();
    }

    /**
     * Returns true if the command sender should be sent feedback about executed commands
     */
    public boolean sendCommandFeedback() {
        return this.sendCommandFeedback != null ? this.sendCommandFeedback.booleanValue()
            : this.delegate.sendCommandFeedback();
    }

    public void setCommandStat(CommandResultStats.Type type, int amount) {
        if (this.entity != null) {
            this.entity.setCommandStat(type, amount);
        } else {
            this.delegate.setCommandStat(type, amount);
        }
    }

    /**
     * Get the Minecraft server instance
     */
    @Nullable
    public MinecraftServer getServer() {
        return this.delegate.getServer();
    }
}
