package shordinger.ModWrapper.migration.wrapper.minecraft.client.renderer.block.model.multipart;

import javax.annotation.Nullable;

import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;

import com.google.common.base.Predicate;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface ICondition {

    ICondition TRUE = new ICondition() {

        public Predicate<IWrapperBlockState> getPredicate(BlockStateContainer blockState) {
            return new Predicate<IWrapperBlockState>() {

                public boolean apply(@Nullable IWrapperBlockState p_apply_1_) {
                    return true;
                }
            };
        }
    };
    ICondition FALSE = new ICondition() {

        public Predicate<IWrapperBlockState> getPredicate(BlockStateContainer blockState) {
            return new Predicate<IWrapperBlockState>() {

                public boolean apply(@Nullable IWrapperBlockState p_apply_1_) {
                    return false;
                }
            };
        }
    };

    Predicate<IWrapperBlockState> getPredicate(BlockStateContainer blockState);
}
