package test.com.blootoothtester.network.linklayer.wifi;


import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import test.com.blootoothtester.util.ArrayUtil;

public class BtMessage {
    private final byte mSessionId;
    private static final int SIZE_SESSION_ID = 1;
    private final byte mFromAddress;
    private final byte mToAddress;
    private static final int SIZE_ADDRESS = 1;
    private final byte mMsgId;
    private static final int SIZE_MSG_ID = 1;
    private final byte[] mBody;

    public static Charset ENCODE_CHARSET = Charset.forName("UTF-8");

    private BtMessage(byte fromAddress, byte toAddress, byte sessionId, byte msgId, byte[] body) {
        mFromAddress = fromAddress;
        mToAddress = toAddress;
        mMsgId = msgId;
        mBody = body;
        mSessionId = sessionId;
    }

    public static BtMessage getBtMessage(byte fromAddress, byte toAddress, byte sessionId, byte msgId,
                                  byte[] body) {
        return new BtMessage(fromAddress, toAddress, sessionId, msgId, body);
    }

    public static boolean isValid(String encoded, byte sessionId) {
        BtMessage btMessage;
        try {
            btMessage = decode(encoded);
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }

        return btMessage.mSessionId == sessionId;
    }

    public String encode() {
        ArrayList<Byte> encoded = new ArrayList<>();

        encoded.add(mSessionId);
        encoded.add(mFromAddress);
        encoded.add(mToAddress);
        encoded.add(mMsgId);

        Byte[] convertedBody = ArrayUtil.toByteArray(mBody);
        encoded.addAll(Arrays.asList(convertedBody));

        return new String(
                ArrayUtil.toPrimitiveByteArray(encoded.toArray(new Byte[encoded.size()])),
                ENCODE_CHARSET
        );

    }

    public static BtMessage decode(String encoded) {
        byte[] encodedPrimitive = encoded.getBytes(ENCODE_CHARSET);
        int nextFieldIndex = 0;

        byte sessionId = encodedPrimitive[nextFieldIndex];
        nextFieldIndex += SIZE_SESSION_ID;

        byte fromAddress = encodedPrimitive[nextFieldIndex];
        nextFieldIndex += SIZE_ADDRESS;

        byte toAddress = encodedPrimitive[nextFieldIndex];
        nextFieldIndex += SIZE_ADDRESS;

        byte msgId = encodedPrimitive[nextFieldIndex];
        nextFieldIndex += SIZE_MSG_ID;

        byte[] body = new byte[encodedPrimitive.length - nextFieldIndex];
        System.arraycopy(encodedPrimitive, nextFieldIndex, body, 0, body.length);

        return new BtMessage(fromAddress, toAddress, sessionId, msgId, body);
    }

    public byte getSessionId() {
        return mSessionId;
    }

    public byte getFromAddress() {
        return mFromAddress;
    }

    public byte getToAddress() {
        return mToAddress;
    }

    public byte getMsgId() {
        return mMsgId;
    }

    public byte[] getBody() {
        return mBody;
    }
}
