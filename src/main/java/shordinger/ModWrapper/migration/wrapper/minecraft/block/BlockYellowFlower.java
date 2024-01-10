package shordinger.ModWrapper.migration.wrapper.minecraft.block;

public class BlockYellowFlower extends BlockFlower {

    /**
     * Get the Type of this flower (Yellow/Red)
     */
    public BlockFlower.EnumFlowerColor getBlockType() {
        return BlockFlower.EnumFlowerColor.YELLOW;
    }
}
