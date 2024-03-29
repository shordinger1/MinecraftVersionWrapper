package shordinger.ModWrapper.migration.wrapper.minecraft.profiler;

public interface ISnooperInfo {

    void addServerStatsToSnooper(Snooper playerSnooper);

    void addServerTypeToSnooper(Snooper playerSnooper);

    /**
     * Returns whether snooping is enabled or not.
     */
    boolean isSnooperEnabled();
}
