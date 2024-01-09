package shordinger.ModWrapper.migration.wrapper.minecraft.block.state;

import javax.annotation.Nullable;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.google.common.base.Predicate;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class BlockWorldState {

    private final World world;
    private final BlockPos pos;
    private final boolean forceLoad;
    private IWrapperBlockState state;
    private TileEntity tileEntity;
    private boolean tileEntityInitialized;

    public BlockWorldState(World worldIn, BlockPos posIn, boolean forceLoadIn) {
        this.world = worldIn;
        this.pos = posIn;
        this.forceLoad = forceLoadIn;
    }

    /**
     * Gets the block state as currently held, or (if it has not gotten it from the world) loads it from the world.
     * This will only look up the state from the world if {@link #forceLoad} is true or the block position is loaded.
     */
    public IWrapperBlockState getBlockState() {
        if (this.state == null && (this.forceLoad || this.world.isBlockLoaded(this.pos))) {
            this.state = this.world.getBlockState(this.pos);
        }

        return this.state;
    }

    /**
     * Gets the tile entity as currently held, or (if it has not gotten it from the world) loads it from the world.
     */
    @Nullable
    public TileEntity getTileEntity() {
        if (this.tileEntity == null && !this.tileEntityInitialized) {
            this.tileEntity = this.world.getTileEntity(this.pos);
            this.tileEntityInitialized = true;
        }

        return this.tileEntity;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    /**
     * Creates a new {@link Predicate} that will match when the given {@link IWrapperBlockState} predicate matches.
     */
    public static Predicate<BlockWorldState> hasState(final Predicate<IWrapperBlockState> predicatesIn) {
        return new Predicate<BlockWorldState>() {

            public boolean apply(@Nullable BlockWorldState p_apply_1_) {
                return p_apply_1_ != null && predicatesIn.apply(p_apply_1_.getBlockState());
            }
        };
    }
}
