package test.com.blootoothtester.network;


import android.util.Log;

import java.io.UnsupportedEncodingException;

public class LinkLayerPdu {
    private byte mFromId;
    private byte mToId;
    private byte[] mData;

    private final static int PAYLOAD_MAX_BYTES = 200;
    private final static int ADDR_SIZE_BYTES = 1;
    private final static int PDU_PREFIX_BYTES = 3;

    public LinkLayerPdu(byte fromId, byte toId, byte[] data) {
        mFromId = fromId;
        mToId = toId;

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
        for(int i = 0; i < prefix.length; i++) {
            if (encoded[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    public byte[] encode() {
        byte[] prefix = getPduPrefix();
        byte[] encoded = new byte[prefix.length + mData.length + ADDR_SIZE_BYTES * 2];
        System.arraycopy(prefix, 0, encoded, 0, prefix.length);
        encoded[prefix.length] = mFromId;
        encoded[prefix.length + ADDR_SIZE_BYTES] = mToId;
        System.arraycopy(mData, 0, encoded, prefix.length + ADDR_SIZE_BYTES * 2, mData.length);
        return encoded;
    }

    public static byte[] getPduPrefix() {
        return new byte[] {(byte) 21, (byte) 22, (byte) 23};
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

    public byte getFromAddress() {
        return mFromId;
    }

    public byte getToAddress() {
        return mToId;
    }
}
