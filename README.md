# hubitat-contactgroups
This app allows you to group multiple contact sensors together as a virtual device so you can report a single status for a group of contact sensors. This can be useful for devices that have more than one contact sensor installed, or to group contact sensors, for example, in a room.
 
## Apps
You must install both the __Contact Sensor Groups__ and __Contact Sensor Group Child__ apps for this to work. Each child app represents a single grouping of contacts.

### Configuration
The name given to the child app will be the name assigned to the virtual contact sensor device associated with the group. Within a group, the selected contact sensors will inform the state of the group. If all of the selected contact sensors are closed, the group is closed. If any of the selected contact sensors are open, the group is open.

### Donations
If you find this app useful, please consider making a [donation](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=7LBRPJRLJSDDN&source=url)! 