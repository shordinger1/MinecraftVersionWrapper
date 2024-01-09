package shordinger.ModWrapper.migration.wrapper.minecraft.block.material;

public class WrapperMaterialLiquid extends WrapperMaterial {

    public WrapperMaterialLiquid(MapColor color) {
        super(color);
        this.setReplaceable();
        this.setNoPushMobility();
    }

    /**
     * Returns if blocks of these materials are liquids.
     */
    public boolean isLiquid() {
        return true;
    }

    /**
     * Returns if this material is considered solid or not
     */
    public boolean blocksMovement() {
        return false;
    }

    /**
     * Returns true if the block is a considered solid. This is true by default.
     */
    public boolean isSolid() {
        return false;
    }
}
