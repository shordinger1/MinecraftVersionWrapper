package shordinger.ModWrapper.migration.wrapper.minecraft.init;

import javax.annotation.Nullable;

import net.minecraft.util.ResourceLocation;
import shordinger.ModWrapper.migration.wrapper.minecraft.potion.WrapperPotion;

public class MobEffects {

    public static final WrapperPotion SPEED;
    public static final WrapperPotion SLOWNESS;
    public static final WrapperPotion HASTE;
    public static final WrapperPotion MINING_FATIGUE;
    public static final WrapperPotion STRENGTH;
    public static final WrapperPotion INSTANT_HEALTH;
    public static final WrapperPotion INSTANT_DAMAGE;
    public static final WrapperPotion JUMP_BOOST;
    public static final WrapperPotion NAUSEA;
    /** None */
    public static final WrapperPotion REGENERATION;
    public static final WrapperPotion RESISTANCE;
    /** The fire resistance Potion object. */
    public static final WrapperPotion FIRE_RESISTANCE;
    /** The water breathing Potion object. */
    public static final WrapperPotion WATER_BREATHING;
    /** The invisibility Potion object. */
    public static final WrapperPotion INVISIBILITY;
    /** The blindness Potion object. */
    public static final WrapperPotion BLINDNESS;
    /** The night vision Potion object. */
    public static final WrapperPotion NIGHT_VISION;
    /** The hunger Potion object. */
    public static final WrapperPotion HUNGER;
    /** The weakness Potion object. */
    public static final WrapperPotion WEAKNESS;
    /** The poison Potion object. */
    public static final WrapperPotion POISON;
    /** The wither Potion object. */
    public static final WrapperPotion WITHER;
    /** The health boost Potion object. */
    public static final WrapperPotion HEALTH_BOOST;
    /** The absorption Potion object. */
    public static final WrapperPotion ABSORPTION;
    /** The saturation Potion object. */
    public static final WrapperPotion SATURATION;
    public static final WrapperPotion GLOWING;
    public static final WrapperPotion LEVITATION;
    public static final WrapperPotion LUCK;
    public static final WrapperPotion UNLUCK;

    @Nullable
    private static WrapperPotion getRegisteredMobEffect(String id) {
        WrapperPotion wrapperPotion = WrapperPotion.REGISTRY.getObject(new ResourceLocation(id));

        if (wrapperPotion == null) {
            throw new IllegalStateException("Invalid MobEffect requested: " + id);
        } else {
            return wrapperPotion;
        }
    }

    static {
        if (!Bootstrap.isRegistered()) {
            throw new RuntimeException("Accessed MobEffects before Bootstrap!");
        } else {
            SPEED = getRegisteredMobEffect("speed");
            SLOWNESS = getRegisteredMobEffect("slowness");
            HASTE = getRegisteredMobEffect("haste");
            MINING_FATIGUE = getRegisteredMobEffect("mining_fatigue");
            STRENGTH = getRegisteredMobEffect("strength");
            INSTANT_HEALTH = getRegisteredMobEffect("instant_health");
            INSTANT_DAMAGE = getRegisteredMobEffect("instant_damage");
            JUMP_BOOST = getRegisteredMobEffect("jump_boost");
            NAUSEA = getRegisteredMobEffect("nausea");
            REGENERATION = getRegisteredMobEffect("regeneration");
            RESISTANCE = getRegisteredMobEffect("resistance");
            FIRE_RESISTANCE = getRegisteredMobEffect("fire_resistance");
            WATER_BREATHING = getRegisteredMobEffect("water_breathing");
            INVISIBILITY = getRegisteredMobEffect("invisibility");
            BLINDNESS = getRegisteredMobEffect("blindness");
            NIGHT_VISION = getRegisteredMobEffect("night_vision");
            HUNGER = getRegisteredMobEffect("hunger");
            WEAKNESS = getRegisteredMobEffect("weakness");
            POISON = getRegisteredMobEffect("poison");
            WITHER = getRegisteredMobEffect("wither");
            HEALTH_BOOST = getRegisteredMobEffect("health_boost");
            ABSORPTION = getRegisteredMobEffect("absorption");
            SATURATION = getRegisteredMobEffect("saturation");
            GLOWING = getRegisteredMobEffect("glowing");
            LEVITATION = getRegisteredMobEffect("levitation");
            LUCK = getRegisteredMobEffect("luck");
            UNLUCK = getRegisteredMobEffect("unluck");
        }
    }
}
