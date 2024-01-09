package shordinger.ModWrapper.migration.wrapper.minecraft.client.renderer.block.statemap;

import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IStateMapper {

    Map<IWrapperBlockState, ModelResourceLocation> putStateModelLocations(Block blockIn);
}
