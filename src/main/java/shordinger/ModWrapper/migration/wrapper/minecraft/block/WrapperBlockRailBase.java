package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.google.common.collect.Lists;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.AxisAlignedBB;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public abstract class WrapperBlockRailBase extends WrapperBlock {

    protected static final AxisAlignedBB FLAT_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D);
    protected static final AxisAlignedBB ASCENDING_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);
    protected final boolean isPowered;

    public static boolean isRailBlock(World worldIn, BlockPos pos) {
        return isRailBlock(worldIn.getBlockState(pos));
    }

    public static boolean isRailBlock(IWrapperBlockState state) {
        WrapperBlock wrapperBlock = state.getBlock();
        return wrapperBlock instanceof WrapperBlockRailBase;
    }

    protected WrapperBlockRailBase(boolean isPowered) {
        super(Material.CIRCUITS);
        this.isPowered = isPowered;
        this.setCreativeTab(CreativeTabs.TRANSPORTATION);
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

    public AxisAlignedBB getBoundingBox(IWrapperBlockState state, IBlockAccess source, BlockPos pos) {
        WrapperBlockRailBase.EnumRailDirection blockrailbase$enumraildirection = state.getBlock() == this
            ? getRailDirection(source, pos, state, null)
            : null;
        return blockrailbase$enumraildirection != null && blockrailbase$enumraildirection.isAscending() ? ASCENDING_AABB
            : FLAT_AABB;
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

    public boolean isFullCube(IWrapperBlockState state) {
        return false;
    }

    /**
     * Checks if this block can be placed exactly at the given position.
     */
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.down())
            .isSideSolid(worldIn, pos.down(), EnumFacing.UP);
    }

    /**
     * Called after the block is set in the Chunk data, but before the Tile Entity is set
     */
    public void onBlockAdded(World worldIn, BlockPos pos, IWrapperBlockState state) {
        if (!worldIn.isRemote) {
            state = this.updateDir(worldIn, pos, state, true);

            if (this.isPowered) {
                state.neighborChanged(worldIn, pos, this, pos);
            }
        }
    }

    /**
     * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
     * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
     * block, etc.
     */
    public void neighborChanged(IWrapperBlockState state, World worldIn, BlockPos pos, WrapperBlock wrapperBlockIn,
        BlockPos fromPos) {
        if (!worldIn.isRemote) {
            final IWrapperBlockState currentState = worldIn.getBlockState(pos);
            WrapperBlockRailBase.EnumRailDirection blockrailbase$enumraildirection = getRailDirection(
                worldIn,
                pos,
                currentState.getBlock() == this ? currentState : state,
                null);
            boolean flag = false;

            if (!worldIn.getBlockState(pos.down())
                .isSideSolid(worldIn, pos.down(), EnumFacing.UP)) {
                flag = true;
            }

            if (blockrailbase$enumraildirection == WrapperBlockRailBase.EnumRailDirection.ASCENDING_EAST
                && !worldIn.getBlockState(pos.east())
                    .isSideSolid(worldIn, pos.east(), EnumFacing.UP)) {
                flag = true;
            } else if (blockrailbase$enumraildirection == WrapperBlockRailBase.EnumRailDirection.ASCENDING_WEST
                && !worldIn.getBlockState(pos.west())
                    .isSideSolid(worldIn, pos.west(), EnumFacing.UP)) {
                        flag = true;
                    } else
                if (blockrailbase$enumraildirection == WrapperBlockRailBase.EnumRailDirection.ASCENDING_NORTH
                    && !worldIn.getBlockState(pos.north())
                        .isSideSolid(worldIn, pos.north(), EnumFacing.UP)) {
                            flag = true;
                        } else
                    if (blockrailbase$enumraildirection == WrapperBlockRailBase.EnumRailDirection.ASCENDING_SOUTH
                        && !worldIn.getBlockState(pos.south())
                            .isSideSolid(worldIn, pos.south(), EnumFacing.UP)) {
                                flag = true;
                            }

            if (flag && !currentState.getBlock()
                .isAir(currentState, worldIn, pos)) {
                this.dropBlockAsItem(worldIn, pos, state, 0);
                worldIn.setBlockToAir(pos);
            } else {
                this.updateState(state, worldIn, pos, wrapperBlockIn);
            }
        }
    }

    protected void updateState(IWrapperBlockState state, World worldIn, BlockPos pos, WrapperBlock wrapperBlockIn) {}

    protected IWrapperBlockState updateDir(World worldIn, BlockPos pos, IWrapperBlockState state,
        boolean initialPlacement) {
        return worldIn.isRemote ? state
            : (new WrapperBlockRailBase.Rail(worldIn, pos, state)).place(worldIn.isBlockPowered(pos), initialPlacement)
                .getBlockState();
    }

    public EnumPushReaction getMobilityFlag(IWrapperBlockState state) {
        return EnumPushReaction.NORMAL;
    }

    /**
     * Gets the render layer this block will render on. SOLID for solid blocks, CUTOUT or CUTOUT_MIPPED for on-off
     * transparency (glass, reeds), TRANSLUCENT for fully blended transparency (stained glass)
     */
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    /**
     * Called serverside after this block is replaced with another in Chunk, but before the Tile Entity is updated
     */
    public void breakBlock(World worldIn, BlockPos pos, IWrapperBlockState state) {
        super.breakBlock(worldIn, pos, state);

        if (getRailDirection(worldIn, pos, state, null).isAscending()) {
            worldIn.notifyNeighborsOfStateChange(pos.up(), this, false);
        }

        if (this.isPowered) {
            worldIn.notifyNeighborsOfStateChange(pos, this, false);
            worldIn.notifyNeighborsOfStateChange(pos.down(), this, false);
        }
    }

    // Forge: Use getRailDirection(IBlockAccess, BlockPos, IWrapperBlockState, EntityMinecart) for enhanced ability
    public abstract IProperty<WrapperBlockRailBase.EnumRailDirection> getShapeProperty();

    /* ======================================== FORGE START ===================================== */
    /**
     * Return true if the rail can make corners.
     * Used by placement logic.
     * 
     * @param world The world.
     * @param pos   Block's position in world
     * @return True if the rail can make corners.
     */
    public boolean isFlexibleRail(IBlockAccess world, BlockPos pos) {
        return !this.isPowered;
    }

    /**
     * Returns true if the rail can make up and down slopes.
     * Used by placement logic.
     * 
     * @param world The world.
     * @param pos   Block's position in world
     * @return True if the rail can make slopes.
     */
    public boolean canMakeSlopes(IBlockAccess world, BlockPos pos) {
        return true;
    }

    /**
     * Return the rail's direction.
     * Can be used to make the cart think the rail is a different shape,
     * for example when making diamond junctions or switches.
     * The cart parameter will often be null unless it it called from EntityMinecart.
     *
     * @param world The world.
     * @param pos   Block's position in world
     * @param state The BlockState
     * @param cart  The cart asking for the metadata, null if it is not called by EntityMinecart.
     * @return The direction.
     */
    public EnumRailDirection getRailDirection(IBlockAccess world, BlockPos pos, IWrapperBlockState state,
        @javax.annotation.Nullable net.minecraft.entity.item.EntityMinecart cart) {
        return state.getValue(getShapeProperty());
    }

    /**
     * Returns the max speed of the rail at the specified position.
     * 
     * @param world The world.
     * @param cart  The cart on the rail, may be null.
     * @param pos   Block's position in world
     * @return The max speed of the current rail.
     */
    public float getRailMaxSpeed(World world, net.minecraft.entity.item.EntityMinecart cart, BlockPos pos) {
        return 0.4f;
    }

    /**
     * This function is called by any minecart that passes over this rail.
     * It is called once per update tick that the minecart is on the rail.
     * 
     * @param world The world.
     * @param cart  The cart on the rail.
     * @param pos   Block's position in world
     */
    public void onMinecartPass(World world, net.minecraft.entity.item.EntityMinecart cart, BlockPos pos) {}

    /**
     * Rotate the block. For vanilla blocks this rotates around the axis passed in (generally, it should be the "face"
     * that was hit).
     * Note: for mod blocks, this is up to the block and modder to decide. It is not mandated that it be a rotation
     * around the
     * face, but could be a rotation to orient *to* that face, or a visiting of possible rotations.
     * The method should return true if the rotation was successful though.
     *
     * @param world The world
     * @param pos   Block position in world
     * @param axis  The axis to rotate around
     * @return True if the rotation was successful, False if the rotation failed, or is not possible
     */
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
        IWrapperBlockState state = world.getBlockState(pos);
        for (IProperty prop : state.getProperties()
            .keySet()) {
            if (prop.getName()
                .equals("shape")) {
                world.setBlockState(pos, state.cycleProperty(prop));
                return true;
            }
        }
        return false;
    }

    /* ======================================== FORGE END ===================================== */

    public static enum EnumRailDirection implements IStringSerializable {

        NORTH_SOUTH(0, "north_south"),
        EAST_WEST(1, "east_west"),
        ASCENDING_EAST(2, "ascending_east"),
        ASCENDING_WEST(3, "ascending_west"),
        ASCENDING_NORTH(4, "ascending_north"),
        ASCENDING_SOUTH(5, "ascending_south"),
        SOUTH_EAST(6, "south_east"),
        SOUTH_WEST(7, "south_west"),
        NORTH_WEST(8, "north_west"),
        NORTH_EAST(9, "north_east");

        private static final WrapperBlockRailBase.EnumRailDirection[] META_LOOKUP = new WrapperBlockRailBase.EnumRailDirection[values().length];
        private final int meta;
        private final String name;

        private EnumRailDirection(int meta, String name) {
            this.meta = meta;
            this.name = name;
        }

        public int getMetadata() {
            return this.meta;
        }

        public String toString() {
            return this.name;
        }

        public boolean isAscending() {
            return this == ASCENDING_NORTH || this == ASCENDING_EAST
                || this == ASCENDING_SOUTH
                || this == ASCENDING_WEST;
        }

        public static WrapperBlockRailBase.EnumRailDirection byMetadata(int meta) {
            if (meta < 0 || meta >= META_LOOKUP.length) {
                meta = 0;
            }

            return META_LOOKUP[meta];
        }

        public String getName() {
            return this.name;
        }

        static {
            for (WrapperBlockRailBase.EnumRailDirection blockrailbase$enumraildirection : values()) {
                META_LOOKUP[blockrailbase$enumraildirection.getMetadata()] = blockrailbase$enumraildirection;
            }
        }
    }

    public class Rail {

        private final World world;
        private final BlockPos pos;
        private final WrapperBlockRailBase block;
        private IWrapperBlockState state;
        private final boolean isPowered;
        private final List<BlockPos> connectedRails = Lists.<BlockPos>newArrayList();
        private final boolean canMakeSlopes;

        public Rail(World worldIn, BlockPos pos, IWrapperBlockState state) {
            this.world = worldIn;
            this.pos = pos;
            this.state = state;
            this.block = (WrapperBlockRailBase) state.getBlock();
            WrapperBlockRailBase.EnumRailDirection blockrailbase$enumraildirection = block
                .getRailDirection(worldIn, pos, state, null);
            this.isPowered = !this.block.isFlexibleRail(worldIn, pos);
            this.canMakeSlopes = this.block.canMakeSlopes(worldIn, pos);
            this.updateConnectedRails(blockrailbase$enumraildirection);
        }

        public List<BlockPos> getConnectedRails() {
            return this.connectedRails;
        }

        private void updateConnectedRails(WrapperBlockRailBase.EnumRailDirection railDirection) {
            this.connectedRails.clear();

            switch (railDirection) {
                case NORTH_SOUTH:
                    this.connectedRails.add(this.pos.north());
                    this.connectedRails.add(this.pos.south());
                    break;
                case EAST_WEST:
                    this.connectedRails.add(this.pos.west());
                    this.connectedRails.add(this.pos.east());
                    break;
                case ASCENDING_EAST:
                    this.connectedRails.add(this.pos.west());
                    this.connectedRails.add(
                        this.pos.east()
                            .up());
                    break;
                case ASCENDING_WEST:
                    this.connectedRails.add(
                        this.pos.west()
                            .up());
                    this.connectedRails.add(this.pos.east());
                    break;
                case ASCENDING_NORTH:
                    this.connectedRails.add(
                        this.pos.north()
                            .up());
                    this.connectedRails.add(this.pos.south());
                    break;
                case ASCENDING_SOUTH:
                    this.connectedRails.add(this.pos.north());
                    this.connectedRails.add(
                        this.pos.south()
                            .up());
                    break;
                case SOUTH_EAST:
                    this.connectedRails.add(this.pos.east());
                    this.connectedRails.add(this.pos.south());
                    break;
                case SOUTH_WEST:
                    this.connectedRails.add(this.pos.west());
                    this.connectedRails.add(this.pos.south());
                    break;
                case NORTH_WEST:
                    this.connectedRails.add(this.pos.west());
                    this.connectedRails.add(this.pos.north());
                    break;
                case NORTH_EAST:
                    this.connectedRails.add(this.pos.east());
                    this.connectedRails.add(this.pos.north());
            }
        }

        private void removeSoftConnections() {
            for (int i = 0; i < this.connectedRails.size(); ++i) {
                WrapperBlockRailBase.Rail blockrailbase$rail = this.findRailAt(this.connectedRails.get(i));

                if (blockrailbase$rail != null && blockrailbase$rail.isConnectedToRail(this)) {
                    this.connectedRails.set(i, blockrailbase$rail.pos);
                } else {
                    this.connectedRails.remove(i--);
                }
            }
        }

        private boolean hasRailAt(BlockPos pos) {
            return WrapperBlockRailBase.isRailBlock(this.world, pos)
                || WrapperBlockRailBase.isRailBlock(this.world, pos.up())
                || WrapperBlockRailBase.isRailBlock(this.world, pos.down());
        }

        @Nullable
        private WrapperBlockRailBase.Rail findRailAt(BlockPos pos) {
            IWrapperBlockState iblockstate = this.world.getBlockState(pos);

            if (WrapperBlockRailBase.isRailBlock(iblockstate)) {
                return WrapperBlockRailBase.this.new Rail(this.world, pos, iblockstate);
            } else {
                BlockPos lvt_2_1_ = pos.up();
                iblockstate = this.world.getBlockState(lvt_2_1_);

                if (WrapperBlockRailBase.isRailBlock(iblockstate)) {
                    return WrapperBlockRailBase.this.new Rail(this.world, lvt_2_1_, iblockstate);
                } else {
                    lvt_2_1_ = pos.down();
                    iblockstate = this.world.getBlockState(lvt_2_1_);
                    return WrapperBlockRailBase.isRailBlock(iblockstate)
                        ? WrapperBlockRailBase.this.new Rail(this.world, lvt_2_1_, iblockstate)
                        : null;
                }
            }
        }

        private boolean isConnectedToRail(WrapperBlockRailBase.Rail rail) {
            return this.isConnectedTo(rail.pos);
        }

        private boolean isConnectedTo(BlockPos posIn) {
            for (int i = 0; i < this.connectedRails.size(); ++i) {
                BlockPos blockpos = this.connectedRails.get(i);

                if (blockpos.getX() == posIn.getX() && blockpos.getZ() == posIn.getZ()) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Counts the number of rails adjacent to this rail.
         */
        protected int countAdjacentRails() {
            int i = 0;

            for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
                if (this.hasRailAt(this.pos.offset(enumfacing))) {
                    ++i;
                }
            }

            return i;
        }

        private boolean canConnectTo(WrapperBlockRailBase.Rail rail) {
            return this.isConnectedToRail(rail) || this.connectedRails.size() != 2;
        }

        private void connectTo(WrapperBlockRailBase.Rail rail) {
            this.connectedRails.add(rail.pos);
            BlockPos blockpos = this.pos.north();
            BlockPos blockpos1 = this.pos.south();
            BlockPos blockpos2 = this.pos.west();
            BlockPos blockpos3 = this.pos.east();
            boolean flag = this.isConnectedTo(blockpos);
            boolean flag1 = this.isConnectedTo(blockpos1);
            boolean flag2 = this.isConnectedTo(blockpos2);
            boolean flag3 = this.isConnectedTo(blockpos3);
            WrapperBlockRailBase.EnumRailDirection blockrailbase$enumraildirection = null;

            if (flag || flag1) {
                blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.NORTH_SOUTH;
            }

            if (flag2 || flag3) {
                blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.EAST_WEST;
            }

            if (!this.isPowered) {
                if (flag1 && flag3 && !flag && !flag2) {
                    blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.SOUTH_EAST;
                }

                if (flag1 && flag2 && !flag && !flag3) {
                    blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.SOUTH_WEST;
                }

                if (flag && flag2 && !flag1 && !flag3) {
                    blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.NORTH_WEST;
                }

                if (flag && flag3 && !flag1 && !flag2) {
                    blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.NORTH_EAST;
                }
            }

            if (blockrailbase$enumraildirection == WrapperBlockRailBase.EnumRailDirection.NORTH_SOUTH
                && canMakeSlopes) {
                if (WrapperBlockRailBase.isRailBlock(this.world, blockpos.up())) {
                    blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.ASCENDING_NORTH;
                }

                if (WrapperBlockRailBase.isRailBlock(this.world, blockpos1.up())) {
                    blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.ASCENDING_SOUTH;
                }
            }

            if (blockrailbase$enumraildirection == WrapperBlockRailBase.EnumRailDirection.EAST_WEST && canMakeSlopes) {
                if (WrapperBlockRailBase.isRailBlock(this.world, blockpos3.up())) {
                    blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.ASCENDING_EAST;
                }

                if (WrapperBlockRailBase.isRailBlock(this.world, blockpos2.up())) {
                    blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.ASCENDING_WEST;
                }
            }

            if (blockrailbase$enumraildirection == null) {
                blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.NORTH_SOUTH;
            }

            this.state = this.state.withProperty(this.block.getShapeProperty(), blockrailbase$enumraildirection);
            this.world.setBlockState(this.pos, this.state, 3);
        }

        private boolean hasNeighborRail(BlockPos posIn) {
            WrapperBlockRailBase.Rail blockrailbase$rail = this.findRailAt(posIn);

            if (blockrailbase$rail == null) {
                return false;
            } else {
                blockrailbase$rail.removeSoftConnections();
                return blockrailbase$rail.canConnectTo(this);
            }
        }

        public WrapperBlockRailBase.Rail place(boolean powered, boolean initialPlacement) {
            BlockPos blockpos = this.pos.north();
            BlockPos blockpos1 = this.pos.south();
            BlockPos blockpos2 = this.pos.west();
            BlockPos blockpos3 = this.pos.east();
            boolean flag = this.hasNeighborRail(blockpos);
            boolean flag1 = this.hasNeighborRail(blockpos1);
            boolean flag2 = this.hasNeighborRail(blockpos2);
            boolean flag3 = this.hasNeighborRail(blockpos3);
            WrapperBlockRailBase.EnumRailDirection blockrailbase$enumraildirection = null;

            if ((flag || flag1) && !flag2 && !flag3) {
                blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.NORTH_SOUTH;
            }

            if ((flag2 || flag3) && !flag && !flag1) {
                blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.EAST_WEST;
            }

            if (!this.isPowered) {
                if (flag1 && flag3 && !flag && !flag2) {
                    blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.SOUTH_EAST;
                }

                if (flag1 && flag2 && !flag && !flag3) {
                    blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.SOUTH_WEST;
                }

                if (flag && flag2 && !flag1 && !flag3) {
                    blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.NORTH_WEST;
                }

                if (flag && flag3 && !flag1 && !flag2) {
                    blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.NORTH_EAST;
                }
            }

            if (blockrailbase$enumraildirection == null) {
                if (flag || flag1) {
                    blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.NORTH_SOUTH;
                }

                if (flag2 || flag3) {
                    blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.EAST_WEST;
                }

                if (!this.isPowered) {
                    if (powered) {
                        if (flag1 && flag3) {
                            blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.SOUTH_EAST;
                        }

                        if (flag2 && flag1) {
                            blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.SOUTH_WEST;
                        }

                        if (flag3 && flag) {
                            blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.NORTH_EAST;
                        }

                        if (flag && flag2) {
                            blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.NORTH_WEST;
                        }
                    } else {
                        if (flag && flag2) {
                            blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.NORTH_WEST;
                        }

                        if (flag3 && flag) {
                            blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.NORTH_EAST;
                        }

                        if (flag2 && flag1) {
                            blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.SOUTH_WEST;
                        }

                        if (flag1 && flag3) {
                            blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.SOUTH_EAST;
                        }
                    }
                }
            }

            if (blockrailbase$enumraildirection == WrapperBlockRailBase.EnumRailDirection.NORTH_SOUTH
                && canMakeSlopes) {
                if (WrapperBlockRailBase.isRailBlock(this.world, blockpos.up())) {
                    blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.ASCENDING_NORTH;
                }

                if (WrapperBlockRailBase.isRailBlock(this.world, blockpos1.up())) {
                    blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.ASCENDING_SOUTH;
                }
            }

            if (blockrailbase$enumraildirection == WrapperBlockRailBase.EnumRailDirection.EAST_WEST && canMakeSlopes) {
                if (WrapperBlockRailBase.isRailBlock(this.world, blockpos3.up())) {
                    blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.ASCENDING_EAST;
                }

                if (WrapperBlockRailBase.isRailBlock(this.world, blockpos2.up())) {
                    blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.ASCENDING_WEST;
                }
            }

            if (blockrailbase$enumraildirection == null) {
                blockrailbase$enumraildirection = WrapperBlockRailBase.EnumRailDirection.NORTH_SOUTH;
            }

            this.updateConnectedRails(blockrailbase$enumraildirection);
            this.state = this.state.withProperty(this.block.getShapeProperty(), blockrailbase$enumraildirection);

            if (initialPlacement || this.world.getBlockState(this.pos) != this.state) {
                this.world.setBlockState(this.pos, this.state, 3);

                for (int i = 0; i < this.connectedRails.size(); ++i) {
                    WrapperBlockRailBase.Rail blockrailbase$rail = this.findRailAt(this.connectedRails.get(i));

                    if (blockrailbase$rail != null) {
                        blockrailbase$rail.removeSoftConnections();

                        if (blockrailbase$rail.canConnectTo(this)) {
                            blockrailbase$rail.connectTo(this);
                        }
                    }
                }
            }

            return this;
        }

        public IWrapperBlockState getBlockState() {
            return this.state;
        }
    }
}
