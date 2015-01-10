package ie.johndoyle.flow;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener {

    private float lastX, lastY, lastZ, lastGX, lastGY, lastGZ;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyro;

    private float trackX = 0;

    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;

    private float deltaGX = 0;
    private float deltaGY = 0;
    private float deltaGZ = 0;

    private float vibrateThreshold = 0;

    private TextView currentX, currentY, currentZ, currentGX, currentGY, currentGZ, nextSlide;

    public Vibrator v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.round_activity_main);
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
    }

    public void initializeViews() {
        currentX = (TextView) findViewById(R.id.currentX);
        currentY = (TextView) findViewById(R.id.currentY);
        currentZ = (TextView) findViewById(R.id.currentZ);
        currentGX = (TextView) findViewById(R.id.currentGX);
        currentGY = (TextView) findViewById(R.id.currentGY);
        currentGZ = (TextView) findViewById(R.id.currentGZ);
        nextSlide = (TextView) findViewById(R.id.nextSlide);
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

        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            // get the change of the x,y,z values of the accelerometer
            deltaX = lastX - event.values[0];//Math.abs(lastX - event.values[0]);
            deltaY = Math.abs(lastY - event.values[1]);
            deltaZ = lastZ - event.values[2];

            lastX = event.values[0];
            lastY = event.values[1];
            lastZ = event.values[2];

            if (deltaZ >= 1) {
                trackX += deltaZ;
            }
            else if (deltaZ >= 0) {}
            else { trackX = 0; }

            // if the change is below 2, it is just plain noise
            if (deltaX < 2)
                deltaX = 0;
            if (deltaY < 2)
                deltaY = 0;
            if ((deltaZ > vibrateThreshold) || (deltaY > vibrateThreshold) || (deltaZ > vibrateThreshold)) {
                v.vibrate(50);
            }
        } else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            // get the change of the x,y,z values of the accelerometer
            deltaGX = Math.abs(lastGX - event.values[0]);
            deltaGY = Math.abs(lastGY - event.values[1]);
            deltaGZ = Math.abs(lastGZ - event.values[2]);

            lastGX = event.values[0];
            lastGY = event.values[1];
            lastGZ = event.values[2];

            // if the change is below 2, it is just plain noise
            if (deltaGX < 2)
                deltaGX = 0;
            if (deltaGY < 2)
                deltaGY = 0;
            if ((deltaGZ > vibrateThreshold) || (deltaGY > vibrateThreshold) || (deltaGZ > vibrateThreshold)) {
                v.vibrate(50);
            }
        }

    }

    public void displayCleanValues() {
        currentX.setText("0.0");
        currentY.setText("0.0");
        currentZ.setText("0.0");
        currentGX.setText("0.0");
        currentGY.setText("0.0");
        currentGZ.setText("0.0");
    }

    // display the current x,y,z accelerometer values
    public void displayCurrentValues() {
        currentX.setText(Float.toString(deltaX));
        currentY.setText(Float.toString(deltaY));
        currentZ.setText(Float.toString(deltaZ));
        currentGX.setText(Float.toString(deltaGX));
        currentGY.setText(Float.toString(deltaGY));
        currentGZ.setText(Float.toString(deltaGZ));

        if (trackX > 20) {
            nextSlide.setText("True");
            Log.i("Flow", "Next Slide");
        } else if (trackX == 0) {
            nextSlide.setText("False");
        }
    }
}

