package test.com.blootoothtester.network.linklayer;


public class LlMessage {
    private final byte mFromAddress;
    private final byte mToAddress;
    private final int mSequenceId;
    private final byte[] mData;

    public LlMessage(byte fromAddress, byte toAddress, int sequenceId, byte[] data) {
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

    public int getSequenceId() {
        return mSequenceId;
    }

    public byte[] getData() {
        return mData;
    }
}
