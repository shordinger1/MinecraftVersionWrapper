package shordinger.ModWrapper.migration.wrapper.minecraft.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.world.World;

public class WrapperItemSoup extends WrapperItemFood {

    public WrapperItemSoup(int healAmount) {
        super(healAmount, false);
        this.setMaxStackSize(1);
    }

    /**
     * Called when the player finishes using this Item (E.g. finishes eating.). Not called when the player stops using
     * the Item before the action is complete.
     */
    public TempItemStack onItemUseFinish(TempItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        super.onItemUseFinish(stack, worldIn, entityLiving);
        return new TempItemStack(Items.BOWL);
    }
}
