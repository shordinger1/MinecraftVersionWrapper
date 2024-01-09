package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.world.IBlockAccess;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WrapperBlockPlanks extends WrapperBlock {

    public static final PropertyEnum<WrapperBlockPlanks.EnumType> VARIANT = PropertyEnum.<WrapperBlockPlanks.EnumType>create(
        "variant",
        WrapperBlockPlanks.EnumType.class);

    public WrapperBlockPlanks() {
        super(Material.WOOD);
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(VARIANT, WrapperBlockPlanks.EnumType.OAK));
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    /**
     * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
     * returns the metadata of the dropped item based on the old metadata of the block.
     */
    public int damageDropped(IWrapperBlockState state) {
        return ((WrapperBlockPlanks.EnumType) state.getValue(VARIANT)).getMetadata();
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
        return this.getDefaultState()
            .withProperty(VARIANT, WrapperBlockPlanks.EnumType.byMetadata(meta));
    }

    /**
     * Get the MapColor for this Block and the given BlockState
     */
    public MapColor getMapColor(IWrapperBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return ((WrapperBlockPlanks.EnumType) state.getValue(VARIANT)).getMapColor();
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        return ((WrapperBlockPlanks.EnumType) state.getValue(VARIANT)).getMetadata();
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { VARIANT });
    }

    public static enum EnumType implements IStringSerializable {

        OAK(0, "oak", MapColor.WOOD),
        SPRUCE(1, "spruce", MapColor.OBSIDIAN),
        BIRCH(2, "birch", MapColor.SAND),
        JUNGLE(3, "jungle", MapColor.DIRT),
        ACACIA(4, "acacia", MapColor.ADOBE),
        DARK_OAK(5, "dark_oak", "big_oak", MapColor.BROWN);

        private static final WrapperBlockPlanks.EnumType[] META_LOOKUP = new WrapperBlockPlanks.EnumType[values().length];
        private final int meta;
        private final String name;
        private final String unlocalizedName;
        /** The color that represents this entry on a map. */
        private final MapColor mapColor;

        private EnumType(int metaIn, String nameIn, MapColor mapColorIn) {
            this(metaIn, nameIn, nameIn, mapColorIn);
        }

        private EnumType(int metaIn, String nameIn, String unlocalizedNameIn, MapColor mapColorIn) {
            this.meta = metaIn;
            this.name = nameIn;
            this.unlocalizedName = unlocalizedNameIn;
            this.mapColor = mapColorIn;
        }

        public int getMetadata() {
            return this.meta;
        }

        /**
         * The color which represents this entry on a map.
         */
        public MapColor getMapColor() {
            return this.mapColor;
        }

        public String toString() {
            return this.name;
        }

        public static WrapperBlockPlanks.EnumType byMetadata(int meta) {
            if (meta < 0 || meta >= META_LOOKUP.length) {
                meta = 0;
            }

            return META_LOOKUP[meta];
        }

        public String getName() {
            return this.name;
        }

        public String getUnlocalizedName() {
            return this.unlocalizedName;
        }

        static {
            for (WrapperBlockPlanks.EnumType blockplanks$enumtype : values()) {
                META_LOOKUP[blockplanks$enumtype.getMetadata()] = blockplanks$enumtype;
            }
        }
    }
}
