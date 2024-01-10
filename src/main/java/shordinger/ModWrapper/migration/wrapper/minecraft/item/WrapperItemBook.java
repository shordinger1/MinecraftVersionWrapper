package shordinger.ModWrapper.migration.wrapper.minecraft.item;

public class WrapperItemBook extends WrapperItem {

    /**
     * Checks isDamagable and if it cannot be stacked
     */
    public boolean isEnchantable(TempItemStack stack) {
        return stack.getCount() == 1;
    }

    /**
     * Return the enchantability factor of the item, most of the time is based on material.
     */
    public int getItemEnchantability() {
        return 1;
    }
}
