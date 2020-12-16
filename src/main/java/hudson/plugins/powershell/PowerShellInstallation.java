package hudson.plugins.powershell;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.EnvironmentSpecific;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolInstallation;
import org.kohsuke.stapler.DataBoundConstructor;
import jenkins.security.MasterToSlaveCallable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class PowerShellInstallation extends ToolInstallation implements NodeSpecific<PowerShellInstallation>,
        EnvironmentSpecific<PowerShellInstallation> {

    @DataBoundConstructor
    public PowerShellInstallation(String name, String home) {
        super(name, home, null);
    }

    public PowerShellInstallation forNode(Node node, TaskListener log) throws IOException, InterruptedException {
        return new PowerShellInstallation(getName(), translateFor(node, log));
    }

    public PowerShellInstallation forEnvironment(EnvVars environment) {
        return new PowerShellInstallation(getName(), environment.expand(getHome()));
    }

    public String getPowershell(Boolean isRunningOnWindows, Boolean isRemote, Boolean prioritizePowerShellCore, Launcher launcher) {
        if (isRunningOnWindows && prioritizePowerShellCore && isPowerShellVersionAvailable("pwsh.exe", isRemote, launcher))
        {
            return "pwsh.exe";
        }
        else if (isRunningOnWindows)
        {
            return "powershell.exe";
        }
        else
        {
            return "pwsh";
        }
    }

    private Boolean isPowerShellVersionAvailable(String powerShellVersion, Boolean isRemote, Launcher launcher)
    {
        if (!isRemote)
        {
            return Stream.of(System.getenv("PATH").split(Pattern.quote(File.pathSeparator)))
                    .map(Paths::get)
                    .anyMatch(path -> Files.exists(path.resolve(powerShellVersion)));
        }
        else
        {
            try
            {
                VirtualChannel channel = launcher.getChannel();
                return (channel == null) ? false : channel.call(new PowerShellChecker(powerShellVersion));
            }
            catch(Exception e)
            {
                return false;
            }
        }
    }

    private static class PowerShellChecker extends MasterToSlaveCallable<Boolean, Exception> {

        private static final long serialVersionUID = 1;

        private String powerShellVersion;

        public PowerShellChecker(String powerShellVersion) {
            this.powerShellVersion = powerShellVersion;
        }

        @Override
        public Boolean call() throws Exception {
            return Stream.of(System.getenv("PATH").split(Pattern.quote(File.pathSeparator)))
                    .map(Paths::get)
                    .anyMatch(path -> Files.exists(path.resolve(powerShellVersion)));
        }
    }
}