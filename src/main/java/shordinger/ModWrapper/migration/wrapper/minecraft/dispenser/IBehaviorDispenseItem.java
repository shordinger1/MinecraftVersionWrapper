package shordinger.ModWrapper.migration.wrapper.minecraft.dispenser;

import net.minecraft.item.ItemStack;

public interface IBehaviorDispenseItem {

    IBehaviorDispenseItem DEFAULT_BEHAVIOR = new IBehaviorDispenseItem() {

        /**
         * Dispenses the specified ItemStack from a dispenser.
         */
        public ItemStack dispense(IWrapperBlockSource source, ItemStack stack) {
            return stack;
        }
    };

    /**
     * Dispenses the specified ItemStack from a dispenser.
     */
    ItemStack dispense(IWrapperBlockSource source, ItemStack stack);
}
