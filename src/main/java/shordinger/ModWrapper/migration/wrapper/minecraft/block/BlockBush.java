package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.AxisAlignedBB;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class BlockBush extends Block implements net.minecraftforge.common.IPlantable {

    protected static final AxisAlignedBB BUSH_AABB = new AxisAlignedBB(
        0.30000001192092896D,
        0.0D,
        0.30000001192092896D,
        0.699999988079071D,
        0.6000000238418579D,
        0.699999988079071D);

    protected BlockBush() {
        this(Material.PLANTS);
    }

    protected BlockBush(Material materialIn) {
        this(materialIn, materialIn.getMaterialMapColor());
    }

    protected BlockBush(Material materialIn, MapColor mapColorIn) {
        super(materialIn, mapColorIn);
        this.setTickRandomly(true);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }

    /**
     * Checks if this block can be placed exactly at the given position.
     */
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        IWrapperBlockState soil = worldIn.getBlockState(pos.down());
        return super.canPlaceBlockAt(worldIn, pos) && soil.getBlock()
            .canSustainPlant(soil, worldIn, pos.down(), net.minecraft.util.EnumFacing.UP, this);
    }

    /**
     * Return true if the block can sustain a Bush
     */
    protected boolean canSustainBush(IWrapperBlockState state) {
        return state.getBlock() == Blocks.GRASS || state.getBlock() == Blocks.DIRT
            || state.getBlock() == Blocks.FARMLAND;
    }

    /**
     * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
     * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
     * block, etc.
     */
    public void neighborChanged(IWrapperBlockState state, World worldIn, BlockPos pos, Block blockIn,
        BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        this.checkAndDropBlock(worldIn, pos, state);
    }

    public void updateTick(World worldIn, BlockPos pos, IWrapperBlockState state, Random rand) {
        this.checkAndDropBlock(worldIn, pos, state);
    }

    protected void checkAndDropBlock(World worldIn, BlockPos pos, IWrapperBlockState state) {
        if (!this.canBlockStay(worldIn, pos, state)) {
            this.dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
        }
    }

    public boolean canBlockStay(World worldIn, BlockPos pos, IWrapperBlockState state) {
        if (state.getBlock() == this) // Forge: This function is called during world gen and placement, before this
                                      // block is set, so if we are not 'here' then assume it's the pre-check.
        {
            IWrapperBlockState soil = worldIn.getBlockState(pos.down());
            return soil.getBlock()
                .canSustainPlant(soil, worldIn, pos.down(), net.minecraft.util.EnumFacing.UP, this);
        }
        return this.canSustainBush(worldIn.getBlockState(pos.down()));
    }

    public AxisAlignedBB getBoundingBox(IWrapperBlockState state, IBlockAccess source, BlockPos pos) {
        return BUSH_AABB;
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

    @Override
    public net.minecraftforge.common.EnumPlantType getPlantType(net.minecraft.world.IBlockAccess world, BlockPos pos) {
        if (this == Blocks.WHEAT) return net.minecraftforge.common.EnumPlantType.Crop;
        if (this == Blocks.CARROTS) return net.minecraftforge.common.EnumPlantType.Crop;
        if (this == Blocks.POTATOES) return net.minecraftforge.common.EnumPlantType.Crop;
        if (this == Blocks.BEETROOTS) return net.minecraftforge.common.EnumPlantType.Crop;
        if (this == Blocks.MELON_STEM) return net.minecraftforge.common.EnumPlantType.Crop;
        if (this == Blocks.PUMPKIN_STEM) return net.minecraftforge.common.EnumPlantType.Crop;
        if (this == Blocks.DEADBUSH) return net.minecraftforge.common.EnumPlantType.Desert;
        if (this == Blocks.WATERLILY) return net.minecraftforge.common.EnumPlantType.Water;
        if (this == Blocks.RED_MUSHROOM) return net.minecraftforge.common.EnumPlantType.Cave;
        if (this == Blocks.BROWN_MUSHROOM) return net.minecraftforge.common.EnumPlantType.Cave;
        if (this == Blocks.NETHER_WART) return net.minecraftforge.common.EnumPlantType.Nether;
        if (this == Blocks.SAPLING) return net.minecraftforge.common.EnumPlantType.Plains;
        if (this == Blocks.TALLGRASS) return net.minecraftforge.common.EnumPlantType.Plains;
        if (this == Blocks.DOUBLE_PLANT) return net.minecraftforge.common.EnumPlantType.Plains;
        if (this == Blocks.RED_FLOWER) return net.minecraftforge.common.EnumPlantType.Plains;
        if (this == Blocks.YELLOW_FLOWER) return net.minecraftforge.common.EnumPlantType.Plains;
        return net.minecraftforge.common.EnumPlantType.Plains;
    }

    @Override
    public IWrapperBlockState getPlant(net.minecraft.world.IBlockAccess world, BlockPos pos) {
        IWrapperBlockState state = world.getBlockState(pos);
        if (state.getBlock() != this) return getDefaultState();
        return state;
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
