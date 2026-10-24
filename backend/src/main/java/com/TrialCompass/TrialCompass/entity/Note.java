package com.TrialCompass.TrialCompass.entity;

import jakarta.persistence.*;

//entity that houses notes that pertain to a saved trial.

@Entity
@Table(name="notes")
public class Note extends BaseEntity{

    //many notes can belong to one trial
    //updatable = false means a note cannot be moved to a different trial - not that it cant be edited

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "saved_study_id", nullable = false, updatable = false)
    private SavedStudies savedStudies;

    @Column(nullable = false, length = 2000)
    private String body;

    protected Note() {}

    public Note(SavedStudies savedstudy, String body) {
        this.savedStudies = savedstudy;
        this.body = body;
    }

    public SavedStudies getSavedstudy() {
        return savedStudies;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
