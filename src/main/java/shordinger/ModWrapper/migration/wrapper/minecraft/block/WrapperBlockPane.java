package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.AxisAlignedBB;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WrapperBlockPane extends WrapperBlock {

    public static final PropertyBool NORTH = PropertyBool.create("north");
    public static final PropertyBool EAST = PropertyBool.create("east");
    public static final PropertyBool SOUTH = PropertyBool.create("south");
    public static final PropertyBool WEST = PropertyBool.create("west");
    protected static final AxisAlignedBB[] AABB_BY_INDEX = new AxisAlignedBB[] {
        new AxisAlignedBB(0.4375D, 0.0D, 0.4375D, 0.5625D, 1.0D, 0.5625D),
        new AxisAlignedBB(0.4375D, 0.0D, 0.4375D, 0.5625D, 1.0D, 1.0D),
        new AxisAlignedBB(0.0D, 0.0D, 0.4375D, 0.5625D, 1.0D, 0.5625D),
        new AxisAlignedBB(0.0D, 0.0D, 0.4375D, 0.5625D, 1.0D, 1.0D),
        new AxisAlignedBB(0.4375D, 0.0D, 0.0D, 0.5625D, 1.0D, 0.5625D),
        new AxisAlignedBB(0.4375D, 0.0D, 0.0D, 0.5625D, 1.0D, 1.0D),
        new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.5625D, 1.0D, 0.5625D),
        new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.5625D, 1.0D, 1.0D),
        new AxisAlignedBB(0.4375D, 0.0D, 0.4375D, 1.0D, 1.0D, 0.5625D),
        new AxisAlignedBB(0.4375D, 0.0D, 0.4375D, 1.0D, 1.0D, 1.0D),
        new AxisAlignedBB(0.0D, 0.0D, 0.4375D, 1.0D, 1.0D, 0.5625D),
        new AxisAlignedBB(0.0D, 0.0D, 0.4375D, 1.0D, 1.0D, 1.0D),
        new AxisAlignedBB(0.4375D, 0.0D, 0.0D, 1.0D, 1.0D, 0.5625D),
        new AxisAlignedBB(0.4375D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D),
        new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.5625D),
        new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D) };
    private final boolean canDrop;

    protected WrapperBlockPane(Material materialIn, boolean canDrop) {
        super(materialIn);
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(NORTH, Boolean.valueOf(false))
                .withProperty(EAST, Boolean.valueOf(false))
                .withProperty(SOUTH, Boolean.valueOf(false))
                .withProperty(WEST, Boolean.valueOf(false)));
        this.canDrop = canDrop;
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }

    public void addCollisionBoxToList(IWrapperBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox,
        List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        if (!isActualState) {
            state = this.getActualState(state, worldIn, pos);
        }

        addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_BY_INDEX[0]);

        if (((Boolean) state.getValue(NORTH)).booleanValue()) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_BY_INDEX[getBoundingBoxIndex(EnumFacing.NORTH)]);
        }

        if (((Boolean) state.getValue(SOUTH)).booleanValue()) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_BY_INDEX[getBoundingBoxIndex(EnumFacing.SOUTH)]);
        }

        if (((Boolean) state.getValue(EAST)).booleanValue()) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_BY_INDEX[getBoundingBoxIndex(EnumFacing.EAST)]);
        }

        if (((Boolean) state.getValue(WEST)).booleanValue()) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_BY_INDEX[getBoundingBoxIndex(EnumFacing.WEST)]);
        }
    }

    private static int getBoundingBoxIndex(EnumFacing p_185729_0_) {
        return 1 << p_185729_0_.getHorizontalIndex();
    }

    public AxisAlignedBB getBoundingBox(IWrapperBlockState state, IBlockAccess source, BlockPos pos) {
        state = this.getActualState(state, source, pos);
        return AABB_BY_INDEX[getBoundingBoxIndex(state)];
    }

    private static int getBoundingBoxIndex(IWrapperBlockState state) {
        int i = 0;

        if (((Boolean) state.getValue(NORTH)).booleanValue()) {
            i |= getBoundingBoxIndex(EnumFacing.NORTH);
        }

        if (((Boolean) state.getValue(EAST)).booleanValue()) {
            i |= getBoundingBoxIndex(EnumFacing.EAST);
        }

        if (((Boolean) state.getValue(SOUTH)).booleanValue()) {
            i |= getBoundingBoxIndex(EnumFacing.SOUTH);
        }

        if (((Boolean) state.getValue(WEST)).booleanValue()) {
            i |= getBoundingBoxIndex(EnumFacing.WEST);
        }

        return i;
    }

    /**
     * Get the actual Block state of this Block at the given position. This applies properties not visible in the
     * metadata, such as fence connections.
     */
    public IWrapperBlockState getActualState(IWrapperBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return state.withProperty(NORTH, canPaneConnectTo(worldIn, pos, EnumFacing.NORTH))
            .withProperty(SOUTH, canPaneConnectTo(worldIn, pos, EnumFacing.SOUTH))
            .withProperty(WEST, canPaneConnectTo(worldIn, pos, EnumFacing.WEST))
            .withProperty(EAST, canPaneConnectTo(worldIn, pos, EnumFacing.EAST));
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    public Item getItemDropped(IWrapperBlockState state, Random rand, int fortune) {
        return !this.canDrop ? Items.AIR : super.getItemDropped(state, rand, fortune);
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
    public boolean isOpaqueCube(IWrapperBlockState state) {
        return false;
    }

    public boolean isFullCube(IWrapperBlockState state) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IWrapperBlockState blockState, IBlockAccess blockAccess, BlockPos pos,
        EnumFacing side) {
        return blockAccess.getBlockState(pos.offset(side))
            .getBlock() == this ? false : super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }

    public final boolean attachesTo(IBlockAccess p_193393_1_, IWrapperBlockState state, BlockPos pos,
        EnumFacing facing) {
        WrapperBlock wrapperBlock = state.getBlock();
        BlockFaceShape blockfaceshape = state.getBlockFaceShape(p_193393_1_, pos, facing);
        return !isExcepBlockForAttachWithPiston(wrapperBlock) && blockfaceshape == BlockFaceShape.SOLID
            || blockfaceshape == BlockFaceShape.MIDDLE_POLE_THIN;
    }

    protected static boolean isExcepBlockForAttachWithPiston(WrapperBlock p_193394_0_) {
        return p_193394_0_ instanceof WrapperBlockShulkerBox || p_193394_0_ instanceof WrapperBlockLeaves
            || p_193394_0_ == Blocks.BEACON
            || p_193394_0_ == Blocks.CAULDRON
            || p_193394_0_ == Blocks.GLOWSTONE
            || p_193394_0_ == Blocks.ICE
            || p_193394_0_ == Blocks.SEA_LANTERN
            || p_193394_0_ == Blocks.PISTON
            || p_193394_0_ == Blocks.STICKY_PISTON
            || p_193394_0_ == Blocks.PISTON_HEAD
            || p_193394_0_ == Blocks.MELON_BLOCK
            || p_193394_0_ == Blocks.PUMPKIN
            || p_193394_0_ == Blocks.LIT_PUMPKIN
            || p_193394_0_ == Blocks.BARRIER;
    }

    protected boolean canSilkHarvest() {
        return true;
    }

    /**
     * Gets the render layer this block will render on. SOLID for solid blocks, CUTOUT or CUTOUT_MIPPED for on-off
     * transparency (glass, reeds), TRANSLUCENT for fully blended transparency (stained glass)
     */
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        return 0;
    }

    /**
     * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     */
    public IWrapperBlockState withRotation(IWrapperBlockState state, Rotation rot) {
        switch (rot) {
            case CLOCKWISE_180:
                return state.withProperty(NORTH, state.getValue(SOUTH))
                    .withProperty(EAST, state.getValue(WEST))
                    .withProperty(SOUTH, state.getValue(NORTH))
                    .withProperty(WEST, state.getValue(EAST));
            case COUNTERCLOCKWISE_90:
                return state.withProperty(NORTH, state.getValue(EAST))
                    .withProperty(EAST, state.getValue(SOUTH))
                    .withProperty(SOUTH, state.getValue(WEST))
                    .withProperty(WEST, state.getValue(NORTH));
            case CLOCKWISE_90:
                return state.withProperty(NORTH, state.getValue(WEST))
                    .withProperty(EAST, state.getValue(NORTH))
                    .withProperty(SOUTH, state.getValue(EAST))
                    .withProperty(WEST, state.getValue(SOUTH));
            default:
                return state;
        }
    }

    /**
     * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     */
    public IWrapperBlockState withMirror(IWrapperBlockState state, Mirror mirrorIn) {
        switch (mirrorIn) {
            case LEFT_RIGHT:
                return state.withProperty(NORTH, state.getValue(SOUTH))
                    .withProperty(SOUTH, state.getValue(NORTH));
            case FRONT_BACK:
                return state.withProperty(EAST, state.getValue(WEST))
                    .withProperty(WEST, state.getValue(EAST));
            default:
                return super.withMirror(state, mirrorIn);
        }
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { NORTH, EAST, WEST, SOUTH });
    }

    /* ======================================== FORGE START ======================================== */

    @Override
    public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, EnumFacing facing) {
        BlockPos offset = pos.offset(facing);
        return attachesTo(world, world.getBlockState(offset), offset, facing.getOpposite());
    }

    public boolean canPaneConnectTo(IBlockAccess world, BlockPos pos, EnumFacing dir) {
        BlockPos other = pos.offset(dir);
        IWrapperBlockState state = world.getBlockState(other);
        return state.getBlock()
            .canBeConnectedTo(world, other, dir.getOpposite()) || attachesTo(world, state, other, dir.getOpposite());
    }

    /* ======================================== FORGE END ======================================== */

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
        return face != EnumFacing.UP && face != EnumFacing.DOWN ? BlockFaceShape.MIDDLE_POLE_THIN
            : BlockFaceShape.CENTER_SMALL;
    }
}