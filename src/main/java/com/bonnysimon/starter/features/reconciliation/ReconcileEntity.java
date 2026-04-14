package com.bonnysimon.starter.features.reconciliation;

import jakarta.persistence.Column;

public class ReconcileEntity {
    @Column()
    private String name;

    @Column()
    private String description;

    @Column()
    private String fileUrl;
}
