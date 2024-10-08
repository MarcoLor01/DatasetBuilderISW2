package org.marcolore.bugginesspredictor.model;

import org.eclipse.jgit.revwalk.RevCommit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Release {

    private int idRelease;
    private String name;
    private LocalDateTime releaseDate;
    private int id;
    private List<RevCommit> commitList;


    public Release(String id, String name, String releaseDate) {
        LocalDate date = LocalDate.parse(releaseDate);
        this.releaseDate = date.atStartOfDay();
        this.idRelease = Integer.parseInt(id);
        this.name = name;
    }

    public Integer getIdRelease() {
        return idRelease;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDateTime releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setIdRelease(int id) {
        this.idRelease = id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<RevCommit> getCommitList() {
        if (commitList == null){
            commitList = new ArrayList<>();
        }
        return commitList;
    }

    public void setCommitList(List<RevCommit> commitList) {
        this.commitList = commitList;
    }


}
