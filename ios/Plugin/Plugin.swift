import AVFoundation
import Foundation
import Capacitor
import CoreAudio

@objc(NativeAudio)
public class NativeAudio: CAPPlugin, AVAudioPlayerDelegate {
    // audio player
    var audioPlayer = AVAudioPlayer()
    var sharedInstance: AVAudioSession = AVAudioSession()
    var isBluetooth = false

    // save callID to access saved calls for later
    var callID: String = ""

    // return docs directory for writing + reading mp3 files for playback
    private func getDocumentsDirectory() -> URL {
        let paths = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)
        let documentsDirectory = paths[0]
        return documentsDirectory
    }

    // play audio from a base64-encoded string
    @objc func playRaw(_ call: CAPPluginCall) {
        // set sharedInstance to current one
        sharedInstance = AVAudioSession.sharedInstance()
        
        try? sharedInstance.setCategory(.playback, mode: .default, options: [])

        // Keep call alive until the next
        call.keepAlive = true
        callID = call.callbackId

        let base64String = call.getString("rawAudio") ?? ""
        let audioData = Data(base64Encoded: base64String)

        let bluetoothBuffer = call.getDouble("bluetoothBuffer") ?? 0
        let bluetoothKeepAlive = call.getDouble("bluetoothKeepAlive") ?? 0
        
        let currentRoute: AVAudioSessionRouteDescription = sharedInstance.currentRoute
        
        for output in currentRoute.outputs {
            if output.portType.rawValue.contains("Bluetooth") {
                isBluetooth = true
                break
            }
        }
        
        if isBluetooth {
            print("NativeAudio: Detected bluetooth speaker, adding provided delay of: ", bluetoothBuffer, " ms")
            print("NativeAudio: Using bluetooth keepalive value of: ", bluetoothKeepAlive, " ms")
        }
        else {
            print("NativeAudio: No bluetooth detected, routing audio to device speakers or other local connection.")
        }

        if audioData != nil {
            let documentsDirectory = getDocumentsDirectory()
            let filename = documentsDirectory.appendingPathComponent("output.mp3")

            do {
                try audioData?.write(to: filename, options: .atomicWrite)
                do {
                    audioPlayer = try AVAudioPlayer(contentsOf: filename)
                    audioPlayer.delegate = self
                    audioPlayer.prepareToPlay()

                    if (isBluetooth) {
                        audioPlayer.play(atTime: audioPlayer.deviceCurrentTime + (bluetoothBuffer / 1000.0))
                    }
                    else {
                        audioPlayer.play()
                    }

                    call.resolve(["ok": true, "done": false, "msg": "Audio started"])
                } catch let error {
                    print(error.localizedDescription)
                    call.reject(error.localizedDescription)
                }
            } catch let errorTop {
                print(errorTop.localizedDescription)
                call.reject(errorTop.localizedDescription)
            }
        }
        else {
            call.reject("audioData not in correct format")
        }
    }
    
    public func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
        if let savedCall: CAPPluginCall = (bridge?.savedCall(withID: callID)) {
            savedCall.keepAlive = false

            if flag {
                savedCall.resolve(["ok": true, "done": true, "msg": "Finished playing audio."])
                return
            }

            savedCall.reject("Some error in finishing playing audio")
        }
    }

    @objc func stop(_ call: CAPPluginCall) {
        call.resolve(["msg": "iOS organically stops audio -- this method not implemented in iOS.", "ok": true, "done": true])
    }
}
