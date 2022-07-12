import { WebPlugin } from '@capacitor/core';
import { NativeAudio, PlayRawCallback } from './definitions';

export class NativeAudioWeb extends WebPlugin implements NativeAudio {
  constructor() {
    super({
      name: 'NativeAudio',
      platforms: ['web'],
    });
  }

  async playRaw(options: { rawAudio: string }, callback: PlayRawCallback): Promise<void> {
    const audio = options.rawAudio;
    console.log('Raw audio received: ', audio, callback);
  }
}

const NativeAudio = new NativeAudioWeb();

export { NativeAudio };

