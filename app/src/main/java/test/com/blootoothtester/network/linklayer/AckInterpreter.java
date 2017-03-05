package test.com.blootoothtester.network.linklayer;

import java.util.HashMap;
import java.util.HashSet;

import test.com.blootoothtester.util.Constants;

public class AckInterpreter {
    private static class MissingMessage {
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
    }

    // maps a missing message to the addrs of people missing it
    private HashMap<MissingMessage, HashSet<Byte>> mMissingMap = new HashMap<>();
    // maps a misser to the MissingMessages he has missed
    private HashMap<Byte, HashSet<MissingMessage>> mMisserMap = new HashMap<>();

    public void handle(byte[] ownAckArray, byte[] receivedAckArray, byte ackArrayOwnerAddr) {
        if (ownAckArray.length != receivedAckArray.length
                || ownAckArray.length != Constants.MAX_USERS) {
            throw new IllegalArgumentException("arrays have different lengths: "
                    + ownAckArray.length
                    + " and "
                    + receivedAckArray.length);
        }
        // TODO: Once it exceeds 127 fixme
        for (byte i = 0; i < ownAckArray.length; i++) {
            removeIfMissingResolved(ackArrayOwnerAddr,
                    AckArrayUtils.getAddressFromIndex(i),
                    receivedAckArray[i]);
            if (ownAckArray[i] > receivedAckArray[i]) {
                addMissing(ackArrayOwnerAddr, AckArrayUtils.getAddressFromIndex(i),
                        (byte) (receivedAckArray[i] + 1));
            }
        }
    }

    private void removeIfMissingResolved(byte misserAddr, byte missingAddr,
                                         byte currSequenceId) {
        for (MissingMessage missingMessage : mMisserMap.get(misserAddr)) {
            if (missingMessage.mFromId == missingAddr
                    && missingMessage.mSequenceId <= currSequenceId) {
                removeMissing(misserAddr, missingAddr, missingMessage.mSequenceId);
            }
        }
    }

    private void addMissing(byte misserAddr, byte missingAddr, byte missingSequenceId) {
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

    private void removeMissing(byte misserAddr, byte missingAddr, byte missingSequenceId) {
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
        }

        // 2.

        HashSet<MissingMessage> missingMessages = mMisserMap.get(misserAddr);
        missingMessages.remove(missingMessage);
        // no need to remove the key if missingMessages becomes 0 since number of users is limited
        // and stays more or less constant
    }
}
