/**
 *
 *  Contact Sensor Groups
 *
 *  Copyright 2020 Dominick Meglio
 *
 *	If you find this useful, donations are always appreciated 
 *	https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=7LBRPJRLJSDDN&source=url
 */
 
definition(
    name: "Leak Sensor Group Child",
    namespace: "dcm.contactgroups",
    author: "Dominick Meglio",
    description: "Allows you to group leak sensors together into a single virtual device",
    category: "My Apps",
	parent: "dcm.contactgroups:Device Groups",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
	documentationLink: "https://github.com/dcmeglio/hubitat-contactgroups/blob/master/README.md")

preferences {
    page(name: "prefLeakGroup")
	page(name: "prefSettings")
}

def prefLeakGroup() {
	return dynamicPage(name: "prefLeakGroup", title: "Create a Leak Group", nextPage: "prefSettings", uninstall:false, install: false) {
		section {
            label title: "Enter a name for this child app. This will create a virtual leak sensor which reports the wet/dry status based on the sensors you select.", required:true
		}
		displayFooter()
	}
}

def prefSettings() {
	createOrUpdateChildDevice()
    return dynamicPage(name: "prefSettings", title: "", install: true, uninstall: true) {
		section {
			paragraph "Please choose which sensors to include in this group. When all the sensors are dry, the virtual device is dry. If any sensor is wet, the virtual device is wet."

			input "leakSensors", "capability.waterSensor", title: "Leak sensors to monitor", multiple:true, required:true
       
            input "debugOutput", "bool", title: "Enable debug logging?", defaultValue: true, displayDuringSetup: false, required: false
        }
		displayFooter()
	}
}

def installed() {
	initialize()
}

def uninstalled() {
	logDebug "uninstalling app"
	for (device in getChildDevices())
	{
		deleteChildDevice(device.deviceNetworkId)
	}
}

def updated() {	
    logDebug "Updated with settings: ${settings}"
	unschedule()
    unsubscribe()
	initialize()
}

def initialize() {
	subscribe(leakSensors, "water.wet", waterWetHandler)
	subscribe(leakSensors, "water.dry", waterDryHandler)
}

def waterWetHandler(evt) {
	logDebug "Leak detected, setting virtual device as wet"

	def device = getChildDevice(state.leakDevice)
	device.wet()
}

def waterDryHandler(evt) {
	def device = getChildDevice(state.leakDevice)
	def totalClosed = 0
	leakSensors.each { it ->
		if (it.currentValue("water") == "dry")
		{
			totalClosed++
		}
	}
	
	if (totalClosed < leakSensors.size())
	{
		logDebug "Leak not detected, but not all sensors dry, leaving virtual device as wet"
		device.wet()
	}
	else
	{
		logDebug "No leaks detected, setting virtual device as dry"
		device.dry()
	}
}

def createOrUpdateChildDevice() {
	def childDevice = getChildDevice("contactgroup:" + app.getId())
    if (!childDevice || state.leakDevice == null) {
        logDebug "Creating child device"
		state.leakDevice = "contactgroup:" + app.getId()
		addChildDevice("hubitat", "Virtual Moisture Sensor", "contactgroup:" + app.getId(), 1234, [name: app.label, isComponent: false])
    }
	else if (childDevice && childDevice.name != app.label)
		childDevice.name = app.label
}

def logDebug(msg) {
    if (settings?.debugOutput) {
		log.debug msg
	}
}

def displayFooter(){
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Device Groups<br><a href='https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=7LBRPJRLJSDDN&source=url' target='_blank'><img src='https://www.paypalobjects.com/webstatic/mktg/logo/pp_cc_mark_37x23.jpg' border='0' alt='PayPal Logo'></a><br><br>Please consider donating. This app took a lot of work to make.<br>If you find it valuable, I'd certainly appreciate it!</div>"
	}       
}

def getFormat(type, myText=""){			// Modified from @Stephack Code   
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;'>"
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
}