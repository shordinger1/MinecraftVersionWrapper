package shordinger.ModWrapper.migration.wrapper.minecraft.world;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.AxisAlignedBB;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.MathHelper;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.Vec3d;

public class Explosion {

    /** whether or not the explosion sets fire to blocks around it */
    public final boolean causesFire;
    /** whether or not this explosion spawns smoke particles */
    public final boolean damagesTerrain;
    public final Random random;
    public final WrapperWorld wrapperWorld;
    public final double x;
    public final double y;
    public final double z;
    public final Entity exploder;
    public final float size;
    /** A list of ChunkPositions of blocks affected by this explosion */
    private final List<BlockPos> affectedBlockPositions;
    /** Maps players to the knockback vector applied by the explosion, to send to the client */
    private final Map<EntityPlayer, Vec3d> playerKnockbackMap;
    private final Vec3d position;

    @SideOnly(Side.CLIENT)
    public Explosion(WrapperWorld wrapperWorldIn, Entity entityIn, double x, double y, double z, float size,
        List<BlockPos> affectedPositions) {
        this(wrapperWorldIn, entityIn, x, y, z, size, false, true, affectedPositions);
    }

    @SideOnly(Side.CLIENT)
    public Explosion(WrapperWorld wrapperWorldIn, Entity entityIn, double x, double y, double z, float size,
        boolean causesFire, boolean damagesTerrain, List<BlockPos> affectedPositions) {
        this(wrapperWorldIn, entityIn, x, y, z, size, causesFire, damagesTerrain);
        this.affectedBlockPositions.addAll(affectedPositions);
    }

    public Explosion(WrapperWorld wrapperWorldIn, Entity entityIn, double x, double y, double z, float size,
        boolean causesFire, boolean damagesTerrain) {
        this.random = new Random();
        this.affectedBlockPositions = Lists.<BlockPos>newArrayList();
        this.playerKnockbackMap = Maps.<EntityPlayer, Vec3d>newHashMap();
        this.wrapperWorld = wrapperWorldIn;
        this.exploder = entityIn;
        this.size = size;
        this.x = x;
        this.y = y;
        this.z = z;
        this.causesFire = causesFire;
        this.damagesTerrain = damagesTerrain;
        this.position = new Vec3d(this.x, this.y, this.z);
    }

