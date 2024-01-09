package shordinger.ModWrapper.migration.wrapper.minecraft.client.tutorial;

import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MouseHelper;
import net.minecraft.util.MovementInput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.RayTraceResult;

@SideOnly(Side.CLIENT)
public interface ITutorialStep {

    default void onStop() {}

    default void update() {}

    default void handleMovement(MovementInput input) {}

    default void handleMouse(MouseHelper mouseHelperIn) {}

    default void onMouseHover(WorldClient worldIn, RayTraceResult result) {}

    default void onHitBlock(WorldClient worldIn, BlockPos pos, IWrapperBlockState state, float diggingStage) {}

    default void openInventory() {}

    default void handleSetSlot(ItemStack stack) {}
}
