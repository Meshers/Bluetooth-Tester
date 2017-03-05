package test.com.blootoothtester.network.linklayer;

public class AckArrayUtils {
    public static byte getAddressFromIndex(int i) {
        return (byte) (i + 1);
    }

    public static int getIndexFromAddress(byte address) {
        return address - 1;
    }
}
