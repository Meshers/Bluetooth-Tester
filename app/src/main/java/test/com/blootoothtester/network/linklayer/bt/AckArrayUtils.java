package test.com.blootoothtester.network.linklayer.bt;

class AckArrayUtils {
    static byte getAddressFromIndex(int i) {
        return (byte) (i + 1);
    }

    static int getIndexFromAddress(byte address) {
        return address - 1;
    }
}
