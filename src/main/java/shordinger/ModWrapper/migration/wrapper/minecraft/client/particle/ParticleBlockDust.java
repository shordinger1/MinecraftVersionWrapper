package shordinger.ModWrapper.migration.wrapper.minecraft.client.particle;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleBlockDust extends ParticleDigging {

    protected ParticleBlockDust(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn,
        double ySpeedIn, double zSpeedIn, IWrapperBlockState state) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, state);
        this.motionX = xSpeedIn;
        this.motionY = ySpeedIn;
        this.motionZ = zSpeedIn;
    }

    @SideOnly(Side.CLIENT)
    public static class Factory implements IParticleFactory {

        @Nullable
        public Particle createParticle(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn,
            double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {
            IWrapperBlockState iblockstate = Block.getStateById(p_178902_15_[0]);
            return iblockstate.getRenderType() == EnumBlockRenderType.INVISIBLE ? null
                : (new ParticleBlockDust(
                    worldIn,
                    xCoordIn,
                    yCoordIn,
                    zCoordIn,
                    xSpeedIn,
                    ySpeedIn,
                    zSpeedIn,
                    iblockstate)).init();
        }
    }
}
