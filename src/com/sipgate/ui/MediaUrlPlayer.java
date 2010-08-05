package com.sipgate.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import com.sipgate.api.types.Voicemail;
import com.sipgate.exceptions.DownloadException;
import com.sipgate.util.ApiServiceProvider;
import com.sipgate.util.Constants;

public class MediaUrlPlayer {
	private static final String TAG = "Mediaplayer";
	
	public static void play (Voicemail vm, Context context){
		try {
			String localName = download(vm, context);
			MediaPlayer mp = new MediaPlayer();
			mp.setDataSource(localName);
			mp.prepare();
			mp.start();
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DownloadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
		
	/**
	 * Downloads voicemail mp3 and returns local path
	 * 
	 * @param Voicemail
	 * @return
	 * @throws VoicemailHelperDownloadException
	 */
	public static String download(Voicemail voiceMail, Context context) throws DownloadException {
		File voicemailFile = null;
		setupDownloadDir();
		voicemailFile = new File(Constants.MP3_DOWNLOAD_DIR, voiceMail.getVoicemail_id() + ".mp3");
		if (! voicemailFile.exists()){
			try {
				InputStream inputStream = null;
				
				ApiServiceProvider apiClient = ApiServiceProvider.getInstance(context);
				Log.d(TAG, voiceMail.getContent_url());
				inputStream = apiClient.getVoicemail(voiceMail.getContent_url());
				if (inputStream == null) {
					throw new RuntimeException("stream is null");
				}

				if (!voicemailFile.createNewFile()) {
					Log.e(TAG, "unable to create " + voicemailFile.getAbsolutePath());
					throw new DownloadException();
				}

				FileOutputStream out = new FileOutputStream(voicemailFile);
				byte buf[] = new byte[1024];
				do {
					int numread = inputStream.read(buf);
					if (numread <= 0)
						break;
					out.write(buf, 0, numread);
				} while (true);
			} catch (Exception e) {
				String msg = e.getLocalizedMessage();
				if (msg == null) {
					msg = e.getClass().getName();
				}
				e.printStackTrace();
				Log.e(TAG, msg);
				
				throw new DownloadException();
			}
			Log.d(TAG, String.format("downloaded voicemail %s as %s", 
					voiceMail.getVoicemail_id() , voicemailFile.getAbsolutePath()));
		} else {
			Log.d(TAG, String.format("cached voicemail %s as %s", 
					voiceMail.getVoicemail_id() , voicemailFile.getAbsolutePath()));
		}

		return voicemailFile.getAbsolutePath();
	}

	private static void setupDownloadDir() throws DownloadException {
		File dir = new File(Constants.MP3_DOWNLOAD_DIR);
		if (!dir.exists()) {
			Log.d(TAG, Constants.MP3_DOWNLOAD_DIR
					+ " does not exist; will try to create ...");
			if (!dir.mkdirs()) {
				Log.e(TAG, "unable to create " + dir.getAbsolutePath());
				throw new DownloadException();
			}
		}
	}

}
