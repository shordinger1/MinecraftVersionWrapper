package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WrapperBlockSand extends WrapperBlockFalling {

    public static final PropertyEnum<WrapperBlockSand.EnumType> VARIANT = PropertyEnum.<WrapperBlockSand.EnumType>create(
        "variant",
        WrapperBlockSand.EnumType.class);

    public WrapperBlockSand() {
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(VARIANT, WrapperBlockSand.EnumType.SAND));
    }

    /**
     * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
     * returns the metadata of the dropped item based on the old metadata of the block.
     */
    public int damageDropped(IWrapperBlockState state) {
        return ((WrapperBlockSand.EnumType) state.getValue(VARIANT)).getMetadata();
    }

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (WrapperBlockSand.EnumType blocksand$enumtype : WrapperBlockSand.EnumType.values()) {
            items.add(new ItemStack(this, 1, blocksand$enumtype.getMetadata()));
        }
    }

    /**
     * Get the MapColor for this Block and the given BlockState
     */
    public MapColor getMapColor(IWrapperBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return ((WrapperBlockSand.EnumType) state.getValue(VARIANT)).getMapColor();
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IWrapperBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
            .withProperty(VARIANT, WrapperBlockSand.EnumType.byMetadata(meta));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        return ((WrapperBlockSand.EnumType) state.getValue(VARIANT)).getMetadata();
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { VARIANT });
    }

    @SideOnly(Side.CLIENT)
    public int getDustColor(IWrapperBlockState state) {
        WrapperBlockSand.EnumType blocksand$enumtype = (WrapperBlockSand.EnumType) state.getValue(VARIANT);
        return blocksand$enumtype.getDustColor();
    }

    public static enum EnumType implements IStringSerializable {

        SAND(0, "sand", "default", MapColor.SAND, -2370656),
        RED_SAND(1, "red_sand", "red", MapColor.ADOBE, -5679071);

        private static final WrapperBlockSand.EnumType[] META_LOOKUP = new WrapperBlockSand.EnumType[values().length];
        private final int meta;
        private final String name;
        private final MapColor mapColor;
        private final String unlocalizedName;
        private final int dustColor;

        private EnumType(int p_i47157_3_, String p_i47157_4_, String p_i47157_5_, MapColor p_i47157_6_,
            int p_i47157_7_) {
            this.meta = p_i47157_3_;
            this.name = p_i47157_4_;
            this.mapColor = p_i47157_6_;
            this.unlocalizedName = p_i47157_5_;
            this.dustColor = p_i47157_7_;
        }

        @SideOnly(Side.CLIENT)
        public int getDustColor() {
            return this.dustColor;
        }

        public int getMetadata() {
            return this.meta;
        }

        public String toString() {
            return this.name;
        }

        public MapColor getMapColor() {
            return this.mapColor;
        }

        public static WrapperBlockSand.EnumType byMetadata(int meta) {
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
            for (WrapperBlockSand.EnumType blocksand$enumtype : values()) {
                META_LOOKUP[blocksand$enumtype.getMetadata()] = blocksand$enumtype;
            }
        }
    }
}
