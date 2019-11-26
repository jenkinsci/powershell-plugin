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

    boolean stopOnError;

    @DataBoundConstructor
    public PowerShell(String command, boolean stopOnError) {
        super(command);
        this.stopOnError = stopOnError;
    }

    public boolean isStopOnError() {
        return stopOnError;
    }

    protected String getFileExtension() {
        return ".ps1";
    }

    public String[] buildCommandLine(FilePath script) {
        if (isRunningOnWindows(script)) {
            return new String[] { "powershell.exe", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-File" , script.getRemote()};
        } else {
            // ExecutionPolicy option does not work (and is not required) for non-Windows platforms
            // See https://github.com/PowerShell/PowerShell/issues/2742
            return new String[] { "pwsh", "-NonInteractive", "-File" , script.getRemote()};
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
