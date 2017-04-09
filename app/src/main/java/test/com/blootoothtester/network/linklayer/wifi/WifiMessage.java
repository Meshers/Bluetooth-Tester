package test.com.blootoothtester.network.linklayer.wifi;


import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import test.com.blootoothtester.util.ArrayUtil;
import test.com.blootoothtester.util.Constants;

public class WifiMessage {
    private final byte mFromAddress;
    private static final int SIZE_ADDR = 1;
    private final byte mMsgId;
    private static final int SIZE_MSG_ID = 1;
    private final byte[] mAckArray;
    private static final int SIZE_ACK_ARRAY = Constants.MAX_USERS;
    private final byte[] mBody;

    public final static Charset ENCODE_CHARSET = Charset.forName("UTF-8");

    private final static byte[] HEADER_PREFIX = {(byte) 21, (byte) 20, (byte) 19};

    public WifiMessage(byte fromAddress, byte msgId, byte[] ackArray, byte[] body) {
        mFromAddress = fromAddress;
        mBody = body;
        mAckArray = ackArray;
        mMsgId = msgId;
    }

    public static WifiMessage getNewWifiMessage(byte fromAddress, byte msgId, byte[] ackArray,
                                         byte[] body) {
        return new WifiMessage(fromAddress, msgId, ackArray, body);
    }

    public static boolean isValidWifiMessage(String encoded) {
        byte[] header = encoded.substring(0, HEADER_PREFIX.length).getBytes(ENCODE_CHARSET);

        return Arrays.equals(header, HEADER_PREFIX);
    }

    public String encode() {
        ArrayList<Byte> encoded = new ArrayList<>();

        encoded.addAll(Arrays.asList(ArrayUtil.toByteArray(HEADER_PREFIX)));

        encoded.add(mFromAddress);
        encoded.add(mMsgId);
        encoded.addAll(Arrays.asList(ArrayUtil.toByteArray(mAckArray)));
        encoded.addAll(Arrays.asList(ArrayUtil.toByteArray(mBody)));

        byte[] encodedPrimitive = new byte[encoded.size()];

        for (int i = 0; i < encoded.size(); i++) {
            encodedPrimitive[i] = encoded.get(i);
        }

        return new String(encodedPrimitive);
    }

    public static WifiMessage decode(String msg) {
        byte[] encodedPrimitive = msg.getBytes(ENCODE_CHARSET);
        int nextFieldIndex = HEADER_PREFIX.length;

        byte fromAddress = encodedPrimitive[nextFieldIndex];
        nextFieldIndex += SIZE_ADDR;

        byte msgId = encodedPrimitive[nextFieldIndex];
        nextFieldIndex += SIZE_MSG_ID;

        byte[] ackArray = new byte[SIZE_ACK_ARRAY];
        for (int i = 0; i < SIZE_ACK_ARRAY; i++) {
            ackArray[i] = encodedPrimitive[i + nextFieldIndex];
        }
        nextFieldIndex += SIZE_ACK_ARRAY;

        byte[] body = new byte[encodedPrimitive.length - nextFieldIndex];
        System.arraycopy(encodedPrimitive, nextFieldIndex, body, 0, body.length);

        return new WifiMessage(fromAddress, msgId, ackArray, body);
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

    public boolean equals(Object obj) {
        if (! (obj instanceof WifiMessage)) {
            return false;
        }
        WifiMessage other = (WifiMessage) obj;

        if (other.mFromAddress != mFromAddress || other.mMsgId != mMsgId
                || other.mAckArray.length != mAckArray.length
                || other.mBody.length != mBody.length) {
            return false;
        }


        for(int i = 0; i <mAckArray.length; i++) {
            if (other.mAckArray[i] != mAckArray[i]) {
                return false;
            }
        }

        for(int i = 0; i <mBody.length; i++) {
            if (other.mBody[i] != mBody[i]) {
                return false;
            }
        }

        return true;
    }
}
