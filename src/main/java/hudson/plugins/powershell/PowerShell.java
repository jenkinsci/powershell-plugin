package hudson.plugins.powershell;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tasks.CommandInterpreter;
import org.apache.commons.lang.SystemUtils;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Invokes PowerShell from Jenkins.
 * 
 * @author Kohsuke Kawaguchi
 */
public class PowerShell extends CommandInterpreter {

    /** boolean switch setting -NoProfile */
    private final boolean useProfile;

    private final boolean stopOnError;
    
    private final String powerShellVersionPreference;

    @Deprecated
    public PowerShell(String command, boolean stopOnError, boolean useProfile) {
        super(command);
        this.stopOnError = stopOnError;
        this.useProfile = useProfile;
        this.powerShellVersionPreference = "osBased";
    }

    @DataBoundConstructor
    public PowerShell(String command, boolean stopOnError, boolean useProfile, String powerShellVersionPreference) {
        super(command);
        this.stopOnError = stopOnError;
        this.useProfile = useProfile;
        this.powerShellVersionPreference = powerShellVersionPreference;
    }

    public boolean isStopOnError() {
        return stopOnError;
    }

    public boolean isUseProfile() {
        return useProfile;
    }
    
    public String getPowerShellVersionPreference() {
        return powerShellVersionPreference;
    }

    protected String getFileExtension() {
        return ".ps1";
    }

    public String[] buildCommandLine(FilePath script) {
        switch (powerShellVersionPreference == null ? "osBased" : powerShellVersionPreference) {
            case "windowsPowerShell":
                if (useProfile) {
                    return new String[]{"powershell.exe", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-File", script.getRemote()};
                } else {
                    return new String[]{"powershell.exe", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-NoProfile", "-File", script.getRemote()};
                }

            case "powershellCore": {
                // ExecutionPolicy option does not work (and is not required) for non-Windows platforms
                // See https://github.com/PowerShell/PowerShell/issues/2742
                if (useProfile) {
                    return new String[]{"pwsh", "-NonInteractive", "-File", script.getRemote()};
                } else {
                    return new String[]{"pwsh", "-NonInteractive", "-NoProfile", "-File", script.getRemote()};
                }
            }

            case "osBased":
            default:
                if (isRunningOnWindows(script)) {
                    if (useProfile) {
                        return new String[]{"powershell.exe", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-File", script.getRemote()};
                    } else {
                        return new String[]{"powershell.exe", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-NoProfile", "-File", script.getRemote()};
                    }
                } else {
                    // ExecutionPolicy option does not work (and is not required) for non-Windows platforms
                    // See https://github.com/PowerShell/PowerShell/issues/2742
                    if (useProfile) {
                        return new String[]{"pwsh", "-NonInteractive", "-File", script.getRemote()};
                    } else {
                        return new String[]{"pwsh", "-NonInteractive", "-NoProfile", "-File", script.getRemote()};
                    }
                }
        }
    }

    protected String getContents() {
        StringBuilder sb = new StringBuilder();
        if (stopOnError) {
            sb.append("$ErrorActionPreference=\"Stop\"");
        } else {
            sb.append("$ErrorActionPreference=\"Continue\"");
        }
        sb.append(System.lineSeparator());
        sb.append(command);
        sb.append(System.lineSeparator());
        sb.append("exit $LastExitCode");
        return sb.toString();
    }

    private boolean isRunningOnWindows(FilePath script) {
        // Ideally this would use a property of the build/run, but unfortunately CommandInterpreter only gives us the
        // FilePath, so we need to guess based on that.
        if (!script.isRemote()) {
            // Running locally, so we can just check the local OS
            return SystemUtils.IS_OS_WINDOWS;
        }

        // Running remotely, guess based on the path. A path starting with something like "C:\" is Windows.
        String path = script.getRemote();
        return path.length() > 3 && path.charAt(1) == ':' && path.charAt(2) == '\\';
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        @Override
        public String getHelpFile() {
            return "/plugin/powershell/help.html";
        }
        
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        public String getDisplayName() {
            return "PowerShell";
        }
    }
}
