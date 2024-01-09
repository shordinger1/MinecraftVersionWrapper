package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WrapperBlockConcretePowder extends WrapperBlockFalling {

    public static final PropertyEnum<EnumDyeColor> COLOR = PropertyEnum
        .<EnumDyeColor>create("color", EnumDyeColor.class);

    public WrapperBlockConcretePowder() {
        super(Material.SAND);
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(COLOR, EnumDyeColor.WHITE));
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    public void onEndFalling(World worldIn, BlockPos pos, IWrapperBlockState p_176502_3_,
        IWrapperBlockState p_176502_4_) {
        if (p_176502_4_.getMaterial()
            .isLiquid()) {
            worldIn.setBlockState(
                pos,
                Blocks.CONCRETE.getDefaultState()
                    .withProperty(WrapperBlockColored.COLOR, p_176502_3_.getValue(COLOR)),
                3);
        }
    }

    protected boolean tryTouchWater(World worldIn, BlockPos pos, IWrapperBlockState state) {
        boolean flag = false;

        for (EnumFacing enumfacing : EnumFacing.values()) {
            if (enumfacing != EnumFacing.DOWN) {
                BlockPos blockpos = pos.offset(enumfacing);

                if (worldIn.getBlockState(blockpos)
                    .getMaterial() == Material.WATER) {
                    flag = true;
                    break;
                }
            }
        }

        if (flag) {
            worldIn.setBlockState(
                pos,
                Blocks.CONCRETE.getDefaultState()
                    .withProperty(WrapperBlockColored.COLOR, state.getValue(COLOR)),
                3);
        }

        return flag;
    }

    /**
     * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
     * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
     * block, etc.
     */
    public void neighborChanged(IWrapperBlockState state, World worldIn, BlockPos pos, WrapperBlock wrapperBlockIn,
        BlockPos fromPos) {
        if (!this.tryTouchWater(worldIn, pos, state)) {
            super.neighborChanged(state, worldIn, pos, wrapperBlockIn, fromPos);
        }
    }

    /**
     * Called after the block is set in the Chunk data, but before the Tile Entity is set
     */
    public void onBlockAdded(World worldIn, BlockPos pos, IWrapperBlockState state) {
        if (!this.tryTouchWater(worldIn, pos, state)) {
            super.onBlockAdded(worldIn, pos, state);
        }
    }

    /**
     * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
     * returns the metadata of the dropped item based on the old metadata of the block.
     */
    public int damageDropped(IWrapperBlockState state) {
        return ((EnumDyeColor) state.getValue(COLOR)).getMetadata();
    }

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (EnumDyeColor enumdyecolor : EnumDyeColor.values()) {
            items.add(new ItemStack(this, 1, enumdyecolor.getMetadata()));
        }
    }

    /**
     * Get the MapColor for this Block and the given BlockState
     */
    public MapColor getMapColor(IWrapperBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return MapColor.getBlockColor((EnumDyeColor) state.getValue(COLOR));
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IWrapperBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
            .withProperty(COLOR, EnumDyeColor.byMetadata(meta));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        return ((EnumDyeColor) state.getValue(COLOR)).getMetadata();
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { COLOR });
    }
}