package com.getcapacitor.community.audio;

import android.Manifest;
import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;

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
    AudioManager am = null;
    AudioAttributes mPlaybackAttributes = null;
    AudioFocusRequest mAudioFocusRequest = null;

    // class-level flag for if we're using a bluetooth offset or not
    Boolean currentlyPlayingBluetoothBuffer = false;

    Boolean playbackDelayed = false;
    Boolean playbackNowAuthorized = false;
    Boolean resumeOnFocusGain = false;
    final Object focusLock = new Object();

    /**
     * Gets offset base64 txt if provided, if not, gets base64 string from call
     * Prepares mediaPlayer for playback
     * Once prepared, either:
     * A) bluetoothBuffer base64 white noise is played
     * B) base64 from call is played
     * If bluetoothBuffer was played, the onCompletion listener will trigger the preparation
     * of the next audio (call base64), and the loop runs one more time
     * Call resolves
     */
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
        } else {
            // get the txt-to-speech base64 string
            base64String = call.getString("rawAudio");
        }

        prePlayFocusChangeRequest(base64String, call);
    }

    public void prePlayFocusChangeRequest(String base64, PluginCall call) {
        System.out.println("SNA > playfocuschangerequest");
        if (am == null) {
            System.out.println("SNA > setting audio manager");
            am = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        }

        if (mediaPlayer == null) {
            System.out.println("SNA > setting media player");
            mPlaybackAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(mPlaybackAttributes);
        }

        int res = 0;
        System.out.println("SNA > defining onaudiofocuschangelistener handler");
        AudioManager.OnAudioFocusChangeListener handler = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        System.out.println("SNA > audiofocus gain!");
                        if (playbackDelayed || resumeOnFocusGain) {
                            synchronized(focusLock) {
                                playbackDelayed = false;
                                resumeOnFocusGain = false;
                            }
                            prepMediaPlayer(base64, call);
                        }
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        System.out.println("SNA > audiofocus loss!");
                        synchronized(focusLock) {
                            resumeOnFocusGain = false;
                            playbackDelayed = false;
                        }
                        // stop(call);
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        System.out.println("SNA > audiofocus loss transient granted!");
                        synchronized(focusLock) {
                            // only resume if playback is being interrupted
                            resumeOnFocusGain = mediaPlayer.isPlaying();
                            playbackDelayed = false;
                        }
                        // stop(call);
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        System.out.println("SNA > can duck audiofocus!");
                        // ... pausing or ducking depends on your app
                        break;
                }
            }
        };

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            System.out.println("SNA > new api making audio focus request");
            mAudioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(mPlaybackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(handler)
                .build();
            res = am.requestAudioFocus(mAudioFocusRequest);
        }
        else {
            System.out.println("SNA > old api making audio focus request");
            res = am.requestAudioFocus(handler, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
        }

        System.out.println("SNA > equest result");
        System.out.println(res);
        System.out.println("SNA > ^^^");

        synchronized(focusLock) {
            if (res == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                System.out.println("SNA > Request failed!");
                playbackNowAuthorized = false;
            } else if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                System.out.println("SNA > Request granted!");
                playbackNowAuthorized = true;
                prepMediaPlayer(base64, call);
            } else if (res == AudioManager.AUDIOFOCUS_REQUEST_DELAYED) {
                System.out.println("SNA > Request delayed!");
                playbackDelayed = true;
                playbackNowAuthorized = false;
            }
        }
    }

    /**
     * Prepare media player. This function completes and will ultimately trigger the
     * onprepared event
     * @param base64
     * @param call
     */
    public void prepMediaPlayer(String base64, PluginCall call) {
        System.out.println("SNA > prepmediaplayer");

        String url = "data:audio/mp3;base64," + base64;

        // set source, prepare, ensure volume and looping are appropriate
        // once .prepare() is called, we then wait until onPrepared() listener is fired.
        try {
            System.out.println("SNA > doing stuff");
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.setVolume(100f, 100f);
            mediaPlayer.setLooping(false);
            System.out.println("SNA > stuff done...");
        }
        catch (Exception e) {
            System.out.println("SNA > oof");
            System.out.println("\nException!!");
            System.out.println(e.getMessage());
            call.setKeepAlive(false);
            call.reject(e.getMessage());
            e.printStackTrace();
        }

        try {
            System.out.println("SNA > making onprepartedlistener");
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer player) {
                    System.out.println("SNA > onprepared, playing...");
                    JSObject res = new JSObject();
                    // mediaPlayer is ready to go, play the audio
                    // next, waiting for onCompletionListener to fire.
                    System.out.println("SNA > start...");
                    player.start();

                    // let the client know we've started audio
                    res.put("msg", "Audio started");
                    res.put("ok", true);
                    res.put("done", false);
                    System.out.println("SNA > resolving call...");
                    call.resolve(res);
                }
            });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    JSObject res = new JSObject();

                    if (mp.isPlaying()) {
                        System.out.println("SNA > stopping");
                        mp.stop();
                    }

                    System.out.println("SNA > abandoning focus");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        am.abandonAudioFocusRequest(mAudioFocusRequest);
                    }
                    else {
                        am.abandonAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
                            @Override
                            public void onAudioFocusChange(int i) {
                                System.out.println("SNA > abandoned old api");
                                System.out.println(i);
                                System.out.println("SNA > ^^");
                                return;
                            }
                        });
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
            System.out.println("SNA > big oof!!");
            System.out.println("\nException!!");
            System.out.println(e.getMessage());
            call.setKeepAlive(false);
            call.reject(e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Stops all audio
     *
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

    /**
     * Turns an input stream into a base64, usable string for audio
     *
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
        } catch (UnsupportedEncodingException err) {
            System.out.println(err);
        } catch (IOException err) {
            System.out.println(err);
        }

        return "";
    }

    /**
     * Gets input stream
     *
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
        } catch (IOException err) {
            System.out.println(err);
        }

        return "";
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
            } catch (UnsupportedEncodingException err) {
                System.out.println(err);
            } catch (IOException err) {
                System.out.println(err);
            }
        } catch (IOException err) {
            System.out.println(err);
        }
    }
}
