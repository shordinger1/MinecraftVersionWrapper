package shordinger.ModWrapper.migration.wrapper.minecraft.world.gen.feature;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WorldGenHellLava extends WorldGenerator {

    private final Block block;
    private final boolean insideRock;

    public WorldGenHellLava(Block blockIn, boolean insideRockIn) {
        this.block = blockIn;
        this.insideRock = insideRockIn;
    }

    public boolean generate(World worldIn, Random rand, BlockPos position) {
        if (worldIn.getBlockState(position.up())
            .getBlock() != Blocks.NETHERRACK) {
            return false;
        } else if (!worldIn.isAirBlock(position) && worldIn.getBlockState(position)
            .getBlock() != Blocks.NETHERRACK) {
                return false;
            } else {
                int i = 0;

                if (worldIn.getBlockState(position.west())
                    .getBlock() == Blocks.NETHERRACK) {
                    ++i;
                }

                if (worldIn.getBlockState(position.east())
                    .getBlock() == Blocks.NETHERRACK) {
                    ++i;
                }

                if (worldIn.getBlockState(position.north())
                    .getBlock() == Blocks.NETHERRACK) {
                    ++i;
                }

                if (worldIn.getBlockState(position.south())
                    .getBlock() == Blocks.NETHERRACK) {
                    ++i;
                }

                if (worldIn.getBlockState(position.down())
                    .getBlock() == Blocks.NETHERRACK) {
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

                if (worldIn.isAirBlock(position.down())) {
                    ++j;
                }

                if (!this.insideRock && i == 4 && j == 1 || i == 5) {
                    IWrapperBlockState iblockstate = this.block.getDefaultState();
                    worldIn.setBlockState(position, iblockstate, 2);
                    worldIn.immediateBlockTick(position, iblockstate, rand);
                }

                return true;
            }
    }
}
