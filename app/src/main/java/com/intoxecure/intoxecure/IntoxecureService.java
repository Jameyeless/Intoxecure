package com.intoxecure.intoxecure;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.SharedPreferencesCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.ListIterator;

public class IntoxecureService extends Service implements SensorEventListener,SharedPreferences.OnSharedPreferenceChangeListener {
    static final String SENT_BROADCAST = "SMS_SENT";
    static final String DELIVERED_BROADCAST = "SMS_DELIVERED";
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
    private static PendingIntent pendSend, pendDeliver;
    private static SmsManager sms;
    private static ContactList contactList;
    private static boolean smsEnabled;
    private static float sensorSensitivity;
    private static SharedPreferences preferences;


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

        // shared preference
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
        smsEnabled = preferences.getBoolean("pref_key_enable_sms", false);
        Log.d("smsEnabled", Boolean.toString(smsEnabled));

        // Prepare sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // sms manager
        registerReceiver(sentReceiver, new IntentFilter(SENT_BROADCAST));
        registerReceiver(deliveredReceiver, new IntentFilter(DELIVERED_BROADCAST));

        pendSend = PendingIntent.getBroadcast(this, 0, new Intent(SENT_BROADCAST), 0);
        pendDeliver = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED_BROADCAST), 0);

        sms = SmsManager.getDefault();

        // contact
        contactList = new ContactList(this, false);
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
        unregisterReceiver(sentReceiver);
        unregisterReceiver(deliveredReceiver);
        preferences.unregisterOnSharedPreferenceChangeListener(this);
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

                if (fault > 10 && smsEnabled) {
                    for (String aContactNo : contactList.contactNo)
                        sms.sendTextMessage(aContactNo,
                                null, "Your friend [insert name] is probably " +
                                        "drunk. Maybe you should check on him on this address",
                                pendSend,
                                pendDeliver);

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

    BroadcastReceiver sentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(context, "SMS sent", Toast.LENGTH_LONG).show();
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(context, "General Failure", Toast.LENGTH_LONG).show();
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(context, "No Service", Toast.LENGTH_LONG).show();
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(context, "Null PDU", Toast.LENGTH_LONG).show();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(context, "Radio off", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };
    BroadcastReceiver deliveredReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(context, "SMS delivered", Toast.LENGTH_LONG).show();
                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(context, "SMS was not delivered", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("asd",  "asd");
        if (key.equals("pref_key_enable_sms")) {
            smsEnabled = sharedPreferences.getBoolean("pref_key_enable_sms", false);
            Log.d("smsEnabled", Boolean.toString(smsEnabled));
        } else if (key.equals("pref_key_sensor_sensitivity")) {
            sensorSensitivity = sharedPreferences.getFloat("pref_key_sensor_sensitivity", 0);
            sigmaDeltaTimeAlpha = sensorSensitivity*1;
            Log.d("sensitivity", Float.toString(sensorSensitivity));
        }
    }
}
