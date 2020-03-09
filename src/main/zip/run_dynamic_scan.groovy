// --------------------------------------------------------------------------------
// Runs a dynamic scan that has previously been configured in Fortify On Demand.
// --------------------------------------------------------------------------------

import com.serena.air.plugin.fod.FodApi
import com.serena.air.plugin.fod.FodEnums
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
Integer entitlementId = props.notNullInt("entitlementId")
String assessmentType = props.notNull("assessmentType")
String notes = props.optional("notes")
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
println "Entitlement Id: ${entitlementId}"
println "Assessment Type: ${assessmentType}"
println "Debug mode value: ${debugMode}"
if (debugMode) { props.setDebugLoggingMode() }

println "----------------------------------------"
println "-- STEP EXECUTION"
println "----------------------------------------"

Proxy proxy = null
def scanId

try {
    if (useProxy) {
        proxy = new Proxy(proxyUrl, proxyUsername, proxyPassword, proxyNtDomain, proxyNtWorkstation)
    }
    FodApi fodApi = new FodApi(fodApiUrl, proxy, fodPortalUrl)
    if (debugMode) { fodApi.setDebugMode(debugMode) }
    fodApi.authenticate(fodTenant, fodUsername, fodPassword, fodGrantType)

    println "Starting dynamic scan" // for application ${applicationId}
    def remediationScan = true
    if (remediationScanPreference != FodEnums.RemediationScanPreferenceType.NonRemediationScanOnly.name()) {
        remediationScan = false
    }
    if (fodApi.dynamicScanController.startDynamicScan(releaseId, false,
            FodEnums.DynamicAssessmentType.valueOf(assessmentType),
            entitlementId, FodEnums.EntitlementFrequencyType.SingleScan,
            FodEnums.DynamicAssessmentType.valueOf(assessmentType),
            false, true )) {
        scanId = fodApi.getDynamicScanController().getTriggeredScanId()
        println "Started scan with id: ${scanId}; see:"
        //println "${fodPortalUrl}/Applications/${applicationId}/Scans"
    }

} catch (StepFailedException e) {
    println "ERROR: ${e.message}"
    System.exit 1
}

println "----------------------------------------"
println "-- STEP OUTPUTS"
println "----------------------------------------"
println("Setting \"scanId\" output property to \"${scanId}\"")
apTool.setOutputProperty("scanId", scanId)
apTool.storeOutputProperties()

//
// An exit with a zero value means the plugin step execution will be deemed successful.
//
System.exit(0)
