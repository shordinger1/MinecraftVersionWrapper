package shordinger.ModWrapper.migration.wrapper.minecraft.client.renderer;

import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ReportedException;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

@SideOnly(Side.CLIENT)
public class BlockRendererDispatcher implements IResourceManagerReloadListener {

    private final BlockModelShapes blockModelShapes;
    private final BlockModelRenderer blockModelRenderer;
    private final ChestRenderer chestRenderer = new ChestRenderer();
    private final BlockFluidRenderer fluidRenderer;

    public BlockRendererDispatcher(BlockModelShapes p_i46577_1_, BlockColors p_i46577_2_) {
        this.blockModelShapes = p_i46577_1_;
        this.blockModelRenderer = new net.minecraftforge.client.model.pipeline.ForgeBlockModelRenderer(p_i46577_2_);
        this.fluidRenderer = new BlockFluidRenderer(p_i46577_2_);
    }

    public BlockModelShapes getBlockModelShapes() {
        return this.blockModelShapes;
    }

    public void renderBlockDamage(IWrapperBlockState state, BlockPos pos, TextureAtlasSprite texture,
        IBlockAccess blockAccess) {
        if (state.getRenderType() == EnumBlockRenderType.MODEL) {
            state = state.getActualState(blockAccess, pos);
            IBakedModel ibakedmodel = this.blockModelShapes.getModelForState(state);
            IBakedModel ibakedmodel1 = net.minecraftforge.client.ForgeHooksClient
                .getDamageModel(ibakedmodel, texture, state, blockAccess, pos);
            this.blockModelRenderer.renderModel(
                blockAccess,
                ibakedmodel1,
                state,
                pos,
                Tessellator.getInstance()
                    .getBuffer(),
                true);
        }
    }

    public boolean renderBlock(IWrapperBlockState state, BlockPos pos, IBlockAccess blockAccess,
        BufferBuilder bufferBuilderIn) {
        try {
            EnumBlockRenderType enumblockrendertype = state.getRenderType();

            if (enumblockrendertype == EnumBlockRenderType.INVISIBLE) {
                return false;
            } else {
                if (blockAccess.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
                    try {
                        state = state.getActualState(blockAccess, pos);
                    } catch (Exception var8) {
                        ;
                    }
                }

                switch (enumblockrendertype) {
                    case MODEL:
                        IBakedModel model = this.getModelForState(state);
                        state = state.getBlock()
                            .getExtendedState(state, blockAccess, pos);
                        return this.blockModelRenderer
                            .renderModel(blockAccess, model, state, pos, bufferBuilderIn, true);
                    case ENTITYBLOCK_ANIMATED:
                        return false;
                    case LIQUID:
                        return this.fluidRenderer.renderFluid(blockAccess, state, pos, bufferBuilderIn);
                    default:
                        return false;
                }
            }
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Tesselating block in world");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being tesselated");
            CrashReportCategory.addBlockInfo(
                crashreportcategory,
                pos,
                state.getBlock(),
                state.getBlock()
                    .getMetaFromState(state));
            throw new ReportedException(crashreport);
        }
    }

    public BlockModelRenderer getBlockModelRenderer() {
        return this.blockModelRenderer;
    }

    public IBakedModel getModelForState(IWrapperBlockState state) {
        return this.blockModelShapes.getModelForState(state);
    }

    @SuppressWarnings("incomplete-switch")
    public void renderBlockBrightness(IWrapperBlockState state, float brightness) {
        EnumBlockRenderType enumblockrendertype = state.getRenderType();

        if (enumblockrendertype != EnumBlockRenderType.INVISIBLE) {
            switch (enumblockrendertype) {
                case MODEL:
                    IBakedModel ibakedmodel = this.getModelForState(state);
                    this.blockModelRenderer.renderModelBrightness(ibakedmodel, state, brightness, true);
                    break;
                case ENTITYBLOCK_ANIMATED:
                    this.chestRenderer.renderChestBrightness(state.getBlock(), brightness);
                case LIQUID:
            }
        }
    }

    public void onResourceManagerReload(IResourceManager resourceManager) {
        this.fluidRenderer.initAtlasSprites();
    }
}
