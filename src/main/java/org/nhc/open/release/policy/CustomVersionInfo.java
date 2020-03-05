package org.nhc.open.release.policy;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.shared.release.versions.VersionInfo;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.codehaus.plexus.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomVersionInfo implements VersionInfo {

    private final String strVersion;
    private final String releaseName;
    private final String alphaAnnotation;

    private final List<String> digits;
    private final String buildSpecifier;
    private static final String ANNOTATIONS_SEPARATOR_STRING = "-";
    private static final String DIGIT_SEPARATOR_STRING = ".";

    public static final Pattern CUSTOM_STANDARD_PATTERN = Pattern.compile(
            "([a-zA-Z]*)?"               // alpha characters (looking for annotation - {deploy Name})
                    + "(?:[-_])?"                // optional - or _  (annotation revision separator)
                    + "([a-zA-Z]*)?"             // alpha characters (looking for annotation - alpha, beta, RC, etc.)
                    + "(?:[-_])?"                // optional - or _  (annotation revision separator)
                    + "((?:\\d+\\.)*\\d*)?"      // digits  (any digits after rc or beta is an annotation revision)
                    + "(?:[-_])?"                // optional - or _  (annotation revision separator)
                    + "(?:(.*?))?$");            // - or _ followed everything else (build specifier like SNAPSHOT)


    /**
     * Constructs this object and parses the supplied version string.
     * @param version
     */
    public CustomVersionInfo(String version)
            throws VersionParseException {
        strVersion = version;

        Matcher m = CUSTOM_STANDARD_PATTERN.matcher(strVersion);
        if (m.matches() && nullIfEmpty(strVersion)!=null) {
            releaseName = m.group(1);
            alphaAnnotation = m.group(2);
            digits = parseDigits(m.group(3));
            buildSpecifier = m.group(4);
        } else {
            throw new VersionParseException("Unable to parse the version string: \"" + version + "\"");
        }
    }

    public CustomVersionInfo(String releaseName, String alphaAnnotation, List<String> digits, String buildSpecifier) {
        this.releaseName = releaseName;
        this.alphaAnnotation = alphaAnnotation;
        this.digits = digits;
        this.buildSpecifier = buildSpecifier;
        this.strVersion = getVersionString(this, buildSpecifier);
    }

    @Override
    public boolean isSnapshot() {
        return ArtifactUtils.isSnapshot(strVersion);
    }

    @Override
    public VersionInfo getNextVersion() {
        CustomVersionInfo version = null;
        if (digits != null) {
            List<String> digits = new ArrayList<>(this.digits);
                digits.set(digits.size() - 1, incrementVersionString(digits.get(digits.size() - 1)));

            version = new CustomVersionInfo(releaseName, alphaAnnotation,digits, buildSpecifier);
        }
        return version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(VersionInfo obj) {
        CustomVersionInfo that = (CustomVersionInfo) obj;
        int result;
        // TODO: this is a workaround for a bug in DefaultArtifactVersion - fix there - 1.01 < 1.01.01
        if (strVersion.startsWith(that.strVersion) && !strVersion.equals(that.strVersion)
                && strVersion.charAt(that.strVersion.length()) != '-') {
            result = 1;
        } else if (that.strVersion.startsWith(strVersion) && !strVersion.equals(that.strVersion)
                && that.strVersion.charAt(strVersion.length()) != '-') {
            result = -1;
        } else {
            // TODO: this is a workaround for a bug in DefaultArtifactVersion - fix there - it should not consider case in comparing the qualifier
            // NOTE: The combination of upper-casing and lower-casing is an approximation of String.equalsIgnoreCase()
            String thisVersion = strVersion.toUpperCase(Locale.ENGLISH).toLowerCase(Locale.ENGLISH);
            String thatVersion = that.strVersion.toUpperCase(Locale.ENGLISH).toLowerCase(Locale.ENGLISH);

            result = new DefaultArtifactVersion(thisVersion).compareTo(new DefaultArtifactVersion(thatVersion));
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CustomVersionInfo)) {
            return false;
        }
        return compareTo((VersionInfo) obj) == 0;
    }

    @Override
    public int hashCode() {
        return strVersion.toLowerCase(Locale.ENGLISH).hashCode();
    }

    /**
     * {@inheritDoc}
     */
    protected String incrementVersionString(String s) {
        int n = Integer.valueOf(s).intValue() + 1;
        String value = String.valueOf(n);
        if (value.length() < s.length()) {
            // String was left-padded with zeros
            value = StringUtils.leftPad(value, s.length(), "0");
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSnapshotVersionString() {
        if (strVersion.equals(Artifact.SNAPSHOT_VERSION)) {
            return strVersion;
        }

        String baseVersion = getReleaseVersionString();

        if (baseVersion.length() > 0) {
            baseVersion += "-";
        }

        return baseVersion + Artifact.SNAPSHOT_VERSION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getReleaseVersionString() {
        String baseVersion = strVersion;

        Matcher m = Artifact.VERSION_FILE_PATTERN.matcher(baseVersion);
        if (m.matches()) {
            baseVersion = m.group(1);
        }
        // MRELEASE-623 SNAPSHOT is case-insensitive
        else if (StringUtils.right(baseVersion, 9).equalsIgnoreCase("-" + Artifact.SNAPSHOT_VERSION)) {
            baseVersion = baseVersion.substring(0, baseVersion.length() - Artifact.SNAPSHOT_VERSION.length() - 1);
        } else if (baseVersion.equals(Artifact.SNAPSHOT_VERSION)) {
            baseVersion = "1.0";
        }
        return baseVersion;
    }

    @Override
    public String toString() {
        return strVersion;
    }

    protected static String getVersionString(CustomVersionInfo info, String buildSpecifier) {
        StringBuilder sb = new StringBuilder();

        if (StringUtils.isNotEmpty(info.releaseName)) {
            sb.append(info.releaseName);
        }

        if (StringUtils.isNotEmpty(info.alphaAnnotation)) {
            sb.append(StringUtils.defaultString(ANNOTATIONS_SEPARATOR_STRING));
            sb.append(info.alphaAnnotation);
        }

        if (info.digits != null) {
            if(sb.length()>0) {
                sb.append(StringUtils.defaultString(ANNOTATIONS_SEPARATOR_STRING));
            }
            sb.append(joinDigitString(info.digits));

        }

        if (StringUtils.isNotEmpty(buildSpecifier)) {
            sb.append(StringUtils.defaultString(ANNOTATIONS_SEPARATOR_STRING));
            sb.append(buildSpecifier);
        }

        return sb.toString();
    }

    /**
     * Simply joins the items in the list with "." period
     * @param digits
     */
    protected static String joinDigitString(List<String> digits) {
        return digits != null ? StringUtils.join(digits.iterator(), DIGIT_SEPARATOR_STRING) : null;
    }

    /**
     * Splits the string on "." and returns a list
     * containing each digit.
     * @param strDigits
     */
    private List<String> parseDigits(String strDigits) {
        return Arrays.asList(StringUtils.split(strDigits, DIGIT_SEPARATOR_STRING));
    }

    //--------------------------------------------------
    // Getters & Setters
    //--------------------------------------------------

    private static String nullIfEmpty(String s) {
        return StringUtils.isEmpty(s) ? null : s;
    }


    public String getStrVersion() {
        return strVersion;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public String getAlphaAnnotation() {
        return alphaAnnotation;
    }

    public List<String> getDigits() {
        return digits;
    }


    public String getBuildSpecifier() {
        return buildSpecifier;
    }
}
