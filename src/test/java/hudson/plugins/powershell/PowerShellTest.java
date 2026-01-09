package hudson.plugins.powershell;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.queue.QueueTaskFuture;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * @author Kohsuke Kawaguchi
 */
@WithJenkins
public class PowerShellTest {

    @Test
    void testConfigRoundtrip(JenkinsRule r) throws Exception {
        FreeStyleProject p = r.createFreeStyleProject();
        PowerShell orig = new PowerShell("script", true, true, null);
        p.getBuildersList().add(orig);

        try (JenkinsRule.WebClient webClient = r.createWebClient()) {
            HtmlPage page = webClient.getPage(p, "configure");
            HtmlForm form = page.getFormByName("config");
            r.submit(form);
        }

        r.assertEqualBeans(orig, p.getBuildersList().get(PowerShell.class), "command");
    }

    @Test
    void testBuildSuccess(JenkinsRule r) throws Exception {
        Assumptions.assumeTrue(isPowerShellAvailable());

        FreeStyleProject project1 = r.createFreeStyleProject("project1");
        project1.getBuildersList().add(new PowerShell("echo 'Hello World!'", true, true, null));

        QueueTaskFuture<FreeStyleBuild> freeStyleBuildQueueTaskFuture = project1.scheduleBuild2(0);
        FreeStyleBuild build = freeStyleBuildQueueTaskFuture.get();

        r.assertBuildStatusSuccess(build);
    }

    @Test
    void testBuildParameterBlockWithStopFails(JenkinsRule r) throws Exception {
        Assumptions.assumeTrue(isPowerShellAvailable());

        FreeStyleProject project1 = r.createFreeStyleProject("project1");
        project1.getBuildersList().add(new PowerShell("""
                param(
                    [Parameter()][String] $Param1 = "this parameter #1",
                    [Parameter()][String] $Param2 = "this parameter #2"
                )
                
                Write-Host $Param1
                Write-Host $Param2""", true, true, null));

        QueueTaskFuture<FreeStyleBuild> freeStyleBuildQueueTaskFuture = project1.scheduleBuild2(0);
        FreeStyleBuild build = freeStyleBuildQueueTaskFuture.get();

        r.assertBuildStatus(Result.FAILURE, build);
    }

    @Test
    void testBuildParameterBlockWithoutStopSucceeds(JenkinsRule r) throws Exception {
        Assumptions.assumeTrue(isPowerShellAvailable());

        FreeStyleProject project1 = r.createFreeStyleProject("project1");
        project1.getBuildersList().add(new PowerShell("""
                param(
                    [Parameter()][String] $Param1 = "this parameter #1",
                    [Parameter()][String] $Param2 = "this parameter #2"
                )
                
                Write-Host $Param1
                Write-Host $Param2""", false, true, null));

        QueueTaskFuture<FreeStyleBuild> freeStyleBuildQueueTaskFuture = project1.scheduleBuild2(0);
        FreeStyleBuild build = freeStyleBuildQueueTaskFuture.get();

        r.assertBuildStatusSuccess(build);
    }

    @Test
    void testBuildBadCommandFails(JenkinsRule r) throws Exception {
        Assumptions.assumeTrue(isPowerShellAvailable());
        FreeStyleProject project1 = r.createFreeStyleProject("project1");
        project1.getBuildersList().add(new PowerShell("wrong command", true, true, null));

        QueueTaskFuture<FreeStyleBuild> freeStyleBuildQueueTaskFuture = project1.scheduleBuild2(0);
        FreeStyleBuild build = freeStyleBuildQueueTaskFuture.get();

        r.assertBuildStatus(Result.FAILURE, build);
    }

    @Test
    void testBuildBadCommandsSucceeds(JenkinsRule r) throws Exception {
        Assumptions.assumeTrue(isPowerShellAvailable());
        FreeStyleProject project1 = r.createFreeStyleProject("project1");
        project1.getBuildersList().add(new PowerShell("wrong command", false, true, null));

        QueueTaskFuture<FreeStyleBuild> freeStyleBuildQueueTaskFuture = project1.scheduleBuild2(0);
        FreeStyleBuild build = freeStyleBuildQueueTaskFuture.get();

        r.assertBuildStatus(Result.SUCCESS, build);
    }

    @Test
    void testBuildAndDisableProject(JenkinsRule r) throws Exception {
        Assumptions.assumeTrue(isPowerShellAvailable());

        FreeStyleProject project1 = r.createFreeStyleProject("project1");
        project1.getBuildersList().add(new PowerShell("echo 'Hello World!'", true, true, null));

        QueueTaskFuture<FreeStyleBuild> freeStyleBuildQueueTaskFuture = project1.scheduleBuild2(0);
        FreeStyleBuild build = freeStyleBuildQueueTaskFuture.get();

        r.assertBuildStatusSuccess(build);

        project1.disable();
    }

    @Test
    void testBuildUnstableEnabledSucceeds(JenkinsRule r) throws Exception {
        Assumptions.assumeTrue(isPowerShellAvailable());
        FreeStyleProject project1 = r.createFreeStyleProject("project1");
        project1.getBuildersList().add(new PowerShell("exit 0", true, true, 123));

        QueueTaskFuture<FreeStyleBuild> freeStyleBuildQueueTaskFuture = project1.scheduleBuild2(0);
        FreeStyleBuild build = freeStyleBuildQueueTaskFuture.get();

        r.assertBuildStatus(Result.SUCCESS, build);
    }

    @Test
    void testBuildUnstableEnabledBadCommandFails(JenkinsRule r) throws Exception {
        Assumptions.assumeTrue(isPowerShellAvailable());
        FreeStyleProject project1 = r.createFreeStyleProject("project1");
        project1.getBuildersList().add(new PowerShell("exit 1", true, true, 123));

        QueueTaskFuture<FreeStyleBuild> freeStyleBuildQueueTaskFuture = project1.scheduleBuild2(0);
        FreeStyleBuild build = freeStyleBuildQueueTaskFuture.get();

        r.assertBuildStatus(Result.FAILURE, build);
    }

    @Test
    void testBuildUnstableEnabledBadCommandUnstableErrorCode(JenkinsRule r) throws Exception {
        Assumptions.assumeTrue(isPowerShellAvailable());
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
}
