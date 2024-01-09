package shordinger.ModWrapper.migration.wrapper.minecraft.block;

public class WrapperBlockRedFlower extends WrapperBlockFlower {

    /**
     * Get the Type of this flower (Yellow/Red)
     */
    public WrapperBlockFlower.EnumFlowerColor getBlockType() {
        return WrapperBlockFlower.EnumFlowerColor.RED;
    }
}
