package shordinger.ModWrapper.migration.wrapper.minecraft.item;

import net.minecraft.block.Block;

public class WrapperItemColored extends WrapperItemBlock {

    private String[] subtypeNames;

    public WrapperItemColored(Block block, boolean hasSubtypes) {
        super(block);

        if (hasSubtypes) {
            this.setMaxDamage(0);
            this.setHasSubtypes(true);
        }
    }

    /**
     * Converts the given ItemStack damage value into a metadata value to be placed in the world when this Item is
     * placed as a Block (mostly used with ItemBlocks).
     */
    public int getMetadata(int damage) {
        return damage;
    }

    public WrapperItemColored setSubtypeNames(String[] names) {
        this.subtypeNames = names;
        return this;
    }

    /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     */
    public String getUnlocalizedName(TempItemStack stack) {
        if (this.subtypeNames == null) {
            return super.getUnlocalizedName(stack);
        } else {
            int i = stack.getMetadata();
            return i >= 0 && i < this.subtypeNames.length ? super.getUnlocalizedName(stack) + "." + this.subtypeNames[i]
                : super.getUnlocalizedName(stack);
        }
    }
}
