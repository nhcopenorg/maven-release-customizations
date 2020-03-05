package org.nhc.open.release.policy;

import org.apache.maven.shared.release.policy.version.VersionPolicy;
import org.apache.maven.shared.release.policy.version.VersionPolicyRequest;
import org.apache.maven.shared.release.policy.version.VersionPolicyResult;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.codehaus.plexus.component.annotations.Component;

@Component(role = VersionPolicy.class, hint = "namedVersionPolicy", description = "A VersionPolicy implementation that uses  pattern versioning")
public class CustomVersionPolicy implements VersionPolicy {

    private final String currentVersion;

    public CustomVersionPolicy(String currentVersion) {
        this.currentVersion = currentVersion.trim();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public VersionPolicyResult getReleaseVersion(VersionPolicyRequest versionPolicyRequest) throws VersionParseException {
        String releaseVersion = (new CustomVersionInfo(versionPolicyRequest.getVersion())).getReleaseVersionString();
        return new VersionPolicyResult()
                .setVersion(releaseVersion);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VersionPolicyResult getDevelopmentVersion(VersionPolicyRequest versionPolicyRequest) throws VersionParseException {
        String developmentVersion =
                new CustomVersionInfo(versionPolicyRequest.getVersion()).getNextVersion().getSnapshotVersionString();
        return new VersionPolicyResult().setVersion(developmentVersion);
    }
}
