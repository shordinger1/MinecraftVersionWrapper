package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.AxisAlignedBB;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class BlockBanner extends BlockContainer {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final PropertyInteger ROTATION = PropertyInteger.create("rotation", 0, 15);
    protected static final AxisAlignedBB STANDING_AABB = new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 1.0D, 0.75D);

    protected BlockBanner() {
        super(Material.WOOD);
    }

    /**
     * Gets the localized name of this block. Used for the statistics page.
     */
    public String getLocalizedName() {
        return I18n.translateToLocal("item.banner.white.name");
    }

    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IWrapperBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    public boolean isFullCube(IWrapperBlockState state) {
        return false;
    }

    /**
     * Determines if an entity can path through this block
     */
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
    public boolean isOpaqueCube(IWrapperBlockState state) {
        return false;
    }

    /**
     * Return true if an entity can be spawned inside the block (used to get the player's bed spawn location)
     */
    public boolean canSpawnInBlock() {
        return true;
    }

    /**
     * Returns a new instance of a block's tile entity class. Called on placing the block.
     */
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityBanner();
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    public Item getItemDropped(IWrapperBlockState state, Random rand, int fortune) {
        return Items.BANNER;
    }

    private ItemStack getTileDataItemStack(World worldIn, BlockPos pos) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity instanceof TileEntityBanner ? ((TileEntityBanner) tileentity).getItem() : ItemStack.EMPTY;
    }

    public ItemStack getItem(World worldIn, BlockPos pos, IWrapperBlockState state) {
        ItemStack itemstack = this.getTileDataItemStack(worldIn, pos);
        return itemstack.isEmpty() ? new ItemStack(Items.BANNER) : itemstack;
    }

    /**
     * Spawns this Block's drops into the World as EntityItems.
     */
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IWrapperBlockState state, float chance,
        int fortune) {
        {
            super.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune);
        }
    }

    /**
     * Checks if this block can be placed exactly at the given position.
     */
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return !this.hasInvalidNeighbor(worldIn, pos) && super.canPlaceBlockAt(worldIn, pos);
    }

    /**
     * Spawns the block's drops in the world. By the time this is called the Block has possibly been set to air via
     * Block.removedByPlayer
     */
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IWrapperBlockState state,
        @Nullable TileEntity te, ItemStack stack) {
        if (te instanceof TileEntityBanner) {
            TileEntityBanner tileentitybanner = (TileEntityBanner) te;
            ItemStack itemstack = tileentitybanner.getItem();
            spawnAsEntity(worldIn, pos, itemstack);
        } else {
            super.harvestBlock(worldIn, player, pos, state, (TileEntity) null, stack);
        }
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

    @Override
    public void getDrops(net.minecraft.util.NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos,
        IWrapperBlockState state, int fortune) {
        TileEntity te = world.getTileEntity(pos);

        if (te instanceof TileEntityBanner) {
            TileEntityBanner tileentitybanner = (TileEntityBanner) te;
            ItemStack itemstack = tileentitybanner.getItem();
            drops.add(itemstack);
        } else {
            drops.add(new ItemStack(Items.BANNER, 1, 0));
        }
    }

    public static class BlockBannerHanging extends BlockBanner {

        protected static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.875D, 1.0D, 0.78125D, 1.0D);
        protected static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.78125D, 0.125D);
        protected static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0.875D, 0.0D, 0.0D, 1.0D, 0.78125D, 1.0D);
        protected static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.125D, 0.78125D, 1.0D);

        public BlockBannerHanging() {
            this.setDefaultState(
                this.blockState.getBaseState()
                    .withProperty(FACING, EnumFacing.NORTH));
        }

        /**
         * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the
         * passed blockstate.
         */
        public IWrapperBlockState withRotation(IWrapperBlockState state, Rotation rot) {
            return state.withProperty(FACING, rot.rotate((EnumFacing) state.getValue(FACING)));
        }

        /**
         * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the
         * passed blockstate.
         */
        public IWrapperBlockState withMirror(IWrapperBlockState state, Mirror mirrorIn) {
            return state.withRotation(mirrorIn.toRotation((EnumFacing) state.getValue(FACING)));
        }

        public AxisAlignedBB getBoundingBox(IWrapperBlockState state, IBlockAccess source, BlockPos pos) {
            switch ((EnumFacing) state.getValue(FACING)) {
                case NORTH:
                default:
                    return NORTH_AABB;
                case SOUTH:
                    return SOUTH_AABB;
                case WEST:
                    return WEST_AABB;
                case EAST:
                    return EAST_AABB;
            }
        }

        /**
         * Called when a neighboring block was changed and marks that this state should perform any checks during a
         * neighbor change. Cases may include when redstone power is updated, cactus blocks popping off due to a
         * neighboring solid block, etc.
         */
        public void neighborChanged(IWrapperBlockState state, World worldIn, BlockPos pos, Block blockIn,
            BlockPos fromPos) {
            EnumFacing enumfacing = (EnumFacing) state.getValue(FACING);

            if (!worldIn.getBlockState(pos.offset(enumfacing.getOpposite()))
                .getMaterial()
                .isSolid()) {
                this.dropBlockAsItem(worldIn, pos, state, 0);
                worldIn.setBlockToAir(pos);
            }

            super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
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

        protected BlockStateContainer createBlockState() {
            return new BlockStateContainer(this, new IProperty[] { FACING });
        }
    }

    public static class BlockBannerStanding extends BlockBanner {

        public BlockBannerStanding() {
            this.setDefaultState(
                this.blockState.getBaseState()
                    .withProperty(ROTATION, Integer.valueOf(0)));
        }

        public AxisAlignedBB getBoundingBox(IWrapperBlockState state, IBlockAccess source, BlockPos pos) {
            return STANDING_AABB;
        }

        /**
         * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the
         * passed blockstate.
         */
        public IWrapperBlockState withRotation(IWrapperBlockState state, Rotation rot) {
            return state.withProperty(
                ROTATION,
                Integer.valueOf(rot.rotate(((Integer) state.getValue(ROTATION)).intValue(), 16)));
        }

        /**
         * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the
         * passed blockstate.
         */
        public IWrapperBlockState withMirror(IWrapperBlockState state, Mirror mirrorIn) {
            return state.withProperty(
                ROTATION,
                Integer.valueOf(mirrorIn.mirrorRotation(((Integer) state.getValue(ROTATION)).intValue(), 16)));
        }

        /**
         * Called when a neighboring block was changed and marks that this state should perform any checks during a
         * neighbor change. Cases may include when redstone power is updated, cactus blocks popping off due to a
         * neighboring solid block, etc.
         */
        public void neighborChanged(IWrapperBlockState state, World worldIn, BlockPos pos, Block blockIn,
            BlockPos fromPos) {
            if (!worldIn.getBlockState(pos.down())
                .getMaterial()
                .isSolid()) {
                this.dropBlockAsItem(worldIn, pos, state, 0);
                worldIn.setBlockToAir(pos);
            }

            super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        }

        /**
         * Convert the given metadata into a BlockState for this Block
         */
        public IWrapperBlockState getStateFromMeta(int meta) {
            return this.getDefaultState()
                .withProperty(ROTATION, Integer.valueOf(meta));
        }

        /**
         * Convert the BlockState into the correct metadata value
         */
        public int getMetaFromState(IWrapperBlockState state) {
            return ((Integer) state.getValue(ROTATION)).intValue();
        }

        protected BlockStateContainer createBlockState() {
            return new BlockStateContainer(this, new IProperty[] { ROTATION });
        }
    }
}
