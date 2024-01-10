package shordinger.ModWrapper.migration.wrapper.minecraft.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.Vec2f;

@SideOnly(Side.CLIENT)
public class MovementInput extends net.minecraft.util.MovementInput {
    public boolean forwardKeyDown;
    public boolean backKeyDown;
    public boolean leftKeyDown;
    public boolean rightKeyDown;

    public Vec2f getMoveVector() {
        return new Vec2f(this.moveStrafe, this.moveForward);
    }
}
