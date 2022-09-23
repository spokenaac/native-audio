import { WebPlugin } from '@capacitor/core';
import { CalibrationOptions, NativeAudio, PlayRawCallback, PlayRawOptions, Response } from './definitions';

export class NativeAudioWeb extends WebPlugin implements NativeAudio {
  audioElement: HTMLAudioElement | null;

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
      // notify user that we're queuing up the audio
      callback({
        msg: "Audio queued to start.",
        ok: true,
        done: false
      });

      // set the src to our decoded base64 data and play it
      this.audioElement.src = "data:audio/mpeg;base64," + options.rawAudio;
      await this.audioElement.play();

      // play() promise resolves when playing has begun
      callback({
        msg: "Audio started.",
        ok: true,
        done: false
      })

      // when playback has ended, return the final callback
      this.audioElement.onended = () => {
        callback({
          msg: "Audio ended.",
          ok: true,
          done: true
        })
      }
    }
  }

  async calibrateBluetooth(options: CalibrationOptions, callback: PlayRawCallback): Promise<void> {
    console.log(options, callback);
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
}

const NativeAudio = new NativeAudioWeb();

export { NativeAudio };

