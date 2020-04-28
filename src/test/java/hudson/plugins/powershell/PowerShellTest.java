package hudson.plugins.powershell;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.FilePath;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.queue.QueueTaskFuture;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.Assert;
import org.apache.commons.lang.SystemUtils;

/**
 * @author Kohsuke Kawaguchi
 */
public class PowerShellTest {

    @Rule
    public JenkinsRule r = new JenkinsRule();

    @Test
    public void testConfigRoundtrip() throws Exception {
        FreeStyleProject p = r.createFreeStyleProject();
        PowerShell orig = new PowerShell("script", true, true, null);
        p.getBuildersList().add(orig);

        JenkinsRule.WebClient webClient = r.createWebClient();
        HtmlPage page = webClient.getPage(p, "configure");
        HtmlForm form = page.getFormByName("config");
        r.submit(form);

        r.assertEqualBeans(orig, p.getBuildersList().get(PowerShell.class), "command");
    }

    @Test
    public void testBuildSuccess() throws Exception {
        Assume.assumeTrue(isPowerShellAvailable());

        FreeStyleProject project1 = r.createFreeStyleProject("project1");
        project1.getBuildersList().add(new PowerShell("echo 'Hello World!'", true, true, null));

        QueueTaskFuture<FreeStyleBuild> freeStyleBuildQueueTaskFuture = project1.scheduleBuild2(0);
        FreeStyleBuild build = freeStyleBuildQueueTaskFuture.get();

        r.assertBuildStatusSuccess(build);
    }

    @Test
    public void testBuildBadCommandFails() throws Exception {
        Assume.assumeTrue(isPowerShellAvailable());
        FreeStyleProject project1 = r.createFreeStyleProject("project1");
        project1.getBuildersList().add(new PowerShell("wrong command", true, true, null));

        QueueTaskFuture<FreeStyleBuild> freeStyleBuildQueueTaskFuture = project1.scheduleBuild2(0);
        FreeStyleBuild build = freeStyleBuildQueueTaskFuture.get();

        r.assertBuildStatus(Result.FAILURE, build);
    }

    @Test
    public void testBuildBadCommandsSucceeds() throws Exception {
        Assume.assumeTrue(isPowerShellAvailable());
        FreeStyleProject project1 = r.createFreeStyleProject("project1");
        project1.getBuildersList().add(new PowerShell("wrong command", false, true, null));

        QueueTaskFuture<FreeStyleBuild> freeStyleBuildQueueTaskFuture = project1.scheduleBuild2(0);
        FreeStyleBuild build = freeStyleBuildQueueTaskFuture.get();
        
        r.assertBuildStatus(Result.SUCCESS, build);
    }

    /**
     * Tests to ensure the correct executable is used
     * Requires tests to be run on Windows and Linux due to {@link PowerShell#isRunningOnWindows(hudson.FilePath)} local vs remote detection
     * @throws Exception 
     */
    @Test
    public void testBuildCommandLineOSBased() throws Exception {
        PowerShell powershell = new PowerShell("echo 'Hello World!'", true, true, "osBased");
        
        String lineSeperator = System.lineSeparator();

        String[] cmdLine = powershell.buildCommandLine(new FilePath(new File(System.lineSeparator() + "Test" + System.lineSeparator() + "fake.ps1")));        
        if(SystemUtils.IS_OS_WINDOWS) {
            Assert.assertEquals("powershell.exe", cmdLine[0]);
        } else {
            Assert.assertEquals("pwsh", cmdLine[0]);
        }
    }
    
    @Test
    public void testBuildCommandLineWindowsPowerShell() throws Exception {
        PowerShell powershell = new PowerShell("echo 'Hello World!'", true, true, "windowsPowerShell");
        
        String lineSeperator = System.lineSeparator();

        String[] cmdLine = powershell.buildCommandLine(new FilePath(new File(System.lineSeparator() + "Test" + System.lineSeparator() + "fake.ps1")));        
        Assert.assertEquals("powershell.exe", cmdLine[0]);
    }
    
    @Test
    public void testBuildCommandLinePowerShellCore() throws Exception {
        PowerShell powershell = new PowerShell("echo 'Hello World!'", true, true, "powershellCore");
        
        String lineSeperator = System.lineSeparator();

        String[] cmdLine = powershell.buildCommandLine(new FilePath(new File(System.lineSeparator() + "Test" + System.lineSeparator() + "fake.ps1")));        
        Assert.assertEquals("pwsh", cmdLine[0]);
    }
    
    private boolean isPowerShellAvailable() {
        return Stream.of(System.getenv("PATH").split(Pattern.quote(File.pathSeparator)))
                .map(Paths::get)
                .anyMatch(path -> Files.exists(path.resolve("pwsh")) || Files.exists(path.resolve("powershell")));
    }

}
