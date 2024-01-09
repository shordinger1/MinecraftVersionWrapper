package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import java.util.Collection;

import javax.annotation.Nullable;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.world.IBlockAccess;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.AxisAlignedBB;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public abstract class WrapperBlockFlower extends WrapperBlockBush {

    protected PropertyEnum<WrapperBlockFlower.EnumFlowerType> type;

    protected WrapperBlockFlower() {
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(
                    this.getTypeProperty(),
                    this.getBlockType() == WrapperBlockFlower.EnumFlowerColor.RED
                        ? WrapperBlockFlower.EnumFlowerType.POPPY
                        : WrapperBlockFlower.EnumFlowerType.DANDELION));
    }

    public AxisAlignedBB getBoundingBox(IWrapperBlockState state, IBlockAccess source, BlockPos pos) {
        return super.getBoundingBox(state, source, pos).offset(state.getOffset(source, pos));
    }

    /**
     * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
     * returns the metadata of the dropped item based on the old metadata of the block.
     */
    public int damageDropped(IWrapperBlockState state) {
        return ((WrapperBlockFlower.EnumFlowerType) state.getValue(this.getTypeProperty())).getMeta();
    }

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (WrapperBlockFlower.EnumFlowerType blockflower$enumflowertype : WrapperBlockFlower.EnumFlowerType
            .getTypes(this.getBlockType())) {
            items.add(new ItemStack(this, 1, blockflower$enumflowertype.getMeta()));
        }
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IWrapperBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
            .withProperty(this.getTypeProperty(), WrapperBlockFlower.EnumFlowerType.getType(this.getBlockType(), meta));
    }

    /**
     * Get the Type of this flower (Yellow/Red)
     */
    public abstract WrapperBlockFlower.EnumFlowerColor getBlockType();

    public IProperty<WrapperBlockFlower.EnumFlowerType> getTypeProperty() {
        if (this.type == null) {
            this.type = PropertyEnum.<WrapperBlockFlower.EnumFlowerType>create(
                "type",
                WrapperBlockFlower.EnumFlowerType.class,
                new Predicate<WrapperBlockFlower.EnumFlowerType>() {

                    public boolean apply(@Nullable WrapperBlockFlower.EnumFlowerType p_apply_1_) {
                        return p_apply_1_.getBlockType() == WrapperBlockFlower.this.getBlockType();
                    }
                });
        }

        return this.type;
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        return ((WrapperBlockFlower.EnumFlowerType) state.getValue(this.getTypeProperty())).getMeta();
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { this.getTypeProperty() });
    }

    /**
     * Get the OffsetType for this Block. Determines if the model is rendered slightly offset.
     */
    public WrapperBlock.EnumOffsetType getOffsetType() {
        return WrapperBlock.EnumOffsetType.XZ;
    }

    public static enum EnumFlowerColor {

        YELLOW,
        RED;

        public WrapperBlockFlower getBlock() {
            return this == YELLOW ? Blocks.YELLOW_FLOWER : Blocks.RED_FLOWER;
        }
    }

    public static enum EnumFlowerType implements IStringSerializable {

        DANDELION(WrapperBlockFlower.EnumFlowerColor.YELLOW, 0, "dandelion"),
        POPPY(WrapperBlockFlower.EnumFlowerColor.RED, 0, "poppy"),
        BLUE_ORCHID(WrapperBlockFlower.EnumFlowerColor.RED, 1, "blue_orchid", "blueOrchid"),
        ALLIUM(WrapperBlockFlower.EnumFlowerColor.RED, 2, "allium"),
        HOUSTONIA(WrapperBlockFlower.EnumFlowerColor.RED, 3, "houstonia"),
        RED_TULIP(WrapperBlockFlower.EnumFlowerColor.RED, 4, "red_tulip", "tulipRed"),
        ORANGE_TULIP(WrapperBlockFlower.EnumFlowerColor.RED, 5, "orange_tulip", "tulipOrange"),
        WHITE_TULIP(WrapperBlockFlower.EnumFlowerColor.RED, 6, "white_tulip", "tulipWhite"),
        PINK_TULIP(WrapperBlockFlower.EnumFlowerColor.RED, 7, "pink_tulip", "tulipPink"),
        OXEYE_DAISY(WrapperBlockFlower.EnumFlowerColor.RED, 8, "oxeye_daisy", "oxeyeDaisy");

        private static final WrapperBlockFlower.EnumFlowerType[][] TYPES_FOR_BLOCK = new WrapperBlockFlower.EnumFlowerType[WrapperBlockFlower.EnumFlowerColor
            .values().length][];
        private final WrapperBlockFlower.EnumFlowerColor blockType;
        private final int meta;
        private final String name;
        private final String unlocalizedName;

        private EnumFlowerType(WrapperBlockFlower.EnumFlowerColor blockType, int meta, String name) {
            this(blockType, meta, name, name);
        }

        private EnumFlowerType(WrapperBlockFlower.EnumFlowerColor blockType, int meta, String name,
            String unlocalizedName) {
            this.blockType = blockType;
            this.meta = meta;
            this.name = name;
            this.unlocalizedName = unlocalizedName;
        }

        public WrapperBlockFlower.EnumFlowerColor getBlockType() {
            return this.blockType;
        }

        public int getMeta() {
            return this.meta;
        }

        /**
         * Get the given FlowerType from BlockType & metadata
         */
        public static WrapperBlockFlower.EnumFlowerType getType(WrapperBlockFlower.EnumFlowerColor blockType,
            int meta) {
            WrapperBlockFlower.EnumFlowerType[] ablockflower$enumflowertype = TYPES_FOR_BLOCK[blockType.ordinal()];

            if (meta < 0 || meta >= ablockflower$enumflowertype.length) {
                meta = 0;
            }

            return ablockflower$enumflowertype[meta];
        }

        /**
         * Get all FlowerTypes that are applicable for the given Flower block ("yellow", "red")
         */
        public static WrapperBlockFlower.EnumFlowerType[] getTypes(WrapperBlockFlower.EnumFlowerColor flowerColor) {
            return TYPES_FOR_BLOCK[flowerColor.ordinal()];
        }

        public String toString() {
            return this.name;
        }

        public String getName() {
            return this.name;
        }

        public String getUnlocalizedName() {
            return this.unlocalizedName;
        }

        static {
            for (final WrapperBlockFlower.EnumFlowerColor blockflower$enumflowercolor : WrapperBlockFlower.EnumFlowerColor
                .values()) {
                Collection<WrapperBlockFlower.EnumFlowerType> collection = Collections2.<WrapperBlockFlower.EnumFlowerType>filter(
                    Lists.newArrayList(values()),
                    new Predicate<WrapperBlockFlower.EnumFlowerType>() {

                        public boolean apply(@Nullable WrapperBlockFlower.EnumFlowerType p_apply_1_) {
                            return p_apply_1_.getBlockType() == blockflower$enumflowercolor;
                        }
                    });
                TYPES_FOR_BLOCK[blockflower$enumflowercolor
                    .ordinal()] = (WrapperBlockFlower.EnumFlowerType[]) collection
                        .toArray(new WrapperBlockFlower.EnumFlowerType[collection.size()]);
            }
        }
    }
}
