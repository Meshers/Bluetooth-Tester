package test.com.blootoothtester.network.linklayer;

import android.annotation.SuppressLint;
import android.os.Message;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import test.com.blootoothtester.util.Constants;

/**
 * This class contains information about the current context in which the LinkLayer is running such
 * as messages received so far, session ID, etc.
 */
public class LlContext {

    /**
     * Represents a user as seen by the Link Layer
     */
    public static class LlUser {
        private final byte mId;
        private final ArrayList<LlMessage> mMessages = new ArrayList<>();

        public LlUser(byte id) {
            mId = id;
        }

        public void addMessage(LlMessage message) {
            mMessages.add(message);
        }

        public int getMessageCount() {
            return mMessages.size();
        }

        public List<LlMessage> getMessages() {
            return new ArrayList<>(mMessages);
        }
    }

    private final byte mSessionId;
    private final byte mOwnAddr;
    private final byte[] mAckArray;
    @SuppressLint("UseSparseArrays")
    private final HashMap<Byte, LlUser> mUserMap = new HashMap<>();

    public LlContext(byte sessionId, int maxUsers, byte ownAddr) {
        mSessionId = sessionId;
        mOwnAddr = ownAddr;
        mAckArray = new byte[maxUsers];
    }

    public void receivePdu(LinkLayerPdu pdu) {
        addPdu(pdu);

        if (pdu.getToAddress() == Constants.PDU_BROADCAST_ADDR ||
                pdu.getToAddress() == mOwnAddr) {
            // send message to upper layer
        }
    }

    public void addPdu(LinkLayerPdu pdu) {
        byte fromAddress = pdu.getFromAddress();
        byte toAddress = pdu.getToAddress();

        if (!mUserMap.containsKey(fromAddress)) {
            mUserMap.put(fromAddress, new LlUser(fromAddress));
        }

        LlUser sendingUser = mUserMap.get(fromAddress);

        sendingUser.addMessage(new LlMessage(fromAddress, toAddress, pdu.getSequenceId(),
                pdu.getData()));
    }

    public int getAckValueFor(byte fromAddress) {
        return getUserFor(fromAddress).getMessageCount() + 1;
    }

    public LlUser getUserFor(byte fromAddress) {
        if (!mUserMap.containsKey(fromAddress)) {
            mUserMap.put(fromAddress, new LlUser(fromAddress));
        }

        return mUserMap.get(fromAddress);
    }

    public LinkLayerPdu getPduToSend(byte toAddr, byte[] data) {

        LinkLayerPdu pdu = new LinkLayerPdu(
                mSessionId,
                mAckArray,
                (byte) (getAckValueFor(mOwnAddr) + 1),
                mOwnAddr,
                toAddr,
                data);

        addPdu(pdu);

        return pdu;
    }
}
