package hudson.plugins.powershell;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tasks.CommandInterpreter;
import jenkins.model.Jenkins;
import org.apache.commons.lang.SystemUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

/**
 * Invokes PowerShell from Jenkins.
 * 
 * @author Kohsuke Kawaguchi
 */
public class PowerShell extends CommandInterpreter {

    /** boolean switch setting -NoProfile */
    private final boolean useProfile;

    private final boolean stopOnError;

    private TaskListener listener;

    @DataBoundConstructor
    public PowerShell(String command, boolean stopOnError, boolean useProfile) {
        super(command);
        this.stopOnError = stopOnError;
        this.useProfile = useProfile;
    }

    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws InterruptedException
    {
        this.listener = listener;
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

    @Override
    protected String getFileExtension() {
        return ".ps1";
    }

    @Override
    public String[] buildCommandLine(FilePath script) {
        String powerShellExecutable = null;
        PowerShellInstallation installation = null;
        if (isRunningOnWindows(script)) {
            installation = Jenkins.get().getDescriptorByType(PowerShellInstallation.DescriptorImpl.class).getAnyInstallation(PowerShellInstallation.DEFAULTWINDOWS);
        }
        else {
            installation = Jenkins.get().getDescriptorByType(PowerShellInstallation.DescriptorImpl.class).getAnyInstallation(PowerShellInstallation.DEFAULTLINUX);
        }
        if (installation != null) {
            Node node = filePathToNode(script);
            try {
                if (node != null && installation.forNode(node, listener) != null) {
                    powerShellExecutable = installation.forNode(node, listener).getPowerShellBinary();
                }
                else {
                    powerShellExecutable = installation.getPowerShellBinary();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (powerShellExecutable == null)
        {
            powerShellExecutable = PowerShellInstallation.getDefaultPowershellWhenNoConfiguration(isRunningOnWindows(script));
        }

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

    @Override
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

    private static Node filePathToNode(FilePath script) {
        Computer computer = script.toComputer();
        Node node = null;
        if (computer != null) {
            node = computer.getNode();
        }
        return node;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public DescriptorImpl()
        {
            super();
            load();
        }

        @Override
        public String getHelpFile() {
            return "/plugin/powershell/help.html";
        }
        
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "PowerShell";
        }
    }
}
