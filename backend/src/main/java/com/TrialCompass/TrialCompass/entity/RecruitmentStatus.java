package com.TrialCompass.TrialCompass.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

//entity to hold status of trial recruitment in db
@Entity
@Table(name = "recruitment_statuses")
public class RecruitmentStatus extends BaseEntity{

    @Column(nullable = false, unique = true, length=50)
    private String code;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(nullable = false, name = "sort_order")
    private Integer sortOrder;

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

}
