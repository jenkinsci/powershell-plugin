package hudson.plugins.powershell;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tasks.CommandInterpreter;
import net.sf.json.JSONObject;
import org.apache.commons.lang.SystemUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Invokes PowerShell from Jenkins.
 * 
 * @author Kohsuke Kawaguchi
 */
public class PowerShell extends CommandInterpreter {

    /** boolean switch setting -NoProfile */
    private final boolean useProfile;

    private final boolean stopOnError;

    private Launcher launcher;

    @DataBoundConstructor
    public PowerShell(String command, boolean stopOnError, boolean useProfile) {
        super(command);
        this.stopOnError = stopOnError;
        this.useProfile = useProfile;
    }

    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws InterruptedException
    {
        this.launcher = launcher;
        try
        {
            return super.perform(build, launcher, listener);
        }
        catch (InterruptedException e)
        {
            throw e;
        }
    }

    public boolean isStopOnError() {
        return stopOnError;
    }

    public boolean isUseProfile() {
        return useProfile;
    }

    protected String getFileExtension() {
        return ".ps1";
    }

    public String[] buildCommandLine(FilePath script) {
        DescriptorImpl descriptor = (DescriptorImpl) getDescriptor();
        boolean prioritizePowerShellCore = false;
        try
        {
            if (descriptor.getPowerShellVersionPreference().equals("powershellCore"))
            {
                prioritizePowerShellCore = true;
            }
        }
        catch(Exception e)
        {
            prioritizePowerShellCore = false;
        }

        PowerShellInstallation tool = new PowerShellInstallation("DEFAULT", "DEFAULT");
        String powerShellExecutable = tool.getPowershell(isRunningOnWindows(script), script.isRemote(), prioritizePowerShellCore, launcher);
        if (isRunningOnWindows(script)) {
            if (useProfile){
                return new String[] { powerShellExecutable, "-NonInteractive", "-ExecutionPolicy", "Bypass", "-File", script.getRemote()};
            } else {
                return new String[] { powerShellExecutable, "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-File", script.getRemote()};
            }
        } else {
            // ExecutionPolicy option does not work (and is not required) for non-Windows platforms
            // See https://github.com/PowerShell/PowerShell/issues/2742
            if (useProfile){
                return new String[] { powerShellExecutable, "-NonInteractive", "-File", script.getRemote()};
            } else {
                return new String[] { powerShellExecutable, "-NonInteractive", "-NoProfile", "-File", script.getRemote()};
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

        private String powerShellVersionPreference;

        public DescriptorImpl()
        {
            super();
            load();
        }

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

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            setPowerShellVersionPreference(formData.getString("powerShellVersionPreference"));
            save();
            return true;
        }

        public String getPowerShellVersionPreference() {
            return powerShellVersionPreference;
        }

        public void setPowerShellVersionPreference(String powerShellVersionPreference) {
            this.powerShellVersionPreference = powerShellVersionPreference;
        }
    }
}
