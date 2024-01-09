package shordinger.ModWrapper.migration.wrapper.minecraft.entity;

import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public interface IEntityMultiPart {

    World getWorld();

    boolean attackEntityFromPart(MultiPartEntityPart dragonPart, DamageSource source, float damage);
}
