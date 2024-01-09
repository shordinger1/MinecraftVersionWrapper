package shordinger.ModWrapper.migration.wrapper.minecraft.block.state.pattern;

import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IWrapperBlockState;

import com.google.common.base.Predicate;

public class BlockMaterialMatcher implements Predicate<IWrapperBlockState> {

    private final Material material;

    private BlockMaterialMatcher(Material materialIn) {
        this.material = materialIn;
    }

    public static BlockMaterialMatcher forMaterial(Material materialIn) {
        return new BlockMaterialMatcher(materialIn);
    }

    public boolean apply(@Nullable IWrapperBlockState p_apply_1_) {
        return p_apply_1_ != null && p_apply_1_.getMaterial() == this.material;
    }
}
