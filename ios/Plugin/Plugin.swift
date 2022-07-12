import AVFoundation
import Foundation
import Capacitor
import CoreAudio

enum MyError: Error {
    case runtimeError(String)
}

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitor.ionicframework.com/docs/plugins/ios
 */
@objc(NativeAudio)
public class NativeAudio: CAPPlugin {
    // audio player
    var audioPlayer = AVAudioPlayer()

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
        let base64String = call.getString("rawAudio") ?? ""
        let audioData = Data(base64Encoded: base64String)

        if audioData != nil {
            let documentsDirectory = getDocumentsDirectory()
            let filename = documentsDirectory.appendingPathComponent("output.mp3")

            do {
                try audioData?.write(to: filename, options: .atomicWrite)
                do {
                    audioPlayer = try AVAudioPlayer(contentsOf: filename)
                    audioPlayer.prepareToPlay()
                    audioPlayer.play()
                    call.resolve()
                } catch let error {
                    print("uh oh!")
                    print(error.localizedDescription)
                    call.reject(error.localizedDescription)
                }
            } catch let errorTop {
                print("")
                print(errorTop.localizedDescription)
                call.reject(errorTop.localizedDescription)
            }
        }
        else {
            call.reject("audioData not correct format")
        }
    }
}
