package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WrapperBlockQuartz extends WrapperBlock {

    public static final PropertyEnum<WrapperBlockQuartz.EnumType> VARIANT = PropertyEnum.<WrapperBlockQuartz.EnumType>create(
        "variant",
        WrapperBlockQuartz.EnumType.class);

    public WrapperBlockQuartz() {
        super(Material.ROCK);
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(VARIANT, WrapperBlockQuartz.EnumType.DEFAULT));
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    /**
     * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
     * IBlockstate
     */
    public IWrapperBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX,
        float hitY, float hitZ, int meta, EntityLivingBase placer) {
        if (meta == WrapperBlockQuartz.EnumType.LINES_Y.getMetadata()) {
            switch (facing.getAxis()) {
                case Z:
                    return this.getDefaultState()
                        .withProperty(VARIANT, WrapperBlockQuartz.EnumType.LINES_Z);
                case X:
                    return this.getDefaultState()
                        .withProperty(VARIANT, WrapperBlockQuartz.EnumType.LINES_X);
                case Y:
                    return this.getDefaultState()
                        .withProperty(VARIANT, WrapperBlockQuartz.EnumType.LINES_Y);
            }
        }

        return meta == WrapperBlockQuartz.EnumType.CHISELED.getMetadata() ? this.getDefaultState()
            .withProperty(VARIANT, WrapperBlockQuartz.EnumType.CHISELED)
            : this.getDefaultState()
                .withProperty(VARIANT, WrapperBlockQuartz.EnumType.DEFAULT);
    }

    /**
     * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
     * returns the metadata of the dropped item based on the old metadata of the block.
     */
    public int damageDropped(IWrapperBlockState state) {
        WrapperBlockQuartz.EnumType blockquartz$enumtype = (WrapperBlockQuartz.EnumType) state.getValue(VARIANT);
        return blockquartz$enumtype != WrapperBlockQuartz.EnumType.LINES_X
            && blockquartz$enumtype != WrapperBlockQuartz.EnumType.LINES_Z ? blockquartz$enumtype.getMetadata()
                : WrapperBlockQuartz.EnumType.LINES_Y.getMetadata();
    }

    protected ItemStack getSilkTouchDrop(IWrapperBlockState state) {
        WrapperBlockQuartz.EnumType blockquartz$enumtype = (WrapperBlockQuartz.EnumType) state.getValue(VARIANT);
        return blockquartz$enumtype != WrapperBlockQuartz.EnumType.LINES_X
            && blockquartz$enumtype != WrapperBlockQuartz.EnumType.LINES_Z ? super.getSilkTouchDrop(state)
                : new ItemStack(Item.getItemFromBlock(this), 1, WrapperBlockQuartz.EnumType.LINES_Y.getMetadata());
    }

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        items.add(new ItemStack(this, 1, WrapperBlockQuartz.EnumType.DEFAULT.getMetadata()));
        items.add(new ItemStack(this, 1, WrapperBlockQuartz.EnumType.CHISELED.getMetadata()));
        items.add(new ItemStack(this, 1, WrapperBlockQuartz.EnumType.LINES_Y.getMetadata()));
    }

    /**
     * Get the MapColor for this Block and the given BlockState
     */
    public MapColor getMapColor(IWrapperBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return MapColor.QUARTZ;
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IWrapperBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
            .withProperty(VARIANT, WrapperBlockQuartz.EnumType.byMetadata(meta));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        return ((WrapperBlockQuartz.EnumType) state.getValue(VARIANT)).getMetadata();
    }

    /**
     * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     */
    public IWrapperBlockState withRotation(IWrapperBlockState state, Rotation rot) {
        switch (rot) {
            case COUNTERCLOCKWISE_90:
            case CLOCKWISE_90:

                switch ((WrapperBlockQuartz.EnumType) state.getValue(VARIANT)) {
                    case LINES_X:
                        return state.withProperty(VARIANT, WrapperBlockQuartz.EnumType.LINES_Z);
                    case LINES_Z:
                        return state.withProperty(VARIANT, WrapperBlockQuartz.EnumType.LINES_X);
                    default:
                        return state;
                }

            default:
                return state;
        }
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { VARIANT });
    }

    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
        IWrapperBlockState state = world.getBlockState(pos);
        for (IProperty prop : state.getProperties()
            .keySet()) {
            if (prop.getName()
                .equals("variant") && prop.getValueClass() == EnumType.class) {
                EnumType current = (EnumType) state.getValue(prop);
                EnumType next = current == EnumType.LINES_X ? EnumType.LINES_Y
                    : current == EnumType.LINES_Y ? EnumType.LINES_Z
                        : current == EnumType.LINES_Z ? EnumType.LINES_X : current;
                if (next == current) return false;
                world.setBlockState(pos, state.withProperty(prop, next));
                return true;
            }
        }
        return false;
    }

    public static enum EnumType implements IStringSerializable {

        DEFAULT(0, "default", "default"),
        CHISELED(1, "chiseled", "chiseled"),
        LINES_Y(2, "lines_y", "lines"),
        LINES_X(3, "lines_x", "lines"),
        LINES_Z(4, "lines_z", "lines");

        private static final WrapperBlockQuartz.EnumType[] META_LOOKUP = new WrapperBlockQuartz.EnumType[values().length];
        private final int meta;
        private final String serializedName;
        private final String unlocalizedName;

        private EnumType(int meta, String name, String unlocalizedName) {
            this.meta = meta;
            this.serializedName = name;
            this.unlocalizedName = unlocalizedName;
        }

        public int getMetadata() {
            return this.meta;
        }

        public String toString() {
            return this.unlocalizedName;
        }

        public static WrapperBlockQuartz.EnumType byMetadata(int meta) {
            if (meta < 0 || meta >= META_LOOKUP.length) {
                meta = 0;
            }

            return META_LOOKUP[meta];
        }

        public String getName() {
            return this.serializedName;
        }

        static {
            for (WrapperBlockQuartz.EnumType blockquartz$enumtype : values()) {
                META_LOOKUP[blockquartz$enumtype.getMetadata()] = blockquartz$enumtype;
            }
        }
    }
}
