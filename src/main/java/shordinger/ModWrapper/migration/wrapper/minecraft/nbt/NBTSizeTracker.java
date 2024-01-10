package shordinger.ModWrapper.migration.wrapper.minecraft.nbt;

public class NBTSizeTracker extends net.minecraft.nbt.NBTSizeTracker {

    public static final NBTSizeTracker INFINITE = new NBTSizeTracker(0L) {

        /**
         * Tracks the reading of the given amount of bits(!)
         */
        public void read(long bits) {}
    };


    public NBTSizeTracker(long max) {
        super(max);
    }

    /**
     * Tracks the reading of the given amount of bits(!)
     */
    public void read(long bits) {
        func_152450_a(bits);
    }

    /*
     * UTF8 is not a simple encoding system, each character can be either
     * 1, 2, or 3 bytes. Depending on where it's numerical value falls.
     * We have to count up each character individually to see the true
     * length of the data.
     * Basic concept is that it uses the MSB of each byte as a 'read more' signal.
     * So it has to shift each 7-bit segment.
     * This will accurately count the correct byte length to encode this string, plus the 2 bytes for it's length
     * prefix.
     */
    public static void readUTF(NBTSizeTracker tracker, String data) {
        net.minecraft.nbt.NBTSizeTracker.readUTF(tracker,data);
    }
}
