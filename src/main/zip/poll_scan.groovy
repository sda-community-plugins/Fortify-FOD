// --------------------------------------------------------------------------------
// Polls a previously initiated scan for results.
// --------------------------------------------------------------------------------

import com.serena.air.plugin.fod.FodApi
import com.serena.air.plugin.fod.FodEnums
import com.serena.air.plugin.fod.PollStatus
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
Integer pollInterval = props.notNullInt("pollInterval")
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
println "Poll Interval: ${pollInterval} minutes"
println "Debug mode value: ${debugMode}"
if (debugMode) { props.setDebugLoggingMode() }

println "----------------------------------------"
println "-- STEP EXECUTION"
println "----------------------------------------"

Proxy proxy = null
File f
boolean passFailPolicy

try {

    if (useProxy) {
        proxy = new Proxy(proxyUrl, proxyUsername, proxyPassword, proxyNtDomain, proxyNtWorkstation)
    }
    FodApi fodApi = new FodApi(fodApiUrl, proxy, fodPortalUrl)
    if (debugMode) { fodApi.setDebugMode(debugMode) }
    fodApi.authenticate(fodTenant, fodUsername, fodPassword, fodGrantType)

    println "Polling scan id ${scanId} for status"
    PollStatus listener = new PollStatus(fodApi, pollInterval);
    passFailPolicy = listener.releaseStatus(releaseId, scanId);
    listener.printPassFail(fodApi.getReleaseController().getReleaseById(releaseId), scanId)
} catch (StepFailedException e) {
    println "ERROR: ${e.message}"
    System.exit 1
}

println "----------------------------------------"
println "-- STEP OUTPUTS"
println "----------------------------------------"
if (scanId) {
    println("Setting \"scanId\" output property to \"${scanId}\"")
    apTool.setOutputProperty("scanId", scanId)
}
if (passFailPolicy) {
    println("Setting \"passFailPolicy\" output property to \"${passFailPolicy}\"")
    apTool.setOutputProperty("passFailPolicy", passFailPolicy)
}
apTool.storeOutputProperties()

//
// An exit with a zero value means the plugin step execution will be deemed successful.
//
System.exit(0)
