package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WrapperBlockDirt extends WrapperBlock {

    public static final PropertyEnum<WrapperBlockDirt.DirtType> VARIANT = PropertyEnum.<WrapperBlockDirt.DirtType>create(
        "variant",
        WrapperBlockDirt.DirtType.class);
    public static final PropertyBool SNOWY = PropertyBool.create("snowy");

    protected WrapperBlockDirt() {
        super(Material.GROUND);
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(VARIANT, WrapperBlockDirt.DirtType.DIRT)
                .withProperty(SNOWY, Boolean.valueOf(false)));
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    /**
     * Get the MapColor for this Block and the given BlockState
     */
    public MapColor getMapColor(IWrapperBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return ((WrapperBlockDirt.DirtType) state.getValue(VARIANT)).getColor();
    }

    /**
     * Get the actual Block state of this Block at the given position. This applies properties not visible in the
     * metadata, such as fence connections.
     */
    public IWrapperBlockState getActualState(IWrapperBlockState state, IBlockAccess worldIn, BlockPos pos) {
        if (state.getValue(VARIANT) == WrapperBlockDirt.DirtType.PODZOL) {
            WrapperBlock wrapperBlock = worldIn.getBlockState(pos.up())
                .getBlock();
            state = state
                .withProperty(SNOWY, Boolean.valueOf(wrapperBlock == Blocks.SNOW || wrapperBlock == Blocks.SNOW_LAYER));
        }

        return state;
    }

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        items.add(new ItemStack(this, 1, WrapperBlockDirt.DirtType.DIRT.getMetadata()));
        items.add(new ItemStack(this, 1, WrapperBlockDirt.DirtType.COARSE_DIRT.getMetadata()));
        items.add(new ItemStack(this, 1, WrapperBlockDirt.DirtType.PODZOL.getMetadata()));
    }

    public ItemStack getItem(World worldIn, BlockPos pos, IWrapperBlockState state) {
        return new ItemStack(this, 1, ((WrapperBlockDirt.DirtType) state.getValue(VARIANT)).getMetadata());
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IWrapperBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
            .withProperty(VARIANT, WrapperBlockDirt.DirtType.byMetadata(meta));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        return ((WrapperBlockDirt.DirtType) state.getValue(VARIANT)).getMetadata();
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { VARIANT, SNOWY });
    }

    /**
     * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
     * returns the metadata of the dropped item based on the old metadata of the block.
     */
    public int damageDropped(IWrapperBlockState state) {
        WrapperBlockDirt.DirtType blockdirt$dirttype = (WrapperBlockDirt.DirtType) state.getValue(VARIANT);

        if (blockdirt$dirttype == WrapperBlockDirt.DirtType.PODZOL) {
            blockdirt$dirttype = WrapperBlockDirt.DirtType.DIRT;
        }

        return blockdirt$dirttype.getMetadata();
    }

    public static enum DirtType implements IStringSerializable {

        DIRT(0, "dirt", "default", MapColor.DIRT),
        COARSE_DIRT(1, "coarse_dirt", "coarse", MapColor.DIRT),
        PODZOL(2, "podzol", MapColor.OBSIDIAN);

        private static final WrapperBlockDirt.DirtType[] METADATA_LOOKUP = new WrapperBlockDirt.DirtType[values().length];
        private final int metadata;
        private final String name;
        private final String unlocalizedName;
        private final MapColor color;

        private DirtType(int metadataIn, String nameIn, MapColor color) {
            this(metadataIn, nameIn, nameIn, color);
        }

        private DirtType(int metadataIn, String nameIn, String unlocalizedNameIn, MapColor color) {
            this.metadata = metadataIn;
            this.name = nameIn;
            this.unlocalizedName = unlocalizedNameIn;
            this.color = color;
        }

        public int getMetadata() {
            return this.metadata;
        }

        public String getUnlocalizedName() {
            return this.unlocalizedName;
        }

        public MapColor getColor() {
            return this.color;
        }

        public String toString() {
            return this.name;
        }

        public static WrapperBlockDirt.DirtType byMetadata(int metadata) {
            if (metadata < 0 || metadata >= METADATA_LOOKUP.length) {
                metadata = 0;
            }

            return METADATA_LOOKUP[metadata];
        }

        public String getName() {
            return this.name;
        }

        static {
            for (WrapperBlockDirt.DirtType blockdirt$dirttype : values()) {
                METADATA_LOOKUP[blockdirt$dirttype.getMetadata()] = blockdirt$dirttype;
            }
        }
    }
}
