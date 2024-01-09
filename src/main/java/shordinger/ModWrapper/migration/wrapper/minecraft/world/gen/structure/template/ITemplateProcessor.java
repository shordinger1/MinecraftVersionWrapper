package shordinger.ModWrapper.migration.wrapper.minecraft.world.gen.structure.template;

import javax.annotation.Nullable;

import net.minecraft.world.World;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public interface ITemplateProcessor {

    @Nullable
    Template.BlockInfo processBlock(World worldIn, BlockPos pos, Template.BlockInfo blockInfoIn);
}
