package org.marcolore.bugginesspredictor.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Release {

    private int idRelease;
    private String name;
    private LocalDateTime releaseDate;
    private int id;


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


}
