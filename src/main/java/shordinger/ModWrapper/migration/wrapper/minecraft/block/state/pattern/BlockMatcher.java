package shordinger.ModWrapper.migration.wrapper.minecraft.block.state.pattern;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IWrapperBlockState;

import com.google.common.base.Predicate;

public class BlockMatcher implements Predicate<IWrapperBlockState> {

    private final Block block;

    private BlockMatcher(Block blockType) {
        this.block = blockType;
    }

    public static BlockMatcher forBlock(Block blockType) {
        return new BlockMatcher(blockType);
    }

    public boolean apply(@Nullable IWrapperBlockState p_apply_1_) {
        return p_apply_1_ != null && p_apply_1_.getBlock() == this.block;
    }
}
