package com.xiaomi.emm.features.white;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import com.pocketdigi.utils.FLameUtils;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.TheTang;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AudioRecorderService extends Service {

    String TAG = "AudioRecorderService";
    static String telephoneNumber = "telephone_number";
    String callNumber = null;
    private static int RECORD_RATE = 0;
    private static int RECORD_BPP = 32;
    private AudioRecord audioRecorder = null;
    private Boolean isRecording = false;
    private int bufferEle = 1024;
    private static int[] recordRate = {44100, 8000, 11025, 16000, 22050, 32000, 47250, 48000};
    int bufferSize = 0;

    String callRecorderPath = Environment.getExternalStorageDirectory() + "callRecorder_" + callNumber + "_"
            + TheTang.getSingleInstance().formatTime(System.currentTimeMillis()) + ".mp3";
    String tempCallRecorderPath = Environment.getExternalStorageDirectory() + "tempCallRecorder.raw";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        callNumber = intent.getStringExtra(telephoneNumber);

        if (!isRecording) {
            startRecord();
        } else {
            Toast.makeText(this, "Recording had started", Toast.LENGTH_SHORT).show();
        }
        return START_REDELIVER_INTENT;
    }

    private void startRecord() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                audioRecorder = initializeRecord();
                if (audioRecorder != null) {
                    try {
                        audioRecorder.startRecording();
                        TheTang.getSingleInstance().showToastByRunnable(TheTang.getSingleInstance().getContext(), "Recording is start", Toast.LENGTH_SHORT);
                    } catch (Exception e) {
                    }
                } else {
                    return;
                }

                isRecording = true;
                writeToFile();
            }
        }, "Recording Thread").start();

    }

    private AudioRecord initializeRecord() {
        short[] audioFormat = new short[]{AudioFormat.ENCODING_PCM_16BIT, AudioFormat.ENCODING_PCM_8BIT};
        short[] channelConfiguration = new short[]{AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO};
        for (int rate : recordRate) {
            for (short aFormat : audioFormat) {
                for (short cConf : channelConfiguration) {
                    try {
                        int buffSize = AudioRecord.getMinBufferSize(rate, cConf, aFormat);
                        bufferSize = buffSize;
                        if (buffSize != AudioRecord.ERROR_BAD_VALUE) {
                            AudioRecord aRecorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, rate, cConf, aFormat, buffSize);
                            if (aRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
                                RECORD_RATE = rate;
                                return aRecorder;
                            } else {
                                aRecorder.release();
                            }
                        }
                    } catch (Exception e) {
                        Log.w(TAG, e.getCause().toString());
                    }
                }
            }
        }
        return null;
    }

    private void writeToFile() {

        short[] buffDate = new short[bufferEle];
        DataOutputStream dos = null;
        int readSize = 0;

        try {

            File recordFile = getTempFile();
            if (recordFile.exists()) {
                recordFile.delete();
                recordFile.createNewFile();
            }

            dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(recordFile)));

            while (isRecording) {
                readSize = audioRecorder.read(buffDate, 0, bufferEle);
                for (int i = 0; i < readSize; i++) {
                    dos.writeShort(buffDate[i]);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private File getTempFile() {
        File tempFile = new File(tempCallRecorderPath);
        return tempFile;
    }

    private void convertRawToMP3File() {
        FLameUtils mFLameUtils = new FLameUtils(1, RECORD_RATE, RECORD_BPP * RECORD_RATE * 1 / 8);
        mFLameUtils.raw2mp3(tempCallRecorderPath, callRecorderPath);
    }

    private void deletTempFile() {
        File file = getTempFile();
        file.delete();
    }

    @Override
    public void onDestroy() {
        if (isRecording) {
            stopRecord();
        } else {
            Toast.makeText(this, "Recording had stopped", Toast.LENGTH_SHORT).show();
        }
        super.onDestroy();
    }

    private void stopRecord() {
        if (null != audioRecorder) {
            isRecording = false;
            audioRecorder.stop();
            audioRecorder.release();
            audioRecorder = null;
            Toast.makeText(this, "Recording had stopped", Toast.LENGTH_LONG).show();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                convertRawToMP3File();
                deletTempFile();
            }
        }).start();
    }

}