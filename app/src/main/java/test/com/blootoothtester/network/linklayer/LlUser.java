package test.com.blootoothtester.network.linklayer;

import android.os.Message;

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

        public void addMessage(LlMessage message) {
            mMessages.add(message);
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

        public LlMessage getMessageWithSequenceId(int sequenceId) {
            for(LlMessage message: mMessages) {
                if (message.getSequenceId() == sequenceId) {
                    return message;
                }
            }
            throw new NoSuchElementException("Message with sequence ID " + sequenceId
                    + " not found!");
        }
}
