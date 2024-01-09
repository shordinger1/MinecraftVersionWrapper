package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.AxisAlignedBB;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WrapperBlockChorusPlant extends WrapperBlock {

    public static final PropertyBool NORTH = PropertyBool.create("north");
    public static final PropertyBool EAST = PropertyBool.create("east");
    public static final PropertyBool SOUTH = PropertyBool.create("south");
    public static final PropertyBool WEST = PropertyBool.create("west");
    public static final PropertyBool UP = PropertyBool.create("up");
    public static final PropertyBool DOWN = PropertyBool.create("down");

    protected WrapperBlockChorusPlant() {
        super(Material.PLANTS, MapColor.PURPLE);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(NORTH, Boolean.valueOf(false))
                .withProperty(EAST, Boolean.valueOf(false))
                .withProperty(SOUTH, Boolean.valueOf(false))
                .withProperty(WEST, Boolean.valueOf(false))
                .withProperty(UP, Boolean.valueOf(false))
                .withProperty(DOWN, Boolean.valueOf(false)));
    }

    /**
     * Get the actual Block state of this Block at the given position. This applies properties not visible in the
     * metadata, such as fence connections.
     */
    public IWrapperBlockState getActualState(IWrapperBlockState state, IBlockAccess worldIn, BlockPos pos) {
        WrapperBlock wrapperBlock = worldIn.getBlockState(pos.down())
            .getBlock();
        WrapperBlock wrapperBlock1 = worldIn.getBlockState(pos.up())
            .getBlock();
        WrapperBlock wrapperBlock2 = worldIn.getBlockState(pos.north())
            .getBlock();
        WrapperBlock wrapperBlock3 = worldIn.getBlockState(pos.east())
            .getBlock();
        WrapperBlock wrapperBlock4 = worldIn.getBlockState(pos.south())
            .getBlock();
        WrapperBlock wrapperBlock5 = worldIn.getBlockState(pos.west())
            .getBlock();
        return state
            .withProperty(
                DOWN,
                Boolean.valueOf(
                    wrapperBlock == this || wrapperBlock == Blocks.CHORUS_FLOWER || wrapperBlock == Blocks.END_STONE))
            .withProperty(UP, Boolean.valueOf(wrapperBlock1 == this || wrapperBlock1 == Blocks.CHORUS_FLOWER))
            .withProperty(NORTH, Boolean.valueOf(wrapperBlock2 == this || wrapperBlock2 == Blocks.CHORUS_FLOWER))
            .withProperty(EAST, Boolean.valueOf(wrapperBlock3 == this || wrapperBlock3 == Blocks.CHORUS_FLOWER))
            .withProperty(SOUTH, Boolean.valueOf(wrapperBlock4 == this || wrapperBlock4 == Blocks.CHORUS_FLOWER))
            .withProperty(WEST, Boolean.valueOf(wrapperBlock5 == this || wrapperBlock5 == Blocks.CHORUS_FLOWER));
    }

    public AxisAlignedBB getBoundingBox(IWrapperBlockState state, IBlockAccess source, BlockPos pos) {
        state = state.getActualState(source, pos);
        float f = 0.1875F;
        float f1 = ((Boolean) state.getValue(WEST)).booleanValue() ? 0.0F : 0.1875F;
        float f2 = ((Boolean) state.getValue(DOWN)).booleanValue() ? 0.0F : 0.1875F;
        float f3 = ((Boolean) state.getValue(NORTH)).booleanValue() ? 0.0F : 0.1875F;
        float f4 = ((Boolean) state.getValue(EAST)).booleanValue() ? 1.0F : 0.8125F;
        float f5 = ((Boolean) state.getValue(UP)).booleanValue() ? 1.0F : 0.8125F;
        float f6 = ((Boolean) state.getValue(SOUTH)).booleanValue() ? 1.0F : 0.8125F;
        return new AxisAlignedBB((double) f1, (double) f2, (double) f3, (double) f4, (double) f5, (double) f6);
    }

    public void addCollisionBoxToList(IWrapperBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox,
        List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        if (!isActualState) {
            state = state.getActualState(worldIn, pos);
        }

        float f = 0.1875F;
        float f1 = 0.8125F;
        addCollisionBoxToList(
            pos,
            entityBox,
            collidingBoxes,
            new AxisAlignedBB(0.1875D, 0.1875D, 0.1875D, 0.8125D, 0.8125D, 0.8125D));

        if (((Boolean) state.getValue(WEST)).booleanValue()) {
            addCollisionBoxToList(
                pos,
                entityBox,
                collidingBoxes,
                new AxisAlignedBB(0.0D, 0.1875D, 0.1875D, 0.1875D, 0.8125D, 0.8125D));
        }

        if (((Boolean) state.getValue(EAST)).booleanValue()) {
            addCollisionBoxToList(
                pos,
                entityBox,
                collidingBoxes,
                new AxisAlignedBB(0.8125D, 0.1875D, 0.1875D, 1.0D, 0.8125D, 0.8125D));
        }

        if (((Boolean) state.getValue(UP)).booleanValue()) {
            addCollisionBoxToList(
                pos,
                entityBox,
                collidingBoxes,
                new AxisAlignedBB(0.1875D, 0.8125D, 0.1875D, 0.8125D, 1.0D, 0.8125D));
        }

        if (((Boolean) state.getValue(DOWN)).booleanValue()) {
            addCollisionBoxToList(
                pos,
                entityBox,
                collidingBoxes,
                new AxisAlignedBB(0.1875D, 0.0D, 0.1875D, 0.8125D, 0.1875D, 0.8125D));
        }

        if (((Boolean) state.getValue(NORTH)).booleanValue()) {
            addCollisionBoxToList(
                pos,
                entityBox,
                collidingBoxes,
                new AxisAlignedBB(0.1875D, 0.1875D, 0.0D, 0.8125D, 0.8125D, 0.1875D));
        }

        if (((Boolean) state.getValue(SOUTH)).booleanValue()) {
            addCollisionBoxToList(
                pos,
                entityBox,
                collidingBoxes,
                new AxisAlignedBB(0.1875D, 0.1875D, 0.8125D, 0.8125D, 0.8125D, 1.0D));
        }
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        return 0;
    }

    public void updateTick(World worldIn, BlockPos pos, IWrapperBlockState state, Random rand) {
        if (!this.canSurviveAt(worldIn, pos)) {
            worldIn.destroyBlock(pos, true);
        }
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    public Item getItemDropped(IWrapperBlockState state, Random rand, int fortune) {
        return Items.CHORUS_FRUIT;
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    public int quantityDropped(Random random) {
        return random.nextInt(2);
    }

    public boolean isFullCube(IWrapperBlockState state) {
        return false;
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
    public boolean isOpaqueCube(IWrapperBlockState state) {
        return false;
    }

    /**
     * Checks if this block can be placed exactly at the given position.
     */
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return super.canPlaceBlockAt(worldIn, pos) ? this.canSurviveAt(worldIn, pos) : false;
    }

    /**
     * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
     * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
     * block, etc.
     */
    public void neighborChanged(IWrapperBlockState state, World worldIn, BlockPos pos, WrapperBlock wrapperBlockIn,
        BlockPos fromPos) {
        if (!this.canSurviveAt(worldIn, pos)) {
            worldIn.scheduleUpdate(pos, this, 1);
        }
    }

    public boolean canSurviveAt(World wordIn, BlockPos pos) {
        boolean flag = wordIn.isAirBlock(pos.up());
        boolean flag1 = wordIn.isAirBlock(pos.down());

        for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
            BlockPos blockpos = pos.offset(enumfacing);
            WrapperBlock wrapperBlock = wordIn.getBlockState(blockpos)
                .getBlock();

            if (wrapperBlock == this) {
                if (!flag && !flag1) {
                    return false;
                }

                WrapperBlock wrapperBlock1 = wordIn.getBlockState(blockpos.down())
                    .getBlock();

                if (wrapperBlock1 == this || wrapperBlock1 == Blocks.END_STONE) {
                    return true;
                }
            }
        }

        WrapperBlock wrapperBlock2 = wordIn.getBlockState(pos.down())
            .getBlock();
        return wrapperBlock2 == this || wrapperBlock2 == Blocks.END_STONE;
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { NORTH, EAST, SOUTH, WEST, UP, DOWN });
    }

    /**
     * Determines if an entity can path through this block
     */
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }

    /**
     * Gets the render layer this block will render on. SOLID for solid blocks, CUTOUT or CUTOUT_MIPPED for on-off
     * transparency (glass, reeds), TRANSLUCENT for fully blended transparency (stained glass)
     */
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IWrapperBlockState blockState, IBlockAccess blockAccess, BlockPos pos,
        EnumFacing side) {
        WrapperBlock wrapperBlock = blockAccess.getBlockState(pos.offset(side))
            .getBlock();
        return wrapperBlock != this && wrapperBlock != Blocks.CHORUS_FLOWER
            && (side != EnumFacing.DOWN || wrapperBlock != Blocks.END_STONE);
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
