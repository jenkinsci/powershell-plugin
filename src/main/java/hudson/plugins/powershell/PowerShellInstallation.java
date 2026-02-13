package hudson.plugins.powershell;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Util;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.EnvironmentSpecific;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolProperty;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest2;

import java.io.IOException;
import java.io.Serial;
import java.lang.reflect.Array;
import java.util.List;

public class PowerShellInstallation extends ToolInstallation implements NodeSpecific<PowerShellInstallation>,
        EnvironmentSpecific<PowerShellInstallation> {

    static final String DEFAULT_WINDOWS_NAME = "DefaultWindows";

    static final String DEFAULT_LINUX_NAME = "DefaultLinux";

    private static final String DEFAULT_WINDOWS_EXECUTABLE = "powershell.exe";

    private static final String DEFAULT_LINUX_EXECUTABLE = "pwsh";

    @Serial
    private static final long serialVersionUID = 1;

    /**
     * Originally, {@link hudson.tools.ToolInstallation#home} was the only field used to indicate both installation
     * directory and executable file.
     * <p>
     * This was split into two fields, but {@link hudson.tools.ToolInstallation#home} is private with no setter and
     * could not be reused, leading to {@link PowerShellInstallation#powershellHome}.
     * <p>
     * In a future version, this field could be migrated back into {@link hudson.tools.ToolInstallation#home}, removing
     * {@link PowerShellInstallation#powershellHome}.
     */
    private /*almost final*/ String powershellHome;
    private /*almost final*/ String executable;

    @DataBoundConstructor
    public PowerShellInstallation(String name, String powershellHome, String executable, List<? extends ToolProperty<?>> properties) {
        super(name, null, properties);
        this.powershellHome = Util.fixEmptyAndTrim(powershellHome);
        this.executable = executable;
    }

    @Override
    public PowerShellInstallation forNode(@NonNull Node node, TaskListener log) throws IOException, InterruptedException {
        return new PowerShellInstallation(getName(), translateFor(node, log), executable, getProperties());
    }

    @Override
    public PowerShellInstallation forEnvironment(EnvVars environment) {
        return new PowerShellInstallation(getName(), environment.expand(getHome()), executable, getProperties());
    }

    public static String getDefaultPowershellWhenNoConfiguration(Boolean isRunningOnWindows) {
        if (isRunningOnWindows) {
            return DEFAULT_WINDOWS_EXECUTABLE;
        }
        else {
            return DEFAULT_LINUX_EXECUTABLE;
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

        PowerShellInstallation windowsInstallation = new PowerShellInstallation(DEFAULT_WINDOWS_NAME, null, DEFAULT_WINDOWS_EXECUTABLE, null);
        PowerShellInstallation linuxInstallation = new PowerShellInstallation(DEFAULT_LINUX_NAME, null, DEFAULT_LINUX_EXECUTABLE, null);
        descriptor.setInstallations(windowsInstallation, linuxInstallation);
        descriptor.save();
    }

    public String getPowershellHome() {
        return powershellHome;
    }

    @Override
    public String getHome() {
        return powershellHome;
    }

    public String getPowerShellBinary() {
        return executable;
    }

    public String getExecutable() {
        return executable;
    }

    @Serial
    @Override
    protected Object readResolve() {
        if (this.executable == null) {
            final var home = super.getHome();
            if (home == null) {
                this.executable = DEFAULT_LINUX_EXECUTABLE;
                this.powershellHome = null;
            } else if (DEFAULT_LINUX_EXECUTABLE.equals(home) || DEFAULT_WINDOWS_EXECUTABLE.equals(home)) {
                this.executable = home;
                this.powershellHome = null;
            } else if (home.endsWith(DEFAULT_LINUX_EXECUTABLE)) {
                this.executable = DEFAULT_LINUX_EXECUTABLE;
                this.powershellHome = home.substring(0, home.length() - DEFAULT_LINUX_EXECUTABLE.length() - 1);
            } else if (home.endsWith(DEFAULT_WINDOWS_EXECUTABLE)) {
                this.executable = DEFAULT_WINDOWS_EXECUTABLE;
                this.powershellHome = home.substring(0, home.length() - DEFAULT_WINDOWS_EXECUTABLE.length() - 1);
            } else {
                this.executable = DEFAULT_LINUX_EXECUTABLE;
                this.powershellHome = home;
            }
            this.executable = Util.fixEmptyAndTrim(this.executable);
            this.powershellHome = Util.fixEmptyAndTrim(this.powershellHome);
        }

        return super.readResolve();
    }

    @Extension
    public static class DescriptorImpl extends ToolDescriptor<PowerShellInstallation>
    {
        public DescriptorImpl() {
            super();
            load();
        }

        @Override
        @NonNull
        public String getDisplayName() {
            return "PowerShell";
        }

        @Override
        public boolean configure(StaplerRequest2 req, JSONObject json) {
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