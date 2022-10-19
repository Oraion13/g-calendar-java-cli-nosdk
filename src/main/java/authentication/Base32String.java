package authentication;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Base32String {
    private static final String SEPARATOR = "-";
    private static final char[] DIGITS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();
    private static final int MASK = DIGITS.length - 1;
    private static final int SHIFT = Integer.numberOfTrailingZeros(DIGITS.length);
    private static final Map<Character, Integer> CHAR_MAP = new HashMap<>();

    static {
        for (int i = 0; i < DIGITS.length; i++) {
            CHAR_MAP.put(DIGITS[i], i);
        }
    }

    public static byte[] decode(String encoded) throws DecodingException {
        // Remove whitespace and separators
        encoded = encoded.trim().replaceAll(SEPARATOR, "").replaceAll(" ", "");

        // Remove padding. Note: the padding is used as hint to determine how many
        // bits to decode from the last incomplete chunk (which is commented out
        // below, so this may have been wrong to start with).
        encoded = encoded.replaceFirst("[=]*$", "");

        // Canonicalize to all upper case
        encoded = encoded.toUpperCase(Locale.US);
        if (encoded.length() == 0) {
            return new byte[0];
        }
        int encodedLength = encoded.length(); // actual input length
        int outLength = encodedLength * SHIFT / 8; // output byte array length ( SHIFT represents the no. of bits used in base32 - 1 (5 - 1 = 4) )
        byte[] result = new byte[outLength];
        int buffer = 0; // used to calculate the actual letter ( buffer size = 8, base32 char bits = 5)
        int next = 0; // array index counter
        int bitsLeft = 0; // bits inside the buffer
        for (char c : encoded.toCharArray()) {
            if (!CHAR_MAP.containsKey(c)) {
                throw new DecodingException("Illegal character: " + c);
            }
            buffer <<= SHIFT; // making space for a base32 char in buffer
            buffer |= CHAR_MAP.get(c) & MASK; // like subnet masking
            bitsLeft += SHIFT; // how many bits left
            if (bitsLeft >= 8) { // if buffer overloaded
                result[next++] = (byte) (buffer >> (bitsLeft - 8)); // take only 8-bit char from buffer and shift the extra bits to right
                bitsLeft -= 8; // reset the buffer for next character
            }
        }
        // We'll ignore leftover bits for now. ( padded... )
        //
        // if (next != outLength || bitsLeft >= SHIFT) {
        //  throw new DecodingException("Bits left: " + bitsLeft);
        // }
        return result;
    }

    public static String encode(byte[] data) {
        int dataLength = data.length;
        if (dataLength == 0) {
            return "";
        }

        // SHIFT is the number of bits per output character, so the length of the
        // output is the length of the input multiplied by 8/SHIFT, rounded up.
        if (dataLength >= (1 << 28)) {
            // The computation below will fail, so don't do it.
            throw new IllegalArgumentException();
        }

        int outputLength = (dataLength * 8 + SHIFT - 1) / SHIFT;
        StringBuilder result = new StringBuilder(outputLength);

        int buffer = data[0];
        int next = 1;
        int bitsLeft = 8;
        while (bitsLeft > 0 || next < dataLength) {
            if (bitsLeft < SHIFT) {
                if (next < dataLength) {
                    buffer <<= 8;
                    buffer |= (data[next++] & 0xff);
                    bitsLeft += 8;
                } else {
                    int pad = SHIFT - bitsLeft;
                    buffer <<= pad;
                    bitsLeft += pad;
                }
            }
            int index = MASK & (buffer >> (bitsLeft - SHIFT));
            bitsLeft -= SHIFT;
            result.append(DIGITS[index]);
        }
        return result.toString();
    }

    /**
     * Exception thrown when decoding fails
     */
    public static class DecodingException extends Exception {
        public DecodingException(String message) {
            super(message);
        }
    }
}
