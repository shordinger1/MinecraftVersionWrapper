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
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WrapperBlockStone extends WrapperBlock {

    public static final PropertyEnum<WrapperBlockStone.EnumType> VARIANT = PropertyEnum.<WrapperBlockStone.EnumType>create(
        "variant",
        WrapperBlockStone.EnumType.class);

    public WrapperBlockStone() {
        super(Material.ROCK);
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(VARIANT, WrapperBlockStone.EnumType.STONE));
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    /**
     * Gets the localized name of this block. Used for the statistics page.
     */
    public String getLocalizedName() {
        return I18n.translateToLocal(
            this.getUnlocalizedName() + "." + WrapperBlockStone.EnumType.STONE.getUnlocalizedName() + ".name");
    }

    /**
     * Get the MapColor for this Block and the given BlockState
     */
    public MapColor getMapColor(IWrapperBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return ((WrapperBlockStone.EnumType) state.getValue(VARIANT)).getMapColor();
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    public Item getItemDropped(IWrapperBlockState state, Random rand, int fortune) {
        return state.getValue(VARIANT) == WrapperBlockStone.EnumType.STONE ? Item.getItemFromBlock(Blocks.COBBLESTONE)
            : Item.getItemFromBlock(Blocks.STONE);
    }

    /**
     * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
     * returns the metadata of the dropped item based on the old metadata of the block.
     */
    public int damageDropped(IWrapperBlockState state) {
        return ((WrapperBlockStone.EnumType) state.getValue(VARIANT)).getMetadata();
    }

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (WrapperBlockStone.EnumType blockstone$enumtype : WrapperBlockStone.EnumType.values()) {
            items.add(new ItemStack(this, 1, blockstone$enumtype.getMetadata()));
        }
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IWrapperBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
            .withProperty(VARIANT, WrapperBlockStone.EnumType.byMetadata(meta));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        return ((WrapperBlockStone.EnumType) state.getValue(VARIANT)).getMetadata();
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { VARIANT });
    }

    public static enum EnumType implements IStringSerializable {

        STONE(0, MapColor.STONE, "stone", true),
        GRANITE(1, MapColor.DIRT, "granite", true),
        GRANITE_SMOOTH(2, MapColor.DIRT, "smooth_granite", "graniteSmooth", false),
        DIORITE(3, MapColor.QUARTZ, "diorite", true),
        DIORITE_SMOOTH(4, MapColor.QUARTZ, "smooth_diorite", "dioriteSmooth", false),
        ANDESITE(5, MapColor.STONE, "andesite", true),
        ANDESITE_SMOOTH(6, MapColor.STONE, "smooth_andesite", "andesiteSmooth", false);

        /** Array of the Block's BlockStates */
        private static final WrapperBlockStone.EnumType[] META_LOOKUP = new WrapperBlockStone.EnumType[values().length];
        /** The BlockState's metadata. */
        private final int meta;
        /** The EnumType's name. */
        private final String name;
        private final String unlocalizedName;
        private final MapColor mapColor;
        private final boolean isNatural;

        private EnumType(int p_i46383_3_, MapColor p_i46383_4_, String p_i46383_5_, boolean p_i46383_6_) {
            this(p_i46383_3_, p_i46383_4_, p_i46383_5_, p_i46383_5_, p_i46383_6_);
        }

        private EnumType(int p_i46384_3_, MapColor p_i46384_4_, String p_i46384_5_, String p_i46384_6_,
            boolean p_i46384_7_) {
            this.meta = p_i46384_3_;
            this.name = p_i46384_5_;
            this.unlocalizedName = p_i46384_6_;
            this.mapColor = p_i46384_4_;
            this.isNatural = p_i46384_7_;
        }

        /**
         * Returns the EnumType's metadata value.
         */
        public int getMetadata() {
            return this.meta;
        }

        public MapColor getMapColor() {
            return this.mapColor;
        }

        public String toString() {
            return this.name;
        }

        /**
         * Returns an EnumType for the BlockState from a metadata value.
         */
        public static WrapperBlockStone.EnumType byMetadata(int meta) {
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

        public boolean isNatural() {
            return this.isNatural;
        }

        static {
            for (WrapperBlockStone.EnumType blockstone$enumtype : values()) {
                META_LOOKUP[blockstone$enumtype.getMetadata()] = blockstone$enumtype;
            }
        }
    }
}
