package shordinger.ModWrapper.migration.wrapper.minecraft.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;

public class WrapperPotionAbsorption extends WrapperPotion {

    protected WrapperPotionAbsorption(boolean isBadEffectIn, int liquidColorIn) {
        super(isBadEffectIn, liquidColorIn);
    }

    public void removeAttributesModifiersFromEntity(EntityLivingBase entityLivingBaseIn,
        AbstractAttributeMap attributeMapIn, int amplifier) {
        entityLivingBaseIn
            .setAbsorptionAmount(entityLivingBaseIn.getAbsorptionAmount() - (float) (4 * (amplifier + 1)));
        super.removeAttributesModifiersFromEntity(entityLivingBaseIn, attributeMapIn, amplifier);
    }

    public void applyAttributesModifiersToEntity(EntityLivingBase entityLivingBaseIn,
        AbstractAttributeMap attributeMapIn, int amplifier) {
        entityLivingBaseIn
            .setAbsorptionAmount(entityLivingBaseIn.getAbsorptionAmount() + (float) (4 * (amplifier + 1)));
        super.applyAttributesModifiersToEntity(entityLivingBaseIn, attributeMapIn, amplifier);
    }
}
