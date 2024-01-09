package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WrapperBlockButtonStone extends WrapperBlockButton {

    protected WrapperBlockButtonStone() {
        super(false);
    }

    protected void playClickSound(@Nullable EntityPlayer player, World worldIn, BlockPos pos) {
        worldIn.playSound(player, pos, SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.6F);
    }

    protected void playReleaseSound(World worldIn, BlockPos pos) {
        worldIn.playSound(
            (EntityPlayer) null,
            pos,
            SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF,
            SoundCategory.BLOCKS,
            0.3F,
            0.5F);
    }
}
