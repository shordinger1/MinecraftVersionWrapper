package shordinger.ModWrapper.migration.wrapper.minecraft.item;

import net.minecraft.block.Block;

public class WrapperItemCloth extends WrapperItemBlock {

    public WrapperItemCloth(Block block) {
        super(block);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    /**
     * Converts the given ItemStack damage value into a metadata value to be placed in the world when this Item is
     * placed as a Block (mostly used with ItemBlocks).
     */
    public int getMetadata(int damage) {
        return damage;
    }

    /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     */
    public String getUnlocalizedName(TempItemStack stack) {
        return super.getUnlocalizedName() + "."
            + EnumDyeColor.byMetadata(stack.getMetadata())
                .getUnlocalizedName();
    }
}
