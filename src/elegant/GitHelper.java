package elegant;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
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

    private static Git git;

    // Load a git repository
    public static void LoadGit(File repoDir) {
        try {
            git = Git.open(repoDir);
        } catch (IOException e) {
            throw new ElephantException("No valid git repository found at : \"" + repoDir + "\"", e);
        }
    }

    public static Path getPath() {
        return git.getRepository().getWorkTree().toPath();
    }

    public static void Syncronize() {

    }

    // check if a repository is valid or not
    private static boolean isValidRepository(File repoDir) {
        return RepositoryCache.FileKey.isGitRepository(repoDir, FS.DETECTED);
    }
}
