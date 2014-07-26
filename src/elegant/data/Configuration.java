package elegant.data;

import elegant.utils.PasswordHash;
import lombok.Getter;

import java.util.prefs.Preferences;

/**
 * Created by regnarock on 24/07/2014.
 */
public class Configuration {

    private static String PREF_LOGIN = "userName";
    private static String PREF_PWD = "pwd";
    private static String PREF_MASTER_PWD_HASH = "master_pwd_hash";
    private static String PREF_IS_SAVED = "is_saved";

    private static Preferences prefs = initialize();
    @Getter
    private static String userName;
    @Getter
    private static String password;
    @Getter
    private static String masterPasswordHash;
    @Getter
    private static boolean isSaved;

    public static void reset() {
        Preferences prefs = Preferences.userNodeForPackage(elegant.data.Configuration.class);
        prefs.remove(PREF_LOGIN);
        prefs.remove(PREF_PWD);
        prefs.remove(PREF_MASTER_PWD_HASH);
        prefs.remove(PREF_IS_SAVED);
    }

    public static Preferences initialize() {
        Preferences prefs = Preferences.userNodeForPackage(elegant.data.Configuration.class);
        userName = prefs.get(PREF_LOGIN, "");
        password = prefs.get(PREF_PWD, "");
        masterPasswordHash = prefs.get(PREF_MASTER_PWD_HASH, "");
        isSaved = prefs.getBoolean(PREF_IS_SAVED, false);
        return prefs;
     }

    public static void setMasterPasswordHash (String masterPasswordHash) {
        prefs.put(PREF_MASTER_PWD_HASH, masterPasswordHash);
        Configuration.masterPasswordHash = masterPasswordHash;
    }

    public static void setUserName(String userName) {
        prefs.put(PREF_LOGIN, userName);
        Configuration.userName = userName;
        System.out.println("UserName : " + userName);
    }

    public static void setPassword(String pwd) {
        prefs.put(PREF_PWD, pwd);
        Configuration.password = pwd;
        System.out.println("Pwd : " + pwd);
    }

    public static void setIsSaved(boolean isSaved) {
        prefs.putBoolean(PREF_IS_SAVED, isSaved);
        Configuration.isSaved = isSaved;
    }
}
