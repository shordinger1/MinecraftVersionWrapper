package shordinger.ModWrapper.migration.wrapper.minecraft.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;

import com.google.common.base.Predicate;

public class EntityAITargetNonTamed<T extends EntityLivingBase> extends EntityAINearestAttackableTarget<T> {

    private final EntityTameable tameable;

    public EntityAITargetNonTamed(EntityTameable entityIn, Class<T> classTarget, boolean checkSight,
        Predicate<? super T> targetSelector) {
        super(entityIn, classTarget, 10, checkSight, false, targetSelector);
        this.tameable = entityIn;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        return !this.tameable.isTamed() && super.shouldExecute();
    }
}
