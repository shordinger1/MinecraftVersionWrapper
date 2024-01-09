package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.AxisAlignedBB;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WrapperBlockPistonExtension extends WrapperBlockDirectional {

    public static final PropertyEnum<WrapperBlockPistonExtension.EnumPistonType> TYPE = PropertyEnum.<WrapperBlockPistonExtension.EnumPistonType>create(
        "type",
        WrapperBlockPistonExtension.EnumPistonType.class);
    public static final PropertyBool SHORT = PropertyBool.create("short");
    protected static final AxisAlignedBB PISTON_EXTENSION_EAST_AABB = new AxisAlignedBB(
        0.75D,
        0.0D,
        0.0D,
        1.0D,
        1.0D,
        1.0D);
    protected static final AxisAlignedBB PISTON_EXTENSION_WEST_AABB = new AxisAlignedBB(
        0.0D,
        0.0D,
        0.0D,
        0.25D,
        1.0D,
        1.0D);
    protected static final AxisAlignedBB PISTON_EXTENSION_SOUTH_AABB = new AxisAlignedBB(
        0.0D,
        0.0D,
        0.75D,
        1.0D,
        1.0D,
        1.0D);
    protected static final AxisAlignedBB PISTON_EXTENSION_NORTH_AABB = new AxisAlignedBB(
        0.0D,
        0.0D,
        0.0D,
        1.0D,
        1.0D,
        0.25D);
    protected static final AxisAlignedBB PISTON_EXTENSION_UP_AABB = new AxisAlignedBB(
        0.0D,
        0.75D,
        0.0D,
        1.0D,
        1.0D,
        1.0D);
    protected static final AxisAlignedBB PISTON_EXTENSION_DOWN_AABB = new AxisAlignedBB(
        0.0D,
        0.0D,
        0.0D,
        1.0D,
        0.25D,
        1.0D);
    protected static final AxisAlignedBB UP_ARM_AABB = new AxisAlignedBB(0.375D, -0.25D, 0.375D, 0.625D, 0.75D, 0.625D);
    protected static final AxisAlignedBB DOWN_ARM_AABB = new AxisAlignedBB(
        0.375D,
        0.25D,
        0.375D,
        0.625D,
        1.25D,
        0.625D);
    protected static final AxisAlignedBB SOUTH_ARM_AABB = new AxisAlignedBB(
        0.375D,
        0.375D,
        -0.25D,
        0.625D,
        0.625D,
        0.75D);
    protected static final AxisAlignedBB NORTH_ARM_AABB = new AxisAlignedBB(
        0.375D,
        0.375D,
        0.25D,
        0.625D,
        0.625D,
        1.25D);
    protected static final AxisAlignedBB EAST_ARM_AABB = new AxisAlignedBB(
        -0.25D,
        0.375D,
        0.375D,
        0.75D,
        0.625D,
        0.625D);
    protected static final AxisAlignedBB WEST_ARM_AABB = new AxisAlignedBB(
        0.25D,
        0.375D,
        0.375D,
        1.25D,
        0.625D,
        0.625D);
    protected static final AxisAlignedBB SHORT_UP_ARM_AABB = new AxisAlignedBB(
        0.375D,
        0.0D,
        0.375D,
        0.625D,
        0.75D,
        0.625D);
    protected static final AxisAlignedBB SHORT_DOWN_ARM_AABB = new AxisAlignedBB(
        0.375D,
        0.25D,
        0.375D,
        0.625D,
        1.0D,
        0.625D);
    protected static final AxisAlignedBB SHORT_SOUTH_ARM_AABB = new AxisAlignedBB(
        0.375D,
        0.375D,
        0.0D,
        0.625D,
        0.625D,
        0.75D);
    protected static final AxisAlignedBB SHORT_NORTH_ARM_AABB = new AxisAlignedBB(
        0.375D,
        0.375D,
        0.25D,
        0.625D,
        0.625D,
        1.0D);
    protected static final AxisAlignedBB SHORT_EAST_ARM_AABB = new AxisAlignedBB(
        0.0D,
        0.375D,
        0.375D,
        0.75D,
        0.625D,
        0.625D);
    protected static final AxisAlignedBB SHORT_WEST_ARM_AABB = new AxisAlignedBB(
        0.25D,
        0.375D,
        0.375D,
        1.0D,
        0.625D,
        0.625D);

    public WrapperBlockPistonExtension() {
        super(Material.PISTON);
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(FACING, EnumFacing.NORTH)
                .withProperty(TYPE, WrapperBlockPistonExtension.EnumPistonType.DEFAULT)
                .withProperty(SHORT, Boolean.valueOf(false)));
        this.setSoundType(SoundType.STONE);
        this.setHardness(0.5F);
    }

    public AxisAlignedBB getBoundingBox(IWrapperBlockState state, IBlockAccess source, BlockPos pos) {
        switch ((EnumFacing) state.getValue(FACING)) {
            case DOWN:
            default:
                return PISTON_EXTENSION_DOWN_AABB;
            case UP:
                return PISTON_EXTENSION_UP_AABB;
            case NORTH:
                return PISTON_EXTENSION_NORTH_AABB;
            case SOUTH:
                return PISTON_EXTENSION_SOUTH_AABB;
            case WEST:
                return PISTON_EXTENSION_WEST_AABB;
            case EAST:
                return PISTON_EXTENSION_EAST_AABB;
        }
    }

    public void addCollisionBoxToList(IWrapperBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox,
        List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        addCollisionBoxToList(pos, entityBox, collidingBoxes, state.getBoundingBox(worldIn, pos));
        addCollisionBoxToList(pos, entityBox, collidingBoxes, this.getArmShape(state));
    }

    private AxisAlignedBB getArmShape(IWrapperBlockState state) {
        boolean flag = ((Boolean) state.getValue(SHORT)).booleanValue();

        switch ((EnumFacing) state.getValue(FACING)) {
            case DOWN:
            default:
                return flag ? SHORT_DOWN_ARM_AABB : DOWN_ARM_AABB;
            case UP:
                return flag ? SHORT_UP_ARM_AABB : UP_ARM_AABB;
            case NORTH:
                return flag ? SHORT_NORTH_ARM_AABB : NORTH_ARM_AABB;
            case SOUTH:
                return flag ? SHORT_SOUTH_ARM_AABB : SOUTH_ARM_AABB;
            case WEST:
                return flag ? SHORT_WEST_ARM_AABB : WEST_ARM_AABB;
            case EAST:
                return flag ? SHORT_EAST_ARM_AABB : EAST_ARM_AABB;
        }
    }

    /**
     * Determines if the block is solid enough on the top side to support other blocks, like redstone components.
     */
    public boolean isTopSolid(IWrapperBlockState state) {
        return state.getValue(FACING) == EnumFacing.UP;
    }

    /**
     * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually
     * collect this block
     */
    public void onBlockHarvested(World worldIn, BlockPos pos, IWrapperBlockState state, EntityPlayer player) {
        if (player.capabilities.isCreativeMode) {
            BlockPos blockpos = pos.offset(((EnumFacing) state.getValue(FACING)).getOpposite());
            WrapperBlock wrapperBlock = worldIn.getBlockState(blockpos)
                .getBlock();

            if (wrapperBlock == Blocks.PISTON || wrapperBlock == Blocks.STICKY_PISTON) {
                worldIn.setBlockToAir(blockpos);
            }
        }

        super.onBlockHarvested(worldIn, pos, state, player);
    }

    /**
     * Called serverside after this block is replaced with another in Chunk, but before the Tile Entity is updated
     */
    public void breakBlock(World worldIn, BlockPos pos, IWrapperBlockState state) {
        super.breakBlock(worldIn, pos, state);
        EnumFacing enumfacing = ((EnumFacing) state.getValue(FACING)).getOpposite();
        pos = pos.offset(enumfacing);
        IWrapperBlockState iblockstate = worldIn.getBlockState(pos);

        if ((iblockstate.getBlock() == Blocks.PISTON || iblockstate.getBlock() == Blocks.STICKY_PISTON)
            && ((Boolean) iblockstate.getValue(WrapperBlockPistonBase.EXTENDED)).booleanValue()) {
            iblockstate.getBlock()
                .dropBlockAsItem(worldIn, pos, iblockstate, 0);
            worldIn.setBlockToAir(pos);
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
     * Checks if this block can be placed exactly at the given position.
     */
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return false;
    }

    /**
     * Check whether this Block can be placed at pos, while aiming at the specified side of an adjacent block
     */
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
        return false;
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    public int quantityDropped(Random random) {
        return 0;
    }

    /**
     * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
     * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
     * block, etc.
     */
    public void neighborChanged(IWrapperBlockState state, World worldIn, BlockPos pos, WrapperBlock wrapperBlockIn,
        BlockPos fromPos) {
        EnumFacing enumfacing = (EnumFacing) state.getValue(FACING);
        BlockPos blockpos = pos.offset(enumfacing.getOpposite());
        IWrapperBlockState iblockstate = worldIn.getBlockState(blockpos);

        if (iblockstate.getBlock() != Blocks.PISTON && iblockstate.getBlock() != Blocks.STICKY_PISTON) {
            worldIn.setBlockToAir(pos);
        } else {
            iblockstate.neighborChanged(worldIn, blockpos, wrapperBlockIn, fromPos);
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IWrapperBlockState blockState, IBlockAccess blockAccess, BlockPos pos,
        EnumFacing side) {
        return true;
    }

    @Nullable
    public static EnumFacing getFacing(int meta) {
        int i = meta & 7;
        return i > 5 ? null : EnumFacing.getFront(i);
    }

    public ItemStack getItem(World worldIn, BlockPos pos, IWrapperBlockState state) {
        return new ItemStack(
            state.getValue(TYPE) == WrapperBlockPistonExtension.EnumPistonType.STICKY ? Blocks.STICKY_PISTON
                : Blocks.PISTON);
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IWrapperBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
            .withProperty(FACING, getFacing(meta))
            .withProperty(
                TYPE,
                (meta & 8) > 0 ? WrapperBlockPistonExtension.EnumPistonType.STICKY
                    : WrapperBlockPistonExtension.EnumPistonType.DEFAULT);
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        int i = 0;
        i = i | ((EnumFacing) state.getValue(FACING)).getIndex();

        if (state.getValue(TYPE) == WrapperBlockPistonExtension.EnumPistonType.STICKY) {
            i |= 8;
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
        return new BlockStateContainer(this, new IProperty[] { FACING, TYPE, SHORT });
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
        return face == state.getValue(FACING) ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }

    public static enum EnumPistonType implements IStringSerializable {

        DEFAULT("normal"),
        STICKY("sticky");

        private final String VARIANT;

        private EnumPistonType(String name) {
            this.VARIANT = name;
        }

        public String toString() {
            return this.VARIANT;
        }

        public String getName() {
            return this.VARIANT;
        }
    }
}
