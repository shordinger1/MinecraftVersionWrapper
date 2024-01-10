package shordinger.ModWrapper.migration.wrapper.minecraft.item;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shordinger.ModWrapper.migration.wrapper.minecraft.block.state.IBlockState;
import shordinger.ModWrapper.migration.wrapper.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import shordinger.ModWrapper.migration.wrapper.minecraft.client.util.ITooltipFlag;
import shordinger.ModWrapper.migration.wrapper.minecraft.init.WrapperBlocks;
import shordinger.ModWrapper.migration.wrapper.minecraft.init.WrapperItems;
import shordinger.ModWrapper.migration.wrapper.minecraft.inventory.EntityEquipmentSlot;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.ActionResult;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.EnumActionResult;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.EnumHand;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.NonNullList;;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.MathHelper;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.RayTraceResult;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.Vec3d;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.registry.IRegistry;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.registry.RegistrySimple;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.registry.WrapperRegistryNamespaced;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.text.translation.I18n;
import shordinger.ModWrapper.migration.wrapper.minecraft.world.World;
import shordinger.ModWrapper.migration.wrapper.minecraftforge.common.IRarity;
import shordinger.ModWrapper.migration.wrapper.minecraftforge.common.capabilities.ICapabilityProvider;

public class WrapperItem extends Item {

    public static final WrapperRegistryNamespaced<ResourceLocation, WrapperItem> REGISTRY = (WrapperRegistryNamespaced<ResourceLocation, WrapperItem>) itemRegistry;
    private static final Map<Block, WrapperItem> BLOCK_TO_ITEM = net.minecraftforge.registries.GameData.getBlockItemMap();
    private static final IItemPropertyGetter DAMAGED_GETTER = new IItemPropertyGetter() {

        @SideOnly(Side.CLIENT)
        public float apply(TempItemStack stack, @Nullable net.minecraft.world.World worldIn, @Nullable EntityLivingBase entityIn) {
            return stack.isItemDamaged() ? 1.0F : 0.0F;
        }
    };
    private static final IItemPropertyGetter DAMAGE_GETTER = new IItemPropertyGetter() {

        @SideOnly(Side.CLIENT)
        public float apply(TempItemStack stack, @Nullable net.minecraft.world.World worldIn, @Nullable EntityLivingBase entityIn) {
            return MathHelper.clamp((float) stack.getItemDamage() / (float) stack.getMaxDamage(), 0.0F, 1.0F);
        }
    };
    private static final IItemPropertyGetter LEFTHANDED_GETTER = new IItemPropertyGetter() {

        @SideOnly(Side.CLIENT)
        public float apply(TempItemStack stack, @Nullable net.minecraft.world.World worldIn, @Nullable EntityLivingBase entityIn) {
            return entityIn != null && entityIn.getPrimaryHand() != EnumHandSide.RIGHT ? 1.0F : 0.0F;
        }
    };
    private static final IItemPropertyGetter COOLDOWN_GETTER = new IItemPropertyGetter() {

        @SideOnly(Side.CLIENT)
        public float apply(TempItemStack stack, @Nullable net.minecraft.world.World worldIn, @Nullable EntityLivingBase entityIn) {
            return entityIn instanceof EntityPlayer ? ((EntityPlayer) entityIn).getCooldownTracker()
                .getCooldown(stack.getItem(), 0.0F) : 0.0F;
        }
    };
    private final IRegistry<ResourceLocation, IItemPropertyGetter> properties = new RegistrySimple<ResourceLocation, IItemPropertyGetter>();
    protected static final UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    protected static final UUID ATTACK_SPEED_MODIFIER = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
    private CreativeTabs tabToDisplayOn;
    /**
     * The RNG used by the Item subclasses.
     */
    protected static Random itemRand = new Random();
    /**
     * Maximum size of the stack.
     */
    protected int maxStackSize = Integer.MAX_VALUE;
    /**
     * Maximum damage an item can handle.
     */
    private int maxDamage;
    /**
     * If true, render the object in full 3D, like weapons and tools.
     */
    protected boolean bFull3D;
    /**
     * Some items (like dyes) have multiple subtypes on same item, this is field define this behavior
     */
    protected boolean hasSubtypes;
    private WrapperItem containerWrapperItem;
    /**
     * The unlocalized name of this item.
     */
    private String unlocalizedName;

    public static int getIdFromItem(WrapperItem wrapperItemIn) {
        return REGISTRY.getIDForObject(wrapperItemIn);
    }

    public static WrapperItem getItemById(int id) {
        return REGISTRY.getObjectById(id);
    }

    public static WrapperItem getItemFromBlock(Block blockIn) {
        WrapperItem wrapperItem = BLOCK_TO_ITEM.get(blockIn);
        return wrapperItem == null ? WrapperItems.AIR : wrapperItem;
    }

    /**
     * Tries to get an Item by it's name (e.g. minecraft:apple) or a String representation of a numerical ID. If both
     * fail, null is returned.
     */
    @Nullable
    public static WrapperItem getByNameOrId(String id) {
        WrapperItem wrapperItem = REGISTRY.getObject(new ResourceLocation(id));

        if (wrapperItem == null) {
            try {
                return getItemById(Integer.parseInt(id));
            } catch (NumberFormatException ignored) {
                ;
            }
        }

        return wrapperItem;
    }

    /**
     * Creates a new override param for item models. See usage in clock, compass, elytra, etc.
     */
    public final void addPropertyOverride(ResourceLocation key, IItemPropertyGetter getter) {
        this.properties.putObject(key, getter);
    }

    @Nullable
    @SideOnly(Side.CLIENT)
    public IItemPropertyGetter getPropertyGetter(ResourceLocation key) {
        return this.properties.getObject(key);
    }

    /**
     * Called when an ItemStack with NBT data is read to potentially that ItemStack's NBT data
     */
    public boolean updateItemStackNBT(NBTTagCompound nbt) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public boolean hasCustomProperties() {
        return !this.properties.getKeys()
            .isEmpty();
    }

    public WrapperItem() {
        this.addPropertyOverride(new ResourceLocation("lefthanded"), LEFTHANDED_GETTER);
        this.addPropertyOverride(new ResourceLocation("cooldown"), COOLDOWN_GETTER);
    }

    public WrapperItem setMaxStackSize(int maxStackSize) {
        this.maxStackSize = maxStackSize;
        return this;
    }

    /**
     * Called when a Block is right-clicked with this Item
     */
    public EnumActionResult onItemUse(EntityPlayer player, net.minecraft.world.World worldIn, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ) {
        return EnumActionResult.PASS;
    }

    public float getDestroySpeed(TempItemStack stack, IBlockState state) {
        return 1.0F;
    }

    /**
     * Called when the equipped item is right clicked.
     */
    public ActionResult<TempItemStack> onItemRightClick(net.minecraft.world.World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        return new ActionResult<TempItemStack>(EnumActionResult.PASS, TempItemStack.getTempItemStack(playerIn.getHeldItem()));
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStackIn, net.minecraft.world.World worldIn, EntityPlayer player) {
        return onItemRightClick(worldIn,player,null).getResult().getItemStack();
    }

    /**
     * Called when the player finishes using this Item (E.g. finishes eating.). Not called when the player stops using
     * the Item before the action is complete.
     */
    public TempItemStack onItemUseFinish(TempItemStack stack, net.minecraft.world.World worldIn, EntityLivingBase entityLiving) {
        return stack;
    }

    /**
     * Returns the maximum size of the stack for a specific item. *Isn't this more a Set than a Get?*
     */
    @Deprecated // Use ItemStack sensitive version below.
    public int getItemStackLimit() {
        return (int) this.maxStackSize;
    }

    /**
     * Converts the given ItemStack damage value into a metadata value to be placed in the world when this Item is
     * placed as a Block (mostly used with ItemBlocks).
     */
    public int getMetadata(int damage) {
        return 0;
    }

    public boolean getHasSubtypes() {
        return this.hasSubtypes;
    }

    public WrapperItem setHasSubtypes(boolean hasSubtypes) {
        this.hasSubtypes = hasSubtypes;
        return this;
    }

    /**
     * Returns the maximum damage an item can take.
     */
    @Deprecated
    public int getMaxDamage() {
        return this.maxDamage;
    }

    /**
     * set max damage of an Item
     */
    public WrapperItem setMaxDamage(int maxDamageIn) {
        this.maxDamage = maxDamageIn;

        if (maxDamageIn > 0) {
            this.addPropertyOverride(new ResourceLocation("damaged"), DAMAGED_GETTER);
            this.addPropertyOverride(new ResourceLocation("damage"), DAMAGE_GETTER);
        }

        return this;
    }

    public boolean isDamageable() {
        return this.maxDamage > 0 && (!this.hasSubtypes || this.maxStackSize == 1);
    }

    /**
     * Current implementations of this method in child classes do not use the entry argument beside ev. They just raise
     * the damage on the stack.
     */
    public boolean hitEntity(TempItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        return false;
    }

    /**
     * Called when a Block is destroyed using this Item. Return true to trigger the "Use Item" statistic.
     */
    public boolean onBlockDestroyed(TempItemStack stack, net.minecraft.world.World worldIn, IBlockState state, BlockPos pos,
                                    EntityLivingBase entityLiving) {
        return false;
    }

    /**
     * Check whether this Item can harvest the given Block
     */
    public boolean canHarvestBlock(IBlockState blockIn) {
        return false;
    }

    /**
     * Returns true if the item can be used on the given entity, e.g. shears on sheep.
     */
    public boolean itemInteractionForEntity(TempItemStack stack, EntityPlayer playerIn, EntityLivingBase target,
                                            EnumHand hand) {
        return false;
    }

    /**
     * Sets bFull3D to True and return the object.
     */
    public WrapperItem setFull3D() {
        this.bFull3D = true;
        return this;
    }

    /**
     * Returns True is the item is renderer in full 3D when hold.
     */
    @SideOnly(Side.CLIENT)
    public boolean isFull3D() {
        return this.bFull3D;
    }

    /**
     * Returns true if this item should be rotated by 180 degrees around the Y axis when being held in an entities
     * hands.
     */
    @SideOnly(Side.CLIENT)
    public boolean shouldRotateAroundWhenRendering() {
        return false;
    }




    @Nullable
    public WrapperItem getContainerItem() {
        return this.containerWrapperItem;
    }


    /**
     * Called each tick as long the item is on a player inventory. Uses by maps to check if is on a player hand and
     * update it's contents.
     */
    public void onUpdate(TempItemStack stack, net.minecraft.world.World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
    }

    @Override
    public void onUpdate(ItemStack stack, net.minecraft.world.World worldIn, Entity entityIn, int p_77663_4_, boolean p_77663_5_) {
        onUpdate(TempItemStack.getTempItemStack(stack),worldIn,entityIn,p_77663_4_,p_77663_5_);
    }

    /**
     * Called when item is crafted/smelted. Used only by maps so far.
     */
    public void onCreated(TempItemStack stack, net.minecraft.world.World worldIn, EntityPlayer playerIn) {

    }

    @Override
    public void onCreated(ItemStack p_77622_1_, net.minecraft.world.World p_77622_2_, EntityPlayer p_77622_3_) {
        onCreated(TempItemStack.getTempItemStack(p_77622_1_), p_77622_2_, p_77622_3_);
    }


    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    public EnumAction getItemUseAction(TempItemStack stack) {
        return EnumAction.NONE;
    }


    /**
     * Called when the player stops using an Item (stops holding the right mouse button).
     */
    public void onPlayerStoppedUsing(TempItemStack stack, net.minecraft.world.World worldIn, EntityLivingBase entityLiving, int timeLeft) {
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack p_77615_1_, net.minecraft.world.World p_77615_2_, EntityPlayer p_77615_3_, int p_77615_4_) {
        onPlayerStoppedUsing(TempItemStack.getTempItemStack(p_77615_1_), p_77615_2_, p_77615_3_, p_77615_4_);
    }

    /**
     * allows items to add custom lines of information to the mouseover description
     */
    @SideOnly(Side.CLIENT)
    public void addInformation(TempItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
    }

    public String getItemStackDisplayName(TempItemStack stack) {
        return I18n.translateToLocal(this.getUnlocalizedNameInefficiently(stack) + ".name")
            .trim();
    }

    /**
     * Returns true if this item has an enchantment glint. By default, this returns
     * <code>stack.isItemEnchanted()</code>, but other items can override it (for instance, written books always return
     * true).
     * <p>
     * Note that if you override this method, you generally want to also call the super version (on {@link WrapperItem}) to get
     * the glint for enchanted items. Of course, that is unnecessary if the overwritten version always returns true.
     */
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(TempItemStack stack) {
        return stack.isItemEnchanted();
    }

    /**
     * Return an item rarity from EnumRarity
     */
    @Deprecated // use Forge version
    public EnumRarity getRarity(TempItemStack stack) {
        return stack.isItemEnchanted() ? EnumRarity.RARE : EnumRarity.COMMON;
    }

    /**
     * Checks isDamagable and if it cannot be stacked
     */
    public boolean isEnchantable(TempItemStack stack) {
        return this.getItemStackLimit(stack) == 1 && this.isDamageable();
    }

