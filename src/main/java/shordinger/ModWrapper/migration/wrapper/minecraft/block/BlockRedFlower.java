package shordinger.ModWrapper.migration.wrapper.minecraft.block;

public class BlockRedFlower extends BlockFlower {

    /**
     * Get the Type of this flower (Yellow/Red)
     */
    public BlockFlower.EnumFlowerColor getBlockType() {
        return BlockFlower.EnumFlowerColor.RED;
    }
}
