package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import java.util.Random;

import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.AxisAlignedBB;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WrapperBlockDoor extends WrapperBlock {

    public static final PropertyDirection FACING = WrapperBlockHorizontal.FACING;
    public static final PropertyBool OPEN = PropertyBool.create("open");
    public static final PropertyEnum<WrapperBlockDoor.EnumHingePosition> HINGE = PropertyEnum.<WrapperBlockDoor.EnumHingePosition>create(
        "hinge",
        WrapperBlockDoor.EnumHingePosition.class);
    public static final PropertyBool POWERED = PropertyBool.create("powered");
    public static final PropertyEnum<WrapperBlockDoor.EnumDoorHalf> HALF = PropertyEnum.<WrapperBlockDoor.EnumDoorHalf>create(
        "half",
        WrapperBlockDoor.EnumDoorHalf.class);
    protected static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.1875D);
    protected static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.8125D, 1.0D, 1.0D, 1.0D);
    protected static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0.8125D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    protected static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.1875D, 1.0D, 1.0D);

    protected WrapperBlockDoor(Material materialIn) {
        super(materialIn);
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(FACING, EnumFacing.NORTH)
                .withProperty(OPEN, Boolean.valueOf(false))
                .withProperty(HINGE, WrapperBlockDoor.EnumHingePosition.LEFT)
                .withProperty(POWERED, Boolean.valueOf(false))
                .withProperty(HALF, WrapperBlockDoor.EnumDoorHalf.LOWER));
    }

    public AxisAlignedBB getBoundingBox(IWrapperBlockState state, IBlockAccess source, BlockPos pos) {
        state = state.getActualState(source, pos);
        EnumFacing enumfacing = (EnumFacing) state.getValue(FACING);
        boolean flag = !((Boolean) state.getValue(OPEN)).booleanValue();
        boolean flag1 = state.getValue(HINGE) == WrapperBlockDoor.EnumHingePosition.RIGHT;

        switch (enumfacing) {
            case EAST:
            default:
                return flag ? EAST_AABB : (flag1 ? NORTH_AABB : SOUTH_AABB);
            case SOUTH:
                return flag ? SOUTH_AABB : (flag1 ? EAST_AABB : WEST_AABB);
            case WEST:
                return flag ? WEST_AABB : (flag1 ? SOUTH_AABB : NORTH_AABB);
            case NORTH:
                return flag ? NORTH_AABB : (flag1 ? WEST_AABB : EAST_AABB);
        }
    }

    /**
     * Gets the localized name of this block. Used for the statistics page.
     */
    public String getLocalizedName() {
        return I18n.translateToLocal((this.getUnlocalizedName() + ".name").replaceAll("tile", "item"));
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
    public boolean isOpaqueCube(IWrapperBlockState state) {
        return false;
    }

    /**
     * Determines if an entity can path through this block
     */
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return isOpen(combineMetadata(worldIn, pos));
    }

    public boolean isFullCube(IWrapperBlockState state) {
        return false;
    }

    private int getCloseSound() {
        return this.blockWrapperMaterial == Material.IRON ? 1011 : 1012;
    }

    private int getOpenSound() {
        return this.blockWrapperMaterial == Material.IRON ? 1005 : 1006;
    }

    /**
     * Get the MapColor for this Block and the given BlockState
     */
    public MapColor getMapColor(IWrapperBlockState state, IBlockAccess worldIn, BlockPos pos) {
        if (state.getBlock() == Blocks.IRON_DOOR) {
            return MapColor.IRON;
        } else if (state.getBlock() == Blocks.OAK_DOOR) {
            return WrapperBlockPlanks.EnumType.OAK.getMapColor();
        } else if (state.getBlock() == Blocks.SPRUCE_DOOR) {
            return WrapperBlockPlanks.EnumType.SPRUCE.getMapColor();
        } else if (state.getBlock() == Blocks.BIRCH_DOOR) {
            return WrapperBlockPlanks.EnumType.BIRCH.getMapColor();
        } else if (state.getBlock() == Blocks.JUNGLE_DOOR) {
            return WrapperBlockPlanks.EnumType.JUNGLE.getMapColor();
        } else if (state.getBlock() == Blocks.ACACIA_DOOR) {
            return WrapperBlockPlanks.EnumType.ACACIA.getMapColor();
        } else {
            return state.getBlock() == Blocks.DARK_OAK_DOOR ? WrapperBlockPlanks.EnumType.DARK_OAK.getMapColor()
                : super.getMapColor(state, worldIn, pos);
        }
    }

    /**
     * Called when the block is right clicked by a player.
     */
    public boolean onBlockActivated(World worldIn, BlockPos pos, IWrapperBlockState state, EntityPlayer playerIn,
        EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (this.blockWrapperMaterial == Material.IRON) {
            return false;
        } else {
            BlockPos blockpos = state.getValue(HALF) == WrapperBlockDoor.EnumDoorHalf.LOWER ? pos : pos.down();
            IWrapperBlockState iblockstate = pos.equals(blockpos) ? state : worldIn.getBlockState(blockpos);

            if (iblockstate.getBlock() != this) {
                return false;
            } else {
                state = iblockstate.cycleProperty(OPEN);
                worldIn.setBlockState(blockpos, state, 10);
                worldIn.markBlockRangeForRenderUpdate(blockpos, pos);
                worldIn.playEvent(
                    playerIn,
                    ((Boolean) state.getValue(OPEN)).booleanValue() ? this.getOpenSound() : this.getCloseSound(),
                    pos,
                    0);
                return true;
            }
        }
    }

    public void toggleDoor(World worldIn, BlockPos pos, boolean open) {
        IWrapperBlockState iblockstate = worldIn.getBlockState(pos);

        if (iblockstate.getBlock() == this) {
            BlockPos blockpos = iblockstate.getValue(HALF) == WrapperBlockDoor.EnumDoorHalf.LOWER ? pos : pos.down();
            IWrapperBlockState iblockstate1 = pos == blockpos ? iblockstate : worldIn.getBlockState(blockpos);

            if (iblockstate1.getBlock() == this && ((Boolean) iblockstate1.getValue(OPEN)).booleanValue() != open) {
                worldIn.setBlockState(blockpos, iblockstate1.withProperty(OPEN, Boolean.valueOf(open)), 10);
                worldIn.markBlockRangeForRenderUpdate(blockpos, pos);
                worldIn.playEvent((EntityPlayer) null, open ? this.getOpenSound() : this.getCloseSound(), pos, 0);
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
        if (state.getValue(HALF) == WrapperBlockDoor.EnumDoorHalf.UPPER) {
            BlockPos blockpos = pos.down();
            IWrapperBlockState iblockstate = worldIn.getBlockState(blockpos);

            if (iblockstate.getBlock() != this) {
                worldIn.setBlockToAir(pos);
            } else if (wrapperBlockIn != this) {
                iblockstate.neighborChanged(worldIn, blockpos, wrapperBlockIn, fromPos);
            }
        } else {
            boolean flag1 = false;
            BlockPos blockpos1 = pos.up();
            IWrapperBlockState iblockstate1 = worldIn.getBlockState(blockpos1);

            if (iblockstate1.getBlock() != this) {
                worldIn.setBlockToAir(pos);
                flag1 = true;
            }

            if (!worldIn.getBlockState(pos.down())
                .isSideSolid(worldIn, pos.down(), EnumFacing.UP)) {
                worldIn.setBlockToAir(pos);
                flag1 = true;

                if (iblockstate1.getBlock() == this) {
                    worldIn.setBlockToAir(blockpos1);
                }
            }

            if (flag1) {
                if (!worldIn.isRemote) {
                    this.dropBlockAsItem(worldIn, pos, state, 0);
                }
            } else {
                boolean flag = worldIn.isBlockPowered(pos) || worldIn.isBlockPowered(blockpos1);

                if (wrapperBlockIn != this && (flag || wrapperBlockIn.getDefaultState()
                    .canProvidePower()) && flag != ((Boolean) iblockstate1.getValue(POWERED)).booleanValue()) {
                    worldIn.setBlockState(blockpos1, iblockstate1.withProperty(POWERED, Boolean.valueOf(flag)), 2);

                    if (flag != ((Boolean) state.getValue(OPEN)).booleanValue()) {
                        worldIn.setBlockState(pos, state.withProperty(OPEN, Boolean.valueOf(flag)), 2);
                        worldIn.markBlockRangeForRenderUpdate(pos, pos);
                        worldIn
                            .playEvent((EntityPlayer) null, flag ? this.getOpenSound() : this.getCloseSound(), pos, 0);
                    }
                }
            }
        }
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    public Item getItemDropped(IWrapperBlockState state, Random rand, int fortune) {
        return state.getValue(HALF) == WrapperBlockDoor.EnumDoorHalf.UPPER ? Items.AIR : this.getItem();
    }

    /**
     * Checks if this block can be placed exactly at the given position.
     */
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        if (pos.getY() >= worldIn.getHeight() - 1) {
            return false;
        } else {
            IWrapperBlockState state = worldIn.getBlockState(pos.down());
            return (state.isTopSolid()
                || state.getBlockFaceShape(worldIn, pos.down(), EnumFacing.UP) == BlockFaceShape.SOLID)
                && super.canPlaceBlockAt(worldIn, pos)
                && super.canPlaceBlockAt(worldIn, pos.up());
        }
    }

    public EnumPushReaction getMobilityFlag(IWrapperBlockState state) {
        return EnumPushReaction.DESTROY;
    }

    public static int combineMetadata(IBlockAccess worldIn, BlockPos pos) {
        IWrapperBlockState iblockstate = worldIn.getBlockState(pos);
        int i = iblockstate.getBlock()
            .getMetaFromState(iblockstate);
        boolean flag = isTop(i);
        IWrapperBlockState iblockstate1 = worldIn.getBlockState(pos.down());
        int j = iblockstate1.getBlock()
            .getMetaFromState(iblockstate1);
        int k = flag ? j : i;
        IWrapperBlockState iblockstate2 = worldIn.getBlockState(pos.up());
        int l = iblockstate2.getBlock()
            .getMetaFromState(iblockstate2);
        int i1 = flag ? i : l;
        boolean flag1 = (i1 & 1) != 0;
        boolean flag2 = (i1 & 2) != 0;
        return removeHalfBit(k) | (flag ? 8 : 0) | (flag1 ? 16 : 0) | (flag2 ? 32 : 0);
    }

    public ItemStack getItem(World worldIn, BlockPos pos, IWrapperBlockState state) {
        return new ItemStack(this.getItem());
    }

    private Item getItem() {
        if (this == Blocks.IRON_DOOR) {
            return Items.IRON_DOOR;
        } else if (this == Blocks.SPRUCE_DOOR) {
            return Items.SPRUCE_DOOR;
        } else if (this == Blocks.BIRCH_DOOR) {
            return Items.BIRCH_DOOR;
        } else if (this == Blocks.JUNGLE_DOOR) {
            return Items.JUNGLE_DOOR;
        } else if (this == Blocks.ACACIA_DOOR) {
            return Items.ACACIA_DOOR;
        } else {
            return this == Blocks.DARK_OAK_DOOR ? Items.DARK_OAK_DOOR : Items.OAK_DOOR;
        }
    }

    /**
     * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually
     * collect this block
     */
    public void onBlockHarvested(World worldIn, BlockPos pos, IWrapperBlockState state, EntityPlayer player) {
        BlockPos blockpos = pos.down();
        BlockPos blockpos1 = pos.up();

        if (player.capabilities.isCreativeMode && state.getValue(HALF) == WrapperBlockDoor.EnumDoorHalf.UPPER
            && worldIn.getBlockState(blockpos)
                .getBlock() == this) {
            worldIn.setBlockToAir(blockpos);
        }

        if (state.getValue(HALF) == WrapperBlockDoor.EnumDoorHalf.LOWER && worldIn.getBlockState(blockpos1)
            .getBlock() == this) {
            if (player.capabilities.isCreativeMode) {
                worldIn.setBlockToAir(pos);
            }

            worldIn.setBlockToAir(blockpos1);
        }
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
     * Get the actual Block state of this Block at the given position. This applies properties not visible in the
     * metadata, such as fence connections.
     */
    public IWrapperBlockState getActualState(IWrapperBlockState state, IBlockAccess worldIn, BlockPos pos) {
        if (state.getValue(HALF) == WrapperBlockDoor.EnumDoorHalf.LOWER) {
            IWrapperBlockState iblockstate = worldIn.getBlockState(pos.up());

            if (iblockstate.getBlock() == this) {
                state = state.withProperty(HINGE, iblockstate.getValue(HINGE))
                    .withProperty(POWERED, iblockstate.getValue(POWERED));
            }
        } else {
            IWrapperBlockState iblockstate1 = worldIn.getBlockState(pos.down());

            if (iblockstate1.getBlock() == this) {
                state = state.withProperty(FACING, iblockstate1.getValue(FACING))
                    .withProperty(OPEN, iblockstate1.getValue(OPEN));
            }
        }

        return state;
    }

    /**
     * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     */
    public IWrapperBlockState withRotation(IWrapperBlockState state, Rotation rot) {
        return state.getValue(HALF) != WrapperBlockDoor.EnumDoorHalf.LOWER ? state
            : state.withProperty(FACING, rot.rotate((EnumFacing) state.getValue(FACING)));
    }

    /**
     * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     */
    public IWrapperBlockState withMirror(IWrapperBlockState state, Mirror mirrorIn) {
        return mirrorIn == Mirror.NONE ? state
            : state.withRotation(mirrorIn.toRotation((EnumFacing) state.getValue(FACING)))
                .cycleProperty(HINGE);
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IWrapperBlockState getStateFromMeta(int meta) {
        return (meta & 8) > 0 ? this.getDefaultState()
            .withProperty(HALF, WrapperBlockDoor.EnumDoorHalf.UPPER)
            .withProperty(
                HINGE,
                (meta & 1) > 0 ? WrapperBlockDoor.EnumHingePosition.RIGHT : WrapperBlockDoor.EnumHingePosition.LEFT)
            .withProperty(POWERED, Boolean.valueOf((meta & 2) > 0))
            : this.getDefaultState()
                .withProperty(HALF, WrapperBlockDoor.EnumDoorHalf.LOWER)
                .withProperty(
                    FACING,
                    EnumFacing.getHorizontal(meta & 3)
                        .rotateYCCW())
                .withProperty(OPEN, Boolean.valueOf((meta & 4) > 0));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        int i = 0;

        if (state.getValue(HALF) == WrapperBlockDoor.EnumDoorHalf.UPPER) {
            i = i | 8;

            if (state.getValue(HINGE) == WrapperBlockDoor.EnumHingePosition.RIGHT) {
                i |= 1;
            }

            if (((Boolean) state.getValue(POWERED)).booleanValue()) {
                i |= 2;
            }
        } else {
            i = i | ((EnumFacing) state.getValue(FACING)).rotateY()
                .getHorizontalIndex();

            if (((Boolean) state.getValue(OPEN)).booleanValue()) {
                i |= 4;
            }
        }

        return i;
    }

    protected static int removeHalfBit(int meta) {
        return meta & 7;
    }

    public static boolean isOpen(IBlockAccess worldIn, BlockPos pos) {
        return isOpen(combineMetadata(worldIn, pos));
    }

    public static EnumFacing getFacing(IBlockAccess worldIn, BlockPos pos) {
        return getFacing(combineMetadata(worldIn, pos));
    }

    public static EnumFacing getFacing(int combinedMeta) {
        return EnumFacing.getHorizontal(combinedMeta & 3)
            .rotateYCCW();
    }

    protected static boolean isOpen(int combinedMeta) {
        return (combinedMeta & 4) != 0;
    }

    protected static boolean isTop(int meta) {
        return (meta & 8) != 0;
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { HALF, FACING, OPEN, HINGE, POWERED });
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

    public static enum EnumDoorHalf implements IStringSerializable {

        UPPER,
        LOWER;

        public String toString() {
            return this.getName();
        }

        public String getName() {
            return this == UPPER ? "upper" : "lower";
        }
    }

    public static enum EnumHingePosition implements IStringSerializable {

        LEFT,
        RIGHT;

        public String toString() {
            return this.getName();
        }

        public String getName() {
            return this == LEFT ? "left" : "right";
        }
    }
}
