package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import java.util.Random;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.AxisAlignedBB;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class BlockNetherWart extends BlockBush {

    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 3);
    private static final AxisAlignedBB[] NETHER_WART_AABB = new AxisAlignedBB[] {
        new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.3125D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D),
        new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.6875D, 1.0D),
        new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.875D, 1.0D) };

    protected BlockNetherWart() {
        super(Material.PLANTS, MapColor.RED);
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(AGE, Integer.valueOf(0)));
        this.setTickRandomly(true);
        this.setCreativeTab((CreativeTabs) null);
    }

    public AxisAlignedBB getBoundingBox(IWrapperBlockState state, IBlockAccess source, BlockPos pos) {
        return NETHER_WART_AABB[((Integer) state.getValue(AGE)).intValue()];
    }

    /**
     * Return true if the block can sustain a Bush
     */
    protected boolean canSustainBush(IWrapperBlockState state) {
        return state.getBlock() == Blocks.SOUL_SAND;
    }

    public boolean canBlockStay(World worldIn, BlockPos pos, IWrapperBlockState state) {
        return super.canBlockStay(worldIn, pos, state);
    }

    public void updateTick(World worldIn, BlockPos pos, IWrapperBlockState state, Random rand) {
        int i = ((Integer) state.getValue(AGE)).intValue();

        if (i < 3 && net.minecraftforge.common.ForgeHooks.onCropsGrowPre(worldIn, pos, state, rand.nextInt(10) == 0)) {
            IWrapperBlockState newState = state.withProperty(AGE, Integer.valueOf(i + 1));
            worldIn.setBlockState(pos, newState, 2);
            net.minecraftforge.common.ForgeHooks.onCropsGrowPost(worldIn, pos, state, newState);
        }

        super.updateTick(worldIn, pos, state, rand);
    }

    /**
     * Spawns this Block's drops into the World as EntityItems.
     */
    @SuppressWarnings("unused")
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IWrapperBlockState state, float chance,
        int fortune) {
        super.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune);
        if (false && !worldIn.isRemote) {
            int i = 1;

            if (((Integer) state.getValue(AGE)).intValue() >= 3) {
                i = 2 + worldIn.rand.nextInt(3);

                if (fortune > 0) {
                    i += worldIn.rand.nextInt(fortune + 1);
                }
            }

            for (int j = 0; j < i; ++j) {
                spawnAsEntity(worldIn, pos, new ItemStack(Items.NETHER_WART));
            }
        }
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    public Item getItemDropped(IWrapperBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    public int quantityDropped(Random random) {
        return 0;
    }

    public ItemStack getItem(World worldIn, BlockPos pos, IWrapperBlockState state) {
        return new ItemStack(Items.NETHER_WART);
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

    @Override
    public void getDrops(net.minecraft.util.NonNullList<ItemStack> drops, net.minecraft.world.IBlockAccess world,
        BlockPos pos, IWrapperBlockState state, int fortune) {
        Random rand = world instanceof World ? ((World) world).rand : new Random();
        int count = 1;

        if (((Integer) state.getValue(AGE)) >= 3) {
            count = 2 + rand.nextInt(3) + (fortune > 0 ? rand.nextInt(fortune + 1) : 0);
        }

        for (int i = 0; i < count; i++) {
            drops.add(new ItemStack(Items.NETHER_WART));
        }
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { AGE });
    }
}
