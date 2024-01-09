package shordinger.ModWrapper.migration.wrapper.minecraft.block;

public class WrapperBlockYellowFlower extends WrapperBlockFlower {

    /**
     * Get the Type of this flower (Yellow/Red)
     */
    public WrapperBlockFlower.EnumFlowerColor getBlockType() {
        return WrapperBlockFlower.EnumFlowerColor.YELLOW;
    }
}
