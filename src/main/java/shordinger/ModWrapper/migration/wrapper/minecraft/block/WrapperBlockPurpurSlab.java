package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import java.util.Random;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.World;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public abstract class WrapperBlockPurpurSlab extends WrapperBlockSlab {

    public static final PropertyEnum<WrapperBlockPurpurSlab.Variant> VARIANT = PropertyEnum.<WrapperBlockPurpurSlab.Variant>create(
        "variant",
        WrapperBlockPurpurSlab.Variant.class);

    public WrapperBlockPurpurSlab() {
        super(Material.ROCK, MapColor.MAGENTA);
        IWrapperBlockState iblockstate = this.blockState.getBaseState();

        if (!this.isDouble()) {
            iblockstate = iblockstate.withProperty(HALF, WrapperBlockSlab.EnumBlockHalf.BOTTOM);
        }

        this.setDefaultState(iblockstate.withProperty(VARIANT, WrapperBlockPurpurSlab.Variant.DEFAULT));
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    public Item getItemDropped(IWrapperBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(Blocks.PURPUR_SLAB);
    }

    public ItemStack getItem(World worldIn, BlockPos pos, IWrapperBlockState state) {
        return new ItemStack(Blocks.PURPUR_SLAB);
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IWrapperBlockState getStateFromMeta(int meta) {
        IWrapperBlockState iblockstate = this.getDefaultState()
            .withProperty(VARIANT, WrapperBlockPurpurSlab.Variant.DEFAULT);

        if (!this.isDouble()) {
            iblockstate = iblockstate.withProperty(
                HALF,
                (meta & 8) == 0 ? WrapperBlockSlab.EnumBlockHalf.BOTTOM : WrapperBlockSlab.EnumBlockHalf.TOP);
        }

        return iblockstate;
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        int i = 0;

        if (!this.isDouble() && state.getValue(HALF) == WrapperBlockSlab.EnumBlockHalf.TOP) {
            i |= 8;
        }

        return i;
    }

    protected BlockStateContainer createBlockState() {
        return this.isDouble() ? new BlockStateContainer(this, new IProperty[] { VARIANT })
            : new BlockStateContainer(this, new IProperty[] { HALF, VARIANT });
    }

    /**
     * Returns the slab block name with the type associated with it
     */
    public String getUnlocalizedName(int meta) {
        return super.getUnlocalizedName();
    }

    public IProperty<?> getVariantProperty() {
        return VARIANT;
    }

    public Comparable<?> getTypeForItem(ItemStack stack) {
        return WrapperBlockPurpurSlab.Variant.DEFAULT;
    }

    public static class Double extends WrapperBlockPurpurSlab {

        public boolean isDouble() {
            return true;
        }
    }

    public static class Half extends WrapperBlockPurpurSlab {

        public boolean isDouble() {
            return false;
        }
    }

    public static enum Variant implements IStringSerializable {

        DEFAULT;

        public String getName() {
            return "default";
        }
    }
}
