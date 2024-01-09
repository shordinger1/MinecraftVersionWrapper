package shordinger.ModWrapper.migration.wrapper.minecraft.world.gen.feature;

import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WorldGenMelon extends WorldGenerator {

    public boolean generate(World worldIn, Random rand, BlockPos position) {
        for (int i = 0; i < 64; ++i) {
            BlockPos blockpos = position.add(
                rand.nextInt(8) - rand.nextInt(8),
                rand.nextInt(4) - rand.nextInt(4),
                rand.nextInt(8) - rand.nextInt(8));

            if (Blocks.MELON_BLOCK.canPlaceBlockAt(worldIn, blockpos) && worldIn.getBlockState(blockpos.down())
                .getBlock() == Blocks.GRASS) {
                worldIn.setBlockState(blockpos, Blocks.MELON_BLOCK.getDefaultState(), 2);
            }
        }

        return true;
    }
}
