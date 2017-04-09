package test.com.blootoothtester.network.linklayer.bt;

import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.HashSet;

import test.com.blootoothtester.util.Constants;

class AckInterpreter {
    static class MissingMessage {
        private final byte mFromId;
        private final byte mSequenceId;

        /**
         * @param fromId     the ID of the guy who sent the missing message
         * @param sequenceId the sequenceId of the missing message
         */
        private MissingMessage(byte fromId, byte sequenceId) {
            mFromId = fromId;
            mSequenceId = sequenceId;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MissingMessage)) {
                return false;
            }
            MissingMessage other = (MissingMessage) obj;
            return other.mFromId == mFromId && other.mSequenceId == mSequenceId;
        }

        @Override
        public int hashCode() {
            return mFromId * 1000 + mSequenceId;
        }

        public byte getFromId() {
            return mFromId;
        }

        public byte getSequenceId() {
            return mSequenceId;
        }
    }

    private Notifier mNotifier;

    public AckInterpreter(@Nullable Notifier notifier) {
        mNotifier = notifier;
    }

    interface Notifier {
        void onMissingCleared(byte missingAddr, byte missingSeqId);
        void onNewMissing(byte missingAddr, byte missingSeqId);
    }

    // maps a missing message to the addrs of people missing it
    private HashMap<MissingMessage, HashSet<Byte>> mMissingMap = new HashMap<>();
    // maps a misser to the MissingMessages he has missed
    private HashMap<Byte, HashSet<MissingMessage>> mMisserMap = new HashMap<>();

    public synchronized void handle(byte[] ownAckArray, byte[] receivedAckArray, byte otherArrayOwner) {
        if (ownAckArray.length != receivedAckArray.length
                || ownAckArray.length != Constants.MAX_USERS) {
            throw new IllegalArgumentException("arrays have different lengths: "
                    + ownAckArray.length
                    + " and "
                    + receivedAckArray.length);
        }
        // TODO: Once it exceeds 127 fixme
        for (byte i = 0; i < ownAckArray.length; i++) {
            removeIfMissingResolved(otherArrayOwner,
                    AckArrayUtils.getAddressFromIndex(i),
                    receivedAckArray[i]);
            if (ownAckArray[i] > receivedAckArray[i]) {
                addMissing(otherArrayOwner, AckArrayUtils.getAddressFromIndex(i),
                        (byte) (receivedAckArray[i] + 1));
            }
        }
    }

    public synchronized HashMap<MissingMessage, Integer> getMissingCounter() {
        HashMap<MissingMessage, Integer> counter = new HashMap<>();
        for (MissingMessage message : mMissingMap.keySet()) {
            counter.put(message, mMissingMap.get(message).size());
        }

        return counter;
    }


    // TODO: Add timestamp for each missing and remove old guys. This prevents DoS by someone
    // dropping from the network
    public synchronized void reset() {
        mMissingMap = new HashMap<>();
        mMisserMap = new HashMap<>();
    }

    private synchronized void removeIfMissingResolved(byte misserAddr, byte missingAddr,
                                         byte currSequenceId) {
        if (!mMisserMap.containsKey(misserAddr)) {
            return;
        }

        HashMap<Byte, HashSet<MissingMessage>> tempMap = new HashMap<>();
        for(Byte key: mMisserMap.keySet()) {
            HashSet<MissingMessage> tempSet = new HashSet<>();
            tempSet.addAll(mMisserMap.get(key));
            tempMap.put(key, tempSet);
        }

        for (MissingMessage missingMessage : tempMap.get(misserAddr)) {
            if (missingMessage.mFromId == missingAddr
                    && missingMessage.mSequenceId <= currSequenceId) {
                removeMissing(misserAddr, missingAddr, missingMessage.mSequenceId);
            }
        }
    }

    private synchronized void addMissing(byte misserAddr, byte missingAddr, byte missingSequenceId) {
        MissingMessage missingMessage = new MissingMessage(missingAddr, missingSequenceId);
        // OPS:
        // 1. We add him to map of missing messages to set of people missing them
        // 2. We add the missing message to the set of messages he has missed

        // 1.
        if (!mMissingMap.containsKey(missingMessage)) {
            mMissingMap.put(missingMessage, new HashSet<Byte>());
        }

        HashSet<Byte> misserAddrs = mMissingMap.get(missingMessage);
        misserAddrs.add(misserAddr);

        // 2.
        if (!mMisserMap.containsKey(misserAddr)) {
            mMisserMap.put(misserAddr, new HashSet<MissingMessage>());
        }

        HashSet<MissingMessage> missingMessages = mMisserMap.get(misserAddr);
        missingMessages.add(missingMessage);
    }

    private synchronized void removeMissing(byte misserAddr, byte missingAddr, byte missingSequenceId) {
        MissingMessage missingMessage = new MissingMessage(missingAddr, missingSequenceId);
        // OPS:
        // 1. We remove him from map of missing messages to set of people missing them
        // 2. We remove the missing message from the set of messages he has missed

        // 1.

        HashSet<Byte> misserAddrs = mMissingMap.get(missingMessage);
        misserAddrs.remove(misserAddr);
        // since there can be many missing messages that are resolved in a session
        // we do not want to pollute the Map with useless values
        // also, convenient when extracting missing messages
        if (misserAddrs.size() == 0) {
            mMissingMap.remove(missingMessage);
            if (mNotifier != null) {
                mNotifier.onMissingCleared(missingMessage.getFromId(),
                        missingMessage.getSequenceId());
            }
        }

        // 2.

        HashSet<MissingMessage> missingMessages = mMisserMap.get(misserAddr);
        missingMessages.remove(missingMessage);
        // no need to remove the key if missingMessages becomes 0 since number of users is limited
        // and stays more or less constant
    }
}
