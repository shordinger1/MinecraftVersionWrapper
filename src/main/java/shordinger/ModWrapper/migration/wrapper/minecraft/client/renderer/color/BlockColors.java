package shordinger.ModWrapper.migration.wrapper.minecraft.client.renderer.color;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.BlockStem;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColorHelper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

@SideOnly(Side.CLIENT)
public class BlockColors {

    // FORGE: Use RegistryDelegates as non-Vanilla block ids are not constant
    private final java.util.Map<net.minecraftforge.registries.IRegistryDelegate<Block>, IBlockColor> blockColorMap = com.google.common.collect.Maps
        .newHashMap();

    public static BlockColors init() {
        final BlockColors blockcolors = new BlockColors();
        blockcolors.registerBlockColorHandler(new IBlockColor() {

            public int colorMultiplier(IWrapperBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos,
                int tintIndex) {
                BlockDoublePlant.EnumPlantType blockdoubleplant$enumplanttype = (BlockDoublePlant.EnumPlantType) state
                    .getValue(BlockDoublePlant.VARIANT);
                return worldIn != null && pos != null
                    && (blockdoubleplant$enumplanttype == BlockDoublePlant.EnumPlantType.GRASS
                        || blockdoubleplant$enumplanttype == BlockDoublePlant.EnumPlantType.FERN)
                            ? BiomeColorHelper.getGrassColorAtPos(
                                worldIn,
                                state.getValue(BlockDoublePlant.HALF) == BlockDoublePlant.EnumBlockHalf.UPPER
                                    ? pos.down()
                                    : pos)
                            : -1;
            }
        }, Blocks.DOUBLE_PLANT);
        blockcolors.registerBlockColorHandler(new IBlockColor() {

            public int colorMultiplier(IWrapperBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos,
                int tintIndex) {
                if (worldIn != null && pos != null) {
                    TileEntity tileentity = worldIn.getTileEntity(pos);

                    if (tileentity instanceof TileEntityFlowerPot) {
                        Item item = ((TileEntityFlowerPot) tileentity).getFlowerPotItem();
                        IWrapperBlockState iblockstate = Block.getBlockFromItem(item)
                            .getDefaultState();
                        return blockcolors.colorMultiplier(iblockstate, worldIn, pos, tintIndex);
                    } else {
                        return -1;
                    }
                } else {
                    return -1;
                }
            }
        }, Blocks.FLOWER_POT);
        blockcolors.registerBlockColorHandler(new IBlockColor() {

            public int colorMultiplier(IWrapperBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos,
                int tintIndex) {
                return worldIn != null && pos != null ? BiomeColorHelper.getGrassColorAtPos(worldIn, pos)
                    : ColorizerGrass.getGrassColor(0.5D, 1.0D);
            }
        }, Blocks.GRASS);
        blockcolors.registerBlockColorHandler(new IBlockColor() {

            public int colorMultiplier(IWrapperBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos,
                int tintIndex) {
                BlockPlanks.EnumType blockplanks$enumtype = (BlockPlanks.EnumType) state.getValue(BlockOldLeaf.VARIANT);

                if (blockplanks$enumtype == BlockPlanks.EnumType.SPRUCE) {
                    return ColorizerFoliage.getFoliageColorPine();
                } else if (blockplanks$enumtype == BlockPlanks.EnumType.BIRCH) {
                    return ColorizerFoliage.getFoliageColorBirch();
                } else {
                    return worldIn != null && pos != null ? BiomeColorHelper.getFoliageColorAtPos(worldIn, pos)
                        : ColorizerFoliage.getFoliageColorBasic();
                }
            }
        }, Blocks.LEAVES);
        blockcolors.registerBlockColorHandler(new IBlockColor() {

            public int colorMultiplier(IWrapperBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos,
                int tintIndex) {
                return worldIn != null && pos != null ? BiomeColorHelper.getFoliageColorAtPos(worldIn, pos)
                    : ColorizerFoliage.getFoliageColorBasic();
            }
        }, Blocks.LEAVES2);
        blockcolors.registerBlockColorHandler(new IBlockColor() {

            public int colorMultiplier(IWrapperBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos,
                int tintIndex) {
                return worldIn != null && pos != null ? BiomeColorHelper.getWaterColorAtPos(worldIn, pos) : -1;
            }
        }, Blocks.WATER, Blocks.FLOWING_WATER);
        blockcolors.registerBlockColorHandler(new IBlockColor() {

            public int colorMultiplier(IWrapperBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos,
                int tintIndex) {
                return BlockRedstoneWire
                    .colorMultiplier(((Integer) state.getValue(BlockRedstoneWire.POWER)).intValue());
            }
        }, Blocks.REDSTONE_WIRE);
        blockcolors.registerBlockColorHandler(new IBlockColor() {

            public int colorMultiplier(IWrapperBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos,
                int tintIndex) {
                return worldIn != null && pos != null ? BiomeColorHelper.getGrassColorAtPos(worldIn, pos) : -1;
            }
        }, Blocks.REEDS);
        blockcolors.registerBlockColorHandler(new IBlockColor() {

            public int colorMultiplier(IWrapperBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos,
                int tintIndex) {
                int i = ((Integer) state.getValue(BlockStem.AGE)).intValue();
                int j = i * 32;
                int k = 255 - i * 8;
                int l = i * 4;
                return j << 16 | k << 8 | l;
            }
        }, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
        blockcolors.registerBlockColorHandler(new IBlockColor() {

            public int colorMultiplier(IWrapperBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos,
                int tintIndex) {
                if (worldIn != null && pos != null) {
                    return BiomeColorHelper.getGrassColorAtPos(worldIn, pos);
                } else {
                    return state.getValue(BlockTallGrass.TYPE) == BlockTallGrass.EnumType.DEAD_BUSH ? 16777215
                        : ColorizerGrass.getGrassColor(0.5D, 1.0D);
                }
            }
        }, Blocks.TALLGRASS);
        blockcolors.registerBlockColorHandler(new IBlockColor() {

            public int colorMultiplier(IWrapperBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos,
                int tintIndex) {
                return worldIn != null && pos != null ? BiomeColorHelper.getFoliageColorAtPos(worldIn, pos)
                    : ColorizerFoliage.getFoliageColorBasic();
            }
        }, Blocks.VINE);
        blockcolors.registerBlockColorHandler(new IBlockColor() {

            public int colorMultiplier(IWrapperBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos,
                int tintIndex) {
                return worldIn != null && pos != null ? 2129968 : 7455580;
            }
        }, Blocks.WATERLILY);
        net.minecraftforge.client.ForgeHooksClient.onBlockColorsInit(blockcolors);
        return blockcolors;
    }

    public int getColor(IWrapperBlockState state, World p_189991_2_, BlockPos p_189991_3_) {
        IBlockColor iblockcolor = this.blockColorMap.get(state.getBlock().delegate);

        if (iblockcolor != null) {
            return iblockcolor.colorMultiplier(state, (IBlockAccess) null, (BlockPos) null, 0);
        } else {
            MapColor mapcolor = state.getMapColor(p_189991_2_, p_189991_3_);
            return mapcolor != null ? mapcolor.colorValue : -1;
        }
    }

    public int colorMultiplier(IWrapperBlockState state, @Nullable IBlockAccess blockAccess, @Nullable BlockPos pos,
        int tintIndex) {
        IBlockColor iblockcolor = this.blockColorMap.get(state.getBlock().delegate);
        return iblockcolor == null ? -1 : iblockcolor.colorMultiplier(state, blockAccess, pos, tintIndex);
    }

    public void registerBlockColorHandler(IBlockColor blockColor, Block... blocksIn) {
        for (Block block : blocksIn) {
            if (block == null)
                throw new IllegalArgumentException("Block registered to block color handler cannot be null!");
            if (block.getRegistryName() == null)
                throw new IllegalArgumentException("Block must be registered before assigning color handler.");
            this.blockColorMap.put(block.delegate, blockColor);
        }
    }
}
