package a238443.colors;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private SensorManager sensorManager;
    private Sensor gravitySensor;
    private Sensor accelerationSensor;
    private TextView colorView;
    private SharedPreferences sharedPref;

    private long lastUpdate = 0;
    private int[] rgba = {255, 200, 0, 255};
    private boolean subtractionCycle = true;        // means that when you tilt to the left it will subtract
    private int activePosition = 1;
    private float tiltAxis = DEFAULT_TILT_AXIS;
    private boolean tiltAxisSet = false;

    private static final float SPEED_DIVISION_FACTOR = 1.5f;
    private static final int ALPHA_POS = 3;
    private static final int ALPHA_CHANGE_SPEED = 1;
    private static final float DEFAULT_TILT_AXIS = 7;
    private static final float TILT_MARGIN = 1.5f;
    private static final float SHAKE_THRESHOLD = 1.75f;
    private static final int COOLDOWN_TIME = 500;

    private ArrayList<Integer> savedColors;

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        sharedPref = getSharedPreferences(getString(R.string.user_saves), Activity.MODE_PRIVATE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        colorView = findViewById(R.id.color_view);
        savedColors = new ArrayList<>();

        readUsersData();

        setupSensors();
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }

    @Override
    protected void onStart() {
        super.onStart();
        sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, accelerationSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
        saveUsersData();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putIntArray("rgba",rgba);
        savedInstanceState.putBoolean("cycle",subtractionCycle);
        savedInstanceState.putInt("active_position",activePosition);
        savedInstanceState.putIntegerArrayList("colors_list",savedColors);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        rgba = savedInstanceState.getIntArray("rgba");
        subtractionCycle = savedInstanceState.getBoolean("cycle");
        activePosition = savedInstanceState.getInt("active_position");
        savedColors = savedInstanceState.getIntegerArrayList("colors_list");
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i("accuracy","Accuracy changed, current accuracy: "+accuracy);
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_GRAVITY:
                colorChange(event.values);
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                shakeHandling(event.values[2]);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_toolbar_menu, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1) {
            if(resultCode == Activity.RESULT_OK) {
                savedColors = data.getIntegerArrayListExtra("result");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_saved) {
            Intent toGrid = new Intent(this, ColorsActivity.class);
            toGrid.putExtra("colors_list", savedColors);
            startActivityForResult(toGrid,1);
        }
        if(item.getItemId() == R.id.action_info) {
            startActivity(new Intent(this, InfoActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupSensors() {
        if(sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null)
            gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        else {
            Log.e("gravity", "Gravity sensor not available");
            closeApp();
        }

        if(sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null)
            accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        else {
            Log.e("acceleration", "Linear acceleration sensor not available");
            closeApp();
        }
    }

    private void prompt(CharSequence csToShow){
        Toast tst_toPrompt = Toast.makeText(getApplicationContext(),csToShow,Toast.LENGTH_SHORT);
        tst_toPrompt.show();
    }

    private void changeActive(boolean counterClockwise) {
        if(counterClockwise)
            activePosition = (activePosition+1) % 3;
        else {
            activePosition = activePosition - 1;
            if(activePosition < 0) activePosition = 2;
        }

        subtractionCycle = !subtractionCycle;
    }

    private void colorChange(float[] coordinates) {
        float x = coordinates[0];
        float y = coordinates[1];

        float speed = Math.abs(x/SPEED_DIVISION_FACTOR);

        if(!tiltAxisSet) {
            tiltAxis = y;
            tiltAxisSet = true;
        }

        if(subtractionCycle ^ (x>0))
            rgba[activePosition] += speed;
        else
            rgba[activePosition] -= speed;

        rgbaValuesCheck(activePosition, true, x > 0);

        if(Math.abs(y-tiltAxis) > TILT_MARGIN) {
            if (y > tiltAxis)
                rgba[ALPHA_POS] -= ALPHA_CHANGE_SPEED;
            else
                rgba[ALPHA_POS] += ALPHA_CHANGE_SPEED;
        }

        rgbaValuesCheck(ALPHA_POS, false, false);

        colorView.setBackgroundColor(Color.argb(rgba[3], rgba[0], rgba[1], rgba[2]));
    }

    private void rgbaValuesCheck(int position, boolean changeNecessary, boolean counterClockwise) {
        boolean corrected = false;

        if(rgba[position] <= 0) {
            rgba[position] = 0;
            corrected = true;
        }
        else {
            if(rgba[position] >= 255) {
                rgba[position] = 255;
                corrected = true;
            }
        }

        if(corrected && changeNecessary)
            changeActive(counterClockwise);
    }

    private void shakeHandling(float z) {
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - lastUpdate;

        if(timeDifference > COOLDOWN_TIME){
            z = Math.abs(z);
            lastUpdate = currentTime;

            if(z >= SHAKE_THRESHOLD) {
                prompt("Color saved");
                savedColors.add(Color.argb(rgba[3], rgba[0], rgba[1], rgba[2]));
            }
        }
    }

    private void saveUsersData() {
        SharedPreferences.Editor sp_editor = sharedPref.edit();
        sp_editor.putInt("size", savedColors.size());
        for(int i=0; i < savedColors.size(); i++) {
            sp_editor.putInt("c" + i, savedColors.get(i));
            sp_editor.apply();
        }
    }

    private void readUsersData() {
        int amountToRead = sharedPref.getInt("size",0);

        for(int i=0; i < amountToRead; i++) {
            savedColors.add(sharedPref.getInt("c"+i,0));
        }
    }

    private void closeApp() {
        prompt("Necessary sensors not found");
        finish();
        moveTaskToBack(true);
    }
}
