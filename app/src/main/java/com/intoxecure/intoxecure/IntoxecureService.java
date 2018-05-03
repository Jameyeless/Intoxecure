package com.intoxecure.intoxecure;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayDeque;

import com.intoxecure.intoxecure.WeightedAverage;
import java.lang.Iterable;

public class IntoxecureService extends Service implements SensorEventListener {
    private static final String LOG_TAG = "ForegroundService";
    public static boolean IS_SERVICE_RUNNING = false;
    private static Notification notification;
    private static SensorManager sensorManager;
    private static Sensor stepCounter;
    private static Toast toast;
    private static long stepOldTime;
    private static long stepCurTime;

    private static int threshold;
    private static WeightedAverage Ave;
    private static double aveTime;
    private static double tuning;
    private static ArrayDeque<Double> movingAverage = new ArrayDeque<>(3);

    @Override
    public void onCreate() {
        super.onCreate();

        // Prepare notification
        CharSequence name = getString(R.string.channel_name);
        String description = getString(R.string.channel_description);
        String channelID = getString(R.string.channel_id);
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(getApplicationContext(), channelID)
                .setContentTitle("Intoxecure")
                .setContentText("Accelerometer is active")
                .setSmallIcon(R.drawable.intoxecure_logo_v1)
                .setAutoCancel(false)
                .setContentIntent(PendingIntent.getActivity(this, 0,
                        new Intent(this, MainActivity.class), 0));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            notification = nBuilder.build();
        else
            notification = nBuilder.getNotification();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelID, name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(description);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        // Prepare sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Prepare external classes and tuning parameter
        Ave = new WeightedAverage();
        tuning = 0.2;
        movingAverage.add(0.0);
        movingAverage.add(0.0);
        movingAverage.add(0.0);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Start Foreground Intent ");
            Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
            registerListener();
            stepCurTime = System.currentTimeMillis();
            toast = Toast.makeText(getApplicationContext(), null, Toast.LENGTH_SHORT);
        } else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            stopForeground(true);
            stopSelf();
        }
        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "In onDestroy");
        unregisterListener();
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Used only in case if services are bound (Bound Services).
        return null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.d("mySensor", "Accelerometer accuracy");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == stepCounter) {
            stepOldTime = stepCurTime;
            stepCurTime = System.currentTimeMillis();
            computeAverage();

        }
    }

    private void registerListener() {
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void unregisterListener() {
        sensorManager.unregisterListener(this);
    }

    private synchronized void computeAverage() {
        double k;
        aveTime = Ave.compute(stepCurTime, tuning);
        movingAverage.removeFirst();
        movingAverage.add(aveTime);
        k = movingAverage.peek();

        //Test threshold
        for (Double number : movingAverage) {
            //Toast.makeText(this, Double.toString(number), Toast.LENGTH_SHORT).show();
            if(number < (k + 0.5) && number > (k - 0.5)){
                threshold++;
            }
        }

        if (threshold>=3){
            //send message
            Toast.makeText(this, "NOTIFICATION", Toast.LENGTH_LONG).show();
        }
        threshold = 0;

        Toast.makeText(this, int.toString(threshold), Toast.LENGTH_SHORT).show();

    }


}
