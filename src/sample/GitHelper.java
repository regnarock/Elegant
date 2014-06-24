package sample;


import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by regnarock on 14/06/2014.
 */
public class GitHelper {

    private static Repository repo;

    public static void LoadGit(String repoDirName) {
        try {
            File repoDir = new File(repoDirName + "/.git");

            if (!isValidRepository(repoDir))
                throw new ElephantException("No valid git repository found at : \"" + repoDirName + "\"");
            FileRepositoryBuilder builder = new FileRepositoryBuilder()
                    .setGitDir(repoDir)
                    .readEnvironment()
                    .findGitDir();
            repo = builder.build();
        } catch (IOException e) {
            Logger.getLogger(Elegant.class.getName()).log(Level.SEVERE, null, e);
        } catch (NoWorkTreeException e) {
            Logger.getLogger(Elegant.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private static boolean isValidRepository(File repoDir) {
        return RepositoryCache.FileKey.isGitRepository(repoDir, FS.DETECTED);
    }

    private static boolean hasAtLeastOneReference(Repository repo) {
        for (Ref ref : repo.getAllRefs().values()) {
            if (ref.getObjectId() == null)
                continue;
            return true;
        }
        return false;
    }

    public static boolean isModified(String fileName) {
        return (true);
    }
}
