package test.com.blootoothtester.network.linklayer.filetransfer;

public class TransfererContext {
    interface Callback {
        void updateApName(String name);

//        void
    }

    private final byte mOwnAddress;
    private final Callback mCallback;

    public TransfererContext(byte ownId, Callback callback) {
        mOwnAddress = ownId;
        mCallback = callback;
    }
}
