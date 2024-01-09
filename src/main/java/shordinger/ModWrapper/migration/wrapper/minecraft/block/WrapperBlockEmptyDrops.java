package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

public class WrapperBlockEmptyDrops extends WrapperBlock {

    public WrapperBlockEmptyDrops(Material materialIn) {
        super(materialIn);
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    public int quantityDropped(Random random) {
        return 0;
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    public Item getItemDropped(IWrapperBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }
}
