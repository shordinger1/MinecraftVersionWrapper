package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import javax.annotation.Nullable;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import com.google.common.base.Predicate;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WrapperBlockNewLeaf extends WrapperBlockLeaves {

    public static final PropertyEnum<WrapperBlockPlanks.EnumType> VARIANT = PropertyEnum.<WrapperBlockPlanks.EnumType>create(
        "variant",
        WrapperBlockPlanks.EnumType.class,
        new Predicate<WrapperBlockPlanks.EnumType>() {

            public boolean apply(@Nullable WrapperBlockPlanks.EnumType p_apply_1_) {
                return p_apply_1_.getMetadata() >= 4;
            }
        });

    public WrapperBlockNewLeaf() {
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(VARIANT, WrapperBlockPlanks.EnumType.ACACIA)
                .withProperty(CHECK_DECAY, Boolean.valueOf(true))
                .withProperty(DECAYABLE, Boolean.valueOf(true)));
    }

    protected void dropApple(World worldIn, BlockPos pos, IWrapperBlockState state, int chance) {
        if (state.getValue(VARIANT) == WrapperBlockPlanks.EnumType.DARK_OAK && worldIn.rand.nextInt(chance) == 0) {
            spawnAsEntity(worldIn, pos, new ItemStack(Items.APPLE));
        }
    }

    /**
     * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
     * returns the metadata of the dropped item based on the old metadata of the block.
     */
    public int damageDropped(IWrapperBlockState state) {
        return ((WrapperBlockPlanks.EnumType) state.getValue(VARIANT)).getMetadata();
    }

    public ItemStack getItem(World worldIn, BlockPos pos, IWrapperBlockState state) {
        return new ItemStack(
            this,
            1,
            state.getBlock()
                .getMetaFromState(state) & 3);
    }

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        items.add(new ItemStack(this, 1, 0));
        items.add(new ItemStack(this, 1, 1));
    }

    protected ItemStack getSilkTouchDrop(IWrapperBlockState state) {
        return new ItemStack(
            Item.getItemFromBlock(this),
            1,
            ((WrapperBlockPlanks.EnumType) state.getValue(VARIANT)).getMetadata() - 4);
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IWrapperBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
            .withProperty(VARIANT, this.getWoodType(meta))
            .withProperty(DECAYABLE, Boolean.valueOf((meta & 4) == 0))
            .withProperty(CHECK_DECAY, Boolean.valueOf((meta & 8) > 0));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        int i = 0;
        i = i | ((WrapperBlockPlanks.EnumType) state.getValue(VARIANT)).getMetadata() - 4;

        if (!((Boolean) state.getValue(DECAYABLE)).booleanValue()) {
            i |= 4;
        }

        if (((Boolean) state.getValue(CHECK_DECAY)).booleanValue()) {
            i |= 8;
        }

        return i;
    }

    public WrapperBlockPlanks.EnumType getWoodType(int meta) {
        return WrapperBlockPlanks.EnumType.byMetadata((meta & 3) + 4);
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { VARIANT, CHECK_DECAY, DECAYABLE });
    }

    /**
     * Spawns the block's drops in the world. By the time this is called the Block has possibly been set to air via
     * Block.removedByPlayer
     */
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IWrapperBlockState state,
        @Nullable TileEntity te, ItemStack stack) {
        if (!worldIn.isRemote && stack.getItem() == Items.SHEARS) {
            player.addStat(StatList.getBlockStats(this));
            spawnAsEntity(
                worldIn,
                pos,
                new ItemStack(
                    Item.getItemFromBlock(this),
                    1,
                    ((WrapperBlockPlanks.EnumType) state.getValue(VARIANT)).getMetadata() - 4));
        } else {
            super.harvestBlock(worldIn, player, pos, state, te, stack);
        }
    }

    @Override
    public NonNullList<ItemStack> onSheared(ItemStack item, net.minecraft.world.IBlockAccess world, BlockPos pos,
        int fortune) {
        return NonNullList.withSize(
            1,
            new ItemStack(
                this,
                1,
                world.getBlockState(pos)
                    .getValue(VARIANT)
                    .getMetadata() - 4));
    }
}
