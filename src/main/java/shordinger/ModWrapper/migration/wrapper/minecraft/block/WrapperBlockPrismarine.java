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
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WrapperBlockPrismarine extends WrapperBlock {

    public static final PropertyEnum<WrapperBlockPrismarine.EnumType> VARIANT = PropertyEnum.<WrapperBlockPrismarine.EnumType>create(
        "variant",
        WrapperBlockPrismarine.EnumType.class);
    public static final int ROUGH_META = WrapperBlockPrismarine.EnumType.ROUGH.getMetadata();
    public static final int BRICKS_META = WrapperBlockPrismarine.EnumType.BRICKS.getMetadata();
    public static final int DARK_META = WrapperBlockPrismarine.EnumType.DARK.getMetadata();

    public WrapperBlockPrismarine() {
        super(Material.ROCK);
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(VARIANT, WrapperBlockPrismarine.EnumType.ROUGH));
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    /**
     * Gets the localized name of this block. Used for the statistics page.
     */
    public String getLocalizedName() {
        return I18n.translateToLocal(
            this.getUnlocalizedName() + "." + WrapperBlockPrismarine.EnumType.ROUGH.getUnlocalizedName() + ".name");
    }

    /**
     * Get the MapColor for this Block and the given BlockState
     */
    public MapColor getMapColor(IWrapperBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return state.getValue(VARIANT) == WrapperBlockPrismarine.EnumType.ROUGH ? MapColor.CYAN : MapColor.DIAMOND;
    }

    /**
     * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
     * returns the metadata of the dropped item based on the old metadata of the block.
     */
    public int damageDropped(IWrapperBlockState state) {
        return ((WrapperBlockPrismarine.EnumType) state.getValue(VARIANT)).getMetadata();
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        return ((WrapperBlockPrismarine.EnumType) state.getValue(VARIANT)).getMetadata();
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { VARIANT });
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IWrapperBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
            .withProperty(VARIANT, WrapperBlockPrismarine.EnumType.byMetadata(meta));
    }

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        items.add(new ItemStack(this, 1, ROUGH_META));
        items.add(new ItemStack(this, 1, BRICKS_META));
        items.add(new ItemStack(this, 1, DARK_META));
    }

    public static enum EnumType implements IStringSerializable {

        ROUGH(0, "prismarine", "rough"),
        BRICKS(1, "prismarine_bricks", "bricks"),
        DARK(2, "dark_prismarine", "dark");

        private static final WrapperBlockPrismarine.EnumType[] META_LOOKUP = new WrapperBlockPrismarine.EnumType[values().length];
        private final int meta;
        private final String name;
        private final String unlocalizedName;

        private EnumType(int meta, String name, String unlocalizedName) {
            this.meta = meta;
            this.name = name;
            this.unlocalizedName = unlocalizedName;
        }

        public int getMetadata() {
            return this.meta;
        }

        public String toString() {
            return this.name;
        }

        public static WrapperBlockPrismarine.EnumType byMetadata(int meta) {
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
            for (WrapperBlockPrismarine.EnumType blockprismarine$enumtype : values()) {
                META_LOOKUP[blockprismarine$enumtype.getMetadata()] = blockprismarine$enumtype;
            }
        }
    }
}
