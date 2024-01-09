package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public class WrapperBlockPackedIce extends WrapperBlock {

    public WrapperBlockPackedIce() {
        super(Material.PACKED_ICE);
        this.slipperiness = 0.98F;
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    public int quantityDropped(Random random) {
        return 0;
    }
}
