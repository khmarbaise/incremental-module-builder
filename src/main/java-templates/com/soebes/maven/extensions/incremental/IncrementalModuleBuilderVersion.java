package com.soebes.maven.extensions.incremental;

public final class IncrementalModuleBuilderVersion {

    private static final String VERSION = "${project.version}";
    private static final String GROUPID = "${project.groupId}";
    private static final String SVN = "${project.scm.developerConnection}";
    private static final String SVN_BRANCH = "${scmBranch}";
    private static final String REVISION = "${buildNumber}";

    
    private IncrementalModuleBuilderVersion () {
        // no one should create an instance of this class.
    }

    public static final String getVersion() {
        return VERSION;
    }

    public static final String getGroupId() {
        return GROUPID;
    }

    public static final String getSVN() {
        return SVN;
    }

    public static final String getRevision() {
        return REVISION;
    }

    public static final String getSVNBranch() {
        return SVN_BRANCH;
    }
}
