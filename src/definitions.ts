export interface NativeAudio {

  /**
   * Takes in a base64-encoded string, decodes, and plays the raw mp3 data via routing
   * to device speakers
   * @param options Object: { rawAudio: string, bluetoothBuffer: number, bluetoothKeepAlive: number }
   * @param callback Callback: (success, error) => {}
   */
  playRaw(options: PlayRawOptions, callback: PlayRawCallback): Promise<void>;

  /**
   * Used to calibrate the bluetooth buffer offset
   * @param options 
   * @param callback 
   */
  calibrateBluetooth(options: CalibrationOptions, callback: PlayRawCallback): Promise<void>;

  /**
   * Immediately stops all audio being played.
   */
  stop(): Promise<Response>;

  audioElement: HTMLAudioElement | null
}

export interface Response {
  ok: boolean,
  done: boolean,
  msg: string
}

export interface CalibrationOptions {
  bluetoothOffset: number;
}

export interface PlayRawOptions {
  rawAudio: string,
  bluetoothBuffer?: number,
  bluetoothKeepAlive?: number
}

export type PlayRawCallback = (response: Response, error?: any) => void;
