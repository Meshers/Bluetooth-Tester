package test.com.blootoothtester.network.linklayer;


public class LlMessage {
    private final byte mFromAddress;
    private final byte mToAddress;
    private final int mSequenceId;
    private final byte[] mContents;

    public LlMessage(byte fromAddress, byte toAddress, int sequenceId, byte[] contents) {
        mFromAddress = fromAddress;
        mToAddress = toAddress;
        mSequenceId = sequenceId;
        mContents = contents;
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

    public byte[] getContents() {
        return mContents;
    }
}
