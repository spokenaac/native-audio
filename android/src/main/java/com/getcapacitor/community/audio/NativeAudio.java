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

  @PluginMethod(returnType = PluginMethod.RETURN_CALLBACK)
  public void playRaw(PluginCall call) {
//    call.setKeepAlive(true);
    JSObject res = new JSObject();

    String base64String = call.getString("rawAudio");

    try{
      String url = "data:audio/mp3;base64," + base64String;
      MediaPlayer mediaPlayer = new MediaPlayer();

      try {
        mediaPlayer.setDataSource(url);
        mediaPlayer.prepareAsync();
        mediaPlayer.setVolume(100f, 100f);
        mediaPlayer.setLooping(false);
      } catch (IllegalArgumentException e) {
        System.out.println("You might not set the DataSource correctly!");
        e.printStackTrace()
      } catch (SecurityException e) {
        System.out.println("You might not set the DataSource correctly!");
        e.printStackTrace()
      } catch (IllegalStateException e) {
        System.out.println("You might not set the DataSource correctly!");      } catch (IOException e) {
        e.printStackTrace();
      }

      mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
          @Override
          public void onPrepared(MediaPlayer player) {
              player.start();
          }
      });

      mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
          @Override
          public void onCompletion(MediaPlayer mp) {
              mp.stop();
              mp.release();
          }
      });
    }
    catch(Exception e){
        e.printStackTrace();
    }
  }
}
