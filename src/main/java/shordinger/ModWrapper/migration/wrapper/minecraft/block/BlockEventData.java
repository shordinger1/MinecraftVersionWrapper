package shordinger.ModWrapper.migration.wrapper.minecraft.block;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;

public class BlockEventData {

    private final BlockPos position;
    private final WrapperBlock wrapperBlockType;
    /** Different for each blockID */
    private final int eventID;
    private final int eventParameter;

    public BlockEventData(BlockPos pos, WrapperBlock wrapperBlockType, int eventId, int p_i45756_4_) {
        this.position = pos;
        this.eventID = eventId;
        this.eventParameter = p_i45756_4_;
        this.wrapperBlockType = wrapperBlockType;
    }

    public BlockPos getPosition() {
        return this.position;
    }

    /**
     * Get the Event ID (different for each BlockID)
     */
    public int getEventID() {
        return this.eventID;
    }

    public int getEventParameter() {
        return this.eventParameter;
    }

    public WrapperBlock getBlock() {
        return this.wrapperBlockType;
    }

    public boolean equals(Object p_equals_1_) {
        if (!(p_equals_1_ instanceof BlockEventData)) {
            return false;
        } else {
            BlockEventData blockeventdata = (BlockEventData) p_equals_1_;
            return this.position.equals(blockeventdata.position) && this.eventID == blockeventdata.eventID
                && this.eventParameter == blockeventdata.eventParameter
                && this.wrapperBlockType == blockeventdata.wrapperBlockType;
        }
    }

    public String toString() {
        return "TE(" + this.position + ")," + this.eventID + "," + this.eventParameter + "," + this.wrapperBlockType;
    }
}
