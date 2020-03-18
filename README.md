# Micro Focus Fortify On Demand (FOD) plugin

The _Micro Focus Fortify On Demand (FOD)_ plugin allows you to execute static and dynamic scans in 
Fortify on Demand, import on-premise scans from Fortify SCA and Fortify WebInspect, and report on the 
status of scans and releases. 

This plugin is a work in progress but it is intended to provide the following steps:

* [x] **Run Static Scan** - Uploads a Zip file containing source files and dependencies to Fortify On Demand and initiates a static scan.
* [x] **Run Dynamic Scan** - Runs a dynamic scan that has previously been configured in Fortify On Demand.
* [x] **Poll Scan** - Polls a previously initiated scan for results.
* [x] **Import Static Scan** - Import the results of an on-premise static scan.
* [x] **Import Dynamic Scan** - Import the results of an on-premise dynamic scan.
* [x] **Publish Scan Report** - Retrieve the details of a scan and produce a HTML report with the results.
* [x] **Publish Release Report** - Retrieve the details of a release and produce a HTML report with the results.
* [x] **Check Compliance** - Retrieve the security rating of a release and fail if a minimum value has not been reached.


### Installing the plugin
 
Download the latest version from the _release_ directory and install into Deployment Automation from the 
**Administration\Automation\Plugins** page.

### Using the plugin

The plugin can authenticate with Fortify on Demand using either API Client Credentials (recommended) or Username and Password
details for the tenant. To create new Client Credentials in Fortify on Demand navigate to **Administration\Settings\API** and
select **Add Key**. Create a new key for an application and select "_Security Lead_" for the role. 

You will also need to create Deployment Automation 
[System Properties](http://help.serena.com/doc_center/sra/ver6_3/sda_help/sra_adm_sys_properties.html) for the
plugin steps to use:

 | Property | Example Value | Description |
 | -------- | ------------- | ----------- |
 |`fod.username`| user | Your username or API Client Id (API Key) |
 |`fod.password`| password | Your password or API Client Secret |
 |`fod.tenant`| tenant001 | The tenant you are using (from login window) - required if not using Client Credentials |
 |`fod.apiUrl`| https://api.emea.fortify.com | URL to the FOD API for your tenant |
 |`fod.portalUrl`| https://emea.fortify.com | The URL to the user portal where you login |

Most of the steps also required "_Application Id_" and "_Release Id_" values - it is recommended that these are set as
[Application Properties](http://help.serena.com/doc_center/sra/ver6_3_1/sda_help/ProcConfAppPropNew.html) in Deployment Automation.
 
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
