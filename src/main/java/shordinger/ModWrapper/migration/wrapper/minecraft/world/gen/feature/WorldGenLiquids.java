package shordinger.ModWrapper.migration.wrapper.minecraft.world.gen.feature;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WorldGenLiquids extends WorldGenerator {

    private final Block block;

    public WorldGenLiquids(Block blockIn) {
        this.block = blockIn;
    }

    public boolean generate(World worldIn, Random rand, BlockPos position) {
        if (worldIn.getBlockState(position.up())
            .getBlock() != Blocks.STONE) {
            return false;
        } else if (worldIn.getBlockState(position.down())
            .getBlock() != Blocks.STONE) {
                return false;
            } else {
                IWrapperBlockState iblockstate = worldIn.getBlockState(position);

                if (!iblockstate.getBlock()
                    .isAir(iblockstate, worldIn, position) && iblockstate.getBlock() != Blocks.STONE) {
                    return false;
                } else {
                    int i = 0;

                    if (worldIn.getBlockState(position.west())
                        .getBlock() == Blocks.STONE) {
                        ++i;
                    }

                    if (worldIn.getBlockState(position.east())
                        .getBlock() == Blocks.STONE) {
                        ++i;
                    }

                    if (worldIn.getBlockState(position.north())
                        .getBlock() == Blocks.STONE) {
                        ++i;
                    }

                    if (worldIn.getBlockState(position.south())
                        .getBlock() == Blocks.STONE) {
                        ++i;
                    }

                    int j = 0;

                    if (worldIn.isAirBlock(position.west())) {
                        ++j;
                    }

                    if (worldIn.isAirBlock(position.east())) {
                        ++j;
                    }

                    if (worldIn.isAirBlock(position.north())) {
                        ++j;
                    }

                    if (worldIn.isAirBlock(position.south())) {
                        ++j;
                    }

                    if (i == 3 && j == 1) {
                        IWrapperBlockState iblockstate1 = this.block.getDefaultState();
                        worldIn.setBlockState(position, iblockstate1, 2);
                        worldIn.immediateBlockTick(position, iblockstate1, rand);
                    }

                    return true;
                }
            }
    }
}
