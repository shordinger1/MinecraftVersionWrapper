package shordinger.ModWrapper.migration.wrapper.minecraft.potion;

public class WrapperPotionHealth extends WrapperPotion {

    public WrapperPotionHealth(boolean isBadEffectIn, int liquidColorIn) {
        super(isBadEffectIn, liquidColorIn);
    }

    /**
     * Returns true if the potion has an instant effect instead of a continuous one (eg Harming)
     */
    public boolean isInstant() {
        return true;
    }

    /**
     * checks if Potion effect is ready to be applied this tick.
     */
    public boolean isReady(int duration, int amplifier) {
        return duration >= 1;
    }
}
