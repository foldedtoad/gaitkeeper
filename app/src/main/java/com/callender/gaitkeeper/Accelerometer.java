package com.callender.gaitkeeper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;


public class Accelerometer extends Activity implements SensorEventListener, OnClickListener {

    private static final String TAG = "ACCEL";

    SensorManager sensorManager;
    Sensor accelerometer = null;

    XYPlot dynamicPlot;

    private SimpleXYSeries axis_X_Series;
    private SimpleXYSeries axis_Y_Series;
    private SimpleXYSeries axis_Z_Series;

    private static final int HISTORY_SIZE = 30;
    private static final int SCALE_FACTOR = 10;
    private static final int DELAY_TIME = 5000; // 5 second delay

    File dir = null;
    String filename = null;
    File dataFile = null;
    private FileWriter writer;

    Button btnStart, btnStop;

    RadioButton radioButton;
    RadioGroup  radioGroup = null;

    ToneGenerator tone = null;

    {
        axis_X_Series = new SimpleXYSeries("X");
        axis_Y_Series = new SimpleXYSeries("Y");
        axis_Z_Series = new SimpleXYSeries("Z");

        axis_X_Series.useImplicitXVals();
        axis_Y_Series.useImplicitXVals();
        axis_Z_Series.useImplicitXVals();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        tone = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

        /*  Plot initialization */
        dynamicPlot = findViewById(R.id.dynamicPlot);

        initializePlot();

        dynamicPlot.clear();

        // set up buttons
        btnStart = findViewById(R.id.btnStart);
        btnStop  = findViewById(R.id.btnStop);

        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);

        btnStart.setEnabled(true);
        btnStop.setEnabled(false);

        radioGroup = findViewById(R.id.radioGait);

        // Create/access "external_sd" directory (external SD card (flash) device)

        File sdCard = Environment.getExternalStorageDirectory();
        String dirpath = sdCard.getAbsolutePath() + "/external_sd";
        Boolean dirsMade = true;

        dir = new File(dirpath);

        if (!dir.exists()) {
            // NOTE: mkdir returns false if directory exists, e.g. it wasn't created.
            dirsMade = dir.mkdir();
        }

