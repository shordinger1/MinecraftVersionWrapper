package shordinger.ModWrapper.migration.wrapper.minecraft.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class WrapperItemWrittenBook extends WrapperItem {

    public WrapperItemWrittenBook() {
        this.setMaxStackSize(1);
    }

    public static boolean validBookTagContents(NBTTagCompound nbt) {
        if (!WrapperItemWritableBook.isNBTValid(nbt)) {
            return false;
        } else if (!nbt.hasKey("title", 8)) {
            return false;
        } else {
            String s = nbt.getString("title");
            return s != null && s.length() <= 32 ? nbt.hasKey("author", 8) : false;
        }
    }

    /**
     * Gets the generation of the book (how many times it has been cloned)
     */
    public static int getGeneration(TempItemStack book) {
        return book.getTagCompound()
            .getInteger("generation");
    }

    public String getItemStackDisplayName(TempItemStack stack) {
        if (stack.hasTagCompound()) {
            NBTTagCompound nbttagcompound = stack.getTagCompound();
            String s = nbttagcompound.getString("title");

            if (!StringUtils.isNullOrEmpty(s)) {
                return s;
            }
        }

        return super.getItemStackDisplayName(stack);
    }

    /**
     * allows items to add custom lines of information to the mouseover description
     */
    @SideOnly(Side.CLIENT)
    public void addInformation(TempItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (stack.hasTagCompound()) {
            NBTTagCompound nbttagcompound = stack.getTagCompound();
            String s = nbttagcompound.getString("author");

            if (!StringUtils.isNullOrEmpty(s)) {
                tooltip.add(TextFormatting.GRAY + I18n.translateToLocalFormatted("book.byAuthor", s));
            }

            tooltip.add(
                TextFormatting.GRAY
                    + I18n.translateToLocal("book.generation." + nbttagcompound.getInteger("generation")));
        }
    }

    /**
     * Called when the equipped item is right clicked.
     */
    public ActionResult<TempItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        TempItemStack itemstack = playerIn.getHeldItem(handIn);

        if (!worldIn.isRemote) {
            this.resolveContents(itemstack, playerIn);
        }

        playerIn.openBook(itemstack, handIn);
        playerIn.addStat(StatList.getObjectUseStats(this));
        return new ActionResult<TempItemStack>(EnumActionResult.SUCCESS, itemstack);
    }

    private void resolveContents(TempItemStack stack, EntityPlayer player) {
        if (stack.getTagCompound() != null) {
            NBTTagCompound nbttagcompound = stack.getTagCompound();

            if (!nbttagcompound.getBoolean("resolved")) {
                nbttagcompound.setBoolean("resolved", true);

                if (validBookTagContents(nbttagcompound)) {
                    NBTTagList nbttaglist = nbttagcompound.getTagList("pages", 8);

                    for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                        String s = nbttaglist.getStringTagAt(i);
                        ITextComponent itextcomponent;

                        try {
                            itextcomponent = ITextComponent.Serializer.fromJsonLenient(s);
                            itextcomponent = TextComponentUtils.processComponent(player, itextcomponent, player);
                        } catch (Exception var9) {
                            itextcomponent = new TextComponentString(s);
                        }

                        nbttaglist.set(i, new NBTTagString(ITextComponent.Serializer.componentToJson(itextcomponent)));
                    }

                    nbttagcompound.setTag("pages", nbttaglist);

                    if (player instanceof EntityPlayerMP && player.getHeldItemMainhand() == stack) {
                        Slot slot = player.openContainer
                            .getSlotFromInventory(player.inventory, player.inventory.currentItem);
                        ((EntityPlayerMP) player).connection.sendPacket(new SPacketSetSlot(0, slot.slotNumber, stack));
                    }
                }
            }
        }
    }

    /**
     * Returns true if this item has an enchantment glint. By default, this returns
     * <code>stack.isItemEnchanted()</code>, but other items can override it (for instance, written books always return
     * true).
     *
     * Note that if you override this method, you generally want to also call the super version (on {@link WrapperItem}) to get
     * the glint for enchanted items. Of course, that is unnecessary if the overwritten version always returns true.
     */
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(TempItemStack stack) {
        return true;
    }
}
