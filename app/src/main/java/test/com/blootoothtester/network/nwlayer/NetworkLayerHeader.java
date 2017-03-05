package test.com.blootoothtester.network.nwlayer;

import android.support.annotation.Nullable;

import java.util.Locale;

/**
 * AUTHORS: adithyaphilip
 * The format of the headers are: msg_id (1 byte), flags (1 byte), fragment_id (1 byte)
 * unique_msg_id - must be maintained by device, randomly picked, but device must ensure no
 * duplicates for as long as possible
 * flags - 1 if another fragment, 0 if last fragment
 * fragment_id - 0-255 value indicating which fragment it is
 * (max size is 256 * size of link layer PDU)
 */
public class NetworkLayerHeader {
    private byte mMsgId, mFlags, mFragmentId;
    public final static int HEADER_SIZE_BYTES = 3;

    public NetworkLayerHeader(byte msgId, boolean lastFragment, byte fragmentId) {
        mMsgId = msgId;
        mFlags = (byte) (lastFragment ? 0 : 1);
        mFragmentId = fragmentId;
    }

    /**
     * writes the headers into a passed byte array, in place, and returns a reference to the same
     * byte array as a convenience
     * @param packet the headers will be written to the start of this byte array. If null will
     *               create a new array big enough to fit the header, to the smallest next biggest
     *               byte
     * @return the same byte array, with the headers written into it, for the purpose of chaining
     */
    public byte[] writeHeaderIntoPacket(@Nullable byte packet[]) {
        if (packet == null) {
            packet = new byte[HEADER_SIZE_BYTES];
        }
        if (packet.length < HEADER_SIZE_BYTES) {
            throw new IllegalArgumentException(
                    String.format(Locale.ENGLISH,
                            "Passed byte array must be at least %d bytes big!", HEADER_SIZE_BYTES)
            );
        }
        packet[0] = mMsgId;
        packet[1] = mFlags;
        packet[2] = mFragmentId;

        return packet;
    }
}
