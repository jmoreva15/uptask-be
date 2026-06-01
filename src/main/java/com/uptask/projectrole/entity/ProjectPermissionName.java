package com.uptask.projectrole.entity;

public final class ProjectPermissionName {

    private ProjectPermissionName() {}

    public static final String PROJECT_VIEW       = "PROJECT_VIEW";
    public static final String PROJECT_EDIT       = "PROJECT_EDIT";
    public static final String PROJECT_DELETE     = "PROJECT_DELETE";

    public static final String MEMBER_VIEW        = "MEMBER_VIEW";
    public static final String MEMBER_INVITE      = "MEMBER_INVITE";
    public static final String MEMBER_REMOVE      = "MEMBER_REMOVE";
    public static final String MEMBER_ROLE_ASSIGN = "MEMBER_ROLE_ASSIGN";

    public static final String ROLE_CREATE        = "ROLE_CREATE";
    public static final String ROLE_EDIT          = "ROLE_EDIT";
    public static final String ROLE_DELETE        = "ROLE_DELETE";

    public static final String TASK_VIEW          = "TASK_VIEW";
    public static final String TASK_CREATE        = "TASK_CREATE";
    public static final String TASK_EDIT          = "TASK_EDIT";
    public static final String TASK_DELETE        = "TASK_DELETE";
    public static final String TASK_ASSIGN        = "TASK_ASSIGN";
    public static final String TASK_STATUS_CHANGE = "TASK_STATUS_CHANGE";

    public static final String COMMENT_CREATE     = "COMMENT_CREATE";
    public static final String COMMENT_DELETE_OWN = "COMMENT_DELETE_OWN";
    public static final String COMMENT_DELETE_ANY = "COMMENT_DELETE_ANY";
}
