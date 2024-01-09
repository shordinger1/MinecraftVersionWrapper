package shordinger.ModWrapper.migration.wrapper.minecraft.world;

import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

public class WrapperWorldServerDemo extends WrapperWorldServer {

    private static final long DEMO_WORLD_SEED = (long) "North Carolina".hashCode();
    public static final WrapperWorldSettings DEMO_WORLD_SETTINGS = (new WrapperWorldSettings(
        DEMO_WORLD_SEED,
        GameType.SURVIVAL,
        true,
        false,
        WorldType.DEFAULT)).enableBonusChest();

    public WrapperWorldServerDemo(MinecraftServer server, ISaveHandler saveHandlerIn, WorldInfo worldInfoIn,
        int dimensionId, Profiler profilerIn) {
        super(server, saveHandlerIn, worldInfoIn, dimensionId, profilerIn);
        this.worldInfo.populateFromWorldSettings(DEMO_WORLD_SETTINGS);
    }
}
