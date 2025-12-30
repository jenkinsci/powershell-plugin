package hudson.plugins.powershell;

import java.util.Collections;

import jenkins.model.Jenkins;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@WithJenkins
class PowerShellInstallationTest {

    @Test
    void testConfigRoundtrip(JenkinsRule r) throws Exception {

        PowerShellInstallation.DescriptorImpl descriptor = r.jenkins.getDescriptorByType(PowerShellInstallation.DescriptorImpl.class);
        descriptor.setInstallations(new PowerShellInstallation("installation1", "home1", "pwsh1", Collections.emptyList()));

        try (JenkinsRule.WebClient webClient = r.createWebClient()) {
            HtmlPage page = webClient.goTo("manage/configureTools");
            HtmlForm form = page.getFormByName("config");
            r.submit(form);
        }

        assertEquals("pwsh1", descriptor.getInstallation("installation1").getExecutable());
        assertEquals("pwsh1", descriptor.getInstallation("installation1").getPowerShellBinary());
        assertEquals("home1", descriptor.getInstallation("installation1").getPowershellHome());
        assertEquals("home1", descriptor.getInstallation("installation1").getHome());

    }

    @Test
    public void testReadResolveOldConfig() {
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

                () -> assertNull(descriptor.getInstallation("DefaultWindows").getHome()),
                () -> assertEquals("powershell.exe", descriptor.getInstallation("DefaultWindows").getPowerShellBinary()),
                () -> assertEquals(0, descriptor.getInstallation("DefaultWindows").getProperties().size()),

                () -> assertNull(descriptor.getInstallation("DefaultLinux").getHome()),
                () -> assertEquals("pwsh", descriptor.getInstallation("DefaultLinux").getPowerShellBinary()),
                () -> assertEquals(0, descriptor.getInstallation("DefaultLinux").getProperties().size()),

                () -> assertNull(descriptor.getInstallation("powershell-7.5.5-linux-x64").getHome()),
                () -> assertEquals("pwsh", descriptor.getInstallation("powershell-7.5.5-linux-x64").getPowerShellBinary()),
                () -> assertEquals(1, descriptor.getInstallation("powershell-7.5.5-linux-x64").getProperties().size()),

                () -> assertEquals("/snap/bin", descriptor.getInstallation("snap").getHome()),
                () -> assertEquals("pwsh", descriptor.getInstallation("snap").getPowerShellBinary()),
                () -> assertEquals(0, descriptor.getInstallation("snap").getProperties().size()),

                () -> assertEquals("c:\\powershell1", descriptor.getInstallation("Windows 1").getHome()),
                () -> assertEquals("powershell.exe", descriptor.getInstallation("Windows 1").getPowerShellBinary()),
                () -> assertEquals(0, descriptor.getInstallation("Windows 1").getProperties().size())
        );

