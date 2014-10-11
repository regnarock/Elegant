/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Thomas Wilgenbus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
