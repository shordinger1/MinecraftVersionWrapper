package shordinger.ModWrapper.migration.wrapper.minecraft.client.renderer.block.statemap;

import net.minecraft.block.Block;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DefaultStateMapper extends StateMapperBase {

    protected ModelResourceLocation getModelResourceLocation(IWrapperBlockState state) {
        return new ModelResourceLocation(
            Block.REGISTRY.getNameForObject(state.getBlock()),
            this.getPropertyString(state.getProperties()));
    }
}
