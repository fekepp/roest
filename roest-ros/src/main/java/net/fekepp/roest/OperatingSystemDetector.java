package net.fekepp.roest;

public final class OperatingSystemDetector {

	private final static String NAME = System.getProperty("os.name")
			.toLowerCase();

	public final static String ACRONYM_MAC_OS_X = "mac";
	public final static String NAME_MAC_OS_X = "Mac OS X";

	public final static String ACRONYM_SOLARIS = "sunos";
	public final static String NAME_SOLARIS = "Solaris";

	public final static String ACRONYM_UNIX = "nix";
	public final static String NAME_UNIX = "Unix/Linux";

	public final static String ACRONYM_WINDOWS = "win";
	public final static String NAME_WINDOWS = "Windows";

	public static String getOperatingSystemAcronym() {

		if (isMacOsX()) {
			return ACRONYM_MAC_OS_X;
		}

		if (isSolaris()) {
			return ACRONYM_SOLARIS;
		}

		if (isUnix()) {
			return ACRONYM_UNIX;
		}

		if (isWindows()) {
			return ACRONYM_WINDOWS;
		}

		return null;

	}

	public static String getOperatingSystemName() {

		if (isMacOsX()) {
			return NAME_MAC_OS_X;
		}

		if (isSolaris()) {
			return NAME_SOLARIS;
		}

		if (isUnix()) {
			return NAME_UNIX;
		}

		if (isWindows()) {
			return NAME_WINDOWS;
		}

		return null;

	}

	public static boolean isMacOsX() {
		return NAME.contains(ACRONYM_MAC_OS_X);
	}

	public static boolean isSolaris() {
		return NAME.contains(ACRONYM_SOLARIS);
	}

	public static boolean isUnix() {
		// Workaround, just "nix" taken as OS name, others just tested
		return (NAME.contains(ACRONYM_UNIX) || NAME.contains("nux") || NAME
				.contains("aix"));
	}

	public static boolean isWindows() {
		return NAME.contains(ACRONYM_WINDOWS);
	}

	public static void main(String[] args) {

		System.out.println("OS acronym: " + getOperatingSystemAcronym());
		System.out.println("OS name: " + getOperatingSystemName());

	}

}
