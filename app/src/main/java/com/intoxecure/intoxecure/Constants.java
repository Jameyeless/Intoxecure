package com.intoxecure.intoxecure;

class Constants {
    private Constants() {}

    public interface ACTION {
        String STARTFOREGROUND_ACTION = "com.intoxecure.intoxecure.action.startforeground";
        String STOPFOREGROUND_ACTION = "com.intoxecure.intoxecure.action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 101;
    }
}