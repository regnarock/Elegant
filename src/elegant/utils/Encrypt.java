package elegant.utils;

/**
 * Created by regnarock on 24/07/2014.
 */
public class Encrypt {

    /**
     * Returns a simply encrypted string with XOR algorithm.
     *
     * @param   clearData   the data to encrypt
     * @param   key         the key used to encrypt
     * @return              the encrypted key
     */
    public static String createEncryption(String clearData, String key)
    {
        byte[] encrypted = new byte[clearData.length()];
        int i = 0, j = 0;

        if (key.isEmpty())
            return clearData;
        while (i < clearData.length()) {
            if (j >= key.length())
                j = 0;
            encrypted[i] = (byte)(clearData.charAt(i) ^ key.charAt(j));
            j++;
            i++;
        }
        return new String(encrypted);
    }

    /**
     * Returns decrypted string of data originally encrypted with createEncryption
     * @param encryptedData
     * @param key
     * @return String of decrypted data
     */
    public static String createDecrypt(String encryptedData, String key)
    {
        return createEncryption(encryptedData, key);
    }

    /**
     * Validate a password using createEncryption
     *
     * @param   clearData           the data to check
     * @param   encryptedData       the right and already encrypted data used to check
     * @param   key                 the key used to crypt the encrypted data
     * @return                      true if both passwords are same, false otherwise
     */
    public static boolean validateData(String clearData, String encryptedData, String key)
    {
        String encryptedInput = createEncryption(clearData, key);
        return encryptedInput.compareTo(encryptedData) == 0;
    }
}
