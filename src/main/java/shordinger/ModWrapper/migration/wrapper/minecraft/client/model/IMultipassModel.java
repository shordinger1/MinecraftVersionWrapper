package shordinger.ModWrapper.migration.wrapper.minecraft.client.model;

import net.minecraft.entity.Entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IMultipassModel {

    void renderMultipass(Entity p_187054_1_, float p_187054_2_, float p_187054_3_, float p_187054_4_, float p_187054_5_,
        float p_187054_6_, float scale);
}
