package test.com.blootoothtester.network.linklayer.wifi;

import test.com.blootoothtester.util.Constants;

public class WifiLlContext {
    private final byte mOwnAddress;
    private byte[] mAckArray = getInitialAckArray();

    private byte mNextMsgId = 1;

    public WifiLlContext(byte ownAddress) {
        mOwnAddress = ownAddress;
    }

    public WifiMessage sendMessage(byte[] body) {
        byte msgId = mNextMsgId;
        mNextMsgId += 1;
        mAckArray = getInitialAckArray();

        return WifiMessage.getNewWifiMessage(mOwnAddress, msgId, mAckArray, body);
    }

    private byte[] getInitialAckArray() {
        byte[] ackArray = new byte[Constants.MAX_USERS];
        for (int i = 0; i < ackArray.length; i++) {
            ackArray[i] = (byte) 1;
        }
        return ackArray;
    }
}
