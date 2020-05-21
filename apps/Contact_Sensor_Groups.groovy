/**
 *
 *  Device Groups
 *
 *  Copyright 2020 Dominick Meglio
 *
 *	If you find this useful, donations are always appreciated 
 *	https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=7LBRPJRLJSDDN&source=url
 */
 
definition(
    name: "Device Groups",
    namespace: "dcm.contactgroups",
    author: "Dominick Meglio",
    description: "Allows you to group devices together into a single virtual device",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
	documentationLink: "https://github.com/dcmeglio/hubitat-contactgroups/blob/master/README.md")

preferences {
     page(name: "mainPage", title: "", install: true, uninstall: true)
} 

def installed() {
    initialize()
}

def updated() {
    initialize()
}

def initialize() {
	// Do nothing for now
}

def mainPage() {
    dynamicPage(name: "mainPage") {
    	isInstalled()
		if(state.appInstalled == 'COMPLETE'){
			section("${app.label}") {
				paragraph "Provides options for combining multiple contact sensors into a single device so you can receive a single status"
			}
			section("Child Apps") {
				app(name: "contactApp", appName: "Contact Sensor Group Child", namespace: "dcm.contactgroups", title: "Add a new Contact Sensor Group", multiple: true)
				app(name: "lockApp", appName: "Lock Group Child", namespace: "dcm.contactgroups", title: "Add a new Lock Group", multiple: true)
				app(name: "leakApp", appName: "Leak Sensor Group Child", namespace: "dcm.contactgroups", title: "Add a new Leak Sensor Group", multiple: true)
			}
			section("General") {
       			label title: "Enter a name for parent app (optional)", required: false
 			}
			displayFooter()
		}
	}
}

def isInstalled() {
	state.appInstalled = app.getInstallationState() 
	if (state.appInstalled != 'COMPLETE') {
		section
		{
			paragraph "Please click <b>Done</b> to install the parent app."
		}
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