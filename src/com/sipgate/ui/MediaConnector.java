package com.sipgate.ui;

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

	public void setMp3(String location) {
		mediaPlayer.stop();
		// TODO: find out, why this new instance is needed; seems not to
		// work with
		// existing MediaPlayer instance
		mediaPlayer = new MediaPlayer();
		try {
			mediaPlayer.setDataSource(location);
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
