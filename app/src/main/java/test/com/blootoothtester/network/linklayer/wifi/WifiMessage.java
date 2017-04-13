package test.com.blootoothtester.network.linklayer.wifi;


import android.util.Base64;
import android.util.Log;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import test.com.blootoothtester.util.ArrayUtil;
import test.com.blootoothtester.util.Constants;

public class WifiMessage {
    private final byte mSessionId;
    private static final int SIZE_SESSION_ID = 1;
    private final byte mFromAddress;
    private static final int SIZE_ADDR = 1;
    private final byte mMsgId;
    private static final int SIZE_MSG_ID = 1;
    private byte[] mAckArray;
    private static final int ACK_ARRAY_BITS_PER_USER = 1;
    private static final int SIZE_ACK_ARRAY = Constants.MAX_USERS;
    private static final int SIZE_ENCODED_ACK_ARRAY = ArrayUtil.getSizeIfPacked(SIZE_ACK_ARRAY,
            ACK_ARRAY_BITS_PER_USER);
    private final byte[] mBody;

    public final static Charset ENCODE_CHARSET = Charset.forName("UTF-8");

    private final static byte[] HEADER_PREFIX = {(byte) 21};

    public final static int BASE64_FLAGS = Base64.NO_WRAP | Base64.NO_PADDING;

    public WifiMessage(byte sessionId, byte fromAddress, byte msgId, byte[] ackArray, byte[] body) {
        mFromAddress = fromAddress;
        mBody = body;
        mAckArray = ackArray;
        mMsgId = msgId;
        mSessionId = sessionId;
    }

    public static WifiMessage getNewWifiMessage(byte sessionId, byte fromAddress, byte msgId, byte[] ackArray,
                                                byte[] body) {
        return new WifiMessage(sessionId, fromAddress, msgId, ackArray, body);
    }

    public static boolean isValidWifiMessage(String encoded, byte sessionId) {
        try {
            if (encoded == null) {
                return false;
            }
            byte[] encodedPrimitive;
            try {
                encodedPrimitive = Base64.decode(encoded, BASE64_FLAGS);
            } catch (IllegalArgumentException e) {
                // not base 64
                return false;
            }
            if (encodedPrimitive.length < HEADER_PREFIX.length) {
                return false;
            }

            byte[] encodedHeader = Arrays.copyOfRange(encodedPrimitive, 0, HEADER_PREFIX.length);
            if (!Arrays.equals(encodedHeader, HEADER_PREFIX)) {
                return false;
            }

            WifiMessage wifiMessage = decode(encoded);
            return wifiMessage.mSessionId == sessionId;
        } catch (Exception e) {
            Log.e("isValidWifiMessage", "Failed while testing for valid wifi message", e);
            return false;
        }
    }

    public String encode() {
        ArrayList<Byte> encoded = new ArrayList<>();

        encoded.addAll(Arrays.asList(ArrayUtil.toByteArray(HEADER_PREFIX)));

        encoded.add(mSessionId);
        encoded.add(mFromAddress);
        encoded.add(mMsgId);
        byte[] packedAckArray = ArrayUtil.pack(mAckArray, 1);
        encoded.addAll(Arrays.asList(ArrayUtil.toByteArray(packedAckArray)));
        encoded.addAll(Arrays.asList(ArrayUtil.toByteArray(mBody)));

        byte[] encodedPrimitive = ArrayUtil.toPrimitiveByteArray(
                encoded.toArray(new Byte[encoded.size()]));

        return Base64.encodeToString(encodedPrimitive, BASE64_FLAGS);
    }

    public static WifiMessage decode(String msg) {
        byte[] encodedPrimitive = Base64.decode(msg, BASE64_FLAGS);
        int nextFieldIndex = HEADER_PREFIX.length;

        byte sessionId = encodedPrimitive[nextFieldIndex];
        nextFieldIndex += SIZE_SESSION_ID;

        byte fromAddress = encodedPrimitive[nextFieldIndex];
        nextFieldIndex += SIZE_ADDR;

        byte msgId = encodedPrimitive[nextFieldIndex];
        nextFieldIndex += SIZE_MSG_ID;

        byte[] packedAckArray = Arrays.copyOfRange(
                encodedPrimitive,
                nextFieldIndex,
                nextFieldIndex + SIZE_ENCODED_ACK_ARRAY);
        byte[] ackArray = ArrayUtil.unpack(packedAckArray, 1, SIZE_ACK_ARRAY);

        nextFieldIndex +=  SIZE_ENCODED_ACK_ARRAY;

        byte[] body = new byte[encodedPrimitive.length - nextFieldIndex];
        System.arraycopy(encodedPrimitive, nextFieldIndex, body, 0, body.length);

        return new WifiMessage(sessionId, fromAddress, msgId, ackArray, body);
    }

    public byte getFromAddress() {
        return mFromAddress;
    }

    public byte getMsgId() {
        return mMsgId;
    }

    public byte[] getAckArray() {
        return mAckArray;
    }

    public byte[] getBody() {
        return mBody;
    }

    protected void setAckArray(byte[] ackArray) {
        mAckArray = ackArray;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof WifiMessage)) {
            return false;
        }
        WifiMessage other = (WifiMessage) obj;

        if (other.mFromAddress != mFromAddress || other.mMsgId != mMsgId
                || other.mAckArray.length != mAckArray.length
                || other.mBody.length != mBody.length) {
            return false;
        }


        for (int i = 0; i < mAckArray.length; i++) {
            if (other.mAckArray[i] != mAckArray[i]) {
                return false;
            }
        }

        for (int i = 0; i < mBody.length; i++) {
            if (other.mBody[i] != mBody[i]) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int ackSum = 0;
        for (byte val : mAckArray) {
            ackSum += val;
        }
        return mFromAddress << 8 + mMsgId << 8 + ackSum;
    }
}
