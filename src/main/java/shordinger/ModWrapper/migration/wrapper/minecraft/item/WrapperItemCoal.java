package shordinger.ModWrapper.migration.wrapper.minecraft.item;

import net.minecraft.creativetab.CreativeTabs;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.NonNullList;

public class WrapperItemCoal extends WrapperItem {

    public WrapperItemCoal() {
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setCreativeTab(CreativeTabs.MATERIALS);
    }

    /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     */
    public String getUnlocalizedName(TempItemStack stack) {
        return stack.getMetadata() == 1 ? "item.charcoal" : "item.coal";
    }

    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    public void getSubItems(CreativeTabs tab, NonNullList<TempItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            items.add(new TempItemStack(this, 1, 0));
            items.add(new TempItemStack(this, 1, 1));
        }
    }
}
