package com.getcapacitor.community.audio;

import android.Manifest;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Base64;
import android.util.Log;
import android.media.AudioTrack;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

@CapacitorPlugin(
  permissions = {
    @Permission(strings = { Manifest.permission.MODIFY_AUDIO_SETTINGS }),
    @Permission(strings = { Manifest.permission.WRITE_EXTERNAL_STORAGE }),
    @Permission(strings = { Manifest.permission.READ_PHONE_STATE }),
  }
)
public class NativeAudio
  extends Plugin {

  public static final String TAG = "NativeAudio";
  MediaPlayer mediaPlayer = null;

  @PluginMethod(returnType = PluginMethod.RETURN_CALLBACK)
  public void playRaw(PluginCall call) {
    call.setKeepAlive(true);
    JSObject res = new JSObject();

    String base64String = call.getString("rawAudio");

    try{
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
  public void stop(PluginCall call) {
    return
  }
}
