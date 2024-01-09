package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.AxisAlignedBB;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WrapperBlockDoublePlant extends WrapperBlockBush
    implements IGrowable, net.minecraftforge.common.IShearable {

    public static final PropertyEnum<WrapperBlockDoublePlant.EnumPlantType> VARIANT = PropertyEnum.<WrapperBlockDoublePlant.EnumPlantType>create(
        "variant",
        WrapperBlockDoublePlant.EnumPlantType.class);
    public static final PropertyEnum<WrapperBlockDoublePlant.EnumBlockHalf> HALF = PropertyEnum.<WrapperBlockDoublePlant.EnumBlockHalf>create(
        "half",
        WrapperBlockDoublePlant.EnumBlockHalf.class);
    public static final PropertyEnum<EnumFacing> FACING = WrapperBlockHorizontal.FACING;

    public WrapperBlockDoublePlant() {
        super(Material.VINE);
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(VARIANT, WrapperBlockDoublePlant.EnumPlantType.SUNFLOWER)
                .withProperty(HALF, WrapperBlockDoublePlant.EnumBlockHalf.LOWER)
                .withProperty(FACING, EnumFacing.NORTH));
        this.setHardness(0.0F);
        this.setSoundType(SoundType.PLANT);
        this.setUnlocalizedName("doublePlant");
    }

    public AxisAlignedBB getBoundingBox(IWrapperBlockState state, IBlockAccess source, BlockPos pos) {
        return FULL_BLOCK_AABB;
    }

    private WrapperBlockDoublePlant.EnumPlantType getType(IBlockAccess blockAccess, BlockPos pos,
        IWrapperBlockState state) {
        if (state.getBlock() == this) {
            state = state.getActualState(blockAccess, pos);
            return (WrapperBlockDoublePlant.EnumPlantType) state.getValue(VARIANT);
        } else {
            return WrapperBlockDoublePlant.EnumPlantType.FERN;
        }
    }

    /**
     * Checks if this block can be placed exactly at the given position.
     */
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return super.canPlaceBlockAt(worldIn, pos) && worldIn.isAirBlock(pos.up());
    }

    /**
     * Whether this Block can be replaced directly by other blocks (true for e.g. tall grass)
     */
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        IWrapperBlockState iblockstate = worldIn.getBlockState(pos);

        if (iblockstate.getBlock() != this) {
            return true;
        } else {
            WrapperBlockDoublePlant.EnumPlantType blockdoubleplant$enumplanttype = (WrapperBlockDoublePlant.EnumPlantType) iblockstate
                .getActualState(worldIn, pos)
                .getValue(VARIANT);
            return blockdoubleplant$enumplanttype == WrapperBlockDoublePlant.EnumPlantType.FERN
                || blockdoubleplant$enumplanttype == WrapperBlockDoublePlant.EnumPlantType.GRASS;
        }
    }

    protected void checkAndDropBlock(World worldIn, BlockPos pos, IWrapperBlockState state) {
        if (!this.canBlockStay(worldIn, pos, state)) {
            boolean flag = state.getValue(HALF) == WrapperBlockDoublePlant.EnumBlockHalf.UPPER;
            BlockPos blockpos = flag ? pos : pos.up();
            BlockPos blockpos1 = flag ? pos.down() : pos;
            WrapperBlock wrapperBlock = (WrapperBlock) (flag ? this
                : worldIn.getBlockState(blockpos)
                    .getBlock());
            WrapperBlock wrapperBlock1 = (WrapperBlock) (flag ? worldIn.getBlockState(blockpos1)
                .getBlock() : this);

            if (!flag) this.dropBlockAsItem(worldIn, pos, state, 0); // Forge move above the setting to air.

            if (wrapperBlock == this) {
                worldIn.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 2);
            }

            if (wrapperBlock1 == this) {
                worldIn.setBlockState(blockpos1, Blocks.AIR.getDefaultState(), 3);
            }
        }
    }

    public boolean canBlockStay(World worldIn, BlockPos pos, IWrapperBlockState state) {
        if (state.getBlock() != this) return super.canBlockStay(worldIn, pos, state); // Forge: This function is called
                                                                                      // during world gen and placement,
                                                                                      // before this block is set, so if
                                                                                      // we are not 'here' then assume
                                                                                      // it's the pre-check.
        if (state.getValue(HALF) == WrapperBlockDoublePlant.EnumBlockHalf.UPPER) {
            return worldIn.getBlockState(pos.down())
                .getBlock() == this;
        } else {
            IWrapperBlockState iblockstate = worldIn.getBlockState(pos.up());
            return iblockstate.getBlock() == this && super.canBlockStay(worldIn, pos, iblockstate);
        }
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    public Item getItemDropped(IWrapperBlockState state, Random rand, int fortune) {
        if (state.getValue(HALF) == WrapperBlockDoublePlant.EnumBlockHalf.UPPER) {
            return Items.AIR;
        } else {
            WrapperBlockDoublePlant.EnumPlantType blockdoubleplant$enumplanttype = (WrapperBlockDoublePlant.EnumPlantType) state
                .getValue(VARIANT);

            if (blockdoubleplant$enumplanttype == WrapperBlockDoublePlant.EnumPlantType.FERN) {
                return Items.AIR;
            } else if (blockdoubleplant$enumplanttype == WrapperBlockDoublePlant.EnumPlantType.GRASS) {
                return rand.nextInt(8) == 0 ? Items.WHEAT_SEEDS : Items.AIR;
            } else {
                return super.getItemDropped(state, rand, fortune);
            }
        }
    }

    /**
     * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
     * returns the metadata of the dropped item based on the old metadata of the block.
     */
    public int damageDropped(IWrapperBlockState state) {
        return state.getValue(HALF) != WrapperBlockDoublePlant.EnumBlockHalf.UPPER
            && state.getValue(VARIANT) != WrapperBlockDoublePlant.EnumPlantType.GRASS
                ? ((WrapperBlockDoublePlant.EnumPlantType) state.getValue(VARIANT)).getMeta()
                : 0;
    }

    public void placeAt(World worldIn, BlockPos lowerPos, WrapperBlockDoublePlant.EnumPlantType variant, int flags) {
        worldIn.setBlockState(
            lowerPos,
            this.getDefaultState()
                .withProperty(HALF, WrapperBlockDoublePlant.EnumBlockHalf.LOWER)
                .withProperty(VARIANT, variant),
            flags);
        worldIn.setBlockState(
            lowerPos.up(),
            this.getDefaultState()
                .withProperty(HALF, WrapperBlockDoublePlant.EnumBlockHalf.UPPER),
            flags);
    }

    /**
     * Called by ItemBlocks after a block is set in the world, to allow post-place logic
     */
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IWrapperBlockState state, EntityLivingBase placer,
        ItemStack stack) {
        worldIn.setBlockState(
            pos.up(),
            this.getDefaultState()
                .withProperty(HALF, WrapperBlockDoublePlant.EnumBlockHalf.UPPER),
            2);
    }

    /**
     * Spawns the block's drops in the world. By the time this is called the Block has possibly been set to air via
     * Block.removedByPlayer
     */
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IWrapperBlockState state,
        @Nullable TileEntity te, ItemStack stack) {
        {
            super.harvestBlock(worldIn, player, pos, state, te, stack);
        }
    }

    /**
     * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually
     * collect this block
     */
    public void onBlockHarvested(World worldIn, BlockPos pos, IWrapperBlockState state, EntityPlayer player) {
        if (state.getValue(HALF) == WrapperBlockDoublePlant.EnumBlockHalf.UPPER) {
            if (worldIn.getBlockState(pos.down())
                .getBlock() == this) {
                if (player.capabilities.isCreativeMode) {
                    worldIn.setBlockToAir(pos.down());
                } else {
                    IWrapperBlockState iblockstate = worldIn.getBlockState(pos.down());
                    WrapperBlockDoublePlant.EnumPlantType blockdoubleplant$enumplanttype = (WrapperBlockDoublePlant.EnumPlantType) iblockstate
                        .getValue(VARIANT);

                    if (blockdoubleplant$enumplanttype != WrapperBlockDoublePlant.EnumPlantType.FERN
                        && blockdoubleplant$enumplanttype != WrapperBlockDoublePlant.EnumPlantType.GRASS) {
                        worldIn.destroyBlock(pos.down(), true);
                    } else if (worldIn.isRemote) {
                        worldIn.setBlockToAir(pos.down());
                    } else if (!player.getHeldItemMainhand()
                        .isEmpty()
                        && player.getHeldItemMainhand()
                            .getItem() == Items.SHEARS) {
                                this.onHarvest(worldIn, pos, iblockstate, player);
                                worldIn.setBlockToAir(pos.down());
                            } else {
                                worldIn.destroyBlock(pos.down(), true);
                            }
                }
            }
        } else if (worldIn.getBlockState(pos.up())
            .getBlock() == this) {
                worldIn.setBlockState(pos.up(), Blocks.AIR.getDefaultState(), 2);
            }

        super.onBlockHarvested(worldIn, pos, state, player);
    }

    private boolean onHarvest(World worldIn, BlockPos pos, IWrapperBlockState state, EntityPlayer player) {
        WrapperBlockDoublePlant.EnumPlantType blockdoubleplant$enumplanttype = (WrapperBlockDoublePlant.EnumPlantType) state
            .getValue(VARIANT);

        if (blockdoubleplant$enumplanttype != WrapperBlockDoublePlant.EnumPlantType.FERN
            && blockdoubleplant$enumplanttype != WrapperBlockDoublePlant.EnumPlantType.GRASS) {
            return false;
        } else {
            player.addStat(StatList.getBlockStats(this));
            return true;
        }
    }

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (WrapperBlockDoublePlant.EnumPlantType blockdoubleplant$enumplanttype : WrapperBlockDoublePlant.EnumPlantType
            .values()) {
            items.add(new ItemStack(this, 1, blockdoubleplant$enumplanttype.getMeta()));
        }
    }

    public ItemStack getItem(World worldIn, BlockPos pos, IWrapperBlockState state) {
        return new ItemStack(
            this,
            1,
            this.getType(worldIn, pos, state)
                .getMeta());
    }

    /**
     * Whether this IGrowable can grow
     */
    public boolean canGrow(World worldIn, BlockPos pos, IWrapperBlockState state, boolean isClient) {
        WrapperBlockDoublePlant.EnumPlantType blockdoubleplant$enumplanttype = this.getType(worldIn, pos, state);
        return blockdoubleplant$enumplanttype != WrapperBlockDoublePlant.EnumPlantType.GRASS
            && blockdoubleplant$enumplanttype != WrapperBlockDoublePlant.EnumPlantType.FERN;
    }

    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IWrapperBlockState state) {
        return true;
    }

    public void grow(World worldIn, Random rand, BlockPos pos, IWrapperBlockState state) {
        spawnAsEntity(
            worldIn,
            pos,
            new ItemStack(
                this,
                1,
                this.getType(worldIn, pos, state)
                    .getMeta()));
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IWrapperBlockState getStateFromMeta(int meta) {
        return (meta & 8) > 0 ? this.getDefaultState()
            .withProperty(HALF, WrapperBlockDoublePlant.EnumBlockHalf.UPPER)
            : this.getDefaultState()
                .withProperty(HALF, WrapperBlockDoublePlant.EnumBlockHalf.LOWER)
                .withProperty(VARIANT, WrapperBlockDoublePlant.EnumPlantType.byMetadata(meta & 7));
    }

    /**
     * Get the actual Block state of this Block at the given position. This applies properties not visible in the
     * metadata, such as fence connections.
     */
    public IWrapperBlockState getActualState(IWrapperBlockState state, IBlockAccess worldIn, BlockPos pos) {
        if (state.getValue(HALF) == WrapperBlockDoublePlant.EnumBlockHalf.UPPER) {
            IWrapperBlockState iblockstate = worldIn.getBlockState(pos.down());

            if (iblockstate.getBlock() == this) {
                state = state.withProperty(VARIANT, iblockstate.getValue(VARIANT));
            }
        }

        return state;
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        return state.getValue(HALF) == WrapperBlockDoublePlant.EnumBlockHalf.UPPER
            ? 8 | ((EnumFacing) state.getValue(FACING)).getHorizontalIndex()
            : ((WrapperBlockDoublePlant.EnumPlantType) state.getValue(VARIANT)).getMeta();
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { HALF, VARIANT, FACING });
    }

    /**
     * Get the OffsetType for this Block. Determines if the model is rendered slightly offset.
     */
    public WrapperBlock.EnumOffsetType getOffsetType() {
        return WrapperBlock.EnumOffsetType.XZ;
    }

    @Override
    public boolean isShearable(ItemStack item, IBlockAccess world, BlockPos pos) {
        IWrapperBlockState state = world.getBlockState(pos);
        EnumPlantType type = (EnumPlantType) state.getValue(VARIANT);
        return state.getValue(HALF) == EnumBlockHalf.LOWER
            && (type == EnumPlantType.FERN || type == EnumPlantType.GRASS);
    }

    @Override
    public java.util.List<ItemStack> onSheared(ItemStack item, net.minecraft.world.IBlockAccess world, BlockPos pos,
        int fortune) {
        java.util.List<ItemStack> ret = new java.util.ArrayList<ItemStack>();
        EnumPlantType type = (EnumPlantType) world.getBlockState(pos)
            .getValue(VARIANT);
        if (type == EnumPlantType.FERN)
            ret.add(new ItemStack(Blocks.TALLGRASS, 2, WrapperBlockTallGrass.EnumType.FERN.getMeta()));
        if (type == EnumPlantType.GRASS)
            ret.add(new ItemStack(Blocks.TALLGRASS, 2, WrapperBlockTallGrass.EnumType.GRASS.getMeta()));
        return ret;
    }

    public static enum EnumBlockHalf implements IStringSerializable {

        UPPER,
        LOWER;

        public String toString() {
            return this.getName();
        }

        public String getName() {
            return this == UPPER ? "upper" : "lower";
        }
    }

    public static enum EnumPlantType implements IStringSerializable {

        SUNFLOWER(0, "sunflower"),
        SYRINGA(1, "syringa"),
        GRASS(2, "double_grass", "grass"),
        FERN(3, "double_fern", "fern"),
        ROSE(4, "double_rose", "rose"),
        PAEONIA(5, "paeonia");

        private static final WrapperBlockDoublePlant.EnumPlantType[] META_LOOKUP = new WrapperBlockDoublePlant.EnumPlantType[values().length];
        private final int meta;
        private final String name;
        private final String unlocalizedName;

        private EnumPlantType(int meta, String name) {
            this(meta, name, name);
        }

        private EnumPlantType(int meta, String name, String unlocalizedName) {
            this.meta = meta;
            this.name = name;
            this.unlocalizedName = unlocalizedName;
        }

        public int getMeta() {
            return this.meta;
        }

        public String toString() {
            return this.name;
        }

        public static WrapperBlockDoublePlant.EnumPlantType byMetadata(int meta) {
            if (meta < 0 || meta >= META_LOOKUP.length) {
                meta = 0;
            }

            return META_LOOKUP[meta];
        }

        public String getName() {
            return this.name;
        }

        public String getUnlocalizedName() {
            return this.unlocalizedName;
        }

        static {
            for (WrapperBlockDoublePlant.EnumPlantType blockdoubleplant$enumplanttype : values()) {
                META_LOOKUP[blockdoubleplant$enumplanttype.getMeta()] = blockdoubleplant$enumplanttype;
            }
        }
    }
}
