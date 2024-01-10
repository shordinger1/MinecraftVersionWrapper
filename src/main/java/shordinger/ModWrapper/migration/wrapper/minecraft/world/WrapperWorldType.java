package shordinger.ModWrapper.migration.wrapper.minecraft.world;

import java.util.Arrays;

import net.minecraft.world.WorldType;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class WrapperWorldType extends WorldType {

    // /** List of world types. */
    // public static WrapperWorldType[] WORLD_TYPES = new WrapperWorldType[16];
    // /** Default world type. */
    // public static final WrapperWorldType DEFAULT = (new WrapperWorldType(0, "default", 1)).setVersioned();
    // /** Flat world type. */
    // public static final WrapperWorldType FLAT = new WrapperWorldType(1, "flat");
    // /** Large Biome world Type. */
    // public static final WrapperWorldType LARGE_BIOMES = new WrapperWorldType(2, "largeBiomes");
    // /** amplified world type */
    // public static final WrapperWorldType AMPLIFIED = (new WrapperWorldType(3, "amplified")).enableInfoNotice();
    public static final WrapperWorldType CUSTOMIZED = new WrapperWorldType(4, "customized");
    public static final WrapperWorldType DEBUG_ALL_BLOCK_STATES = new WrapperWorldType(5, "debug_all_block_states");

    // private final int worldTypeId;
    // /** 'default' or 'flat' */
    // private final String worldType;
    // /** The int version of the ChunkProvider that generated this world. */
    // private final int generatorVersion;
    // /** Whether this world type can be generated. Normally true; set to false for out-of-date generator versions. */
    // private boolean canBeCreated;
    // /** Whether this WorldType has a version or not. */
    // private boolean isWorldTypeVersioned;
    // private boolean hasNotificationData;
    /**
     * Default (1.1) world type.
     */
    // public static final WrapperWorldType DEFAULT_1_1 = (new WrapperWorldType(8, "default_1_1",
    // 0)).setCanBeCreated(false);
    // /** ID for this world type. */
    // private boolean hasInfoNotice;
    private WrapperWorldType(int id, String name) {
        this(id, name, 0);
    }

    private WrapperWorldType(int id, String name, int version) {
        super(name);
        // this.worldType = name;
        // this.generatorVersion = version;
        // this.canBeCreated = true;
        // this.worldTypeId = id;
    }

    public String getName() {
        return getWorldTypeName();
    }

    /**
     * Gets the translation key for the name of this world type.
     */
    @SideOnly(Side.CLIENT)
    public String getTranslationKey() {
        return "generator." + this.getName();
    }

    /**
     * Gets the translation key for the info text for this world type.
     */
    @SideOnly(Side.CLIENT)
    public String getInfoTranslationKey() {
        return this.getTranslationKey() + ".info";
    }

    /**
     * Returns generatorVersion.
     */
    public int getVersion() {
        return super.getGeneratorVersion();
    }

    public WrapperWorldType getWorldTypeForGeneratorVersion(int version) {
        return this == DEFAULT && version == 0 ? (WrapperWorldType) DEFAULT_1_1 : this;
    }

    /**
     * Gets whether this WrapperWorldType can be used to generate a new world.
     */
    @SideOnly(Side.CLIENT)
    public boolean canBeCreated() {
        return getCanBeCreated();
    }

    /**
     * Returns true if this world Type has a version associated with it.
     */
    public boolean isVersioned() {
        return super.isVersioned();
    }

    public static WrapperWorldType parseWorldType(String type) {
        return (WrapperWorldType) WorldType.parseWorldType(type);
    }

    public int getId() {
        return super.getWorldTypeID();
    }

    /**
     * returns true if selecting this WrapperWorldType from the customize menu should display the
     * generator.[WrapperWorldType].info
     * message
     */
    @SideOnly(Side.CLIENT)
    public boolean hasInfoNotice() {
        return super.showWorldInfoNotice();
    }

    public net.minecraft.world.biome.BiomeProvider getBiomeProvider(World world) {
        if (this == FLAT) {
            net.minecraft.world.gen.FlatGeneratorInfo flatgeneratorinfo = net.minecraft.world.gen.FlatGeneratorInfo
                .createFlatGeneratorFromString(
                    world.getWorldInfo()
                        .getGeneratorOptions());
            return new net.minecraft.world.biome.BiomeProviderSingle(
                net.minecraft.world.biome.Biome
                    .getBiome(flatgeneratorinfo.getBiome(), net.minecraft.init.Biomes.DEFAULT));
        } else if (this == DEBUG_ALL_BLOCK_STATES) {
            return new net.minecraft.world.biome.BiomeProviderSingle(net.minecraft.init.Biomes.PLAINS);
        } else {
            return new net.minecraft.world.biome.BiomeProvider(world.getWorldInfo());
        }
    }

    public net.minecraft.world.gen.IChunkGenerator getChunkGenerator(World world,
                                                                     String generatorOptions) {
        if (this == FLAT) return new net.minecraft.world.gen.ChunkGeneratorFlat(
                world,
            world.getSeed(),
            world.getWorldInfo()
                .isMapFeaturesEnabled(),
            generatorOptions);
        if (this == DEBUG_ALL_BLOCK_STATES) return new net.minecraft.world.gen.ChunkGeneratorDebug(world);
        if (this == CUSTOMIZED) return new net.minecraft.world.gen.ChunkGeneratorOverworld(
                world,
            world.getSeed(),
            world.getWorldInfo()
                .isMapFeaturesEnabled(),
            generatorOptions);
        return new net.minecraft.world.gen.ChunkGeneratorOverworld(
                world,
            world.getSeed(),
            world.getWorldInfo()
                .isMapFeaturesEnabled(),
            generatorOptions);
    }

    public int getMinimumSpawnHeight(World world) {
        return this == FLAT ? 4 : world.getSeaLevel() + 1;
    }

    public double getHorizon(World world) {
        return this == FLAT ? 0.0D : 63.0D;
    }

    public double voidFadeMagnitude() {
        return super.voidFadeMagnitude();
    }

    public boolean handleSlimeSpawnReduction(java.util.Random random, World world) {
        return this == FLAT && random.nextInt(4) != 1;
    }

    /* =================================================== FORGE START ====================================== */
    private static int getNextID() {
        for (int x = 0; x < worldTypes.length; x++) {
            if (worldTypes[x] == null) {
                return x;
            }
        }

        int oldLen = worldTypes.length;
        worldTypes = Arrays.copyOf(worldTypes, oldLen + 16);
        return oldLen;
    }

    /**
     * Creates a new world type, the ID is hidden and should not be referenced by modders.
     * It will automatically expand the underlying workdType array if there are no IDs left.
     *
     * @param name
     */
    public WrapperWorldType(String name) {
        this(getNextID(), name);
    }

    /**
     * Called when 'Create New World' button is pressed before starting game
     */
    public void onGUICreateWorldPress() {
        super.onGUICreateWorldPress();
    }

    /**
     * Gets the spawn fuzz for players who join the world.
     * Useful for void world types.
     *
     * @return Fuzz for entity initial spawn in blocks.
     */
    public int getSpawnFuzz(WorldServer world, net.minecraft.server.MinecraftServer server) {
        return Math.max(0, server.getSpawnRadius(world));
    }

    /**
     * Called when the 'Customize' button is pressed on world creation GUI
     *
     * @param mc             The Minecraft instance
     * @param guiCreateWorld the createworld GUI
     */
    @SideOnly(Side.CLIENT)
    public void onCustomizeButton(net.minecraft.client.Minecraft mc,
        net.minecraft.client.gui.GuiCreateWorld guiCreateWorld) {
        if (this == WrapperWorldType.FLAT) {
            mc.displayGuiScreen(
                new net.minecraft.client.gui.GuiCreateFlatWorld(
                    guiCreateWorld,
                    guiCreateWorld.chunkProviderSettingsJson));
        } else if (this == WrapperWorldType.CUSTOMIZED) {
            mc.displayGuiScreen(
                new net.minecraft.client.gui.GuiCustomizeWorldScreen(
                    guiCreateWorld,
                    guiCreateWorld.chunkProviderSettingsJson));
        }
    }

    /**
     * Should world creation GUI show 'Customize' button for this world type?
     *
     * @return if this world type has customization parameters
     */
    public boolean isCustomizable() {
        return this == FLAT || this == WrapperWorldType.CUSTOMIZED;
    }

    /**
     * Get the height to render the clouds for this world type
     *
     * @return The height to render clouds at
     */
    public float getCloudHeight() {
        return 128.0F;
    }

    /**
     * Creates the GenLayerBiome used for generating the world with the specified ChunkProviderSettings JSON String
     * *IF AND ONLY IF* this WrapperWorldType == WrapperWorldType.CUSTOMIZED.
     *
     * @param worldSeed     The world seed
     * @param parentLayer   The parent layer to feed into any layer you return
     * @param chunkSettings The ChunkGeneratorSettings constructed from the custom JSON
     * @return A GenLayer that will return ints representing the Biomes to be generated, see GenLayerBiome
     */
    public net.minecraft.world.gen.layer.GenLayer getBiomeLayer(long worldSeed,
        net.minecraft.world.gen.layer.GenLayer parentLayer,
        net.minecraft.world.gen.ChunkGeneratorSettings chunkSettings) {
        net.minecraft.world.gen.layer.GenLayer ret = new net.minecraft.world.gen.layer.GenLayerBiome(
            200L,
            parentLayer,
            this,
            chunkSettings);
        ret = net.minecraft.world.gen.layer.GenLayerZoom.magnify(1000L, ret, 2);
        ret = new net.minecraft.world.gen.layer.GenLayerBiomeEdge(1000L, ret);
        return ret;
    }
}
