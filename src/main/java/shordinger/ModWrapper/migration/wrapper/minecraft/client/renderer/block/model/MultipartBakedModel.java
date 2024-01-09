package shordinger.ModWrapper.migration.wrapper.minecraft.client.renderer.block.model;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MultipartBakedModel implements IBakedModel {

    private final Map<Predicate<IWrapperBlockState>, IBakedModel> selectors;
    protected final boolean ambientOcclusion;
    protected final boolean gui3D;
    protected final TextureAtlasSprite particleTexture;
    protected final ItemCameraTransforms cameraTransforms;
    protected final ItemOverrideList overrides;

    public MultipartBakedModel(Map<Predicate<IWrapperBlockState>, IBakedModel> selectorsIn) {
        this.selectors = selectorsIn;
        IBakedModel ibakedmodel = selectorsIn.values()
            .iterator()
            .next();
        this.ambientOcclusion = ibakedmodel.isAmbientOcclusion();
        this.gui3D = ibakedmodel.isGui3d();
        this.particleTexture = ibakedmodel.getParticleTexture();
        this.cameraTransforms = ibakedmodel.getItemCameraTransforms();
        this.overrides = ibakedmodel.getOverrides();
    }

    public List<BakedQuad> getQuads(@Nullable IWrapperBlockState state, @Nullable EnumFacing side, long rand) {
        List<BakedQuad> list = Lists.<BakedQuad>newArrayList();

        if (state != null) {
            for (Entry<Predicate<IWrapperBlockState>, IBakedModel> entry : this.selectors.entrySet()) {
                if (((Predicate) entry.getKey()).apply(state)) {
                    list.addAll((entry.getValue()).getQuads(state, side, rand++));
                }
            }
        }

        return list;
    }

    public boolean isAmbientOcclusion() {
        return this.ambientOcclusion;
    }

    public boolean isGui3d() {
        return this.gui3D;
    }

    public boolean isBuiltInRenderer() {
        return false;
    }

    public TextureAtlasSprite getParticleTexture() {
        return this.particleTexture;
    }

    public ItemCameraTransforms getItemCameraTransforms() {
        return this.cameraTransforms;
    }

    public ItemOverrideList getOverrides() {
        return this.overrides;
    }

    @SideOnly(Side.CLIENT)
    public static class Builder {

        private final Map<Predicate<IWrapperBlockState>, IBakedModel> builderSelectors = Maps
            .<Predicate<IWrapperBlockState>, IBakedModel>newLinkedHashMap();

        public void putModel(Predicate<IWrapperBlockState> predicate, IBakedModel model) {
            this.builderSelectors.put(predicate, model);
        }

        public IBakedModel makeMultipartModel() {
            return new MultipartBakedModel(this.builderSelectors);
        }
    }
}
