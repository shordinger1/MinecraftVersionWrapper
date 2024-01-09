package shordinger.ModWrapper.migration.wrapper.minecraft.client.renderer.block.statemap;

import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;

import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class StateMapperBase implements IStateMapper {

    protected Map<IWrapperBlockState, ModelResourceLocation> mapStateModelLocations = Maps
        .<IWrapperBlockState, ModelResourceLocation>newLinkedHashMap();

    public String getPropertyString(Map<IProperty<?>, Comparable<?>> values) {
        StringBuilder stringbuilder = new StringBuilder();

        for (Entry<IProperty<?>, Comparable<?>> entry : values.entrySet()) {
            if (stringbuilder.length() != 0) {
                stringbuilder.append(",");
            }

            IProperty<?> iproperty = (IProperty) entry.getKey();
            stringbuilder.append(iproperty.getName());
            stringbuilder.append("=");
            stringbuilder.append(this.getPropertyName(iproperty, entry.getValue()));
        }

        if (stringbuilder.length() == 0) {
            stringbuilder.append("normal");
        }

        return stringbuilder.toString();
    }

    private <T extends Comparable<T>> String getPropertyName(IProperty<T> property, Comparable<?> value) {
        return property.getName((T) value);
    }

    public Map<IWrapperBlockState, ModelResourceLocation> putStateModelLocations(Block blockIn) {
        UnmodifiableIterator unmodifiableiterator = blockIn.getBlockState()
            .getValidStates()
            .iterator();

        while (unmodifiableiterator.hasNext()) {
            IWrapperBlockState iblockstate = (IWrapperBlockState) unmodifiableiterator.next();
            this.mapStateModelLocations.put(iblockstate, this.getModelResourceLocation(iblockstate));
        }

        return this.mapStateModelLocations;
    }

    protected abstract ModelResourceLocation getModelResourceLocation(IWrapperBlockState state);
}
