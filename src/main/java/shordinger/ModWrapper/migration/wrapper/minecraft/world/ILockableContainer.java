package shordinger.ModWrapper.migration.wrapper.minecraft.world;

import net.minecraft.inventory.IInventory;

public interface ILockableContainer extends IInventory, IInteractionObject {

    boolean isLocked();

    void setLockCode(LockCode code);

    LockCode getLockCode();
}
