import { WebPlugin } from '@capacitor/core';
import { NativeAudio, PlayRawCallback, PlayRawOptions, Response } from './definitions';

export class NativeAudioWeb extends WebPlugin implements NativeAudio {
  audioElement: HTMLAudioElement | null;
  currentlyPlayingBluetoothBuffer: boolean = false;

  constructor() {
    super({
      name: 'NativeAudio',
      platforms: ['web'],
    });

    const audioElementsOnPage = document.querySelectorAll('audio');

    if (audioElementsOnPage.length > 0) {
      this.audioElement = audioElementsOnPage[0];
    }
    else {
      // Add audio node to html on plugin instantiation
      const elementId = "audioElement" + new Date().valueOf().toString();
      const audioElement = document.createElement('audio');
      audioElement.setAttribute("id", elementId);
      document.body.appendChild(audioElement);
      this.audioElement = audioElement;
    }
  }

  async playRaw(options: PlayRawOptions, callback: PlayRawCallback): Promise<void> {
    /**
     * 
     * TODO: Implement bluetooth checking + delay for web
     * 
     */
    // if we have a viable audioElement on the class
    if (this.audioElement) {
      let base64String = '';

      if (options.bluetoothBuffer >= 25) {
        const buffer = options.bluetoothBuffer - (options.bluetoothBuffer % 25);
        // we're doing bluetooth offset first, get it and not the txt-to-speech string
        this.currentlyPlayingBluetoothBuffer = true;
        const localResponse = await fetch(`./assets/web_bluetooth_delays/${buffer}.txt`)
        base64String = await localResponse.text();
      }
      else {
        // get the txt-to-speech string
        base64String = options.rawAudio;
      }

      // notify user that we're queuing up the audio
      callback({
        msg: "Audio queued to start.",
        ok: true,
        done: false
      });

      // set the src to our decoded base64 data and play it
      this.audioElement.src = "data:audio/mpeg;base64," + base64String;
      await this.audioElement.play();

      // play() promise resolves when playing has begun
      callback({
        msg: "Audio started.",
        ok: true,
        done: false
      })

      // when playback has ended, return the final callback
      this.audioElement.onended = async() => {
        if (this.currentlyPlayingBluetoothBuffer) {
          // we've played the bluetooth buffer, now play the text to speech
          this.currentlyPlayingBluetoothBuffer = false;

          callback({
            msg: 'Bluetooth offset finished, starting txt to speech...',
            ok: true,
            done: false
          });
          
          if (this.audioElement) {
            this.audioElement.src = "data:audio/mpeg;base64," + options.rawAudio;
            await this.audioElement.play();
          }

          return
        }

        // we played the txt to speech, end the call
        callback({
          msg: "Audio ended.",
          ok: true,
          done: true
        })

        return
      }
    }
  }

  async stop(): Promise<Response> {
    console.log('Stopping audio...');

    if (this.audioElement) {
      this.audioElement.pause();

      return {
        msg: "Audio stopped.",
        ok: true,
        done: true
      }
    }

    return {
      msg: "Error: failed to find audio element in DOM",
      ok: false,
      done: true
    }
  }

  async bingbong(): Promise<void> {
    const response = await fetch('./assets/mp3/bingbong.txt');
    const rawAudio = await response.text();
    this.playRaw({rawAudio: rawAudio, bluetoothBuffer: 0 }, (c,e) => {
      if (e) {
        console.log('BINGBONG: ', e);
        return
      }
      
      console.log('BINGBONG Fine: ', c);
      return
    })
  }
}

const NativeAudio = new NativeAudioWeb();

export { NativeAudio };

