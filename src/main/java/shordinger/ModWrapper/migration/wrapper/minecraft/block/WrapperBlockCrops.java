package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import java.util.Random;

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
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.MathHelper;

public class WrapperBlockCrops extends WrapperBlockBush implements IGrowable {

    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 7);
    private static final AxisAlignedBB[] CROPS_AABB = new AxisAlignedBB[] {
        new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.25D, 1.0D),
        new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.375D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D),
        new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.625D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.75D, 1.0D),
        new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.875D, 1.0D),
        new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D) };

    protected WrapperBlockCrops() {
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(this.getAgeProperty(), Integer.valueOf(0)));
        this.setTickRandomly(true);
        this.setCreativeTab((CreativeTabs) null);
        this.setHardness(0.0F);
        this.setSoundType(SoundType.PLANT);
        this.disableStats();
    }

    public AxisAlignedBB getBoundingBox(IWrapperBlockState state, IBlockAccess source, BlockPos pos) {
        return CROPS_AABB[((Integer) state.getValue(this.getAgeProperty())).intValue()];
    }

    /**
     * Return true if the block can sustain a Bush
     */
    protected boolean canSustainBush(IWrapperBlockState state) {
        return state.getBlock() == Blocks.FARMLAND;
    }

    protected PropertyInteger getAgeProperty() {
        return AGE;
    }

    public int getMaxAge() {
        return 7;
    }

    protected int getAge(IWrapperBlockState state) {
        return ((Integer) state.getValue(this.getAgeProperty())).intValue();
    }

    public IWrapperBlockState withAge(int age) {
        return this.getDefaultState()
            .withProperty(this.getAgeProperty(), Integer.valueOf(age));
    }

    public boolean isMaxAge(IWrapperBlockState state) {
        return ((Integer) state.getValue(this.getAgeProperty())).intValue() >= this.getMaxAge();
    }

    public void updateTick(World worldIn, BlockPos pos, IWrapperBlockState state, Random rand) {
        super.updateTick(worldIn, pos, state, rand);

        if (!worldIn.isAreaLoaded(pos, 1)) return; // Forge: prevent loading unloaded chunks when checking neighbor's
                                                   // light
        if (worldIn.getLightFromNeighbors(pos.up()) >= 9) {
            int i = this.getAge(state);

            if (i < this.getMaxAge()) {
                float f = getGrowthChance(this, worldIn, pos);

                if (net.minecraftforge.common.ForgeHooks
                    .onCropsGrowPre(worldIn, pos, state, rand.nextInt((int) (25.0F / f) + 1) == 0)) {
                    worldIn.setBlockState(pos, this.withAge(i + 1), 2);
                    net.minecraftforge.common.ForgeHooks
                        .onCropsGrowPost(worldIn, pos, state, worldIn.getBlockState(pos));
                }
            }
        }
    }

    public void grow(World worldIn, BlockPos pos, IWrapperBlockState state) {
        int i = this.getAge(state) + this.getBonemealAgeIncrease(worldIn);
        int j = this.getMaxAge();

        if (i > j) {
            i = j;
        }

        worldIn.setBlockState(pos, this.withAge(i), 2);
    }

    protected int getBonemealAgeIncrease(World worldIn) {
        return MathHelper.getInt(worldIn.rand, 2, 5);
    }

    protected static float getGrowthChance(WrapperBlock wrapperBlockIn, World worldIn, BlockPos pos) {
        float f = 1.0F;
        BlockPos blockpos = pos.down();

        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                float f1 = 0.0F;
                IWrapperBlockState iblockstate = worldIn.getBlockState(blockpos.add(i, 0, j));

                if (iblockstate.getBlock()
                    .canSustainPlant(
                        iblockstate,
                        worldIn,
                        blockpos.add(i, 0, j),
                        net.minecraft.util.EnumFacing.UP,
                        (net.minecraftforge.common.IPlantable) wrapperBlockIn)) {
                    f1 = 1.0F;

                    if (iblockstate.getBlock()
                        .isFertile(worldIn, blockpos.add(i, 0, j))) {
                        f1 = 3.0F;
                    }
                }

                if (i != 0 || j != 0) {
                    f1 /= 4.0F;
                }

                f += f1;
            }
        }

        BlockPos blockpos1 = pos.north();
        BlockPos blockpos2 = pos.south();
        BlockPos blockpos3 = pos.west();
        BlockPos blockpos4 = pos.east();
        boolean flag = wrapperBlockIn == worldIn.getBlockState(blockpos3)
            .getBlock() || wrapperBlockIn
                == worldIn.getBlockState(blockpos4)
                    .getBlock();
        boolean flag1 = wrapperBlockIn == worldIn.getBlockState(blockpos1)
            .getBlock() || wrapperBlockIn
                == worldIn.getBlockState(blockpos2)
                    .getBlock();

        if (flag && flag1) {
            f /= 2.0F;
        } else {
            boolean flag2 = wrapperBlockIn == worldIn.getBlockState(blockpos3.north())
                .getBlock() || wrapperBlockIn
                    == worldIn.getBlockState(blockpos4.north())
                        .getBlock()
                || wrapperBlockIn == worldIn.getBlockState(blockpos4.south())
                    .getBlock()
                || wrapperBlockIn == worldIn.getBlockState(blockpos3.south())
                    .getBlock();

            if (flag2) {
                f /= 2.0F;
            }
        }

        return f;
    }

    public boolean canBlockStay(World worldIn, BlockPos pos, IWrapperBlockState state) {
        IWrapperBlockState soil = worldIn.getBlockState(pos.down());
        return (worldIn.getLight(pos) >= 8 || worldIn.canSeeSky(pos)) && soil.getBlock()
            .canSustainPlant(soil, worldIn, pos.down(), net.minecraft.util.EnumFacing.UP, this);
    }

    protected Item getSeed() {
        return Items.WHEAT_SEEDS;
    }

    protected Item getCrop() {
        return Items.WHEAT;
    }

    @Override
    public void getDrops(net.minecraft.util.NonNullList<ItemStack> drops, net.minecraft.world.IBlockAccess world,
        BlockPos pos, IWrapperBlockState state, int fortune) {
        super.getDrops(drops, world, pos, state, 0);
        int age = getAge(state);
        Random rand = world instanceof World ? ((World) world).rand : new Random();

        if (age >= getMaxAge()) {
            int k = 3 + fortune;

            for (int i = 0; i < 3 + fortune; ++i) {
                if (rand.nextInt(2 * getMaxAge()) <= age) {
                    drops.add(new ItemStack(this.getSeed(), 1, 0));
                }
            }
        }
    }

    /**
     * Spawns this Block's drops into the World as EntityItems.
     */
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IWrapperBlockState state, float chance,
        int fortune) {
        super.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune);

        if (false && !worldIn.isRemote) // Forge: NOP all this.
        {
            int i = this.getAge(state);

            if (i >= this.getMaxAge()) {
                int j = 3 + fortune;

                for (int k = 0; k < j; ++k) {
                    if (worldIn.rand.nextInt(2 * this.getMaxAge()) <= i) {
                        spawnAsEntity(worldIn, pos, new ItemStack(this.getSeed()));
                    }
                }
            }
        }
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    public Item getItemDropped(IWrapperBlockState state, Random rand, int fortune) {
        return this.isMaxAge(state) ? this.getCrop() : this.getSeed();
    }

    public ItemStack getItem(World worldIn, BlockPos pos, IWrapperBlockState state) {
        return new ItemStack(this.getSeed());
    }

    /**
     * Whether this IGrowable can grow
     */
    public boolean canGrow(World worldIn, BlockPos pos, IWrapperBlockState state, boolean isClient) {
        return !this.isMaxAge(state);
    }

    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IWrapperBlockState state) {
        return true;
    }

    public void grow(World worldIn, Random rand, BlockPos pos, IWrapperBlockState state) {
        this.grow(worldIn, pos, state);
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IWrapperBlockState getStateFromMeta(int meta) {
        return this.withAge(meta);
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        return this.getAge(state);
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { AGE });
    }
}
