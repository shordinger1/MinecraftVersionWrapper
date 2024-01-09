package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WrapperBlockFalling extends WrapperBlock {

    public static boolean fallInstantly;

    public WrapperBlockFalling() {
        super(Material.SAND);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    public WrapperBlockFalling(Material materialIn) {
        super(materialIn);
    }

    /**
     * Called after the block is set in the Chunk data, but before the Tile Entity is set
     */
    public void onBlockAdded(World worldIn, BlockPos pos, IWrapperBlockState state) {
        worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
    }

    /**
     * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
     * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
     * block, etc.
     */
    public void neighborChanged(IWrapperBlockState state, World worldIn, BlockPos pos, WrapperBlock wrapperBlockIn,
        BlockPos fromPos) {
        worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
    }

    public void updateTick(World worldIn, BlockPos pos, IWrapperBlockState state, Random rand) {
        if (!worldIn.isRemote) {
            this.checkFallable(worldIn, pos);
        }
    }

    private void checkFallable(World worldIn, BlockPos pos) {
        if ((worldIn.isAirBlock(pos.down()) || canFallThrough(worldIn.getBlockState(pos.down()))) && pos.getY() >= 0) {
            int i = 32;

            if (!fallInstantly && worldIn.isAreaLoaded(pos.add(-32, -32, -32), pos.add(32, 32, 32))) {
                if (!worldIn.isRemote) {
                    EntityFallingBlock entityfallingblock = new EntityFallingBlock(
                        worldIn,
                        (double) pos.getX() + 0.5D,
                        (double) pos.getY(),
                        (double) pos.getZ() + 0.5D,
                        worldIn.getBlockState(pos));
                    this.onStartFalling(entityfallingblock);
                    worldIn.spawnEntity(entityfallingblock);
                }
            } else {
                IWrapperBlockState state = worldIn.getBlockState(pos);
                worldIn.setBlockToAir(pos);
                BlockPos blockpos;

                for (blockpos = pos
                    .down(); (worldIn.isAirBlock(blockpos) || canFallThrough(worldIn.getBlockState(blockpos)))
                        && blockpos.getY() > 0; blockpos = blockpos.down()) {
                    ;
                }

                if (blockpos.getY() > 0) {
                    worldIn.setBlockState(blockpos.up(), state); // Forge: Fix loss of state information during world
                                                                 // gen.
                }
            }
        }
    }

    protected void onStartFalling(EntityFallingBlock fallingEntity) {}

    /**
     * How many world ticks before ticking
     */
    public int tickRate(World worldIn) {
        return 2;
    }

    public static boolean canFallThrough(IWrapperBlockState state) {
        WrapperBlock wrapperBlock = state.getBlock();
        Material material = state.getMaterial();
        return wrapperBlock == Blocks.FIRE || material == Material.AIR
            || material == Material.WATER
            || material == Material.LAVA;
    }

    public void onEndFalling(World worldIn, BlockPos pos, IWrapperBlockState p_176502_3_,
        IWrapperBlockState p_176502_4_) {}

    public void onBroken(World worldIn, BlockPos pos) {}

    /**
     * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
     * this method is unrelated to {@link randomTick} and {@link #needsRandomTick}, and will always be called regardless
     * of whether the block can receive random update ticks
     */
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IWrapperBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (rand.nextInt(16) == 0) {
            BlockPos blockpos = pos.down();

            if (canFallThrough(worldIn.getBlockState(blockpos))) {
                double d0 = (double) ((float) pos.getX() + rand.nextFloat());
                double d1 = (double) pos.getY() - 0.05D;
                double d2 = (double) ((float) pos.getZ() + rand.nextFloat());
                worldIn.spawnParticle(
                    EnumParticleTypes.FALLING_DUST,
                    d0,
                    d1,
                    d2,
                    0.0D,
                    0.0D,
                    0.0D,
                    WrapperBlock.getStateId(stateIn));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public int getDustColor(IWrapperBlockState state) {
        return -16777216;
    }
}
