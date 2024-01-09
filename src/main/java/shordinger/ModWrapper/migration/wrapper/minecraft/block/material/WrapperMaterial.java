package shordinger.ModWrapper.migration.wrapper.minecraft.block.material;

import net.minecraft.block.material.Material;

public class WrapperMaterial extends Material {

    public static final WrapperMaterial AIR = (WrapperMaterial) air;
    public static final WrapperMaterial GRASS = new WrapperMaterial(MapColor.GRASS);
    public static final WrapperMaterial GROUND = new WrapperMaterial(MapColor.DIRT);
    public static final WrapperMaterial WOOD = (new WrapperMaterial(MapColor.WOOD)).setBurning();
    public static final WrapperMaterial ROCK = (new WrapperMaterial(MapColor.STONE)).setRequiresTool();
    public static final WrapperMaterial IRON = (new WrapperMaterial(MapColor.IRON)).setRequiresTool();
    public static final WrapperMaterial ANVIL = (new WrapperMaterial(MapColor.IRON)).setRequiresTool()
        .setImmovableMobility();
    public static final WrapperMaterial WATER = (new WrapperMaterialLiquid(MapColor.WATER)).setNoPushMobility();
    public static final WrapperMaterial LAVA = (new WrapperMaterialLiquid(MapColor.TNT)).setNoPushMobility();
    public static final WrapperMaterial LEAVES = (new WrapperMaterial(MapColor.FOLIAGE)).setBurning()
        .setTranslucent()
        .setNoPushMobility();
    public static final WrapperMaterial PLANTS = (new WrapperMaterialLogic(MapColor.FOLIAGE)).setNoPushMobility();
    public static final WrapperMaterial VINE = (new WrapperMaterialLogic(MapColor.FOLIAGE)).setBurning()
        .setNoPushMobility()
        .setReplaceable();
    public static final WrapperMaterial SPONGE = new WrapperMaterial(MapColor.YELLOW);
    public static final WrapperMaterial CLOTH = (new WrapperMaterial(MapColor.CLOTH)).setBurning();
    public static final WrapperMaterial FIRE = (new WrapperMaterialTransparent(MapColor.AIR)).setNoPushMobility();
    public static final WrapperMaterial SAND = new WrapperMaterial(MapColor.SAND);
    public static final WrapperMaterial CIRCUITS = (new WrapperMaterialLogic(MapColor.AIR)).setNoPushMobility();
    public static final WrapperMaterial CARPET = (new WrapperMaterialLogic(MapColor.CLOTH)).setBurning();
    public static final WrapperMaterial GLASS = (new WrapperMaterial(MapColor.AIR)).setTranslucent()
        .setAdventureModeExempt();
    public static final WrapperMaterial REDSTONE_LIGHT = (new WrapperMaterial(MapColor.AIR)).setAdventureModeExempt();
    public static final WrapperMaterial TNT = (new WrapperMaterial(MapColor.TNT)).setBurning()
        .setTranslucent();
    public static final WrapperMaterial CORAL = (new WrapperMaterial(MapColor.FOLIAGE)).setNoPushMobility();
    public static final WrapperMaterial ICE = (new WrapperMaterial(MapColor.ICE)).setTranslucent()
        .setAdventureModeExempt();
    public static final WrapperMaterial PACKED_ICE = (new WrapperMaterial(MapColor.ICE)).setAdventureModeExempt();
    public static final WrapperMaterial SNOW = (new WrapperMaterialLogic(MapColor.SNOW)).setReplaceable()
        .setTranslucent()
        .setRequiresTool()
        .setNoPushMobility();
    /** The material for crafted snow. */
    public static final WrapperMaterial CRAFTED_SNOW = (new WrapperMaterial(MapColor.SNOW)).setRequiresTool();
    public static final WrapperMaterial CACTUS = (new WrapperMaterial(MapColor.FOLIAGE)).setTranslucent()
        .setNoPushMobility();
    public static final WrapperMaterial CLAY = new WrapperMaterial(MapColor.CLAY);
    public static final WrapperMaterial GOURD = (new WrapperMaterial(MapColor.FOLIAGE)).setNoPushMobility();
    public static final WrapperMaterial DRAGON_EGG = (new WrapperMaterial(MapColor.FOLIAGE)).setNoPushMobility();
    public static final WrapperMaterial PORTAL = (new WrapperMaterialPortal(MapColor.AIR)).setImmovableMobility();
    public static final WrapperMaterial CAKE = (new WrapperMaterial(MapColor.AIR)).setNoPushMobility();
    public static final WrapperMaterial WEB = (new WrapperMaterial(MapColor.CLOTH) {

        /**
         * Returns if this material is considered solid or not
         */
        public boolean blocksMovement() {
            return false;
        }
    }).setRequiresTool()
        .setNoPushMobility();
    /** Pistons' material. */
    public static final WrapperMaterial PISTON = (new WrapperMaterial(MapColor.STONE)).setImmovableMobility();
    public static final WrapperMaterial BARRIER = (new WrapperMaterial(MapColor.AIR)).setRequiresTool()
        .setImmovableMobility();
    public static final WrapperMaterial STRUCTURE_VOID = new WrapperMaterialTransparent(MapColor.AIR);
    /** Bool defining if the block can burn or not. */
    private boolean canBurn;
    /**
     * Determines whether blocks with this material can be "overwritten" by other blocks when placed - eg snow, vines
     * and tall grass.
     */
    private boolean replaceable;
    /** Indicates if the material is translucent */
    private boolean isTranslucent;
    /** The color index used to draw the blocks of this material on maps. */
    private final MapColor materialMapColor;
    /** Determines if the material can be harvested without a tool (or with the wrong tool) */
    private boolean requiresNoTool = true;
    /**
     * Mobility information flag. 0 indicates that this block is normal, 1 indicates that it can't push other blocks, 2
     * indicates that it can't be pushed.
     */
    private EnumPushReaction mobilityFlag = EnumPushReaction.NORMAL;
    private boolean isAdventureModeExempt;

