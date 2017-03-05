package test.com.blootoothtester.network.linklayer;


import java.nio.charset.Charset;

public class LlMessage {
    private final byte mFromAddress;
    private final byte mToAddress;
    private final byte mSequenceId;
    private final byte[] mData;

    private final static Charset CHARSET = Charset.forName("UTF-8");

    public LlMessage(byte fromAddress, byte toAddress, byte sequenceId, byte[] data) {
        mFromAddress = fromAddress;
        mToAddress = toAddress;
        mSequenceId = sequenceId;
        mData = data;
    }

    public byte getFromAddress() {
        return mFromAddress;
    }

    public byte getToAddress() {
        return mToAddress;
    }

    public byte getSequenceId() {
        return mSequenceId;
    }

    public byte[] getData() {
        return mData;
    }

    public String getDataAsString() {
        return new String(mData, CHARSET);
    }
}
