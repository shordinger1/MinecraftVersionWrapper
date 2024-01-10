package shordinger.ModWrapper.migration.wrapper.minecraft.item;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.block.BlockJukebox;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import com.google.common.collect.Maps;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WrapperItemRecord extends WrapperItem {

    private static final Map<SoundEvent, WrapperItemRecord> RECORDS = Maps.<SoundEvent, WrapperItemRecord>newHashMap();
    private final SoundEvent sound;
    private final String displayName;

    protected WrapperItemRecord(String recordName, SoundEvent soundIn) {
        this.displayName = "item.record." + recordName + ".desc";
        this.sound = soundIn;
        this.maxStackSize = 1;
        this.setCreativeTab(CreativeTabs.MISC);
        RECORDS.put(this.sound, this);
    }

    /**
     * Called when a Block is right-clicked with this Item
     */
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
        EnumFacing facing, float hitX, float hitY, float hitZ) {
        IWrapperBlockState iblockstate = worldIn.getBlockState(pos);

        if (iblockstate.getBlock() == Blocks.JUKEBOX
            && !((Boolean) iblockstate.getValue(BlockJukebox.HAS_RECORD)).booleanValue()) {
            if (!worldIn.isRemote) {
                TempItemStack itemstack = player.getHeldItem(hand);
                ((BlockJukebox) Blocks.JUKEBOX).insertRecord(worldIn, pos, iblockstate, itemstack);
                worldIn.playEvent((EntityPlayer) null, 1010, pos, WrapperItem.getIdFromItem(this));
                itemstack.shrink(1);
                player.addStat(StatList.RECORD_PLAYED);
            }

            return EnumActionResult.SUCCESS;
        } else {
            return EnumActionResult.PASS;
        }
    }

    /**
     * allows items to add custom lines of information to the mouseover description
     */
    @SideOnly(Side.CLIENT)
    public void addInformation(TempItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(this.getRecordNameLocal());
    }

    @SideOnly(Side.CLIENT)
    public String getRecordNameLocal() {
        return I18n.translateToLocal(this.displayName);
    }

    /**
     * Return an item rarity from EnumRarity
     */
    public EnumRarity getRarity(TempItemStack stack) {
        return EnumRarity.RARE;
    }

    @Nullable
    @SideOnly(Side.CLIENT)
    public static WrapperItemRecord getBySound(SoundEvent soundIn) {
        return RECORDS.get(soundIn);
    }

    @SideOnly(Side.CLIENT)
    public SoundEvent getSound() {
        return this.sound;
    }
}
