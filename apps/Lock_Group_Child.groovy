/**
 *
 *  Lock Groups
 *
 *  Copyright 2020 Dominick Meglio
 *
 *	If you find this useful, donations are always appreciated 
 *	https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=7LBRPJRLJSDDN&source=url
 */
 
definition(
    name: "Lock Group Child",
    namespace: "dcm.contactgroups",
    author: "Dominick Meglio",
    description: "Allows you to group locks together into a single virtual device",
    category: "My Apps",
	parent: "dcm.contactgroups:Device Groups",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
	documentationLink: "https://github.com/dcmeglio/hubitat-contactgroups/blob/master/README.md")

preferences {
    page(name: "prefDeviceGroup")
	page(name: "prefSettings")
}

def prefDeviceGroup() {
	return dynamicPage(name: "prefDeviceGroup", title: "Create a Lock Group", nextPage: "prefSettings", uninstall:false, install: false) {
		section {
            label title: "Enter a name for this child app. This will create a virtual lock which reports the locked/unlocked status based on the locks you select.", required:true
		}
		displayFooter()
	}
}

def prefSettings() {
	createOrUpdateChildDevice()
    return dynamicPage(name: "prefSettings", title: "", install: true, uninstall: true) {
		section {
			paragraph "unlocked choose which locks to include in this group. When all the locks are locked, the virtual device is locked. If any lock is unlocked, the virtual device is open. Locking or unlocking the virtual device will lock or unlock all of the devices in the group."

			input "locks", "capability.lock", title: "Locks to monitor", multiple:true, required:true
       
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
	subscribe(locks, "lock.locked", lockLockedHandler)
	subscribe(locks, "lock.unlocked", lockUnlockedHandler)
	def virtualLock = getChildDevice(state.lockDevice)
	subscribe(virtualLock, "lock.locked", virtualLockedHandler)
	subscribe(virtualLock, "lock.unlocked", virtualUnlockedHandler)
}

def virtualLockedHandler(evt) {
	if (!state.bypass) {
		locks*.lock()
	}
	state.bypass = false
}

def virtualUnlockedHandler(evt) {
	if (!state.bypass) {
		locks*.unlock()
	}
	state.bypass = false
}

def lockUnlockedHandler(evt) {
	logDebug "Lock unlocked, setting virtual device as unlocked"

	def device = getChildDevice(state.lockDevice)
	state.bypass = true
	device.unlock()
}

def lockLockedHandler(evt) {
	def device = getChildDevice(state.lockDevice)
	def totalLocked = 0
	locks.each { it ->
		if (it.currentValue("lock") == "locked")
		{
			totalLocked++
		}
	}
	
	if (totalLocked < locks.size())
	{
		logDebug "Lock locked, not all locked, leaving virtual device as unlocked"
		state.bypass = true
		device.unlock()
	}
	else
	{
		logDebug "Lock locked, all locked, setting virtual device as locked"
		state.bypass = true
		device.lock()
	}
}

def createOrUpdateChildDevice() {
	def childDevice = getChildDevice("contactgroup:" + app.getId())
    if (!childDevice || state.lockDevice == null) {
        logDebug "Creating child device"
		state.lockDevice = "contactgroup:" + app.getId()
		addChildDevice("hubitat", "Virtual Lock", "contactgroup:" + app.getId(), 1234, [name: app.label, isComponent: false])
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