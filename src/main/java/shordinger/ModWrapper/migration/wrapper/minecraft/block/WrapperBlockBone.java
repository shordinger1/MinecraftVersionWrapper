package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public class WrapperBlockBone extends WrapperBlockRotatedPillar {

    public WrapperBlockBone() {
        super(Material.ROCK, MapColor.SAND);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        this.setHardness(2.0F);
        this.setSoundType(SoundType.STONE);
    }
}
