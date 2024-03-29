package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.block.state.pattern.FactoryBlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.google.common.base.Predicates;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.AxisAlignedBB;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class BlockEndPortalFrame extends Block {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final PropertyBool EYE = PropertyBool.create("eye");
    protected static final AxisAlignedBB AABB_BLOCK = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.8125D, 1.0D);
    protected static final AxisAlignedBB AABB_EYE = new AxisAlignedBB(
        0.3125D,
        0.8125D,
        0.3125D,
        0.6875D,
        1.0D,
        0.6875D);
    private static BlockPattern portalShape;

    public BlockEndPortalFrame() {
        super(Material.ROCK, MapColor.GREEN);
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(FACING, EnumFacing.NORTH)
                .withProperty(EYE, Boolean.valueOf(false)));
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
    public boolean isOpaqueCube(IWrapperBlockState state) {
        return false;
    }

    public AxisAlignedBB getBoundingBox(IWrapperBlockState state, IBlockAccess source, BlockPos pos) {
        return AABB_BLOCK;
    }

    public void addCollisionBoxToList(IWrapperBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox,
                                      List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_BLOCK);

        if (((Boolean) worldIn.getBlockState(pos)
            .getValue(EYE)).booleanValue()) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_EYE);
        }
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    public Item getItemDropped(IWrapperBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    /**
     * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
     * IBlockstate
     */
    public IWrapperBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX,
        float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState()
            .withProperty(
                FACING,
                placer.getHorizontalFacing()
                    .getOpposite())
            .withProperty(EYE, Boolean.valueOf(false));
    }

    public boolean hasComparatorInputOverride(IWrapperBlockState state) {
        return true;
    }

    public int getComparatorInputOverride(IWrapperBlockState blockState, World worldIn, BlockPos pos) {
        return ((Boolean) blockState.getValue(EYE)).booleanValue() ? 15 : 0;
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IWrapperBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
            .withProperty(EYE, Boolean.valueOf((meta & 4) != 0))
            .withProperty(FACING, EnumFacing.getHorizontal(meta & 3));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        int i = 0;
        i = i | ((EnumFacing) state.getValue(FACING)).getHorizontalIndex();

        if (((Boolean) state.getValue(EYE)).booleanValue()) {
            i |= 4;
        }

        return i;
    }

    /**
     * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     */
    public IWrapperBlockState withRotation(IWrapperBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate((EnumFacing) state.getValue(FACING)));
    }

    /**
     * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     */
    public IWrapperBlockState withMirror(IWrapperBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation((EnumFacing) state.getValue(FACING)));
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { FACING, EYE });
    }

    public boolean isFullCube(IWrapperBlockState state) {
        return false;
    }

    public static BlockPattern getOrCreatePortalShape() {
        if (portalShape == null) {
            portalShape = FactoryBlockPattern.start()
                .aisle("?vvv?", ">???<", ">???<", ">???<", "?^^^?")
                .where('?', BlockWorldState.hasState(BlockStateMatcher.ANY))
                .where(
                    '^',
                    BlockWorldState.hasState(
                        BlockStateMatcher.forBlock(Blocks.END_PORTAL_FRAME)
                            .where(EYE, Predicates.equalTo(Boolean.valueOf(true)))
                            .where(FACING, Predicates.equalTo(EnumFacing.SOUTH))))
                .where(
                    '>',
                    BlockWorldState.hasState(
                        BlockStateMatcher.forBlock(Blocks.END_PORTAL_FRAME)
                            .where(EYE, Predicates.equalTo(Boolean.valueOf(true)))
                            .where(FACING, Predicates.equalTo(EnumFacing.WEST))))
                .where(
                    'v',
                    BlockWorldState.hasState(
                        BlockStateMatcher.forBlock(Blocks.END_PORTAL_FRAME)
                            .where(EYE, Predicates.equalTo(Boolean.valueOf(true)))
                            .where(FACING, Predicates.equalTo(EnumFacing.NORTH))))
                .where(
                    '<',
                    BlockWorldState.hasState(
                        BlockStateMatcher.forBlock(Blocks.END_PORTAL_FRAME)
                            .where(EYE, Predicates.equalTo(Boolean.valueOf(true)))
                            .where(FACING, Predicates.equalTo(EnumFacing.EAST))))
                .build();
        }

        return portalShape;
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
        return face == EnumFacing.DOWN ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }
}
