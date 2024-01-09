package shordinger.ModWrapper.migration.wrapper.minecraft.util.datafix;

import net.minecraft.nbt.NBTTagCompound;

public interface IDataFixer {

    NBTTagCompound process(IFixType type, NBTTagCompound compound, int versionIn);
}
