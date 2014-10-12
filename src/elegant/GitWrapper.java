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

package elegant;


import elegant.exceptions.ElephantException;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Created by regnarock on 14/06/2014.
 */
@Log
public class GitWrapper
{
    private Git git;

    // Load a git repository
    @SneakyThrows
    public GitWrapper(File repoDir) {
        try {
            git = Git.open(repoDir);
        } catch (IOException e) {
            throw new ElephantException("No valid git repository found at : \"" + repoDir + "\"", e);
        }
        // TODO allow user to select this
        //allowStashRebase(true);
    }

    // check if a repository is valid or not
    private static boolean isValidRepository(File repoDir) {
        return RepositoryCache.FileKey.isGitRepository(repoDir, FS.DETECTED);
    }

    public void allowStashRebase(boolean bool) {
        git.getRepository()
           .getConfig()
           .setBoolean(ConfigConstants.CONFIG_REBASE_SECTION, ConfigConstants.CONFIG_KEY_AUTOSTASH, "rebase.autostash",
                       bool
           );
        try {
            git.getRepository().getConfig().save();
        } catch (IOException e) {
            throw new ElephantException("Could not write on configuration file.", e);
        }
    }

    public Path getPath() {
        return git.getRepository().getWorkTree().toPath();
    }

    @SneakyThrows(IOException.class) // ignores exception launched when trying to resolve HEAD
    public void synchronize(String login, String pwd, ProgressMonitor pm)
    {
        //check if repository is empty by checking if HEAD exists
        Optional<ObjectId> optional = Optional.ofNullable(git.getRepository().resolve(Constants.HEAD));
        PullResult pullResult = pull(pm, optional.isPresent());
        //TODO: put the following in a message for the user
        log.info(pullResult.getFetchResult().getMessages());
        if (!optional.isPresent()) return;//nothing more to do if empty repo
        handleSynchronizePullStatus(pullResult.getRebaseResult());
        Iterable<PushResult> pushResult = push(login, pwd, pm);
        pushResult.forEach(
            result -> result.getRemoteUpdates().forEach(remoteRefUpdate -> handleSynchronizePushStatus(remoteRefUpdate)
            )
        );

    }

    private PullResult pull(ProgressMonitor pm, boolean doRebase) {
        try {
            return git.pull().setProgressMonitor(pm).setRebase(doRebase).call();
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

    private void handleSynchronizePullStatus(RebaseResult rebaseResult) {
        switch (rebaseResult.getStatus()) {
            case FAILED:
                log.info("Synchronize: pull - FAILED");
                throw new ElephantException("Could not rebase remote changes.");
            case STASH_APPLY_CONFLICTS:
                log.info("Synchronize: pull - STASH_APPLY_CONFLICTS");
                throw new ElephantException(
                    "Conflicts were found when applying stash. Resolve first then synchronize again."
                );
            case CONFLICTS:
                log.info("Synchronize: pull - CONFLICTS");
                throw new ElephantException("Conflicts were found. Resolve first then synchronize again.");
            case STOPPED:
                log.info("Synchronize: pull - STOPPED");
                throw new ElephantException("Conflicts were found. Resolve first then synchronize again.");
            case UNCOMMITTED_CHANGES:
                log.info("Synchronize: pull - UNCOMMITTED_CHANGES");
                throw new ElephantException("You have uncommited changes. Commit first then synchronize again.");
            case OK:
                log.info("Synchronize: pull - OK");
                break;
            case FAST_FORWARD:
                log.info("Synchronize: pull - FAST_FORWARD");
                break;
            case UP_TO_DATE://nothing to do
                log.info("Synchronize: pull - UP_TO_DATE");
                break;
            case NOTHING_TO_COMMIT://can't happen, operation is BEGIN
                log.info("Synchronize: pull - NOTHING_TO_COMMIT");
                break;
            case ABORTED://can't happen, operation is BEGIN
                log.info("Synchronize: pull - ABORTED");
                break;
            case INTERACTIVE_PREPARED://can't happen here, this is not rebase -i
                log.info("Synchronize: pull - INTERACTIVE_PREPARED");
                break;
            case EDIT://can't happen here, this is not rebase -i
                log.info("Synchronize: pull - EDIT");
                break;
        }
    }

    private Iterable<PushResult> push(String login, String pwd, ProgressMonitor pm) {
        try {
            PushCommand pushCmd = git.push().setProgressMonitor(pm);
            if (login != null && pwd != null)
                pushCmd.setCredentialsProvider(new UsernamePasswordCredentialsProvider(login, pwd));
            return pushCmd.call();
        }/*catch (InvalidRemoteException e) {
        }catch (TransportException e) {
            throw new ElephantException(e.getMessage(), e);
        }*/ catch (GitAPIException e) {
            throw new ElephantException(e.getMessage(), e);
        }
    }

    private void handleSynchronizePushStatus(RemoteRefUpdate remoteRefUpdate) {
        switch (remoteRefUpdate.getStatus()) {
            case REJECTED_NODELETE:
                System.out.println("Synchronize: push - REJECTED_NODELETE");
                throw new ElephantException(
                    "Cannot synchronize: remote directory doesn't support/allow deleting references."
                );
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
                break;
        }
    }

    public boolean isSynchronizable() {
        return git.getRepository().getRepositoryState() == RepositoryState.SAFE;
    }

    public String getBranch() {
        try {
            return git.getRepository().getBranch();
        } catch (IOException e) {
            throw new ElephantException(e.getMessage(), e);
        }
    }
}
