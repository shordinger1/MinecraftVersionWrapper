package shordinger.ModWrapper.migration.wrapper.minecraft.stats;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IStatType {

    /**
     * Formats a given stat for human consumption.
     */
    @SideOnly(Side.CLIENT)
    String format(int number);
}
