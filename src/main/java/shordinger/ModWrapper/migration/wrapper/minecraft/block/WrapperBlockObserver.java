package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WrapperBlockObserver extends WrapperBlockDirectional {

    public static final PropertyBool POWERED = PropertyBool.create("powered");

    public WrapperBlockObserver() {
        super(Material.ROCK);
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(FACING, EnumFacing.SOUTH)
                .withProperty(POWERED, Boolean.valueOf(false)));
        this.setCreativeTab(CreativeTabs.REDSTONE);
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { FACING, POWERED });
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

    public void updateTick(World worldIn, BlockPos pos, IWrapperBlockState state, Random rand) {
        if (((Boolean) state.getValue(POWERED)).booleanValue()) {
            worldIn.setBlockState(pos, state.withProperty(POWERED, Boolean.valueOf(false)), 2);
        } else {
            worldIn.setBlockState(pos, state.withProperty(POWERED, Boolean.valueOf(true)), 2);
            worldIn.scheduleUpdate(pos, this, 2);
        }

        this.updateNeighborsInFront(worldIn, pos, state);
    }

    /**
     * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
     * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
     * block, etc.
     */
    public void neighborChanged(IWrapperBlockState state, World worldIn, BlockPos pos, WrapperBlock wrapperBlockIn,
        BlockPos fromPos) {}

    public void observedNeighborChanged(IWrapperBlockState state, World worldIn, BlockPos pos,
        WrapperBlock wrapperBlockIn, BlockPos fromPos) {
        if (!worldIn.isRemote && pos.offset((EnumFacing) state.getValue(FACING))
            .equals(fromPos)) {
            this.startSignal(state, worldIn, pos);
        }
    }

    private void startSignal(IWrapperBlockState p_190960_1_, World p_190960_2_, BlockPos pos) {
        if (!((Boolean) p_190960_1_.getValue(POWERED)).booleanValue()) {
            if (!p_190960_2_.isUpdateScheduled(pos, this)) {
                p_190960_2_.scheduleUpdate(pos, this, 2);
            }
        }
    }

    protected void updateNeighborsInFront(World worldIn, BlockPos pos, IWrapperBlockState state) {
        EnumFacing enumfacing = (EnumFacing) state.getValue(FACING);
        BlockPos blockpos = pos.offset(enumfacing.getOpposite());
        worldIn.neighborChanged(blockpos, this, pos);
        worldIn.notifyNeighborsOfStateExcept(blockpos, this, enumfacing);
    }

    /**
     * Can this block provide power. Only wire currently seems to have this change based on its state.
     */
    public boolean canProvidePower(IWrapperBlockState state) {
        return true;
    }

    public int getStrongPower(IWrapperBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return blockState.getWeakPower(blockAccess, pos, side);
    }

    public int getWeakPower(IWrapperBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return ((Boolean) blockState.getValue(POWERED)).booleanValue() && blockState.getValue(FACING) == side ? 15 : 0;
    }

    /**
     * Called after the block is set in the Chunk data, but before the Tile Entity is set
     */
    public void onBlockAdded(World worldIn, BlockPos pos, IWrapperBlockState state) {
        if (!worldIn.isRemote) {
            if (((Boolean) state.getValue(POWERED)).booleanValue()) {
                this.updateTick(worldIn, pos, state, worldIn.rand);
            }

            this.startSignal(state, worldIn, pos);
        }
    }

    /**
     * Called serverside after this block is replaced with another in Chunk, but before the Tile Entity is updated
     */
    public void breakBlock(World worldIn, BlockPos pos, IWrapperBlockState state) {
        if (((Boolean) state.getValue(POWERED)).booleanValue() && worldIn.isUpdateScheduled(pos, this)) {
            this.updateNeighborsInFront(worldIn, pos, state.withProperty(POWERED, Boolean.valueOf(false)));
        }
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
                EnumFacing.getDirectionFromEntityLiving(pos, placer)
                    .getOpposite());
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        int i = 0;
        i = i | ((EnumFacing) state.getValue(FACING)).getIndex();

        if (((Boolean) state.getValue(POWERED)).booleanValue()) {
            i |= 8;
        }

        return i;
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IWrapperBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
            .withProperty(FACING, EnumFacing.getFront(meta & 7));
    }

    /* ======================================== FORGE START ===================================== */
    @Override
    public void observedNeighborChange(IWrapperBlockState observerState, World world, BlockPos observerPos,
        WrapperBlock changedWrapperBlock, BlockPos changedBlockPos) {
        observedNeighborChanged(observerState, world, observerPos, changedWrapperBlock, changedBlockPos);
    }
    /* ========================================= FORGE END ====================================== */
}
