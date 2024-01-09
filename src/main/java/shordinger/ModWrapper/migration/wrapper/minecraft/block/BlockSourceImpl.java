package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import net.minecraft.block.state.IWrapperBlockState;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class BlockSourceImpl implements IBlockSource {

    private final World world;
    private final BlockPos pos;

    public BlockSourceImpl(World worldIn, BlockPos posIn) {
        this.world = worldIn;
        this.pos = posIn;
    }

    public World getWorld() {
        return this.world;
    }

    public double getX() {
        return (double) this.pos.getX() + 0.5D;
    }

    public double getY() {
        return (double) this.pos.getY() + 0.5D;
    }

    public double getZ() {
        return (double) this.pos.getZ() + 0.5D;
    }

    public BlockPos getBlockPos() {
        return this.pos;
    }

    /**
     * Gets the block state of this position and returns it.
     * 
     * @return Block state in this position
     */
    public IWrapperBlockState getBlockState() {
        return this.world.getBlockState(this.pos);
    }

    public <T extends TileEntity> T getBlockTileEntity() {
        return (T) this.world.getTileEntity(this.pos);
    }
}
