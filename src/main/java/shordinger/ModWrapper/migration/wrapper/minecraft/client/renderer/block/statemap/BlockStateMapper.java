package shordinger.ModWrapper.migration.wrapper.minecraft.client.renderer.block.statemap;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockStateMapper {

    private final Map<Block, IStateMapper> blockStateMap = Maps.<Block, IStateMapper>newIdentityHashMap();
    private final Set<Block> setBuiltInBlocks = Sets.<Block>newIdentityHashSet();

    public void registerBlockStateMapper(Block blockIn, IStateMapper stateMapper) {
        this.blockStateMap.put(blockIn, stateMapper);
    }

    public void registerBuiltInBlocks(Block... blockIn) {
        Collections.addAll(this.setBuiltInBlocks, blockIn);
    }

    public Map<IWrapperBlockState, ModelResourceLocation> putAllStateModelLocations() {
        Map<IWrapperBlockState, ModelResourceLocation> map = Maps
            .<IWrapperBlockState, ModelResourceLocation>newIdentityHashMap();

        for (Block block : Block.REGISTRY) {
            map.putAll(this.getVariants(block));
        }

        return map;
    }

    public Set<ResourceLocation> getBlockstateLocations(Block blockIn) {
        if (this.setBuiltInBlocks.contains(blockIn)) {
            return Collections.<ResourceLocation>emptySet();
        } else {
            IStateMapper istatemapper = this.blockStateMap.get(blockIn);

            if (istatemapper == null) {
                return Collections.<ResourceLocation>singleton(Block.REGISTRY.getNameForObject(blockIn));
            } else {
                Set<ResourceLocation> set = Sets.<ResourceLocation>newHashSet();

                for (ModelResourceLocation modelresourcelocation : istatemapper.putStateModelLocations(blockIn)
                    .values()) {
                    set.add(
                        new ResourceLocation(
                            modelresourcelocation.getResourceDomain(),
                            modelresourcelocation.getResourcePath()));
                }

                return set;
            }
        }
    }

    public Map<IWrapperBlockState, ModelResourceLocation> getVariants(Block blockIn) {
        return this.setBuiltInBlocks.contains(blockIn) ? Collections.emptyMap()
            : ((IStateMapper) MoreObjects.firstNonNull(this.blockStateMap.get(blockIn), new DefaultStateMapper()))
                .putStateModelLocations(blockIn);
    }
}
