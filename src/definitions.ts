export interface NativeAudio {

  /**
   * Takes in a base64-encoded string, decodes, and plays the raw mp3 data via routing
   * to device speakers
   * @param options Object: { rawAudio: string }
   * @param callback Callback: (success, error) => {}
   */
  playRaw(options: { rawAudio: string }, callback: PlayRawCallback): Promise<void>;
}

export interface Response {
  ok: boolean,
  done: boolean,
  msg: string
}

export type PlayRawCallback = (response: Response, error?: any) => void;
