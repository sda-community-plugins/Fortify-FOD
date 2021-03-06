// --------------------------------------------------------------------------------
// Retrieve the details of a scan and produce a HTML report with the results.
// --------------------------------------------------------------------------------

import com.serena.air.plugin.fod.FodApi
import com.serena.air.plugin.fod.parser.Proxy

import com.serena.air.StepFailedException
import com.serena.air.StepPropertiesHelper
import com.urbancode.air.AirPluginTool

//
// Create some variables that we can use throughout the plugin step.
// These are mainly for checking what operating system we are running on.
//
final def PLUGIN_HOME = System.getenv()['PLUGIN_HOME']
final String lineSep = System.getProperty('line.separator')
final String osName = System.getProperty('os.name').toLowerCase(Locale.US)
final String pathSep = System.getProperty('path.separator')
final boolean windows = (osName =~ /windows/)
final boolean vms = (osName =~ /vms/)
final boolean os9 = (osName =~ /mac/ && !osName.endsWith('x'))
final boolean unix = (pathSep == ':' && !vms && !os9)

//
// Initialise the plugin tool and retrieve all the properties that were sent to the step.
//
final def  apTool = new AirPluginTool(this.args[0], this.args[1])
final def  props  = new StepPropertiesHelper(apTool.getStepProperties(), true)

//
// Set a variable for each of the plugin steps's inputs.
// We can check whether a required input is supplied (the helper will fire an exception if not) and
// if it is of the required type.
//
File workDir = new File('.').canonicalFile
String fodGrantType = props.notNull('fodGrantType')
String fodUsername = props.notNull('fodUsername')
String fodPassword = props.notNull('fodPassword')
String fodTenant = props.optional('fodTenant')
String fodApiUrl = props.notNull("fodApiUrl")
String fodPortalUrl = props.notNull("fodPortalUrl")
Integer applicationId = props.notNullInt("applicationId")
Integer releaseId = props.notNullInt("releaseId")
Integer scanId = props.notNullInt("scanId")
String resultsFile = props.notNull("resultsFile")
String fodScope = props.optional("fodScope", "api-tenant")
boolean useProxy = props.optionalBoolean("useProxy", false)
String proxyUrl = props.optional("proxyUrl")
String proxyUsername = props.optional("proxyUsername")
String proxyPassword = props.optional("proxyPassword")
String proxyNtDomain = props.optional("proxyNtDomain")
String proxyNtWorkstation = props.optional("proxyNtWorkstation")
boolean debugMode = props.optionalBoolean("debugMode", false)

println "----------------------------------------"
println "-- STEP INPUTS"
println "----------------------------------------"

//
// Print out each of the property values.
//
println "Working directory: ${workDir.canonicalPath}"
println "FOD API URL: ${fodApiUrl}"
println "FOD Portal URL: ${fodPortalUrl}"
println "Grant Type: ${fodGrantType}"
if (fodGrantType.equals(FodApi.GRANT_TYPE_CLIENT_CREDENTIALS)) {
    println "Client Id: ${fodUsername}"
    println "Client Secret: ${fodPassword.replaceAll(".", "*")}"
} else {
    println "Username: ${fodUsername}"
    println "Password: ${fodPassword.replaceAll(".", "*")}"
    if (fodTenant.isEmpty()) {
        throw new StepFailedException("No FOD tenant specified, please enter your tenant.")
    } else {
        println "Tenant: ${fodTenant}"
    }
}
if (useProxy) {
    println "Using proxy: ${proxyUrl}"
}
println "Application Id: ${applicationId}"
println "Release Id: ${releaseId}"
println "Scan Id: ${scanId}"
println "Results File: ${resultsFile}"
println "Debug mode value: ${debugMode}"
if (debugMode) { props.setDebugLoggingMode() }

println "----------------------------------------"
println "-- STEP EXECUTION"
println "----------------------------------------"

Proxy proxy = null
def scan

