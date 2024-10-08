package org.marcolore.bugginesspredictor.controller;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.marcolore.bugginesspredictor.model.Release;
import org.marcolore.bugginesspredictor.utility.ReleaseUtility;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class GitController {
    private final String PROJECT_NAME;

    public GitController(String projectName) {
        PROJECT_NAME = projectName;
    }

    public static List<RevCommit> GetCommits(List<Release> releaseList, Path repoPath) throws IOException, GitAPIException {
        ArrayList<RevCommit> commitList = new ArrayList<>();
        try (Git git = Git.open(repoPath.toFile())) {

            Iterable<RevCommit> commitIterable = git.log().all().call(); //Take all the commits
            for (RevCommit commit : commitIterable) {
                commitList.add(commit);

                LocalDateTime commitDate = commit.getCommitterIdent().getWhen().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

                int indexRelease = ReleaseUtility.matchCommitsRelease(commitDate, releaseList);
                for (Release release : releaseList) {
                    if (release.getId().equals(indexRelease)) {
                        release.getCommitList().add(commit);
                    }
                }
            }

            releaseList.removeIf(release -> release.getCommitList().isEmpty());

            int i = 0;
            for (Release release : releaseList) {
                release.setId(++i);
            }
        }
        return commitList;
    }
}
