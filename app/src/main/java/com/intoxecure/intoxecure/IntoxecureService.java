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

import java.util.LinkedList;
import java.util.ListIterator;

public class IntoxecureService extends Service implements SensorEventListener {
    private static final String LOG_TAG = "ForegroundService";
    public static boolean IS_SERVICE_RUNNING = false;
    private static Notification notification;
    private static SensorManager sensorManager;
    private static Sensor accelerometer;
    public static double acceleration;
    private static long count = 0;
    private static long countTime;
    private static StepDetector accelStepDetector = new StepDetector();
    private static LinkedList<Long> countTimeDelta = new LinkedList<>();
    private static double sigmaDeltaTime, sigmaDeltaTimeAlpha = 0.5;
    private static double expMean, expMeanAlpha = 0.125;
    private static int fault = 0;

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
            //noinspection deprecation
            notification = nBuilder.getNotification();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelID, name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(description);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }

        // Prepare sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() != null) {
            if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
                Log.i(LOG_TAG, "Received Start Foreground Intent ");
                Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
                startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
                registerListener();
            } else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)) {
                Log.i(LOG_TAG, "Received Stop Foreground Intent");
                stopForeground(true);
                stopSelf();
            }
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
        if (event.sensor == accelerometer) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            acceleration = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
            //Log.d("Accelerometer", "Acceleration:" + acceleration + "m/s^2");

            long countTemp = accelStepDetector.Iterate(acceleration);
            long countTimeTemp = event.timestamp;
            if (count != countTemp) {
                count = countTemp;

                long timeDeltaTemp = countTimeTemp-countTime;
                if (countTimeDelta.size() == 1)
                    expMean = timeDeltaTemp;
                else if (countTimeDelta.size() > 1)
                    expMean = expMean*(1-expMeanAlpha) + timeDeltaTemp*expMeanAlpha;

                if (timeDeltaTemp > expMean + sigmaDeltaTime*sigmaDeltaTimeAlpha || timeDeltaTemp < expMean - sigmaDeltaTime*sigmaDeltaTimeAlpha)
                    fault += 1;
                if (timeDeltaTemp > 10e9)
                    fault = 0;

                if (fault > 5) {
                    // TODO: send sms
                    Log.d("Lasing ka pre", "UWI NA UY");
                    fault = 0;
                }

                // Add new time delta, and recompute for standard dev
                while (countTimeDelta.size() >= 25) {
                    countTimeDelta.poll();
                }
                countTimeDelta.offer(countTimeTemp-countTime);
                countTime = countTimeTemp;
                ListIterator<Long> iterator = countTimeDelta.listIterator();
                double mean = 0;
                while (iterator.hasNext())
                    mean += iterator.next();
                mean /= countTimeDelta.size();
                iterator = countTimeDelta.listIterator();
                sigmaDeltaTime = 0;
                while (iterator.hasNext())
                    sigmaDeltaTime += Math.pow(iterator.next(),2);
                sigmaDeltaTime = Math.sqrt(sigmaDeltaTime/countTimeDelta.size() - Math.pow(mean,2));

                Log.d("count", Long.toString(count));
                Log.d("fault", Integer.toString(fault));
                Log.d("average", Double.toString(expMean));
                Log.d("std dev", Double.toString(sigmaDeltaTime));
                Log.d("timeDeltaTemp", Long.toString(timeDeltaTemp));
                Log.d("array size", Integer.toString(countTimeDelta.size()));
            }
        }
    }

    private void registerListener() {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void unregisterListener() {
        sensorManager.unregisterListener(this);
    }
}
