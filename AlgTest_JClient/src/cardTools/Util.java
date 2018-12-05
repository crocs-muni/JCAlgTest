package cardTools;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author Vasilios Mavroudis and Petr Svenda
 */
public class Util {

    public static String toHex(byte[] bytes) {
        return toHex(bytes, 0, bytes.length);
    }

    public static String toHex(byte[] bytes, int offset, int len) {
        StringBuilder result = new StringBuilder();

        for (int i = offset; i < offset + len; i++) {
            result.append(String.format("%02X", bytes[i]));
        }

        return result.toString();
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    public static byte[] hexStringToByteArray(String s) {
        String sanitized = s.replace(" ", "");
        byte[] b = new byte[sanitized.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(sanitized.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }    
    
    
    // Utils 
    public static short getShort(byte[] buffer, int offset) {
        return ByteBuffer.wrap(buffer, offset, 2).order(ByteOrder.BIG_ENDIAN).getShort();
    }

    public static short readShort(byte[] data, int offset) {
        return (short) (((data[offset] << 8)) | ((data[offset + 1] & 0xff)));
    }

    public static byte[] shortToByteArray(int s) {
        return new byte[]{(byte) ((s & 0xFF00) >> 8), (byte) (s & 0x00FF)};
    }
    
    
    public static byte[] joinArray(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }

        final byte[] result = new byte[length];

        int offset = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }

        return result;
    }

    public static byte[] trimLeadingZeroes(byte[] array) {
        short startOffset = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] != 0) {
                break;
            } else {
                // still zero
                startOffset++;
            }
        }

        byte[] result = new byte[array.length - startOffset];
        System.arraycopy(array, startOffset, result, 0, array.length - startOffset);
        return result;
    }

    public static byte[] concat(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c = new byte[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    public static byte[] concat(byte[] a, byte[] b, byte[] c) {
        byte[] tmp_conc = concat(a, b);
        return concat(tmp_conc, c);

    }
}
