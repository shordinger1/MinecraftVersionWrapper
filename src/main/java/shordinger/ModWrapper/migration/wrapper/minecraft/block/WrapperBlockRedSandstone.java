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

public class WrapperBlockRedSandstone extends WrapperBlock {

    public static final PropertyEnum<WrapperBlockRedSandstone.EnumType> TYPE = PropertyEnum.<WrapperBlockRedSandstone.EnumType>create(
        "type",
        WrapperBlockRedSandstone.EnumType.class);

    public WrapperBlockRedSandstone() {
        super(Material.ROCK, WrapperBlockSand.EnumType.RED_SAND.getMapColor());
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(TYPE, WrapperBlockRedSandstone.EnumType.DEFAULT));
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    /**
     * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
     * returns the metadata of the dropped item based on the old metadata of the block.
     */
    public int damageDropped(IWrapperBlockState state) {
        return ((WrapperBlockRedSandstone.EnumType) state.getValue(TYPE)).getMetadata();
    }

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (WrapperBlockRedSandstone.EnumType blockredsandstone$enumtype : WrapperBlockRedSandstone.EnumType
            .values()) {
            items.add(new ItemStack(this, 1, blockredsandstone$enumtype.getMetadata()));
        }
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IWrapperBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
            .withProperty(TYPE, WrapperBlockRedSandstone.EnumType.byMetadata(meta));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        return ((WrapperBlockRedSandstone.EnumType) state.getValue(TYPE)).getMetadata();
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { TYPE });
    }

    public static enum EnumType implements IStringSerializable {

        DEFAULT(0, "red_sandstone", "default"),
        CHISELED(1, "chiseled_red_sandstone", "chiseled"),
        SMOOTH(2, "smooth_red_sandstone", "smooth");

        private static final WrapperBlockRedSandstone.EnumType[] META_LOOKUP = new WrapperBlockRedSandstone.EnumType[values().length];
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

        public static WrapperBlockRedSandstone.EnumType byMetadata(int meta) {
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
            for (WrapperBlockRedSandstone.EnumType blockredsandstone$enumtype : values()) {
                META_LOOKUP[blockredsandstone$enumtype.getMetadata()] = blockredsandstone$enumtype;
            }
        }
    }
}
