package org.marcolore.datasetbuilderisw2.controller;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
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
import java.util.Collections;
import java.util.List;

import static org.marcolore.datasetbuilderisw2.utility.ReleaseUtility.checkIfNewClassTouched;

public class GitController implements AutoCloseable {
    File projectFile;
    List<RevCommit> commitList = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(GitController.class);
    private final Git git;

    public GitController(String path) throws IOException {
        this.projectFile = new File(path);
        this.git = Git.open(this.projectFile);
    }

    public List<RevCommit> getCommits(List<Release> releaseList) throws GitAPIException {
        ArrayList<RevCommit> listOfCommit = new ArrayList<>();

        try {
            Iterable<RevCommit> commitIterable = git.log().all().call(); //Take all the commits
            for (RevCommit commit : commitIterable) {
                listOfCommit.add(commit);
                LocalDateTime commitDate = commit.getCommitterIdent().getWhen().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(); //Date of Commit

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
            this.commitList.addAll(listOfCommit);
        } catch(IOException e){
            logger.error("Error opening File ", e);
        }

        return commitList;
    }

    public List<JavaClass> retrieveClasses(List<Release> releaseList) throws IOException {
        List<JavaClass> javaClassList = new ArrayList<>();
        //Now we take the class from the commits of Releases
        try {
            for (Release release : releaseList) {

                List<String> javaClassRelease = new ArrayList<>();
                List<RevCommit> releaseCommits = release.getCommitList();

                for (RevCommit commit : releaseCommits) {
                    ObjectId idTree = commit.getTree(); //References to this commit tree
                    Repository repository = git.getRepository();
                    TreeWalk treeWalk = getTreeWalk(idTree, repository);

                    while (treeWalk.next()) {
                        JavaClass file = loadJavaClassFromTreeWalk(treeWalk, release, repository, javaClassRelease);
                        if (file != null) {

                            release.addJavaClass(file);
                            javaClassList.add(file);
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error retrieving classes ", e);
        }
        //If a new release doesn't have new classes, we copy the last release classes in this
        checkIfNewClassTouched(releaseList);
        //Now we want to associate Java classes with the correct commits
        associateCommitsToClass(javaClassList, releaseList);

        return javaClassList;
    }

    private static TreeWalk getTreeWalk(ObjectId idTree, Repository repository) throws IOException {
        TreeWalk treeWalk = new TreeWalk(repository);
        treeWalk.reset(idTree); //I want to explore this tree
        treeWalk.setRecursive(true); //Go in the subdirectory
        return treeWalk;
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
            javaClassRelease.add(filename); //For duplicate
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

    public void calculateLocMeasures(JavaClass javaClass) throws IOException, GitAPIException {
        List<Integer> locTouched = new ArrayList<>();
        List<Integer> totalAddedLines = new ArrayList<>();
        List<Integer> totalChurn = new ArrayList<>();

        for(RevCommit commit : javaClass.getListOfCommit()) {
            try (ObjectReader reader = git.getRepository().newObjectReader()) {
                if (commit.getParentCount() == 0) {
                    continue;
                }
                CanonicalTreeParser commitTree = prepareTreeParser(reader, commit);
                CanonicalTreeParser commitParentTree = prepareTreeParser(reader, commit.getParent(0));

                List<DiffEntry> diffs = git.diff()
                        .setNewTree(commitTree)
                        .setOldTree(commitParentTree)
                        .call();

                for (DiffEntry entry : diffs) {
                    if (entry.getNewPath().equals(javaClass.getClassName())) {
                        locTouched.add(calculateLocTouched(entry));
                        totalAddedLines.add(calculateTotalAddLines(entry));
                        totalChurn.add(calculateTotalChurn(entry));
                    }
                }
            }
        }

        javaClass.setTouchedLoc(locTouched.stream().mapToInt(Integer::intValue).sum());
        javaClass.setTotalChurn(totalChurn.stream().mapToInt(Integer::intValue).sum());
        javaClass.setTotalAddedLines(totalAddedLines.stream().mapToInt(Integer::intValue).sum());
        javaClass.setMaxAddedLines(totalAddedLines.isEmpty() ? 0 : Collections.max(totalAddedLines));
        javaClass.setMaxChurn(totalChurn.isEmpty() ? 0 : Collections.max(totalChurn));
    }

    private int calculateTotalAddLines(DiffEntry entry) throws IOException {
        int totalAddLines = 0;

        FileHeader fileHeader = getFileHeader(entry);
        for (Edit edit : fileHeader.toEditList()){
            totalAddLines += (edit.getEndB() - edit.getBeginB());
        }

        return totalAddLines;
    }

    private int calculateLocTouched(DiffEntry entry) throws IOException {
        int touchedLoc = 0;
        FileHeader fileHeader = getFileHeader(entry);

        for (Edit edit : fileHeader.toEditList()) {
            touchedLoc += (edit.getEndA() - edit.getBeginA()) + (edit.getEndB() - edit.getBeginB());
        }

        return touchedLoc;
    }

    private int calculateTotalChurn(DiffEntry entry) throws IOException {
        int churn = 0;
        FileHeader fileHeader = getFileHeader(entry);

        for (Edit edit : fileHeader.toEditList()) {
            churn += Math.abs((edit.getEndB() - edit.getBeginB()) - (edit.getEndA() - edit.getBeginA()));
        }
        return churn;
    }

    private FileHeader getFileHeader(DiffEntry entry) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DiffFormatter diffFormatter = new DiffFormatter(outputStream);
        diffFormatter.setRepository(git.getRepository());

        return diffFormatter.toFileHeader(entry);
    }

    public List<String> findTouchedClassFromCommit(RevCommit commit) throws IOException {
        List<String> touchedClassesNames = new ArrayList<>();

        try (ObjectReader reader = git.getRepository().newObjectReader();
             DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {

            if (commit.getParentCount() == 0) {
                return touchedClassesNames;
            }

            RevCommit commitParent = commit.getParent(0);
            diffFormatter.setRepository(git.getRepository());
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
        ObjectId treeId = commit.getTree();
        treeParser.reset(reader, treeId);
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

    @Override
    public void close() {
        if(git != null){
            git.close();
        }
    }
}