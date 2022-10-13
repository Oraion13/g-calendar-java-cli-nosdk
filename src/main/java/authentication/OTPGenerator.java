package authentication;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class OTPGenerator implements Runnable {
    private byte[] key;
    public String getOtp() {
        return otp;
    }

    private String otp;

    public void setSTOP_FLAG(boolean STOP_FLAG) {
        this.STOP_FLAG = STOP_FLAG;
    }

    private boolean STOP_FLAG = false;

    public OTPGenerator(){
    }
    public void setKey(String key){
        // key length = 40
        this.key =  getKeyInBytes(key);
    }

    // prepend with 0's
    private String appendToLength(String value, int len){
        StringBuilder valueBuilder = new StringBuilder(value);
        while(valueBuilder.length() < len){
            valueBuilder.insert(0, "0");
        }
        return valueBuilder.toString();
    }

    // hex string to byte[]
    private byte[] hexStrToBytes(String hexStr){
//        // declare the byte array
//        byte[] byteArr = new byte[hexStr.length() / 2];
//
//        for (int i = 0; i < byteArr.length; i++) {
//            int index = i * 2;
//            int strToInt = Integer.parseInt(hexStr.substring(index, index + 2), 16);
//            byteArr[i] = (byte) strToInt;
//        }
//
//        return byteArr;

        byte[] bArray = new BigInteger("10" + hexStr, 16).toByteArray();

        // Copy all the REAL bytes, not the "first"
        byte[] ret = new byte[bArray.length - 1];
        System.arraycopy(bArray, 1, ret, 0, ret.length);
        return ret;
    }

    // Get the current time stamp in hex to calculate TOTP
    private byte[] getTimeStampInBytes(){
        long T0 = 0; // network time delay
        long currentTimeInMilli = System.currentTimeMillis() / 1000L; // get unix epoch milli
        long epochTimeStamp = (currentTimeInMilli - T0) / 30; // calculate the time stamp ( 30s interval )

//        LocalDateTime date =
//                LocalDateTime.ofInstant(Instant.ofEpochMilli(currentTimeInMilli), ZoneId.systemDefault());
//        System.out.println("Date: " + date.getDayOfMonth() + "-" + date.getMonth() + "-" + date.getYear());
//        System.out.println("Time: " + date.getHour() + ":" + date.getMinute() + ":" + date.getSecond());

        return hexStrToBytes(appendToLength(Long.toHexString(epochTimeStamp), 16));
    }

    // generate random numbers in range
    public static int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    // generate a key of length 40 ( Numbers )
    public static String generateKey(){
        StringBuilder key = new StringBuilder();

        for(int i = 0; i < 40; i++){
            key.append(getRandomNumber(0, 10));
        }

        return key.toString();
    }

    // convert the key string to byte array
    private byte[] getKeyInBytes(String key){
        return hexStrToBytes(key);
    }

    // hash function ( HMAC-SHA1 )
    private byte[] generateHash(){
        try {
            Mac hmac;
            hmac = Mac.getInstance("HmacSHA1");
            SecretKeySpec macKey =
                    new SecretKeySpec(key, "RAW");
            hmac.init(macKey);
            return hmac.doFinal(getTimeStampInBytes());
        }catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateOTP(){
        byte[] hash = generateHash();

        // get the last hex value in hash
        int offset = hash[hash.length - 1] & 0xf;

        // last hex value decides what 4 bytes should be taken
        int binary = ((hash[offset] & 0x7f) << 24) | // MSB
                        ((hash[offset + 1] & 0xff) << 16) |
                        ((hash[offset + 2] & 0xff) << 8) |
                        (hash[offset + 3] & 0xff); // LSB

        int otp = binary % 1000000; // 6 - digit value is sufficient

        return appendToLength(Integer.toString(otp), 6);
    }

    @Override
    public void run() {
        while(!STOP_FLAG){
            try {
                otp = generateOTP();
                System.out.println("System OTP: " + otp);
                LocalDateTime date =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.systemDefault());
                System.out.println("Sleeping " + (30 - date.getSecond() % 30) + "s ...");
                Thread.sleep((30 - date.getSecond() % 30) * 1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
