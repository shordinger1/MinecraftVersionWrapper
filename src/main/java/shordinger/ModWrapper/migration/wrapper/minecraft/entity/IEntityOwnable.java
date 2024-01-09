package shordinger.ModWrapper.migration.wrapper.minecraft.entity;

import java.util.UUID;

import javax.annotation.Nullable;

public interface IEntityOwnable {

    @Nullable
    UUID getOwnerId();

    @Nullable
    Entity getOwner();
}
