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

 - [Replace JSR-305 annotations with spotbugs annotations](https://github.com/jenkinsci/powershell-plugin/pull/29)
 
### [Version 2.1] (2023-09-05)

 - [Upgrade HtmlUnit from 2.x to 3.x](https://github.com/jenkinsci/powershell-plugin/pull/25)
 - [Support building on Java 21](https://github.com/jenkinsci/powershell-plugin/pull/27)
 - [JENKINS-71515 Make codemirror-config a valid JSON](https://github.com/jenkinsci/powershell-plugin/pull/26)
 - [Change from groovy to charp in codemirror configuration](https://github.com/jenkinsci/powershell-plugin/pull/28)

### [Version 2.0] (2023-01-06)

 - [Use HTTPS instead of git](https://github.com/jenkinsci/powershell-plugin/pull/23)
 - [Updates parent pom to 4.51](https://github.com/jenkinsci/powershell-plugin/pull/22)
 - [Require Jenkins 2.361.4 and Java 11](https://github.com/jenkinsci/powershell-plugin/pull/24)
 
### [Version 1.8] (2022-12-23)

 - [Removes force insertion of '$ErrorActionPreference = "Continue"' if "Stop On Errors" is unchecked](https://github.com/jenkinsci/powershell-plugin/pull/21)

### [Version 1.7] (2021-10-25)

 - [Bring PowerShell on par with BatchFile by supporting unstable builds](https://github.com/jenkinsci/powershell-plugin/pull/14)

### [Version 1.6] (2021-09-20)

 - [fixed serialization issue that occurs in some scenarios](https://github.com/jenkinsci/powershell-plugin/pull/17), fixes [JENKINS-65613 After upgrading to powershell-plugin 1.5 enabling/disabling projects causes an error](https://issues.jenkins.io/browse/JENKINS-65613)

### [Version 1.5] (2021-05-04)

- [Added selection of preferred PowerShell version for Windows machines](https://github.com/jenkinsci/powershell-plugin/pull/16)

### [Version 1.4] (2019-12-04)

- Support for Linux PowerShell
- Add $ErrorActionPreference = "Stop" to the top of each script before executing ([JENKINS-36002](https://issues.jenkins-ci.org/browse/JENKINS-36002))
- Required Jenkins version:  [2.138.4](https://jenkins.io/changelog-stable/)
- Added codemirror mode text/x-csharp for command syntax highlighting
- Support -NoProfile via checkbox

### [Version 1.3] (2015-09-18)

-   PowerShell now runs in Non-Interactive mode to prevent interactive
    prompts from hanging the build
-   PowerShell now runs with ExecutionPolicy set to "Bypass" to avoid
    execution policy issues
-   Scripts now exit with $LastExitCode, causing non-zero exit codes to
    mark a build as failed
-   Added help and list of available environment variables (including
    English and French translations)

### [Version 1.2] (2009-08-05)

-   Fixed a quotation problem.

### [Version 1.1] (2009-07-01)

-   Fixed a bug in the launch of PowerShell

### Version 1.0 (2009-06-16)

-   Initial version

[Unreleased]: https://github.com/jenkinsci/powershell-plugin/compare/plugin-usage-plugin-2.1...HEAD
[Version 2.1]: https://github.com/jenkinsci/powershell-plugin/compare/powershell-2.0...powershell-2.1
[Version 2.0]: https://github.com/jenkinsci/powershell-plugin/compare/powershell-1.8...powershell-2.0
[Version 1.8]: https://github.com/jenkinsci/powershell-plugin/compare/powershell-1.7...powershell-1.8
[Version 1.7]: https://github.com/jenkinsci/powershell-plugin/compare/powershell-1.6...powershell-1.7
[Version 1.6]: https://github.com/jenkinsci/powershell-plugin/compare/powershell-1.5...powershell-1.6
[Version 1.5]: https://github.com/jenkinsci/powershell-plugin/compare/powershell-1.4...powershell-1.5
[Version 1.4]: https://github.com/jenkinsci/powershell-plugin/compare/powershell-1.3...powershell-1.4
[Version 1.3]: https://github.com/jenkinsci/powershell-plugin/compare/powershell-1.2...powershell-1.3
[Version 1.2]: https://github.com/jenkinsci/powershell-plugin/compare/powershell-1.1...powershell-1.2
[Version 1.1]: https://github.com/jenkinsci/powershell-plugin/compare/powershell-1.0...powershell-1.1
[Version 1.0]: https://github.com/jenkinsci/powershell-plugin/compare/powershell-1.0...powershell-1.0
