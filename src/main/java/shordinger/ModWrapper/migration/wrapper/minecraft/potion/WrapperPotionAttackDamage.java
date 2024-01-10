package shordinger.ModWrapper.migration.wrapper.minecraft.potion;

import net.minecraft.entity.ai.attributes.AttributeModifier;

public class WrapperPotionAttackDamage extends WrapperPotion {

    protected final double bonusPerLevel;

    protected WrapperPotionAttackDamage(boolean isBadEffectIn, int liquidColorIn, double bonusPerLevelIn) {
        super(isBadEffectIn, liquidColorIn);
        this.bonusPerLevel = bonusPerLevelIn;
    }

    public double getAttributeModifierAmount(int amplifier, AttributeModifier modifier) {
        return this.bonusPerLevel * (double) (amplifier + 1);
    }
}
