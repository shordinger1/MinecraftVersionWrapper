package shordinger.ModWrapper.migration.wrapper.minecraft.item;

import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class WrapperItemMinecart extends WrapperItem {

    private static final IBehaviorDispenseItem MINECART_DISPENSER_BEHAVIOR = new BehaviorDefaultDispenseItem() {

        private final BehaviorDefaultDispenseItem behaviourDefaultDispenseItem = new BehaviorDefaultDispenseItem();

        /**
         * Dispense the specified stack, play the dispense sound and spawn particles.
         */
        public TempItemStack dispenseStack(IBlockSource source, TempItemStack stack) {
            EnumFacing enumfacing = (EnumFacing) source.getBlockState()
                .getValue(BlockDispenser.FACING);
            World world = source.getWorld();
            double d0 = source.getX() + (double) enumfacing.getFrontOffsetX() * 1.125D;
            double d1 = Math.floor(source.getY()) + (double) enumfacing.getFrontOffsetY();
            double d2 = source.getZ() + (double) enumfacing.getFrontOffsetZ() * 1.125D;
            BlockPos blockpos = source.getBlockPos()
                .offset(enumfacing);
            IWrapperBlockState iblockstate = world.getBlockState(blockpos);
            BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = iblockstate
                .getBlock() instanceof BlockRailBase
                    ? ((BlockRailBase) iblockstate.getBlock()).getRailDirection(world, blockpos, iblockstate, null)
                    : BlockRailBase.EnumRailDirection.NORTH_SOUTH;
            double d3;

            if (BlockRailBase.isRailBlock(iblockstate)) {
                if (blockrailbase$enumraildirection.isAscending()) {
                    d3 = 0.6D;
                } else {
                    d3 = 0.1D;
                }
            } else {
                if (iblockstate.getMaterial() != Material.AIR
                    || !BlockRailBase.isRailBlock(world.getBlockState(blockpos.down()))) {
                    return this.behaviourDefaultDispenseItem.dispense(source, stack);
                }

                IWrapperBlockState iblockstate1 = world.getBlockState(blockpos.down());
                BlockRailBase.EnumRailDirection blockrailbase$enumraildirection1 = iblockstate1
                    .getBlock() instanceof BlockRailBase
                        ? ((BlockRailBase) iblockstate1.getBlock())
                            .getRailDirection(world, blockpos.down(), iblockstate1, null)
                        : BlockRailBase.EnumRailDirection.NORTH_SOUTH;

                if (enumfacing != EnumFacing.DOWN && blockrailbase$enumraildirection1.isAscending()) {
                    d3 = -0.4D;
                } else {
                    d3 = -0.9D;
                }
            }

            EntityMinecart entityminecart = EntityMinecart
                .create(world, d0, d1 + d3, d2, ((WrapperItemMinecart) stack.getItem()).minecartType);

            if (stack.hasDisplayName()) {
                entityminecart.setCustomNameTag(stack.getDisplayName());
            }

            world.spawnEntity(entityminecart);
            stack.shrink(1);
            return stack;
        }

        /**
         * Play the dispense sound from the specified block.
         */
        protected void playDispenseSound(IBlockSource source) {
            source.getWorld()
                .playEvent(1000, source.getBlockPos(), 0);
        }
    };
    private final EntityMinecart.Type minecartType;

    public WrapperItemMinecart(EntityMinecart.Type typeIn) {
        this.maxStackSize = 1;
        this.minecartType = typeIn;
        this.setCreativeTab(CreativeTabs.TRANSPORTATION);
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, MINECART_DISPENSER_BEHAVIOR);
    }

    /**
     * Called when a Block is right-clicked with this Item
     */
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
        EnumFacing facing, float hitX, float hitY, float hitZ) {
        IWrapperBlockState iblockstate = worldIn.getBlockState(pos);

        if (!BlockRailBase.isRailBlock(iblockstate)) {
            return EnumActionResult.FAIL;
        } else {
            TempItemStack itemstack = player.getHeldItem(hand);

            if (!worldIn.isRemote) {
                BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = iblockstate
                    .getBlock() instanceof BlockRailBase
                        ? ((BlockRailBase) iblockstate.getBlock()).getRailDirection(worldIn, pos, iblockstate, null)
                        : BlockRailBase.EnumRailDirection.NORTH_SOUTH;
                double d0 = 0.0D;

                if (blockrailbase$enumraildirection.isAscending()) {
                    d0 = 0.5D;
                }

                EntityMinecart entityminecart = EntityMinecart.create(
                    worldIn,
                    (double) pos.getX() + 0.5D,
                    (double) pos.getY() + 0.0625D + d0,
                    (double) pos.getZ() + 0.5D,
                    this.minecartType);

                if (itemstack.hasDisplayName()) {
                    entityminecart.setCustomNameTag(itemstack.getDisplayName());
                }

                worldIn.spawnEntity(entityminecart);
            }

            itemstack.shrink(1);
            return EnumActionResult.SUCCESS;
        }
    }
}
