package shordinger.ModWrapper.migration.wrapper.minecraft.item;

import javax.annotation.Nullable;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class WrapperItemBow extends WrapperItem {

    public WrapperItemBow() {
        this.maxStackSize = 1;
        this.setMaxDamage(384);
        this.setCreativeTab(CreativeTabs.COMBAT);
        this.addPropertyOverride(new ResourceLocation("pull"), new IItemPropertyGetter() {

            @SideOnly(Side.CLIENT)
            public float apply(TempItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
                if (entityIn == null) {
                    return 0.0F;
                } else {
                    return !(entityIn.getActiveItemStack()
                        .getItem() instanceof WrapperItemBow) ? 0.0F
                            : (float) (stack.getMaxItemUseDuration() - entityIn.getItemInUseCount()) / 20.0F;
                }
            }
        });
        this.addPropertyOverride(new ResourceLocation("pulling"), new IItemPropertyGetter() {

            @SideOnly(Side.CLIENT)
            public float apply(TempItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
                return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F
                    : 0.0F;
            }
        });
    }

    protected TempItemStack findAmmo(EntityPlayer player) {
        if (this.isArrow(player.getHeldItem(EnumHand.OFF_HAND))) {
            return player.getHeldItem(EnumHand.OFF_HAND);
        } else if (this.isArrow(player.getHeldItem(EnumHand.MAIN_HAND))) {
            return player.getHeldItem(EnumHand.MAIN_HAND);
        } else {
            for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
                TempItemStack itemstack = player.inventory.getStackInSlot(i);

                if (this.isArrow(itemstack)) {
                    return itemstack;
                }
            }

            return TempItemStack.EMPTY;
        }
    }

    protected boolean isArrow(TempItemStack stack) {
        return stack.getItem() instanceof WrapperItemArrow;
    }

    /**
     * Called when the player stops using an Item (stops holding the right mouse button).
     */
    public void onPlayerStoppedUsing(TempItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
        if (entityLiving instanceof EntityPlayer) {
            EntityPlayer entityplayer = (EntityPlayer) entityLiving;
            boolean flag = entityplayer.capabilities.isCreativeMode
                || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, stack) > 0;
            TempItemStack itemstack = this.findAmmo(entityplayer);

            int i = this.getMaxItemUseDuration(stack) - timeLeft;
            i = net.minecraftforge.event.ForgeEventFactory
                .onArrowLoose(stack, worldIn, entityplayer, i, !itemstack.isEmpty() || flag);
            if (i < 0) return;

            if (!itemstack.isEmpty() || flag) {
                if (itemstack.isEmpty()) {
                    itemstack = new TempItemStack(Items.ARROW);
                }

                float f = getArrowVelocity(i);

                if ((double) f >= 0.1D) {
                    boolean flag1 = entityplayer.capabilities.isCreativeMode
                        || (itemstack.getItem() instanceof WrapperItemArrow
                            && ((WrapperItemArrow) itemstack.getItem()).isInfinite(itemstack, stack, entityplayer));

                    if (!worldIn.isRemote) {
                        WrapperItemArrow itemarrow = (WrapperItemArrow) (itemstack.getItem() instanceof WrapperItemArrow
                            ? itemstack.getItem()
                            : Items.ARROW);
                        EntityArrow entityarrow = itemarrow.createArrow(worldIn, itemstack, entityplayer);
                        entityarrow = this.customizeArrow(entityarrow);
                        entityarrow.shoot(
                            entityplayer,
                            entityplayer.rotationPitch,
                            entityplayer.rotationYaw,
                            0.0F,
                            f * 3.0F,
                            1.0F);

                        if (f == 1.0F) {
                            entityarrow.setIsCritical(true);
                        }

                        int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack);

                        if (j > 0) {
                            entityarrow.setDamage(entityarrow.getDamage() + (double) j * 0.5D + 0.5D);
                        }

                        int k = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, stack);

                        if (k > 0) {
                            entityarrow.setKnockbackStrength(k);
                        }

                        if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, stack) > 0) {
                            entityarrow.setFire(100);
                        }

                        stack.damageItem(1, entityplayer);

                        if (flag1
                            || entityplayer.capabilities.isCreativeMode && (itemstack.getItem() == Items.SPECTRAL_ARROW
                                || itemstack.getItem() == Items.TIPPED_ARROW)) {
                            entityarrow.pickupStatus = EntityArrow.PickupStatus.CREATIVE_ONLY;
                        }

                        worldIn.spawnEntity(entityarrow);
                    }

                    worldIn.playSound(
                        (EntityPlayer) null,
                        entityplayer.posX,
                        entityplayer.posY,
                        entityplayer.posZ,
                        SoundEvents.ENTITY_ARROW_SHOOT,
                        SoundCategory.PLAYERS,
                        1.0F,
                        1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + f * 0.5F);

                    if (!flag1 && !entityplayer.capabilities.isCreativeMode) {
                        itemstack.shrink(1);

                        if (itemstack.isEmpty()) {
                            entityplayer.inventory.deleteStack(itemstack);
                        }
                    }

                    entityplayer.addStat(StatList.getObjectUseStats(this));
                }
            }
        }
    }

    /**
     * Gets the velocity of the arrow entity from the bow's charge
     */
    public static float getArrowVelocity(int charge) {
        float f = (float) charge / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;

        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    /**
     * How long it takes to use or consume an item
     */
    public int getMaxItemUseDuration(TempItemStack stack) {
        return 72000;
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    public EnumAction getItemUseAction(TempItemStack stack) {
        return EnumAction.BOW;
    }

    /**
     * Called when the equipped item is right clicked.
     */
    public ActionResult<TempItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        TempItemStack itemstack = playerIn.getHeldItem(handIn);
        boolean flag = !this.findAmmo(playerIn)
            .isEmpty();

        ActionResult<TempItemStack> ret = net.minecraftforge.event.ForgeEventFactory
            .onArrowNock(itemstack, worldIn, playerIn, handIn, flag);
        if (ret != null) return ret;

        if (!playerIn.capabilities.isCreativeMode && !flag) {
            return flag ? new ActionResult(EnumActionResult.PASS, itemstack)
                : new ActionResult(EnumActionResult.FAIL, itemstack);
        } else {
            playerIn.setActiveHand(handIn);
            return new ActionResult<TempItemStack>(EnumActionResult.SUCCESS, itemstack);
        }
    }

    /**
     * Return the enchantability factor of the item, most of the time is based on material.
     */
    public int getItemEnchantability() {
        return 1;
    }

    public EntityArrow customizeArrow(EntityArrow arrow) {
        return arrow;
    }
}
