package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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

public class WrapperBlockFenceGate extends WrapperBlockHorizontal {

    public static final PropertyBool OPEN = PropertyBool.create("open");
    public static final PropertyBool POWERED = PropertyBool.create("powered");
    public static final PropertyBool IN_WALL = PropertyBool.create("in_wall");
    protected static final AxisAlignedBB AABB_HITBOX_ZAXIS = new AxisAlignedBB(0.0D, 0.0D, 0.375D, 1.0D, 1.0D, 0.625D);
    protected static final AxisAlignedBB AABB_HITBOX_XAXIS = new AxisAlignedBB(0.375D, 0.0D, 0.0D, 0.625D, 1.0D, 1.0D);
    protected static final AxisAlignedBB AABB_HITBOX_ZAXIS_INWALL = new AxisAlignedBB(
        0.0D,
        0.0D,
        0.375D,
        1.0D,
        0.8125D,
        0.625D);
    protected static final AxisAlignedBB AABB_HITBOX_XAXIS_INWALL = new AxisAlignedBB(
        0.375D,
        0.0D,
        0.0D,
        0.625D,
        0.8125D,
        1.0D);
    protected static final AxisAlignedBB AABB_COLLISION_BOX_ZAXIS = new AxisAlignedBB(
        0.0D,
        0.0D,
        0.375D,
        1.0D,
        1.5D,
        0.625D);
    protected static final AxisAlignedBB AABB_COLLISION_BOX_XAXIS = new AxisAlignedBB(
        0.375D,
        0.0D,
        0.0D,
        0.625D,
        1.5D,
        1.0D);

