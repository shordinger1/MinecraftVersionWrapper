package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.AxisAlignedBB;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.MathHelper;

public class BlockChest extends BlockContainer {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    protected static final AxisAlignedBB NORTH_CHEST_AABB = new AxisAlignedBB(
        0.0625D,
        0.0D,
        0.0D,
        0.9375D,
        0.875D,
        0.9375D);
    protected static final AxisAlignedBB SOUTH_CHEST_AABB = new AxisAlignedBB(
        0.0625D,
        0.0D,
        0.0625D,
        0.9375D,
        0.875D,
        1.0D);
    protected static final AxisAlignedBB WEST_CHEST_AABB = new AxisAlignedBB(
        0.0D,
        0.0D,
        0.0625D,
        0.9375D,
        0.875D,
        0.9375D);
    protected static final AxisAlignedBB EAST_CHEST_AABB = new AxisAlignedBB(
        0.0625D,
        0.0D,
        0.0625D,
        1.0D,
        0.875D,
        0.9375D);
    protected static final AxisAlignedBB NOT_CONNECTED_AABB = new AxisAlignedBB(
        0.0625D,
        0.0D,
        0.0625D,
        0.9375D,
        0.875D,
        0.9375D);
    /** 0 : Normal chest, 1 : Trapped chest */
    public final BlockChest.Type chestType;

    protected BlockChest(BlockChest.Type chestTypeIn) {
        super(Material.WOOD);
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(FACING, EnumFacing.NORTH));
        this.chestType = chestTypeIn;
        this.setCreativeTab(
            chestTypeIn == BlockChest.Type.TRAP ? CreativeTabs.REDSTONE : CreativeTabs.DECORATIONS);
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
    public boolean hasCustomBreakingProgress(IWrapperBlockState state) {
        return true;
    }

    /**
     * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
     * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
     */
    public EnumBlockRenderType getRenderType(IWrapperBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    public AxisAlignedBB getBoundingBox(IWrapperBlockState state, IBlockAccess source, BlockPos pos) {
        if (source.getBlockState(pos.north())
            .getBlock() == this) {
            return NORTH_CHEST_AABB;
        } else if (source.getBlockState(pos.south())
            .getBlock() == this) {
                return SOUTH_CHEST_AABB;
            } else if (source.getBlockState(pos.west())
                .getBlock() == this) {
                    return WEST_CHEST_AABB;
                } else {
                    return source.getBlockState(pos.east())
                        .getBlock() == this ? EAST_CHEST_AABB : NOT_CONNECTED_AABB;
                }
    }

    /**
     * Called after the block is set in the Chunk data, but before the Tile Entity is set
     */
    public void onBlockAdded(World worldIn, BlockPos pos, IWrapperBlockState state) {
        this.checkForSurroundingChests(worldIn, pos, state);

        for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
            BlockPos blockpos = pos.offset(enumfacing);
            IWrapperBlockState iblockstate = worldIn.getBlockState(blockpos);

            if (iblockstate.getBlock() == this) {
                this.checkForSurroundingChests(worldIn, blockpos, iblockstate);
            }
        }
    }

