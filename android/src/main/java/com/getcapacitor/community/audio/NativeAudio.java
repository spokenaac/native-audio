package com.getcapacitor.community.audio;

import android.Manifest;
import android.content.res.AssetManager;
import android.media.MediaPlayer;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

@CapacitorPlugin(
  permissions = {
    @Permission(strings = { Manifest.permission.MODIFY_AUDIO_SETTINGS }),
    @Permission(strings = { Manifest.permission.WRITE_EXTERNAL_STORAGE }),
    @Permission(strings = { Manifest.permission.READ_PHONE_STATE }),
  }
)
public class NativeAudio extends Plugin {

    // instantiate our class-level media player
    MediaPlayer mediaPlayer = null;

    // class-level flag for if we're using a bluetooth offset or not
    Boolean currentlyPlayingBluetoothBuffer = false;

    /**
     * Gets offset base64 txt if provided, if not, gets base64 string from call
     * Prepares mediaPlayer for playback
     * Once prepared, either:
     *      A) bluetoothBuffer base64 white noise is played
     *      B) base64 from call is played
     * If bluetoothBuffer was played, the onCompletion listener will trigger the preparation
     *      of the next audio (call base64), and the loop runs one more time
     * Call resolves
     * */
    @PluginMethod(returnType = PluginMethod.RETURN_CALLBACK)
    public void playRaw(PluginCall call) {
        // this is a callback hook, keep the call alive until we manually finish it
        call.setKeepAlive(true);

        // get our offset
        Integer bluetoothBuffer = call.getInt("bluetoothBuffer");

        String base64String = "";

        if (bluetoothBuffer >= 25) {
            // we're doing the bluetooth offset first, get it and not the txt-to-speech string
            currentlyPlayingBluetoothBuffer = true;
            Integer safeBluetoothBuffer = bluetoothBuffer - (bluetoothBuffer % 25);
            base64String = getAssets(bluetoothBuffer);
        }
        else {
            // get the txt-to-speech base64 string
            base64String = call.getString("rawAudio");
        }

        // This fires mediaPlayer.prepare(), so next code to execute should be onPrepared listener
        prepMediaPlayer(base64String, call);
    }

    /**
     * Stops all audio
     * @param call
     */
    @PluginMethod(returnType = PluginMethod.RETURN_PROMISE)
    public void stop(PluginCall call) {
        JSObject res = new JSObject();

        if (mediaPlayer == null) {
            res.put("msg", "Audio hasn't started playing yet.");
            res.put("ok", true);
            res.put("done", true);
            call.resolve(res);

            return;
        }

        try {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer = null;

            res.put("msg", "Audio stopped");
            res.put("ok", true);
            res.put("done", true);
            call.resolve(res);
        } catch (Exception e) {
            System.out.println("Exception stopping audio: " + e.getMessage());
            call.reject(e.getMessage());
        }
    }

    @PluginMethod(returnType = PluginMethod.RETURN_PROMISE)
    public void bingbong(PluginCall call) {
        AssetManager assetManager = getContext().getAssets();
        try {
            String filename = "bingbong.txt";
            InputStream file = assetManager.open(filename);

            final int bufferSize = 1024;
            final char[] buffer = new char[bufferSize];
            final StringBuilder out = new StringBuilder();

            try {
                Reader in = new InputStreamReader(file, "UTF-8");
                for (; ; ) {
                    int rsz = in.read(buffer, 0, buffer.length);
                    if (rsz < 0)
                        break;
                    out.append(buffer, 0, rsz);
                }

                String result = out.toString();

                prepMediaPlayer(result, call);

                JSObject bong = new JSObject();
                bong.put("ok", true);
                bong.put("done", true);
                bong.put("msg", "bingbong");
            }
            catch(UnsupportedEncodingException err) {
                System.out.println(err);
            }

            catch(IOException err) {
                System.out.println(err);
            }
        }
        catch (IOException err) {
            System.out.println(err);
        }
    }

    /**
     * Gets input stream
     * @param filepath
     * @return
     */
    public String getAssets(int filepath) {
        AssetManager assetManager = getContext().getAssets();
        try {
            String filename = filepath + ".txt";
            InputStream file = assetManager.open(filename);
            String result = streamToString(file);
            return result;
        }
        catch (IOException err) {
            System.out.println(err);
        }

        return "";
    }

    /**
     * Turns an input stream into a base64, usable string for audio
     * @param is
     * @return
     */
    public String streamToString(InputStream is) {
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();

        try {
            Reader in = new InputStreamReader(is, "UTF-8");
            for (; ; ) {
                int rsz = in.read(buffer, 0, buffer.length);
                if (rsz < 0)
                    break;
                out.append(buffer, 0, rsz);
            }

            return out.toString();
        }
        catch(UnsupportedEncodingException err) {
            System.out.println(err);
        }

        catch(IOException err) {
            System.out.println(err);
        }

        return "";
    }

    /**
     * Prepare media player. This function completes and will ultimately trigger the
     * onprepared event
     * @param base64
     * @param call
     */
    public void prepMediaPlayer(String base64, PluginCall call) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }

        // just like in js/html!
        String url = "data:audio/mp3;base64," + base64;

        // set source, prepare, ensure volume and looping are appropriate
        // once .prepare() is called, we then wait until onPrepared() listener is fired.
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.setVolume(100f, 100f);
            mediaPlayer.setLooping(false);
        }
        catch (Exception e) {
            System.out.println("\nException!!");
            System.out.println(e.getMessage());
            call.setKeepAlive(false);
            call.reject(e.getMessage());
            e.printStackTrace();
        }

        try {
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer player) {
                    JSObject res = new JSObject();
                    // mediaPlayer is ready to go, play the audio
                    // next, waiting for onCompletionListener to fire.
                    player.start();

                    // let the client know we've started audio
                    res.put("msg", "Audio started");
                    res.put("ok", true);
                    res.put("done", false);
                    call.resolve(res);
                }
            });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    JSObject res = new JSObject();

                    if (mp.isPlaying()) {
                        mp.stop();
                    }

                    // reset our player
                    mp.reset();
                    mediaPlayer = null;

                    if (currentlyPlayingBluetoothBuffer) {
                        // bluetooth offset just completed
                        // prepare again but this time use the txt-to-speech base64
                        String txtToSpeechB64 = call.getString("rawAudio");
                        prepMediaPlayer(txtToSpeechB64, call);

                        // flip the flag
                        currentlyPlayingBluetoothBuffer = false;

                        res.put("msg", "Bluetooth offset finished, starting txt to speech...");
                        res.put("ok", true);
                        res.put("done", false);
                    }
                    else {
                        // this is the last audio track we need to play
                        call.setKeepAlive(false);

                        res.put("msg", "Audio finished playing");
                        res.put("ok", true);
                        res.put("done", true);
                    }

                    // inform client of our status
                    call.resolve(res);
                }
            });
        }
        catch(Exception e){
            System.out.println("\nException!!");
            System.out.println(e.getMessage());
            call.setKeepAlive(false);
            call.reject(e.getMessage());
            e.printStackTrace();
        }
    }
}