    public WrapperMaterial(MapColor color) {
        this.materialMapColor = color;
    }


    /**
     * Returns true if the block is a considered solid. This is true by default.
     */
    public boolean isSolid() {
        return true;
    }

    /**
     * Will prevent grass from growing on dirt underneath and kill any grass below it if it returns true
     */
    public boolean blocksLight() {
        return true;
    }


    /**
     * Marks the material as translucent
     */
    private WrapperMaterial setTranslucent() {
        this.isTranslucent = true;
        return this;
    }

    /**
     * Makes blocks with this material require the correct tool to be harvested.
     */
    protected WrapperMaterial setRequiresTool() {
        this.requiresNoTool = false;
        return this;
    }

    /**
     * Set the canBurn bool to True and return the current object.
     */
    protected WrapperMaterial setBurning() {
        this.canBurn = true;
        return this;
    }

    /**
     * Returns if the block can burn or not.
     */
    public boolean getCanBurn() {
        return this.canBurn;
    }

    /**
     * Sets {@link #replaceable} to true.
     */
    public WrapperMaterial setReplaceable() {
        this.replaceable = true;
        return this;
    }

    /**
     * Returns whether the material can be replaced by other blocks when placed - eg snow, vines and tall grass.
     */
    public boolean isReplaceable() {
        return this.replaceable;
    }

    /**
     * Indicate if the material is opaque
     */
    public boolean isOpaque() {
        return this.isTranslucent ? false : this.blocksMovement();
    }

    /**
     * Returns true if the material can be harvested without a tool (or with the wrong tool)
     */
    public boolean isToolNotRequired() {
        return this.requiresNoTool;
    }

    public EnumPushReaction getMobilityFlag() {
        return this.mobilityFlag;
    }

    /**
     * This type of material can't be pushed, but pistons can move over it.
     */
    protected WrapperMaterial setNoPushMobility() {
        this.mobilityFlag = EnumPushReaction.DESTROY;
        return this;
    }

    /**
     * This type of material can't be pushed, and pistons are blocked to move.
     */
    protected WrapperMaterial setImmovableMobility() {
        this.mobilityFlag = EnumPushReaction.BLOCK;
        return this;
    }



    /**
     * Retrieves the color index of the block. This is is the same color used by vanilla maps to represent this block.
     */
    public MapColor getMaterialMapColor() {
        return this.materialMapColor;
    }
}
