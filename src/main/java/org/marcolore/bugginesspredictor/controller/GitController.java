package org.marcolore.bugginesspredictor.controller;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.marcolore.bugginesspredictor.model.JavaClass;
import org.marcolore.bugginesspredictor.model.Release;
import org.marcolore.bugginesspredictor.utility.ReleaseUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.marcolore.bugginesspredictor.utility.ReleaseUtility.checkIfNewClassTouched;

public class GitController {
    File projectFile;
    List<RevCommit> commitList = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(GitController.class);

    public GitController(String path) {
        this.projectFile = new File(path);
    }

    public List<RevCommit> GetCommits(List<Release> releaseList) throws GitAPIException {
        ArrayList<RevCommit> commitList = new ArrayList<>();

        try (Git git = Git.open(this.projectFile)) {
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
            this.commitList.addAll(commitList);
        } catch(IOException err){
            logger.error("Error opening File ", err);
        }

        return commitList;
    }

    public List<JavaClass> retrieveClasses(List<Release> releaseList) throws IOException {
        List<JavaClass> javaClassList = new ArrayList<>();
        //Now we take the class from the commits of Releases
        try (Git git = Git.open(this.projectFile)) {
            for (Release release : releaseList) {
                List<String> javaClassRelease = new ArrayList<>();
                List<RevCommit> releaseCommits = release.getCommitList();
                for (RevCommit commit : releaseCommits) {
                    ObjectId idTree = commit.getTree(); //References to this commit tree
                    Repository repository = git.getRepository();
                    TreeWalk treeWalk = new TreeWalk(repository);
                    treeWalk.reset(idTree); //I want to explore this tree
                    treeWalk.setRecursive(true); //Go in the subdirectory
                    while (treeWalk.next()) {
                        JavaClass file = loadJavaClassFromTreeWalk(treeWalk, release, repository, javaClassRelease);
                        javaClassList.add(file);
                        release.addJavaClass(file);
                    }
                }
            }
        }
        //If a new release doesn't have new classes, we copy the last release classes in this
        checkIfNewClassTouched(releaseList);
        //Now we want to associate Java classes with the correct commits
        associateCommitsToClass(javaClassList, releaseList);
        return javaClassList;
    }

    private JavaClass loadJavaClassFromTreeWalk(TreeWalk treeWalk, Release release, Repository repository, List<String> javaClassRelease) throws IOException {
        String filename = treeWalk.getPathString();

        if (filename.endsWith(".java") && !filename.contains("/test/") && !javaClassRelease.contains(filename)) { //Excluding file not .java and the test classes
            javaClassRelease.add(filename);
            ObjectId objectId = treeWalk.getObjectId(0);
            ObjectLoader loader = repository.open(objectId);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            loader.copyTo(output);
            String fileContent = output.toString();

            return new JavaClass(filename, release, fileContent);
            }
        return null;
    }

    public void associateCommitsToClass(List<JavaClass> javaClassList, List<Release> releaseList){
        for(RevCommit commit : commitList){
            Release commitRelease = findReleaseFromCommit(commit, releaseList);
            List<String> touchedJavaClass = findTouchedClassFromCommit(commitRelease);
        }
    }

    private List<String> findTouchedClassFromCommit(Release commitRelease) {
        //Implement it
        return null;
    }

    public Release findReleaseFromCommit(RevCommit extractedCommit, List<Release> releaseList) {
        LocalDateTime commitDate = extractedCommit.getCommitterIdent().getWhen()
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        Release previousRelease = null;

        for (Release release : releaseList) {
            LocalDateTime releaseDate = release.getReleaseDate();
            if (commitDate.isBefore(releaseDate) || commitDate.isEqual(releaseDate)) {
                return release;
            }
            previousRelease = release;
        }
        return previousRelease;
    }

}