        final var xmlAfter = Jenkins.XSTREAM2.toXML(descriptor);
        assertEquals("""
                    <hudson.plugins.powershell.PowerShellInstallation_-DescriptorImpl>
                      <installations class="hudson.plugins.powershell.PowerShellInstallation-array">
                        <hudson.plugins.powershell.PowerShellInstallation>
                          <name>DefaultWindows</name>
                          <home>powershell.exe</home>
                          <properties/>
                          <executable>powershell.exe</executable>
                        </hudson.plugins.powershell.PowerShellInstallation>
                        <hudson.plugins.powershell.PowerShellInstallation>
                          <name>DefaultLinux</name>
                          <home>pwsh</home>
                          <properties/>
                          <executable>pwsh</executable>
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
                          <executable>pwsh</executable>
                        </hudson.plugins.powershell.PowerShellInstallation>
                        <hudson.plugins.powershell.PowerShellInstallation>
                          <name>snap</name>
                          <home>/snap/bin/pwsh</home>
                          <properties/>
                          <powershellHome>/snap/bin</powershellHome>
                          <executable>pwsh</executable>
                        </hudson.plugins.powershell.PowerShellInstallation>
                        <hudson.plugins.powershell.PowerShellInstallation>
                          <name>Windows 1</name>
                          <home>c:\\powershell1\\powershell.exe</home>
                          <properties/>
                          <powershellHome>c:\\powershell1</powershellHome>
                          <executable>powershell.exe</executable>
                        </hudson.plugins.powershell.PowerShellInstallation>
                      </installations>
                    </hudson.plugins.powershell.PowerShellInstallation_-DescriptorImpl>""", xmlAfter);
    }

    @Test
    public void testToolInstallationFromXml() {
        String xml = """
                    <hudson.plugins.powershell.PowerShellInstallation_-DescriptorImpl plugin="powershell@999999-SNAPSHOT">
                      <installations class="hudson.plugins.powershell.PowerShellInstallation-array">
                        <hudson.plugins.powershell.PowerShellInstallation>
                          <name>DefaultWindows</name>
                          <properties/>
                          <executable>powershell.exe</executable>
                        </hudson.plugins.powershell.PowerShellInstallation>
                        <hudson.plugins.powershell.PowerShellInstallation>
                          <name>DefaultLinux</name>
                          <properties/>
                          <executable>pwsh</executable>
                        </hudson.plugins.powershell.PowerShellInstallation>
                        <hudson.plugins.powershell.PowerShellInstallation>
                          <name>powershell-7.5.5-linux-x64</name>
                          <properties>
                            <hudson.tools.InstallSourceProperty>
                              <installers>
                                <hudson.tools.ZipExtractionInstaller>
                                  <url>https://github.com/PowerShell/PowerShell/releases/download/v7.5.5/powershell-7.5.5-linux-x64.tar.gz</url>
                                </hudson.tools.ZipExtractionInstaller>
                              </installers>
                            </hudson.tools.InstallSourceProperty>
                          </properties>
                          <executable>pwsh</executable>
                        </hudson.plugins.powershell.PowerShellInstallation>
                        <hudson.plugins.powershell.PowerShellInstallation>
                          <name>snap</name>
                          <properties/>
                          <powershellHome>/snap/bin</powershellHome>
                          <executable>pwsh</executable>
                        </hudson.plugins.powershell.PowerShellInstallation>
                        <hudson.plugins.powershell.PowerShellInstallation>
                          <name>Windows 1</name>
                          <properties/>
                          <powershellHome>c:\\powershell1</powershellHome>
                          <executable>powershell.exe</executable>
                        </hudson.plugins.powershell.PowerShellInstallation>
                      </installations>
                    </hudson.plugins.powershell.PowerShellInstallation_-DescriptorImpl>
                """;

        final var descriptor = (PowerShellInstallation.DescriptorImpl) Jenkins.XSTREAM2.fromXML(xml);

        assertAll(
                () -> assertEquals(5, descriptor.getInstallations().length),

                () -> assertNull(descriptor.getInstallation("DefaultWindows").getHome()),
                () -> assertEquals("powershell.exe", descriptor.getInstallation("DefaultWindows").getPowerShellBinary()),
                () -> assertEquals(0, descriptor.getInstallation("DefaultWindows").getProperties().size()),

                () -> assertNull(descriptor.getInstallation("DefaultLinux").getHome()),
                () -> assertEquals("pwsh", descriptor.getInstallation("DefaultLinux").getPowerShellBinary()),
                () -> assertEquals(0, descriptor.getInstallation("DefaultLinux").getProperties().size()),

                () -> assertNull(descriptor.getInstallation("powershell-7.5.5-linux-x64").getHome()),
                () -> assertEquals("pwsh", descriptor.getInstallation("powershell-7.5.5-linux-x64").getPowerShellBinary()),
                () -> assertEquals(1, descriptor.getInstallation("powershell-7.5.5-linux-x64").getProperties().size()),

                () -> assertEquals("/snap/bin", descriptor.getInstallation("snap").getHome()),
                () -> assertEquals("pwsh", descriptor.getInstallation("snap").getPowerShellBinary()),
                () -> assertEquals(0, descriptor.getInstallation("snap").getProperties().size()),

                () -> assertEquals("c:\\powershell1", descriptor.getInstallation("Windows 1").getHome()),
                () -> assertEquals("powershell.exe", descriptor.getInstallation("Windows 1").getPowerShellBinary()),
                () -> assertEquals(0, descriptor.getInstallation("Windows 1").getProperties().size())
        );
    }
}
