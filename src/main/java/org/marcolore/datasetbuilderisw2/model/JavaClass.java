package org.marcolore.datasetbuilderisw2.model;

import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.List;

public class JavaClass {
    private String className;
    private List<RevCommit> listOfCommit = new ArrayList<>();
    private Release release;
    private String fileContent;

    public JavaClass(String className, Release release, String fileContent) {
        this.className = className;
        this.release = release;
        this.fileContent = fileContent;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<RevCommit> getListOfCommit() {
        return listOfCommit;
    }

    public void setListOfCommit(List<RevCommit> listOfCommit) {
        this.listOfCommit = listOfCommit;
    }

    public void addCommitToList(RevCommit commit) {
        this.listOfCommit.add(commit);
    }

    public Release getRelease() {
        return release;
    }

    public void setRelease(Release release) {
        this.release = release;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }
}
