package hudson.plugins.powershell;

import hudson.EnvVars;
import hudson.model.EnvironmentSpecific;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolInstallation;
import org.kohsuke.stapler.DataBoundConstructor;

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

    public String getPowershell(Boolean isRunningOnWindows, Boolean prioritizePowerShellCore) {
        if (isRunningOnWindows && prioritizePowerShellCore && isPowerShellAvailable("pwsh.exe"))
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

    private boolean isPowerShellAvailable(String powerShellVersion)
    {
        return Stream.of(System.getenv("PATH").split(Pattern.quote(File.pathSeparator)))
                .map(Paths::get)
                .anyMatch(path -> Files.exists(path.resolve(powerShellVersion)));
    }
}
