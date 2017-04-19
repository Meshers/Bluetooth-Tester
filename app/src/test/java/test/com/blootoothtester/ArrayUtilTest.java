package test.com.blootoothtester;


import org.junit.Test;

import test.com.blootoothtester.util.ArrayUtil;

import static org.junit.Assert.*;

public class ArrayUtilTest {
    @Test
    public void testPack() {
        byte[] prepacked = { (byte) 1, (byte) 1, (byte) 0, (byte) 1};

        byte[] packed = ArrayUtil.pack(prepacked, 1);

        assertArrayEquals(new byte[] {(byte) (((byte) 1101)<<4)}, packed);

        prepacked = new byte[] {(byte) 1, (byte) 1, (byte) 0, (byte) 1};
        assertArrayEquals(new byte[] {17, 1}, ArrayUtil.pack(prepacked, 4));

    }

    @Test
    public void testPackUnpack() {
        byte[] unpacked = {1, 2, 3, 4, 5};

        assertArrayEquals(unpacked, ArrayUtil.unpack(ArrayUtil.pack(unpacked, 4), 4, unpacked.length));
        unpacked = new byte[] {1, 0, 1, 0, 1, 1, 0};

        assertArrayEquals(unpacked, ArrayUtil.unpack(ArrayUtil.pack(unpacked, 1), 1, unpacked.length));
    }
}
