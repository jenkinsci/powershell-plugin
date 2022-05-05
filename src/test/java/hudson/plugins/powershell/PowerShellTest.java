package hudson.plugins.powershell;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.model.Computer;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.queue.QueueTaskFuture;
import hudson.tasks.BatchFile;
import hudson.tools.ToolLocationNodeProperty;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

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

    @Test
    public void testBuildAndDisableProject() throws Exception {
        Assume.assumeTrue(isPowerShellAvailable());

        FreeStyleProject project1 = r.createFreeStyleProject("project1");
        project1.getBuildersList().add(new PowerShell("echo 'Hello World!'", true, true, null));

        QueueTaskFuture<FreeStyleBuild> freeStyleBuildQueueTaskFuture = project1.scheduleBuild2(0);
        FreeStyleBuild build = freeStyleBuildQueueTaskFuture.get();

        r.assertBuildStatusSuccess(build);

        project1.disable();
    }

    @Test
    public void testBuildUnstableEnabledSucceeds() throws Exception {
        Assume.assumeTrue(isPowerShellAvailable());
        FreeStyleProject project1 = r.createFreeStyleProject("project1");
        project1.getBuildersList().add(new PowerShell("exit 0", true, true, 123));

        QueueTaskFuture<FreeStyleBuild> freeStyleBuildQueueTaskFuture = project1.scheduleBuild2(0);
        FreeStyleBuild build = freeStyleBuildQueueTaskFuture.get();

        r.assertBuildStatus(Result.SUCCESS, build);
    }

    @Test
    public void testBuildUnstableEnabledBadCommandFails() throws Exception {
        Assume.assumeTrue(isPowerShellAvailable());
        FreeStyleProject project1 = r.createFreeStyleProject("project1");
        project1.getBuildersList().add(new PowerShell("exit 1", true, true, 123));

        QueueTaskFuture<FreeStyleBuild> freeStyleBuildQueueTaskFuture = project1.scheduleBuild2(0);
        FreeStyleBuild build = freeStyleBuildQueueTaskFuture.get();

        r.assertBuildStatus(Result.FAILURE, build);
    }

    @Test
    public void testBuildUnstableEnabledBadCommandUnstableErrorCode() throws Exception {
        Assume.assumeTrue(isPowerShellAvailable());
        FreeStyleProject project1 = r.createFreeStyleProject("project1");
        project1.getBuildersList().add(new PowerShell("exit 123", true, true, 123));

        QueueTaskFuture<FreeStyleBuild> freeStyleBuildQueueTaskFuture = project1.scheduleBuild2(0);
        FreeStyleBuild build = freeStyleBuildQueueTaskFuture.get();

        r.assertBuildStatus(Result.UNSTABLE, build);
    }

    private boolean isPowerShellAvailable() {
        return Stream.of(System.getenv("PATH").split(Pattern.quote(File.pathSeparator)))
                .map(Paths::get)
                .anyMatch(path ->
                        Files.exists(path.resolve("pwsh")) ||
                                Files.exists(path.resolve("pwsh.exe")) ||
                                Files.exists(path.resolve("powershell")) ||
                                Files.exists(path.resolve("powershell.exe")));
    }

    @Test
    public void testBuildEncodingOnWindowsWithPowerShellCore() throws Exception {
        Computer computer = r.jenkins.getComputer("");
        Assume.assumeTrue(Boolean.FALSE.equals(computer.isUnix()));

        PowerShellInstallation.DescriptorImpl descriptor =
                r.jenkins.getDescriptorByType(PowerShellInstallation.DescriptorImpl.class);
        ToolLocationNodeProperty.ToolLocation toolLocation
                = new ToolLocationNodeProperty.ToolLocation(descriptor,
                "DefaultWindows",
                "C:\\Program Files\\PowerShell\\7\\pwsh.exe");
        ToolLocationNodeProperty toolLocationNodeProperty = new ToolLocationNodeProperty(toolLocation);
        computer.getNode().getNodeProperties().add(toolLocationNodeProperty);

        FreeStyleProject project1 = r.createFreeStyleProject("project1");
        project1.getBuildersList().add(new PowerShell("Write-Host \"UTF-8 Sign: ✅\"", true, false, 123));

        QueueTaskFuture<FreeStyleBuild> freeStyleBuildQueueTaskFuture = project1.scheduleBuild2(0);
        FreeStyleBuild build = freeStyleBuildQueueTaskFuture.get();

        r.assertLogContains("UTF-8 Sign: ✅", build);
        r.assertBuildStatusSuccess(build);
    }

    @Test
    public void testBuildEncodingOnWindowsWithBatch() throws Exception {
        Computer computer = r.jenkins.getComputer("");
        Assume.assumeTrue(Boolean.FALSE.equals(computer.isUnix()));

        FreeStyleProject project1 = r.createFreeStyleProject("project1");
        project1.getBuildersList()
                .add(new BatchFile("pwsh.exe -NonInteractive -ExecutionPolicy Bypass -Command \"Write-Host UTF-8 Sign: ✅\""));

        QueueTaskFuture<FreeStyleBuild> freeStyleBuildQueueTaskFuture = project1.scheduleBuild2(0);
        FreeStyleBuild build = freeStyleBuildQueueTaskFuture.get();

        r.assertLogContains("UTF-8 Sign: ✅", build);
        r.assertBuildStatusSuccess(build);
    }
}