        if (!dirsMade) {
            Log.e("ACCEL", " ***** dir.mkdir() failed: " + dirpath);
        }
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);

        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                Log.e(TAG, "close failed");
            }
            writer = null;
        }
    }

    private void initializePlot() {

        if (dynamicPlot == null)
            throw new RuntimeException("no instance of AndroidPlot");

        dynamicPlot.getGraph().getGridBackgroundPaint().setColor(Color.WHITE);
        dynamicPlot.getGraph().getDomainGridLinePaint().setColor(Color.BLACK);
        dynamicPlot.getGraph().getDomainOriginLinePaint().setColor(Color.BLACK);
        dynamicPlot.getGraph().getDomainGridLinePaint().setPathEffect(new DashPathEffect(new float[]{1, 1}, 1));

        dynamicPlot.getGraph().getRangeGridLinePaint().setColor(Color.BLACK);
        dynamicPlot.getGraph().getRangeGridLinePaint().setPathEffect(new DashPathEffect(new float[]{1, 1}, 1));
        dynamicPlot.getGraph().getRangeOriginLinePaint().setColor(Color.BLACK);

        // only display whole numbers in domain labels
        dynamicPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new DecimalFormat("0"));

        LineAndPointFormatter formatter_X =
                new LineAndPointFormatter(Color.GREEN, null, null, null);

        formatter_X.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
        formatter_X.getLinePaint().setStrokeWidth(3);

        dynamicPlot.addSeries(axis_X_Series, formatter_X);

        LineAndPointFormatter formatter_Y =
                new LineAndPointFormatter(Color.BLUE, null, null, null);

        formatter_Y.getLinePaint().setStrokeWidth(3);
        formatter_Y.getLinePaint().setStrokeJoin(Paint.Join.ROUND);

        dynamicPlot.addSeries(axis_Y_Series, formatter_Y);

        LineAndPointFormatter formatter_Z =
                new LineAndPointFormatter(Color.RED, null, null, null);

        formatter_Z.getLinePaint().setStrokeWidth(3);
        formatter_Z.getLinePaint().setStrokeJoin(Paint.Join.ROUND);

        dynamicPlot.addSeries(axis_Z_Series, formatter_Z);

        // thin out domain tick labels so they dont overlap each other:
        dynamicPlot.setDomainStepMode(StepMode.INCREMENT_BY_VAL);
        dynamicPlot.setDomainStepValue(5);

        dynamicPlot.setRangeStepMode(StepMode.INCREMENT_BY_VAL);
        dynamicPlot.setRangeStepValue(20);

        dynamicPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).setFormat(new DecimalFormat("###.#"));

        dynamicPlot.setRangeBoundaries(-120, 120, BoundaryMode.FIXED);
        dynamicPlot.setDomainBoundaries(0, HISTORY_SIZE, BoundaryMode.FIXED);
    }

    /*
     *   Retrieve the label text for the currently selected radio button --
     */
    public String getGaitLabel() {

        // get selected radio button from radioGroup
        if (radioGroup == null)
            throw new RuntimeException("radioGroup is null");
        int selectedId = radioGroup.getCheckedRadioButtonId();
        radioButton = findViewById(selectedId);

        return (String) radioButton.getText();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // empty function on purpose.
    }

    /*
     *   Receive sensor update events (gaitkeeper)
     *   and write event timestamp+data to current sd-card file.
     *   Also display event values on graph plot.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

        // Remove oldest vector data.
        if (axis_X_Series.size() > HISTORY_SIZE)
            axis_X_Series.removeFirst();

        if (axis_Y_Series.size() > HISTORY_SIZE) {
            axis_Y_Series.removeFirst();
        }
        if (axis_Z_Series.size() > HISTORY_SIZE) {
            axis_Z_Series.removeFirst();
        }

        // Add the latest value to lists.
        axis_X_Series.addLast(null, event.values[0] * SCALE_FACTOR);
        axis_Y_Series.addLast(null, event.values[1] * SCALE_FACTOR);
        axis_Z_Series.addLast(null, event.values[2] * SCALE_FACTOR);

        // redraw the plots:
        dynamicPlot.redraw();

        // Write data to dataFile
        long timestamp = System.currentTimeMillis();

        AccelData data = new AccelData(timestamp, event.values[0], event.values[1], event.values[2]);

        if (writer != null) {
            try {
                writer.write(data.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     *   Start recording gaitkeeper events to sd-card file --
     */
    private void startRecording() {

        String gait = getGaitLabel();

        // save prev data if available
        try {
            if (dataFile == null) {
                try {
                    long timestamp = System.currentTimeMillis();
                    filename = gait + "_" + timestamp + ".csv";
                    dataFile = new File(dir, filename);
                    Log.i(TAG, "startRecording: file: " + filename);
                    writer = new FileWriter(dataFile, false);
                } catch (IOException e) {
                    Log.e(TAG, "startRecording: dataFile open status: " + dataFile);
                }
            }
            else {
                Log.i(TAG, "startRecording: dataFile is null on btnStart event ********");
            }
        } catch (Exception e) {
            Log.e(TAG, "Start failed: ");
        }

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    /*
     *   Stop recording gaitkeeper events and close sd-card file --
     */
    private void stopRecording(){

        btnStart.setEnabled(true);
        btnStop.setEnabled(false);

        if (radioGroup == null)
            throw new RuntimeException("radioGroup is null");

        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            radioGroup.getChildAt(i).setEnabled(true);
        }

        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                Log.e(TAG, "close failed");
            }
            Log.i(TAG, "stopRecording: file: " + filename);
            writer = null;
            dataFile = null;
        }
        else {
            Log.i(TAG, "writer is null on btnStop event ********");
        }

        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }

        dynamicPlot.clear();

        tone.startTone(ToneGenerator.TONE_PROP_ACK,250);
    }

    /*
     *   Delay the start of recording by delayTime milliseconds --
     */
    private void startDelay(int delayTime) {

        if (delayTime < 1000) {
            throw new RuntimeException("Invalid delayTimer value");
        }

        initializePlot();

        if (radioGroup == null)
            throw new RuntimeException("radioGroup is null");

        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            radioGroup.getChildAt(i).setEnabled(false);
        }

        new CountDownTimer(delayTime, 300) {

            @Override
            public void onTick(long millisUntilFinished) {

                Button button = findViewById(R.id.btnStart);

                if (button.getVisibility() == View.INVISIBLE) {
                    button.setVisibility(View.VISIBLE);
                    tone.startTone(ToneGenerator.TONE_CDMA_PIP,150);
                }
                else {
                    button.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onFinish() {

                Button button = findViewById(R.id.btnStart);
                button.setVisibility(View.VISIBLE);

                btnStart.setEnabled(false);
                btnStop.setEnabled(true);

                tone.startTone(ToneGenerator.TONE_PROP_ACK,250);

                startRecording();
            }
        }.start();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btnStart:
                startDelay(DELAY_TIME);
                break;

            case R.id.btnStop:
                stopRecording();
                break;

            default:
                throw new RuntimeException("Unrecognised button ID");
        }

    }
}
