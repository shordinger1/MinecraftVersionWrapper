package shordinger.ModWrapper.migration.wrapper.minecraft.util;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public enum EnumHandSide {

    LEFT(new TextComponentTranslation("options.mainHand.left", new Object[0])),
    RIGHT(new TextComponentTranslation("options.mainHand.right", new Object[0]));

    private final ITextComponent handName;

    private EnumHandSide(ITextComponent nameIn) {
        this.handName = nameIn;
    }

    @SideOnly(Side.CLIENT)
    public EnumHandSide opposite() {
        return this == LEFT ? RIGHT : LEFT;
    }

    public String toString() {
        return this.handName.getUnformattedText();
    }
}
