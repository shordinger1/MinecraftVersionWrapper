package shordinger.ModWrapper.migration.wrapper.minecraft.client.renderer.chunk;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ListedRenderChunk extends RenderChunk {

    private final int baseDisplayList = GLAllocation.generateDisplayLists(BlockRenderLayer.values().length);

    public ListedRenderChunk(World worldIn, RenderGlobal renderGlobalIn, int index) {
        super(worldIn, renderGlobalIn, index);
    }

    public int getDisplayList(BlockRenderLayer layer, CompiledChunk p_178600_2_) {
        return !p_178600_2_.isLayerEmpty(layer) ? this.baseDisplayList + layer.ordinal() : -1;
    }

    public void deleteGlResources() {
        super.deleteGlResources();
        GLAllocation.deleteDisplayLists(this.baseDisplayList, BlockRenderLayer.values().length);
    }
}
