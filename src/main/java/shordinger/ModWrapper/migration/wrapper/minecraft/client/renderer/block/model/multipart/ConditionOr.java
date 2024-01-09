package shordinger.ModWrapper.migration.wrapper.minecraft.client.renderer.block.model.multipart;

import javax.annotation.Nullable;

import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ConditionOr implements ICondition {

    final Iterable<ICondition> conditions;

    public ConditionOr(Iterable<ICondition> conditionsIn) {
        this.conditions = conditionsIn;
    }

    public Predicate<IWrapperBlockState> getPredicate(final BlockStateContainer blockState) {
        return Predicates
            .or(Iterables.transform(this.conditions, new Function<ICondition, Predicate<IWrapperBlockState>>() {

                @Nullable
                public Predicate<IWrapperBlockState> apply(@Nullable ICondition p_apply_1_) {
                    return p_apply_1_ == null ? null : p_apply_1_.getPredicate(blockState);
                }
            }));
    }
}
