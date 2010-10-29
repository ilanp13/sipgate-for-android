package com.sipgate.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.media.MediaPlayer;
import android.widget.MediaController.MediaPlayerControl;

public class MediaConnector implements MediaPlayerControl 
{
	private MediaPlayer mediaPlayer;

	public MediaConnector() {
		mediaPlayer = new MediaPlayer();
	}

	public int getBufferPercentage() {
		return 0;
	}

	public int getCurrentPosition() {
		return mediaPlayer.getCurrentPosition();
	}

	public int getDuration() {
		return mediaPlayer.getDuration();
	}

	public boolean isPlaying() {
		return mediaPlayer.isPlaying();
	}

	public void pause() {
		mediaPlayer.pause();
	}

	public void seekTo(int pos) {
		mediaPlayer.seekTo(pos);
	}

	public void setMp3(File cacheDir, byte[] data) {
		
		mediaPlayer.stop();
		mediaPlayer = new MediaPlayer();
		
		try {
			File temp = File.createTempFile("data", null);
			FileOutputStream out = new FileOutputStream(temp);
			
			out.write(data);
			out.close();
			
			mediaPlayer.setDataSource(temp.getAbsolutePath());
			mediaPlayer.prepare();
			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		mediaPlayer.start();
	}

	public boolean canPause() {
		return true;
	}

	public boolean canSeekBackward() {
		return true;
	}

	public boolean canSeekForward() {
		return true;
	}
}
