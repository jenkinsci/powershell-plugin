package hudson.plugins.powershell;

import jenkins.model.Jenkins;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PowerShellInstallationTest {

    @Test
    public void testToolInstallationFromXml() {
        String xml = """
                    <hudson.plugins.powershell.PowerShellInstallation_-DescriptorImpl plugin="powershell@2.4-SNAPSHOT">
                      <installations class="hudson.plugins.powershell.PowerShellInstallation-array">
                        <hudson.plugins.powershell.PowerShellInstallation>
                          <name>DefaultWindows</name>
                          <home>powershell.exe</home>
                          <properties/>
                        </hudson.plugins.powershell.PowerShellInstallation>
                        <hudson.plugins.powershell.PowerShellInstallation>
                          <name>DefaultLinux</name>
                          <home>pwsh</home>
                          <properties/>
                        </hudson.plugins.powershell.PowerShellInstallation>
                        <hudson.plugins.powershell.PowerShellInstallation>
                          <name>powershell-7.5.5-linux-x64</name>
                          <home></home>
                          <properties>
                            <hudson.tools.InstallSourceProperty>
                              <installers>
                                <hudson.tools.ZipExtractionInstaller>
                                  <url>https://github.com/PowerShell/PowerShell/releases/download/v7.5.5/powershell-7.5.5-linux-x64.tar.gz</url>
                                </hudson.tools.ZipExtractionInstaller>
                              </installers>
                            </hudson.tools.InstallSourceProperty>
                          </properties>
                        </hudson.plugins.powershell.PowerShellInstallation>
                        <hudson.plugins.powershell.PowerShellInstallation>
                          <name>snap</name>
                          <home>/snap/bin/pwsh</home>
                          <properties/>
                        </hudson.plugins.powershell.PowerShellInstallation>
                        <hudson.plugins.powershell.PowerShellInstallation>
                          <name>Windows 1</name>
                          <home>c:\\powershell1\\powershell.exe</home>
                          <properties/>
                        </hudson.plugins.powershell.PowerShellInstallation>
                      </installations>
                    </hudson.plugins.powershell.PowerShellInstallation_-DescriptorImpl>
                """;

        final var descriptor = (PowerShellInstallation.DescriptorImpl) Jenkins.XSTREAM2.fromXML(xml);

        assertAll(
                () -> assertEquals(5, descriptor.getInstallations().length),

                () -> assertEquals("powershell.exe", descriptor.getInstallation("DefaultWindows").getHome()),
                () -> assertEquals("powershell.exe", descriptor.getInstallation("DefaultWindows").getPowerShellBinary()),
                () -> assertEquals(0, descriptor.getInstallation("DefaultWindows").getProperties().size()),

                () -> assertEquals("pwsh", descriptor.getInstallation("DefaultLinux").getHome()),
                () -> assertEquals("pwsh", descriptor.getInstallation("DefaultLinux").getPowerShellBinary()),
                () -> assertEquals(0, descriptor.getInstallation("DefaultLinux").getProperties().size()),

                () -> assertEquals("", descriptor.getInstallation("powershell-7.5.5-linux-x64").getHome()),
                () -> assertEquals("", descriptor.getInstallation("powershell-7.5.5-linux-x64").getPowerShellBinary()),
                () -> assertEquals(1, descriptor.getInstallation("powershell-7.5.5-linux-x64").getProperties().size()),

                () -> assertEquals("/snap/bin/pwsh", descriptor.getInstallation("snap").getHome()),
                () -> assertEquals("/snap/bin/pwsh", descriptor.getInstallation("snap").getPowerShellBinary()),
                () -> assertEquals(0, descriptor.getInstallation("snap").getProperties().size()),

                () -> assertEquals("c:\\powershell1\\powershell.exe", descriptor.getInstallation("Windows 1").getHome()),
                () -> assertEquals("c:\\powershell1\\powershell.exe", descriptor.getInstallation("Windows 1").getPowerShellBinary()),
                () -> assertEquals(0, descriptor.getInstallation("Windows 1").getProperties().size())
        );
    }
}
