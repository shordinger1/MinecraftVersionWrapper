// package shordinger.ModWrapper.migration.wrapper.minecraft.world;
//
// import cpw.mods.fml.relauncher.Side;
// import cpw.mods.fml.relauncher.SideOnly;
// import net.minecraft.world.WorldSettings;
// import net.minecraft.world.storage.WorldInfo;
//
// public final class WrapperWorldSettings extends WorldSettings
// {
// /** The seed for the map. */
// private final long seed;
// /** The EnumGameType. */
// private final GameType gameType;
// /** Switch for the map features. 'true' for enabled, 'false' for disabled. */
// private final boolean mapFeaturesEnabled;
// /** True if hardcore mode is enabled */
// private final boolean hardcoreEnabled;
// private final WrapperWorldType terrainType;
// /** True if Commands (cheats) are allowed. */
// private boolean commandsAllowed;
// /** True if the Bonus Chest is enabled. */
// private boolean bonusChestEnabled;
// private String generatorOptions;
//
// public WrapperWorldSettings(long seedIn, GameType gameType, boolean enableMapFeatures, boolean hardcoreMode,
// WorldType worldTypeIn)
// {
// this.generatorOptions = "";
// this.seed = seedIn;
// this.gameType = gameType;
// this.mapFeaturesEnabled = enableMapFeatures;
// this.hardcoreEnabled = hardcoreMode;
// this.terrainType = worldTypeIn;
// }
//
// public WrapperWorldSettings(WorldInfo info)
// {
// this(info.getSeed(), info.getGameType(), info.isMapFeaturesEnabled(), info.isHardcoreModeEnabled(),
// info.getTerrainType());
// }
//
// /**
// * Enables the bonus chest.
// */
// public WrapperWorldSettings enableBonusChest()
// {
// this.bonusChestEnabled = true;
// return this;
// }
//
// public WrapperWorldSettings setGeneratorOptions(String options)
// {
// this.generatorOptions = options;
// return this;
// }
//
// /**
// * Enables Commands (cheats).
// */
// @SideOnly(Side.CLIENT)
// public WrapperWorldSettings enableCommands()
// {
// this.commandsAllowed = true;
// return this;
// }
//
// /**
// * Returns true if the Bonus Chest is enabled.
// */
// public boolean isBonusChestEnabled()
// {
// return this.bonusChestEnabled;
// }
//
// /**
// * Returns the seed for the world.
// */
// public long getSeed()
// {
// return this.seed;
// }
//
// /**
// * Gets the game type.
// */
// public GameType getGameType()
// {
// return this.gameType;
// }
//
// /**
// * Returns true if hardcore mode is enabled, otherwise false
// */
// public boolean getHardcoreEnabled()
// {
// return this.hardcoreEnabled;
// }
//
// /**
// * Get whether the map features (e.g. strongholds) generation is enabled or disabled.
// */
// public boolean isMapFeaturesEnabled()
// {
// return this.mapFeaturesEnabled;
// }
//
// public WrapperWorldType getTerrainType()
// {
// return this.terrainType;
// }
//
// /**
// * Returns true if Commands (cheats) are allowed.
// */
// public boolean areCommandsAllowed()
// {
// return this.commandsAllowed;
// }
//
// /**
// * Gets the GameType by ID
// */
// public static GameType getGameTypeById(int id)
// {
// return GameType.getByID(id);
// }
//
// public String getGeneratorOptions()
// {
// return this.generatorOptions;
// }
// }
