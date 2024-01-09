package shordinger.ModWrapper.migration.wrapper.minecraft.world.chunk;

import javax.annotation.Nullable;

public interface IChunkProvider {

    @Nullable
    WrapperChunk getLoadedChunk(int x, int z);

    WrapperChunk provideChunk(int x, int z);

    /**
     * Unloads chunks that are marked to be unloaded. This is not guaranteed to unload every such chunk.
     */
    boolean tick();

    /**
     * Converts the instance data to a readable string.
     */
    String makeString();

    boolean isChunkGeneratedAt(int x, int z);
}
