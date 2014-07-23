package elegant;


import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.RebaseCommand;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.RepositoryState;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by regnarock on 14/06/2014.
 */
public class GitHelper {

    private static Git git;

    // Load a git repository
    public static void loadGit(File repoDir) {
        try {
            git = Git.open(repoDir);
        } catch (IOException e) {
            throw new ElephantException("No valid git repository found at : \"" + repoDir + "\"", e);
        }
        // TODO allow user to select this
        //allowStashRebase(true);
    }

    public static void allowStashRebase(boolean bool) {
        git.getRepository().getConfig().setBoolean(ConfigConstants.CONFIG_REBASE_SECTION, ConfigConstants.CONFIG_KEY_AUTOSTASH, "rebase.autostash", bool);
        try {
            git.getRepository().getConfig().save();
        } catch (IOException e) {
            throw new ElephantException("Could not write on configuration file.", e);
        }
    }

    public static Path getPath() {
        return git.getRepository().getWorkTree().toPath();
    }

    public static void synchronize() {
        PullResult pullResult = pull();
        switch (pullResult.getRebaseResult().getStatus()) {
            case FAILED:
                System.out.println("Synchronize: pull - FAILED");
                throw new ElephantException("Could not rebase remote changes.");
            case STASH_APPLY_CONFLICTS:
                System.out.println("Synchronize: pull - STASH_APPLY_CONFLICTS");
                throw new ElephantException("Conflicts were found when applying stash. Resolve first then synchronize again.");
            case CONFLICTS:
                System.out.println("Synchronize: pull - CONFLICTS");
                throw new ElephantException("Conflicts were found. Resolve first then synchronize again.");
            case STOPPED:
                System.out.println("Synchronize: pull - STOPPED");
                throw new ElephantException("Conflicts were found. Resolve first then synchronize again.");
            case UNCOMMITTED_CHANGES:
                System.out.println("Synchronize: pull - UNCOMMITTED_CHANGES");
                throw new ElephantException("You have uncommited changes. Commit first then synchronize again.");
            case OK:
                System.out.println("Synchronize: pull - OK");
                break;
            case FAST_FORWARD:
                System.out.println("Synchronize: pull - FAST_FORWARD");
                break;
            case UP_TO_DATE:
                //nothing to do
                System.out.println("Synchronize: pull - UP_TO_DATE");
                break;
            case NOTHING_TO_COMMIT:
                System.out.println("Synchronize: pull - NOTHING_TO_COMMIT");
                //can't happen, operation is BEGIN
                break;
            case ABORTED:
                System.out.println("Synchronize: pull - ABORTED");
                //can't happen, operation is BEGIN
                break;
            case INTERACTIVE_PREPARED:
                System.out.println("Synchronize: pull - INTERACTIVE_PREPARED");
                //can't happen here, this is not rebase -i
                break;
            case EDIT:
                System.out.println("Synchronize: pull - EDIT");
                //can't happen here, this is not rebase -i
                break;
        }
        Iterable<PushResult> pushResult = push();
        pushResult.forEach(result -> {
            result.getRemoteUpdates().forEach(remoteRefUpdate -> {
                switch (remoteRefUpdate.getStatus()) {
                    case REJECTED_NODELETE:
                        System.out.println("Synchronize: push - REJECTED_NODELETE");
                        throw new ElephantException("Cannot synchronize: remote directory doesn't support/allow deleting references.");
                    case REJECTED_NONFASTFORWARD:
                        System.out.println("Synchronize: push - REJECTED_NONFASTFORWARD");
                        // shouldn't happen since we just pulled
                        break;
                    case NON_EXISTING:
                        System.out.println("Synchronize: push - NON_EXISTING");
                    case NOT_ATTEMPTED:
                        System.out.println("Synchronize: push - NOT_ATTEMPTED");
                    case AWAITING_REPORT:
                        System.out.println("Synchronize: push - AWAITING_REPORT");
                        // shouldn't happen
                        throw new ElephantException("Cannot synchronize: " + remoteRefUpdate.getMessage());
                    case REJECTED_OTHER_REASON:
                        System.out.println("Synchronize: push - REJECTED_OTHER_REASON");
                        throw new ElephantException("Cannot synchronize: " + remoteRefUpdate.getMessage());
                    case REJECTED_REMOTE_CHANGED:
                        System.out.println("Synchronize: push - REJECTED_REMOTE_CHANGED");
                        throw new ElephantException("Cannot synchronize: remote directory has new changes.");
                    case OK:
                        System.out.println("Synchronize: push - OK");
                    case UP_TO_DATE:
                        System.out.println("Synchronize: push - UP_TO_DATE");
                        // nothing to do
                        break;
                }
            });
        });
    }

    public static boolean isSynchronizable() {
        return git.getRepository().getRepositoryState() == RepositoryState.SAFE;
    }

    public static String getBranch() {
        try {
            return git.getRepository().getBranch();
        } catch (IOException e) {
            throw new ElephantException(e.getMessage(), e);
        }
    }

    private static Iterable<PushResult> push() {
        try {
            return git.push()
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider("regnarock", "|+x;4N$kvH"))
                    .call();
        }/*catch (InvalidRemoteException e) {
        } catch (TransportException e) {
        }*/catch (GitAPIException e) {
            throw new ElephantException("push failed:" + e.getMessage(), e);
        }
    }

    private static PullResult pull() {
        try {
            return git.pull()
                    .setRebase(true)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider("regnarock", "Oblivion6"))
                    .call();
        } /* catch (WrongRepositoryStateException e) {
        } catch (InvalidConfigurationException e) {
        } catch (DetachedHeadException e) {
        } catch (InvalidRemoteException e) {
        } catch (CanceledException e) {
        } catch (RefNotFoundException e) {
        } catch (NoHeadException e) {
        } catch (TransportException e) {
        } */ catch (GitAPIException e) {
            throw new ElephantException(e.getMessage(), e);
        }
    }

    // check if a repository is valid or not
    private static boolean isValidRepository(File repoDir) {
        return RepositoryCache.FileKey.isGitRepository(repoDir, FS.DETECTED);
    }
}
