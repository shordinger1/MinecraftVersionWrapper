package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDaylightDetector;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.NonNullList;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.AxisAlignedBB;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.MathHelper;

public class BlockDaylightDetector extends BlockContainer {

    public static final PropertyInteger POWER = PropertyInteger.create("power", 0, 15);
    protected static final AxisAlignedBB DAYLIGHT_DETECTOR_AABB = new AxisAlignedBB(
        0.0D,
        0.0D,
        0.0D,
        1.0D,
        0.375D,
        1.0D);
    private final boolean inverted;

    public BlockDaylightDetector(boolean inverted) {
        super(Material.WOOD);
        this.inverted = inverted;
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(POWER, Integer.valueOf(0)));
        this.setCreativeTab(CreativeTabs.REDSTONE);
        this.setHardness(0.2F);
        this.setSoundType(SoundType.WOOD);
        this.setUnlocalizedName("daylightDetector");
    }

    public AxisAlignedBB getBoundingBox(IWrapperBlockState state, IBlockAccess source, BlockPos pos) {
        return DAYLIGHT_DETECTOR_AABB;
    }

    public int getWeakPower(IWrapperBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return ((Integer) blockState.getValue(POWER)).intValue();
    }

    public void updatePower(World worldIn, BlockPos pos) {
        if (worldIn.provider.hasSkyLight()) {
            IWrapperBlockState iblockstate = worldIn.getBlockState(pos);
            int i = worldIn.getLightFor(EnumSkyBlock.SKY, pos) - worldIn.getSkylightSubtracted();
            float f = worldIn.getCelestialAngleRadians(1.0F);

            if (this.inverted) {
                i = 15 - i;
            }

            if (i > 0 && !this.inverted) {
                float f1 = f < (float) Math.PI ? 0.0F : ((float) Math.PI * 2F);
                f = f + (f1 - f) * 0.2F;
                i = Math.round((float) i * MathHelper.cos(f));
            }

            i = MathHelper.clamp(i, 0, 15);

            if (((Integer) iblockstate.getValue(POWER)).intValue() != i) {
                worldIn.setBlockState(pos, iblockstate.withProperty(POWER, Integer.valueOf(i)), 3);
            }
        }
    }

    /**
     * Called when the block is right clicked by a player.
     */
    public boolean onBlockActivated(World worldIn, BlockPos pos, IWrapperBlockState state, EntityPlayer playerIn,
        EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (playerIn.isAllowEdit()) {
            if (worldIn.isRemote) {
                return true;
            } else {
                if (this.inverted) {
                    worldIn.setBlockState(
                        pos,
                        Blocks.DAYLIGHT_DETECTOR.getDefaultState()
                            .withProperty(POWER, state.getValue(POWER)),
                        4);
                    Blocks.DAYLIGHT_DETECTOR.updatePower(worldIn, pos);
                } else {
                    worldIn.setBlockState(
                        pos,
                        Blocks.DAYLIGHT_DETECTOR_INVERTED.getDefaultState()
                            .withProperty(POWER, state.getValue(POWER)),
                        4);
                    Blocks.DAYLIGHT_DETECTOR_INVERTED.updatePower(worldIn, pos);
                }

                return true;
            }
        } else {
            return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
        }
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    public Item getItemDropped(IWrapperBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(Blocks.DAYLIGHT_DETECTOR);
    }

    public ItemStack getItem(World worldIn, BlockPos pos, IWrapperBlockState state) {
        return new ItemStack(Blocks.DAYLIGHT_DETECTOR);
    }

    public boolean isFullCube(IWrapperBlockState state) {
        return false;
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

    /**
     * Can this block provide power. Only wire currently seems to have this change based on its state.
     */
    public boolean canProvidePower(IWrapperBlockState state) {
        return true;
    }

    /**
     * Returns a new instance of a block's tile entity class. Called on placing the block.
     */
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityDaylightDetector();
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IWrapperBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
            .withProperty(POWER, Integer.valueOf(meta));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        return ((Integer) state.getValue(POWER)).intValue();
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { POWER });
    }

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        if (!this.inverted) {
            super.getSubBlocks(itemIn, items);
        }
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
        return face == EnumFacing.DOWN ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }
}
