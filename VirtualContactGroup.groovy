metadata {
	definition(
    name: "Virtual Contact Group",
    namespace: "FriedCheese2006",
    author: "RLE"
) {
    capability "ContactSensor"
    capability "Actuator"
        
    command "open"
    command "close"
        
    attribute "contact", "enum", ["closed", "open"]
    attribute "TotalCount", "number"
    attribute "TotalOpen", "number"
    attribute "TotalClosed", "number"
}

}

def open() {
    sendEvent(name: "contact", value: "open")
}

def close() {
    sendEvent(name: "contact", value: "closed")
}