try {
    if (useProxy) {
        proxy = new Proxy(proxyUrl, proxyUsername, proxyPassword, proxyNtDomain, proxyNtWorkstation)
    }
    FodApi fodApi = new FodApi(fodApiUrl, proxy, fodPortalUrl)
    if (debugMode) { fodApi.setDebugMode(debugMode) }
    fodApi.authenticate(fodTenant, fodUsername, fodPassword, fodGrantType)

    println "Creating scan report for scan id: ${scanId}"
    scan = fodApi.getScanSummaryController().getScanSummary(releaseId, scanId)
    String scanUrl = "${fodPortalUrl}/Redirect/Scans/${scan.getScanId()}"
    String releaseUrl = "${fodPortalUrl}/Redirect/Releases/${scan.getReleaseId()}"
    String applicationUrl = "${fodPortalUrl}/Redirect/Applications/${scan.getApplicationId()}"
    def sb = new StringBuilder()
    sb << """
<style type='text/css'>
    body { padding: 10px; 	background: #F5F5F5; font-family: Arial; }
    div { 
        background: #fff; box-shadow: 4px 4px 10px 0px rgba(119, 119, 119, 0.3);
        -moz-box-shadow: 4px 4px 10px 0px rgba(119, 119, 119, 0.3);
        -webkit-box-shadow: 4px 4px 10px 0px rgba(119, 119, 119, 0.3);
        margin-bottom: 20px; }
    a { color: #337ab7; text-decoration: underline; }
    a:hover { text-decoration: none; }
    span { font-weight: bold; }
    span.failed { color: #df1e1e; }
    span.success { color: #32ad12; }
    table { border-collapse: collapse; width: 100%; color: #333; }
    table td, table th { border: 1px solid #ddd; }
    table th { padding: 10px; text-align: center; background: #eee; font-size: 16px; }
    table td { padding: 7px; font-size: 12px; } " +
    table tr th:first-child { max-width: 200px; min-width: 200px; }
    table tr td:first-child { width: 200px; text-align: right; font-weight: bold;}
    table tr td:first-child:after {content: ':' }
</style>
"""
    sb << "<div> <table> <tr> <th colspan='2'>${scan.getScanType()} Scan: ${scan.getScanId()} </th> </tr>"
    sb << "<tr> <td> Application" + "</td> <td> <a href='${applicationUrl}'  target=_blank>" + scan.getApplicationName() + "</a> </td> </tr>"
    sb << "<tr> <td> Release" + "</td> <td> <a href='${releaseUrl}'  target=_blank>" + scan.getReleaseName() + "</a> </td> </tr>"
    sb << "<tr> <td> Assessment Type </td> <td> ${scan.getAssessmentTypeName()} </td> </tr>"
    sb << "<tr> <td> Date Started </td> <td> ${scan.getStartedDatetime()} </td> </tr>"
    sb << "<tr> <td> Date Completed </td> <td> ${scan.getCompletedDateTime()} </td> </tr>"
    sb << "<tr> <td> Notes </td> <td> ${scan.getNotes()} </td> </tr>"
    sb << "<tr> <td> Scan Tool </td> <td> ${scan.getScanTool()} - ${scan.getScanToolVersion()} </td> </tr>"
    sb << "<tr> <td> Number of Criticals </td> <td> ${scan.getIssueCountCritical()} </td> </tr>"
    sb << "<tr> <td> Number of Highs </td> <td> ${scan.getIssueCountHigh()} </td> </tr>"
    sb << "<tr> <td> Number of Mediums </td> <td> ${scan.getIssueCountMedium()} </td> </tr>"
    sb << "<tr> <td> Number of Lows </td> <td> ${scan.getIssueCountLow()} </td> </tr>"
    sb << "<tr> <td> Star Rating </td> <td> ${scan.getStarRating()} </td> </tr>"
    sb << "<tr> <td> Analysis status </td> <td> " +
            (scan.getAnalysisStatusType().equals("Completed") ? "<span class=\"success\">Completed</span>" : "<span class=\"failed\">${scan.getAnalysisStatusType()}</span>") +
            " </td> </tr>"
    sb << "<tr> <td> URL" + "</td> <td> <a href='${scanUrl}'  target=_blank>" + scanUrl + "</a> </td> </tr>"
    sb << "<br>"
    sb << "</table> </div>"
    sb << "</body>"
    sb << "</html>"

    File f = new File(resultsFile)
    BufferedWriter bw = new BufferedWriter(new FileWriter(f))
    bw.write(sb.toString())
    bw.close()

    //long startTime = new Date().getTime();
    //wieClient.debug("Start UNIX time is: ${startTime}")

    //long finishTime = new Date().getTime();
    //wieClient.debug("Finish UNIX time is: ${finishTime}")

} catch (StepFailedException e) {
    println "ERROR: ${e.message}"
    System.exit 1
}

println "----------------------------------------"
println "-- STEP OUTPUTS"
println "----------------------------------------"
if (resultsFile) {
    println("Setting \"resultsFile\" output property to \"${resultsFile}\"")
    apTool.setOutputProperty("resultsFile", resultsFile)
}
apTool.storeOutputProperties()

//
// An exit with a zero value means the plugin step execution will be deemed successful.
//
System.exit(0)
