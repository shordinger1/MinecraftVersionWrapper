package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.AxisAlignedBB;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WrapperBlockFlowerPot extends WrapperBlockContainer {

    public static final PropertyInteger LEGACY_DATA = PropertyInteger.create("legacy_data", 0, 15);
    public static final PropertyEnum<WrapperBlockFlowerPot.EnumFlowerType> CONTENTS = PropertyEnum.<WrapperBlockFlowerPot.EnumFlowerType>create(
        "contents",
        WrapperBlockFlowerPot.EnumFlowerType.class);
    protected static final AxisAlignedBB FLOWER_POT_AABB = new AxisAlignedBB(
        0.3125D,
        0.0D,
        0.3125D,
        0.6875D,
        0.375D,
        0.6875D);

    public WrapperBlockFlowerPot() {
        super(Material.CIRCUITS);
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(CONTENTS, WrapperBlockFlowerPot.EnumFlowerType.EMPTY)
                .withProperty(LEGACY_DATA, Integer.valueOf(0)));
    }

    /**
     * Gets the localized name of this block. Used for the statistics page.
     */
    public String getLocalizedName() {
        return I18n.translateToLocal("item.flowerPot.name");
    }

    public AxisAlignedBB getBoundingBox(IWrapperBlockState state, IBlockAccess source, BlockPos pos) {
        return FLOWER_POT_AABB;
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
    public boolean isOpaqueCube(IWrapperBlockState state) {
        return false;
    }

    /**
     * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
     * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
     */
    public EnumBlockRenderType getRenderType(IWrapperBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    public boolean isFullCube(IWrapperBlockState state) {
        return false;
    }

    /**
     * Called when the block is right clicked by a player.
     */
    public boolean onBlockActivated(World worldIn, BlockPos pos, IWrapperBlockState state, EntityPlayer playerIn,
        EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack itemstack = playerIn.getHeldItem(hand);
        TileEntityFlowerPot tileentityflowerpot = this.getTileEntity(worldIn, pos);

        if (tileentityflowerpot == null) {
            return false;
        } else {
            ItemStack itemstack1 = tileentityflowerpot.getFlowerItemStack();

            if (itemstack1.isEmpty()) {
                if (!this.canBePotted(itemstack)) {
                    return false;
                }

                tileentityflowerpot.setItemStack(itemstack);
                playerIn.addStat(StatList.FLOWER_POTTED);

                if (!playerIn.capabilities.isCreativeMode) {
                    itemstack.shrink(1);
                }
            } else {
                if (itemstack.isEmpty()) {
                    playerIn.setHeldItem(hand, itemstack1);
                } else if (!playerIn.addItemStackToInventory(itemstack1)) {
                    playerIn.dropItem(itemstack1, false);
                }

                tileentityflowerpot.setItemStack(ItemStack.EMPTY);
            }

            tileentityflowerpot.markDirty();
            worldIn.notifyBlockUpdate(pos, state, state, 3);
            return true;
        }
    }

    private boolean canBePotted(ItemStack stack) {
        WrapperBlock wrapperBlock = WrapperBlock.getBlockFromItem(stack.getItem());

        if (wrapperBlock != Blocks.YELLOW_FLOWER && wrapperBlock != Blocks.RED_FLOWER
            && wrapperBlock != Blocks.CACTUS
            && wrapperBlock != Blocks.BROWN_MUSHROOM
            && wrapperBlock != Blocks.RED_MUSHROOM
            && wrapperBlock != Blocks.SAPLING
            && wrapperBlock != Blocks.DEADBUSH) {
            int i = stack.getMetadata();
            return wrapperBlock == Blocks.TALLGRASS && i == WrapperBlockTallGrass.EnumType.FERN.getMeta();
        } else {
            return true;
        }
    }

    public ItemStack getItem(World worldIn, BlockPos pos, IWrapperBlockState state) {
        TileEntityFlowerPot tileentityflowerpot = this.getTileEntity(worldIn, pos);

        if (tileentityflowerpot != null) {
            ItemStack itemstack = tileentityflowerpot.getFlowerItemStack();

            if (!itemstack.isEmpty()) {
                return itemstack;
            }
        }

        return new ItemStack(Items.FLOWER_POT);
    }

    /**
     * Checks if this block can be placed exactly at the given position.
     */
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        IWrapperBlockState downState = worldIn.getBlockState(pos.down());
        return super.canPlaceBlockAt(worldIn, pos) && (downState.isTopSolid()
            || downState.getBlockFaceShape(worldIn, pos.down(), EnumFacing.UP) == BlockFaceShape.SOLID);
    }

    /**
     * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
     * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
     * block, etc.
     */
    public void neighborChanged(IWrapperBlockState state, World worldIn, BlockPos pos, WrapperBlock wrapperBlockIn,
        BlockPos fromPos) {
        IWrapperBlockState downState = worldIn.getBlockState(pos.down());
        if (!downState.isTopSolid()
            && downState.getBlockFaceShape(worldIn, pos.down(), EnumFacing.UP) != BlockFaceShape.SOLID) {
            this.dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockToAir(pos);
        }
    }

    /**
     * Called serverside after this block is replaced with another in Chunk, but before the Tile Entity is updated
     */
    public void breakBlock(World worldIn, BlockPos pos, IWrapperBlockState state) {
        super.breakBlock(worldIn, pos, state);
    }

    /**
     * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually
     * collect this block
     */
    public void onBlockHarvested(World worldIn, BlockPos pos, IWrapperBlockState state, EntityPlayer player) {
        super.onBlockHarvested(worldIn, pos, state, player);

        if (player.capabilities.isCreativeMode) {
            TileEntityFlowerPot tileentityflowerpot = this.getTileEntity(worldIn, pos);

            if (tileentityflowerpot != null) {
                tileentityflowerpot.setItemStack(ItemStack.EMPTY);
            }
        }
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    public Item getItemDropped(IWrapperBlockState state, Random rand, int fortune) {
        return Items.FLOWER_POT;
    }

    @Nullable
    private TileEntityFlowerPot getTileEntity(World worldIn, BlockPos pos) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity instanceof TileEntityFlowerPot ? (TileEntityFlowerPot) tileentity : null;
    }

    /**
     * Returns a new instance of a block's tile entity class. Called on placing the block.
     */
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        WrapperBlock wrapperBlock = null;
        int i = 0;

        switch (meta) {
            case 1:
                wrapperBlock = Blocks.RED_FLOWER;
                i = WrapperBlockFlower.EnumFlowerType.POPPY.getMeta();
                break;
            case 2:
                wrapperBlock = Blocks.YELLOW_FLOWER;
                break;
            case 3:
                wrapperBlock = Blocks.SAPLING;
                i = WrapperBlockPlanks.EnumType.OAK.getMetadata();
                break;
            case 4:
                wrapperBlock = Blocks.SAPLING;
                i = WrapperBlockPlanks.EnumType.SPRUCE.getMetadata();
                break;
            case 5:
                wrapperBlock = Blocks.SAPLING;
                i = WrapperBlockPlanks.EnumType.BIRCH.getMetadata();
                break;
            case 6:
                wrapperBlock = Blocks.SAPLING;
                i = WrapperBlockPlanks.EnumType.JUNGLE.getMetadata();
                break;
            case 7:
                wrapperBlock = Blocks.RED_MUSHROOM;
                break;
            case 8:
                wrapperBlock = Blocks.BROWN_MUSHROOM;
                break;
            case 9:
                wrapperBlock = Blocks.CACTUS;
                break;
            case 10:
                wrapperBlock = Blocks.DEADBUSH;
                break;
            case 11:
                wrapperBlock = Blocks.TALLGRASS;
                i = WrapperBlockTallGrass.EnumType.FERN.getMeta();
                break;
            case 12:
                wrapperBlock = Blocks.SAPLING;
                i = WrapperBlockPlanks.EnumType.ACACIA.getMetadata();
                break;
            case 13:
                wrapperBlock = Blocks.SAPLING;
                i = WrapperBlockPlanks.EnumType.DARK_OAK.getMetadata();
        }

        return new TileEntityFlowerPot(Item.getItemFromBlock(wrapperBlock), i);
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { CONTENTS, LEGACY_DATA });
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        return ((Integer) state.getValue(LEGACY_DATA)).intValue();
    }

    /**
     * Get the actual Block state of this Block at the given position. This applies properties not visible in the
     * metadata, such as fence connections.
     */
    public IWrapperBlockState getActualState(IWrapperBlockState state, IBlockAccess worldIn, BlockPos pos) {
        WrapperBlockFlowerPot.EnumFlowerType blockflowerpot$enumflowertype = WrapperBlockFlowerPot.EnumFlowerType.EMPTY;
        TileEntity tileentity = worldIn instanceof ChunkCache
            ? ((ChunkCache) worldIn).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK)
            : worldIn.getTileEntity(pos);

        if (tileentity instanceof TileEntityFlowerPot) {
            TileEntityFlowerPot tileentityflowerpot = (TileEntityFlowerPot) tileentity;
            Item item = tileentityflowerpot.getFlowerPotItem();

            if (item instanceof ItemBlock) {
                int i = tileentityflowerpot.getFlowerPotData();
                WrapperBlock wrapperBlock = WrapperBlock.getBlockFromItem(item);

                if (wrapperBlock == Blocks.SAPLING) {
                    switch (WrapperBlockPlanks.EnumType.byMetadata(i)) {
                        case OAK:
                            blockflowerpot$enumflowertype = WrapperBlockFlowerPot.EnumFlowerType.OAK_SAPLING;
                            break;
                        case SPRUCE:
                            blockflowerpot$enumflowertype = WrapperBlockFlowerPot.EnumFlowerType.SPRUCE_SAPLING;
                            break;
                        case BIRCH:
                            blockflowerpot$enumflowertype = WrapperBlockFlowerPot.EnumFlowerType.BIRCH_SAPLING;
                            break;
                        case JUNGLE:
                            blockflowerpot$enumflowertype = WrapperBlockFlowerPot.EnumFlowerType.JUNGLE_SAPLING;
                            break;
                        case ACACIA:
                            blockflowerpot$enumflowertype = WrapperBlockFlowerPot.EnumFlowerType.ACACIA_SAPLING;
                            break;
                        case DARK_OAK:
                            blockflowerpot$enumflowertype = WrapperBlockFlowerPot.EnumFlowerType.DARK_OAK_SAPLING;
                            break;
                        default:
                            blockflowerpot$enumflowertype = WrapperBlockFlowerPot.EnumFlowerType.EMPTY;
                    }
                } else if (wrapperBlock == Blocks.TALLGRASS) {
                    switch (i) {
                        case 0:
                            blockflowerpot$enumflowertype = WrapperBlockFlowerPot.EnumFlowerType.DEAD_BUSH;
                            break;
                        case 2:
                            blockflowerpot$enumflowertype = WrapperBlockFlowerPot.EnumFlowerType.FERN;
                            break;
                        default:
                            blockflowerpot$enumflowertype = WrapperBlockFlowerPot.EnumFlowerType.EMPTY;
                    }
                } else if (wrapperBlock == Blocks.YELLOW_FLOWER) {
                    blockflowerpot$enumflowertype = WrapperBlockFlowerPot.EnumFlowerType.DANDELION;
                } else if (wrapperBlock == Blocks.RED_FLOWER) {
                    switch (WrapperBlockFlower.EnumFlowerType.getType(WrapperBlockFlower.EnumFlowerColor.RED, i)) {
                        case POPPY:
                            blockflowerpot$enumflowertype = WrapperBlockFlowerPot.EnumFlowerType.POPPY;
                            break;
                        case BLUE_ORCHID:
                            blockflowerpot$enumflowertype = WrapperBlockFlowerPot.EnumFlowerType.BLUE_ORCHID;
                            break;
                        case ALLIUM:
                            blockflowerpot$enumflowertype = WrapperBlockFlowerPot.EnumFlowerType.ALLIUM;
                            break;
                        case HOUSTONIA:
                            blockflowerpot$enumflowertype = WrapperBlockFlowerPot.EnumFlowerType.HOUSTONIA;
                            break;
                        case RED_TULIP:
                            blockflowerpot$enumflowertype = WrapperBlockFlowerPot.EnumFlowerType.RED_TULIP;
                            break;
                        case ORANGE_TULIP:
                            blockflowerpot$enumflowertype = WrapperBlockFlowerPot.EnumFlowerType.ORANGE_TULIP;
                            break;
                        case WHITE_TULIP:
                            blockflowerpot$enumflowertype = WrapperBlockFlowerPot.EnumFlowerType.WHITE_TULIP;
                            break;
                        case PINK_TULIP:
                            blockflowerpot$enumflowertype = WrapperBlockFlowerPot.EnumFlowerType.PINK_TULIP;
                            break;
                        case OXEYE_DAISY:
                            blockflowerpot$enumflowertype = WrapperBlockFlowerPot.EnumFlowerType.OXEYE_DAISY;
                            break;
                        default:
                            blockflowerpot$enumflowertype = WrapperBlockFlowerPot.EnumFlowerType.EMPTY;
                    }
                } else if (wrapperBlock == Blocks.RED_MUSHROOM) {
                    blockflowerpot$enumflowertype = WrapperBlockFlowerPot.EnumFlowerType.MUSHROOM_RED;
                } else if (wrapperBlock == Blocks.BROWN_MUSHROOM) {
                    blockflowerpot$enumflowertype = WrapperBlockFlowerPot.EnumFlowerType.MUSHROOM_BROWN;
                } else if (wrapperBlock == Blocks.DEADBUSH) {
                    blockflowerpot$enumflowertype = WrapperBlockFlowerPot.EnumFlowerType.DEAD_BUSH;
                } else if (wrapperBlock == Blocks.CACTUS) {
                    blockflowerpot$enumflowertype = WrapperBlockFlowerPot.EnumFlowerType.CACTUS;
                }
            }
        }

        return state.withProperty(CONTENTS, blockflowerpot$enumflowertype);
    }

    /**
     * Gets the render layer this block will render on. SOLID for solid blocks, CUTOUT or CUTOUT_MIPPED for on-off
     * transparency (glass, reeds), TRANSLUCENT for fully blended transparency (stained glass)
     */
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
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
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IWrapperBlockState state, BlockPos pos,
        EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    /* ============================FORGE START===================================== */
    @Override
    public void getDrops(net.minecraft.util.NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos,
        IWrapperBlockState state, int fortune) {
        super.getDrops(drops, world, pos, state, fortune);
        TileEntityFlowerPot te = world.getTileEntity(pos) instanceof TileEntityFlowerPot
            ? (TileEntityFlowerPot) world.getTileEntity(pos)
            : null;
        if (te != null && te.getFlowerPotItem() != null)
            drops.add(new ItemStack(te.getFlowerPotItem(), 1, te.getFlowerPotData()));
    }

    @Override
    public boolean removedByPlayer(IWrapperBlockState state, World world, BlockPos pos, EntityPlayer player,
        boolean willHarvest) {
        if (willHarvest) return true; // If it will harvest, delay deletion of the block until after getDrops
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    /**
     * Spawns the block's drops in the world. By the time this is called the Block has possibly been set to air via
     * Block.removedByPlayer
     */
    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IWrapperBlockState state,
        @Nullable TileEntity te, ItemStack tool) {
        super.harvestBlock(world, player, pos, state, te, tool);
        world.setBlockToAir(pos);
    }
    /* ===========================FORGE END========================================== */

    public static enum EnumFlowerType implements IStringSerializable {

        EMPTY("empty"),
        POPPY("rose"),
        BLUE_ORCHID("blue_orchid"),
        ALLIUM("allium"),
        HOUSTONIA("houstonia"),
        RED_TULIP("red_tulip"),
        ORANGE_TULIP("orange_tulip"),
        WHITE_TULIP("white_tulip"),
        PINK_TULIP("pink_tulip"),
        OXEYE_DAISY("oxeye_daisy"),
        DANDELION("dandelion"),
        OAK_SAPLING("oak_sapling"),
        SPRUCE_SAPLING("spruce_sapling"),
        BIRCH_SAPLING("birch_sapling"),
        JUNGLE_SAPLING("jungle_sapling"),
        ACACIA_SAPLING("acacia_sapling"),
        DARK_OAK_SAPLING("dark_oak_sapling"),
        MUSHROOM_RED("mushroom_red"),
        MUSHROOM_BROWN("mushroom_brown"),
        DEAD_BUSH("dead_bush"),
        FERN("fern"),
        CACTUS("cactus");

        private final String name;

        private EnumFlowerType(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }

        public String getName() {
            return this.name;
        }
    }
}
