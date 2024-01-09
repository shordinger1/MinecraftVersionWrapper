package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shordinger.ModWrapper.migration.wrapper.minecraft.block.material.MapColor;
import shordinger.ModWrapper.migration.wrapper.minecraft.block.material.WrapperMaterial;
import shordinger.ModWrapper.migration.wrapper.minecraft.block.state.BlockFaceShape;
import shordinger.ModWrapper.migration.wrapper.minecraft.block.state.IWrapperBlockState;
import shordinger.ModWrapper.migration.wrapper.minecraft.init.WrapperBlocks;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.EnumBlockRenderType;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.Mirror;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.WrapperRotation;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.AxisAlignedBB;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.MathHelper;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.RayTraceResult;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.Vec3d;
import shordinger.ModWrapper.migration.wrapper.minecraft.world.IWrapperBlockAccess;

public class WrapperBlock extends Block {

    /**
     * ResourceLocation for the Air block
     */
    private static final ResourceLocation AIR_ID = new ResourceLocation("air");
    @Deprecated // Modders: DO NOT use this! Use GameRegistry
    public static final ObjectIntIdentityMap<IWrapperBlockState> BLOCK_STATE_IDS = net.minecraftforge.registries.GameData
        .getWrapperBlockstateIDMap();
    public static final AxisAlignedBB FULL_BLOCK_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    @Nullable
    public static final AxisAlignedBB NULL_AABB = null;
    private CreativeTabs displayOnCreativeTab;
    protected boolean fullBlock;

    protected boolean translucent;
    /**
     * Amount of light emitted
     */
    protected boolean useNeighborBrightness;

    /**
     * Flags whether or not this block is of a type that needs random ticking. Ref-counted by ExtendedWrapperBlockstorage in
     * order to broadly cull a chunk from the random chunk update list for efficiency's sake.
     */
    protected boolean needsRandomTick;
    /**
     * true if the Block contains a Tile Entity
     */
    protected boolean hasTileEntity;
    /**
     * Sound of stepping on the block
     */
    protected SoundType WrapperBlocksoundType;
    public float blockParticleGravity;
    protected final WrapperMaterial blockWrapperMaterial;
    /**
     * The Block's MapColor
     */
    protected final MapColor blockMapColor;
    /**
     * Determines how much velocity is maintained while moving on top of this block
     */
    @Deprecated // Forge: State/world/pos/entity sensitive version below
    public float slipperiness;
    public WrapperBlockstateContainer WrapperBlockstate;
    private IWrapperBlockState defaultWrapperBlockstate;
    private String unlocalizedName;

    public static int getIdFromBlock(WrapperBlock wrapperBlockIn) {
        return REGISTRY.getIDForObject(wrapperBlockIn);
    }

    /**
     * Get a unique ID for the given WrapperBlockstate, containing both BlockID and metadata
     */
    public static int getStateId(IWrapperBlockState state) {
        WrapperBlock wrapperBlock = state.getBlock();
        return getIdFromBlock(wrapperBlock) + (wrapperBlock.getMetaFromState(state) << 12);
    }

    public static WrapperBlock getBlockById(int id) {
        return REGISTRY.getObjectById(id);
    }

    /**
     * Get a WrapperBlockstate by it's ID (see getStateId)
     */
    public static IWrapperBlockState getStateById(int id) {
        int i = id & 4095;
        int j = id >> 12 & 15;
        return getBlockById(i).getStateFromMeta(j);
    }

    public static WrapperBlock getBlockFromItem(@Nullable Item itemIn) {
        return itemIn instanceof ItemBlock ? ((ItemBlock) itemIn).getBlock() : WrapperBlocks.AIR;
    }

    @Nullable
    public static WrapperBlock getBlockFromName(String name) {
        ResourceLocation resourcelocation = new ResourceLocation(name);

        if (blockRegistry.containsKey(resourcelocation)) {
            return (WrapperBlock) blockRegistry.getObject(resourcelocation);
        } else {
            try {
                return (WrapperBlock) blockRegistry.getObjectById(Integer.parseInt(name));
            } catch (NumberFormatException var3) {
                return null;
            }
        }
    }

    /**
     * Determines if the block is solid enough on the top side to support other WrapperBlocks, like redstone components.
     */
    @Deprecated
    public boolean isTopSolid(IWrapperBlockState state) {
        return state.getMaterial()
            .isOpaque() && state.isFullCube();
    }

    /**
     * @return true if the state occupies all of its 1x1x1 cube
     */
    @Deprecated
    public boolean isFullBlock(IWrapperBlockState state) {
        return this.fullBlock;
    }

    @Deprecated
    public boolean canEntitySpawn(IWrapperBlockState state, Entity entityIn) {
        return true;
    }

    @Deprecated
    public int getLightOpacity(IWrapperBlockState state) {
        return this.lightOpacity;
    }

    /**
     * Used in the renderer to apply ambient occlusion
     */
    @Deprecated
    @SideOnly(Side.CLIENT)
    public boolean isTranslucent(IWrapperBlockState state) {
        return this.translucent;
    }

    @Deprecated
    public int getLightValue(IWrapperBlockState state) {
        return this.lightValue;
    }

    /**
     * Should block use the brightest neighbor light value as its own
     */
    @Deprecated
    public boolean getUseNeighborBrightness(IWrapperBlockState state) {
        return this.useNeighborBrightness;
    }

    /**
     * Get a material of block
     */
    @Deprecated
    public WrapperMaterial getMaterial(IWrapperBlockState state) {
        return this.blockWrapperMaterial;
    }

    /**
     * Get the MapColor for this Block and the given WrapperBlockstate
     */
    @Deprecated
    public MapColor getMapColor(IWrapperBlockState state, IWrapperBlockAccess worldIn, BlockPos pos) {
        return this.blockMapColor;
    }

    /**
     * Convert the given metadata into a WrapperBlockstate for this Block
     */
    @Deprecated
    public IWrapperBlockState getStateFromMeta(int meta) {
        return this.getDefaultState();
    }

    /**
     * Convert the WrapperBlockstate into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        if (state.getPropertyKeys()
            .isEmpty()) {
            return 0;
        } else {
            throw new IllegalArgumentException("Don't know how to convert " + state + " back into data...");
        }
    }

    /**
     * Get the actual Block state of this Block at the given position. This applies properties not visible in the
     * metadata, such as fence connections.
     */
    @Deprecated
    public IWrapperBlockState getActualState(IWrapperBlockState state, IWrapperBlockAccess worldIn, BlockPos pos) {
        return state;
    }

    /**
     * Returns the WrapperBlockstate with the given rotation from the passed WrapperBlockstate. If inapplicable, returns the passed
     * WrapperBlockstate.
     */
    @Deprecated
    public IWrapperBlockState withRotation(IWrapperBlockState state, WrapperRotation rot) {
        return state;
    }

    /**
     * Returns the WrapperBlockstate with the given mirror of the passed WrapperBlockstate. If inapplicable, returns the passed
     * WrapperBlockstate.
     */
    @Deprecated
    public IWrapperBlockState withMirror(IWrapperBlockState state, Mirror mirrorIn) {
        return state;
    }

    public WrapperBlock(WrapperMaterial blockWrapperMaterialIn, MapColor blockMapColorIn) {
        super();
        this.enableStats = true;
        this.WrapperBlocksoundType = SoundType.STONE;
        this.blockParticleGravity = 1.0F;
        this.slipperiness = 0.6F;
        this.blockWrapperMaterial = blockWrapperMaterialIn;
        this.blockMapColor = blockMapColorIn;
        this.WrapperBlockstate = this.createWrapperBlockstate();
        this.setDefaultState(this.WrapperBlockstate.getBaseState());
        this.fullBlock = this.getDefaultState()
            .isOpaqueCube();
        this.lightOpacity = this.fullBlock ? 255 : 0;
        this.translucent = !blockWrapperMaterialIn.WrapperBlocksLight();
    }

    public WrapperBlock(WrapperMaterial wrapperMaterialIn) {
        this(wrapperMaterialIn, wrapperMaterialIn.getMaterialMapColor());
    }

    /**
     * Sets the footstep sound for the block. Returns the object for convenience in constructing.
     */
    protected WrapperBlock setSoundType(SoundType sound) {
        this.WrapperBlocksoundType = sound;
        return this;
    }

    /**
     * Sets how much light is blocked going through this block. Returns the object for convenience in constructing.
     */
    public WrapperBlock setLightOpacity(int opacity) {
        this.lightOpacity = opacity;
        return this;
    }

    /**
     * Sets the light value that the block emits. Returns resulting block instance for constructing convenience.
     */
    public WrapperBlock setLightLevel(float value) {
        this.lightValue = (int) (15.0F * value);
        return this;
    }

    /**
     * Sets the the WrapperBlocks resistance to explosions. Returns the object for convenience in constructing.
     */
    public WrapperBlock setResistance(float resistance) {
        this.blockResistance = resistance * 3.0F;
        return this;
    }

    protected static boolean isExceptionBlockForAttaching(WrapperBlock attachWrapperBlock) {
        return attachWrapperBlock instanceof WrapperWrapperBlockshulkerBox || attachWrapperBlock instanceof WrapperBlockLeaves
            || attachWrapperBlock instanceof WrapperBlockTrapDoor
            || attachWrapperBlock == WrapperBlocks.BEACON
            || attachWrapperBlock == WrapperBlocks.CAULDRON
            || attachWrapperBlock == WrapperBlocks.GLASS
            || attachWrapperBlock == WrapperBlocks.GLOWSTONE
            || attachWrapperBlock == WrapperBlocks.ICE
            || attachWrapperBlock == WrapperBlocks.SEA_LANTERN
            || attachWrapperBlock == WrapperBlocks.STAINED_GLASS;
    }

    protected static boolean isExceptBlockForAttachWithPiston(WrapperBlock attachWrapperBlock) {
        return isExceptionBlockForAttaching(attachWrapperBlock) || attachWrapperBlock == WrapperBlocks.PISTON
            || attachWrapperBlock == WrapperBlocks.STICKY_PISTON
            || attachWrapperBlock == WrapperBlocks.PISTON_HEAD;
    }

    /**
     * Indicate if a material is a normal solid opaque cube
     */
    @Deprecated
    public boolean isBlockNormalCube(IWrapperBlockState state) {
        return state.getMaterial()
            .WrapperBlocksMovement() && state.isFullCube();
    }

    /**
     * Used for nearly all game logic (non-rendering) purposes. Use Forge-provided isNormalCube(IWrapperBlockAccess, BlockPos)
     * instead.
     */
    @Deprecated
    public boolean isNormalCube(IWrapperBlockState state) {
        return state.getMaterial()
            .isOpaque() && state.isFullCube()
            && !state.canProvidePower();
    }

    @Deprecated
    public boolean causesSuffocation(IWrapperBlockState state) {
        return this.blockWrapperMaterial.WrapperBlocksMovement() && this.getDefaultState()
            .isFullCube();
    }

    @Deprecated
    public boolean isFullCube(IWrapperBlockState state) {
        return true;
    }

    @Deprecated
    @SideOnly(Side.CLIENT)
    public boolean hasCustomBreakingProgress(IWrapperBlockState state) {
        return false;
    }

    /**
     * Determines if an entity can path through this block
     */
    public boolean isPassable(IWrapperBlockAccess worldIn, BlockPos pos) {
        return !this.blockWrapperMaterial.WrapperBlocksMovement();
    }

    /**
     * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
     * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
     */
    @Deprecated
    public EnumBlockRenderType getRenderType(IWrapperBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    /**
     * Whether this Block can be replaced directly by other WrapperBlocks (true for e.g. tall grass)
     */
    public boolean isReplaceable(IWrapperBlockAccess worldIn, BlockPos pos) {
        return worldIn.getWrapperBlockstate(pos)
            .getMaterial()
            .isReplaceable();
    }

    /**
     * Sets how many hits it takes to break a block.
     */
    public WrapperBlock setHardness(float hardness) {
        this.blockHardness = hardness;

        if (this.blockResistance < hardness * 5.0F) {
            this.blockResistance = hardness * 5.0F;
        }

        return this;
    }

    public WrapperBlock setBlockUnbreakable() {
        this.setHardness(-1.0F);
        return this;
    }

    @Deprecated
    public float getBlockHardness(IWrapperBlockState WrapperBlockstate, World worldIn, BlockPos pos) {
        return this.blockHardness;
    }

    /**
     * Sets whether this block type will receive random update ticks
     */
    public WrapperBlock setTickRandomly(boolean shouldTick) {
        this.needsRandomTick = shouldTick;
        return this;
    }

    /**
     * Returns whether or not this block is of a type that needs random ticking. Called for ref-counting purposes by
     * ExtendedWrapperBlockstorage in order to broadly cull a chunk from the random chunk update list for efficiency's sake.
     */
    public boolean getTickRandomly() {
        return this.needsRandomTick;
    }

    @Deprecated // Forge: New State sensitive version.
    public boolean hasTileEntity() {
        return hasTileEntity(getDefaultState());
    }

    @Deprecated
    public AxisAlignedBB getBoundingBox(IWrapperBlockState state, IWrapperBlockAccess source, BlockPos pos) {
        return FULL_BLOCK_AABB;
    }

    @Deprecated
    @SideOnly(Side.CLIENT)
    public int getPackedLightmapCoords(IWrapperBlockState state, IWrapperBlockAccess source, BlockPos pos) {
        int i = source.getCombinedLight(pos, state.getLightValue(source, pos));

        if (i == 0 && state.getBlock() instanceof WrapperWrapperBlockslab) {
            pos = pos.down();
            state = source.getWrapperBlockstate(pos);
            return source.getCombinedLight(pos, state.getLightValue(source, pos));
        } else {
            return i;
        }
    }

    @Deprecated
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IWrapperBlockState WrapperBlockstate, IWrapperBlockAccess blockAccess, BlockPos pos,
                                        EnumFacing side) {
        AxisAlignedBB axisalignedbb = WrapperBlockstate.getBoundingBox(blockAccess, pos);

        switch (side) {
            case DOWN:

                if (axisalignedbb.minY > 0.0D) {
                    return true;
                }

                break;
            case UP:

                if (axisalignedbb.maxY < 1.0D) {
                    return true;
                }

                break;
            case NORTH:

                if (axisalignedbb.minZ > 0.0D) {
                    return true;
                }

                break;
            case SOUTH:

                if (axisalignedbb.maxZ < 1.0D) {
                    return true;
                }

                break;
            case WEST:

                if (axisalignedbb.minX > 0.0D) {
                    return true;
                }

                break;
            case EAST:

                if (axisalignedbb.maxX < 1.0D) {
                    return true;
                }
        }

        return !blockAccess.getWrapperBlockstate(pos.offset(side))
            .doesSideBlockRendering(blockAccess, pos.offset(side), side.getOpposite());
    }

    /**
     * Get the geometry of the queried face at the given position and state. This is used to decide whether things like
     * buttons are allowed to be placed on the face, or how glass panes connect to the face, among other things.
     * <p>
     * Common values are {@code SOLID}, which is the default, and {@code UNDEFINED}, which represents something that
     * does not fit the other descriptions and will generally cause other things not to connect to the face.
     *
     * @return an approximation of the form of the given face
     */
    @Deprecated
    public BlockFaceShape getBlockFaceShape(IWrapperBlockAccess worldIn, IWrapperBlockState state, BlockPos pos,
                                            EnumFacing face) {
        return BlockFaceShape.SOLID;
    }

    @Deprecated
    public void addCollisionBoxToList(IWrapperBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox,
                                      List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        addCollisionBoxToList(pos, entityBox, collidingBoxes, state.getCollisionBoundingBox(worldIn, pos));
    }

    protected static void addCollisionBoxToList(BlockPos pos, AxisAlignedBB entityBox,
                                                List<AxisAlignedBB> collidingBoxes, @Nullable AxisAlignedBB blockBox) {
        if (blockBox != NULL_AABB) {
            AxisAlignedBB axisalignedbb = blockBox.offset(pos);

            if (entityBox.intersects(axisalignedbb)) {
                collidingBoxes.add(axisalignedbb);
            }
        }
    }

