package hudson.plugins.powershell;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.model.Item;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tasks.CommandInterpreter;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import org.apache.commons.lang.SystemUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.verb.POST;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Invokes PowerShell from Jenkins.
 * 
 * @author Kohsuke Kawaguchi
 */
public class PowerShell extends CommandInterpreter {

    /** boolean switch setting -NoProfile */
    private final boolean useProfile;

    private Integer unstableReturn;

    private final boolean stopOnError;

    private transient TaskListener listener;

    private String installation;

    @DataBoundConstructor
    public PowerShell(String command, boolean stopOnError, boolean useProfile, Integer unstableReturn) {
        super(command);
        this.stopOnError = stopOnError;
        this.useProfile = useProfile;
        this.unstableReturn = unstableReturn;
    }

    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws InterruptedException
    {
        this.listener = listener;
        return super.perform(build, launcher, listener);
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

    @CheckForNull
    public final Integer getUnstableReturn() {
        return Integer.valueOf(0).equals(unstableReturn) ? null : unstableReturn;
    }

    @DataBoundSetter
    public void setUnstableReturn(Integer unstableReturn) {
        this.unstableReturn = unstableReturn;
    }

    @Override
    protected boolean isErrorlevelForUnstableBuild(int exitCode) {
        return this.unstableReturn != null && exitCode != 0 && this.unstableReturn.equals(exitCode);
    }

    @DataBoundSetter
    public void setInstallation(String installation) {
        this.installation = Util.fixEmptyAndTrim(installation);
    }

    public String getInstallation() {
        return installation;
    }

    @Override
    public String[] buildCommandLine(FilePath script) {

        final var installation = getPowerShellInstallation(script);
        final var powerShellExecutable = getPowerShellExecutable(script, installation);

        List<String> args = new ArrayList<>();
        args.add(powerShellExecutable);
        args.add("-NonInteractive");
        if (!useProfile) {
            args.add("-NoProfile");
        }
        if (isRunningOnWindows(script)) {
            // ExecutionPolicy option does not work (and is not required) for non-Windows platforms
            // See https://github.com/PowerShell/PowerShell/issues/2742
            args.add("-ExecutionPolicy");
            args.add("Bypass");
        }
        args.add("-File");
        args.add(script.getRemote());
        return args.toArray(new String[0]);
    }

    @NonNull
    private String getPowerShellExecutable(FilePath script, PowerShellInstallation installation) {
        String powerShellExecutable = null;

        if (installation != null) {
            Node node = filePathToNode(script);
            try {
                if (node != null && installation.forNode(node, listener) != null) {
                    installation = installation.forNode(node, listener);
                }

                final var home = installation.getPowershellHome();
                if (home != null) {
                    final var separator = isRunningOnWindows(script) ? "\\" : "/";
                    powerShellExecutable = home + separator + installation.getPowerShellBinary();
                } else  {
                    powerShellExecutable = installation.getPowerShellBinary();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        // fallback to installed version on agent
        if (powerShellExecutable == null)
        {
            powerShellExecutable = PowerShellInstallation.getDefaultPowershellWhenNoConfiguration(isRunningOnWindows(script));
        }

        return powerShellExecutable;
    }

    @Nullable
    private PowerShellInstallation getPowerShellInstallation(FilePath script) {
        PowerShellInstallation powerShellInstallation;

        final var descriptor = Jenkins.get().getDescriptorByType(PowerShellInstallation.DescriptorImpl.class);

        powerShellInstallation = descriptor.getInstallation(this.installation);
        if (powerShellInstallation == null) {
            if (isRunningOnWindows(script)) {
                powerShellInstallation = descriptor.getAnyInstallation(PowerShellInstallation.DEFAULT_WINDOWS_NAME);
            }
            else {
                powerShellInstallation = descriptor.getAnyInstallation(PowerShellInstallation.DEFAULT_LINUX_NAME);
            }
        }

        return powerShellInstallation;
    }

    @Override
    protected String getContents() {
        StringBuilder sb = new StringBuilder();
        if (stopOnError) {
            sb.append("$ErrorActionPreference=\"Stop\"");
            sb.append(System.lineSeparator());
        }
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

        @NonNull
        @Override
        public String getDisplayName() {
            return "PowerShell";
        }

        @POST
        public ListBoxModel doFillInstallationItems() {
            Jenkins.get().checkPermission(Item.CONFIGURE);

            ListBoxModel model = new ListBoxModel();

            PowerShellInstallation.DescriptorImpl descriptor =
                    Jenkins.get().getDescriptorByType(PowerShellInstallation.DescriptorImpl.class);

            model.add(Messages.none(), "");
            for (PowerShellInstallation tool : descriptor.getInstallations()) {
                model.add(tool.getName());
            }
            return model;
        }
    }
}
