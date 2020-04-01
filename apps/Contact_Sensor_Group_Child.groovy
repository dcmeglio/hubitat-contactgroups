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
    name: "Contact Sensor Group Child",
    namespace: "dcm.contactgroups",
    author: "Dominick Meglio",
    description: "Allows you to group contact sensors together into a single virtual device",
    category: "My Apps",
	parent: "dcm.contactgroups:Contact Sensor Groups",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
	documentationLink: "https://github.com/dcmeglio/hubitat-contactgroups/blob/master/README.md")

preferences {
    page(name: "prefContactGroup")
	page(name: "prefSettings")
}

def prefContactGroup() {
	return dynamicPage(name: "prefContactGroup", title: "Create a Contact Group", nextPage: "prefSettings", uninstall:false, install: false) {
		section {
            label title: "Enter a name for this child app. This will create a virtual contact sensor which reports the open/closed status based on the sensors you select.", required:true
		}
	}
}

def prefSettings() {
	createOrUpdateChildDevice()
    return dynamicPage(name: "prefSettings", title: "", install: true, uninstall: true) {
		section {
			paragraph "Please choose which sensors to include in this group. When all the sensors are closed, the virtual device is closed. If any sensor is open, the virtual device is open."

			input "contactSensors", "capability.contactSensor", title: "Contact sensors to monitor", multiple:true, required:true
       
            input "debugOutput", "bool", title: "Enable debug logging?", defaultValue: true, displayDuringSetup: false, required: false
        }
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
	subscribe(contactSensors, "contact.open", contactOpenHandler)
	subscribe(contactSensors, "contact.closed", contactClosedHandler)
}

def contactOpenHandler(evt) {
	logDebug "Contact opened, setting virtual device as open"

	def device = getChildDevice(state.contactDevice)
	device.open()
}

def contactClosedHandler(evt) {
	def device = getChildDevice(state.contactDevice)
	def totalClosed = 0
	contactSensors.each { it ->
		if (it.currentValue("contact") == "closed")
		{
			totalClosed++
		}
	}
	
	if (totalClosed < contactSensors.size())
	{
		logDebug "Contact closed, all closed, leaving virtual device as open"
		device.open()
	}
	else
	{
		logDebug "Contact closed, all closed, setting virtual device as closed"
		device.close()
	}
}

def createOrUpdateChildDevice() {
	def childDevice = getChildDevice("contactgroup:" + app.getId())
    if (!childDevice || state.contactDevice == null) {
        logDebug "Creating child device"
		state.contactDevice = "contactgroup:" + app.getId()
		addChildDevice("hubitat", "Virtual Contact Sensor", "contactgroup:" + app.getId(), 1234, [name: app.label, isComponent: false])
    }
	else if (childDevice && childDevice.name != app.label)
		childDevice.name = app.label
}

def logDebug(msg) {
    if (settings?.debugOutput) {
		log.debug msg
	}
}