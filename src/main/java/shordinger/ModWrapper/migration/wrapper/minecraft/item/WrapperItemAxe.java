package shordinger.ModWrapper.migration.wrapper.minecraft.item;

import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.init.Blocks;

import com.google.common.collect.Sets;

public class WrapperItemAxe extends WrapperItemTool {

    private static final Set<Block> EFFECTIVE_ON = Sets.newHashSet(
        Blocks.PLANKS,
        Blocks.BOOKSHELF,
        Blocks.LOG,
        Blocks.LOG2,
        Blocks.CHEST,
        Blocks.PUMPKIN,
        Blocks.LIT_PUMPKIN,
        Blocks.MELON_BLOCK,
        Blocks.LADDER,
        Blocks.WOODEN_BUTTON,
        Blocks.WOODEN_PRESSURE_PLATE);
    private static final float[] ATTACK_DAMAGES = new float[] { 6.0F, 8.0F, 8.0F, 8.0F, 6.0F };
    private static final float[] ATTACK_SPEEDS = new float[] { -3.2F, -3.2F, -3.1F, -3.0F, -3.0F };

    protected WrapperItemAxe(WrapperItem.ToolMaterial material) {
        super(material, EFFECTIVE_ON);
        this.attackDamage = ATTACK_DAMAGES[material.ordinal()];
        this.attackSpeed = ATTACK_SPEEDS[material.ordinal()];
    }

    protected WrapperItemAxe(WrapperItem.ToolMaterial material, float damage, float speed) {
        super(material, EFFECTIVE_ON);
        this.attackDamage = damage;
        this.attackSpeed = speed;
    }

    public float getDestroySpeed(TempItemStack stack, IWrapperBlockState state) {
        Material material = state.getMaterial();
        return material != Material.WOOD && material != Material.PLANTS && material != Material.VINE
            ? super.getDestroySpeed(stack, state)
            : this.efficiency;
    }
}
