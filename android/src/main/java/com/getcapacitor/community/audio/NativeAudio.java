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
import java.util.Timer;
import java.util.TimerTask;

@CapacitorPlugin(
  permissions = {
    @Permission(strings = { Manifest.permission.MODIFY_AUDIO_SETTINGS }),
    @Permission(strings = { Manifest.permission.WRITE_EXTERNAL_STORAGE }),
    @Permission(strings = { Manifest.permission.READ_PHONE_STATE }),
  }
)
public class NativeAudio extends Plugin {

    public static final String TAG = "NativeAudio";
    MediaPlayer mediaPlayer = null;

    @PluginMethod(returnType = PluginMethod.RETURN_CALLBACK)
    public void playRaw(PluginCall call) {
        call.setKeepAlive(true);
        JSObject res = new JSObject();

        String base64String = call.getString("rawAudio");

        try {
          String url = "data:audio/mp3;base64," + base64String;
          if (mediaPlayer == null) {
              mediaPlayer = new MediaPlayer();
          }

          try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.setVolume(100f, 100f);
            mediaPlayer.setLooping(false);
          } catch (Exception e) {
              System.out.println("\nException!!");
              System.out.println(e.getMessage());
              call.setKeepAlive(false);
              call.reject(e.getMessage());
              e.printStackTrace();
          }

          mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
              @Override
              public void onPrepared(MediaPlayer player) {
                  player.start();
                  res.put("msg", "Audio started");
                  res.put("ok", true);
                  res.put("done", false);
              }
          });

          mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
              @Override
              public void onCompletion(MediaPlayer mp) {
                  call.setKeepAlive(false);

                  if (mp.isPlaying()) {
                      mp.stop();
                  }
                  mp.reset();
                  mp = null;
                  mediaPlayer = null;

                  res.put("msg", "Audio finished playing");
                  res.put("ok", true);
                  res.put("done", true);
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

    @PluginMethod(returnType = PluginMethod.RETURN_CALLBACK)
    public void calibrateBluetooth(PluginCall call) {
        call.setKeepAlive(true);
        JSObject res = new JSObject();

        Integer bluetoothOffset = call.getInt("bluetoothOffset");

        String base64String = getAssets(bluetoothOffset);

        try {
            String url = "data:audio/mp3;base64," + base64String;
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            }

            try {
                mediaPlayer.setDataSource(url);
                mediaPlayer.prepare();
                mediaPlayer.setVolume(100f, 100f);
                mediaPlayer.setLooping(false);
            } catch (Exception e) {
                call.setKeepAlive(false);
                call.reject(e.getMessage());
                e.printStackTrace();
            }

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer player) {
                    player.start();

                    res.put("msg", "Audio started");
                    res.put("ok", true);
                    res.put("done", false);
                }
            });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    call.setKeepAlive(false);

                    if (mp.isPlaying()) {
                        mp.stop();
                    }
                    mp.reset();
                    mp = null;
                    mediaPlayer = null;

                    res.put("msg", "Audio finished playing");
                    res.put("ok", true);
                    res.put("done", true);
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

    @PluginMethod(returnType = PluginMethod.RETURN_PROMISE)
    public void stop(PluginCall call) {
      JSObject res = new JSObject();

      if (mediaPlayer == null) {
          res.put("msg", "Audio hasn't started playing yet.");
          res.put("ok", true);
          res.put("done", true);
          call.resolve(res);
      }

      try {
          mediaPlayer.stop();
          mediaPlayer.reset();

          res.put("msg", "Audio stopped");
          res.put("ok", true);
          res.put("done", true);
          call.resolve(res);
      } catch (Exception e) {
          System.out.println("Exception stopping audio: " + e.getMessage());
          call.reject(e.getMessage());
      }
    }

    public String getAssets(int filepath) {
        AssetManager assetManager = getContext().getAssets();
        try {
            String filename = filepath + ".txt";
            InputStream file = assetManager.open(filename);
            String result = isToString(file);
            return result;
        }
        catch (IOException err) {
            System.out.println(err);
        }

        return "";
    }

    public String isToString(InputStream is) {
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
}
