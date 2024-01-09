package shordinger.ModWrapper.migration.wrapper.minecraft.entity.ai.attributes;

import javax.annotation.Nullable;

public interface IAttribute {

    String getName();

    double clampValue(double value);

    double getDefaultValue();

    boolean getShouldWatch();

    @Nullable
    IAttribute getParent();
}
