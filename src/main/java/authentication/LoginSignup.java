package authentication;

import org.json.JSONArray;
import org.json.JSONObject;
import utils.ValidIOHandlers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.HashMap;
import java.util.Map;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class LoginSignup {
    public static void generateQRcode(String data, String path, String charset, Map map, int h, int w) throws WriterException, IOException
    {
//the BitMatrix class represents the 2D matrix of bits
//MultiFormatWriter is a factory class that finds the appropriate Writer subclass for the BarcodeFormat requested and encodes the barcode with the supplied contents.
        BitMatrix matrix = new MultiFormatWriter().encode(new String(data.getBytes(charset), charset), BarcodeFormat.QR_CODE, w, h);
        MatrixToImageWriter.writeToFile(matrix, path.substring(path.lastIndexOf('.') + 1), new File(path));
    }

    public static void callGenerateQRcode(String qrString, String qrPath)
    {
        try{
            //Encoding charset to be used
            String charset = "UTF-8";
            Map<EncodeHintType, ErrorCorrectionLevel> hashMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();
//generates QR code with Low level(L) error correction capability
            hashMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            int dimension = 500;
//invoking the user-defined method that creates the QR code
            generateQRcode(qrString, qrPath, charset, hashMap, dimension, dimension);//increase or decrease height and width accodingly
//prints if the QR code is generated
            System.out.println("QR Code created successfully.");
            new ImageJFrame();
        }catch (WriterException | IOException e){
            e.printStackTrace();
        }
    }

    // Path to store user credentials
    private static final String USER_DATA_FILE = new File("bin/main/authentication/user_data.json").getAbsolutePath();

    // write user data in file
    private void setUserData(String userData){
        try {
            Path path = Path.of(USER_DATA_FILE);

            Files.writeString(path, userData);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    // get user data from file
    private JSONObject getUserData() throws IOException {
        if(!(new File(USER_DATA_FILE).exists())) return null;

        String fileContent = Files.readString(Path.of(USER_DATA_FILE));
        if(fileContent == null ||
                fileContent.length() == 0)
            return null;
        return new JSONObject(Files.readString(Path.of(USER_DATA_FILE)));
    }

    // checks if user already exists
    private boolean userExists(String username){
        try{
            // get user data from file
            JSONObject userData = getUserData();

            // empty file
            if(userData == null) return false;

            JSONArray users = userData.getJSONArray("users");
            for(int i = 0; i < users.length(); i++){
                JSONObject user = (JSONObject) users.get(i);
                // check if user already exists
                if(user.getString("username").equals(username)){
                    return true;
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }

    // get key associated with an user
    private String getUserKey(String username){
        try{
            // get user data from file
            JSONObject userData = getUserData();

            // empty file
            if(userData == null) return null;

            JSONArray users = userData.getJSONArray("users");
            for(int i = 0; i < users.length(); i++){
                JSONObject user = (JSONObject) users.get(i);
                // check if user already exists
                if(user.getString("username").equals(username)){
                    return user.getString("key");
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    private void appendUser(JSONObject user){
        try {
            // get user data from file
            JSONObject userData = getUserData();
            JSONObject usersObj = new JSONObject();
            JSONArray users;

            // if an empty file, build a new users array
            if (userData == null){
                users = new JSONArray();
            }else{
                users = userData.getJSONArray("users");
            }

            users.put(user);
            usersObj.put("users", users);

            setUserData(usersObj.toString());

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    // signup
    public void signup(){
        System.out.println("Enter '0' to exit...");
        while (true){
            // get username
            String username = ValidIOHandlers.getString("Enter the username [Username / 0]: ");

            if(username.equals("0")) return;

            // check if username already exists
            if(userExists(username)){
                System.out.println("Username already exists!, Try another name...");
                continue;
            }

            // generate a unique key string
            String key = OTPGenerator.generateKey();

            // build a json object for the user
            JSONObject user = new JSONObject();
            user.put("username", username);
            user.put("key", key);
            System.out.println("Secret Key: " + key);
            callGenerateQRcode(key, "QRCode.png");
            System.out.println("\nScan the QR code to get secret key \nOR \nUse this website to get a QR code: https://www.thonky.com/qrcode/");


            // append the new user
            appendUser(user);

            System.out.println("User created successfully!\n");

            ValidIOHandlers.getString("Enter anything to exit...");
            break;
        }

    }

    // login validator
    public boolean userLogin(){
        try{
            // set the key before .....
            System.out.println("Enter '0' to exit...");
            String username = ValidIOHandlers.getString("Enter the username [Username / 0]: ");

            if(username.equals("0")) return false;

            // get the key if exits
            String key = getUserKey(username);

            if(key == null) {
                System.out.println("User Not Found!");
                return false;
            }

            OTPGenerator otpGenerator = new OTPGenerator();
            // set the key
            otpGenerator.setKey(key);

            // For testing purpose only
//            Thread t1 = new Thread(otpGenerator);
//            t1.start();

            System.out.println("Enter '0' to exit...");
            while(true){
                int otp = ValidIOHandlers.getChoice("Enter the OTP [Number / 0]: ");

                if(otp == 0){
                    otpGenerator.setSTOP_FLAG(true);
                    break;
                }

                // get the otp to check
                if(Integer.parseInt(otpGenerator.generateOTP()) == otp){
                    otpGenerator.setSTOP_FLAG(true);
                    System.out.println("Login successfully!");
                    return true;
                }

                System.out.println("Invalid OTP!...");
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return false;
    }
}
