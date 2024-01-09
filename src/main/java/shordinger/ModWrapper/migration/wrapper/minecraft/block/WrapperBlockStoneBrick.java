package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;

public class WrapperBlockStoneBrick extends WrapperBlock {

    public static final PropertyEnum<WrapperBlockStoneBrick.EnumType> VARIANT = PropertyEnum.<WrapperBlockStoneBrick.EnumType>create(
        "variant",
        WrapperBlockStoneBrick.EnumType.class);
    public static final int DEFAULT_META = WrapperBlockStoneBrick.EnumType.DEFAULT.getMetadata();
    public static final int MOSSY_META = WrapperBlockStoneBrick.EnumType.MOSSY.getMetadata();
    public static final int CRACKED_META = WrapperBlockStoneBrick.EnumType.CRACKED.getMetadata();
    public static final int CHISELED_META = WrapperBlockStoneBrick.EnumType.CHISELED.getMetadata();

    public WrapperBlockStoneBrick() {
        super(Material.ROCK);
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(VARIANT, WrapperBlockStoneBrick.EnumType.DEFAULT));
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    /**
     * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
     * returns the metadata of the dropped item based on the old metadata of the block.
     */
    public int damageDropped(IWrapperBlockState state) {
        return ((WrapperBlockStoneBrick.EnumType) state.getValue(VARIANT)).getMetadata();
    }

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (WrapperBlockStoneBrick.EnumType blockstonebrick$enumtype : WrapperBlockStoneBrick.EnumType.values()) {
            items.add(new ItemStack(this, 1, blockstonebrick$enumtype.getMetadata()));
        }
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IWrapperBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
            .withProperty(VARIANT, WrapperBlockStoneBrick.EnumType.byMetadata(meta));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        return ((WrapperBlockStoneBrick.EnumType) state.getValue(VARIANT)).getMetadata();
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { VARIANT });
    }

    public static enum EnumType implements IStringSerializable {

        DEFAULT(0, "stonebrick", "default"),
        MOSSY(1, "mossy_stonebrick", "mossy"),
        CRACKED(2, "cracked_stonebrick", "cracked"),
        CHISELED(3, "chiseled_stonebrick", "chiseled");

        private static final WrapperBlockStoneBrick.EnumType[] META_LOOKUP = new WrapperBlockStoneBrick.EnumType[values().length];
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

        public static WrapperBlockStoneBrick.EnumType byMetadata(int meta) {
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
            for (WrapperBlockStoneBrick.EnumType blockstonebrick$enumtype : values()) {
                META_LOOKUP[blockstonebrick$enumtype.getMetadata()] = blockstonebrick$enumtype;
            }
        }
    }
}
