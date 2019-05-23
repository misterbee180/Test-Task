package com.deviousindustries.testtask;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.deviousindustries.testtask.Classes.TaskInstance;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;

public class AlarmReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction() == null){
            generatePush(context, "Alarm not working", "Alarm not working");
        } else if (intent.getAction().equals("com.deviousindustries.testtask.SYNC") ||
                intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            //        - Need to establish task list for today

            BusinessLogic b = new BusinessLogic(context);
            b.generateTaskInstances();

            Boolean blnToday = false;
            try(Cursor tblInstance = DatabaseAccess.getRecordsFromTable("tblTaskInstance", "fdtmCompleted = -1 and fdtmSystemCompleted = -1 and fdtmDeleted = -1",null)){
                int i = 0;
                while(tblInstance.moveToNext()){
                    i++;
                    TaskInstance ti = new TaskInstance(tblInstance.getLong(tblInstance.getColumnIndex("flngInstanceID")));
                    Calendar from = b.getCalendar(ti.fdtmFrom);
                    if(from.after(b.getBeginningCurentDay()) && from.before(b.getEndCurrentDay())){
                        //        - Need to generate push for today
                        if(blnToday == false){
                            //Check that the last time this was ran was NOT TODAY
                            Boolean blnRedo = false;
                            if(b.getCalendar(b.mPrefs.getLong("general_last_sync",-1)).before(b.getBeginningCurentDay()) ||
                            blnRedo){
                                //Generate notification for today
                                generatePush(context, "Tasks Available", "Click to see what tasks have to do today");
                            }
                            blnToday = true;
                        }
                        //        - Need to re generate alerts for priority
                        //TODO: figure out alert where only to is set
                        if(ti.fblnFromTime){
                            //Generate alert for priority
                            Intent intent2 = new Intent(context, AlarmReceiver.class);
                            intent2.setAction("com.deviousindustries.testtask.Notification");
                            intent2.putExtra("EXTRA_PUSH_TITLE", "Active Priority");
                            intent2.putExtra("EXTRA_PUSH_DESC", ti.fstrTitle);
                            //Fire a broadcast which is picked up by the alarmReceiver class which catches the broadcast and triggers the notification.
                            generateAlert(context, intent2, ti.fdtmFrom, i, AlarmManager.RTC_WAKEUP);
                        }
                    }
                }
            }
//                - Need to set up tomorrow 3am alert (not today)
            //Generate alert for 3am update
            intent = new Intent(context, AlarmReceiver.class);
            intent.setAction("com.deviousindustries.testtask.SYNC");
            //Set to fire next day at 3:00am
            Calendar temp = b.getCurrentCalendar();
            temp.add(Calendar.DAY_OF_YEAR,1);
            temp.set(Calendar.HOUR_OF_DAY, 3);
            temp.set(Calendar.MINUTE,0);
            temp.set(Calendar.SECOND,0);
            temp.set(Calendar.MILLISECOND,0);
            generateAlert(context, intent, temp.getTimeInMillis(), 0, AlarmManager.RTC_WAKEUP);

//                - Need to set database sync date
            SharedPreferences.Editor editor = b.mPrefs.edit();
            editor.putLong("general_last_sync",b.getCurrentCalendar().getTimeInMillis());
            editor.commit();

        } else {
            Bundle extras = intent.getExtras();
            String title = extras.getString("EXTRA_PUSH_TITLE","");
            String desc = extras.getString("EXTRA_PUSH_DESC", "");
            generatePush(context, title, desc);
        }
    }

    public static void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library

        //Should probably be moved to the Alarm Receiver class so that it always has the channel generated. For purposes
        //of demo this is fine.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("STANDARD", "GENERAL", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("General alerts");
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void generatePush(Context pContext, String pTitle, String pDesc){
        createNotificationChannel(pContext);

        PendingIntent pendingIntent = PendingIntent.getActivity(pContext, 0, new Intent(pContext, Viewer_Tasklist.class), 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(pContext, "STANDARD")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle(pTitle)//
                .setContentText(pDesc)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) //sets the event to fire when notification is clicked
                .setAutoCancel(true); //Removes notification when user taps it

        NotificationManagerCompat.from(pContext).notify(0, builder.build());
    }

    public static void generateAlert(Context context, Intent intent, long pdtmWhen, int pintReqCode, int pintAlarmType){
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, pintReqCode, intent, 0);
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(ALARM_SERVICE);
        alarmMgr.set(
                pintAlarmType,
                pdtmWhen,
                pendingIntent);
    }

    public static void cancelAlert(Context context, Intent intent){
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(ALARM_SERVICE);
        alarmMgr.cancel(pendingIntent);
    }
}