    public WrapperBlockFenceGate(WrapperBlockPlanks.EnumType p_i46394_1_) {
        super(Material.WOOD, p_i46394_1_.getMapColor());
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(OPEN, Boolean.valueOf(false))
                .withProperty(POWERED, Boolean.valueOf(false))
                .withProperty(IN_WALL, Boolean.valueOf(false)));
        this.setCreativeTab(CreativeTabs.REDSTONE);
    }

    public AxisAlignedBB getBoundingBox(IWrapperBlockState state, IBlockAccess source, BlockPos pos) {
        state = this.getActualState(state, source, pos);

        if (((Boolean) state.getValue(IN_WALL)).booleanValue()) {
            return ((EnumFacing) state.getValue(FACING)).getAxis() == EnumFacing.Axis.X ? AABB_HITBOX_XAXIS_INWALL
                : AABB_HITBOX_ZAXIS_INWALL;
        } else {
            return ((EnumFacing) state.getValue(FACING)).getAxis() == EnumFacing.Axis.X ? AABB_HITBOX_XAXIS
                : AABB_HITBOX_ZAXIS;
        }
    }

    /**
     * Get the actual Block state of this Block at the given position. This applies properties not visible in the
     * metadata, such as fence connections.
     */
    public IWrapperBlockState getActualState(IWrapperBlockState state, IBlockAccess worldIn, BlockPos pos) {
        EnumFacing.Axis enumfacing$axis = ((EnumFacing) state.getValue(FACING)).getAxis();

        if (enumfacing$axis == EnumFacing.Axis.Z && (worldIn.getBlockState(pos.west())
            .getBlock() instanceof WrapperBlockWall
            || worldIn.getBlockState(pos.east())
                .getBlock() instanceof WrapperBlockWall)
            || enumfacing$axis == EnumFacing.Axis.X && (worldIn.getBlockState(pos.north())
                .getBlock() instanceof WrapperBlockWall
                || worldIn.getBlockState(pos.south())
                    .getBlock() instanceof WrapperBlockWall)) {
            state = state.withProperty(IN_WALL, Boolean.valueOf(true));
        }

        return state;
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

    /**
     * Checks if this block can be placed exactly at the given position.
     */
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.down())
            .getMaterial()
            .isSolid() ? super.canPlaceBlockAt(worldIn, pos) : false;
    }

    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IWrapperBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        if (((Boolean) blockState.getValue(OPEN)).booleanValue()) {
            return NULL_AABB;
        } else {
            return ((EnumFacing) blockState.getValue(FACING)).getAxis() == EnumFacing.Axis.Z ? AABB_COLLISION_BOX_ZAXIS
                : AABB_COLLISION_BOX_XAXIS;
        }
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
        return ((Boolean) worldIn.getBlockState(pos)
            .getValue(OPEN)).booleanValue();
    }

    /**
     * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
     * IBlockstate
     */
    public IWrapperBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX,
        float hitY, float hitZ, int meta, EntityLivingBase placer) {
        boolean flag = worldIn.isBlockPowered(pos);
        return this.getDefaultState()
            .withProperty(FACING, placer.getHorizontalFacing())
            .withProperty(OPEN, Boolean.valueOf(flag))
            .withProperty(POWERED, Boolean.valueOf(flag))
            .withProperty(IN_WALL, Boolean.valueOf(false));
    }

    /**
     * Called when the block is right clicked by a player.
     */
    public boolean onBlockActivated(World worldIn, BlockPos pos, IWrapperBlockState state, EntityPlayer playerIn,
        EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (((Boolean) state.getValue(OPEN)).booleanValue()) {
            state = state.withProperty(OPEN, Boolean.valueOf(false));
            worldIn.setBlockState(pos, state, 10);
        } else {
            EnumFacing enumfacing = EnumFacing.fromAngle((double) playerIn.rotationYaw);

            if (state.getValue(FACING) == enumfacing.getOpposite()) {
                state = state.withProperty(FACING, enumfacing);
            }

            state = state.withProperty(OPEN, Boolean.valueOf(true));
            worldIn.setBlockState(pos, state, 10);
        }

        worldIn.playEvent(playerIn, ((Boolean) state.getValue(OPEN)).booleanValue() ? 1008 : 1014, pos, 0);
        return true;
    }

    /**
     * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
     * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
     * block, etc.
     */
    public void neighborChanged(IWrapperBlockState state, World worldIn, BlockPos pos, WrapperBlock wrapperBlockIn,
        BlockPos fromPos) {
        if (!worldIn.isRemote) {
            boolean flag = worldIn.isBlockPowered(pos);

            if (((Boolean) state.getValue(POWERED)).booleanValue() != flag) {
                worldIn.setBlockState(
                    pos,
                    state.withProperty(POWERED, Boolean.valueOf(flag))
                        .withProperty(OPEN, Boolean.valueOf(flag)),
                    2);

                if (((Boolean) state.getValue(OPEN)).booleanValue() != flag) {
                    worldIn.playEvent((EntityPlayer) null, flag ? 1008 : 1014, pos, 0);
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IWrapperBlockState blockState, IBlockAccess blockAccess, BlockPos pos,
        EnumFacing side) {
        return true;
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IWrapperBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
            .withProperty(FACING, EnumFacing.getHorizontal(meta))
            .withProperty(OPEN, Boolean.valueOf((meta & 4) != 0))
            .withProperty(POWERED, Boolean.valueOf((meta & 8) != 0));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        int i = 0;
        i = i | ((EnumFacing) state.getValue(FACING)).getHorizontalIndex();

        if (((Boolean) state.getValue(POWERED)).booleanValue()) {
            i |= 8;
        }

        if (((Boolean) state.getValue(OPEN)).booleanValue()) {
            i |= 4;
        }

        return i;
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { FACING, OPEN, POWERED, IN_WALL });
    }

    /* ======================================== FORGE START ======================================== */

    @Override
    public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, EnumFacing facing) {
        IWrapperBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof WrapperBlockFenceGate
            && state.getBlockFaceShape(world, pos, facing) == BlockFaceShape.MIDDLE_POLE) {
            WrapperBlock connector = world.getBlockState(pos.offset(facing))
                .getBlock();
            return connector instanceof WrapperBlockFence || connector instanceof WrapperBlockWall;
        }
        return false;
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
        if (face != EnumFacing.UP && face != EnumFacing.DOWN) {
            return ((EnumFacing) state.getValue(FACING)).getAxis() == face.rotateY()
                .getAxis() ? BlockFaceShape.MIDDLE_POLE : BlockFaceShape.UNDEFINED;
        } else {
            return BlockFaceShape.UNDEFINED;
        }
    }
}
