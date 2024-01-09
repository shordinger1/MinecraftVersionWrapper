package shordinger.ModWrapper.migration.wrapper.minecraft.world.gen.feature;

import java.util.Random;

import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WorldGenDeadBush extends WorldGenerator {

    public boolean generate(World worldIn, Random rand, BlockPos position) {
        for (IWrapperBlockState iblockstate = worldIn.getBlockState(position); (iblockstate.getBlock()
            .isAir(iblockstate, worldIn, position)
            || iblockstate.getBlock()
                .isLeaves(iblockstate, worldIn, position))
            && position.getY() > 0; iblockstate = worldIn.getBlockState(position)) {
            position = position.down();
        }

        for (int i = 0; i < 4; ++i) {
            BlockPos blockpos = position.add(
                rand.nextInt(8) - rand.nextInt(8),
                rand.nextInt(4) - rand.nextInt(4),
                rand.nextInt(8) - rand.nextInt(8));

            if (worldIn.isAirBlock(blockpos)
                && Blocks.DEADBUSH.canBlockStay(worldIn, blockpos, Blocks.DEADBUSH.getDefaultState())) {
                worldIn.setBlockState(blockpos, Blocks.DEADBUSH.getDefaultState(), 2);
            }
        }

        return true;
    }
}
