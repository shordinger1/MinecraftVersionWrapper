package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import java.util.Random;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.AxisAlignedBB;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public abstract class WrapperBlockSlab extends WrapperBlock {

    public static final PropertyEnum<WrapperBlockSlab.EnumBlockHalf> HALF = PropertyEnum.<WrapperBlockSlab.EnumBlockHalf>create(
        "half",
        WrapperBlockSlab.EnumBlockHalf.class);
    protected static final AxisAlignedBB AABB_BOTTOM_HALF = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);
    protected static final AxisAlignedBB AABB_TOP_HALF = new AxisAlignedBB(0.0D, 0.5D, 0.0D, 1.0D, 1.0D, 1.0D);

    public WrapperBlockSlab(Material materialIn) {
        this(materialIn, materialIn.getMaterialMapColor());
    }

    public WrapperBlockSlab(Material p_i47249_1_, MapColor p_i47249_2_) {
        super(p_i47249_1_, p_i47249_2_);
        this.fullBlock = this.isDouble();
        this.setLightOpacity(255);
    }

    protected boolean canSilkHarvest() {
        return false;
    }

    public AxisAlignedBB getBoundingBox(IWrapperBlockState state, IBlockAccess source, BlockPos pos) {
        if (this.isDouble()) {
            return FULL_BLOCK_AABB;
        } else {
            return state.getValue(HALF) == WrapperBlockSlab.EnumBlockHalf.TOP ? AABB_TOP_HALF : AABB_BOTTOM_HALF;
        }
    }

    /**
     * Determines if the block is solid enough on the top side to support other blocks, like redstone components.
     */
    public boolean isTopSolid(IWrapperBlockState state) {
        return ((WrapperBlockSlab) state.getBlock()).isDouble()
            || state.getValue(HALF) == WrapperBlockSlab.EnumBlockHalf.TOP;
    }

    /**
     * Get the geometry of the queried face at the given position and state. This is used to decide whether things like
     * buttons are allowed to be placed on the face, or how glass panes connect to the face, among other things.
     * <p>
     * Common values are {@code SOLID}, which is the default, and {@code UNDEFINED}, which represents something that
     * does not fit the other descriptions and will generally cause other things not to connect to the face.
     *
     * @return an approximation of the form of the given face
     */
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IWrapperBlockState state, BlockPos pos,
        EnumFacing face) {
        if (((WrapperBlockSlab) state.getBlock()).isDouble()) {
            return BlockFaceShape.SOLID;
        } else if (face == EnumFacing.UP && state.getValue(HALF) == WrapperBlockSlab.EnumBlockHalf.TOP) {
            return BlockFaceShape.SOLID;
        } else {
            return face == EnumFacing.DOWN && state.getValue(HALF) == WrapperBlockSlab.EnumBlockHalf.BOTTOM
                ? BlockFaceShape.SOLID
                : BlockFaceShape.UNDEFINED;
        }
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
    public boolean isOpaqueCube(IWrapperBlockState state) {
        return this.isDouble();
    }

    @Override
    public boolean doesSideBlockRendering(IWrapperBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        if (net.minecraftforge.common.ForgeModContainer.disableStairSlabCulling)
            return super.doesSideBlockRendering(state, world, pos, face);

        if (state.isOpaqueCube()) return true;

        EnumBlockHalf side = state.getValue(HALF);
        return (side == EnumBlockHalf.TOP && face == EnumFacing.UP)
            || (side == EnumBlockHalf.BOTTOM && face == EnumFacing.DOWN);
    }

    /**
     * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
     * IBlockstate
     */
    public IWrapperBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX,
        float hitY, float hitZ, int meta, EntityLivingBase placer) {
        IWrapperBlockState iblockstate = super.getStateForPlacement(
            worldIn,
            pos,
            facing,
            hitX,
            hitY,
            hitZ,
            meta,
            placer).withProperty(HALF, WrapperBlockSlab.EnumBlockHalf.BOTTOM);

        if (this.isDouble()) {
            return iblockstate;
        } else {
            return facing != EnumFacing.DOWN && (facing == EnumFacing.UP || (double) hitY <= 0.5D) ? iblockstate
                : iblockstate.withProperty(HALF, WrapperBlockSlab.EnumBlockHalf.TOP);
        }
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    public int quantityDropped(Random random) {
        return this.isDouble() ? 2 : 1;
    }

    public boolean isFullCube(IWrapperBlockState state) {
        return this.isDouble();
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IWrapperBlockState blockState, IBlockAccess blockAccess, BlockPos pos,
        EnumFacing side) {
        if (this.isDouble()) {
            return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
        } else if (side != EnumFacing.UP && side != EnumFacing.DOWN
            && !super.shouldSideBeRendered(blockState, blockAccess, pos, side)) {
                return false;
            } else if (false) // Forge: Additional logic breaks doesSideBlockRendering and is no longer useful.
        {
            IWrapperBlockState iblockstate = blockAccess.getBlockState(pos.offset(side));
            boolean flag = isHalfSlab(iblockstate) && iblockstate.getValue(HALF) == WrapperBlockSlab.EnumBlockHalf.TOP;
            boolean flag1 = isHalfSlab(blockState) && blockState.getValue(HALF) == WrapperBlockSlab.EnumBlockHalf.TOP;

            if (flag1) {
                if (side == EnumFacing.DOWN) {
                    return true;
                } else if (side == EnumFacing.UP && super.shouldSideBeRendered(blockState, blockAccess, pos, side)) {
                    return true;
                } else {
                    return !isHalfSlab(iblockstate) || !flag;
                }
            } else if (side == EnumFacing.UP) {
                return true;
            } else if (side == EnumFacing.DOWN && super.shouldSideBeRendered(blockState, blockAccess, pos, side)) {
                return true;
            } else {
                return !isHalfSlab(iblockstate) || flag;
            }
        }
        return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }

    @SideOnly(Side.CLIENT)
    protected static boolean isHalfSlab(IWrapperBlockState state) {
        WrapperBlock wrapperBlock = state.getBlock();
        return wrapperBlock == Blocks.STONE_SLAB || wrapperBlock == Blocks.WOODEN_SLAB
            || wrapperBlock == Blocks.STONE_SLAB2
            || wrapperBlock == Blocks.PURPUR_SLAB;
    }

    /**
     * Returns the slab block name with the type associated with it
     */
    public abstract String getUnlocalizedName(int meta);

    public abstract boolean isDouble();

    public abstract IProperty<?> getVariantProperty();

    public abstract Comparable<?> getTypeForItem(ItemStack stack);

    public static enum EnumBlockHalf implements IStringSerializable {

        TOP("top"),
        BOTTOM("bottom");

        private final String name;

        private EnumBlockHalf(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }

        public String getName() {
            return this.name;
        }
    }
}