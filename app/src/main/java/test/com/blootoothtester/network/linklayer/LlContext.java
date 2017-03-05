package test.com.blootoothtester.network.linklayer;

import android.annotation.SuppressLint;

import java.util.HashMap;

import test.com.blootoothtester.util.Constants;

/**
 * This class contains information about the current context in which the LinkLayer is running such
 * as messages received so far, session ID, etc.
 */
@SuppressWarnings("WeakerAccess") // TODO: remove once finalised
public class LlContext {

    public interface Callback {
        void transmitPdu(LinkLayerPdu pdu);
        void sendUpperLayer(LinkLayerPdu pdu);
    }

    private final byte mSessionId;
    private final byte mOwnAddr;
    private final byte[] mAckArray;
    private final Callback mCallback;
    @SuppressLint("UseSparseArrays")
    private final HashMap<Byte, LlUser> mUserMap = new HashMap<>();

    private LinkLayerPdu mCurrentPdu;
    private final AckInterpreter mAckInterpreter = new AckInterpreter();

    public LlContext(byte sessionId, int maxUsers, byte ownAddr, Callback callback) {
        mSessionId = sessionId;
        mOwnAddr = ownAddr;
        mCallback = callback;
        mAckArray = new byte[maxUsers];
        // as 0 will end up truncating it
        for(int i = 0; i < mAckArray.length; i++) {
            mAckArray[i] = 1;
        }
    }

    /**
     * updates the LinkLayer context to reflect the receival of the message
     * @param pdu the pdu discovered
     */
    public void receivePdu(LinkLayerPdu pdu) {
        switch (pdu.getType()) {
            case MESSAGE:
            case REPEAT:
                boolean isNewAcceptedPdu = addPdu(pdu);

                if (isNewAcceptedPdu) {
                    sendUpdatedAckArray();
                }

                if (isNewAcceptedPdu && (pdu.getToAddress() == Constants.PDU_BROADCAST_ADDR ||
                        pdu.getToAddress() == mOwnAddr)) {
                    // send message to upper layer
                    mCallback.sendUpperLayer(pdu);
                }
                break;
        }
    }

    private void sendUpdatedAckArray() {
        mCurrentPdu = LinkLayerPdu.getAckChangedPdu(mCurrentPdu, mAckArray);
        mCallback.transmitPdu(mCurrentPdu);
    }

    /**
     * adds a received PDU to the LLContext if the message is not out of order and not a repeat,
     * else ignores the message
     * Also compares the AckArray with
     * @param pdu the pdu to process
     * @return true if this is a new PDU which has been accepted into the context (not necessarily
     * destined for this device though), false otherwise
     */
    public boolean addPdu(LinkLayerPdu pdu) {
        byte fromAddress = pdu.getFromAddress();
        byte toAddress = pdu.getToAddress();

        LlUser sendingUser = getUserFor(fromAddress);

        byte sequenceId = pdu.getSequenceId();
        if (sequenceId != getAckValueFor(fromAddress) + 1) {
            // this packet is out of order or redundant, ignore it
            // TODO: Allow for storing of out of order packets too, e.g. based on a window
            return false;
        }

        sendingUser.addMessage(new LlMessage(fromAddress, toAddress, pdu.getSequenceId(),
                pdu.getData()));
        // TODO: This is tightly coupled with the way we allocate addresses (starting from 1)
        setInAckArray(sendingUser.getAddr(), sequenceId);
        return true;
    }

    public int getAckValueFor(byte addr) {
        return mAckArray[AckArrayUtils.getIndexFromAddress(addr)];
    }

    public LlUser getUserFor(byte fromAddress) {
        if (!mUserMap.containsKey(fromAddress)) {
            mUserMap.put(fromAddress, new LlUser(fromAddress));
        }

        return mUserMap.get(fromAddress);
    }

    private void setInAckArray(byte addr, byte sequenceId) {
        mAckArray[AckArrayUtils.getIndexFromAddress(addr)] = sequenceId;
    }

    /**
     * This function assumes that the message it generates will be sent immediately
     * and updates its context correspondingly
     * it DOES NOT send the pdu, only returns the pdu to be sent
     * @param toAddr the address of recipient device
     * @param data the data to be encoded and sent
     */
    public void sendPdu(byte toAddr, byte[] data) {
        byte newSequenceId = (byte) (getAckValueFor(mOwnAddr) + 1);

        setInAckArray(mOwnAddr, newSequenceId);
        getUserFor(mOwnAddr).addMessage(
                new LlMessage(mOwnAddr, toAddr, newSequenceId, data)
        );

        LinkLayerPdu pdu = LinkLayerPdu.getMessagePdu(
                mSessionId,
                mAckArray,
                newSequenceId,
                mOwnAddr,
                toAddr,
                data);

        mCurrentPdu = pdu;
        mCallback.transmitPdu(pdu);
    }
}
