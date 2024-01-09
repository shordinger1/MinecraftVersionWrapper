package shordinger.ModWrapper.migration.wrapper.minecraft.enchantment;

import net.minecraft.util.WeightedRandom;

public class EnchantmentData extends WeightedRandom.Item {

    /** Enchantment object associated with this EnchantmentData */
    public Enchantment enchantment;
    /** Enchantment level associated with this EnchantmentData */
    public int enchantmentLevel;

    public EnchantmentData(Enchantment enchantmentObj, int enchLevel) {
        super(
            enchantmentObj.getRarity()
                .getWeight());
        this.enchantment = enchantmentObj;
        this.enchantmentLevel = enchLevel;
    }
}
