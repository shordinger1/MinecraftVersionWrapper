package shordinger.ModWrapper.migration.wrapper.minecraft.item;

import net.minecraft.block.Block;

public class WrapperItemPiston extends WrapperItemBlock {

    public WrapperItemPiston(Block block) {
        super(block);
    }

    /**
     * Converts the given ItemStack damage value into a metadata value to be placed in the world when this Item is
     * placed as a Block (mostly used with ItemBlocks).
     */
    public int getMetadata(int damage) {
        return 7;
    }
}
