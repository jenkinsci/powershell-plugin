package hudson.plugins.powershell;

import org.jvnet.hudson.test.HudsonTestCase;
import hudson.model.FreeStyleProject;

/**
 * @author Kohsuke Kawaguchi
 */
public class PowerShellTest extends HudsonTestCase {
    public void testConfigRoundtrip() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        PowerShell orig = new PowerShell("script");
        p.getBuildersList().add(orig);
        submit(new WebClient().getPage(p,"configure").getFormByName("config"));

        assertEqualBeans(orig, p.getBuildersList().get(PowerShell.class), "command");
    }
}