    /**
     * Does the first part of the explosion (destroy blocks)
     */
    public void doExplosionA() {
        Set<BlockPos> set = Sets.<BlockPos>newHashSet();
        int i = 16;

        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 16; ++l) {
                    if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                        double d0 = (double) ((float) j / 15.0F * 2.0F - 1.0F);
                        double d1 = (double) ((float) k / 15.0F * 2.0F - 1.0F);
                        double d2 = (double) ((float) l / 15.0F * 2.0F - 1.0F);
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                        d0 = d0 / d3;
                        d1 = d1 / d3;
                        d2 = d2 / d3;
                        float f = this.size * (0.7F + this.wrapperWorld.rand.nextFloat() * 0.6F);
                        double d4 = this.x;
                        double d6 = this.y;
                        double d8 = this.z;

                        for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                            BlockPos blockpos = new BlockPos(d4, d6, d8);
                            IWrapperBlockState iblockstate = this.wrapperWorld.getBlockState(blockpos);

                            if (iblockstate.getMaterial() != Material.AIR) {
                                float f2 = this.exploder != null
                                    ? this.exploder
                                        .getExplosionResistance(this, this.wrapperWorld, blockpos, iblockstate)
                                    : iblockstate.getBlock()
                                        .getExplosionResistance(wrapperWorld, blockpos, (Entity) null, this);
                                f -= (f2 + 0.3F) * 0.3F;
                            }

                            if (f > 0.0F && (this.exploder == null || this.exploder
                                .canExplosionDestroyBlock(this, this.wrapperWorld, blockpos, iblockstate, f))) {
                                set.add(blockpos);
                            }

                            d4 += d0 * 0.30000001192092896D;
                            d6 += d1 * 0.30000001192092896D;
                            d8 += d2 * 0.30000001192092896D;
                        }
                    }
                }
            }
        }

        this.affectedBlockPositions.addAll(set);
        float f3 = this.size * 2.0F;
        int k1 = MathHelper.floor(this.x - (double) f3 - 1.0D);
        int l1 = MathHelper.floor(this.x + (double) f3 + 1.0D);
        int i2 = MathHelper.floor(this.y - (double) f3 - 1.0D);
        int i1 = MathHelper.floor(this.y + (double) f3 + 1.0D);
        int j2 = MathHelper.floor(this.z - (double) f3 - 1.0D);
        int j1 = MathHelper.floor(this.z + (double) f3 + 1.0D);
        List<Entity> list = this.wrapperWorld.getEntitiesWithinAABBExcludingEntity(
            this.exploder,
            new AxisAlignedBB((double) k1, (double) i2, (double) j2, (double) l1, (double) i1, (double) j1));
        net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(this.wrapperWorld, this, list, f3);
        Vec3d vec3d = new Vec3d(this.x, this.y, this.z);

        for (int k2 = 0; k2 < list.size(); ++k2) {
            Entity entity = list.get(k2);

            if (!entity.isImmuneToExplosions()) {
                double d12 = entity.getDistance(this.x, this.y, this.z) / (double) f3;

                if (d12 <= 1.0D) {
                    double d5 = entity.posX - this.x;
                    double d7 = entity.posY + (double) entity.getEyeHeight() - this.y;
                    double d9 = entity.posZ - this.z;
                    double d13 = (double) MathHelper.sqrt(d5 * d5 + d7 * d7 + d9 * d9);

                    if (d13 != 0.0D) {
                        d5 = d5 / d13;
                        d7 = d7 / d13;
                        d9 = d9 / d13;
                        double d14 = (double) this.wrapperWorld.getBlockDensity(vec3d, entity.getEntityBoundingBox());
                        double d10 = (1.0D - d12) * d14;
                        entity.attackEntityFrom(
                            DamageSource.causeExplosionDamage(this),
                            (float) ((int) ((d10 * d10 + d10) / 2.0D * 7.0D * (double) f3 + 1.0D)));
                        double d11 = d10;

                        if (entity instanceof EntityLivingBase) {
                            d11 = EnchantmentProtection.getBlastDamageReduction((EntityLivingBase) entity, d10);
                        }

                        entity.motionX += d5 * d11;
                        entity.motionY += d7 * d11;
                        entity.motionZ += d9 * d11;

                        if (entity instanceof EntityPlayer) {
                            EntityPlayer entityplayer = (EntityPlayer) entity;

                            if (!entityplayer.isSpectator()
                                && (!entityplayer.isCreative() || !entityplayer.capabilities.isFlying)) {
                                this.playerKnockbackMap.put(entityplayer, new Vec3d(d5 * d10, d7 * d10, d9 * d10));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Does the second part of the explosion (sound, particles, drop spawn)
     */
    public void doExplosionB(boolean spawnParticles) {
        this.wrapperWorld.playSound(
            (EntityPlayer) null,
            this.x,
            this.y,
            this.z,
            SoundEvents.ENTITY_GENERIC_EXPLODE,
            SoundCategory.BLOCKS,
            4.0F,
            (1.0F + (this.wrapperWorld.rand.nextFloat() - this.wrapperWorld.rand.nextFloat()) * 0.2F) * 0.7F);

        if (this.size >= 2.0F && this.damagesTerrain) {
            this.wrapperWorld.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
        } else {
            this.wrapperWorld
                .spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
        }

        if (this.damagesTerrain) {
            for (BlockPos blockpos : this.affectedBlockPositions) {
                IWrapperBlockState iblockstate = this.wrapperWorld.getBlockState(blockpos);
                Block block = iblockstate.getBlock();

                if (spawnParticles) {
                    double d0 = (double) ((float) blockpos.getX() + this.wrapperWorld.rand.nextFloat());
                    double d1 = (double) ((float) blockpos.getY() + this.wrapperWorld.rand.nextFloat());
                    double d2 = (double) ((float) blockpos.getZ() + this.wrapperWorld.rand.nextFloat());
                    double d3 = d0 - this.x;
                    double d4 = d1 - this.y;
                    double d5 = d2 - this.z;
                    double d6 = (double) MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
                    d3 = d3 / d6;
                    d4 = d4 / d6;
                    d5 = d5 / d6;
                    double d7 = 0.5D / (d6 / (double) this.size + 0.1D);
                    d7 = d7 * (double) (this.wrapperWorld.rand.nextFloat() * this.wrapperWorld.rand.nextFloat() + 0.3F);
                    d3 = d3 * d7;
                    d4 = d4 * d7;
                    d5 = d5 * d7;
                    this.wrapperWorld.spawnParticle(
                        EnumParticleTypes.EXPLOSION_NORMAL,
                        (d0 + this.x) / 2.0D,
                        (d1 + this.y) / 2.0D,
                        (d2 + this.z) / 2.0D,
                        d3,
                        d4,
                        d5);
                    this.wrapperWorld.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, d3, d4, d5);
                }

                if (iblockstate.getMaterial() != Material.AIR) {
                    if (block.canDropFromExplosion(this)) {
                        block.dropBlockAsItemWithChance(
                            this.wrapperWorld,
                            blockpos,
                            this.wrapperWorld.getBlockState(blockpos),
                            1.0F / this.size,
                            0);
                    }

                    block.onBlockExploded(this.wrapperWorld, blockpos, this);
                }
            }
        }

        if (this.causesFire) {
            for (BlockPos blockpos1 : this.affectedBlockPositions) {
                if (this.wrapperWorld.getBlockState(blockpos1)
                    .getMaterial() == Material.AIR && this.wrapperWorld.getBlockState(blockpos1.down())
                        .isFullBlock()
                    && this.random.nextInt(3) == 0) {
                    this.wrapperWorld.setBlockState(blockpos1, Blocks.FIRE.getDefaultState());
                }
            }
        }
    }

    public Map<EntityPlayer, Vec3d> getPlayerKnockbackMap() {
        return this.playerKnockbackMap;
    }

    /**
     * Returns either the entity that placed the explosive block, the entity that caused the explosion or null.
     */
    @Nullable
    public EntityLivingBase getExplosivePlacedBy() {
        if (this.exploder == null) {
            return null;
        } else if (this.exploder instanceof EntityTNTPrimed) {
            return ((EntityTNTPrimed) this.exploder).getTntPlacedBy();
        } else {
            return this.exploder instanceof EntityLivingBase ? (EntityLivingBase) this.exploder : null;
        }
    }

    public void clearAffectedBlockPositions() {
        this.affectedBlockPositions.clear();
    }

    public List<BlockPos> getAffectedBlockPositions() {
        return this.affectedBlockPositions;
    }

    public Vec3d getPosition() {
        return this.position;
    }
}
