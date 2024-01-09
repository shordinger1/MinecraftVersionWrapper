package shordinger.ModWrapper.migration.wrapper.minecraft.block.state.pattern;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

public class BlockStateMatcher implements Predicate<IWrapperBlockState> {

    public static final Predicate<IWrapperBlockState> ANY = new Predicate<IWrapperBlockState>() {

        public boolean apply(@Nullable IWrapperBlockState p_apply_1_) {
            return true;
        }
    };
    private final BlockStateContainer blockstate;
    private final Map<IProperty<?>, Predicate<?>> propertyPredicates = Maps.<IProperty<?>, Predicate<?>>newHashMap();

    private BlockStateMatcher(BlockStateContainer blockStateIn) {
        this.blockstate = blockStateIn;
    }

    public static BlockStateMatcher forBlock(Block blockIn) {
        return new BlockStateMatcher(blockIn.getBlockState());
    }

    public boolean apply(@Nullable IWrapperBlockState p_apply_1_) {
        if (p_apply_1_ != null && p_apply_1_.getBlock()
            .equals(this.blockstate.getBlock())) {
            if (this.propertyPredicates.isEmpty()) {
                return true;
            } else {
                for (Entry<IProperty<?>, Predicate<?>> entry : this.propertyPredicates.entrySet()) {
                    if (!this.matches(p_apply_1_, (IProperty) entry.getKey(), (Predicate) entry.getValue())) {
                        return false;
                    }
                }

                return true;
            }
        } else {
            return false;
        }
    }

    protected <T extends Comparable<T>> boolean matches(IWrapperBlockState blockState, IProperty<T> property,
        Predicate<T> predicate) {
        return predicate.apply(blockState.getValue(property));
    }

    public <V extends Comparable<V>> BlockStateMatcher where(IProperty<V> property, Predicate<? extends V> is) {
        if (!this.blockstate.getProperties()
            .contains(property)) {
            throw new IllegalArgumentException(this.blockstate + " cannot support property " + property);
        } else {
            this.propertyPredicates.put(property, is);
            return this;
        }
    }
}
