package shordinger.ModWrapper.migration.wrapper.minecraft.util.datafix;

import net.minecraft.nbt.NBTTagCompound;

public interface IFixableData {

    int getFixVersion();

    NBTTagCompound fixTagCompound(NBTTagCompound compound);
}
