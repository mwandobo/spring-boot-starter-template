package com.bonnysimon.starter.features.approval.enums;

//public enum ApprovalActionEnum {
//    PENDING,
//    APPROVED,
//    REJECTED,
//    CANCELLED
//
//
//}


public enum ApprovalActionEnum {
    APPROVED,
    REJECTED,
    PENDING,
    CANCELLED;

    public static ApprovalActionEnum fromString(String value) {
        if (value == null) throw new IllegalArgumentException("Action cannot be null");

        switch (value.trim().toLowerCase()) {
            case "approve":
            case "approved":
                return APPROVED;
            case "pending":
                return PENDING;
            case "cancelled":
            case "cancel":
                return CANCELLED;
            case "reject":
            case "rejected":
                return REJECTED;
            default:
                throw new IllegalArgumentException("Unknown action: " + value);
        }
    }
}
