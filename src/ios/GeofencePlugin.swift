import WebKit
import UserNotifications

@objc(HWPGeofencePlugin) class GeofencePlugin : CDVPlugin {
    
    let geoNotificationManager = GeoNotificationManager()
    
    override func pluginInitialize () {
        print("pluginInitialize GeofencePlugin");
    }
    
    func initialize(_ command: CDVInvokedUrlCommand) {
        print("initialize GeofencePlugin")
        geoNotificationManager.webView = self.webView
        geoNotificationManager.center.requestAuthorization(options: [.alert, .sound]) {
            (granted, error) in
            if granted && self.geoNotificationManager.checkRequirements() {
                self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_OK), callbackId: command.callbackId)
            }
            self.commandDelegate!.send(
                CDVPluginResult(status: CDVCommandStatus_ILLEGAL_ACCESS_EXCEPTION,
                                messageAs: "no auth to send notifications"), callbackId: command.callbackId)
        }
    }
    
    func deviceReady(_ command: CDVInvokedUrlCommand) {
        print("deviceReady GeofencePlugin")
        commandDelegate!.send(
            CDVPluginResult(status: CDVCommandStatus_OK), callbackId: command.callbackId)
    }
    
    func addOrUpdate(_ command: CDVInvokedUrlCommand) {
        print("addOrUpdate GeofencePlugin")
        DispatchQueue.global(qos: DispatchQoS.QoSClass.default).async {
            for geo in command.arguments {
                self.geoNotificationManager.addOrUpdateGeoNotification(JSON(geo))
            }
            DispatchQueue.main.async {
                self.commandDelegate!.send(
                    CDVPluginResult(status: CDVCommandStatus_OK), callbackId: command.callbackId)
            }
        }
    }
    
    func removeAll(_ command: CDVInvokedUrlCommand) {
        print("removeAll GeofencePlugin")
        DispatchQueue.global(qos: DispatchQoS.QoSClass.default).async {
            self.geoNotificationManager.removeAllGeoNotifications()
            DispatchQueue.main.async {
                self.commandDelegate!.send(
                    CDVPluginResult(status: CDVCommandStatus_OK), callbackId: command.callbackId)
            }
        }
    }
}

class GeoNotificationManager : NSObject, UNUserNotificationCenterDelegate {
    var webView: Optional<UIView>
    let center = UNUserNotificationCenter.current()
    
    override init() {
        super.init()
        center.delegate = self
    }
    
    func addOrUpdateGeoNotification(_ geoNotification: JSON) {
        print("AddOrUpdate geo: \(geoNotification)")
        let location = CLLocationCoordinate2DMake(
            geoNotification["latitude"].doubleValue,
            geoNotification["longitude"].doubleValue
        )
        let radius = geoNotification["radius"].doubleValue as CLLocationDistance
        let id = geoNotification["id"].stringValue
        let region = CLCircularRegion(center: location, radius: radius, identifier: id)
        var transitionType = 0
        if let i = geoNotification["transitionType"].int {
            transitionType = i
        }
        region.notifyOnEntry = 0 != transitionType & 1
        region.notifyOnExit = 0 != transitionType & 2
        
        let content = UNMutableNotificationContent()
        let uuid = UUID().uuidString
        content.title = geoNotification["notification"]["title"].stringValue
        content.body = geoNotification["notification"]["text"].stringValue
        content.userInfo.updateValue(uuid, forKey: "uuid")
        content.sound = UNNotificationSound.default()
        if let json = geoNotification["notification"]["data"] as JSON? {
            content.userInfo.updateValue(json.rawString(String.Encoding.utf8.rawValue, options: [])!, forKey: "geofence.notification.data")
        }
        
        let trigger = UNLocationNotificationTrigger(region: region, repeats: true)
        let identifier = "BWBLocalNotification_" + region.identifier
        let request = UNNotificationRequest(identifier: identifier,
                                            content: content, trigger: trigger)
        center.add(request, withCompletionHandler: { (error) in
            if let error = error {
                print(error)
            } else {
                print("notification added for " + identifier)
            }
        })
    }
    
    func evaluateJs (_ script: String) {
        print("evaluateJs GeofencePlugin")
        if let webView = self.webView  {
            if let uiWebView = webView as? UIWebView {
                uiWebView.stringByEvaluatingJavaScript(from: script)
            } else if let wkWebView = webView as? WKWebView {
                wkWebView.evaluateJavaScript(script, completionHandler: nil)
            }
        } else {
            print("webView is nil")
        }
    }
    
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification,
                                withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        let uuid = notification.request.content.userInfo["uuid"] as? String
        print("userNotificationCenter1 GeofencePlugin: " + uuid!)
        completionHandler([.alert,.sound])
    }
    
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                didReceive response: UNNotificationResponse,
                                withCompletionHandler completionHandler: @escaping () -> Void) {
        
        print("userNotificationCenter2 GeofencePlugin")
        switch response.actionIdentifier {
        case UNNotificationDefaultActionIdentifier:
            let data = response.notification.request.content.userInfo["geofence.notification.data"] as? String
            let js = "setTimeout('geofence.onNotificationClicked(" + data! + ")',0)"
            evaluateJs(js)
        default:
            print("Unknown action")
        }
        completionHandler()
    }
    
    func checkRequirements() -> Bool {
        print("GeoNotificationManager checkRequirements GeofencePlugin")
        if (!CLLocationManager.isMonitoringAvailable(for: CLRegion.self)) {
            print("Geofencing not available")
            return false
        }
        if (!CLLocationManager.locationServicesEnabled()) {
            print("Error: Locationservices not enabled")
            return false
        }
        if (CLLocationManager.authorizationStatus() != CLAuthorizationStatus.authorizedWhenInUse) {
            print("Warning: Location permissions not granted")
            return false
        }
        return true
    }
    
    func removeAllGeoNotifications() {
        center.removeAllPendingNotificationRequests()
    }
}


