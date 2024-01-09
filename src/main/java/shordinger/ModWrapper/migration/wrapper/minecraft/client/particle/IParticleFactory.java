package shordinger.ModWrapper.migration.wrapper.minecraft.client.particle;

import javax.annotation.Nullable;

import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IParticleFactory {

    @Nullable
    Particle createParticle(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn,
        double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_);
}
