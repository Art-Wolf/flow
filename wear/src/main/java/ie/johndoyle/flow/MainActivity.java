package ie.johndoyle.flow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity implements SensorEventListener {

    private float lastX, lastY, lastZ, lastGX, lastGY, lastGZ;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyro;

    Calendar calendar = Calendar.getInstance();

    Date now = calendar.getTime();
    private long starttime = now.getTime();

    private float trackX = 0;
    private float trackY = 0;
    private float trackZ = 0;

    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;

    private float deltaGX = 0;
    private float deltaGY = 0;
    private float deltaGZ = 0;

    private float vibrateThreshold = 0;

    private TextView currentX, currentY, currentZ, currentGX, currentGY, currentGZ, nextSlide, mTextView;

    public Vibrator v;

    private int startpresentation = 0;
    private int training = 0;

    private static final int SPEECH_REQUEST_CODE = 0;

    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);

            if (spokenText.equals("presentation")) {
                Log.i("Flow-Present-Start", spokenText);
                startpresentation = 1;
            }
            if (spokenText.equals("start")) {
                Log.i("Flow-Training", spokenText);
                training = 1;
            }

            if (spokenText.equals("stop")) {
                Log.i("Flow-Training", spokenText);
                training = 0;
                startpresentation = 0;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initializeViews();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL);

            vibrateThreshold = accelerometer.getMaximumRange() / 2;
        }

        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.accelerationTitle);
                mTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        displaySpeechRecognizer();
                    }
                });
            }
        });
    }

    public void initializeViews() {
//        currentX = (TextView) findViewById(R.id.currentX);
//        currentY = (TextView) findViewById(R.id.currentY);
//        currentZ = (TextView) findViewById(R.id.currentZ);
//        currentGX = (TextView) findViewById(R.id.currentGX);
//        currentGY = (TextView) findViewById(R.id.currentGY);
//        currentGZ = (TextView) findViewById(R.id.currentGZ);
//        nextSlide = (TextView) findViewById(R.id.nextSlide);
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL);
    }
    //onPause() unregister the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        Sensor sensor = event.sensor;

        // clean current values
        displayCleanValues();
        // display the current x,y,z accelerometer values
        displayCurrentValues();

        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER && training == 1) {
            calendar = Calendar.getInstance();
            now = calendar.getTime();
            Timestamp timestampNow = new Timestamp(now.getTime());
            Log.i("Flow-Training", timestampNow.toString() + "\t " +
                       Float.toString(event.values[0]) + "\t" +
                       Float.toString(event.values[1]) + "\t" +
                       Float.toString(event.values[2]));
        }
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER && startpresentation == 1) {

            // get the change of the x,y,z values of the accelerometer
            deltaX = event.values[0];//Math.abs(lastX - event.values[0]);
            deltaY = event.values[1];
            deltaZ = event.values[2];

            lastX = event.values[0];
            lastY = event.values[1];
            lastZ = event.values[2];


            if (deltaY < -4 && trackY == 0) {
                trackY = 1;
                calendar = Calendar.getInstance();
                now = calendar.getTime();
                starttime = now.getTime();
                //Log.i("Flow", "Initial stage!");
                //Log.i("Flow", "starttime: " + starttime);
                v.vibrate(100);
            }

            if (trackY ==1 && deltaY > 1) {
                trackY = 2;
                //Log.i("Flow", "Success stage!");
            }

            //if ((deltaZ > vibrateThreshold) || (deltaY > vibrateThreshold) || (deltaZ > vibrateThreshold)) {
            if (trackY == 2) {

                calendar = Calendar.getInstance();
                now = calendar.getTime();
                long currenttime = now.getTime();
                if (((starttime - currenttime)/1000) >= -1) {
                    Log.i("Flow", "Next Slide: Y");

                    v.vibrate(100);
                }
                //Log.i("Flow", "starttime: " + starttime);
                //Log.i("Flow", "currenttime: " + currenttime);
                //Log.i("Flow", "Duration: " + (starttime - currenttime)/1000);
                //Log.i("Flow", "X:" + Float.toString(deltaX));
                //Log.i("Flow", "Y:" + Float.toString(deltaY));
                //Log.i("Flow", "Z:" + Float.toString(deltaZ));
                trackY = 0;
            }
        }
//        else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
//            // get the change of the x,y,z values of the accelerometer
//            deltaGX = Math.abs(lastGX - event.values[0]);
//            deltaGY = Math.abs(lastGY - event.values[1]);
//            deltaGZ = Math.abs(lastGZ - event.values[2]);
//
//            lastGX = event.values[0];
//            lastGY = event.values[1];
//            lastGZ = event.values[2];
//
//            // if the change is below 2, it is just plain noise
//            if (deltaGX < 2)
//                deltaGX = 0;
//            if (deltaGY < 2)
//                deltaGY = 0;
//            if ((deltaGZ > vibrateThreshold) || (deltaGY > vibrateThreshold) || (deltaGZ > vibrateThreshold)) {
//                v.vibrate(50);
//            }
//        }

    }

    public void displayCleanValues() {

    //    currentX.setText("0.0");
    //    currentY.setText("0.0");
    //    currentZ.setText("0.0");
    //    currentGX.setText("0.0");
    //    currentGY.setText("0.0");
    //    currentGZ.setText("0.0");
    }

    // display the current x,y,z accelerometer values
    public void displayCurrentValues() {
    //    currentX.setText(Float.toString(deltaX));
    //    currentY.setText(Float.toString(deltaY));
    //    currentZ.setText(Float.toString(deltaZ));
    //    currentGX.setText(Float.toString(deltaGX));
    //    currentGY.setText(Float.toString(deltaGY));
    //    currentGZ.setText(Float.toString(deltaGZ));

        if (trackX > 0) {
    //        nextSlide.setText("True: X");
           // Log.i("Flow", "Next Slide: X");
            //trackX = 0;
        } else if (trackY > 0) {
    //        nextSlide.setText("True: Y");
           // Log.i("Flow", "Next Slide: Y");
            //trackY = 0;
        } else if (trackZ > 0) {
    //        nextSlide.setText("True: Z");
           // Log.i("Flow", "Next Slide: Z");
            //trackZ = 0;
        }
    }
}