    protected RayTraceResult rayTrace(World worldIn, EntityPlayer playerIn, boolean useLiquids) {
        float f = playerIn.rotationPitch;
        float f1 = playerIn.rotationYaw;
        double d0 = playerIn.posX;
        double d1 = playerIn.posY + (double) playerIn.getEyeHeight();
        double d2 = playerIn.posZ;
        Vec3d vec3d = new Vec3d(d0, d1, d2);
        float f2 = MathHelper.cos(-f1 * 0.017453292F - (float) Math.PI);
        float f3 = MathHelper.sin(-f1 * 0.017453292F - (float) Math.PI);
        float f4 = -MathHelper.cos(-f * 0.017453292F);
        float f5 = MathHelper.sin(-f * 0.017453292F);
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        double d3 = playerIn.getEntityAttribute(EntityPlayer.REACH_DISTANCE)
            .getAttributeValue();
        Vec3d vec3d1 = vec3d.addVector((double) f6 * d3, (double) f5 * d3, (double) f7 * d3);
        return worldIn.rayTraceBlocks(vec3d, vec3d1, useLiquids, !useLiquids, false);
    }


    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    public void getSubItems(CreativeTabs tab, NonNullList<TempItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            items.add(new TempItemStack(this));
        }
    }

    public boolean isInCreativeTab(CreativeTabs targetTab) {
        for (CreativeTabs tab : this.getCreativeTabs()) if (tab == targetTab) return true;
        CreativeTabs creativetabs = this.getCreativeTab();
        return creativetabs != null && (targetTab == CreativeTabs.SEARCH || targetTab == creativetabs);
    }

    /**
     * gets the CreativeTab this item is displayed on
     */
    @Nullable
    public CreativeTabs getCreativeTab() {
        return this.tabToDisplayOn;
    }

    /**
     * returns this;
     */
    public WrapperItem setCreativeTab(CreativeTabs tab) {
        this.tabToDisplayOn = tab;
        return this;
    }

    /**
     * Returns whether this item is always allowed to edit the world. Forces {@link
     * net.minecraft.entity.player.EntityPlayer#canPlayerEdit EntityPlayer#canPlayerEdit} to return {@code true}.
     *
     * @return whether this item ignores other restrictions on how a player can modify the world.
     * @see TempItemStack#canEditBlocks
     */
    public boolean canItemEditBlocks() {
        return false;
    }

    /**
     * Return whether this item is repairable in an anvil.
     *
     * @param toRepair the {@code ItemStack} being repaired
     * @param repair   the {@code ItemStack} being used to perform the repair
     */
    public boolean getIsRepairable(TempItemStack toRepair, TempItemStack repair) {
        return true;
    }

    /**
     * Gets a map of item attribute modifiers, used by ItemSword to increase hit damage.
     */
    @Deprecated // Use ItemStack sensitive version below.
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
        return HashMultimap.<String, AttributeModifier>create();
    }

    /* ======================================== FORGE START ===================================== */

    /**
     * ItemStack sensitive version of getItemAttributeModifiers
     */
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, TempItemStack stack) {
        return this.getItemAttributeModifiers(slot);
    }

    /**
     * Called when a player drops the item into the world,
     * returning false from this will prevent the item from
     * being removed from the players inventory and spawning
     * in the world
     *
     * @param player The player that dropped the item
     * @param item   The item stack, before the item is removed.
     */
    public boolean onDroppedByPlayer(TempItemStack item, EntityPlayer player) {
        return true;
    }

    /**
     * Allow the item one last chance to modify its name used for the
     * tool highlight useful for adding something extra that can't be removed
     * by a user in the displayed name, such as a mode of operation.
     *
     * @param item        the ItemStack for the item.
     * @param displayName the name that will be displayed unless it is changed in this method.
     */
    public String getHighlightTip(TempItemStack item, String displayName) {
        return displayName;
    }

    /**
     * This is called when the item is used, before the block is activated.
     *
     * @param stack  The Item Stack
     * @param player The Player that used the item
     * @param world  The Current World
     * @param pos    Target position
     * @param side   The side of the target hit
     * @param hand   Which hand the item is being held in.
     * @return Return PASS to allow vanilla handling, any other to skip normal code.
     */
    public EnumActionResult onItemUseFirst(EntityPlayer player, net.minecraft.world.World world, BlockPos pos, EnumFacing side, float hitX,
                                           float hitY, float hitZ, EnumHand hand) {
        return EnumActionResult.PASS;
    }

    protected boolean canRepair = true;

    /**
     * Called by CraftingManager to determine if an item is reparable.
     *
     * @return True if reparable
     */
    public boolean isRepairable() {
        return canRepair && isDamageable();
    }

    /**
     * Call to disable repair recipes.
     *
     * @return The current Item instance
     */
    public WrapperItem setNoRepair() {
        canRepair = false;
        return this;
    }

    /**
     * Determines the amount of durability the mending enchantment
     * will repair, on average, per point of experience.
     */
    public float getXpRepairRatio(TempItemStack stack) {
        return 2f;
    }

    /**
     * Override this method to change the NBT data being sent to the client.
     * You should ONLY override this when you have no other choice, as this might change behavior client side!
     * <p>
     * Note that this will sometimes be applied multiple times, the following MUST be supported:
     * Item item = stack.getItem();
     * NBTTagCompound nbtShare1 = item.getNBTShareTag(stack);
     * stack.setTagCompound(nbtShare1);
     * NBTTagCompound nbtShare2 = item.getNBTShareTag(stack);
     * assert nbtShare1.equals(nbtShare2);
     *
     * @param stack The stack to send the NBT tag for
     * @return The NBT tag
     */
    @Nullable
    public NBTTagCompound getNBTShareTag(TempItemStack stack) {
        return stack.getTagCompound();
    }

    /**
     * Override this method to decide what to do with the NBT data received from getNBTShareTag().
     *
     * @param stack The stack that received NBT
     * @param nbt   Received NBT, can be null
     */
    public void readNBTShareTag(TempItemStack stack, @Nullable NBTTagCompound nbt) {
        stack.setTagCompound(nbt);
    }

    /**
     * Called before a block is broken. Return true to prevent default block harvesting.
     * <p>
     * Note: In SMP, this is called on both client and server sides!
     *
     * @param itemstack The current ItemStack
     * @param pos       Block's position in world
     * @param player    The Player that is wielding the item
     * @return True to prevent harvesting, false to continue as normal
     */
    public boolean onBlockStartBreak(TempItemStack itemstack, BlockPos pos, EntityPlayer player) {
        return false;
    }

    /**
     * Called each tick while using an item.
     *
     * @param stack  The Item being used
     * @param player The Player using the item
     * @param count  The amount of time in tick the item has been used for continuously
     */
    public void onUsingTick(TempItemStack stack, EntityLivingBase player, int count) {
    }

    /**
     * Called when the player Left Clicks (attacks) an entity.
     * Processed before damage is done, if return value is true further processing is canceled
     * and the entity is not attacked.
     *
     * @param stack  The Item being used
     * @param player The player that is attacking
     * @param entity The entity being attacked
     * @return True to cancel the rest of the interaction.
     */
    public boolean onLeftClickEntity(TempItemStack stack, EntityPlayer player, Entity entity) {
        return false;
    }

    /**
     * ItemStack sensitive version of getContainerItem.
     * Returns a full ItemStack instance of the result.
     *
     * @param tempItemStack The current ItemStack
     * @return The resulting ItemStack
     */
    public TempItemStack getContainerItem(TempItemStack tempItemStack) {
        if (!hasContainerItem(tempItemStack)) {
            return TempItemStack.EMPTY;
        }
        return new TempItemStack(getContainerItem());
    }

    /**
     * ItemStack sensitive version of hasContainerItem
     *
     * @param stack The current item stack
     * @return True if this item has a 'container'
     */
    public boolean hasContainerItem(TempItemStack stack) {
        return hasContainerItem();
    }

    /**
     * Retrieves the normal 'lifespan' of this item when it is dropped on the ground as a EntityItem.
     * This is in ticks, standard result is 6000, or 5 mins.
     *
     * @param tempItemStack The current ItemStack
     * @param world         The world the entity is in
     * @return The normal lifespan in ticks.
     */
    public int getEntityLifespan(TempItemStack tempItemStack, net.minecraft.world.World world) {
        return 6000;
    }

    /**
     * Determines if this Item has a special entity for when they are in the world.
     * Is called when a EntityItem is spawned in the world, if true and Item#createCustomEntity
     * returns non null, the EntityItem will be destroyed and the new Entity will be added to the world.
     *
     * @param stack The current item stack
     * @return True of the item has a custom entity, If true, Item#createCustomEntity will be called
     */
    public boolean hasCustomEntity(TempItemStack stack) {
        return false;
    }

    /**
     * This function should return a new entity to replace the dropped item.
     * Returning null here will not kill the EntityItem and will leave it to function normally.
     * Called when the item it placed in a world.
     *
     * @param world     The world object
     * @param location  The EntityItem object, useful for getting the position of the entity
     * @param itemstack The current item stack
     * @return A new Entity object to spawn or null
     */
    @Nullable
    public Entity createEntity(net.minecraft.world.World world, Entity location, TempItemStack itemstack) {
        return null;
    }


    /**
     * Gets a list of tabs that items belonging to this class can display on,
     * combined properly with getSubItems allows for a single item to span
     * many sub-items across many tabs.
     *
     * @return A list of all tabs that this item could possibly be one.
     */
    public CreativeTabs[] getCreativeTabs() {
        return new CreativeTabs[]{getCreativeTab()};
    }

    /**
     * Determines the base experience for a player when they remove this item from a furnace slot.
     * This number must be between 0 and 1 for it to be valid.
     * This number will be multiplied by the stack size to get the total experience.
     *
     * @param item The item stack the player is picking up.
     * @return The amount to award for each item.
     */
    public float getSmeltingExperience(TempItemStack item) {
        return -1; // -1 will default to the old lookups.
    }

    /**
     * Should this item, when held, allow sneak-clicks to pass through to the underlying block?
     *
     * @param world  The world
     * @param pos    Block position in world
     * @param player The Player that is wielding the item
     * @return
     */
    public boolean doesSneakBypassUse(TempItemStack stack, net.minecraft.world.IBlockAccess world, BlockPos pos,
                                      EntityPlayer player) {
        return false;
    }

    /**
     * Called to tick armor in the armor slot. Override to do something
     */
    public void onArmorTick(net.minecraft.world.World world, EntityPlayer player, TempItemStack tempItemStack) {
    }

    /**
     * Determines if the specific ItemStack can be placed in the specified armor slot, for the entity.
     * <p>
     * TODO: Change name to canEquip in 1.13?
     *
     * @param stack     The ItemStack
     * @param armorType Armor slot to be verified.
     * @param entity    The entity trying to equip the armor
     * @return True if the given ItemStack can be inserted in the slot
     */
    public boolean isValidArmor(TempItemStack stack, EntityEquipmentSlot armorType, Entity entity) {
        return net.minecraft.entity.EntityLiving.getSlotForItemStack(stack) == armorType;
    }

    /**
     * Override this to set a non-default armor slot for an ItemStack, but
     * <em>do not use this to get the armor slot of said stack; for that, use
     * {@link net.minecraft.entity.EntityLiving#getSlotForItemStack(TempItemStack)}.</em>
     *
     * @param stack the ItemStack
     * @return the armor slot of the ItemStack, or {@code null} to let the default
     * vanilla logic as per {@code EntityLiving.getSlotForItemStack(stack)} decide
     */
    @Nullable
    public EntityEquipmentSlot getEquipmentSlot(TempItemStack stack) {
        return null;
    }

    /**
     * Allow or forbid the specific book/item combination as an anvil enchant
     *
     * @param stack The item
     * @param book  The book
     * @return if the enchantment is allowed
     */
    public boolean isBookEnchantable(TempItemStack stack, TempItemStack book) {
        return true;
    }

    /**
     * Called by RenderBiped and RenderPlayer to determine the armor texture that
     * should be use for the currently equipped item.
     * This will only be called on instances of ItemArmor.
     * <p>
     * Returning null from this function will use the default value.
     *
     * @param stack  ItemStack for the equipped armor
     * @param entity The entity wearing the armor
     * @param slot   The slot the armor is in
     * @param type   The subtype, can be null or "overlay"
     * @return Path of texture to bind, or null to use default
     */
    @Nullable
    public String getArmorTexture(TempItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return null;
    }

    /**
     * Returns the font renderer used to render tooltips and overlays for this item.
     * Returning null will use the standard font renderer.
     *
     * @param stack The current item stack
     * @return A instance of FontRenderer or null to use default
     */
    @SideOnly(Side.CLIENT)
    @Nullable
    public net.minecraft.client.gui.FontRenderer getFontRenderer(TempItemStack stack) {
        return null;
    }

    /**
     * Override this method to have an item handle its own armor rendering.
     *
     * @param entityLiving  The entity wearing the armor
     * @param tempItemStack The itemStack to render the model of
     * @param armorSlot     The slot the armor is in
     * @param _default      Original armor model. Will have attributes set.
     * @return A ModelBiped to render instead of the default
     */
    @SideOnly(Side.CLIENT)
    @Nullable
    public net.minecraft.client.model.ModelBiped getArmorModel(EntityLivingBase entityLiving, TempItemStack tempItemStack,
                                                               EntityEquipmentSlot armorSlot, net.minecraft.client.model.ModelBiped _default) {
        return null;
    }

    /**
     * Called when a entity tries to play the 'swing' animation.
     *
     * @param entityLiving The entity swinging the item.
     * @param stack        The Item stack
     * @return True to cancel any further processing by EntityLiving
     */
    public boolean onEntitySwing(EntityLivingBase entityLiving, TempItemStack stack) {
        return false;
    }

    /**
     * Called when the client starts rendering the HUD, for whatever item the player currently has as a helmet.
     * This is where pumpkins would render there overlay.
     *
     * @param stack        The ItemStack that is equipped
     * @param player       Reference to the current client entity
     * @param resolution   Resolution information about the current viewport and configured GUI Scale
     * @param partialTicks Partial ticks for the renderer, useful for interpolation
     */
    @SideOnly(Side.CLIENT)
    public void renderHelmetOverlay(TempItemStack stack, EntityPlayer player,
                                    net.minecraft.client.gui.ScaledResolution resolution, float partialTicks) {
    }

    /**
     * Return the itemDamage represented by this ItemStack. Defaults to the itemDamage field on ItemStack, but can be
     * overridden here for other sources such as NBT.
     *
     * @param stack The itemstack that is damaged
     * @return the damage value
     */
    public int getDamage(TempItemStack stack) {
        return stack.itemDamage;
    }

    /**
     * This used to be 'display damage' but its really just 'aux' data in the ItemStack, usually shares the same
     * variable as damage.
     *
     * @param stack
     * @return
     */
    public int getMetadata(TempItemStack stack) {
        return stack.itemDamage;
    }

    /**
     * Determines if the durability bar should be rendered for this item.
     * Defaults to vanilla stack.isDamaged behavior.
     * But modders can use this for any data they wish.
     *
     * @param stack The current Item Stack
     * @return True if it should render the 'durability' bar.
     */
    public boolean showDurabilityBar(TempItemStack stack) {
        return stack.isItemDamaged();
    }

    /**
     * Queries the percentage of the 'Durability' bar that should be drawn.
     *
     * @param stack The current ItemStack
     * @return 0.0 for 100% (no damage / full bar), 1.0 for 0% (fully damaged / empty bar)
     */
    public double getDurabilityForDisplay(TempItemStack stack) {
        return (double) stack.getItemDamage() / (double) stack.getMaxDamage();
    }

    /**
     * Returns the packed int RGB value used to render the durability bar in the GUI.
     * Defaults to a value based on the hue scaled based on {@link #getDurabilityForDisplay}, but can be overriden.
     *
     * @param stack Stack to get durability from
     * @return A packed RGB value for the durability colour (0x00RRGGBB)
     */
    public int getRGBDurabilityForDisplay(TempItemStack stack) {
        return MathHelper.hsvToRGB(Math.max(0.0F, (float) (1.0F - getDurabilityForDisplay(stack))) / 3.0F, 1.0F, 1.0F);
    }

    /**
     * Return the maxDamage for this ItemStack. Defaults to the maxDamage field in this item,
     * but can be overridden here for other sources such as NBT.
     *
     * @param stack The itemstack that is damaged
     * @return the damage value
     */
    public int getMaxDamage(TempItemStack stack) {
        return getMaxDamage();
    }

    /**
     * Return if this itemstack is damaged. Note only called if {@link #isDamageable()} is true.
     *
     * @param stack the stack
     * @return if the stack is damaged
     */
    public boolean isDamaged(TempItemStack stack) {
        return stack.itemDamage > 0;
    }

    /**
     * Set the damage for this itemstack. Note, this method is responsible for zero checking.
     *
     * @param stack  the stack
     * @param damage the new damage value
     */
    public void setDamage(TempItemStack stack, int damage) {
        stack.itemDamage = damage;

        if (stack.itemDamage < 0) {
            stack.itemDamage = 0;
        }
    }

    /**
     * Checked from {@link net.minecraft.client.multiplayer.PlayerControllerMP#onPlayerDestroyBlock(BlockPos pos)
     * PlayerControllerMP.onPlayerDestroyBlock()}
     * when a creative player left-clicks a block with this item.
     * Also checked from
     * {@link net.minecraftforge.common.ForgeHooks#onBlockBreakEvent(net.minecraft.world.World, GameType, EntityPlayerMP, BlockPos)
     * ForgeHooks.onBlockBreakEvent()}
     * to prevent sending an event.
     *
     * @return true if the given player can destroy specified block in creative mode with this item
     */
    public boolean canDestroyBlockInCreative(net.minecraft.world.World world, BlockPos pos, TempItemStack stack, EntityPlayer player) {
        return !(this instanceof WrapperItemSword);
    }

    /**
     * ItemStack sensitive version of {@link #canHarvestBlock(IBlockState)}
     *
     * @param state The block trying to harvest
     * @param stack The itemstack used to harvest the block
     * @return true if can harvest the block
     */
    public boolean canHarvestBlock(IBlockState state, TempItemStack stack) {
        return canHarvestBlock(state);
    }

    /**
     * Gets the maximum number of items that this stack should be able to hold.
     * This is a ItemStack (and thus NBT) sensitive version of Item.getItemStackLimit()
     *
     * @param stack The ItemStack
     * @return The maximum number this item can be stacked to
     */
    public int getItemStackLimit(TempItemStack stack) {
        return this.getItemStackLimit();
    }

    private java.util.Map<String, Integer> toolClasses = new java.util.HashMap<String, Integer>();

    /**
     * Sets or removes the harvest level for the specified tool class.
     *
     * @param toolClass Class
     * @param level     Harvest level:
     *                  Wood: 0
     *                  Stone: 1
     *                  Iron: 2
     *                  Diamond: 3
     *                  Gold: 0
     */
    public void setHarvestLevel(String toolClass, int level) {
        if (level < 0) toolClasses.remove(toolClass);
        else toolClasses.put(toolClass, level);
    }

    public java.util.Set<String> getToolClasses(TempItemStack stack) {
        return toolClasses.keySet();
    }

    /**
     * Queries the harvest level of this item stack for the specified tool class,
     * Returns -1 if this tool is not of the specified type
     *
     * @param stack      This item stack instance
     * @param toolClass  Tool Class
     * @param player     The player trying to harvest the given blockstate
     * @param blockState The block to harvest
     * @return Harvest level, or -1 if not the specified tool type.
     */
    public int getHarvestLevel(TempItemStack stack, String toolClass, @Nullable EntityPlayer player,
                               @Nullable IBlockState blockState) {
        Integer ret = toolClasses.get(toolClass);
        return ret == null ? -1 : ret;
    }

    /**
     * ItemStack sensitive version of getItemEnchantability
     *
     * @param stack The ItemStack
     * @return the item echantability value
     */
    public int getItemEnchantability(TempItemStack stack) {
        return getItemEnchantability();
    }

    /**
     * Checks whether an item can be enchanted with a certain enchantment. This applies specifically to enchanting an
     * item in the enchanting table and is called when retrieving the list of possible enchantments for an item.
     * Enchantments may additionally (or exclusively) be doing their own checks in
     * {@link net.minecraft.enchantment.Enchantment#canApplyAtEnchantingTable(TempItemStack)}; check the individual
     * implementation for reference.
     * By default this will check if the enchantment type is valid for this item type.
     *
     * @param stack       the item stack to be enchanted
     * @param enchantment the enchantment to be applied
     * @return true if the enchantment can be applied to this item
     */
    public boolean canApplyAtEnchantingTable(TempItemStack stack, net.minecraft.enchantment.Enchantment enchantment) {
        return enchantment.type.canEnchantItem(stack.getItem());
    }

    /**
     * Whether this Item can be used as a payment to activate the vanilla beacon.
     *
     * @param stack the ItemStack
     * @return true if this Item can be used
     */
    public boolean isBeaconPayment(TempItemStack stack) {
        return this == WrapperItems.EMERALD || this == WrapperItems.DIAMOND || this == WrapperItems.GOLD_INGOT || this == WrapperItems.IRON_INGOT;
    }

    /**
     * Determine if the player switching between these two item stacks
     *
     * @param oldStack    The old stack that was equipped
     * @param newStack    The new stack
     * @param slotChanged If the current equipped slot was changed,
     *                    Vanilla does not play the animation if you switch between two
     *                    slots that hold the exact same item.
     * @return True to play the item change animation
     */
    public boolean shouldCauseReequipAnimation(TempItemStack oldStack, TempItemStack newStack, boolean slotChanged) {
        return !oldStack.equals(newStack); // !ItemStack.areItemStacksEqual(oldStack, newStack);
    }

    /**
     * Called when the player is mining a block and the item in his hand changes.
     * Allows to not reset blockbreaking if only NBT or similar changes.
     *
     * @param oldStack The old stack that was used for mining. Item in players main hand
     * @param newStack The new stack
     * @return True to reset block break progress
     */
    public boolean shouldCauseBlockBreakReset(TempItemStack oldStack, TempItemStack newStack) {
        return !(newStack.getItem() == oldStack.getItem() && TempItemStack.areItemStackTagsEqual(newStack, oldStack)
            && (newStack.isItemStackDamageable() || newStack.getMetadata() == oldStack.getMetadata()));
    }

    /**
     * Called while an item is in 'active' use to determine if usage should continue.
     * Allows items to continue being used while sustaining damage, for example.
     *
     * @param oldStack the previous 'active' stack
     * @param newStack the stack currently in the active hand
     * @return true to set the new stack to active and continue using it
     */
    public boolean canContinueUsing(TempItemStack oldStack, TempItemStack newStack) {
        return oldStack.equals(newStack);
    }

    /**
     * Called to get the Mod ID of the mod that *created* the ItemStack,
     * instead of the real Mod ID that *registered* it.
     * <p>
     * For example the Forge Universal Bucket creates a subitem for each modded fluid,
     * and it returns the modded fluid's Mod ID here.
     * <p>
     * Mods that register subitems for other mods can override this.
     * Informational mods can call it to show the mod that created the item.
     *
     * @param tempItemStack the ItemStack to check
     * @return the Mod ID for the ItemStack, or
     * null when there is no specially associated mod and {@link #getRegistryName()} would return null.
     */
    @Nullable
    public String getCreatorModId(TempItemStack tempItemStack) {
        return net.minecraftforge.common.ForgeHooks.getDefaultCreatorModId(tempItemStack);
    }

    /**
     * Called from ItemStack.setItem, will hold extra data for the life of this ItemStack.
     * Can be retrieved from stack.getCapabilities()
     * The NBT can be null if this is not called from readNBT or if the item the stack is
     * changing FROM is different then this item, or the previous item had no capabilities.
     * <p>
     * This is called BEFORE the stacks item is set so you can use stack.getItem() to see the OLD item.
     * Remember that getItem CAN return null.
     *
     * @param stack The ItemStack
     * @param nbt   NBT of this item serialized, or null.
     * @return A holder instance associated with this ItemStack where you can hold capabilities for the life of this
     * item.
     */
    @Nullable
    public ICapabilityProvider initCapabilities(TempItemStack stack,
                                                @Nullable NBTTagCompound nbt) {
        return null;
    }

    public com.google.common.collect.ImmutableMap<String, animation.ITimeValue> getAnimationParameters(
        final TempItemStack stack, final net.minecraft.world.World world, final EntityLivingBase entity) {
        com.google.common.collect.ImmutableMap.Builder<String, animation.ITimeValue> builder = com.google.common.collect.ImmutableMap
            .builder();
        for (ResourceLocation location : properties.getKeys()) {
            final IItemPropertyGetter parameter = properties.getObject(location);
            builder.put(location.toString(), new net.minecraftforge.common.animation.ITimeValue() {

                public float apply(float input) {
                    return parameter.apply(stack, world, entity);
                }
            });
        }
        return builder.build();
    }

    /**
     * Can this Item disable a shield
     *
     * @param stack    The ItemStack
     * @param shield   The shield in question
     * @param entity   The EntityLivingBase holding the shield
     * @param attacker The EntityLivingBase holding the ItemStack
     * @retrun True if this ItemStack can disable the shield in question.
     */
    public boolean canDisableShield(TempItemStack stack, TempItemStack shield, EntityLivingBase entity,
                                    EntityLivingBase attacker) {
        return this instanceof WrapperItemAxe;
    }

    /**
     * Is this Item a shield
     *
     * @param stack  The ItemStack
     * @param entity The Entity holding the ItemStack
     * @return True if the ItemStack is considered a shield
     */
    public boolean isShield(TempItemStack stack, @Nullable EntityLivingBase entity) {
        return stack.getItem() == Items.SHIELD;
    }

    /**
     * @return the fuel burn time for this itemStack in a furnace.
     * Return 0 to make it not act as a fuel.
     * Return -1 to let the default vanilla logic decide.
     */
    public int getItemBurnTime(TempItemStack tempItemStack) {
        return -1;
    }

    /**
     * Returns an enum constant of type {@code HorseArmorType}.
     * The returned enum constant will be used to determine the armor value and texture of this item when equipped.
     *
     * @param stack the armor stack
     * @return an enum constant of type {@code HorseArmorType}. Return HorseArmorType.NONE if this is not horse armor
     */
    public net.minecraft.entity.passive.HorseArmorType getHorseArmorType(TempItemStack stack) {
        return net.minecraft.entity.passive.HorseArmorType.getByItem(stack.getItem());
    }

    public String getHorseArmorTexture(net.minecraft.entity.EntityLiving wearer, TempItemStack stack) {
        return getHorseArmorType(stack).getTextureName();
    }

    /**
     * Called every tick from {@link EntityHorse#onUpdate()} on the item in the armor slot.
     *
     * @param world the world the horse is in
     * @param horse the horse wearing this armor
     * @param armor the armor itemstack
     */
    public void onHorseArmorTick(net.minecraft.world.World world, net.minecraft.entity.EntityLiving horse, TempItemStack armor) {
    }

    @SideOnly(Side.CLIENT)
    @Nullable
    private TileEntityItemStackRenderer teisr;

    /**
     * @return This Item's renderer, or the default instance if it does not have one.
     */
    @SideOnly(Side.CLIENT)
    public final TileEntityItemStackRenderer getTileEntityItemStackRenderer() {
        return teisr != null ? teisr : TileEntityItemStackRenderer.instance;
    }

    @SideOnly(Side.CLIENT)
    public void setTileEntityItemStackRenderer(
        @Nullable TileEntityItemStackRenderer teisr) {
        this.teisr = teisr;
    }

    public IRarity getForgeRarity(TempItemStack stack) {
        return this.getRarity(stack);
    }

    /* ======================================== FORGE END ===================================== */

