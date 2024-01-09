package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.AxisAlignedBB;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WrapperBlockWallSign extends WrapperBlockSign {

    public static final PropertyDirection FACING = WrapperBlockHorizontal.FACING;
    protected static final AxisAlignedBB SIGN_EAST_AABB = new AxisAlignedBB(
        0.0D,
        0.28125D,
        0.0D,
        0.125D,
        0.78125D,
        1.0D);
    protected static final AxisAlignedBB SIGN_WEST_AABB = new AxisAlignedBB(
        0.875D,
        0.28125D,
        0.0D,
        1.0D,
        0.78125D,
        1.0D);
    protected static final AxisAlignedBB SIGN_SOUTH_AABB = new AxisAlignedBB(
        0.0D,
        0.28125D,
        0.0D,
        1.0D,
        0.78125D,
        0.125D);
    protected static final AxisAlignedBB SIGN_NORTH_AABB = new AxisAlignedBB(
        0.0D,
        0.28125D,
        0.875D,
        1.0D,
        0.78125D,
        1.0D);

    public WrapperBlockWallSign() {
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(FACING, EnumFacing.NORTH));
    }

    public AxisAlignedBB getBoundingBox(IWrapperBlockState state, IBlockAccess source, BlockPos pos) {
        switch ((EnumFacing) state.getValue(FACING)) {
            case NORTH:
            default:
                return SIGN_NORTH_AABB;
            case SOUTH:
                return SIGN_SOUTH_AABB;
            case WEST:
                return SIGN_WEST_AABB;
            case EAST:
                return SIGN_EAST_AABB;
        }
    }

    /**
     * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
     * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
     * block, etc.
     */
    public void neighborChanged(IWrapperBlockState state, World worldIn, BlockPos pos, WrapperBlock wrapperBlockIn,
        BlockPos fromPos) {
        EnumFacing enumfacing = (EnumFacing) state.getValue(FACING);

        if (!worldIn.getBlockState(pos.offset(enumfacing.getOpposite()))
            .getMaterial()
            .isSolid()) {
            this.dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockToAir(pos);
        }

        super.neighborChanged(state, worldIn, pos, wrapperBlockIn, fromPos);
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IWrapperBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = EnumFacing.getFront(meta);

        if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
            enumfacing = EnumFacing.NORTH;
        }

        return this.getDefaultState()
            .withProperty(FACING, enumfacing);
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        return ((EnumFacing) state.getValue(FACING)).getIndex();
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
        return new BlockStateContainer(this, new IProperty[] { FACING });
    }
}
