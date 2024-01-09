package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.AxisAlignedBB;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.MathHelper;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.Vec3d;

public abstract class WrapperBlockLiquid extends WrapperBlock {

    public static final PropertyInteger LEVEL = PropertyInteger.create("level", 0, 15);

    protected WrapperBlockLiquid(Material materialIn) {
        super(materialIn);
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(LEVEL, Integer.valueOf(0)));
        this.setTickRandomly(true);
    }

    public AxisAlignedBB getBoundingBox(IWrapperBlockState state, IBlockAccess source, BlockPos pos) {
        return FULL_BLOCK_AABB;
    }

    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IWrapperBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    /**
     * Determines if an entity can path through this block
     */
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return this.blockWrapperMaterial != Material.LAVA;
    }

    /**
     * Returns the percentage of the liquid block that is air, based on the given flow decay of the liquid
     */
    public static float getLiquidHeightPercent(int meta) {
        if (meta >= 8) {
            meta = 0;
        }

        return (float) (meta + 1) / 9.0F;
    }

    protected int getDepth(IWrapperBlockState state) {
        return state.getMaterial() == this.blockWrapperMaterial ? ((Integer) state.getValue(LEVEL)).intValue() : -1;
    }

    protected int getRenderedDepth(IWrapperBlockState state) {
        int i = this.getDepth(state);
        return i >= 8 ? 0 : i;
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

    public boolean canCollideCheck(IWrapperBlockState state, boolean hitIfLiquid) {
        return hitIfLiquid && ((Integer) state.getValue(LEVEL)).intValue() == 0;
    }

    /**
     * Checks if an additional {@code -6} vertical drag should be applied to the entity. See {#link
     * net.minecraft.block.BlockLiquid#getFlow()}
     */
    private boolean causesDownwardCurrent(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
        IWrapperBlockState iblockstate = worldIn.getBlockState(pos);
        WrapperBlock wrapperBlock = iblockstate.getBlock();
        Material material = iblockstate.getMaterial();

        if (material == this.blockWrapperMaterial) {
            return false;
        } else if (side == EnumFacing.UP) {
            return true;
        } else if (material == Material.ICE) {
            return false;
        } else {
            boolean flag = isExceptBlockForAttachWithPiston(wrapperBlock) || wrapperBlock instanceof WrapperBlockStairs;
            return !flag && iblockstate.getBlockFaceShape(worldIn, pos, side) == BlockFaceShape.SOLID;
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IWrapperBlockState blockState, IBlockAccess blockAccess, BlockPos pos,
        EnumFacing side) {
        if (blockAccess.getBlockState(pos.offset(side))
            .getMaterial() == this.blockWrapperMaterial) {
            return false;
        } else {
            return side == EnumFacing.UP ? true : super.shouldSideBeRendered(blockState, blockAccess, pos, side);
        }
    }

    /**
     * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
     * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
     */
    public EnumBlockRenderType getRenderType(IWrapperBlockState state) {
        return EnumBlockRenderType.LIQUID;
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    public Item getItemDropped(IWrapperBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    public int quantityDropped(Random random) {
        return 0;
    }

    protected Vec3d getFlow(IBlockAccess worldIn, BlockPos pos, IWrapperBlockState state) {
        double d0 = 0.0D;
        double d1 = 0.0D;
        double d2 = 0.0D;
        int i = this.getRenderedDepth(state);
        BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

        for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
            blockpos$pooledmutableblockpos.setPos(pos)
                .move(enumfacing);
            int j = this.getRenderedDepth(worldIn.getBlockState(blockpos$pooledmutableblockpos));

            if (j < 0) {
                if (!worldIn.getBlockState(blockpos$pooledmutableblockpos)
                    .getMaterial()
                    .blocksMovement()) {
                    j = this.getRenderedDepth(worldIn.getBlockState(blockpos$pooledmutableblockpos.down()));

                    if (j >= 0) {
                        int k = j - (i - 8);
                        d0 += (double) (enumfacing.getFrontOffsetX() * k);
                        d1 += (double) (enumfacing.getFrontOffsetY() * k);
                        d2 += (double) (enumfacing.getFrontOffsetZ() * k);
                    }
                }
            } else if (j >= 0) {
                int l = j - i;
                d0 += (double) (enumfacing.getFrontOffsetX() * l);
                d1 += (double) (enumfacing.getFrontOffsetY() * l);
                d2 += (double) (enumfacing.getFrontOffsetZ() * l);
            }
        }

        Vec3d vec3d = new Vec3d(d0, d1, d2);

        if (((Integer) state.getValue(LEVEL)).intValue() >= 8) {
            for (EnumFacing enumfacing1 : EnumFacing.Plane.HORIZONTAL) {
                blockpos$pooledmutableblockpos.setPos(pos)
                    .move(enumfacing1);

                if (this.causesDownwardCurrent(worldIn, blockpos$pooledmutableblockpos, enumfacing1)
                    || this.causesDownwardCurrent(worldIn, blockpos$pooledmutableblockpos.up(), enumfacing1)) {
                    vec3d = vec3d.normalize()
                        .addVector(0.0D, -6.0D, 0.0D);
                    break;
                }
            }
        }

        blockpos$pooledmutableblockpos.release();
        return vec3d.normalize();
    }

    public Vec3d modifyAcceleration(World worldIn, BlockPos pos, Entity entityIn, Vec3d motion) {
        return motion.add(this.getFlow(worldIn, pos, worldIn.getBlockState(pos)));
    }

    /**
     * How many world ticks before ticking
     */
    public int tickRate(World worldIn) {
        if (this.blockWrapperMaterial == Material.WATER) {
            return 5;
        } else if (this.blockWrapperMaterial == Material.LAVA) {
            return worldIn.provider.isNether() ? 10 : 30;
        } else {
            return 0;
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldRenderSides(IBlockAccess blockAccess, BlockPos pos) {
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                IWrapperBlockState iblockstate = blockAccess.getBlockState(pos.add(i, 0, j));

                if (iblockstate.getMaterial() != this.blockWrapperMaterial && !iblockstate.isFullBlock()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Called after the block is set in the Chunk data, but before the Tile Entity is set
     */
    public void onBlockAdded(World worldIn, BlockPos pos, IWrapperBlockState state) {
        this.checkForMixing(worldIn, pos, state);
    }

    /**
     * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
     * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
     * block, etc.
     */
    public void neighborChanged(IWrapperBlockState state, World worldIn, BlockPos pos, WrapperBlock wrapperBlockIn,
        BlockPos fromPos) {
        this.checkForMixing(worldIn, pos, state);
    }

    @SideOnly(Side.CLIENT)
    public int getPackedLightmapCoords(IWrapperBlockState state, IBlockAccess source, BlockPos pos) {
        int i = source.getCombinedLight(pos, 0);
        int j = source.getCombinedLight(pos.up(), 0);
        int k = i & 255;
        int l = j & 255;
        int i1 = i >> 16 & 255;
        int j1 = j >> 16 & 255;
        return (k > l ? k : l) | (i1 > j1 ? i1 : j1) << 16;
    }

    public boolean checkForMixing(World worldIn, BlockPos pos, IWrapperBlockState state) {
        if (this.blockWrapperMaterial == Material.LAVA) {
            boolean flag = false;

            for (EnumFacing enumfacing : EnumFacing.values()) {
                if (enumfacing != EnumFacing.DOWN && worldIn.getBlockState(pos.offset(enumfacing))
                    .getMaterial() == Material.WATER) {
                    flag = true;
                    break;
                }
            }

            if (flag) {
                Integer integer = (Integer) state.getValue(LEVEL);

                if (integer.intValue() == 0) {
                    worldIn.setBlockState(
                        pos,
                        net.minecraftforge.event.ForgeEventFactory
                            .fireFluidPlaceBlockEvent(worldIn, pos, pos, Blocks.OBSIDIAN.getDefaultState()));
                    this.triggerMixEffects(worldIn, pos);
                    return true;
                }

                if (integer.intValue() <= 4) {
                    worldIn.setBlockState(
                        pos,
                        net.minecraftforge.event.ForgeEventFactory
                            .fireFluidPlaceBlockEvent(worldIn, pos, pos, Blocks.COBBLESTONE.getDefaultState()));
                    this.triggerMixEffects(worldIn, pos);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Gets the render layer this block will render on. SOLID for solid blocks, CUTOUT or CUTOUT_MIPPED for on-off
     * transparency (glass, reeds), TRANSLUCENT for fully blended transparency (stained glass)
     */
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return this.blockWrapperMaterial == Material.WATER ? BlockRenderLayer.TRANSLUCENT : BlockRenderLayer.SOLID;
    }

    /**
     * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
     * this method is unrelated to {@link randomTick} and {@link #needsRandomTick}, and will always be called regardless
     * of whether the block can receive random update ticks
     */
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IWrapperBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        double d0 = (double) pos.getX();
        double d1 = (double) pos.getY();
        double d2 = (double) pos.getZ();

        if (this.blockWrapperMaterial == Material.WATER) {
            int i = ((Integer) stateIn.getValue(LEVEL)).intValue();

            if (i > 0 && i < 8) {
                if (rand.nextInt(64) == 0) {
                    worldIn.playSound(
                        d0 + 0.5D,
                        d1 + 0.5D,
                        d2 + 0.5D,
                        SoundEvents.BLOCK_WATER_AMBIENT,
                        SoundCategory.BLOCKS,
                        rand.nextFloat() * 0.25F + 0.75F,
                        rand.nextFloat() + 0.5F,
                        false);
                }
            } else if (rand.nextInt(10) == 0) {
                worldIn.spawnParticle(
                    EnumParticleTypes.SUSPENDED,
                    d0 + (double) rand.nextFloat(),
                    d1 + (double) rand.nextFloat(),
                    d2 + (double) rand.nextFloat(),
                    0.0D,
                    0.0D,
                    0.0D);
            }
        }

        if (this.blockWrapperMaterial == Material.LAVA && worldIn.getBlockState(pos.up())
            .getMaterial() == Material.AIR
            && !worldIn.getBlockState(pos.up())
                .isOpaqueCube()) {
            if (rand.nextInt(100) == 0) {
                double d8 = d0 + (double) rand.nextFloat();
                double d4 = d1 + stateIn.getBoundingBox(worldIn, pos).maxY;
                double d6 = d2 + (double) rand.nextFloat();
                worldIn.spawnParticle(EnumParticleTypes.LAVA, d8, d4, d6, 0.0D, 0.0D, 0.0D);
                worldIn.playSound(
                    d8,
                    d4,
                    d6,
                    SoundEvents.BLOCK_LAVA_POP,
                    SoundCategory.BLOCKS,
                    0.2F + rand.nextFloat() * 0.2F,
                    0.9F + rand.nextFloat() * 0.15F,
                    false);
            }

            if (rand.nextInt(200) == 0) {
                worldIn.playSound(
                    d0,
                    d1,
                    d2,
                    SoundEvents.BLOCK_LAVA_AMBIENT,
                    SoundCategory.BLOCKS,
                    0.2F + rand.nextFloat() * 0.2F,
                    0.9F + rand.nextFloat() * 0.15F,
                    false);
            }
        }

        if (rand.nextInt(10) == 0 && worldIn.getBlockState(pos.down())
            .isTopSolid()) {
            Material material = worldIn.getBlockState(pos.down(2))
                .getMaterial();

            if (!material.blocksMovement() && !material.isLiquid()) {
                double d3 = d0 + (double) rand.nextFloat();
                double d5 = d1 - 1.05D;
                double d7 = d2 + (double) rand.nextFloat();

                if (this.blockWrapperMaterial == Material.WATER) {
                    worldIn.spawnParticle(EnumParticleTypes.DRIP_WATER, d3, d5, d7, 0.0D, 0.0D, 0.0D);
                } else {
                    worldIn.spawnParticle(EnumParticleTypes.DRIP_LAVA, d3, d5, d7, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static float getSlopeAngle(IBlockAccess worldIn, BlockPos pos, Material materialIn,
        IWrapperBlockState state) {
        Vec3d vec3d = getFlowingBlock(materialIn).getFlow(worldIn, pos, state);
        return vec3d.x == 0.0D && vec3d.z == 0.0D ? -1000.0F
            : (float) MathHelper.atan2(vec3d.z, vec3d.x) - ((float) Math.PI / 2F);
    }

    protected void triggerMixEffects(World worldIn, BlockPos pos) {
        double d0 = (double) pos.getX();
        double d1 = (double) pos.getY();
        double d2 = (double) pos.getZ();
        worldIn.playSound(
            (EntityPlayer) null,
            pos,
            SoundEvents.BLOCK_LAVA_EXTINGUISH,
            SoundCategory.BLOCKS,
            0.5F,
            2.6F + (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.8F);

        for (int i = 0; i < 8; ++i) {
            worldIn.spawnParticle(
                EnumParticleTypes.SMOKE_LARGE,
                d0 + Math.random(),
                d1 + 1.2D,
                d2 + Math.random(),
                0.0D,
                0.0D,
                0.0D);
        }
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IWrapperBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
            .withProperty(LEVEL, Integer.valueOf(meta));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IWrapperBlockState state) {
        return ((Integer) state.getValue(LEVEL)).intValue();
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { LEVEL });
    }

    public static WrapperBlockDynamicLiquid getFlowingBlock(Material materialIn) {
        if (materialIn == Material.WATER) {
            return Blocks.FLOWING_WATER;
        } else if (materialIn == Material.LAVA) {
            return Blocks.FLOWING_LAVA;
        } else {
            throw new IllegalArgumentException("Invalid material");
        }
    }

    public static WrapperBlockStaticLiquid getStaticBlock(Material materialIn) {
        if (materialIn == Material.WATER) {
            return Blocks.WATER;
        } else if (materialIn == Material.LAVA) {
            return Blocks.LAVA;
        } else {
            throw new IllegalArgumentException("Invalid material");
        }
    }

    public static float getBlockLiquidHeight(IWrapperBlockState state, IBlockAccess worldIn, BlockPos pos) {
        int i = ((Integer) state.getValue(LEVEL)).intValue();
        return (i & 7) == 0 && worldIn.getBlockState(pos.up())
            .getMaterial() == Material.WATER ? 1.0F : 1.0F - getLiquidHeightPercent(i);
    }

    public static float getLiquidHeight(IWrapperBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return (float) pos.getY() + getBlockLiquidHeight(state, worldIn, pos);
    }

    @Override
    public float getBlockLiquidHeight(World world, BlockPos pos, IWrapperBlockState state, Material material) {
        return WrapperBlockLiquid.getBlockLiquidHeight(state, world, pos);
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

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3d getFogColor(World world, BlockPos pos, IWrapperBlockState state, Entity entity, Vec3d originalColor,
        float partialTicks) {
        Vec3d viewport = net.minecraft.client.renderer.ActiveRenderInfo.projectViewFromEntity(entity, partialTicks);

        if (state.getMaterial()
            .isLiquid()) {
            float height = 0.0F;
            if (state.getBlock() instanceof WrapperBlockLiquid) {
                height = getLiquidHeightPercent(state.getValue(LEVEL)) - 0.11111111F;
            }
            float f1 = (float) (pos.getY() + 1) - height;
            if (viewport.y > (double) f1) {
                BlockPos upPos = pos.up();
                IWrapperBlockState upState = world.getBlockState(upPos);
                return upState.getBlock()
                    .getFogColor(world, upPos, upState, entity, originalColor, partialTicks);
            }
        }

        return super.getFogColor(world, pos, state, entity, originalColor, partialTicks);
    }
}