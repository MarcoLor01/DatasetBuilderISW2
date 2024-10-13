package org.marcolore.datasetbuilderisw2.controller;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.marcolore.datasetbuilderisw2.model.JavaClass;
import org.marcolore.datasetbuilderisw2.model.Release;
import org.marcolore.datasetbuilderisw2.utility.ReleaseUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.marcolore.datasetbuilderisw2.utility.ReleaseUtility.checkIfNewClassTouched;

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

        if (isJavaClassFile(filename) && !javaClassRelease.contains(filename)) { //Excluding file not .java and the test classes
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

    public void associateCommitsToClass(List<JavaClass> javaClassList, List<Release> releaseList) throws IOException {
        for (RevCommit commit : commitList) {
            Release commitRelease = findReleaseFromCommit(commit, releaseList); //Release of the commit
            List<String> touchedJavaClassName = findTouchedClassFromCommit(commit); //List of the name of the touched classes by the commit
            for (String touchedClassName : touchedJavaClassName) {
                for (JavaClass classJava : javaClassList) {
                    if (classJava.getRelease().equals(commitRelease)
                            && classJava.getClassName().equals(touchedClassName)
                            && !classJava.getListOfCommit().contains(commit)) {
                        classJava.addCommitToList(commit);
                    }
                }
            }
        }
    }



    public List<String> findTouchedClassFromCommit(RevCommit commit) throws IOException {
        List<String> touchedClassesNames = new ArrayList<>();

        try (Git git = Git.open(this.projectFile);
             Repository repository = git.getRepository();
             ObjectReader reader = repository.newObjectReader();
             DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {

            if (commit.getParentCount() == 0) {
                return touchedClassesNames;
            }

            RevCommit commitParent = commit.getParent(0);
            diffFormatter.setRepository(repository);
            diffFormatter.setPathFilter(PathSuffixFilter.create(".java"));

            CanonicalTreeParser newTreeIter = prepareTreeParser(reader, commit);
            CanonicalTreeParser oldTreeIter = prepareTreeParser(reader, commitParent);

            List<DiffEntry> entries = diffFormatter.scan(oldTreeIter, newTreeIter);

            for (DiffEntry entry : entries) {
                if (isJavaClassFile(entry.getNewPath())) {
                    touchedClassesNames.add(entry.getNewPath());
                }
            }

        } catch (IOException e) {
            throw new IOException("Error during the opening of repository", e);
        }

        return touchedClassesNames;
    }

    private boolean isJavaClassFile(String filePath) {
        return filePath.endsWith(".java") && !filePath.contains("/test/");
    }

    private CanonicalTreeParser prepareTreeParser(ObjectReader reader, RevCommit commit) throws IOException {
        CanonicalTreeParser treeParser = new CanonicalTreeParser();
        ObjectId treeId = commit.getTree(); // Ottiene l'ID dell'albero associato al commit
        treeParser.reset(reader, treeId);   // Resetta il parser con l'albero corrispondente
        return treeParser;
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