    /**
     * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
     * IBlockstate
     */
    public IWrapperBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX,
        float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState()
            .withProperty(FACING, placer.getHorizontalFacing());
    }

    /**
     * Called by ItemBlocks after a block is set in the world, to allow post-place logic
     */
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IWrapperBlockState state, EntityLivingBase placer,
        ItemStack stack) {
        EnumFacing enumfacing = EnumFacing
            .getHorizontal(MathHelper.floor((double) (placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3)
            .getOpposite();
        state = state.withProperty(FACING, enumfacing);
        BlockPos blockpos = pos.north();
        BlockPos blockpos1 = pos.south();
        BlockPos blockpos2 = pos.west();
        BlockPos blockpos3 = pos.east();
        boolean flag = this == worldIn.getBlockState(blockpos)
            .getBlock();
        boolean flag1 = this == worldIn.getBlockState(blockpos1)
            .getBlock();
        boolean flag2 = this == worldIn.getBlockState(blockpos2)
            .getBlock();
        boolean flag3 = this == worldIn.getBlockState(blockpos3)
            .getBlock();

        if (!flag && !flag1 && !flag2 && !flag3) {
            worldIn.setBlockState(pos, state, 3);
        } else if (enumfacing.getAxis() != EnumFacing.Axis.X || !flag && !flag1) {
            if (enumfacing.getAxis() == EnumFacing.Axis.Z && (flag2 || flag3)) {
                if (flag2) {
                    worldIn.setBlockState(blockpos2, state, 3);
                } else {
                    worldIn.setBlockState(blockpos3, state, 3);
                }

                worldIn.setBlockState(pos, state, 3);
            }
        } else {
            if (flag) {
                worldIn.setBlockState(blockpos, state, 3);
            } else {
                worldIn.setBlockState(blockpos1, state, 3);
            }

            worldIn.setBlockState(pos, state, 3);
        }

        if (stack.hasDisplayName()) {
            TileEntity tileentity = worldIn.getTileEntity(pos);

            if (tileentity instanceof TileEntityChest) {
                ((TileEntityChest) tileentity).setCustomName(stack.getDisplayName());
            }
        }
    }

    public IWrapperBlockState checkForSurroundingChests(World worldIn, BlockPos pos, IWrapperBlockState state) {
        if (worldIn.isRemote) {
            return state;
        } else {
            IWrapperBlockState iblockstate = worldIn.getBlockState(pos.north());
            IWrapperBlockState iblockstate1 = worldIn.getBlockState(pos.south());
            IWrapperBlockState iblockstate2 = worldIn.getBlockState(pos.west());
            IWrapperBlockState iblockstate3 = worldIn.getBlockState(pos.east());
            EnumFacing enumfacing = (EnumFacing) state.getValue(FACING);

            if (iblockstate.getBlock() != this && iblockstate1.getBlock() != this) {
                boolean flag = iblockstate.isFullBlock();
                boolean flag1 = iblockstate1.isFullBlock();

                if (iblockstate2.getBlock() == this || iblockstate3.getBlock() == this) {
                    BlockPos blockpos1 = iblockstate2.getBlock() == this ? pos.west() : pos.east();
                    IWrapperBlockState iblockstate7 = worldIn.getBlockState(blockpos1.north());
                    IWrapperBlockState iblockstate6 = worldIn.getBlockState(blockpos1.south());
                    enumfacing = EnumFacing.SOUTH;
                    EnumFacing enumfacing2;

                    if (iblockstate2.getBlock() == this) {
                        enumfacing2 = (EnumFacing) iblockstate2.getValue(FACING);
                    } else {
                        enumfacing2 = (EnumFacing) iblockstate3.getValue(FACING);
                    }

                    if (enumfacing2 == EnumFacing.NORTH) {
                        enumfacing = EnumFacing.NORTH;
                    }

                    if ((flag || iblockstate7.isFullBlock()) && !flag1 && !iblockstate6.isFullBlock()) {
                        enumfacing = EnumFacing.SOUTH;
                    }

                    if ((flag1 || iblockstate6.isFullBlock()) && !flag && !iblockstate7.isFullBlock()) {
                        enumfacing = EnumFacing.NORTH;
                    }
                }
            } else {
                BlockPos blockpos = iblockstate.getBlock() == this ? pos.north() : pos.south();
                IWrapperBlockState iblockstate4 = worldIn.getBlockState(blockpos.west());
                IWrapperBlockState iblockstate5 = worldIn.getBlockState(blockpos.east());
                enumfacing = EnumFacing.EAST;
                EnumFacing enumfacing1;

                if (iblockstate.getBlock() == this) {
                    enumfacing1 = (EnumFacing) iblockstate.getValue(FACING);
                } else {
                    enumfacing1 = (EnumFacing) iblockstate1.getValue(FACING);
                }

                if (enumfacing1 == EnumFacing.WEST) {
                    enumfacing = EnumFacing.WEST;
                }

                if ((iblockstate2.isFullBlock() || iblockstate4.isFullBlock()) && !iblockstate3.isFullBlock()
                    && !iblockstate5.isFullBlock()) {
                    enumfacing = EnumFacing.EAST;
                }

                if ((iblockstate3.isFullBlock() || iblockstate5.isFullBlock()) && !iblockstate2.isFullBlock()
                    && !iblockstate4.isFullBlock()) {
                    enumfacing = EnumFacing.WEST;
                }
            }

            state = state.withProperty(FACING, enumfacing);
            worldIn.setBlockState(pos, state, 3);
            return state;
        }
    }

    public IWrapperBlockState correctFacing(World worldIn, BlockPos pos, IWrapperBlockState state) {
        EnumFacing enumfacing = null;

        for (EnumFacing enumfacing1 : EnumFacing.Plane.HORIZONTAL) {
            IWrapperBlockState iblockstate = worldIn.getBlockState(pos.offset(enumfacing1));

            if (iblockstate.getBlock() == this) {
                return state;
            }

            if (iblockstate.isFullBlock()) {
                if (enumfacing != null) {
                    enumfacing = null;
                    break;
                }

                enumfacing = enumfacing1;
            }
        }

        if (enumfacing != null) {
            return state.withProperty(FACING, enumfacing.getOpposite());
        } else {
            EnumFacing enumfacing2 = (EnumFacing) state.getValue(FACING);

            if (worldIn.getBlockState(pos.offset(enumfacing2))
                .isFullBlock()) {
                enumfacing2 = enumfacing2.getOpposite();
            }

            if (worldIn.getBlockState(pos.offset(enumfacing2))
                .isFullBlock()) {
                enumfacing2 = enumfacing2.rotateY();
            }

            if (worldIn.getBlockState(pos.offset(enumfacing2))
                .isFullBlock()) {
                enumfacing2 = enumfacing2.getOpposite();
            }

            return state.withProperty(FACING, enumfacing2);
        }
    }

    /**
     * Checks if this block can be placed exactly at the given position.
     */
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        int i = 0;
        BlockPos blockpos = pos.west();
        BlockPos blockpos1 = pos.east();
        BlockPos blockpos2 = pos.north();
        BlockPos blockpos3 = pos.south();

        if (worldIn.getBlockState(blockpos)
            .getBlock() == this) {
            if (this.isDoubleChest(worldIn, blockpos)) {
                return false;
            }

            ++i;
        }

        if (worldIn.getBlockState(blockpos1)
            .getBlock() == this) {
            if (this.isDoubleChest(worldIn, blockpos1)) {
                return false;
            }

            ++i;
        }

        if (worldIn.getBlockState(blockpos2)
            .getBlock() == this) {
            if (this.isDoubleChest(worldIn, blockpos2)) {
                return false;
            }

            ++i;
        }

        if (worldIn.getBlockState(blockpos3)
            .getBlock() == this) {
            if (this.isDoubleChest(worldIn, blockpos3)) {
                return false;
            }

            ++i;
        }

        return i <= 1;
    }

    private boolean isDoubleChest(World worldIn, BlockPos pos) {
        if (worldIn.getBlockState(pos)
            .getBlock() != this) {
            return false;
        } else {
            for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
                if (worldIn.getBlockState(pos.offset(enumfacing))
                    .getBlock() == this) {
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
     * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
     * block, etc.
     */
    public void neighborChanged(IWrapperBlockState state, World worldIn, BlockPos pos, Block blockIn,
        BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity instanceof TileEntityChest) {
            tileentity.updateContainingBlockInfo();
        }
    }

    /**
     * Called serverside after this block is replaced with another in Chunk, but before the Tile Entity is updated
     */
    public void breakBlock(World worldIn, BlockPos pos, IWrapperBlockState state) {
        TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity instanceof IInventory) {
            InventoryHelper.dropInventoryItems(worldIn, pos, (IInventory) tileentity);
            worldIn.updateComparatorOutputLevel(pos, this);
        }

        super.breakBlock(worldIn, pos, state);
    }

    /**
     * Called when the block is right clicked by a player.
     */
    public boolean onBlockActivated(World worldIn, BlockPos pos, IWrapperBlockState state, EntityPlayer playerIn,
        EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) {
            return true;
        } else {
            ILockableContainer ilockablecontainer = this.getLockableContainer(worldIn, pos);

            if (ilockablecontainer != null) {
                playerIn.displayGUIChest(ilockablecontainer);

                if (this.chestType == BlockChest.Type.BASIC) {
                    playerIn.addStat(StatList.CHEST_OPENED);
                } else if (this.chestType == BlockChest.Type.TRAP) {
                    playerIn.addStat(StatList.TRAPPED_CHEST_TRIGGERED);
                }
            }

            return true;
        }
    }

    /**
     * Gets the chest inventory at the given location, returning null if the chest is blocked or there is no chest at
     * that location. Handles large chests.
     *
     * @param worldIn The world
     * @param pos     The position to check
     */
    @Nullable
    public ILockableContainer getLockableContainer(World worldIn, BlockPos pos) {
        return this.getContainer(worldIn, pos, false);
    }

    /**
     * Gets the chest inventory at the given location, returning null if there is no chest at that location or
     * optionally if the chest is blocked. Handles large chests.
     *
     * @param worldIn       The world
     * @param pos           The position to check
     * @param allowBlocking If false, then if the chest is blocked then <code>null</code> will be returned. If true,
     *                      ignores blocking for the chest at the given position (but, due to
     *                      <a href="https://bugs.mojang.com/browse/MC-
     *                      99321">a bug</a>, still checks if the neighbor is blocked).
     */
    @Nullable
    public ILockableContainer getContainer(World worldIn, BlockPos pos, boolean allowBlocking) {
        TileEntity tileentity = worldIn.getTileEntity(pos);

        if (!(tileentity instanceof TileEntityChest)) {
            return null;
        } else {
            ILockableContainer ilockablecontainer = (TileEntityChest) tileentity;

            if (!allowBlocking && this.isBlocked(worldIn, pos)) {
                return null;
            } else {
                for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
                    BlockPos blockpos = pos.offset(enumfacing);
                    Block block = worldIn.getBlockState(blockpos)
                        .getBlock();

                    if (block == this) {
                        if (!allowBlocking && this.isBlocked(worldIn, blockpos)) // Forge: fix MC-99321
                        {
                            return null;
                        }

                        TileEntity tileentity1 = worldIn.getTileEntity(blockpos);

                        if (tileentity1 instanceof TileEntityChest) {
                            if (enumfacing != EnumFacing.WEST && enumfacing != EnumFacing.NORTH) {
                                ilockablecontainer = new InventoryLargeChest(
                                    "container.chestDouble",
                                    ilockablecontainer,
                                    (TileEntityChest) tileentity1);
                            } else {
                                ilockablecontainer = new InventoryLargeChest(
                                    "container.chestDouble",
                                    (TileEntityChest) tileentity1,
                                    ilockablecontainer);
                            }
                        }
                    }
                }

                return ilockablecontainer;
            }
        }
    }

    /**
     * Returns a new instance of a block's tile entity class. Called on placing the block.
     */
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityChest();
    }

    /**
     * Can this block provide power. Only wire currently seems to have this change based on its state.
     */
    public boolean canProvidePower(IWrapperBlockState state) {
        return this.chestType == BlockChest.Type.TRAP;
    }

    public int getWeakPower(IWrapperBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        if (!blockState.canProvidePower()) {
            return 0;
        } else {
            int i = 0;
            TileEntity tileentity = blockAccess.getTileEntity(pos);

            if (tileentity instanceof TileEntityChest) {
                i = ((TileEntityChest) tileentity).numPlayersUsing;
            }

            return MathHelper.clamp(i, 0, 15);
        }
    }

    public int getStrongPower(IWrapperBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return side == EnumFacing.UP ? blockState.getWeakPower(blockAccess, pos, side) : 0;
    }

    private boolean isBlocked(World worldIn, BlockPos pos) {
        return this.isBelowSolidBlock(worldIn, pos) || this.isOcelotSittingOnChest(worldIn, pos);
    }

    private boolean isBelowSolidBlock(World worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.up())
            .doesSideBlockChestOpening(worldIn, pos.up(), EnumFacing.DOWN);
    }

    private boolean isOcelotSittingOnChest(World worldIn, BlockPos pos) {
        for (Entity entity : worldIn.getEntitiesWithinAABB(
            EntityOcelot.class,
            new AxisAlignedBB(
                (double) pos.getX(),
                (double) (pos.getY() + 1),
                (double) pos.getZ(),
                (double) (pos.getX() + 1),
                (double) (pos.getY() + 2),
                (double) (pos.getZ() + 1)))) {
            EntityOcelot entityocelot = (EntityOcelot) entity;

            if (entityocelot.isSitting()) {
                return true;
            }
        }

        return false;
    }

    public boolean hasComparatorInputOverride(IWrapperBlockState state) {
        return true;
    }

    public int getComparatorInputOverride(IWrapperBlockState blockState, World worldIn, BlockPos pos) {
        return Container.calcRedstoneFromInventory(this.getLockableContainer(worldIn, pos));
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

    public static enum Type {
        BASIC,
        TRAP;
    }

    /* ======================================== FORGE START ===================================== */
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
        return !isDoubleChest(world, pos) && super.rotateBlock(world, pos, axis);
    }
}
