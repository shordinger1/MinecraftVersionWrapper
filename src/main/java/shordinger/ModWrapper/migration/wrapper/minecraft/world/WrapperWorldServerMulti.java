package shordinger.ModWrapper.migration.wrapper.minecraft.world;

import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraft.world.storage.ISaveHandler;

public class WrapperWorldServerMulti extends WrapperWorldServer {

    private final WrapperWorldServer delegate;
    private IBorderListener borderListener;

    public WrapperWorldServerMulti(MinecraftServer server, ISaveHandler saveHandlerIn, int dimensionId,
        WrapperWorldServer delegate, Profiler profilerIn) {
        super(server, saveHandlerIn, new DerivedWorldInfo(delegate.getWorldInfo()), dimensionId, profilerIn);
        this.delegate = delegate;
        this.borderListener = new IBorderListener() {

            public void onSizeChanged(WorldBorder border, double newSize) {
                WrapperWorldServerMulti.this.getWorldBorder()
                    .setTransition(newSize);
            }

            public void onTransitionStarted(WorldBorder border, double oldSize, double newSize, long time) {
                WrapperWorldServerMulti.this.getWorldBorder()
                    .setTransition(oldSize, newSize, time);
            }

            public void onCenterChanged(WorldBorder border, double x, double z) {
                WrapperWorldServerMulti.this.getWorldBorder()
                    .setCenter(x, z);
            }

            public void onWarningTimeChanged(WorldBorder border, int newTime) {
                WrapperWorldServerMulti.this.getWorldBorder()
                    .setWarningTime(newTime);
            }

            public void onWarningDistanceChanged(WorldBorder border, int newDistance) {
                WrapperWorldServerMulti.this.getWorldBorder()
                    .setWarningDistance(newDistance);
            }

            public void onDamageAmountChanged(WorldBorder border, double newAmount) {
                WrapperWorldServerMulti.this.getWorldBorder()
                    .setDamageAmount(newAmount);
            }

            public void onDamageBufferChanged(WorldBorder border, double newSize) {
                WrapperWorldServerMulti.this.getWorldBorder()
                    .setDamageBuffer(newSize);
            }
        };
        this.delegate.getWorldBorder()
            .addListener(this.borderListener);
    }

    /**
     * Saves the chunks to disk.
     */
    protected void saveLevel() throws MinecraftException {
        this.perWorldStorage.saveAllData();
    }

    public WrapperWorld init() {
        this.mapStorage = this.delegate.getMapStorage();
        this.worldScoreboard = this.delegate.getScoreboard();
        this.lootTable = this.delegate.getLootTableManager();
        this.advancementManager = this.delegate.getAdvancementManager();
        String s = VillageCollection.fileNameForProvider(this.provider);
        VillageCollection villagecollection = (VillageCollection) this.perWorldStorage
            .getOrLoadData(VillageCollection.class, s);

        if (villagecollection == null) {
            this.villageCollection = new VillageCollection(this);
            this.perWorldStorage.setData(s, this.villageCollection);
        } else {
            this.villageCollection = villagecollection;
            this.villageCollection.setWorldsForAll(this);
        }

        this.initCapabilities();
        return this;
    }

    /**
     * Syncs all changes to disk and wait for completion.
     */
    @Override
    public void flush() {
        super.flush();
        this.delegate.getWorldBorder()
            .removeListener(this.borderListener); // Unlink ourselves, to prevent world leak.
    }

    /**
     * Called during saving of a world to give children worlds a chance to save additional data. Only used to save
     * WorldProviderEnd's data in Vanilla.
     */
    public void saveAdditionalData() {
        this.provider.onWorldSave();
    }
}
