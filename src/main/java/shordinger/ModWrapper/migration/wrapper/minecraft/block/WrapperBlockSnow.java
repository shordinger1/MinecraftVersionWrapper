package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.AxisAlignedBB;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WrapperBlockSnow extends WrapperBlock {

    public static final PropertyInteger LAYERS = PropertyInteger.create("layers", 1, 8);
    protected static final AxisAlignedBB[] SNOW_AABB = new AxisAlignedBB[] {
        new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.0D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D),
        new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.25D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.375D, 1.0D),
        new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.625D, 1.0D),
        new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.75D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.875D, 1.0D),
        new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D) };

    protected WrapperBlockSnow() {
        super(Material.SNOW);
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(LAYERS, Integer.valueOf(1)));
        this.setTickRandomly(true);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }

    public AxisAlignedBB getBoundingBox(IWrapperBlockState state, IBlockAccess source, BlockPos pos) {
        return SNOW_AABB[((Integer) state.getValue(LAYERS)).intValue()];
    }

    /**
     * Determines if an entity can path through this block
     */
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return ((Integer) worldIn.getBlockState(pos)
            .getValue(LAYERS)).intValue() < 5;
    }

    /**
     * Determines if the block is solid enough on the top side to support other blocks, like redstone components.
     */
    public boolean isTopSolid(IWrapperBlockState state) {
        return ((Integer) state.getValue(LAYERS)).intValue() == 8;
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
        return face == EnumFacing.DOWN ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }

    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IWrapperBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        int i = ((Integer) blockState.getValue(LAYERS)).intValue() - 1;
        float f = 0.125F;
        AxisAlignedBB axisalignedbb = blockState.getBoundingBox(worldIn, pos);
        return new AxisAlignedBB(
            axisalignedbb.minX,
            axisalignedbb.minY,
            axisalignedbb.minZ,
            axisalignedbb.maxX,
            (double) ((float) i * 0.125F),
            axisalignedbb.maxZ);
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
        IWrapperBlockState iblockstate = worldIn.getBlockState(pos.down());
        WrapperBlock wrapperBlock = iblockstate.getBlock();

        if (wrapperBlock != Blocks.ICE && wrapperBlock != Blocks.PACKED_ICE && wrapperBlock != Blocks.BARRIER) {
            BlockFaceShape blockfaceshape = iblockstate.getBlockFaceShape(worldIn, pos.down(), EnumFacing.UP);
            return blockfaceshape == BlockFaceShape.SOLID || iblockstate.getBlock()
                .isLeaves(iblockstate, worldIn, pos.down())
                || wrapperBlock == this && ((Integer) iblockstate.getValue(LAYERS)).intValue() == 8;
        } else {
            return false;
        }
    }

    /**
     * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
     * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
     * block, etc.
     */
    public void neighborChanged(IWrapperBlockState state, World worldIn, BlockPos pos, WrapperBlock wrapperBlockIn,
        BlockPos fromPos) {
        this.checkAndDropBlock(worldIn, pos, state);
    }

    private boolean checkAndDropBlock(World worldIn, BlockPos pos, IWrapperBlockState state) {
        if (!this.canPlaceBlockAt(worldIn, pos)) {
            worldIn.setBlockToAir(pos);
            return false;
        } else {
            return true;
        }
    }

    /**
     * Spawns the block's drops in the world. By the time this is called the Block has possibly been set to air via
     * Block.removedByPlayer
     */
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IWrapperBlockState state,
        @Nullable TileEntity te, ItemStack stack) {
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        worldIn.setBlockToAir(pos);
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    public Item getItemDropped(IWrapperBlockState state, Random rand, int fortune) {
        return Items.SNOWBALL;
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    public int quantityDropped(Random random) {
        return 1;
    }

    public void updateTick(World worldIn, BlockPos pos, IWrapperBlockState state, Random rand) {
        if (worldIn.getLightFor(EnumSkyBlock.BLOCK, pos) > 11) {
            worldIn.setBlockToAir(pos);
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IWrapperBlockState blockState, IBlockAccess blockAccess, BlockPos pos,
        EnumFacing side) {
        if (side == EnumFacing.UP) {
            return true;
        } else {
            IWrapperBlockState iblockstate = blockAccess.getBlockState(pos.offset(side));
            return iblockstate.getBlock() == this && ((Integer) iblockstate.getValue(LAYERS)).intValue()
                >= ((Integer) blockState.getValue(LAYERS)).intValue() ? false
                    : super.shouldSideBeRendered(blockState, blockAccess, pos, side);
        }
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IWrapperBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
            .withProperty(LAYERS, Integer.valueOf((meta & 7) + 1));
    }

    /**
     * Whether this Block can be replaced directly by other blocks (true for e.g. tall grass)
     */
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return ((Integer) worldIn.getBlockState(pos)
            .getValue(LAYERS)).intValue() == 1;
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        return ((Integer) state.getValue(LAYERS)).intValue() - 1;
    }

    @Override
    public int quantityDropped(IWrapperBlockState state, int fortune, Random random) {
        return ((Integer) state.getValue(LAYERS)) + 1;
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { LAYERS });
    }
}
