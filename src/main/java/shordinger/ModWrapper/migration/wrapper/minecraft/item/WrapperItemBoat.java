package shordinger.ModWrapper.migration.wrapper.minecraft.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.AxisAlignedBB;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.MathHelper;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.RayTraceResult;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.Vec3d;

public class WrapperItemBoat extends WrapperItem {

    private final EntityBoat.Type type;

    public WrapperItemBoat(EntityBoat.Type typeIn) {
        this.type = typeIn;
        this.maxStackSize = 1;
        this.setCreativeTab(CreativeTabs.TRANSPORTATION);
        this.setUnlocalizedName("boat." + typeIn.getName());
    }

    /**
     * Called when the equipped item is right clicked.
     */
    public ActionResult<TempItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        TempItemStack itemstack = playerIn.getHeldItem(handIn);
        float f = 1.0F;
        float f1 = playerIn.prevRotationPitch + (playerIn.rotationPitch - playerIn.prevRotationPitch) * 1.0F;
        float f2 = playerIn.prevRotationYaw + (playerIn.rotationYaw - playerIn.prevRotationYaw) * 1.0F;
        double d0 = playerIn.prevPosX + (playerIn.posX - playerIn.prevPosX) * 1.0D;
        double d1 = playerIn.prevPosY + (playerIn.posY - playerIn.prevPosY) * 1.0D + (double) playerIn.getEyeHeight();
        double d2 = playerIn.prevPosZ + (playerIn.posZ - playerIn.prevPosZ) * 1.0D;
        Vec3d vec3d = new Vec3d(d0, d1, d2);
        float f3 = MathHelper.cos(-f2 * 0.017453292F - (float) Math.PI);
        float f4 = MathHelper.sin(-f2 * 0.017453292F - (float) Math.PI);
        float f5 = -MathHelper.cos(-f1 * 0.017453292F);
        float f6 = MathHelper.sin(-f1 * 0.017453292F);
        float f7 = f4 * f5;
        float f8 = f3 * f5;
        double d3 = 5.0D;
        Vec3d vec3d1 = vec3d.addVector((double) f7 * 5.0D, (double) f6 * 5.0D, (double) f8 * 5.0D);
        RayTraceResult raytraceresult = worldIn.rayTraceBlocks(vec3d, vec3d1, true);

        if (raytraceresult == null) {
            return new ActionResult<TempItemStack>(EnumActionResult.PASS, itemstack);
        } else {
            Vec3d vec3d2 = playerIn.getLook(1.0F);
            boolean flag = false;
            List<Entity> list = worldIn.getEntitiesWithinAABBExcludingEntity(
                playerIn,
                playerIn.getEntityBoundingBox()
                    .expand(vec3d2.x * 5.0D, vec3d2.y * 5.0D, vec3d2.z * 5.0D)
                    .grow(1.0D));

            for (int i = 0; i < list.size(); ++i) {
                Entity entity = list.get(i);

                if (entity.canBeCollidedWith()) {
                    AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox()
                        .grow((double) entity.getCollisionBorderSize());

                    if (axisalignedbb.contains(vec3d)) {
                        flag = true;
                    }
                }
            }

            if (flag) {
                return new ActionResult<TempItemStack>(EnumActionResult.PASS, itemstack);
            } else if (raytraceresult.typeOfHit != RayTraceResult.Type.BLOCK) {
                return new ActionResult<TempItemStack>(EnumActionResult.PASS, itemstack);
            } else {
                Block block = worldIn.getBlockState(raytraceresult.getBlockPos())
                    .getBlock();
                boolean flag1 = block == Blocks.WATER || block == Blocks.FLOWING_WATER;
                EntityBoat entityboat = new EntityBoat(
                    worldIn,
                    raytraceresult.hitVec.x,
                    flag1 ? raytraceresult.hitVec.y - 0.12D : raytraceresult.hitVec.y,
                    raytraceresult.hitVec.z);
                entityboat.setBoatType(this.type);
                entityboat.rotationYaw = playerIn.rotationYaw;

                if (!worldIn.getCollisionBoxes(
                    entityboat,
                    entityboat.getEntityBoundingBox()
                        .grow(-0.1D))
                    .isEmpty()) {
                    return new ActionResult<TempItemStack>(EnumActionResult.FAIL, itemstack);
                } else {
                    if (!worldIn.isRemote) {
                        worldIn.spawnEntity(entityboat);
                    }

                    if (!playerIn.capabilities.isCreativeMode) {
                        itemstack.shrink(1);
                    }

                    playerIn.addStat(StatList.getObjectUseStats(this));
                    return new ActionResult<TempItemStack>(EnumActionResult.SUCCESS, itemstack);
                }
            }
        }
    }
}
