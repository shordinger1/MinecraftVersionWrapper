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
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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

public class BlockTripWire extends Block {

    public static final PropertyBool POWERED = PropertyBool.create("powered");
    public static final PropertyBool ATTACHED = PropertyBool.create("attached");
    public static final PropertyBool DISARMED = PropertyBool.create("disarmed");
    public static final PropertyBool NORTH = PropertyBool.create("north");
    public static final PropertyBool EAST = PropertyBool.create("east");
    public static final PropertyBool SOUTH = PropertyBool.create("south");
    public static final PropertyBool WEST = PropertyBool.create("west");
    protected static final AxisAlignedBB AABB = new AxisAlignedBB(0.0D, 0.0625D, 0.0D, 1.0D, 0.15625D, 1.0D);
    protected static final AxisAlignedBB TRIP_WRITE_ATTACHED_AABB = new AxisAlignedBB(
        0.0D,
        0.0D,
        0.0D,
        1.0D,
        0.5D,
        1.0D);

    public BlockTripWire() {
        super(Material.CIRCUITS);
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(POWERED, Boolean.valueOf(false))
                .withProperty(ATTACHED, Boolean.valueOf(false))
                .withProperty(DISARMED, Boolean.valueOf(false))
                .withProperty(NORTH, Boolean.valueOf(false))
                .withProperty(EAST, Boolean.valueOf(false))
                .withProperty(SOUTH, Boolean.valueOf(false))
                .withProperty(WEST, Boolean.valueOf(false)));
        this.setTickRandomly(true);
    }

    public AxisAlignedBB getBoundingBox(IWrapperBlockState state, IBlockAccess source, BlockPos pos) {
        return !((Boolean) state.getValue(ATTACHED)).booleanValue() ? TRIP_WRITE_ATTACHED_AABB : AABB;
    }

    /**
     * Get the actual Block state of this Block at the given position. This applies properties not visible in the
     * metadata, such as fence connections.
     */
    public IWrapperBlockState getActualState(IWrapperBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return state.withProperty(NORTH, Boolean.valueOf(isConnectedTo(worldIn, pos, state, EnumFacing.NORTH)))
            .withProperty(EAST, Boolean.valueOf(isConnectedTo(worldIn, pos, state, EnumFacing.EAST)))
            .withProperty(SOUTH, Boolean.valueOf(isConnectedTo(worldIn, pos, state, EnumFacing.SOUTH)))
            .withProperty(WEST, Boolean.valueOf(isConnectedTo(worldIn, pos, state, EnumFacing.WEST)));
    }

    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IWrapperBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
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
     * Gets the render layer this block will render on. SOLID for solid blocks, CUTOUT or CUTOUT_MIPPED for on-off
     * transparency (glass, reeds), TRANSLUCENT for fully blended transparency (stained glass)
     */
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    public Item getItemDropped(IWrapperBlockState state, Random rand, int fortune) {
        return Items.STRING;
    }

    public ItemStack getItem(World worldIn, BlockPos pos, IWrapperBlockState state) {
        return new ItemStack(Items.STRING);
    }

    /**
     * Called after the block is set in the Chunk data, but before the Tile Entity is set
     */
    public void onBlockAdded(World worldIn, BlockPos pos, IWrapperBlockState state) {
        worldIn.setBlockState(pos, state, 3);
        this.notifyHook(worldIn, pos, state);
    }

    /**
     * Called serverside after this block is replaced with another in Chunk, but before the Tile Entity is updated
     */
    public void breakBlock(World worldIn, BlockPos pos, IWrapperBlockState state) {
        this.notifyHook(worldIn, pos, state.withProperty(POWERED, Boolean.valueOf(true)));
    }

    /**
     * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually
     * collect this block
     */
    public void onBlockHarvested(World worldIn, BlockPos pos, IWrapperBlockState state, EntityPlayer player) {
        if (!worldIn.isRemote) {
            if (!player.getHeldItemMainhand()
                .isEmpty()
                && player.getHeldItemMainhand()
                    .getItem() == Items.SHEARS) {
                worldIn.setBlockState(pos, state.withProperty(DISARMED, Boolean.valueOf(true)), 4);
            }
        }
    }

    private void notifyHook(World worldIn, BlockPos pos, IWrapperBlockState state) {
        for (EnumFacing enumfacing : new EnumFacing[] { EnumFacing.SOUTH, EnumFacing.WEST }) {
            for (int i = 1; i < 42; ++i) {
                BlockPos blockpos = pos.offset(enumfacing, i);
                IWrapperBlockState iblockstate = worldIn.getBlockState(blockpos);

                if (iblockstate.getBlock() == Blocks.TRIPWIRE_HOOK) {
                    if (iblockstate.getValue(BlockTripWireHook.FACING) == enumfacing.getOpposite()) {
                        Blocks.TRIPWIRE_HOOK.calculateState(worldIn, blockpos, iblockstate, false, true, i, state);
                    }

                    break;
                }

                if (iblockstate.getBlock() != Blocks.TRIPWIRE) {
                    break;
                }
            }
        }
    }

    /**
     * Called When an Entity Collided with the Block
     */
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IWrapperBlockState state, Entity entityIn) {
        if (!worldIn.isRemote) {
            if (!((Boolean) state.getValue(POWERED)).booleanValue()) {
                this.updateState(worldIn, pos);
            }
        }
    }

    /**
     * Called randomly when setTickRandomly is set to true (used by e.g. crops to grow, etc.)
     */
    public void randomTick(World worldIn, BlockPos pos, IWrapperBlockState state, Random random) {}

    public void updateTick(World worldIn, BlockPos pos, IWrapperBlockState state, Random rand) {
        if (!worldIn.isRemote) {
            if (((Boolean) worldIn.getBlockState(pos)
                .getValue(POWERED)).booleanValue()) {
                this.updateState(worldIn, pos);
            }
        }
    }

    private void updateState(World worldIn, BlockPos pos) {
        IWrapperBlockState iblockstate = worldIn.getBlockState(pos);
        boolean flag = ((Boolean) iblockstate.getValue(POWERED)).booleanValue();
        boolean flag1 = false;
        List<? extends Entity> list = worldIn.getEntitiesWithinAABBExcludingEntity(
            (Entity) null,
            iblockstate.getBoundingBox(worldIn, pos)
                .offset(pos));

        if (!list.isEmpty()) {
            for (Entity entity : list) {
                if (!entity.doesEntityNotTriggerPressurePlate()) {
                    flag1 = true;
                    break;
                }
            }
        }

        if (flag1 != flag) {
            iblockstate = iblockstate.withProperty(POWERED, Boolean.valueOf(flag1));
            worldIn.setBlockState(pos, iblockstate, 3);
            this.notifyHook(worldIn, pos, iblockstate);
        }

        if (flag1) {
            worldIn.scheduleUpdate(new BlockPos(pos), this, this.tickRate(worldIn));
        }
    }

    public static boolean isConnectedTo(IBlockAccess worldIn, BlockPos pos, IWrapperBlockState state,
        EnumFacing direction) {
        BlockPos blockpos = pos.offset(direction);
        IWrapperBlockState iblockstate = worldIn.getBlockState(blockpos);
        Block block = iblockstate.getBlock();

        if (block == Blocks.TRIPWIRE_HOOK) {
            EnumFacing enumfacing = direction.getOpposite();
            return iblockstate.getValue(BlockTripWireHook.FACING) == enumfacing;
        } else {
            return block == Blocks.TRIPWIRE;
        }
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IWrapperBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
            .withProperty(POWERED, Boolean.valueOf((meta & 1) > 0))
            .withProperty(ATTACHED, Boolean.valueOf((meta & 4) > 0))
            .withProperty(DISARMED, Boolean.valueOf((meta & 8) > 0));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        int i = 0;

        if (((Boolean) state.getValue(POWERED)).booleanValue()) {
            i |= 1;
        }

        if (((Boolean) state.getValue(ATTACHED)).booleanValue()) {
            i |= 4;
        }

        if (((Boolean) state.getValue(DISARMED)).booleanValue()) {
            i |= 8;
        }

        return i;
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
        return new BlockStateContainer(this, new IProperty[] { POWERED, ATTACHED, DISARMED, NORTH, EAST, WEST, SOUTH });
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
        return BlockFaceShape.UNDEFINED;
    }
}
