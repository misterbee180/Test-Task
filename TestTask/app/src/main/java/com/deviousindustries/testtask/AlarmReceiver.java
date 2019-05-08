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
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            //        - Need to establish task list for today
            Viewer_Tasklist.generateTaskInstances();

            Boolean blnToday = false;
            try(Cursor tblInstance = DatabaseAccess.getRecordsFromTable("tblTaskInstance", "fdtmCompleted = -1 and fdtmSystemCompleted = -1 and fdtmDeleted = -1",null)){
                while(tblInstance.moveToNext()){
                    TaskInstance ti = new TaskInstance(tblInstance.getLong(tblInstance.getColumnIndex("flngTaskInstance")));
                    Calendar from = Viewer_Tasklist.getCalendar(ti.mdtmFrom);
                    if(from.after(Viewer_Tasklist.getBeginningCurentDay()) && from.before(Viewer_Tasklist.getEndCurrentDay())){
                        //        - Need to generate push for today
                        if(blnToday == false){
                            //Check that the last time this was ran was NOT TODAY
                            if(Viewer_Tasklist.getCalendar(Viewer_Tasklist.mPrefs.getLong("general_last_sync",-1)).before(Viewer_Tasklist.getBeginningCurentDay())){
                                //Generate notification for today
                                generatePush(context, "Tasks Available Today", "");
                            }
                            blnToday = true;
                        }
                        //        - Need to re generate alerts for priority
                        //TODO: figure out alert where only to is set
                        if(ti.mblnFromTime){
                            //Generate alert for priority
                            intent = new Intent(context, AlarmReceiver.class);
                            intent.putExtra("EXTRA_PUSH_TITLE", "Active Priority");
                            intent.putExtra("EXTRA_PUSH_DESC",ti.mstrTitle);
                            //Fire a broadcast which is picked up by the alarmReceiver class which catches the broadcast and triggers the notification.
                            generateAlert(context, intent, ti.mdtmFrom);
//                            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
//                            AlarmManager alarmMgr = (AlarmManager)context.getSystemService(ALARM_SERVICE);
//                            alarmMgr.set(
//                                    AlarmManager.RTC_WAKEUP,
//                                    ti.mdtmFrom,
//                                    pendingIntent);
                        }
                    }
                }
            }
//                - Need to set up tomorrow 3am alert (not today)
            //Generate alert for 3am update
            intent = new Intent(context, AlarmReceiver.class);
            intent.setAction(Intent.ACTION_BOOT_COMPLETED);

            //Set to fire next day at 3:00am
            Calendar temp = Viewer_Tasklist.getCurrentCalendar();
            temp.add(Calendar.DAY_OF_YEAR,1);
            temp.set(Calendar.HOUR_OF_DAY, 3);
            temp.set(Calendar.MINUTE,0);
            temp.set(Calendar.SECOND,0);
            temp.set(Calendar.MILLISECOND,0);

            generateAlert(context, intent, temp.getTimeInMillis());
//            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
//            ((AlarmManager)context.getSystemService(ALARM_SERVICE)).set(
//                    AlarmManager.RTC_WAKEUP,
//                    temp.getTimeInMillis(),
//                    pendingIntent);

//                - Need to set database sync date
            SharedPreferences.Editor editor = Viewer_Tasklist.mPrefs.edit();
            editor.putLong("general_last_sync",Viewer_Tasklist.getCurrentCalendar().getTimeInMillis());
            editor.commit();

        } else {
            Bundle extras = intent.getExtras();
            String title = extras.getString("EXTRA_PUSH_TITLE","");
            String desc = extras.getString("EXTRA_PUSH_DESC", "");
            generatePush(context, title, desc);
        }
    }

    private void generatePush(Context pContext, String pTitle, String pDesc){
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

    public static void generateAlert(Context context, Intent intent, long pdtmWhen){
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(ALARM_SERVICE);
        alarmMgr.set(
                AlarmManager.RTC_WAKEUP,
                pdtmWhen,
                pendingIntent);
    }
}
