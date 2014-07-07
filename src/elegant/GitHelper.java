package elegant;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by regnarock on 14/06/2014.
 */
public class GitHelper {

    private static Repository repo;
    @Getter @Setter(AccessLevel.PRIVATE)
    private static PathItem    path;

    // Load a git repository
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
        }
        path = new PathItem(new File(repoDirName).toPath());
    }

    // check if a repository is valid or not
    private static boolean isValidRepository(File repoDir) {
        return RepositoryCache.FileKey.isGitRepository(repoDir, FS.DETECTED);
    }
}
