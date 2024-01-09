package shordinger.ModWrapper.migration.wrapper.minecraft.inventory;

import net.minecraft.util.text.ITextComponent;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ContainerHorseChest extends InventoryBasic {

    public ContainerHorseChest(String inventoryTitle, int slotCount) {
        super(inventoryTitle, false, slotCount);
    }

    @SideOnly(Side.CLIENT)
    public ContainerHorseChest(ITextComponent inventoryTitle, int slotCount) {
        super(inventoryTitle, slotCount);
    }
}
