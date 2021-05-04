package hudson.plugins.powershell;

import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.EnvVars;
import hudson.Extension;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.EnvironmentSpecific;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.lang.reflect.Array;
import javax.annotation.Nonnull;

public class PowerShellInstallation extends ToolInstallation implements NodeSpecific<PowerShellInstallation>,
        EnvironmentSpecific<PowerShellInstallation> {

    public static transient final String DEFAULTWINDOWS = "DefaultWindows";

    public static transient final String DEFAULTLINUX = "DefaultLinux";


    private static final long serialVersionUID = 1;

    @DataBoundConstructor
    public PowerShellInstallation(String name, String home) {
        super(name, home, null);
    }

    @Override
    public PowerShellInstallation forNode(Node node, TaskListener log) throws IOException, InterruptedException {
        return new PowerShellInstallation(getName(), translateFor(node, log));
    }

    @Override
    public PowerShellInstallation forEnvironment(EnvVars environment) {
        return new PowerShellInstallation(getName(), environment.expand(getHome()));
    }

    public static String getDefaultPowershellWhenNoConfiguration(Boolean isRunningOnWindows) {
        if (isRunningOnWindows) {
            return "powershell.exe";
        }
        else {
            return "pwsh";
        }
    }

    @Initializer(after = InitMilestone.EXTENSIONS_AUGMENTED)
    public static void onLoaded() {
        DescriptorImpl descriptor = (PowerShellInstallation.DescriptorImpl) Jenkins.get().getDescriptor(PowerShellInstallation.class);
        assert descriptor != null;
        PowerShellInstallation[] installations = descriptor.getInstallations();
        if (installations != null && installations.length > 0) {
            return;
        }
        String defaultPowershell = "powershell.exe";
        PowerShellInstallation windowsInstallation = new PowerShellInstallation(DEFAULTWINDOWS, defaultPowershell);
        defaultPowershell = "pwsh";
        PowerShellInstallation linuxInstallation = new PowerShellInstallation(DEFAULTLINUX, defaultPowershell);
        PowerShellInstallation[] defaultInstallations = { windowsInstallation, linuxInstallation};
        descriptor.setInstallations(defaultInstallations);
        descriptor.save();
    }

    public String getPowerShellBinary() {
        return getHome();
    }

    @Extension
    public static class DescriptorImpl extends ToolDescriptor<PowerShellInstallation>
    {
        public DescriptorImpl() {
            super();
            load();
        }

        @Override
        @Nonnull
        public String getDisplayName() {
            return "PowerShell";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) {
            setInstallations(req.bindJSONToList(PowerShellInstallation.class, json.get("tool"))
                    .toArray((PowerShellInstallation[]) Array.newInstance(PowerShellInstallation.class, 0)));
            save();
            return true;
        }

        @Nullable
        public PowerShellInstallation getInstallation(String name) {
            for (PowerShellInstallation i : getInstallations()) {
                if (i.getName().equals(name)) {
                    return i;
                }
            }
            return null;
        }

        @Nullable
        public PowerShellInstallation getAnyInstallation(String name) {
            PowerShellInstallation defaultInstallation = getInstallation(name);
            if (defaultInstallation != null) {
                return defaultInstallation;
            }
            else if (getInstallations().length > 0) {
                return getInstallations()[0];
            }
            return null;
        }
    }
}