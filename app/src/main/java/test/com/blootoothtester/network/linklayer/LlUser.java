package test.com.blootoothtester.network.linklayer;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Represents a user as seen by the Link Layer. Is package local.
 */
class LlUser {
    private final byte mAddr;
    private final ArrayList<LlMessage> mMessages = new ArrayList<>();

    public LlUser(byte addr) {
        mAddr = addr;
    }

    /**
     * @param message the message to be added
     * @return true if the message was added, false if it already existed
     */
    public boolean addMessage(LlMessage message) {
        if (mMessages.size() == 0) {
            mMessages.add(message);
            return true;
        }

        for (int i = mMessages.size() - 1; i >= 0; i--) {
            if (message.getSequenceId() == mMessages.get(i).getSequenceId()) {
                return false;
            } else if (message.getSequenceId() > mMessages.get(i).getSequenceId()) {
                mMessages.add(i + 1, message);
                return true;
            }
        }

        mMessages.add(0, message);
        return true;
    }

    public int getMessageCount() {
        return mMessages.size();
    }

    public byte getAddr() {
        return mAddr;
    }

    public List<LlMessage> getMessages() {
        return new ArrayList<>(mMessages);
    }

    public LlMessage getMessageWithSequenceId(byte sequenceId) {
        for (LlMessage message : mMessages) {
            if (message.getSequenceId() == sequenceId) {
                return message;
            }
        }
        throw new NoSuchElementException("Message with sequence ID " + sequenceId
                + " not found!");
    }

    public byte getLastConsecutiveSeqId() {
        byte lastSeqId = LlContext.DEFAULT_SEQ_ID;

        for (LlMessage message : mMessages) {
            if (message.getSequenceId() == lastSeqId + 1) {
                lastSeqId = message.getSequenceId();
            } else {
                break;
            }
        }

        return lastSeqId;
    }
}
