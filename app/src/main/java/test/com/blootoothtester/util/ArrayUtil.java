package test.com.blootoothtester.util;

import java.util.ArrayList;

public class ArrayUtil {
    public static Byte[] toByteArray(byte[] arr) {
        Byte[] bArr = new Byte[arr.length];

        for (int i = 0; i < arr.length; i++) {
            bArr[i] = arr[i];
        }

        return bArr;
    }

    public static byte[] toPrimitiveByteArray(Byte[] arr) {
        byte newArr[] = new byte[arr.length];
        for (int i = 0; i < newArr.length; i++) {
            newArr[i] = arr[i];
        }
        return newArr;
    }

    public static byte[] pack(byte[] origArray, int bits) {
        if (bits >= 8) {
            throw new IllegalArgumentException("bits parameter value must be less than 8. Received " + bits);
        }
        if (8 % bits != 0) {
            throw new IllegalArgumentException("bits parameter must be a divisor of 8");
        }
        final int MAX_ELE_VAL = (int) Math.pow(2, bits) - 1;
        byte packed[] = new byte[(int) Math.ceil(origArray.length * bits / 8.0)];
        int pos = 0;
        int index = 0;
        for (byte ele : origArray) {
            if (ele > MAX_ELE_VAL) {
                throw new IllegalArgumentException("Value " + ele + " in given array larger than max of " + MAX_ELE_VAL);
            }
            packed[index] = (byte) (packed[index] | (byte) (ele << (8 - (pos + bits))));
            pos += bits;
            index += pos / 8;
            pos %= 8;
        }

        return packed;
    }

    public static byte[] unpack(byte[] packedArr, final int bits, final int length) {
        if (bits >= 8) {
            throw new IllegalArgumentException("bits parameter value must be less than 8. Received " + bits);
        }
        if (8 % bits != 0) {
            throw new IllegalArgumentException("bits parameter must be a divisor of 8");
        }
        ArrayList<Byte> unpacked = new ArrayList<>();
        for (byte packed : packedArr) {
            byte mask = (byte) ((int) (Math.pow(2, bits) - 1) & 0xff);
            mask = (byte) ((mask << (8 - bits)) & 0xff);
            for (int i = 0; i < 8 / bits; i++) {
                int rightShift = (8 / bits - i - 1) * bits;
//                System.out.println((packed & 0xff) + " " + (mask & 0xff) + " " + (((packed & 0xff) & (mask & 0xff)) >>> rightShift));
                unpacked.add((byte) (((packed & 0xff) & (mask & 0xff)) >>> rightShift));
                mask = (byte) ((mask & 0xff) >>> bits);
            }
        }

        byte[] result = new byte[length];

        for (int i = 0; i < result.length; i++) {
            result[i] = unpacked.get(i);
        }

        return result;
    }

    public static int getSizeIfPacked(int origSize, int bits) {
        return (int) Math.ceil(origSize * bits / 8);
    }
}
