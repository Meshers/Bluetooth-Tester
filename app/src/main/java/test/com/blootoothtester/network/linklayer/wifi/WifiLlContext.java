package test.com.blootoothtester.network.linklayer.wifi;

import java.util.HashSet;

import test.com.blootoothtester.util.Constants;

public class WifiLlContext {
    private final byte mOwnAddress;
    private final byte mSessionId;
    private final Callback mCallback;

    private WifiMessage mLastSentWifiMessage;
    private byte[] mOwnAckArray = getInitialAckArray();

    private byte mNextMsgId = 1;

    // messages whose fromAddres and MsgId combo we are encountering the first time
    private HashSet<AddressAndMsgId> mReceivedMessages = new HashSet<>();
    // messages for which we've been acked
    private HashSet<AddressAndMsgId> mReceivedAck = new HashSet<>();

    protected interface Callback {
        void transmitWifiMessage(WifiMessage message);

        void transmitBtMessage(BtMessage btMessage);

        void onWifiMessageReceived(WifiMessage message);

        void onBtMessageReceived(BtMessage btMessage);

        void onAckedByWifi();
    }

    private class AddressAndMsgId {
        private final byte mMsgId;
        private final byte mFromAddress;

        public AddressAndMsgId(WifiMessage wifiMessage) {
            mMsgId = wifiMessage.getMsgId();
            mFromAddress = wifiMessage.getFromAddress();
        }

        public AddressAndMsgId(BtMessage btMessage) {
            mMsgId = btMessage.getMsgId();
            mFromAddress = btMessage.getToAddress();
        }

        @Override
        public int hashCode() {
            return mMsgId << 8 + mFromAddress;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof AddressAndMsgId)) return false;
            AddressAndMsgId other = (AddressAndMsgId) obj;

            return other.mFromAddress == mFromAddress && other.mMsgId == mMsgId;
        }
    }

    public WifiLlContext(byte ownAddress, byte sessionId, Callback callback) {
        mOwnAddress = ownAddress;
        mSessionId = sessionId;
        mCallback = callback;
    }

    public void sendWifiMessage(byte[] body) {
        byte msgId = mNextMsgId;
        mNextMsgId += 1;
        mOwnAckArray = getInitialAckArray();

        mLastSentWifiMessage =
                WifiMessage.getNewWifiMessage(mSessionId, mOwnAddress, msgId, mOwnAckArray, body);

        sendUpdatedWifiMessage();
    }

    public void receiveBtMessage(BtMessage btMessage) {

        if (mLastSentWifiMessage == null) {
            // we aren't looking for any responses as we haven't sent out a Wifi message
            return;
        }

        AddressAndMsgId identifier = new AddressAndMsgId(btMessage);
        if (identifier.equals(new AddressAndMsgId(mLastSentWifiMessage))) {
            // this response is for our current Wifi message
            // check if we are yet to ack them
            if (!isAcked(btMessage.getFromAddress(), mOwnAckArray)) {
                setAcked(btMessage.getFromAddress(), mOwnAckArray);
                sendUpdatedWifiMessage();
                mCallback.onBtMessageReceived(btMessage);
            }
        }
    }

    private void sendUpdatedWifiMessage() {
        mLastSentWifiMessage.setAckArray(mOwnAckArray);
        mCallback.transmitWifiMessage(mLastSentWifiMessage);
    }

    public void stopWifiTransmission() {
        mLastSentWifiMessage = null;
    }

    public byte getSessionId() {
        return mSessionId;
    }

    public void receiveWifiMessage(WifiMessage wifiMessage) {
        // we check if this a new message, or if we have been newly acked
        // in both of the above cases, we trigger callback, else stay silent

        // if this is the first time we're seeing this message
        AddressAndMsgId identifier = new AddressAndMsgId(wifiMessage);
        if (!mReceivedMessages.contains(identifier)) {
            mReceivedMessages.add(identifier);
            mCallback.onWifiMessageReceived(wifiMessage);
        }
        // check if we have been ACKed yet by this message
        if (!mReceivedAck.contains(identifier)) {
            // we haven't been acked yet - check if we've received one now
            if (isAcked(mOwnAddress, wifiMessage.getAckArray())) {
                mReceivedAck.add(identifier);
                mCallback.onAckedByWifi();
            }
        }
    }

    public void sendBtResponse(byte toId, byte msgId, byte[] responseBody) {
        mCallback.transmitBtMessage(
                BtMessage.getBtMessage(mOwnAddress, toId, mSessionId, msgId, responseBody)
        );
    }

    private static boolean isAcked(byte address, byte[] ackArray) {
        int addressIndex = getIndexForAddressInAckArray(address);
        if (addressIndex >= ackArray.length) {
            throw new IllegalArgumentException("Given address " + address + " does not fit in" +
                    "ack array of length " + ackArray.length);
        }
        return ackArray[addressIndex] != 0;
    }

    private static void setAcked(byte address, byte[] ackArray) {
        int addressIndex = getIndexForAddressInAckArray(address);
        if (addressIndex >= ackArray.length) {
            throw new IllegalArgumentException("Given address " + address + " does not fit in" +
                    "ack array of length " + ackArray.length);
        }
        ackArray[addressIndex] = 1;
    }

    private static int getIndexForAddressInAckArray(byte addrress) {
        return addrress - 1;
    }

    private byte[] getInitialAckArray() {
        byte[] ackArray = new byte[Constants.MAX_USERS];
        for (int i = 0; i < ackArray.length; i++) {
            ackArray[i] = (byte) 0;
        }
        return ackArray;
    }
}
