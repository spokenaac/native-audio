import AVFoundation
import Foundation
import Capacitor
import CoreAudio

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

    // @objc initializePlugin(_ call: CAPPluginCall) {
    //     let finishedPlayingNotif = NotificationCenter.default.addObserver(        
    //         forName: NSNotification.Name.mlkitModelDownloadDidSucceed,
    //         object: nil,
    //         queue: OperationQueue.main,
    //         using: {
    //         [unowned self]
    //         (notification) in
    //             // access saved call from earlier when downloads were called
    //             if let savedCall: CAPPluginCall = (bridge?.savedCall(withID: callID)) {
    //                 let downloadedModel: DigitalInkRecognitionModel = notification.userInfo![ModelDownloadUserInfoKey.remoteModel.rawValue] as! DigitalInkRecognitionModel
    //                 let langTag: String = downloadedModel.modelIdentifier.languageTag
                    
    //                 downloadCount -= 1
                    
    //                 savedCall.resolve(["ok": true, "done": downloadCount == 0, "msg": langTag + " model successfully downloaded."])
    //             }
    //         }
    //     )
    // }
}
