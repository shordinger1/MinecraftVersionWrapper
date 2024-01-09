package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.util.EnumFacing;

public abstract class WrapperBlockHorizontal extends WrapperBlock {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    protected WrapperBlockHorizontal(Material materialIn) {
        super(materialIn);
    }

    protected WrapperBlockHorizontal(Material materialIn, MapColor colorIn) {
        super(materialIn, colorIn);
    }
}
