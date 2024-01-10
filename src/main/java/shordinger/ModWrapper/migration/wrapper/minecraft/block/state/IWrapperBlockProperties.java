package shordinger.ModWrapper.migration.wrapper.minecraft.block.state;

import java.util.List;

import javax.annotation.Nullable;


import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shordinger.ModWrapper.migration.wrapper.minecraft.block.material.EnumPushReaction;
import shordinger.ModWrapper.migration.wrapper.minecraft.block.material.MapColor;
import shordinger.ModWrapper.migration.wrapper.minecraft.block.material.Material;
import shordinger.ModWrapper.migration.wrapper.minecraft.entity.Entity;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.EnumBlockRenderType;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.Mirror;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.Rotation;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.AxisAlignedBB;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.RayTraceResult;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.Vec3d;
import shordinger.ModWrapper.migration.wrapper.minecraft.world.IWrapperBlockAccess;

public interface IWrapperBlockProperties {

    Material getMaterial();

    boolean isFullBlock();

    boolean canEntitySpawn(Entity entityIn);

    @Deprecated
        // Forge location aware version below
    int getLightOpacity();

    int getLightOpacity(IWrapperBlockAccess world, BlockPos pos);

    @Deprecated
        // Forge location aware version below
    int getLightValue();

    int getLightValue(IWrapperBlockAccess world, BlockPos pos);

    @SideOnly(Side.CLIENT)
    boolean isTranslucent();

    boolean useNeighborBrightness();

    MapColor getMapColor(IWrapperBlockAccess p_185909_1_, BlockPos p_185909_2_);

    /**
     * Returns the blockstate with the given rotation. If inapplicable, returns itself.
     */
    IBlockState withRotation(Rotation rot);

    /**
     * Returns the blockstate mirrored in the given way. If inapplicable, returns itself.
     */
    IBlockState withMirror(Mirror mirrorIn);

    boolean isFullCube();

    @SideOnly(Side.CLIENT)
    boolean hasCustomBreakingProgress();

    EnumBlockRenderType getRenderType();

    @SideOnly(Side.CLIENT)
    int getPackedLightmapCoords(IWrapperBlockAccess source, BlockPos pos);

    @SideOnly(Side.CLIENT)
    float getAmbientOcclusionLightValue();

    boolean isBlockNormalCube();

    boolean isNormalCube();

    boolean canProvidePower();

    int getWeakPower(IWrapperBlockAccess blockAccess, BlockPos pos, EnumFacing side);

    boolean hasComparatorInputOverride();

    int getComparatorInputOverride(World worldIn, BlockPos pos);

    float getBlockHardness(World worldIn, BlockPos pos);

    float getPlayerRelativeBlockHardness(EntityPlayer player, World worldIn, BlockPos pos);

    int getStrongPower(IWrapperBlockAccess blockAccess, BlockPos pos, EnumFacing side);

    EnumPushReaction getMobilityFlag();

    IBlockState getActualState(IWrapperBlockAccess blockAccess, BlockPos pos);

    @SideOnly(Side.CLIENT)
    AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos);

    @SideOnly(Side.CLIENT)
    boolean shouldSideBeRendered(IWrapperBlockAccess blockAccess, BlockPos pos, EnumFacing facing);

    boolean isOpaqueCube();

    @Nullable
    AxisAlignedBB getCollisionBoundingBox(IWrapperBlockAccess worldIn, BlockPos pos);

    void addCollisionBoxToList(World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes,
                               @Nullable Entity entityIn, boolean p_185908_6_);

    AxisAlignedBB getBoundingBox(IWrapperBlockAccess blockAccess, BlockPos pos);

    RayTraceResult collisionRayTrace(World worldIn, BlockPos pos, Vec3d start, Vec3d end);

    /**
     * Determines if the block is solid enough on the top side to support other blocks, like redstone components.
     */
    @Deprecated
    // Forge: Use isSideSolid(IWrapperBlockAccess, BlockPos, EnumFacing.UP) instead
    boolean isTopSolid();

    // Forge added functions
    boolean doesSideBlockRendering(IWrapperBlockAccess world, BlockPos pos, EnumFacing side);

    boolean isSideSolid(IWrapperBlockAccess world, BlockPos pos, EnumFacing side);

    boolean doesSideBlockChestOpening(IWrapperBlockAccess world, BlockPos pos, EnumFacing side);

    Vec3d getOffset(IWrapperBlockAccess access, BlockPos pos);

    boolean causesSuffocation();

    BlockFaceShape getBlockFaceShape(IWrapperBlockAccess worldIn, BlockPos pos, EnumFacing facing);
}
