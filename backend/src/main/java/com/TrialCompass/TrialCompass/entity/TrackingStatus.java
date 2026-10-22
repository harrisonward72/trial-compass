package com.TrialCompass.TrialCompass.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

//tracking status lookup table

@Entity
@Table(name = "tracking_statuses")
public class TrackingStatus extends BaseEntity{
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    protected TrackingStatus() {

    }

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
