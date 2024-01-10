package shordinger.ModWrapper.migration.wrapper.minecraft.item;

import net.minecraft.block.Block;

public class WrapperItemShulkerBox extends WrapperItemBlock {

    public WrapperItemShulkerBox(Block blockInstance) {
        super(blockInstance);
        this.setMaxStackSize(1);
    }
}