    @Deprecated
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IWrapperBlockState WrapperBlockstate, IWrapperBlockAccess worldIn, BlockPos pos) {
        return WrapperBlockstate.getBoundingBox(worldIn, pos);
    }

    /**
     * Return an AABB (in world coords!) that should be highlighted when the player is targeting this Block
     */
    @Deprecated
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBox(IWrapperBlockState state, World worldIn, BlockPos pos) {
        return state.getBoundingBox(worldIn, pos)
            .offset(pos);
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
    @Deprecated
    public boolean isOpaqueCube(IWrapperBlockState state) {
        return true;
    }

    public boolean canCollideCheck(IWrapperBlockState state, boolean hitIfLiquid) {
        return this.isCollidable();
    }

    /**
     * Returns if this block is collidable. Only used by fire, although stairs return that of the block that the stair
     * is made of (though nobody's going to make fire stairs, right?)
     */
    public boolean isCollidable() {
        return true;
    }

    /**
     * Called randomly when setTickRandomly is set to true (used by e.g. crops to grow, etc.)
     */
    public void randomTick(World worldIn, BlockPos pos, IWrapperBlockState state, Random random) {
        this.updateTick(worldIn, pos, state, random);
    }

    public void updateTick(World worldIn, BlockPos pos, IWrapperBlockState state, Random rand) {
    }
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IWrapperBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
    }

    /**
     * Called after a player destroys this Block - the posiiton pos may no longer hold the state indicated.
     */
    public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IWrapperBlockState state) {
    }

    /**
     * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
     * change. Cases may include when redstone power is updated, cactus WrapperBlocks popping off due to a neighboring solid
     * block, etc.
     */
    @Deprecated
    public void neighborChanged(IWrapperBlockState state, World worldIn, BlockPos pos, WrapperBlock wrapperBlockIn,
                                BlockPos fromPos) {
    }


    /**
     * Called after the block is set in the Chunk data, but before the Tile Entity is set
     */
    public void onBlockAdded(World worldIn, BlockPos pos, IWrapperBlockState state) {
    }

    /**
     * Called serverside after this block is replaced with another in Chunk, but before the Tile Entity is updated
     */
    public void breakBlock(World worldIn, BlockPos pos, IWrapperBlockState state) {
        if (hasTileEntity(state) && !(this instanceof WrapperBlockContainer)) {
            worldIn.removeTileEntity(pos);
        }
    }


    /**
     * Get the Item that this Block should drop when harvested.
     */
    public Item getItemDropped(IWrapperBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(this);
    }

    /**
     * Get the hardness of this Block relative to the ability of the given player
     */
    @Deprecated
    public float getPlayerRelativeBlockHardness(IWrapperBlockState state, EntityPlayer player, World worldIn,
                                                BlockPos pos) {
        return net.minecraftforge.common.ForgeHooks.WrapperBlockstrength(state, player, worldIn, pos);
    }

    /**
     * Spawn this Block's drops into the World as EntityItems
     */
    public final void dropBlockAsItem(World worldIn, BlockPos pos, IWrapperBlockState state, int fortune) {
        this.dropBlockAsItemWithChance(worldIn, pos, state, 1.0F, fortune);
    }

    /**
     * Spawns this Block's drops into the World as EntityItems.
     */
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IWrapperBlockState state, float chance,
                                          int fortune) {
        if (!worldIn.isRemote && !worldIn.restoringWrapperBlocksnapshots) // do not drop items while restoring WrapperBlockstates,
        // prevents item dupe
        {
            List<ItemStack> drops = getDrops(worldIn, pos, state, fortune); // use the old method until it gets removed,
            // for backward compatibility
            chance = net.minecraftforge.event.ForgeEventFactory
                .fireBlockHarvesting(drops, worldIn, pos, state, fortune, chance, false, harvesters.get());

            for (ItemStack drop : drops) {
                if (worldIn.rand.nextFloat() <= chance) {
                    spawnAsEntity(worldIn, pos, drop);
                }
            }
        }
    }

    /**
     * Spawns the given ItemStack as an EntityItem into the World at the given position
     */
    public static void spawnAsEntity(World worldIn, BlockPos pos, ItemStack stack) {
        if (!worldIn.isRemote && !stack.isEmpty()
            && worldIn.getGameRules()
            .getBoolean("doTileDrops")
            && !worldIn.restoringWrapperBlocksnapshots) // do not drop items while restoring WrapperBlockstates, prevents item dupe
        {
            if (captureDrops.get()) {
                capturedDrops.get()
                    .add(stack);
                return;
            }
            float f = 0.5F;
            double d0 = (double) (worldIn.rand.nextFloat() * 0.5F) + 0.25D;
            double d1 = (double) (worldIn.rand.nextFloat() * 0.5F) + 0.25D;
            double d2 = (double) (worldIn.rand.nextFloat() * 0.5F) + 0.25D;
            EntityItem entityitem = new EntityItem(
                worldIn,
                (double) pos.getX() + d0,
                (double) pos.getY() + d1,
                (double) pos.getZ() + d2,
                stack);
            entityitem.setDefaultPickupDelay();
            worldIn.spawnEntity(entityitem);
        }
    }

    /**
     * Spawns the given amount of experience into the World as XP orb entities
     */
    public void dropXpOnBlockBreak(World worldIn, BlockPos pos, int amount) {
        if (!worldIn.isRemote && worldIn.getGameRules()
            .getBoolean("doTileDrops")) {
            while (amount > 0) {
                int i = EntityXPOrb.getXPSplit(amount);
                amount -= i;
                worldIn.spawnEntity(
                    new EntityXPOrb(
                        worldIn,
                        (double) pos.getX() + 0.5D,
                        (double) pos.getY() + 0.5D,
                        (double) pos.getZ() + 0.5D,
                        i));
            }
        }
    }

    /**
     * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
     * returns the metadata of the dropped item based on the old metadata of the block.
     */
    public int damageDropped(IWrapperBlockState state) {
        return 0;
    }

    /**
     * Returns how much this block can resist explosions from the passed in entity.
     */
    @Deprecated // Forge: State sensitive version
    public float getExplosionResistance(Entity exploder) {
        return this.blockResistance / 5.0F;
    }

    /**
     * Ray traces through the WrapperBlocks collision from start vector to end vector returning a ray trace hit.
     */
    @Deprecated
    @Nullable
    public RayTraceResult collisionRayTrace(IWrapperBlockState WrapperBlockstate, World worldIn, BlockPos pos, Vec3d start,
                                            Vec3d end) {
        return this.rayTrace(pos, start, end, WrapperBlockstate.getBoundingBox(worldIn, pos));
    }

    @Nullable
    protected RayTraceResult rayTrace(BlockPos pos, Vec3d start, Vec3d end, AxisAlignedBB boundingBox) {
        Vec3d vec3d = start.subtract((double) pos.getX(), (double) pos.getY(), (double) pos.getZ());
        Vec3d vec3d1 = end.subtract((double) pos.getX(), (double) pos.getY(), (double) pos.getZ());
        RayTraceResult raytraceresult = boundingBox.calculateIntercept(vec3d, vec3d1);
        return raytraceresult == null ? null
            : new RayTraceResult(
            raytraceresult.hitVec.addVector((double) pos.getX(), (double) pos.getY(), (double) pos.getZ()),
            raytraceresult.sideHit,
            pos);
    }

    /**
     * Called when this Block is destroyed by an Explosion
     */
    public void onBlockDestroyedByExplosion(World worldIn, BlockPos pos, Explosion explosionIn) {
    }

    /**
     * Gets the render layer this block will render on. SOLID for solid WrapperBlocks, CUTOUT or CUTOUT_MIPPED for on-off
     * transparency (glass, reeds), TRANSLUCENT for fully blended transparency (stained glass)
     */
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.SOLID;
    }

    /**
     * Check whether this Block can be placed at pos, while aiming at the specified side of an adjacent block
     */
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
        return this.canPlaceBlockAt(worldIn, pos);
    }

    /**
     * Checks if this block can be placed exactly at the given position.
     */
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return worldIn.getWrapperBlockstate(pos)
            .getBlock()
            .isReplaceable(worldIn, pos);
    }

    /**
     * Called when the block is right clicked by a player.
     */
    public boolean onBlockActivated(World worldIn, BlockPos pos, IWrapperBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return false;
    }

    /**
     * Called when the given entity walks on this Block
     */
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
    }

    // Forge: use getStateForPlacement

    /**
     * Called by ItemWrapperBlocks just before a block is actually set in the world, to allow for adjustments to the
     * IWrapperBlockState
     */
    @Deprecated
    public IWrapperBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX,
                                                   float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getStateFromMeta(meta);
    }

    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn) {
    }

    public Vec3d modifyAcceleration(World worldIn, BlockPos pos, Entity entityIn, Vec3d motion) {
        return motion;
    }

    @Deprecated
    public int getWeakPower(IWrapperBlockState WrapperBlockstate, IWrapperBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return 0;
    }

    /**
     * Can this block provide power. Only wire currently seems to have this change based on its state.
     */
    @Deprecated
    public boolean canProvidePower(IWrapperBlockState state) {
        return false;
    }

    /**
     * Called When an Entity Collided with the Block
     */
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IWrapperBlockState state, Entity entityIn) {
    }

    @Deprecated
    public int getStrongPower(IWrapperBlockState WrapperBlockstate, IWrapperBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return 0;
    }

    /**
     * Spawns the block's drops in the world. By the time this is called the Block has possibly been set to air via
     * Block.removedByPlayer
     */
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IWrapperBlockState state,
                             @Nullable TileEntity te, ItemStack stack) {
        player.addStat(StatList.getWrapperBlockstats(this));
        player.addExhaustion(0.005F);

        if (this.canSilkHarvest(worldIn, pos, state, player)
            && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0) {
            java.util.List<ItemStack> items = new java.util.ArrayList<ItemStack>();
            ItemStack itemstack = this.getSilkTouchDrop(state);

            if (!itemstack.isEmpty()) {
                items.add(itemstack);
            }

            net.minecraftforge.event.ForgeEventFactory
                .fireBlockHarvesting(items, worldIn, pos, state, 0, 1.0f, true, player);
            for (ItemStack item : items) {
                spawnAsEntity(worldIn, pos, item);
            }
        } else {
            harvesters.set(player);
            int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
            this.dropBlockAsItem(worldIn, pos, state, i);
            harvesters.set(null);
        }
    }

    @Deprecated // Forge: State sensitive version
    protected boolean canSilkHarvest() {
        return this.getDefaultState()
            .isFullCube() && !this.hasTileEntity(silk_check_state.get());
    }

    protected ItemStack getSilkTouchDrop(IWrapperBlockState state) {
        Item item = Item.getItemFromBlock(this);
        int i = 0;

        if (item.getHasSubtypes()) {
            i = this.getMetaFromState(state);
        }

        return new ItemStack(item, 1, i);
    }

    /**
     * Get the quantity dropped based on the given fortune level
     */
    public int quantityDroppedWithBonus(int fortune, Random random) {
        return this.quantityDropped(random);
    }

    /**
     * Called by ItemWrapperBlocks after a block is set in the world, to allow post-place logic
     */
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IWrapperBlockState state, EntityLivingBase placer,
                                ItemStack stack) {
    }

    /**
     * Return true if an entity can be spawned inside the block (used to get the player's bed spawn location)
     */
    public boolean canSpawnInBlock() {
        return !this.blockWrapperMaterial.isSolid() && !this.blockWrapperMaterial.isLiquid();
    }

    public WrapperBlock setUnlocalizedName(String name) {
        this.unlocalizedName = name;
        return this;
    }

    /**
     * Gets the localized name of this block. Used for the statistics page.
     */
    public String getLocalizedName() {
        return I18n.translateToLocal(this.getUnlocalizedName() + ".name");
    }

    /**
     * Returns the unlocalized name of the block with "tile." appended to the front.
     */
    public String getUnlocalizedName() {
        return "tile." + this.unlocalizedName;
    }

    /**
     * Called on server when World#addBlockEvent is called. If server returns true, then also called on the client. On
     * the Server, this may perform additional changes to the world, like pistons replacing the block with an extended
     * base. On the client, the update may involve replacing tile entities or effects such as sounds or particles
     */
    @Deprecated
    public boolean eventReceived(IWrapperBlockState state, World worldIn, BlockPos pos, int id, int param) {
        return false;
    }

    /**
     * Return the state of WrapperBlocks statistics flags - if the block is counted for mined and placed.
     */
    public boolean getEnableStats() {
        return this.enableStats;
    }

    protected WrapperBlock disableStats() {
        this.enableStats = false;
        return this;
    }

    @Deprecated
    public EnumPushReaction getMobilityFlag(IWrapperBlockState state) {
        return this.blockWrapperMaterial.getMobilityFlag();
    }

    @Deprecated
    @SideOnly(Side.CLIENT)
    public float getAmbientOcclusionLightValue(IWrapperBlockState state) {
        return state.isBlockNormalCube() ? 0.2F : 1.0F;
    }

    /**
     * Block's chance to react to a living entity falling on it.
     */
    public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
        entityIn.fall(fallDistance, 1.0F);
    }

    /**
     * Called when an Entity lands on this Block. This method *must* update motionY because the entity will not do that
     * on its own
     */
    public void onLanded(World worldIn, Entity entityIn) {
        entityIn.motionY = 0.0D;
    }

    @Deprecated // Forge: Use more sensitive version below: getPickBlock
    public ItemStack getItem(World worldIn, BlockPos pos, IWrapperBlockState state) {
        return new ItemStack(Item.getItemFromBlock(this), 1, this.damageDropped(state));
    }

    /**
     * returns a list of WrapperBlocks with the same ID, but different meta (eg: wood returns 4 WrapperBlocks)
     */
    public void getSubWrapperBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        items.add(new ItemStack(this));
    }

    /**
     * Returns the CreativeTab to display the given block on.
     */
    public CreativeTabs getCreativeTabToDisplayOn() {
        return this.displayOnCreativeTab;
    }

    public WrapperBlock setCreativeTab(CreativeTabs tab) {
        this.displayOnCreativeTab = tab;
        return this;
    }

    /**
     * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually
     * collect this block
     */
    public void onBlockHarvested(World worldIn, BlockPos pos, IWrapperBlockState state, EntityPlayer player) {
    }

    /**
     * Called similar to random ticks, but only when it is raining.
     */
    public void fillWithRain(World worldIn, BlockPos pos) {
    }

    public boolean requiresUpdates() {
        return true;
    }

    /**
     * Return whether this block can drop from an explosion.
     */
    public boolean canDropFromExplosion(Explosion explosionIn) {
        return true;
    }

    public boolean isAssociatedBlock(WrapperBlock other) {
        return this == other;
    }

    public static boolean isEqualTo(WrapperBlock wrapperBlockIn, WrapperBlock other) {
        if (wrapperBlockIn != null && other != null) {
            return wrapperBlockIn == other ? true : wrapperBlockIn.isAssociatedBlock(other);
        } else {
            return false;
        }
    }

    @Deprecated
    public boolean hasComparatorInputOverride(IWrapperBlockState state) {
        return false;
    }

    @Deprecated
    public int getComparatorInputOverride(IWrapperBlockState WrapperBlockstate, World worldIn, BlockPos pos) {
        return 0;
    }

    protected WrapperBlockstateContainer createWrapperBlockstate() {
        return new WrapperBlockstateContainer(this, new IProperty[0]);
    }

    public WrapperBlockstateContainer getWrapperBlockstate() {
        return this.WrapperBlockstate;
    }

    protected final void setDefaultState(IWrapperBlockState state) {
        this.defaultWrapperBlockstate = state;
    }

    public final IWrapperBlockState getDefaultState() {
        return this.defaultWrapperBlockstate;
    }

    /**
     * Get the OffsetType for this Block. Determines if the model is rendered slightly offset.
     */
    public WrapperBlock.EnumOffsetType getOffsetType() {
        return WrapperBlock.EnumOffsetType.NONE;
    }

    @Deprecated
    public Vec3d getOffset(IWrapperBlockState state, IWrapperBlockAccess worldIn, BlockPos pos) {
        WrapperBlock.EnumOffsetType block$enumoffsettype = this.getOffsetType();

        if (block$enumoffsettype == WrapperBlock.EnumOffsetType.NONE) {
            return Vec3d.ZERO;
        } else {
            long i = MathHelper.getCoordinateRandom(pos.getX(), 0, pos.getZ());
            return new Vec3d(
                ((double) ((float) (i >> 16 & 15L) / 15.0F) - 0.5D) * 0.5D,
                block$enumoffsettype == WrapperBlock.EnumOffsetType.XYZ
                    ? ((double) ((float) (i >> 20 & 15L) / 15.0F) - 1.0D) * 0.2D
                    : 0.0D,
                ((double) ((float) (i >> 24 & 15L) / 15.0F) - 0.5D) * 0.5D);
        }
    }

    @Deprecated // Forge - World/state/pos/entity sensitive version below
    public SoundType getSoundType() {
        return this.WrapperBlocksoundType;
    }

    public String toString() {
        return "Block{" + REGISTRY.getNameForObject(this) + "}";
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
    }

    /* ======================================== FORGE START ===================================== */
    // For ForgeInternal use Only!
    protected ThreadLocal<EntityPlayer> harvesters = new ThreadLocal();
    private ThreadLocal<IWrapperBlockState> silk_check_state = new ThreadLocal();
    protected static java.util.Random RANDOM = new java.util.Random(); // Useful for random things without a seed.

    /**
     * Gets the slipperiness at the given location at the given state. Normally
     * between 0 and 1.
     * <p>
     * Note that entities may reduce slipperiness by a certain factor of their own;
     * for {@link net.minecraft.entity.EntityLivingBase}, this is {@code .91}.
     * {@link net.minecraft.entity.item.EntityItem} uses {@code .98}, and
     * {@link net.minecraft.entity.projectile.EntityFishHook} uses {@code .92}.
     *
     * @param state  state of the block
     * @param world  the world
     * @param pos    the position in the world
     * @param entity the entity in question
     * @return the factor by which the entity's motion should be multiplied
     */
    public float getSlipperiness(IWrapperBlockState state, IWrapperBlockAccess world, BlockPos pos, @Nullable Entity entity) {
        return slipperiness;
    }

    /**
     * Sets the base slipperiness level. Normally between 0 and 1.
     * <p>
     * <b>Calling this method may have no effect on the function of this block</b>,
     * or may not have the expected result. This block is free to caclculate
     * its slipperiness arbitrarily. This method is guaranteed to work on the
     * base {@code Block} class.
     *
     * @param slipperiness the base slipperiness of this block
     * @see #getSlipperiness(IWrapperBlockState, IWrapperBlockAccess, BlockPos, Entity)
     */
    public void setDefaultSlipperiness(float slipperiness) {
        this.slipperiness = slipperiness;
    }

    /**
     * Get a light value for this block, taking into account the given state and coordinates, normal ranges are between
     * 0 and 15
     *
     * @param state Block state
     * @param world The current world
     * @param pos   Block position in world
     * @return The light value
     */
    public int getLightValue(IWrapperBlockState state, IWrapperBlockAccess world, BlockPos pos) {
        return state.getLightValue();
    }

    /**
     * Checks if a player or entity can use this block to 'climb' like a ladder.
     *
     * @param state  The current state
     * @param world  The current world
     * @param pos    Block position in world
     * @param entity The entity trying to use the ladder, CAN be null.
     * @return True if the block should act like a ladder
     */
    public boolean isLadder(IWrapperBlockState state, IWrapperBlockAccess world, BlockPos pos, EntityLivingBase entity) {
        return false;
    }

    /**
     * Return true if the block is a normal, solid cube. This
     * determines indirect power state, entity ejection from WrapperBlocks, and a few
     * others.
     *
     * @param state The current state
     * @param world The current world
     * @param pos   Block position in world
     * @return True if the block is a full cube
     */
    public boolean isNormalCube(IWrapperBlockState state, IWrapperBlockAccess world, BlockPos pos) {
        return state.isNormalCube();
    }

    /**
     * Check if the face of a block should block rendering.
     * <p>
     * Faces which are fully opaque should return true, faces with transparency
     * or faces which do not span the full size of the block should return false.
     *
     * @param state The current block state
     * @param world The current world
     * @param pos   Block position in world
     * @param face  The side to check
     * @return True if the block is opaque on the specified side.
     */
    public boolean doesSideBlockRendering(IWrapperBlockState state, IWrapperBlockAccess world, BlockPos pos, EnumFacing face) {
        return state.isOpaqueCube();
    }

    /**
     * Checks if the block is a solid face on the given side, used by placement logic.
     *
     * @param base_state The base state, getActualState should be called first
     * @param world      The current world
     * @param pos        Block position in world
     * @param side       The side to check
     * @return True if the block is solid on the specified side.
     */
    @Deprecated // Use IWrapperBlockState.getBlockFaceShape
    public boolean isSideSolid(IWrapperBlockState base_state, IWrapperBlockAccess world, BlockPos pos, EnumFacing side) {
        if (base_state.isTopSolid() && side == EnumFacing.UP) // Short circuit to vanilla function if its true
            return true;

        if (this instanceof WrapperWrapperBlockslab) {
            IWrapperBlockState state = this.getActualState(base_state, world, pos);
            return base_state.isFullBlock()
                || (state.getValue(WrapperWrapperBlockslab.HALF) == WrapperWrapperBlockslab.EnumBlockHalf.TOP
                && side == EnumFacing.UP)
                || (state.getValue(WrapperWrapperBlockslab.HALF) == WrapperWrapperBlockslab.EnumBlockHalf.BOTTOM
                && side == EnumFacing.DOWN);
        } else if (this instanceof WrapperBlockFarmland) {
            return (side != EnumFacing.DOWN && side != EnumFacing.UP);
        } else if (this instanceof WrapperWrapperBlockstairs) {
            IWrapperBlockState state = this.getActualState(base_state, world, pos);
            boolean flipped = state.getValue(WrapperWrapperBlockstairs.HALF) == WrapperWrapperBlockstairs.EnumHalf.TOP;
            WrapperWrapperBlockstairs.EnumShape shape = (WrapperWrapperBlockstairs.EnumShape) state
                .getValue(WrapperWrapperBlockstairs.SHAPE);
            EnumFacing facing = (EnumFacing) state.getValue(WrapperWrapperBlockstairs.FACING);
            if (side == EnumFacing.UP) return flipped;
            if (side == EnumFacing.DOWN) return !flipped;
            if (facing == side) return true;
            if (flipped) {
                if (shape == WrapperWrapperBlockstairs.EnumShape.INNER_LEFT) return side == facing.rotateYCCW();
                if (shape == WrapperWrapperBlockstairs.EnumShape.INNER_RIGHT) return side == facing.rotateY();
            } else {
                if (shape == WrapperWrapperBlockstairs.EnumShape.INNER_LEFT) return side == facing.rotateY();
                if (shape == WrapperWrapperBlockstairs.EnumShape.INNER_RIGHT) return side == facing.rotateYCCW();
            }
            return false;
        } else if (this instanceof WrapperWrapperBlocksnow) {
            IWrapperBlockState state = this.getActualState(base_state, world, pos);
            return ((Integer) state.getValue(WrapperWrapperBlocksnow.LAYERS)) >= 8;
        } else if (this instanceof WrapperBlockHopper && side == EnumFacing.UP) {
            return true;
        } else if (this instanceof WrapperBlockCompressedPowered) {
            return true;
        }
        return isNormalCube(base_state, world, pos);
    }

    /**
     * Determines if this block should set fire and deal fire damage
     * to entities coming into contact with it.
     *
     * @param world The current world
     * @param pos   Block position in world
     * @return True if the block should deal damage
     */
    public boolean isBurning(IWrapperBlockAccess world, BlockPos pos) {
        return false;
    }

    /**
     * Determines this block should be treated as an air block
     * by the rest of the code. This method is primarily
     * useful for creating pure logic-WrapperBlocks that will be invisible
     * to the player and otherwise interact as air would.
     *
     * @param state The current state
     * @param world The current world
     * @param pos   Block position in world
     * @return True if the block considered air
     */
    public boolean isAir(IWrapperBlockState state, IWrapperBlockAccess world, BlockPos pos) {
        return state.getMaterial() == WrapperMaterial.AIR;
    }

    /**
     * Determines if the player can harvest this block, obtaining it's drops when the block is destroyed.
     *
     * @param player The player damaging the block
     * @param pos    The block's current position
     * @return True to spawn the drops
     */
    public boolean canHarvestBlock(IWrapperBlockAccess world, BlockPos pos, EntityPlayer player) {
        return net.minecraftforge.common.ForgeHooks.canHarvestBlock(this, player, world, pos);
    }

    /**
     * Called when a player removes a block. This is responsible for
     * actually destroying the block, and the block is intact at time of call.
     * This is called regardless of whether the player can harvest the block or
     * not.
     * <p>
     * Return true if the block is actually destroyed.
     * <p>
     * Note: When used in multiplayer, this is called on both client and
     * server sides!
     *
     * @param state       The current state.
     * @param world       The current world
     * @param player      The player damaging the block, may be null
     * @param pos         Block position in world
     * @param willHarvest True if Block.harvestBlock will be called after this, if the return in true.
     *                    Can be useful to delay the destruction of tile entities till after harvestBlock
     * @return True if the block is actually destroyed.
     */
    public boolean removedByPlayer(IWrapperBlockState state, World world, BlockPos pos, EntityPlayer player,
                                   boolean willHarvest) {
        this.onBlockHarvested(world, pos, state, player);
        return world.setWrapperBlockstate(pos, net.minecraft.init.WrapperBlocks.AIR.getDefaultState(), world.isRemote ? 11 : 3);
    }

    /**
     * Chance that fire will spread and consume this block.
     * 300 being a 100% chance, 0, being a 0% chance.
     *
     * @param world The current world
     * @param pos   Block position in world
     * @param face  The face that the fire is coming from
     * @return A number ranging from 0 to 300 relating used to determine if the block will be consumed by fire
     */
    public int getFlammability(IWrapperBlockAccess world, BlockPos pos, EnumFacing face) {
        return net.minecraft.init.WrapperBlocks.FIRE.getFlammability(this);
    }

    /**
     * Called when fire is updating, checks if a block face can catch fire.
     *
     * @param world The current world
     * @param pos   Block position in world
     * @param face  The face that the fire is coming from
     * @return True if the face can be on fire, false otherwise.
     */
    public boolean isFlammable(IWrapperBlockAccess world, BlockPos pos, EnumFacing face) {
        return getFlammability(world, pos, face) > 0;
    }

    /**
     * Called when fire is updating on a neighbor block.
     * The higher the number returned, the faster fire will spread around this block.
     *
     * @param world The current world
     * @param pos   Block position in world
     * @param face  The face that the fire is coming from
     * @return A number that is used to determine the speed of fire growth around the block
     */
    public int getFireSpreadSpeed(IWrapperBlockAccess world, BlockPos pos, EnumFacing face) {
        return net.minecraft.init.WrapperBlocks.FIRE.getEncouragement(this);
    }

    /**
     * Currently only called by fire when it is on top of this block.
     * Returning true will prevent the fire from naturally dying during updating.
     * Also prevents firing from dying from rain.
     *
     * @param world The current world
     * @param pos   Block position in world
     * @param side  The face that the fire is coming from
     * @return True if this block sustains fire, meaning it will never go out.
     */
    public boolean isFireSource(World world, BlockPos pos, EnumFacing side) {
        if (side != EnumFacing.UP) return false;
        if (this == WrapperBlocks.NETHERRACK || this == WrapperBlocks.MAGMA) return true;
        if ((world.provider instanceof net.minecraft.world.WorldProviderEnd) && this == WrapperBlocks.BEDROCK)
            return true;
        return false;
    }

    private boolean isTileProvider = this instanceof ITileEntityProvider;

    /**
     * Called throughout the code as a replacement for block instanceof BlockContainer
     * Moving this to the Block base class allows for mods that wish to extend vanilla
     * WrapperBlocks, and also want to have a tile entity on that block, may.
     * <p>
     * Return true from this function to specify this block has a tile entity.
     *
     * @param state State of the current block
     * @return True if block has a tile entity, false otherwise
     */
    public boolean hasTileEntity(IWrapperBlockState state) {
        return isTileProvider;
    }

    /**
     * Called throughout the code as a replacement for ITileEntityProvider.createNewTileEntity
     * Return the same thing you would from that function.
     * This will fall back to ITileEntityProvider.createNewTileEntity(World) if this block is a ITileEntityProvider
     *
     * @param state The state of the current block
     * @return A instance of a class extending TileEntity
     */
    @Nullable
    public TileEntity createTileEntity(World world, IWrapperBlockState state) {
        if (isTileProvider) {
            return ((ITileEntityProvider) this).createNewTileEntity(world, getMetaFromState(state));
        }
        return null;
    }

    /**
     * State and fortune sensitive version, this replaces the old (int meta, Random rand)
     * version in 1.1.
     *
     * @param state   Current state
     * @param fortune Current item fortune level
     * @param random  Random number generator
     * @return The number of items to drop
     */
    public int quantityDropped(IWrapperBlockState state, int fortune, Random random) {
        return quantityDroppedWithBonus(fortune, random);
    }

    /**
     * @deprecated use {@link #getDrops(NonNullList, IWrapperBlockAccess, BlockPos, IWrapperBlockState, int)}
     */
    @Deprecated
    public List<ItemStack> getDrops(IWrapperBlockAccess world, BlockPos pos, IWrapperBlockState state, int fortune) {
        NonNullList<ItemStack> ret = NonNullList.create();
        getDrops(ret, world, pos, state, fortune);
        return ret;
    }

    /**
     * This gets a complete list of items dropped from this block.
     *
     * @param drops   add all items this block drops to this drops list
     * @param world   The current world
     * @param pos     Block position in world
     * @param state   Current state
     * @param fortune Breakers fortune level
     */
    public void getDrops(NonNullList<ItemStack> drops, IWrapperBlockAccess world, BlockPos pos, IWrapperBlockState state,
                         int fortune) {
        Random rand = world instanceof World ? ((World) world).rand : RANDOM;

        int count = quantityDropped(state, fortune, rand);
        for (int i = 0; i < count; i++) {
            Item item = this.getItemDropped(state, rand, fortune);
            if (item != Items.AIR) {
                drops.add(new ItemStack(item, 1, this.damageDropped(state)));
            }
        }
    }

    /**
     * Return true from this function if the player with silk touch can harvest this block directly, and not it's normal
     * drops.
     *
     * @param world  The world
     * @param pos    Block position in world
     * @param state  current block state
     * @param player The player doing the harvesting
     * @return True if the block can be directly harvested using silk touch
     */
    public boolean canSilkHarvest(World world, BlockPos pos, IWrapperBlockState state, EntityPlayer player) {
        silk_check_state.set(state);
        ;
        boolean ret = this.canSilkHarvest();
        silk_check_state.set(null);
        return ret;
    }

    /**
     * Determines if a specified mob type can spawn on this block, returning false will
     * prevent any mob from spawning on the block.
     *
     * @param state The current state
     * @param world The current world
     * @param pos   Block position in world
     * @param type  The Mob Category Type
     * @return True to allow a mob of the specified category to spawn, false to prevent it.
     */
    public boolean canCreatureSpawn(IWrapperBlockState state, IWrapperBlockAccess world, BlockPos pos,
                                    net.minecraft.entity.EntityLiving.SpawnPlacementType type) {
        return isSideSolid(state, world, pos, EnumFacing.UP);
    }

    /**
     * Determines if this block is classified as a Bed, Allowing
     * players to sleep in it, though the block has to specifically
     * perform the sleeping functionality in it's activated event.
     *
     * @param state  The current state
     * @param world  The current world
     * @param pos    Block position in world
     * @param player The player or camera entity, null in some cases.
     * @return True to treat this as a bed
     */
    public boolean isBed(IWrapperBlockState state, IWrapperBlockAccess world, BlockPos pos, @Nullable Entity player) {
        return this == net.minecraft.init.WrapperBlocks.BED;
    }

    /**
     * Returns the position that the player is moved to upon
     * waking up, or respawning at the bed.
     *
     * @param state  The current state
     * @param world  The current world
     * @param pos    Block position in world
     * @param player The player or camera entity, null in some cases.
     * @return The spawn position
     */
    @Nullable
    public BlockPos getBedSpawnPosition(IWrapperBlockState state, IWrapperBlockAccess world, BlockPos pos,
                                        @Nullable EntityPlayer player) {
        if (world instanceof World) return WrapperBlockBed.getSafeExitLocation((World) world, pos, 0);
        return null;
    }

    /**
     * Called when a user either starts or stops sleeping in the bed.
     *
     * @param world    The current world
     * @param pos      Block position in world
     * @param player   The player or camera entity, null in some cases.
     * @param occupied True if we are occupying the bed, or false if they are stopping use of the bed
     */
    public void setBedOccupied(IWrapperBlockAccess world, BlockPos pos, EntityPlayer player, boolean occupied) {
        if (world instanceof World) {
            IWrapperBlockState state = world.getWrapperBlockstate(pos);
            state = state.getBlock()
                .getActualState(state, world, pos);
            state = state.withProperty(WrapperBlockBed.OCCUPIED, occupied);
            ((World) world).setWrapperBlockstate(pos, state, 4);
        }
    }

    /**
     * Returns the direction of the block. Same values that
     * are returned by BlockDirectional
     *
     * @param state The current state
     * @param world The current world
     * @param pos   Block position in world
     * @return Bed direction
     */
    public EnumFacing getBedDirection(IWrapperBlockState state, IWrapperBlockAccess world, BlockPos pos) {
        return (EnumFacing) getActualState(state, world, pos).getValue(WrapperBlockHorizontal.FACING);
    }

    /**
     * Determines if the current block is the foot half of the bed.
     *
     * @param world The current world
     * @param pos   Block position in world
     * @return True if the current block is the foot side of a bed.
     */
    public boolean isBedFoot(IWrapperBlockAccess world, BlockPos pos) {
        return getActualState(world.getWrapperBlockstate(pos), world, pos).getValue(WrapperBlockBed.PART)
            == WrapperBlockBed.EnumPartType.FOOT;
    }

    /**
     * Called when a leaf should start its decay process.
     *
     * @param state The current state
     * @param world The current world
     * @param pos   Block position in world
     */
    public void beginLeavesDecay(IWrapperBlockState state, World world, BlockPos pos) {
    }

    /**
     * Determines if this block can prevent leaves connected to it from decaying.
     *
     * @param state The current state
     * @param world The current world
     * @param pos   Block position in world
     * @return true if the presence this block can prevent leaves from decaying.
     */
    public boolean canSustainLeaves(IWrapperBlockState state, IWrapperBlockAccess world, BlockPos pos) {
        return false;
    }

    /**
     * Determines if this block is considered a leaf block, used to apply the leaf decay and generation system.
     *
     * @param state The current state
     * @param world The current world
     * @param pos   Block position in world
     * @return true if this block is considered leaves.
     */
    public boolean isLeaves(IWrapperBlockState state, IWrapperBlockAccess world, BlockPos pos) {
        return state.getMaterial() == WrapperMaterial.LEAVES;
    }

    /**
     * Used during tree growth to determine if newly generated leaves can replace this block.
     *
     * @param state The current state
     * @param world The current world
     * @param pos   Block position in world
     * @return true if this block can be replaced by growing leaves.
     */
    public boolean canBeReplacedByLeaves(IWrapperBlockState state, IWrapperBlockAccess world, BlockPos pos) {
        return isAir(state, world, pos) || isLeaves(state, world, pos); // !state.isFullBlock();
    }

    /**
     * @param world The current world
     * @param pos   Block position in world
     * @return true if the block is wood (logs)
     */
    public boolean isWood(IWrapperBlockAccess world, BlockPos pos) {
        return false;
    }

    /**
     * Determines if the current block is replaceable by Ore veins during world generation.
     *
     * @param state  The current state
     * @param world  The current world
     * @param pos    Block position in world
     * @param target The generic target block the gen is looking for, Standards define stone
     *               for overworld generation, and neatherack for the nether.
     * @return True to allow this block to be replaced by a ore
     */
    public boolean isReplaceableOreGen(IWrapperBlockState state, IWrapperBlockAccess world, BlockPos pos,
                                       com.google.common.base.Predicate<IWrapperBlockState> target) {
        return target.apply(state);
    }

    /**
     * Location sensitive version of getExplosionResistance
     *
     * @param world     The current world
     * @param pos       Block position in world
     * @param exploder  The entity that caused the explosion, can be null
     * @param explosion The explosion
     * @return The amount of the explosion absorbed.
     */
    public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        return getExplosionResistance(exploder);
    }

    /**
     * Called when the block is destroyed by an explosion.
     * Useful for allowing the block to take into account tile entities,
     * state, etc. when exploded, before it is removed.
     *
     * @param world     The current world
     * @param pos       Block position in world
     * @param explosion The explosion instance affecting the block
     */
    public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
        world.setBlockToAir(pos);
        onBlockDestroyedByExplosion(world, pos, explosion);
    }

    /**
     * Determine if this block can make a redstone connection on the side provided,
     * Useful to control which sides are inputs and outputs for redstone wires.
     *
     * @param state The current state
     * @param world The current world
     * @param pos   Block position in world
     * @param side  The side that is trying to make the connection, CAN BE NULL
     * @return True to make the connection
     */
    public boolean canConnectRedstone(IWrapperBlockState state, IWrapperBlockAccess world, BlockPos pos,
                                      @Nullable EnumFacing side) {
        return state.canProvidePower() && side != null;
    }

    /**
     * Determines if a torch can be placed on the top surface of this block.
     * Useful for creating your own block that torches can be on, such as fences.
     *
     * @param state The current state
     * @param world The current world
     * @param pos   Block position in world
     * @return True to allow the torch to be placed
     */
    public boolean canPlaceTorchOnTop(IWrapperBlockState state, IWrapperBlockAccess world, BlockPos pos) {
        if (this == WrapperBlocks.END_GATEWAY || this == WrapperBlocks.LIT_PUMPKIN) {
            return false;
        } else if (state.isTopSolid() || this instanceof WrapperBlockFence
            || this == WrapperBlocks.GLASS
            || this == WrapperBlocks.COBBLESTONE_WALL
            || this == WrapperBlocks.STAINED_GLASS) {
            return true;
        } else {
            BlockFaceShape shape = state.getBlockFaceShape(world, pos, EnumFacing.UP);
            return (shape == BlockFaceShape.SOLID || shape == BlockFaceShape.CENTER
                || shape == BlockFaceShape.CENTER_BIG) && !isExceptionBlockForAttaching(this);
        }
    }

    /**
     * Called when a user uses the creative pick block button on this block
     *
     * @param target The full target the player is looking at
     * @return A ItemStack to add to the player's inventory, empty itemstack if nothing should be added.
     */
    public ItemStack getPickBlock(IWrapperBlockState state, RayTraceResult target, World world, BlockPos pos,
                                  EntityPlayer player) {
        return getItem(world, pos, state);
    }

    /**
     * Used by getTopSolidOrLiquidBlock while placing biome decorations, villages, etc
     * Also used to determine if the player can spawn on this block.
     *
     * @return False to disallow spawning
     */
    public boolean isFoliage(IWrapperBlockAccess world, BlockPos pos) {
        return false;
    }

    /**
     * Allows a block to override the standard EntityLivingBase.updateFallState
     * particles, this is a server side method that spawns particles with
     * WorldServer.spawnParticle
     *
     * @param world              The current Server world
     * @param blockPosition      of the block that the entity landed on.
     * @param IWrapperBlockState State at the specific world/pos
     * @param entity             the entity that hit landed on the block.
     * @param numberOfParticles  that vanilla would have spawned.
     * @return True to prevent vanilla landing particles form spawning.
     */
    public boolean addLandingEffects(IWrapperBlockState state, net.minecraft.world.WorldServer worldObj,
                                     BlockPos blockPosition, IWrapperBlockState IWrapperBlockState, EntityLivingBase entity, int numberOfParticles) {
        return false;
    }

    /**
     * Allows a block to override the standard vanilla running particles.
     * This is called from {@link Entity#spawnRunningParticles} and is called both,
     * Client and server side, it's up to the implementor to client check / server check.
     * By default vanilla spawns particles only on the client and the server methods no-op.
     *
     * @param state  The WrapperBlockstate the entity is running on.
     * @param world  The world.
     * @param pos    The position at the entities feet.
     * @param entity The entity running on the block.
     * @return True to prevent vanilla running particles from spawning.
     */
    public boolean addRunningEffects(IWrapperBlockState state, World world, BlockPos pos, Entity entity) {
        return false;
    }

    /**
     * Spawn a digging particle effect in the world, this is a wrapper
     * around EffectRenderer.addBlockHitEffects to allow the block more
     * control over the particles. Useful when you have entirely different
     * texture sheets for different sides/locations in the world.
     *
     * @param state   The current state
     * @param world   The current world
     * @param target  The target the player is looking at {x/y/z/side/sub}
     * @param manager A reference to the current particle manager.
     * @return True to prevent vanilla digging particles form spawning.
     */
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(IWrapperBlockState state, World worldObj, RayTraceResult target,
                                 net.minecraft.client.particle.ParticleManager manager) {
        return false;
    }

    /**
     * Spawn particles for when the block is destroyed. Due to the nature
     * of how this is invoked, the x/y/z locations are not always guaranteed
     * to host your block. So be sure to do proper sanity checks before assuming
     * that the location is this block.
     *
     * @param world   The current world
     * @param pos     Position to spawn the particle
     * @param manager A reference to the current particle manager.
     * @return True to prevent vanilla break particles from spawning.
     */
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, BlockPos pos, net.minecraft.client.particle.ParticleManager manager) {
        return false;
    }

    /**
     * Determines if this block can support the passed in plant, allowing it to be planted and grow.
     * Some examples:
     * Reeds check if its a reed, or if its sand/dirt/grass and adjacent to water
     * Cacti checks if its a cacti, or if its sand
     * Nether types check for soul sand
     * Crops check for tilled soil
     * Caves check if it's a solid surface
     * Plains check if its grass or dirt
     * Water check if its still water
     *
     * @param state     The Current state
     * @param world     The current world
     * @param pos       Block position in world
     * @param direction The direction relative to the given position the plant wants to be, typically its UP
     * @param plantable The plant that wants to check
     * @return True to allow the plant to be planted/stay.
     */
    public boolean canSustainPlant(IWrapperBlockState state, IWrapperBlockAccess world, BlockPos pos, EnumFacing direction,
                                   net.minecraftforge.common.IPlantable plantable) {
        IWrapperBlockState plant = plantable.getPlant(world, pos.offset(direction));
        net.minecraftforge.common.EnumPlantType plantType = plantable.getPlantType(world, pos.offset(direction));

        if (plant.getBlock() == net.minecraft.init.WrapperBlocks.CACTUS) {
            return this == net.minecraft.init.WrapperBlocks.CACTUS || this == net.minecraft.init.WrapperBlocks.SAND;
        }

        if (plant.getBlock() == net.minecraft.init.WrapperBlocks.REEDS && this == net.minecraft.init.WrapperBlocks.REEDS) {
            return true;
        }

        if (plantable instanceof WrapperBlockBush && ((WrapperBlockBush) plantable).canSustainBush(state)) {
            return true;
        }

        switch (plantType) {
            case Desert:
                return this == net.minecraft.init.WrapperBlocks.SAND || this == net.minecraft.init.WrapperBlocks.HARDENED_CLAY
                    || this == net.minecraft.init.WrapperBlocks.STAINED_HARDENED_CLAY;
            case Nether:
                return this == net.minecraft.init.WrapperBlocks.SOUL_SAND;
            case Crop:
                return this == net.minecraft.init.WrapperBlocks.FARMLAND;
            case Cave:
                return state.isSideSolid(world, pos, EnumFacing.UP);
            case Plains:
                return this == net.minecraft.init.WrapperBlocks.GRASS || this == net.minecraft.init.WrapperBlocks.DIRT
                    || this == net.minecraft.init.WrapperBlocks.FARMLAND;
            case Water:
                return state.getMaterial() == WrapperMaterial.WATER && state.getValue(WrapperBlockLiquid.LEVEL) == 0;
            case Beach:
                boolean isBeach = this == net.minecraft.init.WrapperBlocks.GRASS || this == net.minecraft.init.WrapperBlocks.DIRT
                    || this == net.minecraft.init.WrapperBlocks.SAND;
                boolean hasWater = (world.getWrapperBlockstate(pos.east())
                    .getMaterial() == WrapperMaterial.WATER
                    || world.getWrapperBlockstate(pos.west())
                    .getMaterial() == WrapperMaterial.WATER
                    || world.getWrapperBlockstate(pos.north())
                    .getMaterial() == WrapperMaterial.WATER
                    || world.getWrapperBlockstate(pos.south())
                    .getMaterial() == WrapperMaterial.WATER);
                return isBeach && hasWater;
        }

        return false;
    }

    /**
     * Called when a plant grows on this block, only implemented for saplings using the WorldGen*Trees classes right
     * now.
     * Modder may implement this for custom plants.
     * This does not use ForgeDirection, because large/huge trees can be located in non-representable direction,
     * so the source location is specified.
     * Currently this just changes the block to dirt if it was grass.
     * <p>
     * Note: This happens DURING the generation, the generation may not be complete when this is called.
     *
     * @param state  The current state
     * @param world  Current world
     * @param pos    Block position in world
     * @param source Source plant's position in world
     */
    public void onPlantGrow(IWrapperBlockState state, World world, BlockPos pos, BlockPos source) {
        if (this == net.minecraft.init.WrapperBlocks.GRASS || this == net.minecraft.init.WrapperBlocks.FARMLAND) {
            world.setWrapperBlockstate(pos, net.minecraft.init.WrapperBlocks.DIRT.getDefaultState(), 2);
        }
    }

    /**
     * Checks if this soil is fertile, typically this means that growth rates
     * of plants on this soil will be slightly sped up.
     * Only vanilla case is tilledField when it is within range of water.
     *
     * @param world The current world
     * @param pos   Block position in world
     * @return True if the soil should be considered fertile.
     */
    public boolean isFertile(World world, BlockPos pos) {
        if (this == net.minecraft.init.WrapperBlocks.FARMLAND) {
            return ((Integer) world.getWrapperBlockstate(pos)
                .getValue(WrapperBlockFarmland.MOISTURE)) > 0;
        }

        return false;
    }

    /**
     * Location aware and overrideable version of the lightOpacity array,
     * return the number to subtract from the light value when it passes through this block.
     * <p>
     * This is not guaranteed to have the tile entity in place before this is called, so it is
     * Recommended that you have your tile entity call relight after being placed if you
     * rely on it for light info.
     *
     * @param state The Block state
     * @param world The current world
     * @param pos   Block position in world
     * @return The amount of light to block, 0 for air, 255 for fully opaque.
     */
    public int getLightOpacity(IWrapperBlockState state, IWrapperBlockAccess world, BlockPos pos) {
        return state.getLightOpacity();
    }

    /**
     * Determines if this block is can be destroyed by the specified entities normal behavior.
     *
     * @param state The current state
     * @param world The current world
     * @param pos   Block position in world
     * @return True to allow the ender dragon to destroy this block
     */
    public boolean canEntityDestroy(IWrapperBlockState state, IWrapperBlockAccess world, BlockPos pos, Entity entity) {
        if (entity instanceof net.minecraft.entity.boss.EntityDragon) {
            return this != net.minecraft.init.WrapperBlocks.BARRIER && this != net.minecraft.init.WrapperBlocks.OBSIDIAN
                && this != net.minecraft.init.WrapperBlocks.END_STONE
                && this != net.minecraft.init.WrapperBlocks.BEDROCK
                && this != net.minecraft.init.WrapperBlocks.END_PORTAL
                && this != net.minecraft.init.WrapperBlocks.END_PORTAL_FRAME
                && this != net.minecraft.init.WrapperBlocks.COMMAND_BLOCK
                && this != net.minecraft.init.WrapperBlocks.REPEATING_COMMAND_BLOCK
                && this != net.minecraft.init.WrapperBlocks.CHAIN_COMMAND_BLOCK
                && this != net.minecraft.init.WrapperBlocks.IRON_BARS
                && this != net.minecraft.init.WrapperBlocks.END_GATEWAY;
        } else if ((entity instanceof net.minecraft.entity.boss.EntityWither)
            || (entity instanceof net.minecraft.entity.projectile.EntityWitherSkull)) {
            return net.minecraft.entity.boss.EntityWither.canDestroyBlock(this);
        }

        return true;
    }

    /**
     * Determines if this block can be used as the base of a beacon.
     *
     * @param world  The current world
     * @param pos    Block position in world
     * @param beacon Beacon position in world
     * @return True, to support the beacon, and make it active with this block.
     */
    public boolean isBeaconBase(IWrapperBlockAccess worldObj, BlockPos pos, BlockPos beacon) {
        return this == net.minecraft.init.WrapperBlocks.EMERALD_BLOCK || this == net.minecraft.init.WrapperBlocks.GOLD_BLOCK
            || this == net.minecraft.init.WrapperBlocks.DIAMOND_BLOCK
            || this == net.minecraft.init.WrapperBlocks.IRON_BLOCK;
    }

    /**
     * Rotate the block. For vanilla WrapperBlocks this rotates around the axis passed in (generally, it should be the "face"
     * that was hit).
     * Note: for mod WrapperBlocks, this is up to the block and modder to decide. It is not mandated that it be a rotation
     * around the
     * face, but could be a rotation to orient *to* that face, or a visiting of possible rotations.
     * The method should return true if the rotation was successful though.
     *
     * @param world The world
     * @param pos   Block position in world
     * @param axis  The axis to rotate around
     * @return True if the rotation was successful, False if the rotation failed, or is not possible
     */
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
        IWrapperBlockState state = world.getWrapperBlockstate(pos);
        for (IProperty<?> prop : state.getProperties()
            .keySet()) {
            if ((prop.getName()
                .equals("facing")
                || prop.getName()
                .equals("rotation"))
                && prop.getValueClass() == EnumFacing.class) {
                WrapperBlock wrapperBlock = state.getBlock();
                if (!(wrapperBlock instanceof WrapperBlockBed)
                    && !(wrapperBlock instanceof WrapperBlockPistonExtension)) {
                    IWrapperBlockState newState;
                    // noinspection unchecked
                    IProperty<EnumFacing> facingProperty = (IProperty<EnumFacing>) prop;
                    EnumFacing facing = state.getValue(facingProperty);
                    java.util.Collection<EnumFacing> validFacings = facingProperty.getAllowedValues();

                    // rotate horizontal facings clockwise
                    if (validFacings.size() == 4 && !validFacings.contains(EnumFacing.UP)
                        && !validFacings.contains(EnumFacing.DOWN)) {
                        newState = state.withProperty(facingProperty, facing.rotateY());
                    } else {
                        // rotate other facings about the axis
                        EnumFacing rotatedFacing = facing.rotateAround(axis.getAxis());
                        if (validFacings.contains(rotatedFacing)) {
                            newState = state.withProperty(facingProperty, rotatedFacing);
                        } else // abnormal facing property, just cycle it
                        {
                            newState = state.cycleProperty(facingProperty);
                        }
                    }

                    world.setWrapperBlockstate(pos, newState);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the rotations that can apply to the block at the specified coordinates. Null means no rotations are possible.
     * Note, this is up to the block to decide. It may not be accurate or representative.
     *
     * @param world The world
     * @param pos   Block position in world
     * @return An array of valid axes to rotate around, or null for none or unknown
     */
    @Nullable
    public EnumFacing[] getValidRotations(World world, BlockPos pos) {
        IWrapperBlockState state = world.getWrapperBlockstate(pos);
        for (IProperty<?> prop : state.getProperties()
            .keySet()) {
            if ((prop.getName()
                .equals("facing")
                || prop.getName()
                .equals("rotation"))
                && prop.getValueClass() == EnumFacing.class) {
                @SuppressWarnings("unchecked")
                java.util.Collection<EnumFacing> values = ((java.util.Collection<EnumFacing>) prop.getAllowedValues());
                return values.toArray(new EnumFacing[values.size()]);
            }
        }
        return null;
    }

    /**
     * Determines the amount of enchanting power this block can provide to an enchanting table.
     *
     * @param world The World
     * @param pos   Block position in world
     * @return The amount of enchanting power this block produces.
     */
    public float getEnchantPowerBonus(World world, BlockPos pos) {
        return this == net.minecraft.init.WrapperBlocks.BOOKSHELF ? 1 : 0;
    }

    /**
     * Common way to recolor a block with an external tool
     *
     * @param world The world
     * @param pos   Block position in world
     * @param side  The side hit with the coloring tool
     * @param color The color to change to
     * @return If the recoloring was successful
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean recolorBlock(World world, BlockPos pos, EnumFacing side, net.minecraft.item.EnumDyeColor color) {
        IWrapperBlockState state = world.getWrapperBlockstate(pos);
        for (IProperty prop : state.getProperties()
            .keySet()) {
            if (prop.getName()
                .equals("color") && prop.getValueClass() == net.minecraft.item.EnumDyeColor.class) {
                net.minecraft.item.EnumDyeColor current = (net.minecraft.item.EnumDyeColor) state.getValue(prop);
                if (current != color && prop.getAllowedValues()
                    .contains(color)) {
                    world.setWrapperBlockstate(pos, state.withProperty(prop, color));
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gathers how much experience this block drops when broken.
     *
     * @param state   The current state
     * @param world   The world
     * @param pos     Block position
     * @param fortune
     * @return Amount of XP from breaking this block.
     */
    public int getExpDrop(IWrapperBlockState state, IWrapperBlockAccess world, BlockPos pos, int fortune) {
        return 0;
    }

    /**
     * Called when a tile entity on a side of this block changes is created or is destroyed.
     *
     * @param world    The world
     * @param pos      Block position in world
     * @param neighbor Block position of neighbor
     */
    public void onNeighborChange(IWrapperBlockAccess world, BlockPos pos, BlockPos neighbor) {
    }

    /**
     * Called on an Observer block whenever an update for an Observer is received.
     *
     * @param observerState       The Observer block's state.
     * @param world               The current world.
     * @param observerPos         The Observer block's position.
     * @param changedWrapperBlock The updated block.
     * @param changedBlockPos     The updated block's position.
     */
    public void observedNeighborChange(IWrapperBlockState observerState, World world, BlockPos observerPos,
                                       WrapperBlock changedWrapperBlock, BlockPos changedBlockPos) {
    }

    /**
     * Called to determine whether to allow the a block to handle its own indirect power rather than using the default
     * rules.
     *
     * @param world The world
     * @param pos   Block position in world
     * @param side  The INPUT side of the block to be powered - ie the opposite of this block's output side
     * @return Whether Block#isProvidingWeakPower should be called when determining indirect power
     */
    public boolean shouldCheckWeakPower(IWrapperBlockState state, IWrapperBlockAccess world, BlockPos pos, EnumFacing side) {
        return state.isNormalCube();
    }

    /**
     * If this block should be notified of weak changes.
     * Weak changes are changes 1 block away through a solid block.
     * Similar to comparators.
     *
     * @param world The current world
     * @param pos   Block position in world
     * @return true To be notified of changes
     */
    public boolean getWeakChanges(IWrapperBlockAccess world, BlockPos pos) {
        return false;
    }

    private String[] harvestTool = new String[16];
    ;
    private int[] harvestLevel = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};

    /**
     * Sets or removes the tool and level required to harvest this block.
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
        java.util.Iterator<IWrapperBlockState> itr = getWrapperBlockstate().getValidStates()
            .iterator();
        while (itr.hasNext()) {
            setHarvestLevel(toolClass, level, itr.next());
        }
    }

    /**
     * Sets or removes the tool and level required to harvest this block.
     *
     * @param toolClass Class
     * @param level     Harvest level:
     *                  Wood: 0
     *                  Stone: 1
     *                  Iron: 2
     *                  Diamond: 3
     *                  Gold: 0
     * @param state     The specific state.
     */
    public void setHarvestLevel(String toolClass, int level, IWrapperBlockState state) {
        int idx = this.getMetaFromState(state);
        this.harvestTool[idx] = toolClass;
        this.harvestLevel[idx] = level;
    }

    /**
     * Queries the class of tool required to harvest this block, if null is returned
     * we assume that anything can harvest this block.
     */
    @Nullable
    public String getHarvestTool(IWrapperBlockState state) {
        return harvestTool[getMetaFromState(state)];
    }

    /**
     * Queries the harvest level of this item stack for the specified tool class,
     * Returns -1 if this tool is not of the specified type
     *
     * @return Harvest level, or -1 if not the specified tool type.
     */
    public int getHarvestLevel(IWrapperBlockState state) {
        return harvestLevel[getMetaFromState(state)];
    }

    /**
     * Checks if the specified tool type is efficient on this block,
     * meaning that it digs at full speed.
     */
    public boolean isToolEffective(String type, IWrapperBlockState state) {
        if ("pickaxe".equals(type)
            && (this == net.minecraft.init.WrapperBlocks.REDSTONE_ORE || this == net.minecraft.init.WrapperBlocks.LIT_REDSTONE_ORE
            || this == net.minecraft.init.WrapperBlocks.OBSIDIAN))
            return false;
        return type != null && type.equals(getHarvestTool(state));
    }

    /**
     * Can return IExtendedWrapperBlockstate
     */
    public IWrapperBlockState getExtendedState(IWrapperBlockState state, IWrapperBlockAccess world, BlockPos pos) {
        return state;
    }

    /**
     * Called when the entity is inside this block, may be used to determined if the entity can breathing,
     * display material overlays, or if the entity can swim inside a block.
     *
     * @param world              that is being tested.
     * @param blockpos           position thats being tested.
     * @param IWrapperBlockState state at world/blockpos
     * @param entity             that is being tested.
     * @param yToTest,           primarily for testingHead, which sends the the eye level of the entity, other wise it sends a
     *                           y that can be tested vs liquid height.
     * @param wrapperMaterialIn  to test for.
     * @param testingHead        when true, its testing the entities head for vision, breathing ect... otherwise its testing
     *                           the body, for swimming and movement adjustment.
     * @return null for default behavior, true if the entity is within the material, false if it was not.
     */
    @Nullable
    public Boolean isEntityInsideMaterial(IWrapperBlockAccess world, BlockPos blockpos, IWrapperBlockState IWrapperBlockState,
                                          Entity entity, double yToTest, WrapperMaterial wrapperMaterialIn, boolean testingHead) {
        return null;
    }

    /**
     * Called when boats or fishing hooks are inside the block to check if they are inside
     * the material requested.
     *
     * @param world             world that is being tested.
     * @param pos               block thats being tested.
     * @param boundingBox       box to test, generally the bounds of an entity that are besting tested.
     * @param wrapperMaterialIn to check for.
     * @return null for default behavior, true if the box is within the material, false if it was not.
     */
    @Nullable
    public Boolean isAABBInsideMaterial(World world, BlockPos pos, AxisAlignedBB boundingBox, WrapperMaterial wrapperMaterialIn) {
        return null;
    }

    /**
     * Called when entities are moving to check if they are inside a liquid
     *
     * @param world       world that is being tested.
     * @param pos         block thats being tested.
     * @param boundingBox box to test, generally the bounds of an entity that are besting tested.
     * @return null for default behavior, true if the box is within the material, false if it was not.
     */
    @Nullable
    public Boolean isAABBInsideLiquid(World world, BlockPos pos, AxisAlignedBB boundingBox) {
        return null;
    }

    /**
     * Called when entities are swimming in the given liquid and returns the relative height (used by
     * {@link net.minecraft.entity.item.EntityBoat})
     *
     * @param world           world that is being tested.
     * @param pos             block thats being tested.
     * @param state           state at world/pos
     * @param wrapperMaterial liquid thats being tested.
     * @return relative height of the given liquid (material), a value between 0 and 1
     */
    public float getBlockLiquidHeight(World world, BlockPos pos, IWrapperBlockState state, WrapperMaterial wrapperMaterial) {
        return 0;
    }

    /**
     * Queries if this block should render in a given layer.
     * ISmartBlockModel can use {@link net.minecraftforge.client.MinecraftForgeClient#getRenderLayer()} to alter their
     * model based on layer.
     */
    public boolean canRenderInLayer(IWrapperBlockState state, BlockRenderLayer layer) {
        return getBlockLayer() == layer;
    }

    // For Internal use only to capture droped items inside getDrops
    protected static ThreadLocal<Boolean> captureDrops = ThreadLocal.withInitial(() -> false);
    protected static ThreadLocal<NonNullList<ItemStack>> capturedDrops = ThreadLocal.withInitial(NonNullList::create);

    protected NonNullList<ItemStack> captureDrops(boolean start) {
        if (start) {
            captureDrops.set(true);
            capturedDrops.get()
                .clear();
            return NonNullList.create();
        } else {
            captureDrops.set(false);
            return capturedDrops.get();
        }
    }

    /**
     * Sensitive version of getSoundType
     *
     * @param state  The state
     * @param world  The world
     * @param pos    The position. Note that the world may not necessarily have {@code state} here!
     * @param entity The entity that is breaking/stepping on/placing/hitting/falling on this block, or null if no entity
     *               is in this context
     * @return A SoundType to use
     */
    public SoundType getSoundType(IWrapperBlockState state, World world, BlockPos pos, @Nullable Entity entity) {
        return getSoundType();
    }

    /**
     * @param state     The state
     * @param world     The world
     * @param pos       The position of this state
     * @param beaconPos The position of the beacon
     * @return A float RGB [0.0, 1.0] array to be averaged with a beacon's existing beam color, or null to do nothing to
     * the beam
     */
    @Nullable
    public float[] getBeaconColorMultiplier(IWrapperBlockState state, World world, BlockPos pos, BlockPos beaconPos) {
        return null;
    }

    /**
     * Use this to change the fog color used when the entity is "inside" a material.
     * Vec3d is used here as "r/g/b" 0 - 1 values.
     *
     * @param world         The world.
     * @param pos           The position at the entity viewport.
     * @param state         The state at the entity viewport.
     * @param entity        the entity
     * @param originalColor The current fog color, You are not expected to use this, Return as the default if
     *                      applicable.
     * @return The new fog color.
     */
    @SideOnly(Side.CLIENT)
    public Vec3d getFogColor(World world, BlockPos pos, IWrapperBlockState state, Entity entity, Vec3d originalColor,
                             float partialTicks) {
        if (state.getMaterial() == WrapperMaterial.WATER) {
            float f12 = 0.0F;

            if (entity instanceof net.minecraft.entity.EntityLivingBase) {
                net.minecraft.entity.EntityLivingBase ent = (net.minecraft.entity.EntityLivingBase) entity;
                f12 = (float) net.minecraft.enchantment.EnchantmentHelper.getRespirationModifier(ent) * 0.2F;

                if (ent.isPotionActive(net.minecraft.init.MobEffects.WATER_BREATHING)) {
                    f12 = f12 * 0.3F + 0.6F;
                }
            }
            return new Vec3d(0.02F + f12, 0.02F + f12, 0.2F + f12);
        } else if (state.getMaterial() == WrapperMaterial.LAVA) {
            return new Vec3d(0.6F, 0.1F, 0.0F);
        }
        return originalColor;
    }

    /**
     * Used to determine the state 'viewed' by an entity (see
     * {@link net.minecraft.client.renderer.ActiveRenderInfo#getWrapperBlockstateAtEntityViewpoint(World, Entity, float)}).
     * Can be used by fluid WrapperBlocks to determine if the viewpoint is within the fluid or not.
     *
     * @param state     the state
     * @param world     the world
     * @param pos       the position
     * @param viewpoint the viewpoint
     * @return the block state that should be 'seen'
     */
    public IWrapperBlockState getStateAtViewpoint(IWrapperBlockState state, IWrapperBlockAccess world, BlockPos pos,
                                                  Vec3d viewpoint) {
        return state;
    }

    /**
     * Gets the {@link IWrapperBlockState} to place
     *
     * @param world  The world the block is being placed in
     * @param pos    The position the block is being placed at
     * @param facing The side the block is being placed on
     * @param hitX   The X coordinate of the hit vector
     * @param hitY   The Y coordinate of the hit vector
     * @param hitZ   The Z coordinate of the hit vector
     * @param meta   The metadata of {@link ItemStack} as processed by {@link Item#getMetadata(int)}
     * @param placer The entity placing the block
     * @param hand   The player hand used to place this block
     * @return The state to be placed in the world
     */
    public IWrapperBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
                                                   float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer);
    }

    /**
     * Determines if another block can connect to this block
     *
     * @param world  The current world
     * @param pos    The position of this block
     * @param facing The side the connecting block is on
     * @return True to allow another block to connect to this block
     */
    public boolean canBeConnectedTo(IWrapperBlockAccess world, BlockPos pos, EnumFacing facing) {
        return false;
    }

    /**
     * @deprecated use
     * {@link #getAiPathNodeType(IWrapperBlockState, IWrapperBlockAccess, BlockPos, net.minecraft.entity.EntityLiving)}
     */
    @Nullable
    @Deprecated // TODO: remove
    public net.minecraft.pathfinding.PathNodeType getAiPathNodeType(IWrapperBlockState state, IWrapperBlockAccess world,
                                                                    BlockPos pos) {
        return isBurning(world, pos) ? net.minecraft.pathfinding.PathNodeType.DAMAGE_FIRE : null;
    }

    /**
     * Get the {@code PathNodeType} for this block. Return {@code null} for vanilla behavior.
     *
     * @return the PathNodeType
     */
    @Nullable
    public net.minecraft.pathfinding.PathNodeType getAiPathNodeType(IWrapperBlockState state, IWrapperBlockAccess world,
                                                                    BlockPos pos, @Nullable net.minecraft.entity.EntityLiving entity) {
        return getAiPathNodeType(state, world, pos);
    }

    /**
     * @param WrapperBlockstate The state for this block
     * @param world             The world this block is in
     * @param pos               The position of this block
     * @param side              The side of this block that the chest lid is trying to open into
     * @return true if the chest should be prevented from opening by this block
     */
    public boolean doesSideBlockChestOpening(IWrapperBlockState WrapperBlockstate, IWrapperBlockAccess world, BlockPos pos,
                                             EnumFacing side) {
        ResourceLocation registryName = this.getRegistryName();
        if (registryName != null && "minecraft".equals(registryName.getResourceDomain())) {
            // maintain the vanilla behavior of https://bugs.mojang.com/browse/MC-378
            return isNormalCube(WrapperBlockstate, world, pos);
        }
        return isSideSolid(WrapperBlockstate, world, pos, side);
    }

    /**
     * @param state The state
     * @return true if the block is sticky block which used for pull or push adjacent WrapperBlocks (use by piston)
     */
    public boolean isStickyBlock(IWrapperBlockState state) {
        return state.getBlock() == WrapperBlocks.SLIME_BLOCK;
    }

    /* ========================================= FORGE END ====================================== */

