package shordinger.ModWrapper.migration.wrapper.minecraft.client.audio;

import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class MovingSound extends PositionedSound implements ITickableSound {

    protected boolean donePlaying;

    protected MovingSound(SoundEvent soundIn, SoundCategory categoryIn) {
        super(soundIn, categoryIn);
    }

    public boolean isDonePlaying() {
        return this.donePlaying;
    }
}
