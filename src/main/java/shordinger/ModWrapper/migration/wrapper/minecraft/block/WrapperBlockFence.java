package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemLead;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.AxisAlignedBB;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WrapperBlockFence extends WrapperBlock {

    /** Whether this fence connects in the northern direction */
    public static final PropertyBool NORTH = PropertyBool.create("north");
    /** Whether this fence connects in the eastern direction */
    public static final PropertyBool EAST = PropertyBool.create("east");
    /** Whether this fence connects in the southern direction */
    public static final PropertyBool SOUTH = PropertyBool.create("south");
    /** Whether this fence connects in the western direction */
    public static final PropertyBool WEST = PropertyBool.create("west");
    protected static final AxisAlignedBB[] BOUNDING_BOXES = new AxisAlignedBB[] {
        new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 1.0D, 0.625D),
        new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 1.0D, 1.0D),
        new AxisAlignedBB(0.0D, 0.0D, 0.375D, 0.625D, 1.0D, 0.625D),
        new AxisAlignedBB(0.0D, 0.0D, 0.375D, 0.625D, 1.0D, 1.0D),
        new AxisAlignedBB(0.375D, 0.0D, 0.0D, 0.625D, 1.0D, 0.625D),
        new AxisAlignedBB(0.375D, 0.0D, 0.0D, 0.625D, 1.0D, 1.0D),
        new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.625D, 1.0D, 0.625D),
        new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.625D, 1.0D, 1.0D),
        new AxisAlignedBB(0.375D, 0.0D, 0.375D, 1.0D, 1.0D, 0.625D),
        new AxisAlignedBB(0.375D, 0.0D, 0.375D, 1.0D, 1.0D, 1.0D),
        new AxisAlignedBB(0.0D, 0.0D, 0.375D, 1.0D, 1.0D, 0.625D),
        new AxisAlignedBB(0.0D, 0.0D, 0.375D, 1.0D, 1.0D, 1.0D),
        new AxisAlignedBB(0.375D, 0.0D, 0.0D, 1.0D, 1.0D, 0.625D),
        new AxisAlignedBB(0.375D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D),
        new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.625D),
        new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D) };
    public static final AxisAlignedBB PILLAR_AABB = new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 1.5D, 0.625D);
    public static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.375D, 0.0D, 0.625D, 0.625D, 1.5D, 1.0D);
    public static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.375D, 0.375D, 1.5D, 0.625D);
    public static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.375D, 0.0D, 0.0D, 0.625D, 1.5D, 0.375D);
    public static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.625D, 0.0D, 0.375D, 1.0D, 1.5D, 0.625D);

    public WrapperBlockFence(Material materialIn, MapColor mapColorIn) {
        super(materialIn, mapColorIn);
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(NORTH, Boolean.valueOf(false))
                .withProperty(EAST, Boolean.valueOf(false))
                .withProperty(SOUTH, Boolean.valueOf(false))
                .withProperty(WEST, Boolean.valueOf(false)));
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }

    public void addCollisionBoxToList(IWrapperBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox,
        List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        if (!isActualState) {
            state = state.getActualState(worldIn, pos);
        }

        addCollisionBoxToList(pos, entityBox, collidingBoxes, PILLAR_AABB);

        if (((Boolean) state.getValue(NORTH)).booleanValue()) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, NORTH_AABB);
        }

        if (((Boolean) state.getValue(EAST)).booleanValue()) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, EAST_AABB);
        }

        if (((Boolean) state.getValue(SOUTH)).booleanValue()) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, SOUTH_AABB);
        }

        if (((Boolean) state.getValue(WEST)).booleanValue()) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, WEST_AABB);
        }
    }

    public AxisAlignedBB getBoundingBox(IWrapperBlockState state, IBlockAccess source, BlockPos pos) {
        state = this.getActualState(state, source, pos);
        return BOUNDING_BOXES[getBoundingBoxIdx(state)];
    }

    /**
     * Returns the correct index into boundingBoxes, based on what the fence is connected to.
     */
    private static int getBoundingBoxIdx(IWrapperBlockState state) {
        int i = 0;

        if (((Boolean) state.getValue(NORTH)).booleanValue()) {
            i |= 1 << EnumFacing.NORTH.getHorizontalIndex();
        }

        if (((Boolean) state.getValue(EAST)).booleanValue()) {
            i |= 1 << EnumFacing.EAST.getHorizontalIndex();
        }

        if (((Boolean) state.getValue(SOUTH)).booleanValue()) {
            i |= 1 << EnumFacing.SOUTH.getHorizontalIndex();
        }

        if (((Boolean) state.getValue(WEST)).booleanValue()) {
            i |= 1 << EnumFacing.WEST.getHorizontalIndex();
        }

        return i;
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

    /**
     * Determines if an entity can path through this block
     */
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }

    public boolean canConnectTo(IBlockAccess worldIn, BlockPos pos, EnumFacing facing) {
        IWrapperBlockState iblockstate = worldIn.getBlockState(pos);
        BlockFaceShape blockfaceshape = iblockstate.getBlockFaceShape(worldIn, pos, facing);
        WrapperBlock wrapperBlock = iblockstate.getBlock();
        boolean flag = blockfaceshape == BlockFaceShape.MIDDLE_POLE
            && (iblockstate.getMaterial() == this.blockWrapperMaterial || wrapperBlock instanceof WrapperBlockFenceGate);
        return !isExcepBlockForAttachWithPiston(wrapperBlock) && blockfaceshape == BlockFaceShape.SOLID || flag;
    }

    protected static boolean isExcepBlockForAttachWithPiston(WrapperBlock p_194142_0_) {
        return WrapperBlock.isExceptBlockForAttachWithPiston(p_194142_0_) || p_194142_0_ == Blocks.BARRIER
            || p_194142_0_ == Blocks.MELON_BLOCK
            || p_194142_0_ == Blocks.PUMPKIN
            || p_194142_0_ == Blocks.LIT_PUMPKIN;
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IWrapperBlockState blockState, IBlockAccess blockAccess, BlockPos pos,
        EnumFacing side) {
        return true;
    }

    /**
     * Called when the block is right clicked by a player.
     */
    public boolean onBlockActivated(World worldIn, BlockPos pos, IWrapperBlockState state, EntityPlayer playerIn,
        EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            return ItemLead.attachToFence(playerIn, worldIn, pos);
        } else {
            ItemStack itemstack = playerIn.getHeldItem(hand);
            return itemstack.getItem() == Items.LEAD || itemstack.isEmpty();
        }
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        return 0;
    }

    /**
     * Get the actual Block state of this Block at the given position. This applies properties not visible in the
     * metadata, such as fence connections.
     */
    public IWrapperBlockState getActualState(IWrapperBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return state.withProperty(NORTH, canFenceConnectTo(worldIn, pos, EnumFacing.NORTH))
            .withProperty(EAST, canFenceConnectTo(worldIn, pos, EnumFacing.EAST))
            .withProperty(SOUTH, canFenceConnectTo(worldIn, pos, EnumFacing.SOUTH))
            .withProperty(WEST, canFenceConnectTo(worldIn, pos, EnumFacing.WEST));
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
        return canConnectTo(world, pos.offset(facing), facing.getOpposite());
    }

    private boolean canFenceConnectTo(IBlockAccess world, BlockPos pos, EnumFacing facing) {
        BlockPos other = pos.offset(facing);
        WrapperBlock wrapperBlock = world.getBlockState(other)
            .getBlock();
        return wrapperBlock.canBeConnectedTo(world, other, facing.getOpposite())
            || canConnectTo(world, other, facing.getOpposite());
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
        return face != EnumFacing.UP && face != EnumFacing.DOWN ? BlockFaceShape.MIDDLE_POLE : BlockFaceShape.CENTER;
    }
}
