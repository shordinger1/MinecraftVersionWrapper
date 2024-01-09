package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.AxisAlignedBB;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.MathHelper;

public class WrapperBlockStem extends WrapperBlockBush implements IGrowable {

    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 7);
    public static final PropertyDirection FACING = WrapperBlockTorch.FACING;
    private final WrapperBlock crop;
    protected static final AxisAlignedBB[] STEM_AABB = new AxisAlignedBB[] {
        new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.125D, 0.625D),
        new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.25D, 0.625D),
        new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.375D, 0.625D),
        new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.5D, 0.625D),
        new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.625D, 0.625D),
        new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.75D, 0.625D),
        new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.875D, 0.625D),
        new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 1.0D, 0.625D) };

    protected WrapperBlockStem(WrapperBlock crop) {
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(AGE, Integer.valueOf(0))
                .withProperty(FACING, EnumFacing.UP));
        this.crop = crop;
        this.setTickRandomly(true);
        this.setCreativeTab((CreativeTabs) null);
    }

    public AxisAlignedBB getBoundingBox(IWrapperBlockState state, IBlockAccess source, BlockPos pos) {
        return STEM_AABB[((Integer) state.getValue(AGE)).intValue()];
    }

    /**
     * Get the actual Block state of this Block at the given position. This applies properties not visible in the
     * metadata, such as fence connections.
     */
    public IWrapperBlockState getActualState(IWrapperBlockState state, IBlockAccess worldIn, BlockPos pos) {
        int i = ((Integer) state.getValue(AGE)).intValue();
        state = state.withProperty(FACING, EnumFacing.UP);

        for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
            if (worldIn.getBlockState(pos.offset(enumfacing))
                .getBlock() == this.crop && i == 7) {
                state = state.withProperty(FACING, enumfacing);
                break;
            }
        }

        return state;
    }

    /**
     * Return true if the block can sustain a Bush
     */
    protected boolean canSustainBush(IWrapperBlockState state) {
        return state.getBlock() == Blocks.FARMLAND;
    }

    public void updateTick(World worldIn, BlockPos pos, IWrapperBlockState state, Random rand) {
        super.updateTick(worldIn, pos, state, rand);

        if (!worldIn.isAreaLoaded(pos, 1)) return; // Forge: prevent loading unloaded chunks when checking neighbor's
                                                   // light
        if (worldIn.getLightFromNeighbors(pos.up()) >= 9) {
            float f = WrapperBlockCrops.getGrowthChance(this, worldIn, pos);

            if (net.minecraftforge.common.ForgeHooks
                .onCropsGrowPre(worldIn, pos, state, rand.nextInt((int) (25.0F / f) + 1) == 0)) {
                int i = ((Integer) state.getValue(AGE)).intValue();

                if (i < 7) {
                    IWrapperBlockState newState = state.withProperty(AGE, Integer.valueOf(i + 1));
                    worldIn.setBlockState(pos, newState, 2);
                } else {
                    for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
                        if (worldIn.getBlockState(pos.offset(enumfacing))
                            .getBlock() == this.crop) {
                            return;
                        }
                    }

                    pos = pos.offset(EnumFacing.Plane.HORIZONTAL.random(rand));
                    IWrapperBlockState soil = worldIn.getBlockState(pos.down());
                    WrapperBlock wrapperBlock = soil.getBlock();

                    if (worldIn.isAirBlock(pos)
                        && (wrapperBlock.canSustainPlant(soil, worldIn, pos.down(), EnumFacing.UP, this)
                            || wrapperBlock == Blocks.DIRT
                            || wrapperBlock == Blocks.GRASS)) {
                        worldIn.setBlockState(pos, this.crop.getDefaultState());
                    }
                }
                net.minecraftforge.common.ForgeHooks.onCropsGrowPost(worldIn, pos, state, worldIn.getBlockState(pos));
            }
        }
    }

    public void growStem(World worldIn, BlockPos pos, IWrapperBlockState state) {
        int i = ((Integer) state.getValue(AGE)).intValue() + MathHelper.getInt(worldIn.rand, 2, 5);
        worldIn.setBlockState(pos, state.withProperty(AGE, Integer.valueOf(Math.min(7, i))), 2);
    }

    /**
     * Spawns this Block's drops into the World as EntityItems.
     */
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IWrapperBlockState state, float chance,
        int fortune) {
        super.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune);
    }

    @Override
    public void getDrops(net.minecraft.util.NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos,
        IWrapperBlockState state, int fortune) {
        {
            Item item = this.getSeedItem();

            if (item != null) {
                int i = ((Integer) state.getValue(AGE)).intValue();

                for (int j = 0; j < 3; ++j) {
                    if (RANDOM.nextInt(15) <= i) {
                        drops.add(new ItemStack(item));
                    }
                }
            }
        }
    }

    @Nullable
    protected Item getSeedItem() {
        if (this.crop == Blocks.PUMPKIN) {
            return Items.PUMPKIN_SEEDS;
        } else {
            return this.crop == Blocks.MELON_BLOCK ? Items.MELON_SEEDS : null;
        }
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    public Item getItemDropped(IWrapperBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    public ItemStack getItem(World worldIn, BlockPos pos, IWrapperBlockState state) {
        Item item = this.getSeedItem();
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    /**
     * Whether this IGrowable can grow
     */
    public boolean canGrow(World worldIn, BlockPos pos, IWrapperBlockState state, boolean isClient) {
        return ((Integer) state.getValue(AGE)).intValue() != 7;
    }

    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IWrapperBlockState state) {
        return true;
    }

    public void grow(World worldIn, Random rand, BlockPos pos, IWrapperBlockState state) {
        this.growStem(worldIn, pos, state);
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IWrapperBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
            .withProperty(AGE, Integer.valueOf(meta));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        return ((Integer) state.getValue(AGE)).intValue();
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { AGE, FACING });
    }
}
