package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import javax.annotation.Nullable;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.IBlockAccess;

import com.google.common.base.Predicate;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WrapperBlockNewLog extends WrapperBlockLog {

    public static final PropertyEnum<WrapperBlockPlanks.EnumType> VARIANT = PropertyEnum.<WrapperBlockPlanks.EnumType>create(
        "variant",
        WrapperBlockPlanks.EnumType.class,
        new Predicate<WrapperBlockPlanks.EnumType>() {

            public boolean apply(@Nullable WrapperBlockPlanks.EnumType p_apply_1_) {
                return p_apply_1_.getMetadata() >= 4;
            }
        });

    public WrapperBlockNewLog() {
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(VARIANT, WrapperBlockPlanks.EnumType.ACACIA)
                .withProperty(LOG_AXIS, WrapperBlockLog.EnumAxis.Y));
    }

    /**
     * Get the MapColor for this Block and the given BlockState
     */
    public MapColor getMapColor(IWrapperBlockState state, IBlockAccess worldIn, BlockPos pos) {
        WrapperBlockPlanks.EnumType blockplanks$enumtype = (WrapperBlockPlanks.EnumType) state.getValue(VARIANT);

        switch ((WrapperBlockLog.EnumAxis) state.getValue(LOG_AXIS)) {
            case X:
            case Z:
            case NONE:
            default:

                switch (blockplanks$enumtype) {
                    case ACACIA:
                    default:
                        return MapColor.STONE;
                    case DARK_OAK:
                        return WrapperBlockPlanks.EnumType.DARK_OAK.getMapColor();
                }

            case Y:
                return blockplanks$enumtype.getMapColor();
        }
    }

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        items.add(new ItemStack(this, 1, WrapperBlockPlanks.EnumType.ACACIA.getMetadata() - 4));
        items.add(new ItemStack(this, 1, WrapperBlockPlanks.EnumType.DARK_OAK.getMetadata() - 4));
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IWrapperBlockState getStateFromMeta(int meta) {
        IWrapperBlockState iblockstate = this.getDefaultState()
            .withProperty(VARIANT, WrapperBlockPlanks.EnumType.byMetadata((meta & 3) + 4));

        switch (meta & 12) {
            case 0:
                iblockstate = iblockstate.withProperty(LOG_AXIS, WrapperBlockLog.EnumAxis.Y);
                break;
            case 4:
                iblockstate = iblockstate.withProperty(LOG_AXIS, WrapperBlockLog.EnumAxis.X);
                break;
            case 8:
                iblockstate = iblockstate.withProperty(LOG_AXIS, WrapperBlockLog.EnumAxis.Z);
                break;
            default:
                iblockstate = iblockstate.withProperty(LOG_AXIS, WrapperBlockLog.EnumAxis.NONE);
        }

        return iblockstate;
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    @SuppressWarnings("incomplete-switch")
    public int getMetaFromState(IWrapperBlockState state) {
        int i = 0;
        i = i | ((WrapperBlockPlanks.EnumType) state.getValue(VARIANT)).getMetadata() - 4;

        switch ((WrapperBlockLog.EnumAxis) state.getValue(LOG_AXIS)) {
            case X:
                i |= 4;
                break;
            case Z:
                i |= 8;
                break;
            case NONE:
                i |= 12;
        }

        return i;
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { VARIANT, LOG_AXIS });
    }

    protected ItemStack getSilkTouchDrop(IWrapperBlockState state) {
        return new ItemStack(
            Item.getItemFromBlock(this),
            1,
            ((WrapperBlockPlanks.EnumType) state.getValue(VARIANT)).getMetadata() - 4);
    }

    /**
     * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
     * returns the metadata of the dropped item based on the old metadata of the block.
     */
    public int damageDropped(IWrapperBlockState state) {
        return ((WrapperBlockPlanks.EnumType) state.getValue(VARIANT)).getMetadata() - 4;
    }
}