//    public static void registerItems() {
//        registerItemBlock(WrapperBlocks.AIR, new WrapperItemAir(WrapperBlocks.AIR));
//        registerItemBlock(
//            WrapperBlocks.STONE,
//            (new WrapperItemMultiTexture(WrapperBlocks.STONE, WrapperBlocks.STONE, new WrapperItemMultiTexture.Mapper() {
//
//                public String apply(ItemStack p_apply_1_) {
//                    return BlockStone.EnumType.byMetadata(p_apply_1_.getMetadata())
//                        .getUnlocalizedName();
//                }
//            })).setUnlocalizedName("stone"));
//        registerItemBlock(WrapperBlocks.GRASS, new WrapperItemColored(WrapperBlocks.GRASS, false));
//        registerItemBlock(WrapperBlocks.DIRT, (new WrapperItemMultiTexture(WrapperBlocks.DIRT, WrapperBlocks.DIRT, new WrapperItemMultiTexture.Mapper() {
//
//            public String apply(ItemStack p_apply_1_) {
//                return BlockDirt.DirtType.byMetadata(p_apply_1_.getMetadata())
//                    .getUnlocalizedName();
//            }
//        })).setUnlocalizedName("dirt"));
//        registerItemBlock(WrapperBlocks.COBBLESTONE);
//        registerItemBlock(
//            WrapperBlocks.PLANKS,
//            (new WrapperItemMultiTexture(WrapperBlocks.PLANKS, WrapperBlocks.PLANKS, new WrapperItemMultiTexture.Mapper() {
//
//                public String apply(ItemStack p_apply_1_) {
//                    return BlockPlanks.EnumType.byMetadata(p_apply_1_.getMetadata())
//                        .getUnlocalizedName();
//                }
//            })).setUnlocalizedName("wood"));
//        registerItemBlock(
//            WrapperBlocks.SAPLING,
//            (new WrapperItemMultiTexture(WrapperBlocks.SAPLING, WrapperBlocks.SAPLING, new WrapperItemMultiTexture.Mapper() {
//
//                public String apply(ItemStack p_apply_1_) {
//                    return BlockPlanks.EnumType.byMetadata(p_apply_1_.getMetadata())
//                        .getUnlocalizedName();
//                }
//            })).setUnlocalizedName("sapling"));
//        registerItemBlock(WrapperBlocks.BEDROCK);
//        registerItemBlock(WrapperBlocks.SAND, (new WrapperItemMultiTexture(WrapperBlocks.SAND, WrapperBlocks.SAND, new WrapperItemMultiTexture.Mapper() {
//
//            public String apply(ItemStack p_apply_1_) {
//                return BlockSand.EnumType.byMetadata(p_apply_1_.getMetadata())
//                    .getUnlocalizedName();
//            }
//        })).setUnlocalizedName("sand"));
//        registerItemBlock(WrapperBlocks.GRAVEL);
//        registerItemBlock(WrapperBlocks.GOLD_ORE);
//        registerItemBlock(WrapperBlocks.IRON_ORE);
//        registerItemBlock(WrapperBlocks.COAL_ORE);
//        registerItemBlock(WrapperBlocks.LOG, (new WrapperItemMultiTexture(WrapperBlocks.LOG, WrapperBlocks.LOG, new WrapperItemMultiTexture.Mapper() {
//
//            public String apply(ItemStack p_apply_1_) {
//                return BlockPlanks.EnumType.byMetadata(p_apply_1_.getMetadata())
//                    .getUnlocalizedName();
//            }
//        })).setUnlocalizedName("log"));
//        registerItemBlock(WrapperBlocks.LOG2, (new WrapperItemMultiTexture(WrapperBlocks.LOG2, WrapperBlocks.LOG2, new WrapperItemMultiTexture.Mapper() {
//
//            public String apply(ItemStack p_apply_1_) {
//                return BlockPlanks.EnumType.byMetadata(p_apply_1_.getMetadata() + 4)
//                    .getUnlocalizedName();
//            }
//        })).setUnlocalizedName("log"));
//        registerItemBlock(WrapperBlocks.LEAVES, (new WrapperItemLeaves(WrapperBlocks.LEAVES)).setUnlocalizedName("leaves"));
//        registerItemBlock(WrapperBlocks.LEAVES2, (new WrapperItemLeaves(WrapperBlocks.LEAVES2)).setUnlocalizedName("leaves"));
//        registerItemBlock(
//            WrapperBlocks.SPONGE,
//            (new WrapperItemMultiTexture(WrapperBlocks.SPONGE, WrapperBlocks.SPONGE, new WrapperItemMultiTexture.Mapper() {
//
//                public String apply(ItemStack p_apply_1_) {
//                    return (p_apply_1_.getMetadata() & 1) == 1 ? "wet" : "dry";
//                }
//            })).setUnlocalizedName("sponge"));
//        registerItemBlock(WrapperBlocks.GLASS);
//        registerItemBlock(WrapperBlocks.LAPIS_ORE);
//        registerItemBlock(WrapperBlocks.LAPIS_BLOCK);
//        registerItemBlock(WrapperBlocks.DISPENSER);
//        registerItemBlock(
//            WrapperBlocks.SANDSTONE,
//            (new WrapperItemMultiTexture(WrapperBlocks.SANDSTONE, WrapperBlocks.SANDSTONE, new WrapperItemMultiTexture.Mapper() {
//
//                public String apply(ItemStack p_apply_1_) {
//                    return BlockSandStone.EnumType.byMetadata(p_apply_1_.getMetadata())
//                        .getUnlocalizedName();
//                }
//            })).setUnlocalizedName("sandStone"));
//        registerItemBlock(WrapperBlocks.NOTEBLOCK);
//        registerItemBlock(WrapperBlocks.GOLDEN_RAIL);
//        registerItemBlock(WrapperBlocks.DETECTOR_RAIL);
//        registerItemBlock(WrapperBlocks.STICKY_PISTON, new WrapperItemPiston(WrapperBlocks.STICKY_PISTON));
//        registerItemBlock(WrapperBlocks.WEB);
//        registerItemBlock(
//            WrapperBlocks.TALLGRASS,
//            (new WrapperItemColored(WrapperBlocks.TALLGRASS, true)).setSubtypeNames(new String[] { "shrub", "grass", "fern" }));
//        registerItemBlock(WrapperBlocks.DEADBUSH);
//        registerItemBlock(WrapperBlocks.PISTON, new WrapperItemPiston(WrapperBlocks.PISTON));
//        registerItemBlock(WrapperBlocks.WOOL, (new WrapperItemCloth(WrapperBlocks.WOOL)).setUnlocalizedName("cloth"));
//        registerItemBlock(
//            WrapperBlocks.YELLOW_FLOWER,
//            (new WrapperItemMultiTexture(WrapperBlocks.YELLOW_FLOWER, WrapperBlocks.YELLOW_FLOWER, new WrapperItemMultiTexture.Mapper() {
//
//                public String apply(ItemStack p_apply_1_) {
//                    return BlockFlower.EnumFlowerType
//                        .getType(BlockFlower.EnumFlowerColor.YELLOW, p_apply_1_.getMetadata())
//                        .getUnlocalizedName();
//                }
//            })).setUnlocalizedName("flower"));
//        registerItemBlock(
//            WrapperBlocks.RED_FLOWER,
//            (new WrapperItemMultiTexture(WrapperBlocks.RED_FLOWER, WrapperBlocks.RED_FLOWER, new WrapperItemMultiTexture.Mapper() {
//
//                public String apply(ItemStack p_apply_1_) {
//                    return BlockFlower.EnumFlowerType.getType(BlockFlower.EnumFlowerColor.RED, p_apply_1_.getMetadata())
//                        .getUnlocalizedName();
//                }
//            })).setUnlocalizedName("rose"));
//        registerItemBlock(WrapperBlocks.BROWN_MUSHROOM);
//        registerItemBlock(WrapperBlocks.RED_MUSHROOM);
//        registerItemBlock(WrapperBlocks.GOLD_BLOCK);
//        registerItemBlock(WrapperBlocks.IRON_BLOCK);
//        registerItemBlock(
//            WrapperBlocks.STONE_SLAB,
//            (new WrapperItemSlab(WrapperBlocks.STONE_SLAB, WrapperBlocks.STONE_SLAB, WrapperBlocks.DOUBLE_STONE_SLAB))
//                .setUnlocalizedName("stoneSlab"));
//        registerItemBlock(WrapperBlocks.BRICK_BLOCK);
//        registerItemBlock(WrapperBlocks.TNT);
//        registerItemBlock(WrapperBlocks.BOOKSHELF);
//        registerItemBlock(WrapperBlocks.MOSSY_COBBLESTONE);
//        registerItemBlock(WrapperBlocks.OBSIDIAN);
//        registerItemBlock(WrapperBlocks.TORCH);
//        registerItemBlock(WrapperBlocks.END_ROD);
//        registerItemBlock(WrapperBlocks.CHORUS_PLANT);
//        registerItemBlock(WrapperBlocks.CHORUS_FLOWER);
//        registerItemBlock(WrapperBlocks.PURPUR_BLOCK);
//        registerItemBlock(WrapperBlocks.PURPUR_PILLAR);
//        registerItemBlock(WrapperBlocks.PURPUR_STAIRS);
//        registerItemBlock(
//            WrapperBlocks.PURPUR_SLAB,
//            (new WrapperItemSlab(WrapperBlocks.PURPUR_SLAB, WrapperBlocks.PURPUR_SLAB, WrapperBlocks.PURPUR_DOUBLE_SLAB))
//                .setUnlocalizedName("purpurSlab"));
//        registerItemBlock(WrapperBlocks.MOB_SPAWNER);
//        registerItemBlock(WrapperBlocks.OAK_STAIRS);
//        registerItemBlock(WrapperBlocks.CHEST);
//        registerItemBlock(WrapperBlocks.DIAMOND_ORE);
//        registerItemBlock(WrapperBlocks.DIAMOND_BLOCK);
//        registerItemBlock(WrapperBlocks.CRAFTING_TABLE);
//        registerItemBlock(WrapperBlocks.FARMLAND);
//        registerItemBlock(WrapperBlocks.FURNACE);
//        registerItemBlock(WrapperBlocks.LADDER);
//        registerItemBlock(WrapperBlocks.RAIL);
//        registerItemBlock(WrapperBlocks.STONE_STAIRS);
//        registerItemBlock(WrapperBlocks.LEVER);
//        registerItemBlock(WrapperBlocks.STONE_PRESSURE_PLATE);
//        registerItemBlock(WrapperBlocks.WOODEN_PRESSURE_PLATE);
//        registerItemBlock(WrapperBlocks.REDSTONE_ORE);
//        registerItemBlock(WrapperBlocks.REDSTONE_TORCH);
//        registerItemBlock(WrapperBlocks.STONE_BUTTON);
//        registerItemBlock(WrapperBlocks.SNOW_LAYER, new WrapperItemSnow(WrapperBlocks.SNOW_LAYER));
//        registerItemBlock(WrapperBlocks.ICE);
//        registerItemBlock(WrapperBlocks.SNOW);
//        registerItemBlock(WrapperBlocks.CACTUS);
//        registerItemBlock(WrapperBlocks.CLAY);
//        registerItemBlock(WrapperBlocks.JUKEBOX);
//        registerItemBlock(WrapperBlocks.OAK_FENCE);
//        registerItemBlock(WrapperBlocks.SPRUCE_FENCE);
//        registerItemBlock(WrapperBlocks.BIRCH_FENCE);
//        registerItemBlock(WrapperBlocks.JUNGLE_FENCE);
//        registerItemBlock(WrapperBlocks.DARK_OAK_FENCE);
//        registerItemBlock(WrapperBlocks.ACACIA_FENCE);
//        registerItemBlock(WrapperBlocks.PUMPKIN);
//        registerItemBlock(WrapperBlocks.NETHERRACK);
//        registerItemBlock(WrapperBlocks.SOUL_SAND);
//        registerItemBlock(WrapperBlocks.GLOWSTONE);
//        registerItemBlock(WrapperBlocks.LIT_PUMPKIN);
//        registerItemBlock(WrapperBlocks.TRAPDOOR);
//        registerItemBlock(
//            WrapperBlocks.MONSTER_EGG,
//            (new WrapperItemMultiTexture(WrapperBlocks.MONSTER_EGG, WrapperBlocks.MONSTER_EGG, new WrapperItemMultiTexture.Mapper() {
//
//                public String apply(ItemStack p_apply_1_) {
//                    return BlockSilverfish.EnumType.byMetadata(p_apply_1_.getMetadata())
//                        .getUnlocalizedName();
//                }
//            })).setUnlocalizedName("monsterStoneEgg"));
//        registerItemBlock(
//            WrapperBlocks.STONEBRICK,
//            (new WrapperItemMultiTexture(WrapperBlocks.STONEBRICK, WrapperBlocks.STONEBRICK, new WrapperItemMultiTexture.Mapper() {
//
//                public String apply(ItemStack p_apply_1_) {
//                    return BlockStoneBrick.EnumType.byMetadata(p_apply_1_.getMetadata())
//                        .getUnlocalizedName();
//                }
//            })).setUnlocalizedName("stonebricksmooth"));
//        registerItemBlock(WrapperBlocks.BROWN_MUSHROOM_BLOCK);
//        registerItemBlock(WrapperBlocks.RED_MUSHROOM_BLOCK);
//        registerItemBlock(WrapperBlocks.IRON_BARS);
//        registerItemBlock(WrapperBlocks.GLASS_PANE);
//        registerItemBlock(WrapperBlocks.MELON_BLOCK);
//        registerItemBlock(WrapperBlocks.VINE, new WrapperItemColored(WrapperBlocks.VINE, false));
//        registerItemBlock(WrapperBlocks.OAK_FENCE_GATE);
//        registerItemBlock(WrapperBlocks.SPRUCE_FENCE_GATE);
//        registerItemBlock(WrapperBlocks.BIRCH_FENCE_GATE);
//        registerItemBlock(WrapperBlocks.JUNGLE_FENCE_GATE);
//        registerItemBlock(WrapperBlocks.DARK_OAK_FENCE_GATE);
//        registerItemBlock(WrapperBlocks.ACACIA_FENCE_GATE);
//        registerItemBlock(WrapperBlocks.BRICK_STAIRS);
//        registerItemBlock(WrapperBlocks.STONE_BRICK_STAIRS);
//        registerItemBlock(WrapperBlocks.MYCELIUM);
//        registerItemBlock(WrapperBlocks.WATERLILY, new WrapperItemLilyPad(WrapperBlocks.WATERLILY));
//        registerItemBlock(WrapperBlocks.NETHER_BRICK);
//        registerItemBlock(WrapperBlocks.NETHER_BRICK_FENCE);
//        registerItemBlock(WrapperBlocks.NETHER_BRICK_STAIRS);
//        registerItemBlock(WrapperBlocks.ENCHANTING_TABLE);
//        registerItemBlock(WrapperBlocks.END_PORTAL_FRAME);
//        registerItemBlock(WrapperBlocks.END_STONE);
//        registerItemBlock(WrapperBlocks.END_BRICKS);
//        registerItemBlock(WrapperBlocks.DRAGON_EGG);
//        registerItemBlock(WrapperBlocks.REDSTONE_LAMP);
//        registerItemBlock(
//            WrapperBlocks.WOODEN_SLAB,
//            (new WrapperItemSlab(WrapperBlocks.WOODEN_SLAB, WrapperBlocks.WOODEN_SLAB, WrapperBlocks.DOUBLE_WOODEN_SLAB))
//                .setUnlocalizedName("woodSlab"));
//        registerItemBlock(WrapperBlocks.SANDSTONE_STAIRS);
//        registerItemBlock(WrapperBlocks.EMERALD_ORE);
//        registerItemBlock(WrapperBlocks.ENDER_CHEST);
//        registerItemBlock(WrapperBlocks.TRIPWIRE_HOOK);
//        registerItemBlock(WrapperBlocks.EMERALD_BLOCK);
//        registerItemBlock(WrapperBlocks.SPRUCE_STAIRS);
//        registerItemBlock(WrapperBlocks.BIRCH_STAIRS);
//        registerItemBlock(WrapperBlocks.JUNGLE_STAIRS);
//        registerItemBlock(WrapperBlocks.COMMAND_BLOCK);
//        registerItemBlock(WrapperBlocks.BEACON);
//        registerItemBlock(
//            WrapperBlocks.COBBLESTONE_WALL,
//            (new WrapperItemMultiTexture(WrapperBlocks.COBBLESTONE_WALL, WrapperBlocks.COBBLESTONE_WALL, new WrapperItemMultiTexture.Mapper() {
//
//                public String apply(ItemStack p_apply_1_) {
//                    return BlockWall.EnumType.byMetadata(p_apply_1_.getMetadata())
//                        .getUnlocalizedName();
//                }
//            })).setUnlocalizedName("cobbleWall"));
//        registerItemBlock(WrapperBlocks.WOODEN_BUTTON);
//        registerItemBlock(WrapperBlocks.ANVIL, (new WrapperItemAnvilBlock(WrapperBlocks.ANVIL)).setUnlocalizedName("anvil"));
//        registerItemBlock(WrapperBlocks.TRAPPED_CHEST);
//        registerItemBlock(WrapperBlocks.LIGHT_WEIGHTED_PRESSURE_PLATE);
//        registerItemBlock(WrapperBlocks.HEAVY_WEIGHTED_PRESSURE_PLATE);
//        registerItemBlock(WrapperBlocks.DAYLIGHT_DETECTOR);
//        registerItemBlock(WrapperBlocks.REDSTONE_BLOCK);
//        registerItemBlock(WrapperBlocks.QUARTZ_ORE);
//        registerItemBlock(WrapperBlocks.HOPPER);
//        registerItemBlock(
//            WrapperBlocks.QUARTZ_BLOCK,
//            (new WrapperItemMultiTexture(
//                WrapperBlocks.QUARTZ_BLOCK,
//                WrapperBlocks.QUARTZ_BLOCK,
//                new String[] { "default", "chiseled", "lines" })).setUnlocalizedName("quartzBlock"));
//        registerItemBlock(WrapperBlocks.QUARTZ_STAIRS);
//        registerItemBlock(WrapperBlocks.ACTIVATOR_RAIL);
//        registerItemBlock(WrapperBlocks.DROPPER);
//        registerItemBlock(
//            WrapperBlocks.STAINED_HARDENED_CLAY,
//            (new WrapperItemCloth(WrapperBlocks.STAINED_HARDENED_CLAY)).setUnlocalizedName("clayHardenedStained"));
//        registerItemBlock(WrapperBlocks.BARRIER);
//        registerItemBlock(WrapperBlocks.IRON_TRAPDOOR);
//        registerItemBlock(WrapperBlocks.HAY_BLOCK);
//        registerItemBlock(WrapperBlocks.CARPET, (new WrapperItemCloth(WrapperBlocks.CARPET)).setUnlocalizedName("woolCarpet"));
//        registerItemBlock(WrapperBlocks.HARDENED_CLAY);
//        registerItemBlock(WrapperBlocks.COAL_BLOCK);
//        registerItemBlock(WrapperBlocks.PACKED_ICE);
//        registerItemBlock(WrapperBlocks.ACACIA_STAIRS);
//        registerItemBlock(WrapperBlocks.DARK_OAK_STAIRS);
//        registerItemBlock(WrapperBlocks.SLIME_BLOCK);
//        registerItemBlock(WrapperBlocks.GRASS_PATH);
//        registerItemBlock(
//            WrapperBlocks.DOUBLE_PLANT,
//            (new WrapperItemMultiTexture(WrapperBlocks.DOUBLE_PLANT, WrapperBlocks.DOUBLE_PLANT, new WrapperItemMultiTexture.Mapper() {
//
//                public String apply(ItemStack p_apply_1_) {
//                    return BlockDoublePlant.EnumPlantType.byMetadata(p_apply_1_.getMetadata())
//                        .getUnlocalizedName();
//                }
//            })).setUnlocalizedName("doublePlant"));
//        registerItemBlock(
//            WrapperBlocks.STAINED_GLASS,
//            (new WrapperItemCloth(WrapperBlocks.STAINED_GLASS)).setUnlocalizedName("stainedGlass"));
//        registerItemBlock(
//            WrapperBlocks.STAINED_GLASS_PANE,
//            (new WrapperItemCloth(WrapperBlocks.STAINED_GLASS_PANE)).setUnlocalizedName("stainedGlassPane"));
//        registerItemBlock(
//            WrapperBlocks.PRISMARINE,
//            (new WrapperItemMultiTexture(WrapperBlocks.PRISMARINE, WrapperBlocks.PRISMARINE, new WrapperItemMultiTexture.Mapper() {
//
//                public String apply(ItemStack p_apply_1_) {
//                    return BlockPrismarine.EnumType.byMetadata(p_apply_1_.getMetadata())
//                        .getUnlocalizedName();
//                }
//            })).setUnlocalizedName("prismarine"));
//        registerItemBlock(WrapperBlocks.SEA_LANTERN);
//        registerItemBlock(
//            WrapperBlocks.RED_SANDSTONE,
//            (new WrapperItemMultiTexture(WrapperBlocks.RED_SANDSTONE, WrapperBlocks.RED_SANDSTONE, new WrapperItemMultiTexture.Mapper() {
//
//                public String apply(ItemStack p_apply_1_) {
//                    return BlockRedSandstone.EnumType.byMetadata(p_apply_1_.getMetadata())
//                        .getUnlocalizedName();
//                }
//            })).setUnlocalizedName("redSandStone"));
//        registerItemBlock(WrapperBlocks.RED_SANDSTONE_STAIRS);
//        registerItemBlock(
//            WrapperBlocks.STONE_SLAB2,
//            (new WrapperItemSlab(WrapperBlocks.STONE_SLAB2, WrapperBlocks.STONE_SLAB2, WrapperBlocks.DOUBLE_STONE_SLAB2))
//                .setUnlocalizedName("stoneSlab2"));
//        registerItemBlock(WrapperBlocks.REPEATING_COMMAND_BLOCK);
//        registerItemBlock(WrapperBlocks.CHAIN_COMMAND_BLOCK);
//        registerItemBlock(WrapperBlocks.MAGMA);
//        registerItemBlock(WrapperBlocks.NETHER_WART_BLOCK);
//        registerItemBlock(WrapperBlocks.RED_NETHER_BRICK);
//        registerItemBlock(WrapperBlocks.BONE_BLOCK);
//        registerItemBlock(WrapperBlocks.STRUCTURE_VOID);
//        registerItemBlock(WrapperBlocks.OBSERVER);
//        registerItemBlock(WrapperBlocks.WHITE_SHULKER_BOX, new WrapperItemShulkerBox(WrapperBlocks.WHITE_SHULKER_BOX));
//        registerItemBlock(WrapperBlocks.ORANGE_SHULKER_BOX, new WrapperItemShulkerBox(WrapperBlocks.ORANGE_SHULKER_BOX));
//        registerItemBlock(WrapperBlocks.MAGENTA_SHULKER_BOX, new WrapperItemShulkerBox(WrapperBlocks.MAGENTA_SHULKER_BOX));
//        registerItemBlock(WrapperBlocks.LIGHT_BLUE_SHULKER_BOX, new WrapperItemShulkerBox(WrapperBlocks.LIGHT_BLUE_SHULKER_BOX));
//        registerItemBlock(WrapperBlocks.YELLOW_SHULKER_BOX, new WrapperItemShulkerBox(WrapperBlocks.YELLOW_SHULKER_BOX));
//        registerItemBlock(WrapperBlocks.LIME_SHULKER_BOX, new WrapperItemShulkerBox(WrapperBlocks.LIME_SHULKER_BOX));
//        registerItemBlock(WrapperBlocks.PINK_SHULKER_BOX, new WrapperItemShulkerBox(WrapperBlocks.PINK_SHULKER_BOX));
//        registerItemBlock(WrapperBlocks.GRAY_SHULKER_BOX, new WrapperItemShulkerBox(WrapperBlocks.GRAY_SHULKER_BOX));
//        registerItemBlock(WrapperBlocks.SILVER_SHULKER_BOX, new WrapperItemShulkerBox(WrapperBlocks.SILVER_SHULKER_BOX));
//        registerItemBlock(WrapperBlocks.CYAN_SHULKER_BOX, new WrapperItemShulkerBox(WrapperBlocks.CYAN_SHULKER_BOX));
//        registerItemBlock(WrapperBlocks.PURPLE_SHULKER_BOX, new WrapperItemShulkerBox(WrapperBlocks.PURPLE_SHULKER_BOX));
//        registerItemBlock(WrapperBlocks.BLUE_SHULKER_BOX, new WrapperItemShulkerBox(WrapperBlocks.BLUE_SHULKER_BOX));
//        registerItemBlock(WrapperBlocks.BROWN_SHULKER_BOX, new WrapperItemShulkerBox(WrapperBlocks.BROWN_SHULKER_BOX));
//        registerItemBlock(WrapperBlocks.GREEN_SHULKER_BOX, new WrapperItemShulkerBox(WrapperBlocks.GREEN_SHULKER_BOX));
//        registerItemBlock(WrapperBlocks.RED_SHULKER_BOX, new WrapperItemShulkerBox(WrapperBlocks.RED_SHULKER_BOX));
//        registerItemBlock(WrapperBlocks.BLACK_SHULKER_BOX, new WrapperItemShulkerBox(WrapperBlocks.BLACK_SHULKER_BOX));
//        registerItemBlock(WrapperBlocks.WHITE_GLAZED_TERRACOTTA);
//        registerItemBlock(WrapperBlocks.ORANGE_GLAZED_TERRACOTTA);
//        registerItemBlock(WrapperBlocks.MAGENTA_GLAZED_TERRACOTTA);
//        registerItemBlock(WrapperBlocks.LIGHT_BLUE_GLAZED_TERRACOTTA);
//        registerItemBlock(WrapperBlocks.YELLOW_GLAZED_TERRACOTTA);
//        registerItemBlock(WrapperBlocks.LIME_GLAZED_TERRACOTTA);
//        registerItemBlock(WrapperBlocks.PINK_GLAZED_TERRACOTTA);
//        registerItemBlock(WrapperBlocks.GRAY_GLAZED_TERRACOTTA);
//        registerItemBlock(WrapperBlocks.SILVER_GLAZED_TERRACOTTA);
//        registerItemBlock(WrapperBlocks.CYAN_GLAZED_TERRACOTTA);
//        registerItemBlock(WrapperBlocks.PURPLE_GLAZED_TERRACOTTA);
//        registerItemBlock(WrapperBlocks.BLUE_GLAZED_TERRACOTTA);
//        registerItemBlock(WrapperBlocks.BROWN_GLAZED_TERRACOTTA);
//        registerItemBlock(WrapperBlocks.GREEN_GLAZED_TERRACOTTA);
//        registerItemBlock(WrapperBlocks.RED_GLAZED_TERRACOTTA);
//        registerItemBlock(WrapperBlocks.BLACK_GLAZED_TERRACOTTA);
//        registerItemBlock(WrapperBlocks.CONCRETE, (new WrapperItemCloth(WrapperBlocks.CONCRETE)).setUnlocalizedName("concrete"));
//        registerItemBlock(
//            WrapperBlocks.CONCRETE_POWDER,
//            (new WrapperItemCloth(WrapperBlocks.CONCRETE_POWDER)).setUnlocalizedName("concrete_powder"));
//        registerItemBlock(WrapperBlocks.STRUCTURE_BLOCK);
//        registerItem(256, "iron_shovel", (new WrapperItemSpade(ToolMaterial.IRON)).setUnlocalizedName("shovelIron"));
//        registerItem(257, "iron_pickaxe", (new WrapperItemPickaxe(ToolMaterial.IRON)).setUnlocalizedName("pickaxeIron"));
//        registerItem(258, "iron_axe", (new WrapperItemAxe(ToolMaterial.IRON)).setUnlocalizedName("hatchetIron"));
//        registerItem(259, "flint_and_steel", (new WrapperItemFlintAndSteel()).setUnlocalizedName("flintAndSteel"));
//        registerItem(260, "apple", (new WrapperItemFood(4, 0.3F, false)).setUnlocalizedName("apple"));
//        registerItem(261, "bow", (new WrapperItemBow()).setUnlocalizedName("bow"));
//        registerItem(262, "arrow", (new WrapperItemArrow()).setUnlocalizedName("arrow"));
//        registerItem(263, "coal", (new WrapperItemCoal()).setUnlocalizedName("coal"));
//        registerItem(
//            264,
//            "diamond",
//            (new WrapperItem()).setUnlocalizedName("diamond")
//                .setCreativeTab(CreativeTabs.MATERIALS));
//        registerItem(
//            265,
//            "iron_ingot",
//            (new WrapperItem()).setUnlocalizedName("ingotIron")
//                .setCreativeTab(CreativeTabs.MATERIALS));
//        registerItem(
//            266,
//            "gold_ingot",
//            (new WrapperItem()).setUnlocalizedName("ingotGold")
//                .setCreativeTab(CreativeTabs.MATERIALS));
//        registerItem(267, "iron_sword", (new WrapperItemSword(ToolMaterial.IRON)).setUnlocalizedName("swordIron"));
//        registerItem(268, "wooden_sword", (new WrapperItemSword(ToolMaterial.WOOD)).setUnlocalizedName("swordWood"));
//        registerItem(269, "wooden_shovel", (new WrapperItemSpade(ToolMaterial.WOOD)).setUnlocalizedName("shovelWood"));
//        registerItem(
//            270,
//            "wooden_pickaxe",
//            (new WrapperItemPickaxe(ToolMaterial.WOOD)).setUnlocalizedName("pickaxeWood"));
//        registerItem(271, "wooden_axe", (new WrapperItemAxe(ToolMaterial.WOOD)).setUnlocalizedName("hatchetWood"));
//        registerItem(272, "stone_sword", (new WrapperItemSword(ToolMaterial.STONE)).setUnlocalizedName("swordStone"));
//        registerItem(273, "stone_shovel", (new WrapperItemSpade(ToolMaterial.STONE)).setUnlocalizedName("shovelStone"));
//        registerItem(
//            274,
//            "stone_pickaxe",
//            (new WrapperItemPickaxe(ToolMaterial.STONE)).setUnlocalizedName("pickaxeStone"));
//        registerItem(275, "stone_axe", (new WrapperItemAxe(ToolMaterial.STONE)).setUnlocalizedName("hatchetStone"));
//        registerItem(
//            276,
//            "diamond_sword",
//            (new WrapperItemSword(ToolMaterial.DIAMOND)).setUnlocalizedName("swordDiamond"));
//        registerItem(
//            277,
//            "diamond_shovel",
//            (new WrapperItemSpade(ToolMaterial.DIAMOND)).setUnlocalizedName("shovelDiamond"));
//        registerItem(
//            278,
//            "diamond_pickaxe",
//            (new WrapperItemPickaxe(ToolMaterial.DIAMOND)).setUnlocalizedName("pickaxeDiamond"));
//        registerItem(279, "diamond_axe", (new WrapperItemAxe(ToolMaterial.DIAMOND)).setUnlocalizedName("hatchetDiamond"));
//        registerItem(
//            280,
//            "stick",
//            (new WrapperItem()).setFull3D()
//                .setUnlocalizedName("stick")
//                .setCreativeTab(CreativeTabs.MATERIALS));
//        registerItem(
//            281,
//            "bowl",
//            (new WrapperItem()).setUnlocalizedName("bowl")
//                .setCreativeTab(CreativeTabs.MATERIALS));
//        registerItem(282, "mushroom_stew", (new WrapperItemSoup(6)).setUnlocalizedName("mushroomStew"));
//        registerItem(283, "golden_sword", (new WrapperItemSword(ToolMaterial.GOLD)).setUnlocalizedName("swordGold"));
//        registerItem(284, "golden_shovel", (new WrapperItemSpade(ToolMaterial.GOLD)).setUnlocalizedName("shovelGold"));
//        registerItem(
//            285,
//            "golden_pickaxe",
//            (new WrapperItemPickaxe(ToolMaterial.GOLD)).setUnlocalizedName("pickaxeGold"));
//        registerItem(286, "golden_axe", (new WrapperItemAxe(ToolMaterial.GOLD)).setUnlocalizedName("hatchetGold"));
//        registerItem(
//            287,
//            "string",
//            (new WrapperItemBlockSpecial(WrapperBlocks.TRIPWIRE)).setUnlocalizedName("string")
//                .setCreativeTab(CreativeTabs.MATERIALS));
//        registerItem(
//            288,
//            "feather",
//            (new WrapperItem()).setUnlocalizedName("feather")
//                .setCreativeTab(CreativeTabs.MATERIALS));
//        registerItem(
//            289,
//            "gunpowder",
//            (new WrapperItem()).setUnlocalizedName("sulphur")
//                .setCreativeTab(CreativeTabs.MATERIALS));
//        registerItem(290, "wooden_hoe", (new WrapperItemHoe(ToolMaterial.WOOD)).setUnlocalizedName("hoeWood"));
//        registerItem(291, "stone_hoe", (new WrapperItemHoe(ToolMaterial.STONE)).setUnlocalizedName("hoeStone"));
//        registerItem(292, "iron_hoe", (new WrapperItemHoe(ToolMaterial.IRON)).setUnlocalizedName("hoeIron"));
//        registerItem(293, "diamond_hoe", (new WrapperItemHoe(ToolMaterial.DIAMOND)).setUnlocalizedName("hoeDiamond"));
//        registerItem(294, "golden_hoe", (new WrapperItemHoe(ToolMaterial.GOLD)).setUnlocalizedName("hoeGold"));
//        registerItem(295, "wheat_seeds", (new WrapperItemSeeds(WrapperBlocks.WHEAT, WrapperBlocks.FARMLAND)).setUnlocalizedName("seeds"));
//        registerItem(
//            296,
//            "wheat",
//            (new WrapperItem()).setUnlocalizedName("wheat")
//                .setCreativeTab(CreativeTabs.MATERIALS));
//        registerItem(297, "bread", (new WrapperItemFood(5, 0.6F, false)).setUnlocalizedName("bread"));
//        registerItem(
//            298,
//            "leather_helmet",
//            (new WrapperItemArmor(WrapperItemArmor.ArmorMaterial.LEATHER, 0, EntityEquipmentSlot.HEAD))
//                .setUnlocalizedName("helmetCloth"));
//        registerItem(
//            299,
//            "leather_chestplate",
//            (new WrapperItemArmor(WrapperItemArmor.ArmorMaterial.LEATHER, 0, EntityEquipmentSlot.CHEST))
//                .setUnlocalizedName("chestplateCloth"));
//        registerItem(
//            300,
//            "leather_leggings",
//            (new WrapperItemArmor(WrapperItemArmor.ArmorMaterial.LEATHER, 0, EntityEquipmentSlot.LEGS))
//                .setUnlocalizedName("leggingsCloth"));
//        registerItem(
//            301,
//            "leather_boots",
//            (new WrapperItemArmor(WrapperItemArmor.ArmorMaterial.LEATHER, 0, EntityEquipmentSlot.FEET))
//                .setUnlocalizedName("bootsCloth"));
//        registerItem(
//            302,
//            "chainmail_helmet",
//            (new WrapperItemArmor(WrapperItemArmor.ArmorMaterial.CHAIN, 1, EntityEquipmentSlot.HEAD))
//                .setUnlocalizedName("helmetChain"));
//        registerItem(
//            303,
//            "chainmail_chestplate",
//            (new WrapperItemArmor(WrapperItemArmor.ArmorMaterial.CHAIN, 1, EntityEquipmentSlot.CHEST))
//                .setUnlocalizedName("chestplateChain"));
//        registerItem(
//            304,
//            "chainmail_leggings",
//            (new WrapperItemArmor(WrapperItemArmor.ArmorMaterial.CHAIN, 1, EntityEquipmentSlot.LEGS))
//                .setUnlocalizedName("leggingsChain"));
//        registerItem(
//            305,
//            "chainmail_boots",
//            (new WrapperItemArmor(WrapperItemArmor.ArmorMaterial.CHAIN, 1, EntityEquipmentSlot.FEET))
//                .setUnlocalizedName("bootsChain"));
//        registerItem(
//            306,
//            "iron_helmet",
//            (new WrapperItemArmor(WrapperItemArmor.ArmorMaterial.IRON, 2, EntityEquipmentSlot.HEAD))
//                .setUnlocalizedName("helmetIron"));
//        registerItem(
//            307,
//            "iron_chestplate",
//            (new WrapperItemArmor(WrapperItemArmor.ArmorMaterial.IRON, 2, EntityEquipmentSlot.CHEST))
//                .setUnlocalizedName("chestplateIron"));
//        registerItem(
//            308,
//            "iron_leggings",
//            (new WrapperItemArmor(WrapperItemArmor.ArmorMaterial.IRON, 2, EntityEquipmentSlot.LEGS))
//                .setUnlocalizedName("leggingsIron"));
//        registerItem(
//            309,
//            "iron_boots",
//            (new WrapperItemArmor(WrapperItemArmor.ArmorMaterial.IRON, 2, EntityEquipmentSlot.FEET)).setUnlocalizedName("bootsIron"));
//        registerItem(
//            310,
//            "diamond_helmet",
//            (new WrapperItemArmor(WrapperItemArmor.ArmorMaterial.DIAMOND, 3, EntityEquipmentSlot.HEAD))
//                .setUnlocalizedName("helmetDiamond"));
//        registerItem(
//            311,
//            "diamond_chestplate",
//            (new WrapperItemArmor(WrapperItemArmor.ArmorMaterial.DIAMOND, 3, EntityEquipmentSlot.CHEST))
//                .setUnlocalizedName("chestplateDiamond"));
//        registerItem(
//            312,
//            "diamond_leggings",
//            (new WrapperItemArmor(WrapperItemArmor.ArmorMaterial.DIAMOND, 3, EntityEquipmentSlot.LEGS))
//                .setUnlocalizedName("leggingsDiamond"));
//        registerItem(
//            313,
//            "diamond_boots",
//            (new WrapperItemArmor(WrapperItemArmor.ArmorMaterial.DIAMOND, 3, EntityEquipmentSlot.FEET))
//                .setUnlocalizedName("bootsDiamond"));
//        registerItem(
//            314,
//            "golden_helmet",
//            (new WrapperItemArmor(WrapperItemArmor.ArmorMaterial.GOLD, 4, EntityEquipmentSlot.HEAD))
//                .setUnlocalizedName("helmetGold"));
//        registerItem(
//            315,
//            "golden_chestplate",
//            (new WrapperItemArmor(WrapperItemArmor.ArmorMaterial.GOLD, 4, EntityEquipmentSlot.CHEST))
//                .setUnlocalizedName("chestplateGold"));
//        registerItem(
//            316,
//            "golden_leggings",
//            (new WrapperItemArmor(WrapperItemArmor.ArmorMaterial.GOLD, 4, EntityEquipmentSlot.LEGS))
//                .setUnlocalizedName("leggingsGold"));
//        registerItem(
//            317,
//            "golden_boots",
//            (new WrapperItemArmor(WrapperItemArmor.ArmorMaterial.GOLD, 4, EntityEquipmentSlot.FEET)).setUnlocalizedName("bootsGold"));
//        registerItem(
//            318,
//            "flint",
//            (new WrapperItem()).setUnlocalizedName("flint")
//                .setCreativeTab(CreativeTabs.MATERIALS));
//        registerItem(319, "porkchop", (new WrapperItemFood(3, 0.3F, true)).setUnlocalizedName("porkchopRaw"));
//        registerItem(320, "cooked_porkchop", (new WrapperItemFood(8, 0.8F, true)).setUnlocalizedName("porkchopCooked"));
//        registerItem(321, "painting", (new WrapperItemHangingEntity(EntityPainting.class)).setUnlocalizedName("painting"));
//        registerItem(
//            322,
//            "golden_apple",
//            (new WrapperItemAppleGold(4, 1.2F, false)).setAlwaysEdible()
//                .setUnlocalizedName("appleGold"));
//        registerItem(323, "sign", (new WrapperItemSign()).setUnlocalizedName("sign"));
//        registerItem(324, "wooden_door", (new WrapperItemDoor(WrapperBlocks.OAK_DOOR)).setUnlocalizedName("doorOak"));
//        WrapperItem wrapperItem = (new WrapperItemBucket(WrapperBlocks.AIR)).setUnlocalizedName("bucket")
//            .setMaxStackSize(16);
//        registerItem(325, "bucket", wrapperItem);
//        registerItem(
//            326,
//            "water_bucket",
//            (new WrapperItemBucket(WrapperBlocks.FLOWING_WATER)).setUnlocalizedName("bucketWater")
//                .setContainerItem(wrapperItem));
//        registerItem(
//            327,
//            "lava_bucket",
//            (new WrapperItemBucket(WrapperBlocks.FLOWING_LAVA)).setUnlocalizedName("bucketLava")
//                .setContainerItem(wrapperItem));
//        registerItem(328, "minecart", (new WrapperItemMinecart(EntityMinecart.Type.RIDEABLE)).setUnlocalizedName("minecart"));
//        registerItem(329, "saddle", (new WrapperItemSaddle()).setUnlocalizedName("saddle"));
//        registerItem(330, "iron_door", (new WrapperItemDoor(WrapperBlocks.IRON_DOOR)).setUnlocalizedName("doorIron"));
//        registerItem(331, "redstone", (new WrapperItemRedstone()).setUnlocalizedName("redstone"));
//        registerItem(332, "snowball", (new WrapperItemSnowball()).setUnlocalizedName("snowball"));
//        registerItem(333, "boat", new WrapperItemBoat(EntityBoat.Type.OAK));
//        registerItem(
//            334,
//            "leather",
//            (new WrapperItem()).setUnlocalizedName("leather")
//                .setCreativeTab(CreativeTabs.MATERIALS));
//        registerItem(
//            335,
//            "milk_bucket",
//            (new WrapperItemBucketMilk()).setUnlocalizedName("milk")
//                .setContainerItem(wrapperItem));
//        registerItem(
//            336,
//            "brick",
//            (new WrapperItem()).setUnlocalizedName("brick")
//                .setCreativeTab(CreativeTabs.MATERIALS));
//        registerItem(
//            337,
//            "clay_ball",
//            (new WrapperItem()).setUnlocalizedName("clay")
//                .setCreativeTab(CreativeTabs.MATERIALS));
//        registerItem(
//            338,
//            "reeds",
//            (new WrapperItemBlockSpecial(WrapperBlocks.REEDS)).setUnlocalizedName("reeds")
//                .setCreativeTab(CreativeTabs.MATERIALS));
//        registerItem(
//            339,
//            "paper",
//            (new WrapperItem()).setUnlocalizedName("paper")
//                .setCreativeTab(CreativeTabs.MISC));
//        registerItem(
//            340,
//            "book",
//            (new WrapperItemBook()).setUnlocalizedName("book")
//                .setCreativeTab(CreativeTabs.MISC));
//        registerItem(
//            341,
//            "slime_ball",
//            (new WrapperItem()).setUnlocalizedName("slimeball")
//                .setCreativeTab(CreativeTabs.MISC));
//        registerItem(
//            342,
//            "chest_minecart",
//            (new WrapperItemMinecart(EntityMinecart.Type.CHEST)).setUnlocalizedName("minecartChest"));
//        registerItem(
//            343,
//            "furnace_minecart",
//            (new WrapperItemMinecart(EntityMinecart.Type.FURNACE)).setUnlocalizedName("minecartFurnace"));
//        registerItem(344, "egg", (new WrapperItemEgg()).setUnlocalizedName("egg"));
//        registerItem(
//            345,
//            "compass",
//            (new WrapperItemCompass()).setUnlocalizedName("compass")
//                .setCreativeTab(CreativeTabs.TOOLS));
//        registerItem(346, "fishing_rod", (new WrapperItemFishingRod()).setUnlocalizedName("fishingRod"));
//        registerItem(
//            347,
//            "clock",
//            (new WrapperItemClock()).setUnlocalizedName("clock")
//                .setCreativeTab(CreativeTabs.TOOLS));
//        registerItem(
//            348,
//            "glowstone_dust",
//            (new WrapperItem()).setUnlocalizedName("yellowDust")
//                .setCreativeTab(CreativeTabs.MATERIALS));
//        registerItem(
//            349,
//            "fish",
//            (new WrapperItemFishFood(false)).setUnlocalizedName("fish")
//                .setHasSubtypes(true));
//        registerItem(
//            350,
//            "cooked_fish",
//            (new WrapperItemFishFood(true)).setUnlocalizedName("fish")
//                .setHasSubtypes(true));
//        registerItem(351, "dye", (new WrapperItemDye()).setUnlocalizedName("dyePowder"));
//        registerItem(
//            352,
//            "bone",
//            (new WrapperItem()).setUnlocalizedName("bone")
//                .setFull3D()
//                .setCreativeTab(CreativeTabs.MISC));
//        registerItem(
//            353,
//            "sugar",
//            (new WrapperItem()).setUnlocalizedName("sugar")
//                .setCreativeTab(CreativeTabs.MATERIALS));
//        registerItem(
//            354,
//            "cake",
//            (new WrapperItemBlockSpecial(WrapperBlocks.CAKE)).setMaxStackSize(1)
//                .setUnlocalizedName("cake")
//                .setCreativeTab(CreativeTabs.FOOD));
//        registerItem(
//            355,
//            "bed",
//            (new WrapperItemBed()).setMaxStackSize(1)
//                .setUnlocalizedName("bed"));
//        registerItem(
//            356,
//            "repeater",
//            (new WrapperItemBlockSpecial(WrapperBlocks.UNPOWERED_REPEATER)).setUnlocalizedName("diode")
//                .setCreativeTab(CreativeTabs.REDSTONE));
//        registerItem(357, "cookie", (new WrapperItemFood(2, 0.1F, false)).setUnlocalizedName("cookie"));
//        registerItem(358, "filled_map", (new WrapperItemMap()).setUnlocalizedName("map"));
//        registerItem(359, "shears", (new WrapperItemShears()).setUnlocalizedName("shears"));
//        registerItem(360, "melon", (new WrapperItemFood(2, 0.3F, false)).setUnlocalizedName("melon"));
//        registerItem(
//            361,
//            "pumpkin_seeds",
//            (new WrapperItemSeeds(WrapperBlocks.PUMPKIN_STEM, WrapperBlocks.FARMLAND)).setUnlocalizedName("seeds_pumpkin"));
//        registerItem(
//            362,
//            "melon_seeds",
//            (new WrapperItemSeeds(WrapperBlocks.MELON_STEM, WrapperBlocks.FARMLAND)).setUnlocalizedName("seeds_melon"));
//        registerItem(363, "beef", (new WrapperItemFood(3, 0.3F, true)).setUnlocalizedName("beefRaw"));
//        registerItem(364, "cooked_beef", (new WrapperItemFood(8, 0.8F, true)).setUnlocalizedName("beefCooked"));
//        registerItem(
//            365,
//            "chicken",
//            (new WrapperItemFood(2, 0.3F, true)).setPotionEffect(new PotionEffect(MobEffects.HUNGER, 600, 0), 0.3F)
//                .setUnlocalizedName("chickenRaw"));
//        registerItem(366, "cooked_chicken", (new WrapperItemFood(6, 0.6F, true)).setUnlocalizedName("chickenCooked"));
//        registerItem(
//            367,
//            "rotten_flesh",
//            (new WrapperItemFood(4, 0.1F, true)).setPotionEffect(new PotionEffect(MobEffects.HUNGER, 600, 0), 0.8F)
//                .setUnlocalizedName("rottenFlesh"));
//        registerItem(368, "ender_pearl", (new WrapperItemEnderPearl()).setUnlocalizedName("enderPearl"));
//        registerItem(
//            369,
//            "blaze_rod",
//            (new WrapperItem()).setUnlocalizedName("blazeRod")
//                .setCreativeTab(CreativeTabs.MATERIALS)
//                .setFull3D());
//        registerItem(
//            370,
//            "ghast_tear",
//            (new WrapperItem()).setUnlocalizedName("ghastTear")
//                .setCreativeTab(CreativeTabs.BREWING));
//        registerItem(
//            371,
//            "gold_nugget",
//            (new WrapperItem()).setUnlocalizedName("goldNugget")
//                .setCreativeTab(CreativeTabs.MATERIALS));
//        registerItem(
//            372,
//            "nether_wart",
//            (new WrapperItemSeeds(WrapperBlocks.NETHER_WART, WrapperBlocks.SOUL_SAND)).setUnlocalizedName("netherStalkSeeds"));
//        registerItem(373, "potion", (new WrapperItemPotion()).setUnlocalizedName("potion"));
//        WrapperItem wrapperItem1 = (new WrapperItemGlassBottle()).setUnlocalizedName("glassBottle");
//        registerItem(374, "glass_bottle", wrapperItem1);
//        registerItem(
//            375,
//            "spider_eye",
//            (new WrapperItemFood(2, 0.8F, false)).setPotionEffect(new PotionEffect(MobEffects.POISON, 100, 0), 1.0F)
//                .setUnlocalizedName("spiderEye"));
//        registerItem(
//            376,
//            "fermented_spider_eye",
//            (new WrapperItem()).setUnlocalizedName("fermentedSpiderEye")
//                .setCreativeTab(CreativeTabs.BREWING));
//        registerItem(
//            377,
//            "blaze_powder",
//            (new WrapperItem()).setUnlocalizedName("blazePowder")
//                .setCreativeTab(CreativeTabs.BREWING));
//        registerItem(
//            378,
//            "magma_cream",
//            (new WrapperItem()).setUnlocalizedName("magmaCream")
//                .setCreativeTab(CreativeTabs.BREWING));
//        registerItem(
//            379,
//            "brewing_stand",
//            (new WrapperItemBlockSpecial(WrapperBlocks.BREWING_STAND)).setUnlocalizedName("brewingStand")
//                .setCreativeTab(CreativeTabs.BREWING));
//        registerItem(
//            380,
//            "cauldron",
//            (new WrapperItemBlockSpecial(WrapperBlocks.CAULDRON)).setUnlocalizedName("cauldron")
//                .setCreativeTab(CreativeTabs.BREWING));
//        registerItem(381, "ender_eye", (new WrapperItemEnderEye()).setUnlocalizedName("eyeOfEnder"));
//        registerItem(
//            382,
//            "speckled_melon",
//            (new WrapperItem()).setUnlocalizedName("speckledMelon")
//                .setCreativeTab(CreativeTabs.BREWING));
//        registerItem(383, "spawn_egg", (new WrapperItemMonsterPlacer()).setUnlocalizedName("monsterPlacer"));
//        registerItem(384, "experience_bottle", (new WrapperItemExpBottle()).setUnlocalizedName("expBottle"));
//        registerItem(385, "fire_charge", (new WrapperItemFireball()).setUnlocalizedName("fireball"));
//        registerItem(
//            386,
//            "writable_book",
//            (new WrapperItemWritableBook()).setUnlocalizedName("writingBook")
//                .setCreativeTab(CreativeTabs.MISC));
//        registerItem(
//            387,
//            "written_book",
//            (new WrapperItemWrittenBook()).setUnlocalizedName("writtenBook")
//                .setMaxStackSize(16));
//        registerItem(
//            388,
//            "emerald",
//            (new WrapperItem()).setUnlocalizedName("emerald")
//                .setCreativeTab(CreativeTabs.MATERIALS));
//        registerItem(389, "item_frame", (new WrapperItemHangingEntity(EntityItemFrame.class)).setUnlocalizedName("frame"));
//        registerItem(
//            390,
//            "flower_pot",
//            (new WrapperItemBlockSpecial(WrapperBlocks.FLOWER_POT)).setUnlocalizedName("flowerPot")
//                .setCreativeTab(CreativeTabs.DECORATIONS));
//        registerItem(
//            391,
//            "carrot",
//            (new WrapperItemSeedFood(3, 0.6F, WrapperBlocks.CARROTS, WrapperBlocks.FARMLAND)).setUnlocalizedName("carrots"));
//        registerItem(
//            392,
//            "potato",
//            (new WrapperItemSeedFood(1, 0.3F, WrapperBlocks.POTATOES, WrapperBlocks.FARMLAND)).setUnlocalizedName("potato"));
//        registerItem(393, "baked_potato", (new WrapperItemFood(5, 0.6F, false)).setUnlocalizedName("potatoBaked"));
//        registerItem(
//            394,
//            "poisonous_potato",
//            (new WrapperItemFood(2, 0.3F, false)).setPotionEffect(new PotionEffect(MobEffects.POISON, 100, 0), 0.6F)
//                .setUnlocalizedName("potatoPoisonous"));
//        registerItem(395, "map", (new WrapperItemEmptyMap()).setUnlocalizedName("emptyMap"));
//        registerItem(
//            396,
//            "golden_carrot",
//            (new WrapperItemFood(6, 1.2F, false)).setUnlocalizedName("carrotGolden")
//                .setCreativeTab(CreativeTabs.BREWING));
//        registerItem(397, "skull", (new WrapperItemSkull()).setUnlocalizedName("skull"));
//        registerItem(398, "carrot_on_a_stick", (new WrapperItemCarrotOnAStick()).setUnlocalizedName("carrotOnAStick"));
//        registerItem(
//            399,
//            "nether_star",
//            (new WrapperItemSimpleFoiled()).setUnlocalizedName("netherStar")
//                .setCreativeTab(CreativeTabs.MATERIALS));
//        registerItem(
//            400,
//            "pumpkin_pie",
//            (new WrapperItemFood(8, 0.3F, false)).setUnlocalizedName("pumpkinPie")
//                .setCreativeTab(CreativeTabs.FOOD));
//        registerItem(401, "fireworks", (new WrapperItemFirework()).setUnlocalizedName("fireworks"));
//        registerItem(
//            402,
//            "firework_charge",
//            (new WrapperItemFireworkCharge()).setUnlocalizedName("fireworksCharge")
//                .setCreativeTab(CreativeTabs.MISC));
//        registerItem(
//            403,
//            "enchanted_book",
//            (new WrapperItemEnchantedBook()).setMaxStackSize(1)
//                .setUnlocalizedName("enchantedBook"));
//        registerItem(
//            404,
//            "comparator",
//            (new WrapperItemBlockSpecial(WrapperBlocks.UNPOWERED_COMPARATOR)).setUnlocalizedName("comparator")
//                .setCreativeTab(CreativeTabs.REDSTONE));
//        registerItem(
//            405,
//            "netherbrick",
//            (new WrapperItem()).setUnlocalizedName("netherbrick")
//                .setCreativeTab(CreativeTabs.MATERIALS));
//        registerItem(
//            406,
//            "quartz",
//            (new WrapperItem()).setUnlocalizedName("netherquartz")
//                .setCreativeTab(CreativeTabs.MATERIALS));
//        registerItem(
//            407,
//            "tnt_minecart",
//            (new WrapperItemMinecart(EntityMinecart.Type.TNT)).setUnlocalizedName("minecartTnt"));
//        registerItem(
//            408,
//            "hopper_minecart",
//            (new WrapperItemMinecart(EntityMinecart.Type.HOPPER)).setUnlocalizedName("minecartHopper"));
//        registerItem(
//            409,
//            "prismarine_shard",
//            (new WrapperItem()).setUnlocalizedName("prismarineShard")
//                .setCreativeTab(CreativeTabs.MATERIALS));
//        registerItem(
//            410,
//            "prismarine_crystals",
//            (new WrapperItem()).setUnlocalizedName("prismarineCrystals")
//                .setCreativeTab(CreativeTabs.MATERIALS));
//        registerItem(411, "rabbit", (new WrapperItemFood(3, 0.3F, true)).setUnlocalizedName("rabbitRaw"));
//        registerItem(412, "cooked_rabbit", (new WrapperItemFood(5, 0.6F, true)).setUnlocalizedName("rabbitCooked"));
//        registerItem(413, "rabbit_stew", (new WrapperItemSoup(10)).setUnlocalizedName("rabbitStew"));
//        registerItem(
//            414,
//            "rabbit_foot",
//            (new WrapperItem()).setUnlocalizedName("rabbitFoot")
//                .setCreativeTab(CreativeTabs.BREWING));
//        registerItem(
//            415,
//            "rabbit_hide",
//            (new WrapperItem()).setUnlocalizedName("rabbitHide")
//                .setCreativeTab(CreativeTabs.MATERIALS));
//        registerItem(
//            416,
//            "armor_stand",
//            (new WrapperItemArmorStand()).setUnlocalizedName("armorStand")
//                .setMaxStackSize(16));
//        registerItem(
//            417,
//            "iron_horse_armor",
//            (new WrapperItem()).setUnlocalizedName("horsearmormetal")
//                .setMaxStackSize(1)
//                .setCreativeTab(CreativeTabs.MISC));
//        registerItem(
//            418,
//            "golden_horse_armor",
//            (new WrapperItem()).setUnlocalizedName("horsearmorgold")
//                .setMaxStackSize(1)
//                .setCreativeTab(CreativeTabs.MISC));
//        registerItem(
//            419,
//            "diamond_horse_armor",
//            (new WrapperItem()).setUnlocalizedName("horsearmordiamond")
//                .setMaxStackSize(1)
//                .setCreativeTab(CreativeTabs.MISC));
//        registerItem(420, "lead", (new WrapperItemLead()).setUnlocalizedName("leash"));
//        registerItem(421, "name_tag", (new WrapperItemNameTag()).setUnlocalizedName("nameTag"));
//        registerItem(
//            422,
//            "command_block_minecart",
//            (new WrapperItemMinecart(EntityMinecart.Type.COMMAND_BLOCK)).setUnlocalizedName("minecartCommandBlock")
//                .setCreativeTab((CreativeTabs) null));
//        registerItem(423, "mutton", (new WrapperItemFood(2, 0.3F, true)).setUnlocalizedName("muttonRaw"));
//        registerItem(424, "cooked_mutton", (new WrapperItemFood(6, 0.8F, true)).setUnlocalizedName("muttonCooked"));
//        registerItem(425, "banner", (new WrapperItemBanner()).setUnlocalizedName("banner"));
//        registerItem(426, "end_crystal", new WrapperItemEndCrystal());
//        registerItem(427, "spruce_door", (new WrapperItemDoor(WrapperBlocks.SPRUCE_DOOR)).setUnlocalizedName("doorSpruce"));
//        registerItem(428, "birch_door", (new WrapperItemDoor(WrapperBlocks.BIRCH_DOOR)).setUnlocalizedName("doorBirch"));
//        registerItem(429, "jungle_door", (new WrapperItemDoor(WrapperBlocks.JUNGLE_DOOR)).setUnlocalizedName("doorJungle"));
//        registerItem(430, "acacia_door", (new WrapperItemDoor(WrapperBlocks.ACACIA_DOOR)).setUnlocalizedName("doorAcacia"));
//        registerItem(431, "dark_oak_door", (new WrapperItemDoor(WrapperBlocks.DARK_OAK_DOOR)).setUnlocalizedName("doorDarkOak"));
//        registerItem(
//            432,
//            "chorus_fruit",
//            (new WrapperItemChorusFruit(4, 0.3F)).setAlwaysEdible()
//                .setUnlocalizedName("chorusFruit")
//                .setCreativeTab(CreativeTabs.MATERIALS));
//        registerItem(
//            433,
//            "chorus_fruit_popped",
//            (new WrapperItem()).setUnlocalizedName("chorusFruitPopped")
//                .setCreativeTab(CreativeTabs.MATERIALS));
//        registerItem(434, "beetroot", (new WrapperItemFood(1, 0.6F, false)).setUnlocalizedName("beetroot"));
//        registerItem(
//            435,
//            "beetroot_seeds",
//            (new WrapperItemSeeds(WrapperBlocks.BEETROOTS, WrapperBlocks.FARMLAND)).setUnlocalizedName("beetroot_seeds"));
//        registerItem(436, "beetroot_soup", (new WrapperItemSoup(6)).setUnlocalizedName("beetroot_soup"));
//        registerItem(
//            437,
//            "dragon_breath",
//            (new WrapperItem()).setCreativeTab(CreativeTabs.BREWING)
//                .setUnlocalizedName("dragon_breath")
//                .setContainerItem(wrapperItem1));
//        registerItem(438, "splash_potion", (new WrapperItemSplashPotion()).setUnlocalizedName("splash_potion"));
//        registerItem(439, "spectral_arrow", (new WrapperItemSpectralArrow()).setUnlocalizedName("spectral_arrow"));
//        registerItem(440, "tipped_arrow", (new WrapperItemTippedArrow()).setUnlocalizedName("tipped_arrow"));
//        registerItem(441, "lingering_potion", (new WrapperItemLingeringPotion()).setUnlocalizedName("lingering_potion"));
//        registerItem(442, "shield", (new WrapperItemShield()).setUnlocalizedName("shield"));
//        registerItem(443, "elytra", (new WrapperItemElytra()).setUnlocalizedName("elytra"));
//        registerItem(444, "spruce_boat", new WrapperItemBoat(EntityBoat.Type.SPRUCE));
//        registerItem(445, "birch_boat", new WrapperItemBoat(EntityBoat.Type.BIRCH));
//        registerItem(446, "jungle_boat", new WrapperItemBoat(EntityBoat.Type.JUNGLE));
//        registerItem(447, "acacia_boat", new WrapperItemBoat(EntityBoat.Type.ACACIA));
//        registerItem(448, "dark_oak_boat", new WrapperItemBoat(EntityBoat.Type.DARK_OAK));
//        registerItem(
//            449,
//            "totem_of_undying",
//            (new WrapperItem()).setUnlocalizedName("totem")
//                .setMaxStackSize(1)
//                .setCreativeTab(CreativeTabs.COMBAT));
//        registerItem(
//            450,
//            "shulker_shell",
//            (new WrapperItem()).setUnlocalizedName("shulkerShell")
//                .setCreativeTab(CreativeTabs.MATERIALS));
//        registerItem(
//            452,
//            "iron_nugget",
//            (new WrapperItem()).setUnlocalizedName("ironNugget")
//                .setCreativeTab(CreativeTabs.MATERIALS));
//        registerItem(453, "knowledge_book", (new WrapperItemKnowledgeBook()).setUnlocalizedName("knowledgeBook"));
//        registerItem(2256, "record_13", (new WrapperItemRecord("13", SoundEvents.RECORD_13)).setUnlocalizedName("record"));
//        registerItem(2257, "record_cat", (new WrapperItemRecord("cat", SoundEvents.RECORD_CAT)).setUnlocalizedName("record"));
//        registerItem(
//            2258,
//            "record_blocks",
//            (new WrapperItemRecord("blocks", SoundEvents.RECORD_BLOCKS)).setUnlocalizedName("record"));
//        registerItem(
//            2259,
//            "record_chirp",
//            (new WrapperItemRecord("chirp", SoundEvents.RECORD_CHIRP)).setUnlocalizedName("record"));
//        registerItem(2260, "record_far", (new WrapperItemRecord("far", SoundEvents.RECORD_FAR)).setUnlocalizedName("record"));
//        registerItem(
//            2261,
//            "record_mall",
//            (new WrapperItemRecord("mall", SoundEvents.RECORD_MALL)).setUnlocalizedName("record"));
//        registerItem(
//            2262,
//            "record_mellohi",
//            (new WrapperItemRecord("mellohi", SoundEvents.RECORD_MELLOHI)).setUnlocalizedName("record"));
//        registerItem(
//            2263,
//            "record_stal",
//            (new WrapperItemRecord("stal", SoundEvents.RECORD_STAL)).setUnlocalizedName("record"));
//        registerItem(
//            2264,
//            "record_strad",
//            (new WrapperItemRecord("strad", SoundEvents.RECORD_STRAD)).setUnlocalizedName("record"));
//        registerItem(
//            2265,
//            "record_ward",
//            (new WrapperItemRecord("ward", SoundEvents.RECORD_WARD)).setUnlocalizedName("record"));
//        registerItem(2266, "record_11", (new WrapperItemRecord("11", SoundEvents.RECORD_11)).setUnlocalizedName("record"));
//        registerItem(
//            2267,
//            "record_wait",
//            (new WrapperItemRecord("wait", SoundEvents.RECORD_WAIT)).setUnlocalizedName("record"));
//    }

    /**
     * Register a default ItemBlock for the given Block.
     */
    private static void registerItemBlock(Block blockIn) {
        registerItemBlock(blockIn, new WrapperItemBlock(blockIn));
    }

    /**
     * Register the given Item as the ItemBlock for the given Block.
     */
    protected static void registerItemBlock(Block blockIn, WrapperItem wrapperItemIn) {
        registerItem(Block.getIdFromBlock(blockIn), Block.REGISTRY.getNameForObject(blockIn), wrapperItemIn);
        BLOCK_TO_ITEM.put(blockIn, wrapperItemIn);
    }

    private static void registerItem(int id, String textualID, WrapperItem wrapperItemIn) {
        registerItem(id, new ResourceLocation(textualID), wrapperItemIn);
    }

    private static void registerItem(int id, ResourceLocation textualID, WrapperItem wrapperItemIn) {
        REGISTRY.register(id, textualID, wrapperItemIn);
    }

    @SideOnly(Side.CLIENT)
    public TempItemStack getDefaultInstance() {
        return new TempItemStack(this);
    }

    public static enum ToolMaterial {

        WOOD(0, 59, 2.0F, 0.0F, 15),
        STONE(1, 131, 4.0F, 1.0F, 5),
        IRON(2, 250, 6.0F, 2.0F, 14),
        DIAMOND(3, 1561, 8.0F, 3.0F, 10),
        GOLD(0, 32, 12.0F, 0.0F, 22);

        /**
         * The level of material this tool can harvest (3 = DIAMOND, 2 = IRON, 1 = STONE, 0 = WOOD/GOLD)
         */
        private final int harvestLevel;
        /**
         * The number of uses this material allows. (wood = 59, stone = 131, iron = 250, diamond = 1561, gold = 32)
         */
        private final int maxUses;
        /**
         * The strength of this tool material against blocks which it is effective against.
         */
        private final float efficiency;
        /**
         * Damage versus entities.
         */
        private final float attackDamage;
        /**
         * Defines the natural enchantability factor of the material.
         */
        private final int enchantability;
        // Added by forge for custom Tool materials.
        private TempItemStack repairMaterial = TempItemStack.EMPTY;

        private ToolMaterial(int harvestLevel, int maxUses, float efficiency, float damageVsEntity,
                             int enchantability) {
            this.harvestLevel = harvestLevel;
            this.maxUses = maxUses;
            this.efficiency = efficiency;
            this.attackDamage = damageVsEntity;
            this.enchantability = enchantability;
        }

        /**
         * The number of uses this material allows. (wood = 59, stone = 131, iron = 250, diamond = 1561, gold = 32)
         */
        public int getMaxUses() {
            return this.maxUses;
        }

        /**
         * The strength of this tool material against blocks which it is effective against.
         */
        public float getEfficiency() {
            return this.efficiency;
        }

        /**
         * Returns the damage against a given entity.
         */
        public float getAttackDamage() {
            return this.attackDamage;
        }

        /**
         * The level of material this tool can harvest (3 = DIAMOND, 2 = IRON, 1 = STONE, 0 = IRON/GOLD)
         */
        public int getHarvestLevel() {
            return this.harvestLevel;
        }

        /**
         * Return the natural enchantability factor of the material.
         */
        public int getEnchantability() {
            return this.enchantability;
        }

        @Deprecated // Use getRepairItemStack below
        public WrapperItem getRepairItem() {
            if (this == WOOD) {
                return WrapperItem.getItemFromBlock(WrapperBlocks.PLANKS);
            } else if (this == STONE) {
                return WrapperItem.getItemFromBlock(WrapperBlocks.COBBLESTONE);
            } else if (this == GOLD) {
                return Items.GOLD_INGOT;
            } else if (this == IRON) {
                return Items.IRON_INGOT;
            } else {
                return this == DIAMOND ? Items.DIAMOND : null;
            }
        }

        public ToolMaterial setRepairItem(TempItemStack stack) {
            if (!this.repairMaterial.isEmpty()) throw new RuntimeException("Repair material has already been set");
            if (this == WOOD || this == STONE || this == GOLD || this == IRON || this == DIAMOND)
                throw new RuntimeException("Can not change vanilla tool repair materials");
            this.repairMaterial = stack;
            return this;
        }

        public TempItemStack getRepairItemStack() {
            if (!repairMaterial.isEmpty()) return repairMaterial;
            WrapperItem ret = this.getRepairItem();
            if (ret != null)
                repairMaterial = new TempItemStack(ret, 1, net.minecraftforge.oredict.OreDictionary.WILDCARD_VALUE);
            return repairMaterial;
        }
    }
}
