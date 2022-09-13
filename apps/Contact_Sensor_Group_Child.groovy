/**
 *
 *  Contact Sensor Groups
 *
 *  Copyright 2020 Dominick Meglio
 *
 *	If you find this useful, donations are always appreciated 
 *	https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=7LBRPJRLJSDDN&source=url
 *
 *      FriedCheese 	v1.1     Updated to track total sensor count, open count, and closed count for each group.
 */
 
definition(
    name: "Contact Sensor Group Child",
    namespace: "dcm.contactgroups",
    author: "Dominick Meglio",
    description: "Allows you to group contact sensors together into a single virtual device",
    category: "My Apps",
	parent: "dcm.contactgroups:Device Groups",
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
		displayFooter()
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
	subscribe(contactSensors, "contact.open", contactOpenHandler)
	subscribe(contactSensors, "contact.closed", contactClosedHandler)
    def device = getChildDevice(state.contactDevice)
	device.sendEvent(name: "TotalCount", value: contactSensors.size())
}

def contactOpenHandler(evt) {
	logDebug "Contact opened, setting virtual device as open"

	def device = getChildDevice(state.contactDevice)
    def totalOpen = 0
    def totalClosed = 0
	contactSensors.each { it ->
		if (it.currentValue("contact") == "open")
		{
			totalOpen++
		}
    }
    contactSensors.each { it ->
		if (it.currentValue("contact") == "closed")
		{
			totalClosed++
		}
	}
    device.sendEvent(name: "TotalClosed", value: totalClosed, descriptionText: "There are ${totalClosed} windows closed")
    device.sendEvent(name: "TotalOpen", value: totalOpen,descriptionText: "There are ${totalOpen} windows open")
	device.open()
}

def contactClosedHandler(evt) {
	def device = getChildDevice(state.contactDevice)
    def totalOpen = 0
    def totalClosed = 0
	contactSensors.each { it ->
		if (it.currentValue("contact") == "open")
		{
			totalOpen++
		}
    }
    contactSensors.each { it ->
		if (it.currentValue("contact") == "closed")
		{
			totalClosed++
		}
    }
    device.sendEvent(name: "TotalClosed", value: totalClosed, descriptionText: "There are ${totalClosed} windows closed")
    device.sendEvent(name: "TotalOpen", value: totalOpen,descriptionText: "There are ${totalOpen} windows open")
    
	if (totalClosed < contactSensors.size())
	{
		logDebug "Contact closed, not all closed, leaving virtual device as open"
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
		addChildDevice("FriedCheese2006", "Virtual Contact Group", "contactgroup:" + app.getId(), 1234, [name: app.label, isComponent: false])
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
