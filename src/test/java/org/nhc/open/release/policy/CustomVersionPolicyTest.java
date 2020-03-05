package org.nhc.open.release.policy;

import org.apache.maven.shared.release.policy.version.VersionPolicy;
import org.apache.maven.shared.release.policy.version.VersionPolicyRequest;
import org.apache.maven.shared.release.policy.version.VersionPolicyResult;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CustomVersionPolicyTest {

    private VersionPolicy testee;
    private VersionPolicyResult result;

    private static VersionPolicyRequest version(String version) {
        return new VersionPolicyRequest().setVersion(version);
    }

    // Release version tests
    @Test
    public void shouldThrowParseException() throws Exception {
        testee = new CustomVersionPolicy(" ");
        Assertions.assertThrows(VersionParseException.class, () -> {
            result = testee.getReleaseVersion(version(" "));
        });
    }

    @Test
    public void shouldRemoveSnapshot_version() throws Exception {
        testee = new CustomVersionPolicy("1.0.1");
        result = testee.getReleaseVersion(version("1.0.1"));
        assertThat(result.getVersion(), is("1.0.1"));
    }

    @Test
    public void shouldRemoveSnapshot_versionSnapshot() throws Exception {
        testee = new CustomVersionPolicy("1.0.1-SNAPSHOT");
        result = testee.getReleaseVersion(version("1.0.1-SNAPSHOT"));
        assertThat(result.getVersion(), is("1.0.1"));
    }

    @Test
    public void shouldRemoveSnapshot_prefix() throws Exception {
        testee = new CustomVersionPolicy("releaseCandidateOrName-1.0.1-SNAPSHOT");
        result = testee.getReleaseVersion(version("releaseCandidateOrName-1.0.1-SNAPSHOT"));
        assertThat(result.getVersion(), is("releaseCandidateOrName-1.0.1"));
    }

    @Test
    public void shouldRemoveSnapshot_full() throws Exception {
        testee = new CustomVersionPolicy("releaseNAME-releaseCandidate-1.0.1-SNAPSHOT");
        result = testee.getReleaseVersion(version("releaseNAME-releaseCandidate-1.0.1-SNAPSHOT"));
        assertThat(result.getVersion(), is("releaseNAME-releaseCandidate-1.0.1"));
    }

    // Development version tests
    @Test
    public void shouldNextMinorVersionSnapshot_version() throws Exception {
        testee = new CustomVersionPolicy("1.0.1");
        result = testee.getDevelopmentVersion(version("1.0.1"));
        assertThat(result.getVersion(), is("1.0.2-SNAPSHOT"));
    }

    @Test
    public void shouldNextMinorVersionSnapshot_versionSnapshot() throws Exception {
        testee = new CustomVersionPolicy("1.0.1-SNAPSHOT");
        result = testee.getDevelopmentVersion(version("1.0.1-SNAPSHOT"));
        assertThat(result.getVersion(), is("1.0.2-SNAPSHOT"));
    }

    @Test
    public void shouldNextMinorVersionSnapshot_prefix() throws Exception {
        testee = new CustomVersionPolicy("releaseCandidateOrName-1.0.1-SNAPSHOT");
        result = testee.getDevelopmentVersion(version("releaseCandidateOrName-1.0.1-SNAPSHOT"));
        assertThat(result.getVersion(), is("releaseCandidateOrName-1.0.2-SNAPSHOT"));
    }

    @Test
    public void shouldNextMinorVersionSnapshot_full() throws Exception {
        testee = new CustomVersionPolicy("releaseNAME-releaseCandidate-1.0.1-SNAPSHOT");
        result = testee.getDevelopmentVersion(version("releaseNAME-releaseCandidate-1.0.1-SNAPSHOT"));
        assertThat(result.getVersion(), is("releaseNAME-releaseCandidate-1.0.2-SNAPSHOT"));
    }

}
