package shordinger.ModWrapper.migration.wrapper.minecraft.client.renderer;

import java.util.List;

import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;

import com.google.common.collect.Lists;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

@SideOnly(Side.CLIENT)
public abstract class ChunkRenderContainer {

    private double viewEntityX;
    private double viewEntityY;
    private double viewEntityZ;
    protected List<RenderChunk> renderChunks = Lists.<RenderChunk>newArrayListWithCapacity(17424);
    protected boolean initialized;

    public void initialize(double viewEntityXIn, double viewEntityYIn, double viewEntityZIn) {
        this.initialized = true;
        this.renderChunks.clear();
        this.viewEntityX = viewEntityXIn;
        this.viewEntityY = viewEntityYIn;
        this.viewEntityZ = viewEntityZIn;
    }

    public void preRenderChunk(RenderChunk renderChunkIn) {
        BlockPos blockpos = renderChunkIn.getPosition();
        GlStateManager.translate(
            (float) ((double) blockpos.getX() - this.viewEntityX),
            (float) ((double) blockpos.getY() - this.viewEntityY),
            (float) ((double) blockpos.getZ() - this.viewEntityZ));
    }

    public void addRenderChunk(RenderChunk renderChunkIn, BlockRenderLayer layer) {
        this.renderChunks.add(renderChunkIn);
    }

    public abstract void renderChunkLayer(BlockRenderLayer layer);
}
