Jenkins PowerShell Plugin
=========================

[![Jenkins Plugins](https://img.shields.io/jenkins/plugin/v/powershell)](https://github.com/jenkinsci/powershell-plugin/releases)
[![Jenkins Plugin installs](https://img.shields.io/jenkins/plugin/i/powershell)](https://plugins.jenkins.io/powershell)
[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/powershell-plugin/master)](https://ci.jenkins.io/blue/organizations/jenkins/Plugins%2Fpowershell-plugin/branches)
[![javadoc](https://img.shields.io/badge/javadoc-available-brightgreen.svg)](https://javadoc.jenkins.io/plugin/powershell/)

Provides Jenkins integration with [PowerShell](http://www.microsoft.com/powershell)

Integrates with PowerShell by allowing you to directly write
PowerShell scripts into the text box in Jenkins. Other than that, this
plugin works pretty much like the standard shell script support.

## Example

![ScreenShot](usage_example.png?raw=true)

## FAQ

- Does this plugin support pipelines?
- No, but there is a _powershell_ step provided by the [Pipeline: Nodes and Processes](https://github.com/jenkinsci/workflow-durable-task-step-plugin) plugin
  

# Changelog

### [Unreleased]

### [Version 1.4] (Dec 4 2019)

- Support for Linux PowerShell
- Add $ErrorActionPreference = "Stop" to the top of each script before executing ([JENKINS-36002](https://issues.jenkins-ci.org/browse/JENKINS-36002))
- Required Jenkins version:  [2.138.4](https://jenkins.io/changelog-stable/)
- Added codemirror mode text/x-csharp for command syntax highlighting
- Support -NoProfile via checkbox

### [Version 1.3] (Sept 18 2015)

-   PowerShell now runs in Non-Interactive mode to prevent interactive
    prompts from hanging the build
-   PowerShell now runs with ExecutionPolicy set to "Bypass" to avoid
    execution policy issues
-   Scripts now exit with $LastExitCode, causing non-zero exit codes to
    mark a build as failed
-   Added help and list of available environment variables (including
    English and French translations)

### [Version 1.2] (Aug 5 2009)

-   Fixed a quotation problem.

### [Version 1.1] (July 1 2009)

-   Fixed a bug in the launch of PowerShell

### Version 1.0 (June 16 2009)

-   Initial version

[Unreleased]: https://github.com/jenkinsci/powershell-plugin/compare/plugin-usage-plugin-1.4...HEAD
[Version 1.4]: https://github.com/jenkinsci/powershell-plugin/compare/powershell-1.3...powershell-1.4
[Version 1.3]: https://github.com/jenkinsci/powershell-plugin/compare/powershell-1.2...powershell-1.3
[Version 1.2]: https://github.com/jenkinsci/powershell-plugin/compare/powershell-1.1...powershell-1.2
[Version 1.1]: https://github.com/jenkinsci/powershell-plugin/compare/powershell-1.0...powershell-1.1
[Version 1.0]: https://github.com/jenkinsci/powershell-plugin/compare/powershell-1.0...powershell-1.0
