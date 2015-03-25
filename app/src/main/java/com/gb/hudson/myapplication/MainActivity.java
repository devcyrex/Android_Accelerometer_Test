package com.gb.hudson.myapplication;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity implements SensorEventListener {

    static final float ALPHA = 0.05f;

    private SensorManager sManager;
    private Sensor sensor;

    private LinearLayout linearLayout;

    private UpdateHistory updateHistory;

    private TextView xTextView, yTextView, zTextView, gTextView;
    private String xTextViewString, yTextViewString, zTextViewString, gTextViewString;

    private float[] accVals = {0f,0f,0f};

    private float lastX, lastY, lastZ;
    private double lastGForce;
    private long lastUpdate = 0;

    @Override
    protected void onPause() {
        super.onPause();

        sManager.unregisterListener(this);

        xTextView.setText(getResources().getString(R.string.x_place));
        yTextView.setText(getResources().getString(R.string.y_place));
        zTextView.setText(getResources().getString(R.string.z_place));
        gTextView.setText(getResources().getString(R.string.g));
    }

    @Override
    protected void onResume() {
        super.onResume();

        sManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        xTextView = (TextView) findViewById(R.id.xTextView);
        yTextView = (TextView) findViewById(R.id.yTextView);
        zTextView = (TextView) findViewById(R.id.zTextView);
        gTextView = (TextView) findViewById(R.id.gTextView);

        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);

        xTextViewString = getResources().getString(R.string.updated_x_place);
        yTextViewString = getResources().getString(R.string.updated_y_place);
        zTextViewString = getResources().getString(R.string.updated_z_place);
        gTextViewString = getResources().getString(R.string.updated_g);

        updateHistory = new UpdateHistory(getApplicationContext());

        updateHistory.setLayoutParams(new FrameLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.FILL_PARENT));

        updateHistory.setBackgroundColor(Color.BLACK);

        linearLayout.addView(updateHistory);

        sManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        Sensor tempSensor = event.sensor;

        if(tempSensor.getType() == Sensor.TYPE_ACCELEROMETER){

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            long curTime = System.currentTimeMillis();

            if((curTime - lastUpdate) > 100){

                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                accVals = lowPass(event.values.clone(), accVals);

                double gForce = Math.sqrt(accVals[0] * accVals[0] + accVals[1] * accVals[1] + accVals[2] * accVals[2]);

                lastX = x;
                lastY = y;
                lastZ = z;
                lastGForce = gForce;

                xTextView.setText(xTextViewString + accVals[0]);
                yTextView.setText(yTextViewString + accVals[1]);
                zTextView.setText(zTextViewString + accVals[2]);
                gTextView.setText(gTextViewString + gForce);


//                updateHistory.setG((float)gForce);
//                updateHistory.invalidate();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class UpdateHistory extends View{

        private static final int NUMBER_OF_UPDATES = 10;

        private float g;

        private int height, width, x, y, rectWdith;

        private int[] lastGs = new int[NUMBER_OF_UPDATES];

        private UpdateHistory(Context context) {
            super(context);

            this.height = this.getHeight();
            this.width = this.getWidth();

            rectWdith = width / NUMBER_OF_UPDATES;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

//            if(lastGs.length == 10){
//
//                canvas.drawColor(Color.BLACK);
//
//                for(int i = 0; i > lastGs.length; i++){
//
//                    lastGs[i] = 0;
//                }
//            }

            for(int i = 0; i > lastGs.length; i++){

                int temp = lastGs[i];
                int parage = Math.round((temp * 100f) / this.height);

                Paint paint = new Paint();
                paint.setColor(Color.WHITE);

                canvas.drawRect(i * rectWdith, height - parage, (i * rectWdith) + rectWdith, height, paint);

                for(int x = 0; lastGs[x] != 0; x++){

                    lastGs[x] = (int)g;
                }
            }
        }

        public void setG(float g){
            this.g = g;
        }
    }

    protected float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;

        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }
}
