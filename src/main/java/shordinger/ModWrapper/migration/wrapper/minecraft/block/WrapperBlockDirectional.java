package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;

public abstract class WrapperBlockDirectional extends WrapperBlock {

    public static final PropertyDirection FACING = PropertyDirection.create("facing");

    protected WrapperBlockDirectional(Material materialIn) {
        super(materialIn);
    }
}