//    public static void registerWrapperBlocks() {
//        registerBlock(0, AIR_ID, (new WrapperBlockAir()).setUnlocalizedName("air"));
//        registerBlock(
//            1,
//            "stone",
//            (new WrapperWrapperBlockstone()).setHardness(1.5F)
//                .setResistance(10.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("stone"));
//        registerBlock(
//            2,
//            "grass",
//            (new WrapperBlockGrass()).setHardness(0.6F)
//                .setSoundType(SoundType.PLANT)
//                .setUnlocalizedName("grass"));
//        registerBlock(
//            3,
//            "dirt",
//            (new WrapperBlockDirt()).setHardness(0.5F)
//                .setSoundType(SoundType.GROUND)
//                .setUnlocalizedName("dirt"));
//        WrapperBlock wrapperBlock = (new WrapperBlock(Material.ROCK)).setHardness(2.0F)
//            .setResistance(10.0F)
//            .setSoundType(SoundType.STONE)
//            .setUnlocalizedName("stonebrick")
//            .setCreativeTab(CreativeTabs.BUILDING_WrapperBlocks);
//        registerBlock(4, "cobblestone", wrapperBlock);
//        WrapperBlock wrapperBlock1 = (new WrapperBlockPlanks()).setHardness(2.0F)
//            .setResistance(5.0F)
//            .setSoundType(SoundType.WOOD)
//            .setUnlocalizedName("wood");
//        registerBlock(5, "planks", wrapperBlock1);
//        registerBlock(
//            6,
//            "sapling",
//            (new WrapperWrapperBlocksapling()).setHardness(0.0F)
//                .setSoundType(SoundType.PLANT)
//                .setUnlocalizedName("sapling"));
//        registerBlock(
//            7,
//            "bedrock",
//            (new WrapperBlockEmptyDrops(Material.ROCK)).setBlockUnbreakable()
//                .setResistance(6000000.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("bedrock")
//                .disableStats()
//                .setCreativeTab(CreativeTabs.BUILDING_WrapperBlocks));
//        registerBlock(
//            8,
//            "flowing_water",
//            (new WrapperBlockDynamicLiquid(Material.WATER)).setHardness(100.0F)
//                .setLightOpacity(3)
//                .setUnlocalizedName("water")
//                .disableStats());
//        registerBlock(
//            9,
//            "water",
//            (new WrapperWrapperBlockstaticLiquid(Material.WATER)).setHardness(100.0F)
//                .setLightOpacity(3)
//                .setUnlocalizedName("water")
//                .disableStats());
//        registerBlock(
//            10,
//            "flowing_lava",
//            (new WrapperBlockDynamicLiquid(Material.LAVA)).setHardness(100.0F)
//                .setLightLevel(1.0F)
//                .setUnlocalizedName("lava")
//                .disableStats());
//        registerBlock(
//            11,
//            "lava",
//            (new WrapperWrapperBlockstaticLiquid(Material.LAVA)).setHardness(100.0F)
//                .setLightLevel(1.0F)
//                .setUnlocalizedName("lava")
//                .disableStats());
//        registerBlock(
//            12,
//            "sand",
//            (new WrapperWrapperBlocksand()).setHardness(0.5F)
//                .setSoundType(SoundType.SAND)
//                .setUnlocalizedName("sand"));
//        registerBlock(
//            13,
//            "gravel",
//            (new WrapperBlockGravel()).setHardness(0.6F)
//                .setSoundType(SoundType.GROUND)
//                .setUnlocalizedName("gravel"));
//        registerBlock(
//            14,
//            "gold_ore",
//            (new WrapperBlockOre()).setHardness(3.0F)
//                .setResistance(5.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("oreGold"));
//        registerBlock(
//            15,
//            "iron_ore",
//            (new WrapperBlockOre()).setHardness(3.0F)
//                .setResistance(5.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("oreIron"));
//        registerBlock(
//            16,
//            "coal_ore",
//            (new WrapperBlockOre()).setHardness(3.0F)
//                .setResistance(5.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("oreCoal"));
//        registerBlock(17, "log", (new WrapperBlockOldLog()).setUnlocalizedName("log"));
//        registerBlock(18, "leaves", (new WrapperBlockOldLeaf()).setUnlocalizedName("leaves"));
//        registerBlock(
//            19,
//            "sponge",
//            (new WrapperWrapperBlocksponge()).setHardness(0.6F)
//                .setSoundType(SoundType.PLANT)
//                .setUnlocalizedName("sponge"));
//        registerBlock(
//            20,
//            "glass",
//            (new WrapperBlockGlass(Material.GLASS, false)).setHardness(0.3F)
//                .setSoundType(SoundType.GLASS)
//                .setUnlocalizedName("glass"));
//        registerBlock(
//            21,
//            "lapis_ore",
//            (new WrapperBlockOre()).setHardness(3.0F)
//                .setResistance(5.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("oreLapis"));
//        registerBlock(
//            22,
//            "lapis_block",
//            (new WrapperBlock(Material.IRON, MapColor.LAPIS)).setHardness(3.0F)
//                .setResistance(5.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("blockLapis")
//                .setCreativeTab(CreativeTabs.BUILDING_WrapperBlocks));
//        registerBlock(
//            23,
//            "dispenser",
//            (new WrapperBlockDispenser()).setHardness(3.5F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("dispenser"));
//        WrapperBlock wrapperBlock2 = (new WrapperWrapperBlocksandStone()).setSoundType(SoundType.STONE)
//            .setHardness(0.8F)
//            .setUnlocalizedName("sandStone");
//        registerBlock(24, "sandstone", wrapperBlock2);
//        registerBlock(
//            25,
//            "noteblock",
//            (new WrapperBlockNote()).setSoundType(SoundType.WOOD)
//                .setHardness(0.8F)
//                .setUnlocalizedName("musicBlock"));
//        registerBlock(
//            26,
//            "bed",
//            (new WrapperBlockBed()).setSoundType(SoundType.WOOD)
//                .setHardness(0.2F)
//                .setUnlocalizedName("bed")
//                .disableStats());
//        registerBlock(
//            27,
//            "golden_rail",
//            (new WrapperBlockRailPowered()).setHardness(0.7F)
//                .setSoundType(SoundType.METAL)
//                .setUnlocalizedName("goldenRail"));
//        registerBlock(
//            28,
//            "detector_rail",
//            (new WrapperBlockRailDetector()).setHardness(0.7F)
//                .setSoundType(SoundType.METAL)
//                .setUnlocalizedName("detectorRail"));
//        registerBlock(29, "sticky_piston", (new WrapperBlockPistonBase(true)).setUnlocalizedName("pistonStickyBase"));
//        registerBlock(
//            30,
//            "web",
//            (new WrapperBlockWeb()).setLightOpacity(1)
//                .setHardness(4.0F)
//                .setUnlocalizedName("web"));
//        registerBlock(
//            31,
//            "tallgrass",
//            (new WrapperBlockTallGrass()).setHardness(0.0F)
//                .setSoundType(SoundType.PLANT)
//                .setUnlocalizedName("tallgrass"));
//        registerBlock(
//            32,
//            "deadbush",
//            (new WrapperBlockDeadBush()).setHardness(0.0F)
//                .setSoundType(SoundType.PLANT)
//                .setUnlocalizedName("deadbush"));
//        registerBlock(33, "piston", (new WrapperBlockPistonBase(false)).setUnlocalizedName("pistonBase"));
//        registerBlock(34, "piston_head", (new WrapperBlockPistonExtension()).setUnlocalizedName("pistonBase"));
//        registerBlock(
//            35,
//            "wool",
//            (new WrapperBlockColored(Material.CLOTH)).setHardness(0.8F)
//                .setSoundType(SoundType.CLOTH)
//                .setUnlocalizedName("cloth"));
//        registerBlock(36, "piston_extension", new WrapperBlockPistonMoving());
//        registerBlock(
//            37,
//            "yellow_flower",
//            (new WrapperBlockYellowFlower()).setHardness(0.0F)
//                .setSoundType(SoundType.PLANT)
//                .setUnlocalizedName("flower1"));
//        registerBlock(
//            38,
//            "red_flower",
//            (new WrapperBlockRedFlower()).setHardness(0.0F)
//                .setSoundType(SoundType.PLANT)
//                .setUnlocalizedName("flower2"));
//        WrapperBlock wrapperBlock3 = (new WrapperBlockMushroom()).setHardness(0.0F)
//            .setSoundType(SoundType.PLANT)
//            .setLightLevel(0.125F)
//            .setUnlocalizedName("mushroom");
//        registerBlock(39, "brown_mushroom", wrapperBlock3);
//        WrapperBlock wrapperBlock4 = (new WrapperBlockMushroom()).setHardness(0.0F)
//            .setSoundType(SoundType.PLANT)
//            .setUnlocalizedName("mushroom");
//        registerBlock(40, "red_mushroom", wrapperBlock4);
//        registerBlock(
//            41,
//            "gold_block",
//            (new WrapperBlock(Material.IRON, MapColor.GOLD)).setHardness(3.0F)
//                .setResistance(10.0F)
//                .setSoundType(SoundType.METAL)
//                .setUnlocalizedName("blockGold")
//                .setCreativeTab(CreativeTabs.BUILDING_WrapperBlocks));
//        registerBlock(
//            42,
//            "iron_block",
//            (new WrapperBlock(Material.IRON, MapColor.IRON)).setHardness(5.0F)
//                .setResistance(10.0F)
//                .setSoundType(SoundType.METAL)
//                .setUnlocalizedName("blockIron")
//                .setCreativeTab(CreativeTabs.BUILDING_WrapperBlocks));
//        registerBlock(
//            43,
//            "double_stone_slab",
//            (new WrapperBlockDoubleStoneSlab()).setHardness(2.0F)
//                .setResistance(10.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("stoneSlab"));
//        registerBlock(
//            44,
//            "stone_slab",
//            (new WrapperBlockHalfStoneSlab()).setHardness(2.0F)
//                .setResistance(10.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("stoneSlab"));
//        WrapperBlock wrapperBlock5 = (new WrapperBlock(Material.ROCK, MapColor.RED)).setHardness(2.0F)
//            .setResistance(10.0F)
//            .setSoundType(SoundType.STONE)
//            .setUnlocalizedName("brick")
//            .setCreativeTab(CreativeTabs.BUILDING_WrapperBlocks);
//        registerBlock(45, "brick_block", wrapperBlock5);
//        registerBlock(
//            46,
//            "tnt",
//            (new WrapperBlockTNT()).setHardness(0.0F)
//                .setSoundType(SoundType.PLANT)
//                .setUnlocalizedName("tnt"));
//        registerBlock(
//            47,
//            "bookshelf",
//            (new WrapperBlockBookshelf()).setHardness(1.5F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("bookshelf"));
//        registerBlock(
//            48,
//            "mossy_cobblestone",
//            (new WrapperBlock(Material.ROCK)).setHardness(2.0F)
//                .setResistance(10.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("stoneMoss")
//                .setCreativeTab(CreativeTabs.BUILDING_WrapperBlocks));
//        registerBlock(
//            49,
//            "obsidian",
//            (new WrapperBlockObsidian()).setHardness(50.0F)
//                .setResistance(2000.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("obsidian"));
//        registerBlock(
//            50,
//            "torch",
//            (new WrapperBlockTorch()).setHardness(0.0F)
//                .setLightLevel(0.9375F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("torch"));
//        registerBlock(
//            51,
//            "fire",
//            (new WrapperBlockFire()).setHardness(0.0F)
//                .setLightLevel(1.0F)
//                .setSoundType(SoundType.CLOTH)
//                .setUnlocalizedName("fire")
//                .disableStats());
//        registerBlock(
//            52,
//            "mob_spawner",
//            (new WrapperBlockMobSpawner()).setHardness(5.0F)
//                .setSoundType(SoundType.METAL)
//                .setUnlocalizedName("mobSpawner")
//                .disableStats());
//        registerBlock(
//            53,
//            "oak_stairs",
//            (new WrapperWrapperBlockstairs(
//                wrapperBlock1.getDefaultState()
//                    .withProperty(WrapperBlockPlanks.VARIANT, WrapperBlockPlanks.EnumType.OAK)))
//                        .setUnlocalizedName("stairsWood"));
//        registerBlock(
//            54,
//            "chest",
//            (new WrapperBlockChest(WrapperBlockChest.Type.BASIC)).setHardness(2.5F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("chest"));
//        registerBlock(
//            55,
//            "redstone_wire",
//            (new WrapperBlockRedstoneWire()).setHardness(0.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("redstoneDust")
//                .disableStats());
//        registerBlock(
//            56,
//            "diamond_ore",
//            (new WrapperBlockOre()).setHardness(3.0F)
//                .setResistance(5.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("oreDiamond"));
//        registerBlock(
//            57,
//            "diamond_block",
//            (new WrapperBlock(Material.IRON, MapColor.DIAMOND)).setHardness(5.0F)
//                .setResistance(10.0F)
//                .setSoundType(SoundType.METAL)
//                .setUnlocalizedName("blockDiamond")
//                .setCreativeTab(CreativeTabs.BUILDING_WrapperBlocks));
//        registerBlock(
//            58,
//            "crafting_table",
//            (new WrapperBlockWorkbench()).setHardness(2.5F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("workbench"));
//        registerBlock(59, "wheat", (new WrapperBlockCrops()).setUnlocalizedName("crops"));
//        WrapperBlock wrapperBlock6 = (new WrapperBlockFarmland()).setHardness(0.6F)
//            .setSoundType(SoundType.GROUND)
//            .setUnlocalizedName("farmland");
//        registerBlock(60, "farmland", wrapperBlock6);
//        registerBlock(
//            61,
//            "furnace",
//            (new WrapperBlockFurnace(false)).setHardness(3.5F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("furnace")
//                .setCreativeTab(CreativeTabs.DECORATIONS));
//        registerBlock(
//            62,
//            "lit_furnace",
//            (new WrapperBlockFurnace(true)).setHardness(3.5F)
//                .setSoundType(SoundType.STONE)
//                .setLightLevel(0.875F)
//                .setUnlocalizedName("furnace"));
//        registerBlock(
//            63,
//            "standing_sign",
//            (new WrapperWrapperBlockstandingSign()).setHardness(1.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("sign")
//                .disableStats());
//        registerBlock(
//            64,
//            "wooden_door",
//            (new WrapperBlockDoor(Material.WOOD)).setHardness(3.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("doorOak")
//                .disableStats());
//        registerBlock(
//            65,
//            "ladder",
//            (new WrapperBlockLadder()).setHardness(0.4F)
//                .setSoundType(SoundType.LADDER)
//                .setUnlocalizedName("ladder"));
//        registerBlock(
//            66,
//            "rail",
//            (new WrapperBlockRail()).setHardness(0.7F)
//                .setSoundType(SoundType.METAL)
//                .setUnlocalizedName("rail"));
//        registerBlock(
//            67,
//            "stone_stairs",
//            (new WrapperWrapperBlockstairs(wrapperBlock.getDefaultState())).setUnlocalizedName("stairsStone"));
//        registerBlock(
//            68,
//            "wall_sign",
//            (new WrapperBlockWallSign()).setHardness(1.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("sign")
//                .disableStats());
//        registerBlock(
//            69,
//            "lever",
//            (new WrapperBlockLever()).setHardness(0.5F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("lever"));
//        registerBlock(
//            70,
//            "stone_pressure_plate",
//            (new WrapperBlockPressurePlate(Material.ROCK, WrapperBlockPressurePlate.Sensitivity.MOBS)).setHardness(0.5F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("pressurePlateStone"));
//        registerBlock(
//            71,
//            "iron_door",
//            (new WrapperBlockDoor(Material.IRON)).setHardness(5.0F)
//                .setSoundType(SoundType.METAL)
//                .setUnlocalizedName("doorIron")
//                .disableStats());
//        registerBlock(
//            72,
//            "wooden_pressure_plate",
//            (new WrapperBlockPressurePlate(Material.WOOD, WrapperBlockPressurePlate.Sensitivity.EVERYTHING))
//                .setHardness(0.5F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("pressurePlateWood"));
//        registerBlock(
//            73,
//            "redstone_ore",
//            (new WrapperBlockRedstoneOre(false)).setHardness(3.0F)
//                .setResistance(5.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("oreRedstone")
//                .setCreativeTab(CreativeTabs.BUILDING_WrapperBlocks));
//        registerBlock(
//            74,
//            "lit_redstone_ore",
//            (new WrapperBlockRedstoneOre(true)).setLightLevel(0.625F)
//                .setHardness(3.0F)
//                .setResistance(5.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("oreRedstone"));
//        registerBlock(
//            75,
//            "unlit_redstone_torch",
//            (new WrapperBlockRedstoneTorch(false)).setHardness(0.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("notGate"));
//        registerBlock(
//            76,
//            "redstone_torch",
//            (new WrapperBlockRedstoneTorch(true)).setHardness(0.0F)
//                .setLightLevel(0.5F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("notGate")
//                .setCreativeTab(CreativeTabs.REDSTONE));
//        registerBlock(
//            77,
//            "stone_button",
//            (new WrapperBlockButtonStone()).setHardness(0.5F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("button"));
//        registerBlock(
//            78,
//            "snow_layer",
//            (new WrapperWrapperBlocksnow()).setHardness(0.1F)
//                .setSoundType(SoundType.SNOW)
//                .setUnlocalizedName("snow")
//                .setLightOpacity(0));
//        registerBlock(
//            79,
//            "ice",
//            (new WrapperBlockIce()).setHardness(0.5F)
//                .setLightOpacity(3)
//                .setSoundType(SoundType.GLASS)
//                .setUnlocalizedName("ice"));
//        registerBlock(
//            80,
//            "snow",
//            (new WrapperBlocksnowWrapperBlock()).setHardness(0.2F)
//                .setSoundType(SoundType.SNOW)
//                .setUnlocalizedName("snow"));
//        registerBlock(
//            81,
//            "cactus",
//            (new WrapperBlockCactus()).setHardness(0.4F)
//                .setSoundType(SoundType.CLOTH)
//                .setUnlocalizedName("cactus"));
//        registerBlock(
//            82,
//            "clay",
//            (new WrapperBlockClay()).setHardness(0.6F)
//                .setSoundType(SoundType.GROUND)
//                .setUnlocalizedName("clay"));
//        registerBlock(
//            83,
//            "reeds",
//            (new WrapperBlockReed()).setHardness(0.0F)
//                .setSoundType(SoundType.PLANT)
//                .setUnlocalizedName("reeds")
//                .disableStats());
//        registerBlock(
//            84,
//            "jukebox",
//            (new WrapperBlockJukebox()).setHardness(2.0F)
//                .setResistance(10.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("jukebox"));
//        registerBlock(
//            85,
//            "fence",
//            (new WrapperBlockFence(Material.WOOD, WrapperBlockPlanks.EnumType.OAK.getMapColor())).setHardness(2.0F)
//                .setResistance(5.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("fence"));
//        WrapperBlock wrapperBlock7 = (new WrapperBlockPumpkin()).setHardness(1.0F)
//            .setSoundType(SoundType.WOOD)
//            .setUnlocalizedName("pumpkin");
//        registerBlock(86, "pumpkin", wrapperBlock7);
//        registerBlock(
//            87,
//            "netherrack",
//            (new WrapperBlockNetherrack()).setHardness(0.4F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("hellrock"));
//        registerBlock(
//            88,
//            "soul_sand",
//            (new WrapperWrapperBlocksoulSand()).setHardness(0.5F)
//                .setSoundType(SoundType.SAND)
//                .setUnlocalizedName("hellsand"));
//        registerBlock(
//            89,
//            "glowstone",
//            (new WrapperBlockGlowstone(Material.GLASS)).setHardness(0.3F)
//                .setSoundType(SoundType.GLASS)
//                .setLightLevel(1.0F)
//                .setUnlocalizedName("lightgem"));
//        registerBlock(
//            90,
//            "portal",
//            (new WrapperBlockPortal()).setHardness(-1.0F)
//                .setSoundType(SoundType.GLASS)
//                .setLightLevel(0.75F)
//                .setUnlocalizedName("portal"));
//        registerBlock(
//            91,
//            "lit_pumpkin",
//            (new WrapperBlockPumpkin()).setHardness(1.0F)
//                .setSoundType(SoundType.WOOD)
//                .setLightLevel(1.0F)
//                .setUnlocalizedName("litpumpkin"));
//        registerBlock(
//            92,
//            "cake",
//            (new WrapperBlockCake()).setHardness(0.5F)
//                .setSoundType(SoundType.CLOTH)
//                .setUnlocalizedName("cake")
//                .disableStats());
//        registerBlock(
//            93,
//            "unpowered_repeater",
//            (new WrapperBlockRedstoneRepeater(false)).setHardness(0.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("diode")
//                .disableStats());
//        registerBlock(
//            94,
//            "powered_repeater",
//            (new WrapperBlockRedstoneRepeater(true)).setHardness(0.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("diode")
//                .disableStats());
//        registerBlock(
//            95,
//            "stained_glass",
//            (new WrapperWrapperBlockstainedGlass(Material.GLASS)).setHardness(0.3F)
//                .setSoundType(SoundType.GLASS)
//                .setUnlocalizedName("stainedGlass"));
//        registerBlock(
//            96,
//            "trapdoor",
//            (new WrapperBlockTrapDoor(Material.WOOD)).setHardness(3.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("trapdoor")
//                .disableStats());
//        registerBlock(
//            97,
//            "monster_egg",
//            (new WrapperWrapperBlocksilverfish()).setHardness(0.75F)
//                .setUnlocalizedName("monsterStoneEgg"));
//        WrapperBlock wrapperBlock8 = (new WrapperWrapperBlockstoneBrick()).setHardness(1.5F)
//            .setResistance(10.0F)
//            .setSoundType(SoundType.STONE)
//            .setUnlocalizedName("stonebricksmooth");
//        registerBlock(98, "stonebrick", wrapperBlock8);
//        registerBlock(
//            99,
//            "brown_mushroom_block",
//            (new WrapperBlockHugeMushroom(Material.WOOD, MapColor.DIRT, wrapperBlock3)).setHardness(0.2F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("mushroom"));
//        registerBlock(
//            100,
//            "red_mushroom_block",
//            (new WrapperBlockHugeMushroom(Material.WOOD, MapColor.RED, wrapperBlock4)).setHardness(0.2F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("mushroom"));
//        registerBlock(
//            101,
//            "iron_bars",
//            (new WrapperBlockPane(Material.IRON, true)).setHardness(5.0F)
//                .setResistance(10.0F)
//                .setSoundType(SoundType.METAL)
//                .setUnlocalizedName("fenceIron"));
//        registerBlock(
//            102,
//            "glass_pane",
//            (new WrapperBlockPane(Material.GLASS, false)).setHardness(0.3F)
//                .setSoundType(SoundType.GLASS)
//                .setUnlocalizedName("thinGlass"));
//        WrapperBlock wrapperBlock9 = (new WrapperBlockMelon()).setHardness(1.0F)
//            .setSoundType(SoundType.WOOD)
//            .setUnlocalizedName("melon");
//        registerBlock(103, "melon_block", wrapperBlock9);
//        registerBlock(
//            104,
//            "pumpkin_stem",
//            (new WrapperWrapperBlockstem(wrapperBlock7)).setHardness(0.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("pumpkinStem"));
//        registerBlock(
//            105,
//            "melon_stem",
//            (new WrapperWrapperBlockstem(wrapperBlock9)).setHardness(0.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("pumpkinStem"));
//        registerBlock(
//            106,
//            "vine",
//            (new WrapperBlockVine()).setHardness(0.2F)
//                .setSoundType(SoundType.PLANT)
//                .setUnlocalizedName("vine"));
//        registerBlock(
//            107,
//            "fence_gate",
//            (new WrapperBlockFenceGate(WrapperBlockPlanks.EnumType.OAK)).setHardness(2.0F)
//                .setResistance(5.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("fenceGate"));
//        registerBlock(
//            108,
//            "brick_stairs",
//            (new WrapperWrapperBlockstairs(wrapperBlock5.getDefaultState())).setUnlocalizedName("stairsBrick"));
//        registerBlock(
//            109,
//            "stone_brick_stairs",
//            (new WrapperWrapperBlockstairs(
//                wrapperBlock8.getDefaultState()
//                    .withProperty(WrapperWrapperBlockstoneBrick.VARIANT, WrapperWrapperBlockstoneBrick.EnumType.DEFAULT)))
//                        .setUnlocalizedName("stairsStoneBrickSmooth"));
//        registerBlock(
//            110,
//            "mycelium",
//            (new WrapperBlockMycelium()).setHardness(0.6F)
//                .setSoundType(SoundType.PLANT)
//                .setUnlocalizedName("mycel"));
//        registerBlock(
//            111,
//            "waterlily",
//            (new WrapperBlockLilyPad()).setHardness(0.0F)
//                .setSoundType(SoundType.PLANT)
//                .setUnlocalizedName("waterlily"));
//        WrapperBlock wrapperBlock10 = (new WrapperBlockNetherBrick()).setHardness(2.0F)
//            .setResistance(10.0F)
//            .setSoundType(SoundType.STONE)
//            .setUnlocalizedName("netherBrick")
//            .setCreativeTab(CreativeTabs.BUILDING_WrapperBlocks);
//        registerBlock(112, "nether_brick", wrapperBlock10);
//        registerBlock(
//            113,
//            "nether_brick_fence",
//            (new WrapperBlockFence(Material.ROCK, MapColor.NETHERRACK)).setHardness(2.0F)
//                .setResistance(10.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("netherFence"));
//        registerBlock(
//            114,
//            "nether_brick_stairs",
//            (new WrapperWrapperBlockstairs(wrapperBlock10.getDefaultState())).setUnlocalizedName("stairsNetherBrick"));
//        registerBlock(115, "nether_wart", (new WrapperBlockNetherWart()).setUnlocalizedName("netherStalk"));
//        registerBlock(
//            116,
//            "enchanting_table",
//            (new WrapperBlockEnchantmentTable()).setHardness(5.0F)
//                .setResistance(2000.0F)
//                .setUnlocalizedName("enchantmentTable"));
//        registerBlock(
//            117,
//            "brewing_stand",
//            (new WrapperBlockBrewingStand()).setHardness(0.5F)
//                .setLightLevel(0.125F)
//                .setUnlocalizedName("brewingStand"));
//        registerBlock(
//            118,
//            "cauldron",
//            (new WrapperBlockCauldron()).setHardness(2.0F)
//                .setUnlocalizedName("cauldron"));
//        registerBlock(
//            119,
//            "end_portal",
//            (new WrapperBlockEndPortal(Material.PORTAL)).setHardness(-1.0F)
//                .setResistance(6000000.0F));
//        registerBlock(
//            120,
//            "end_portal_frame",
//            (new WrapperBlockEndPortalFrame()).setSoundType(SoundType.GLASS)
//                .setLightLevel(0.125F)
//                .setHardness(-1.0F)
//                .setUnlocalizedName("endPortalFrame")
//                .setResistance(6000000.0F)
//                .setCreativeTab(CreativeTabs.DECORATIONS));
//        registerBlock(
//            121,
//            "end_stone",
//            (new WrapperBlock(Material.ROCK, MapColor.SAND)).setHardness(3.0F)
//                .setResistance(15.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("whiteStone")
//                .setCreativeTab(CreativeTabs.BUILDING_WrapperBlocks));
//        registerBlock(
//            122,
//            "dragon_egg",
//            (new WrapperBlockDragonEgg()).setHardness(3.0F)
//                .setResistance(15.0F)
//                .setSoundType(SoundType.STONE)
//                .setLightLevel(0.125F)
//                .setUnlocalizedName("dragonEgg"));
//        registerBlock(
//            123,
//            "redstone_lamp",
//            (new WrapperBlockRedstoneLight(false)).setHardness(0.3F)
//                .setSoundType(SoundType.GLASS)
//                .setUnlocalizedName("redstoneLight")
//                .setCreativeTab(CreativeTabs.REDSTONE));
//        registerBlock(
//            124,
//            "lit_redstone_lamp",
//            (new WrapperBlockRedstoneLight(true)).setHardness(0.3F)
//                .setSoundType(SoundType.GLASS)
//                .setUnlocalizedName("redstoneLight"));
//        registerBlock(
//            125,
//            "double_wooden_slab",
//            (new WrapperBlockDoubleWoodSlab()).setHardness(2.0F)
//                .setResistance(5.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("woodSlab"));
//        registerBlock(
//            126,
//            "wooden_slab",
//            (new WrapperBlockHalfWoodSlab()).setHardness(2.0F)
//                .setResistance(5.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("woodSlab"));
//        registerBlock(
//            127,
//            "cocoa",
//            (new WrapperBlockCocoa()).setHardness(0.2F)
//                .setResistance(5.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("cocoa"));
//        registerBlock(
//            128,
//            "sandstone_stairs",
//            (new WrapperWrapperBlockstairs(
//                wrapperBlock2.getDefaultState()
//                    .withProperty(WrapperWrapperBlocksandStone.TYPE, WrapperWrapperBlocksandStone.EnumType.SMOOTH)))
//                        .setUnlocalizedName("stairsSandStone"));
//        registerBlock(
//            129,
//            "emerald_ore",
//            (new WrapperBlockOre()).setHardness(3.0F)
//                .setResistance(5.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("oreEmerald"));
//        registerBlock(
//            130,
//            "ender_chest",
//            (new WrapperBlockEnderChest()).setHardness(22.5F)
//                .setResistance(1000.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("enderChest")
//                .setLightLevel(0.5F));
//        registerBlock(131, "tripwire_hook", (new WrapperBlockTripWireHook()).setUnlocalizedName("tripWireSource"));
//        registerBlock(132, "tripwire", (new WrapperBlockTripWire()).setUnlocalizedName("tripWire"));
//        registerBlock(
//            133,
//            "emerald_block",
//            (new WrapperBlock(Material.IRON, MapColor.EMERALD)).setHardness(5.0F)
//                .setResistance(10.0F)
//                .setSoundType(SoundType.METAL)
//                .setUnlocalizedName("blockEmerald")
//                .setCreativeTab(CreativeTabs.BUILDING_WrapperBlocks));
//        registerBlock(
//            134,
//            "spruce_stairs",
//            (new WrapperWrapperBlockstairs(
//                wrapperBlock1.getDefaultState()
//                    .withProperty(WrapperBlockPlanks.VARIANT, WrapperBlockPlanks.EnumType.SPRUCE)))
//                        .setUnlocalizedName("stairsWoodSpruce"));
//        registerBlock(
//            135,
//            "birch_stairs",
//            (new WrapperWrapperBlockstairs(
//                wrapperBlock1.getDefaultState()
//                    .withProperty(WrapperBlockPlanks.VARIANT, WrapperBlockPlanks.EnumType.BIRCH)))
//                        .setUnlocalizedName("stairsWoodBirch"));
//        registerBlock(
//            136,
//            "jungle_stairs",
//            (new WrapperWrapperBlockstairs(
//                wrapperBlock1.getDefaultState()
//                    .withProperty(WrapperBlockPlanks.VARIANT, WrapperBlockPlanks.EnumType.JUNGLE)))
//                        .setUnlocalizedName("stairsWoodJungle"));
//        registerBlock(
//            137,
//            "command_block",
//            (new BlockCommandWrapperBlock(MapColor.BROWN)).setBlockUnbreakable()
//                .setResistance(6000000.0F)
//                .setUnlocalizedName("commandBlock"));
//        registerBlock(
//            138,
//            "beacon",
//            (new WrapperBlockBeacon()).setUnlocalizedName("beacon")
//                .setLightLevel(1.0F));
//        registerBlock(139, "cobblestone_wall", (new WrapperBlockWall(wrapperBlock)).setUnlocalizedName("cobbleWall"));
//        registerBlock(
//            140,
//            "flower_pot",
//            (new WrapperBlockFlowerPot()).setHardness(0.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("flowerPot"));
//        registerBlock(141, "carrots", (new WrapperBlockCarrot()).setUnlocalizedName("carrots"));
//        registerBlock(142, "potatoes", (new WrapperBlockPotato()).setUnlocalizedName("potatoes"));
//        registerBlock(
//            143,
//            "wooden_button",
//            (new WrapperBlockButtonWood()).setHardness(0.5F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("button"));
//        registerBlock(
//            144,
//            "skull",
//            (new WrapperWrapperBlockskull()).setHardness(1.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("skull"));
//        registerBlock(
//            145,
//            "anvil",
//            (new WrapperBlockAnvil()).setHardness(5.0F)
//                .setSoundType(SoundType.ANVIL)
//                .setResistance(2000.0F)
//                .setUnlocalizedName("anvil"));
//        registerBlock(
//            146,
//            "trapped_chest",
//            (new WrapperBlockChest(WrapperBlockChest.Type.TRAP)).setHardness(2.5F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("chestTrap"));
//        registerBlock(
//            147,
//            "light_weighted_pressure_plate",
//            (new WrapperBlockPressurePlateWeighted(Material.IRON, 15, MapColor.GOLD)).setHardness(0.5F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("weightedPlate_light"));
//        registerBlock(
//            148,
//            "heavy_weighted_pressure_plate",
//            (new WrapperBlockPressurePlateWeighted(Material.IRON, 150)).setHardness(0.5F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("weightedPlate_heavy"));
//        registerBlock(
//            149,
//            "unpowered_comparator",
//            (new WrapperBlockRedstoneComparator(false)).setHardness(0.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("comparator")
//                .disableStats());
//        registerBlock(
//            150,
//            "powered_comparator",
//            (new WrapperBlockRedstoneComparator(true)).setHardness(0.0F)
//                .setLightLevel(0.625F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("comparator")
//                .disableStats());
//        registerBlock(151, "daylight_detector", new WrapperBlockDaylightDetector(false));
//        registerBlock(
//            152,
//            "redstone_block",
//            (new WrapperBlockCompressedPowered(Material.IRON, MapColor.TNT)).setHardness(5.0F)
//                .setResistance(10.0F)
//                .setSoundType(SoundType.METAL)
//                .setUnlocalizedName("blockRedstone")
//                .setCreativeTab(CreativeTabs.REDSTONE));
//        registerBlock(
//            153,
//            "quartz_ore",
//            (new WrapperBlockOre(MapColor.NETHERRACK)).setHardness(3.0F)
//                .setResistance(5.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("netherquartz"));
//        registerBlock(
//            154,
//            "hopper",
//            (new WrapperBlockHopper()).setHardness(3.0F)
//                .setResistance(8.0F)
//                .setSoundType(SoundType.METAL)
//                .setUnlocalizedName("hopper"));
//        WrapperBlock wrapperBlock11 = (new WrapperBlockQuartz()).setSoundType(SoundType.STONE)
//            .setHardness(0.8F)
//            .setUnlocalizedName("quartzBlock");
//        registerBlock(155, "quartz_block", wrapperBlock11);
//        registerBlock(
//            156,
//            "quartz_stairs",
//            (new WrapperWrapperBlockstairs(
//                wrapperBlock11.getDefaultState()
//                    .withProperty(WrapperBlockQuartz.VARIANT, WrapperBlockQuartz.EnumType.DEFAULT)))
//                        .setUnlocalizedName("stairsQuartz"));
//        registerBlock(
//            157,
//            "activator_rail",
//            (new WrapperBlockRailPowered(true)).setHardness(0.7F)
//                .setSoundType(SoundType.METAL)
//                .setUnlocalizedName("activatorRail"));
//        registerBlock(
//            158,
//            "dropper",
//            (new WrapperBlockDropper()).setHardness(3.5F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("dropper"));
//        registerBlock(
//            159,
//            "stained_hardened_clay",
//            (new WrapperWrapperBlockstainedHardenedClay()).setHardness(1.25F)
//                .setResistance(7.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("clayHardenedStained"));
//        registerBlock(
//            160,
//            "stained_glass_pane",
//            (new WrapperWrapperBlockstainedGlassPane()).setHardness(0.3F)
//                .setSoundType(SoundType.GLASS)
//                .setUnlocalizedName("thinStainedGlass"));
//        registerBlock(161, "leaves2", (new WrapperBlockNewLeaf()).setUnlocalizedName("leaves"));
//        registerBlock(162, "log2", (new WrapperBlockNewLog()).setUnlocalizedName("log"));
//        registerBlock(
//            163,
//            "acacia_stairs",
//            (new WrapperWrapperBlockstairs(
//                wrapperBlock1.getDefaultState()
//                    .withProperty(WrapperBlockPlanks.VARIANT, WrapperBlockPlanks.EnumType.ACACIA)))
//                        .setUnlocalizedName("stairsWoodAcacia"));
//        registerBlock(
//            164,
//            "dark_oak_stairs",
//            (new WrapperWrapperBlockstairs(
//                wrapperBlock1.getDefaultState()
//                    .withProperty(WrapperBlockPlanks.VARIANT, WrapperBlockPlanks.EnumType.DARK_OAK)))
//                        .setUnlocalizedName("stairsWoodDarkOak"));
//        registerBlock(
//            165,
//            "slime",
//            (new WrapperWrapperBlockslime()).setUnlocalizedName("slime")
//                .setSoundType(SoundType.SLIME));
//        registerBlock(166, "barrier", (new WrapperBlockBarrier()).setUnlocalizedName("barrier"));
//        registerBlock(
//            167,
//            "iron_trapdoor",
//            (new WrapperBlockTrapDoor(Material.IRON)).setHardness(5.0F)
//                .setSoundType(SoundType.METAL)
//                .setUnlocalizedName("ironTrapdoor")
//                .disableStats());
//        registerBlock(
//            168,
//            "prismarine",
//            (new WrapperBlockPrismarine()).setHardness(1.5F)
//                .setResistance(10.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("prismarine"));
//        registerBlock(
//            169,
//            "sea_lantern",
//            (new WrapperWrapperBlockseaLantern(Material.GLASS)).setHardness(0.3F)
//                .setSoundType(SoundType.GLASS)
//                .setLightLevel(1.0F)
//                .setUnlocalizedName("seaLantern"));
//        registerBlock(
//            170,
//            "hay_block",
//            (new WrapperBlockHay()).setHardness(0.5F)
//                .setSoundType(SoundType.PLANT)
//                .setUnlocalizedName("hayBlock")
//                .setCreativeTab(CreativeTabs.BUILDING_WrapperBlocks));
//        registerBlock(
//            171,
//            "carpet",
//            (new WrapperBlockCarpet()).setHardness(0.1F)
//                .setSoundType(SoundType.CLOTH)
//                .setUnlocalizedName("woolCarpet")
//                .setLightOpacity(0));
//        registerBlock(
//            172,
//            "hardened_clay",
//            (new WrapperBlockHardenedClay()).setHardness(1.25F)
//                .setResistance(7.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("clayHardened"));
//        registerBlock(
//            173,
//            "coal_block",
//            (new WrapperBlock(Material.ROCK, MapColor.BLACK)).setHardness(5.0F)
//                .setResistance(10.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("blockCoal")
//                .setCreativeTab(CreativeTabs.BUILDING_WrapperBlocks));
//        registerBlock(
//            174,
//            "packed_ice",
//            (new WrapperBlockPackedIce()).setHardness(0.5F)
//                .setSoundType(SoundType.GLASS)
//                .setUnlocalizedName("icePacked"));
//        registerBlock(175, "double_plant", new WrapperBlockDoublePlant());
//        registerBlock(
//            176,
//            "standing_banner",
//            (new WrapperBlockBanner.WrapperBlockBannerStanding()).setHardness(1.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("banner")
//                .disableStats());
//        registerBlock(
//            177,
//            "wall_banner",
//            (new WrapperBlockBanner.WrapperBlockBannerHanging()).setHardness(1.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("banner")
//                .disableStats());
//        registerBlock(178, "daylight_detector_inverted", new WrapperBlockDaylightDetector(true));
//        WrapperBlock wrapperBlock12 = (new WrapperBlockRedSandstone()).setSoundType(SoundType.STONE)
//            .setHardness(0.8F)
//            .setUnlocalizedName("redSandStone");
//        registerBlock(179, "red_sandstone", wrapperBlock12);
//        registerBlock(
//            180,
//            "red_sandstone_stairs",
//            (new WrapperWrapperBlockstairs(
//                wrapperBlock12.getDefaultState()
//                    .withProperty(WrapperBlockRedSandstone.TYPE, WrapperBlockRedSandstone.EnumType.SMOOTH)))
//                        .setUnlocalizedName("stairsRedSandStone"));
//        registerBlock(
//            181,
//            "double_stone_slab2",
//            (new WrapperBlockDoubleStoneSlabNew()).setHardness(2.0F)
//                .setResistance(10.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("stoneSlab2"));
//        registerBlock(
//            182,
//            "stone_slab2",
//            (new WrapperBlockHalfStoneSlabNew()).setHardness(2.0F)
//                .setResistance(10.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("stoneSlab2"));
//        registerBlock(
//            183,
//            "spruce_fence_gate",
//            (new WrapperBlockFenceGate(WrapperBlockPlanks.EnumType.SPRUCE)).setHardness(2.0F)
//                .setResistance(5.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("spruceFenceGate"));
//        registerBlock(
//            184,
//            "birch_fence_gate",
//            (new WrapperBlockFenceGate(WrapperBlockPlanks.EnumType.BIRCH)).setHardness(2.0F)
//                .setResistance(5.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("birchFenceGate"));
//        registerBlock(
//            185,
//            "jungle_fence_gate",
//            (new WrapperBlockFenceGate(WrapperBlockPlanks.EnumType.JUNGLE)).setHardness(2.0F)
//                .setResistance(5.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("jungleFenceGate"));
//        registerBlock(
//            186,
//            "dark_oak_fence_gate",
//            (new WrapperBlockFenceGate(WrapperBlockPlanks.EnumType.DARK_OAK)).setHardness(2.0F)
//                .setResistance(5.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("darkOakFenceGate"));
//        registerBlock(
//            187,
//            "acacia_fence_gate",
//            (new WrapperBlockFenceGate(WrapperBlockPlanks.EnumType.ACACIA)).setHardness(2.0F)
//                .setResistance(5.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("acaciaFenceGate"));
//        registerBlock(
//            188,
//            "spruce_fence",
//            (new WrapperBlockFence(Material.WOOD, WrapperBlockPlanks.EnumType.SPRUCE.getMapColor())).setHardness(2.0F)
//                .setResistance(5.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("spruceFence"));
//        registerBlock(
//            189,
//            "birch_fence",
//            (new WrapperBlockFence(Material.WOOD, WrapperBlockPlanks.EnumType.BIRCH.getMapColor())).setHardness(2.0F)
//                .setResistance(5.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("birchFence"));
//        registerBlock(
//            190,
//            "jungle_fence",
//            (new WrapperBlockFence(Material.WOOD, WrapperBlockPlanks.EnumType.JUNGLE.getMapColor())).setHardness(2.0F)
//                .setResistance(5.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("jungleFence"));
//        registerBlock(
//            191,
//            "dark_oak_fence",
//            (new WrapperBlockFence(Material.WOOD, WrapperBlockPlanks.EnumType.DARK_OAK.getMapColor())).setHardness(2.0F)
//                .setResistance(5.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("darkOakFence"));
//        registerBlock(
//            192,
//            "acacia_fence",
//            (new WrapperBlockFence(Material.WOOD, WrapperBlockPlanks.EnumType.ACACIA.getMapColor())).setHardness(2.0F)
//                .setResistance(5.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("acaciaFence"));
//        registerBlock(
//            193,
//            "spruce_door",
//            (new WrapperBlockDoor(Material.WOOD)).setHardness(3.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("doorSpruce")
//                .disableStats());
//        registerBlock(
//            194,
//            "birch_door",
//            (new WrapperBlockDoor(Material.WOOD)).setHardness(3.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("doorBirch")
//                .disableStats());
//        registerBlock(
//            195,
//            "jungle_door",
//            (new WrapperBlockDoor(Material.WOOD)).setHardness(3.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("doorJungle")
//                .disableStats());
//        registerBlock(
//            196,
//            "acacia_door",
//            (new WrapperBlockDoor(Material.WOOD)).setHardness(3.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("doorAcacia")
//                .disableStats());
//        registerBlock(
//            197,
//            "dark_oak_door",
//            (new WrapperBlockDoor(Material.WOOD)).setHardness(3.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("doorDarkOak")
//                .disableStats());
//        registerBlock(
//            198,
//            "end_rod",
//            (new WrapperBlockEndRod()).setHardness(0.0F)
//                .setLightLevel(0.9375F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("endRod"));
//        registerBlock(
//            199,
//            "chorus_plant",
//            (new WrapperBlockChorusPlant()).setHardness(0.4F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("chorusPlant"));
//        registerBlock(
//            200,
//            "chorus_flower",
//            (new WrapperBlockChorusFlower()).setHardness(0.4F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("chorusFlower"));
//        WrapperBlock wrapperBlock13 = (new WrapperBlock(Material.ROCK, MapColor.MAGENTA)).setHardness(1.5F)
//            .setResistance(10.0F)
//            .setSoundType(SoundType.STONE)
//            .setCreativeTab(CreativeTabs.BUILDING_WrapperBlocks)
//            .setUnlocalizedName("purpurBlock");
//        registerBlock(201, "purpur_block", wrapperBlock13);
//        registerBlock(
//            202,
//            "purpur_pillar",
//            (new WrapperBlockRotatedPillar(Material.ROCK, MapColor.MAGENTA)).setHardness(1.5F)
//                .setResistance(10.0F)
//                .setSoundType(SoundType.STONE)
//                .setCreativeTab(CreativeTabs.BUILDING_WrapperBlocks)
//                .setUnlocalizedName("purpurPillar"));
//        registerBlock(
//            203,
//            "purpur_stairs",
//            (new WrapperWrapperBlockstairs(wrapperBlock13.getDefaultState())).setUnlocalizedName("stairsPurpur"));
//        registerBlock(
//            204,
//            "purpur_double_slab",
//            (new WrapperBlockPurpurSlab.Double()).setHardness(2.0F)
//                .setResistance(10.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("purpurSlab"));
//        registerBlock(
//            205,
//            "purpur_slab",
//            (new WrapperBlockPurpurSlab.Half()).setHardness(2.0F)
//                .setResistance(10.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("purpurSlab"));
//        registerBlock(
//            206,
//            "end_bricks",
//            (new WrapperBlock(Material.ROCK, MapColor.SAND)).setSoundType(SoundType.STONE)
//                .setHardness(0.8F)
//                .setCreativeTab(CreativeTabs.BUILDING_WrapperBlocks)
//                .setUnlocalizedName("endBricks"));
//        registerBlock(207, "beetroots", (new WrapperBlockBeetroot()).setUnlocalizedName("beetroots"));
//        WrapperBlock wrapperBlock14 = (new WrapperBlockGrassPath()).setHardness(0.65F)
//            .setSoundType(SoundType.PLANT)
//            .setUnlocalizedName("grassPath")
//            .disableStats();
//        registerBlock(208, "grass_path", wrapperBlock14);
//        registerBlock(
//            209,
//            "end_gateway",
//            (new WrapperBlockEndGateway(Material.PORTAL)).setHardness(-1.0F)
//                .setResistance(6000000.0F));
//        registerBlock(
//            210,
//            "repeating_command_block",
//            (new BlockCommandWrapperBlock(MapColor.PURPLE)).setBlockUnbreakable()
//                .setResistance(6000000.0F)
//                .setUnlocalizedName("repeatingCommandBlock"));
//        registerBlock(
//            211,
//            "chain_command_block",
//            (new BlockCommandWrapperBlock(MapColor.GREEN)).setBlockUnbreakable()
//                .setResistance(6000000.0F)
//                .setUnlocalizedName("chainCommandBlock"));
//        registerBlock(
//            212,
//            "frosted_ice",
//            (new WrapperBlockFrostedIce()).setHardness(0.5F)
//                .setLightOpacity(3)
//                .setSoundType(SoundType.GLASS)
//                .setUnlocalizedName("frostedIce"));
//        registerBlock(
//            213,
//            "magma",
//            (new WrapperBlockMagma()).setHardness(0.5F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("magma"));
//        registerBlock(
//            214,
//            "nether_wart_block",
//            (new WrapperBlock(Material.GRASS, MapColor.RED)).setCreativeTab(CreativeTabs.BUILDING_WrapperBlocks)
//                .setHardness(1.0F)
//                .setSoundType(SoundType.WOOD)
//                .setUnlocalizedName("netherWartBlock"));
//        registerBlock(
//            215,
//            "red_nether_brick",
//            (new WrapperBlockNetherBrick()).setHardness(2.0F)
//                .setResistance(10.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("redNetherBrick")
//                .setCreativeTab(CreativeTabs.BUILDING_WrapperBlocks));
//        registerBlock(216, "bone_block", (new WrapperBlockBone()).setUnlocalizedName("boneBlock"));
//        registerBlock(217, "structure_void", (new WrapperWrapperBlockstructureVoid()).setUnlocalizedName("structureVoid"));
//        registerBlock(
//            218,
//            "observer",
//            (new WrapperBlockObserver()).setHardness(3.0F)
//                .setUnlocalizedName("observer"));
//        registerBlock(
//            219,
//            "white_shulker_box",
//            (new WrapperWrapperBlockshulkerBox(EnumDyeColor.WHITE)).setHardness(2.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("shulkerBoxWhite"));
//        registerBlock(
//            220,
//            "orange_shulker_box",
//            (new WrapperWrapperBlockshulkerBox(EnumDyeColor.ORANGE)).setHardness(2.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("shulkerBoxOrange"));
//        registerBlock(
//            221,
//            "magenta_shulker_box",
//            (new WrapperWrapperBlockshulkerBox(EnumDyeColor.MAGENTA)).setHardness(2.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("shulkerBoxMagenta"));
//        registerBlock(
//            222,
//            "light_blue_shulker_box",
//            (new WrapperWrapperBlockshulkerBox(EnumDyeColor.LIGHT_BLUE)).setHardness(2.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("shulkerBoxLightBlue"));
//        registerBlock(
//            223,
//            "yellow_shulker_box",
//            (new WrapperWrapperBlockshulkerBox(EnumDyeColor.YELLOW)).setHardness(2.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("shulkerBoxYellow"));
//        registerBlock(
//            224,
//            "lime_shulker_box",
//            (new WrapperWrapperBlockshulkerBox(EnumDyeColor.LIME)).setHardness(2.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("shulkerBoxLime"));
//        registerBlock(
//            225,
//            "pink_shulker_box",
//            (new WrapperWrapperBlockshulkerBox(EnumDyeColor.PINK)).setHardness(2.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("shulkerBoxPink"));
//        registerBlock(
//            226,
//            "gray_shulker_box",
//            (new WrapperWrapperBlockshulkerBox(EnumDyeColor.GRAY)).setHardness(2.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("shulkerBoxGray"));
//        registerBlock(
//            227,
//            "silver_shulker_box",
//            (new WrapperWrapperBlockshulkerBox(EnumDyeColor.SILVER)).setHardness(2.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("shulkerBoxSilver"));
//        registerBlock(
//            228,
//            "cyan_shulker_box",
//            (new WrapperWrapperBlockshulkerBox(EnumDyeColor.CYAN)).setHardness(2.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("shulkerBoxCyan"));
//        registerBlock(
//            229,
//            "purple_shulker_box",
//            (new WrapperWrapperBlockshulkerBox(EnumDyeColor.PURPLE)).setHardness(2.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("shulkerBoxPurple"));
//        registerBlock(
//            230,
//            "blue_shulker_box",
//            (new WrapperWrapperBlockshulkerBox(EnumDyeColor.BLUE)).setHardness(2.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("shulkerBoxBlue"));
//        registerBlock(
//            231,
//            "brown_shulker_box",
//            (new WrapperWrapperBlockshulkerBox(EnumDyeColor.BROWN)).setHardness(2.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("shulkerBoxBrown"));
//        registerBlock(
//            232,
//            "green_shulker_box",
//            (new WrapperWrapperBlockshulkerBox(EnumDyeColor.GREEN)).setHardness(2.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("shulkerBoxGreen"));
//        registerBlock(
//            233,
//            "red_shulker_box",
//            (new WrapperWrapperBlockshulkerBox(EnumDyeColor.RED)).setHardness(2.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("shulkerBoxRed"));
//        registerBlock(
//            234,
//            "black_shulker_box",
//            (new WrapperWrapperBlockshulkerBox(EnumDyeColor.BLACK)).setHardness(2.0F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("shulkerBoxBlack"));
//        registerBlock(235, "white_glazed_terracotta", new WrapperBlockGlazedTerracotta(EnumDyeColor.WHITE));
//        registerBlock(236, "orange_glazed_terracotta", new WrapperBlockGlazedTerracotta(EnumDyeColor.ORANGE));
//        registerBlock(237, "magenta_glazed_terracotta", new WrapperBlockGlazedTerracotta(EnumDyeColor.MAGENTA));
//        registerBlock(238, "light_blue_glazed_terracotta", new WrapperBlockGlazedTerracotta(EnumDyeColor.LIGHT_BLUE));
//        registerBlock(239, "yellow_glazed_terracotta", new WrapperBlockGlazedTerracotta(EnumDyeColor.YELLOW));
//        registerBlock(240, "lime_glazed_terracotta", new WrapperBlockGlazedTerracotta(EnumDyeColor.LIME));
//        registerBlock(241, "pink_glazed_terracotta", new WrapperBlockGlazedTerracotta(EnumDyeColor.PINK));
//        registerBlock(242, "gray_glazed_terracotta", new WrapperBlockGlazedTerracotta(EnumDyeColor.GRAY));
//        registerBlock(243, "silver_glazed_terracotta", new WrapperBlockGlazedTerracotta(EnumDyeColor.SILVER));
//        registerBlock(244, "cyan_glazed_terracotta", new WrapperBlockGlazedTerracotta(EnumDyeColor.CYAN));
//        registerBlock(245, "purple_glazed_terracotta", new WrapperBlockGlazedTerracotta(EnumDyeColor.PURPLE));
//        registerBlock(246, "blue_glazed_terracotta", new WrapperBlockGlazedTerracotta(EnumDyeColor.BLUE));
//        registerBlock(247, "brown_glazed_terracotta", new WrapperBlockGlazedTerracotta(EnumDyeColor.BROWN));
//        registerBlock(248, "green_glazed_terracotta", new WrapperBlockGlazedTerracotta(EnumDyeColor.GREEN));
//        registerBlock(249, "red_glazed_terracotta", new WrapperBlockGlazedTerracotta(EnumDyeColor.RED));
//        registerBlock(250, "black_glazed_terracotta", new WrapperBlockGlazedTerracotta(EnumDyeColor.BLACK));
//        registerBlock(
//            251,
//            "concrete",
//            (new WrapperBlockColored(Material.ROCK)).setHardness(1.8F)
//                .setSoundType(SoundType.STONE)
//                .setUnlocalizedName("concrete"));
//        registerBlock(
//            252,
//            "concrete_powder",
//            (new WrapperBlockConcretePowder()).setHardness(0.5F)
//                .setSoundType(SoundType.SAND)
//                .setUnlocalizedName("concretePowder"));
//        registerBlock(
//            255,
//            "structure_block",
//            (new WrapperWrapperBlockstructure()).setBlockUnbreakable()
//                .setResistance(6000000.0F)
//                .setUnlocalizedName("structureBlock"));
//        REGISTRY.validateKey();
//
//        for (WrapperBlock wrapperBlock15 : REGISTRY) {
//            if (wrapperBlock15.blockMaterial == Material.AIR) {
//                wrapperBlock15.useNeighborBrightness = false;
//            } else {
//                boolean flag = false;
//                boolean flag1 = wrapperBlock15 instanceof WrapperWrapperBlockstairs;
//                boolean flag2 = wrapperBlock15 instanceof WrapperWrapperBlockslab;
//                boolean flag3 = wrapperBlock15 == wrapperBlock6 || wrapperBlock15 == wrapperBlock14;
//                boolean flag4 = wrapperBlock15.translucent;
//                boolean flag5 = wrapperBlock15.lightOpacity == 0;
//
//                if (flag1 || flag2 || flag3 || flag4 || flag5) {
//                    flag = true;
//                }
//
//                wrapperBlock15.useNeighborBrightness = flag;
//            }
//        }
//    }

    private static void registerBlock(int id, ResourceLocation textualID, WrapperBlock wrapperBlock_) {
        blockRegistry.register(id, textualID, wrapperBlock_);
    }

    private static void registerBlock(int id, String textualID, WrapperBlock wrapperBlock_) {
        registerBlock(id, new ResourceLocation(textualID), wrapperBlock_);
    }

    public static enum EnumOffsetType {
        NONE,
        XZ,
        XYZ;
    }
}
