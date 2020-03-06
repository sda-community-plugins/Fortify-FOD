# Micro Focus Fortify On Demand (FOD) plugin

The _Micro Focus Fortify On Demand_ plugin plugin allows you to execute static and dynamic scans in Fortify on Demand 
as well as import existing scans from the Fortify on-premise tools: Fortify SCA and WebInspect. 

This plugin is a work in progress but it is intended to provide the following steps:

* [x] **Step 1** - Description


### Installing the plugin
 
Download the latest version from the _release_ directory and install into Deployment Automation from the 
**Administration\Automation\Plugins** page.

### Building the plugin

To build the plugin you will need to clone the following repositories (at the same level as this repository):

 - [mavenBuildConfig](https://github.com/sda-community-plugins/mavenBuildConfig)
 - [plugins-build-parent](https://github.com/sda-community-plugins/plugins-build-parent)
 - [air-plugin-build-script](https://github.com/sda-community-plugins/air-plugin-build-script)
 
 and then compile using the following command
 ```
   mvn clean package
 ```  

This will create a _.zip_ file in the `target` directory when you can then install into Deployment Automation
from the **Administration\Automation\Plugins** page.

If you have any feedback or suggestions on this template then please contact me using the details below.

Kevin A. Lee

kevin.lee@microfocus.com

**Please note: this plugins is provided as a "community" plugin and is not supported by Micro Focus in any way**.
