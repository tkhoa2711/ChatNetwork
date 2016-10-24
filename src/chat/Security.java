package chat;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * This class provides utility functions related to security.
 *
 * @author Khoa Le
 * @version 1.0
 */
public class Security {

    private static final String ENCRYPTION_ALGO = "DESede";
    private static final String CIPHER_ALGO_PADDING = "PKCS5Padding";
    private static final String CIPHER_ALGO_MODE = "CBC";
    private static final String CIPHER_ALGO = String.join("/",
            Arrays.asList(ENCRYPTION_ALGO, CIPHER_ALGO_MODE, CIPHER_ALGO_PADDING));

    public static final String SECRET_KEY_FILE = "secret.key";

    // NOTE:
    //  IV parameter is needed when using block-chaining mode like CBC
    //  see http://stackoverflow.com/questions/6669181/why-does-my-aes-encryption-throws-an-invalidkeyexception
    //
    //  For triple-DES (DESede), the length of IV is 8 bytes.
    private static final byte[] IV = new byte[8];
    static {
        new SecureRandom().nextBytes(IV);
    }

    private static boolean ENCRYPTION = true;

    /**
     * Check if encryption is enabled or not.
     *
     * @return  true if encryption is enabled
     */
    public static boolean isEncryptionEnabled() {
        return ENCRYPTION;
    }

    /**
     * Toggle the encryption mode of the application.
     */
    public static void toggleEncryption() {
        ENCRYPTION = !ENCRYPTION;
    }

    /**
     * Get the initialization vector (IV) parameter
     * @return  the IV parameter spec
     */
    public static IvParameterSpec getIV() {
        return new IvParameterSpec(IV);
    }

    /**
     * Encrypt a series of raw byte data.
     * The IV is prepended to the cipher text so that receiver can use it to perform decryption.
     *
     * @param data          data to be encrypted in raw bytes
     * @return              encrypted data
     * @throws Exception    if an error occurs during encryption
     */
    public static byte[] encrypt(byte[] data) throws Exception {
        SecretKey key = readSecretKey();
        Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
        if (isIVRequired(CIPHER_ALGO_MODE))
            cipher.init(Cipher.ENCRYPT_MODE, key, Security.getIV());
        else
            cipher.init(Cipher.ENCRYPT_MODE, key);
        return combine(IV, cipher.doFinal(data));
    }

    /**
     * Decrypt a series of raw byte data.
     *
     * @param data          data to be decrypted in raw bytes
     * @return              encrypted data
     * @throws Exception    if an error occurs during decryption
     */
    public static byte[] decrypt(byte[] data) throws Exception {
        // extract IV and the cipher text from the encrypted data
        byte[] iv = new byte[IV.length];
        byte[] ciphertext = new byte[data.length - IV.length];
        System.arraycopy(data, 0, iv, 0, IV.length);
        System.arraycopy(data, IV.length, ciphertext, 0, ciphertext.length);

        // decrypt the cipher text using existing secret key
        SecretKey key = readSecretKey();
        Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
        if (isIVRequired(CIPHER_ALGO_MODE))
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        else
            cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(ciphertext);
    }

    /**
     * Decrypt a file using the save secret key.
     *
     * @param filename      name of file to be decrypted
     * @throws Exception    if an error occurs while decrypting the file
     */
    public static void decryptFile(String filename) throws Exception {
        // read in file content
        byte[] data = new byte[(int) new File(filename).length()];
        DataInputStream dis = new DataInputStream(new FileInputStream(filename));
        dis.readFully(data);
        dis.close();

        // decrypt the data and write it back to file
        byte[] decryptedData = decrypt(data);
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(filename));
        dos.write(decryptedData);
        dos.close();
    }

    /**
     * Generate a secret key for encryption/decryption and save it to file.
     *
     * @throws IOException              if the process of writing to disk fails
     * @throws NoSuchAlgorithmException if the algorithm name is wrong
     */
    public static void generateSecretKey() throws IOException, NoSuchAlgorithmException {
        KeyGenerator keygen = KeyGenerator.getInstance(ENCRYPTION_ALGO);
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SECRET_KEY_FILE));
        out.writeObject(keygen.generateKey());
        out.close();
    }

    /**
     * Read a secret key from file.
     *
     * @return                          the saved secret key
     * @throws IOException              if there is error while reading the file
     * @throws ClassNotFoundException   if the file does not contains object of the specified class
     */
    private static SecretKey readSecretKey() throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SECRET_KEY_FILE));
        SecretKey key = (SecretKey) ois.readObject();
        ois.close();
        return key;
    }

    /**
     * Check whether an initialization vector (IV) is required for this cipher mode of operation.
     *
     * @param cipherMode    the mode of operation
     * @return              whether this is a block mode
     */
    private static boolean isIVRequired(String cipherMode) {
        return cipherMode.contains("CBC");
    }

    /**
     * Combine two byte arrays together into one array.
     *
     * @see "https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/ArrayUtils.html"
     *
     * @param array1    the 1st byte array
     * @param array2    the 2nd byte array
     * @return          the combined result byte array
     */
    private static byte[] combine(byte[] array1, byte[] array2) {
        if (array1 == null)
            return array2.clone();
        else if (array2 == null)
            return array1.clone();

        final byte[] result = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }
}
