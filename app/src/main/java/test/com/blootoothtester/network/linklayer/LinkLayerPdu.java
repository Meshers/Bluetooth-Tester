package test.com.blootoothtester.network.linklayer;


import android.util.Log;

import java.io.UnsupportedEncodingException;

import test.com.blootoothtester.util.Constants;

/**
 * NOTE: This class heavily relies on sizes of fields and number of users. Please modify any time
 * any of them change.
 */
public class LinkLayerPdu {
    private byte mFromId;
    private byte mToId;
    private byte mSessionId;

    private byte mSequenceId;
    private byte[] mAckArray;
    private byte[] mData;

    private final static int TOT_SIZE = 248;
    private final static int ADDR_SIZE_BYTES = 1;
    private final static int PDU_PREFIX_BYTES = getPduPrefix().length;
    private final static int PDU_SESSION_ID_BYTES = 1;
    private final static int PDU_SEQ_ID_BYTES = 1;
    private final static int PDU_ACK_ARRAY_BYTES = Constants.MAX_USERS; // 1 ACK byte per user

    private final static int PDU_HEADER_BYTES = PDU_PREFIX_BYTES
            + PDU_SESSION_ID_BYTES
            + PDU_ACK_ARRAY_BYTES
            + PDU_SEQ_ID_BYTES
            + ADDR_SIZE_BYTES * 2;

    private final static int PAYLOAD_MAX_BYTES = TOT_SIZE - PDU_HEADER_BYTES;

    public LinkLayerPdu(byte sessionId, byte[] ackArray, byte sequenceId, byte fromId, byte toId,
                        byte[] data) {
        mFromId = fromId;
        mToId = toId;
        mAckArray = ackArray;
        if (mAckArray.length != PDU_ACK_ARRAY_BYTES) {
            throw new IllegalArgumentException("Expected ack array of length " + PDU_ACK_ARRAY_BYTES
            + " but received length " + mAckArray.length);
        }
        mSessionId = sessionId;
        mSequenceId = sequenceId;

        if (data.length > PAYLOAD_MAX_BYTES) {
            throw new IllegalArgumentException("Payload size greater than max (received "
                    + data.length + " max " + PAYLOAD_MAX_BYTES + " bytes)");
        }

        mData = data;
    }

    /**
     * Creates a LinkLayerPdu object from its encoded representation
     *
     * @param encoded an encoded LinkLayerPdu
     */
    public LinkLayerPdu(byte[] encoded) {
        if (!isValidPdu(encoded)) {
            throw new IllegalArgumentException("Invalid PDU format!");
        }
        mFromId = encoded[PDU_PREFIX_BYTES];
        mToId = encoded[PDU_PREFIX_BYTES + ADDR_SIZE_BYTES];
        mData = new byte[encoded.length - PDU_PREFIX_BYTES - ADDR_SIZE_BYTES * 2];
        System.arraycopy(encoded, PDU_PREFIX_BYTES + ADDR_SIZE_BYTES * 2, mData, 0, mData.length);
    }

    public LinkLayerPdu(String encoded) throws UnsupportedEncodingException {
        this(encoded.getBytes("UTF-8"));
    }

    public static boolean isValidPdu(String encoded) {
        try {
            return isValidPdu(encoded.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Log.e("LLPDU", "isValid failed", e);
            return false;
        }
    }

    public static boolean isValidPdu(byte[] encoded) {
        byte[] prefix = getPduPrefix();
        if (encoded.length < prefix.length + 2 * ADDR_SIZE_BYTES) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (encoded[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    public byte[] encode() {
        byte[] prefix = getPduPrefix();
        byte[] encoded = new byte[PDU_HEADER_BYTES + mData.length];
        // add prefix
        System.arraycopy(prefix, 0, encoded, 0, prefix.length);
        int nextFieldIndex = prefix.length;
        // add session ID
        encoded[nextFieldIndex] = mSessionId;
        nextFieldIndex += PDU_SESSION_ID_BYTES;
        // add ACK array
        System.arraycopy(mAckArray, 0, encoded, nextFieldIndex, mAckArray.length);
        nextFieldIndex += PDU_ACK_ARRAY_BYTES;
        // add Sequence ID for message
        encoded[nextFieldIndex] = mToId;
        System.arraycopy(mData, 0, encoded, prefix.length + ADDR_SIZE_BYTES * 2, mData.length);
        return encoded;
    }

    public static byte[] getPduPrefix() {
        return new byte[]{(byte) 21, (byte) 22, (byte) 23};
    }

    public byte[] getData() {
        return mData;
    }

    public String getDataAsString() {
        try {
            return new String(mData, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e("LLPDU", "Failed to decode", e);
            return null;
        }
    }

    public byte getSequenceId() {
        return mSequenceId;
    }

    public byte getFromAddress() {
        return mFromId;
    }

    public byte getToAddress() {
        return mToId;
    }
}
