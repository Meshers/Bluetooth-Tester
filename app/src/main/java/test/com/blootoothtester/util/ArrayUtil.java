package test.com.blootoothtester.util;

public class ArrayUtil {
    public static Byte[] toByteArray(byte[] arr) {
        Byte[] bArr = new Byte[arr.length];

        for (int i = 0; i < arr.length; i++) {
            bArr[i] = arr[i];
        }

        return bArr;
    }
}
