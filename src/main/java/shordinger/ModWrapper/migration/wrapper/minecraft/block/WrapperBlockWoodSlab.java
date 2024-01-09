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
import net.minecraft.util.NonNullList;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public abstract class WrapperBlockWoodSlab extends WrapperBlockSlab {

    public static final PropertyEnum<WrapperBlockPlanks.EnumType> VARIANT = PropertyEnum.<WrapperBlockPlanks.EnumType>create(
        "variant",
        WrapperBlockPlanks.EnumType.class);

    public WrapperBlockWoodSlab() {
        super(Material.WOOD);
        IWrapperBlockState iblockstate = this.blockState.getBaseState();

        if (!this.isDouble()) {
            iblockstate = iblockstate.withProperty(HALF, WrapperBlockSlab.EnumBlockHalf.BOTTOM);
        }

        this.setDefaultState(iblockstate.withProperty(VARIANT, WrapperBlockPlanks.EnumType.OAK));
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    /**
     * Get the MapColor for this Block and the given BlockState
     */
    public MapColor getMapColor(IWrapperBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return ((WrapperBlockPlanks.EnumType) state.getValue(VARIANT)).getMapColor();
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    public Item getItemDropped(IWrapperBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(Blocks.WOODEN_SLAB);
    }

    public ItemStack getItem(World worldIn, BlockPos pos, IWrapperBlockState state) {
        return new ItemStack(
            Blocks.WOODEN_SLAB,
            1,
            ((WrapperBlockPlanks.EnumType) state.getValue(VARIANT)).getMetadata());
    }

    /**
     * Returns the slab block name with the type associated with it
     */
    public String getUnlocalizedName(int meta) {
        return super.getUnlocalizedName() + "."
            + WrapperBlockPlanks.EnumType.byMetadata(meta)
                .getUnlocalizedName();
    }

    public IProperty<?> getVariantProperty() {
        return VARIANT;
    }

    public Comparable<?> getTypeForItem(ItemStack stack) {
        return WrapperBlockPlanks.EnumType.byMetadata(stack.getMetadata() & 7);
    }

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (WrapperBlockPlanks.EnumType blockplanks$enumtype : WrapperBlockPlanks.EnumType.values()) {
            items.add(new ItemStack(this, 1, blockplanks$enumtype.getMetadata()));
        }
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IWrapperBlockState getStateFromMeta(int meta) {
        IWrapperBlockState iblockstate = this.getDefaultState()
            .withProperty(VARIANT, WrapperBlockPlanks.EnumType.byMetadata(meta & 7));

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
        i = i | ((WrapperBlockPlanks.EnumType) state.getValue(VARIANT)).getMetadata();

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
     * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
     * returns the metadata of the dropped item based on the old metadata of the block.
     */
    public int damageDropped(IWrapperBlockState state) {
        return ((WrapperBlockPlanks.EnumType) state.getValue(VARIANT)).getMetadata();
    }
}
