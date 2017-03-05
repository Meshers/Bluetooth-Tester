package test.com.blootoothtester.network.linklayer;

import java.nio.charset.Charset;

import test.com.blootoothtester.util.Constants;

/**
 * NOTE: This class heavily relies on sizes of fields and number of users. Please modify any time
 * any of them change.
 */
@SuppressWarnings("WeakerAccess") // TODO: Remove once finalised
public class LinkLayerPdu {

    // priority wise - MESSAGE > REPEAT
    private enum Type {
        MESSAGE,
        REPEAT
    }

    private final byte mSessionId;
    private final byte[] mAckArray;
    private final Type mType;

    // applicable to Type Message
    private byte mSequenceId;
    private byte mFromId;
    private byte mToId;
    private byte[] mData;


    // TODO: Allow NACK to be requested from specific phones e.g. phones whose responses you
    // receive a lot, who are also currently not transmitting a message of their own or a response
    // to a NACK
    // JUSTIFICATION: Experimentally discovered that most devices which transmit well also receive
    // well as the root cause is good BT hardware.
    // Also, since BT scanning is active, you seeing it <=> it can see you

    private final static Charset CHARSET = Charset.forName("UTF-8");

    private final static int TOT_SIZE = 248;
    private final static int ADDR_SIZE_BYTES = 1;
    private final static int PDU_PREFIX_BYTES = getPduPrefix().length;
    private final static int PDU_SESSION_ID_BYTES = 1;
    private final static int PDU_TYPE_BYTES = 1;
    private final static int PDU_SEQ_ID_BYTES = 1;
    private final static int PDU_ACK_ARRAY_BYTES = Constants.MAX_USERS; // 1 ACK byte per user

    private final static int PDU_HEADER_BYTES = PDU_PREFIX_BYTES
            + PDU_SESSION_ID_BYTES
            + PDU_TYPE_BYTES
            + PDU_ACK_ARRAY_BYTES
            + PDU_SEQ_ID_BYTES
            + ADDR_SIZE_BYTES * 2;

    private final static int PAYLOAD_MAX_BYTES = TOT_SIZE - PDU_HEADER_BYTES;

    private LinkLayerPdu(byte sessionId, byte[] ackArray, byte sequenceId, byte fromId, byte toId,
                         byte[] data, Type type) {

        mType = type;

        mFromId = fromId;
        mToId = toId;
        mAckArray = ackArray;
        if (mAckArray.length != PDU_ACK_ARRAY_BYTES) {
            throw new IllegalArgumentException("Expected ack array of length " + PDU_ACK_ARRAY_BYTES
                    + " but received length " + mAckArray.length);
        }
        mSessionId = sessionId;
        mSequenceId = sequenceId;

        mData = data;

        if (mData.length > PAYLOAD_MAX_BYTES) {
            throw new IllegalArgumentException("Payload size greater than max (received "
                    + data.length + " max " + PAYLOAD_MAX_BYTES + " bytes)");
        }
    }

    public static LinkLayerPdu getMessagePdu(byte sessionId, byte[] ackArray, byte sequenceId,
                                             byte fromId, byte toId,
                                             byte[] data) {
        return new LinkLayerPdu(sessionId, ackArray, sequenceId, fromId, toId, data,
                Type.MESSAGE);
    }

    public static LinkLayerPdu getRepeatPdu(byte sessionId, byte[] ackArray, LinkLayerPdu repeatPdu) {
        return new LinkLayerPdu(
                sessionId,
                ackArray,
                repeatPdu.getSequenceId(),
                repeatPdu.getFromAddress(),
                repeatPdu.getToAddress(),
                repeatPdu.getData(),
                Type.REPEAT
        );
    }

    public static boolean isValidPdu(String encoded) {
        return encoded != null && isValidPdu(encoded.getBytes(CHARSET));
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

    public String getAsString() {
        return new String(encode(), CHARSET);
    }


    /**
     * adds 1 to ordinal value of type as encoding since 0 truncates BT string
     * @return type encoded into a single byte
     */
    private static byte getTypeEncoded(Type type) {
        return (byte) (type.ordinal() + 1);
    }

    /**
     * subtracts 1 from ordinal value of type and uses it to obtain the Type object,
     * as 1 is added to the ordinal value in encoding
     * @return Type that represents the given byte
     */
    private static Type getTypeDecoded(byte type) {
        return Type.values()[type - 1];
    }

    private byte[] encode() {
        byte[] prefix = getPduPrefix();
        byte[] encoded = new byte[PDU_HEADER_BYTES + mData.length];
        // add prefix
        System.arraycopy(prefix, 0, encoded, 0, prefix.length);
        int nextFieldIndex = prefix.length;
        // add session ID
        encoded[nextFieldIndex] = mSessionId;
        nextFieldIndex += PDU_SESSION_ID_BYTES;
        // add Type
        encoded[nextFieldIndex] = getTypeEncoded(mType);
        nextFieldIndex += PDU_TYPE_BYTES;
        // add ACK array
        System.arraycopy(mAckArray, 0, encoded, nextFieldIndex, mAckArray.length);
        nextFieldIndex += PDU_ACK_ARRAY_BYTES;
        // add Sequence ID for message
        encoded[nextFieldIndex] = mSequenceId;
        nextFieldIndex += PDU_SEQ_ID_BYTES;
        // add fromID
        encoded[nextFieldIndex] = mFromId;
        nextFieldIndex += ADDR_SIZE_BYTES;
        // add toID
        encoded[nextFieldIndex] = mToId;
        nextFieldIndex += ADDR_SIZE_BYTES;
        // add the actual data to send
        System.arraycopy(mData, 0, encoded, nextFieldIndex, mData.length);
        return encoded;
    }

    private static LinkLayerPdu decode(byte[] encoded) {
        byte[] prefix = getPduPrefix();
        int nextFieldIndex = prefix.length;
        // get session ID
        byte sessionId = encoded[nextFieldIndex];
        nextFieldIndex += PDU_SESSION_ID_BYTES;
        // get type
        Type type = getTypeDecoded(encoded[nextFieldIndex]);
        nextFieldIndex += PDU_TYPE_BYTES;
        // get ACK array
        byte[] ackArray = new byte[Constants.MAX_USERS];
        System.arraycopy(encoded, nextFieldIndex, ackArray, 0, ackArray.length);
        nextFieldIndex += PDU_ACK_ARRAY_BYTES;
        // get Sequence ID for message
        byte sequenceId = encoded[nextFieldIndex];
        nextFieldIndex += PDU_SEQ_ID_BYTES;
        // get fromID
        byte fromId = encoded[nextFieldIndex];
        nextFieldIndex += ADDR_SIZE_BYTES;
        // get toID
        byte toId = encoded[nextFieldIndex];
        nextFieldIndex += ADDR_SIZE_BYTES;
        // get the actual data
        byte[] data = new byte[encoded.length - nextFieldIndex];
        System.arraycopy(encoded, nextFieldIndex, data, 0, data.length);

        return new LinkLayerPdu(sessionId, ackArray, sequenceId, fromId, toId, data, type);
    }

    public static LinkLayerPdu from(String encoded) {
        return decode(encoded.getBytes(CHARSET));
    }

    public static byte[] getPduPrefix() {
        return new byte[]{(byte) 21, (byte) 22, (byte) 23};
    }

    public byte[] getData() {
        return mData;
    }

    /**
     * temporary while only link layer is present
     */
    public String getDataAsString() {
        return new String(mData, CHARSET);
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
