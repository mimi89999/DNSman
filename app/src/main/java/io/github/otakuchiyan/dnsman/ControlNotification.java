package io.github.otakuchiyan.dnsman;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;


public class ControlNotification {
    private static final String NOTIFICATION_TAG = "Control";

    public static void notify(final Context context, String dns1, String dns2) {
        final Resources res = context.getResources();

        final String title = res.getString(
                R.string.control_notification_title);
        final String text = res.getString(
                R.string.control_notification_placeholder_text, dns1, dns2);


        final Notification.Builder builder = new Notification.Builder(context)
                .setSmallIcon(android.R.drawable.ic_menu_preferences)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(
                        PendingIntent.getActivity(
                                context,
                                0,
                                new Intent(context, MainActivity.class),
                                PendingIntent.FLAG_ONE_SHOT))
                .setStyle(new Notification.BigTextStyle()
                        .bigText(text)
                        .setBigContentTitle(title)
                        .setSummaryText("Dummy summary text"))
                        // Example additional actions for this notification. These will
                        // only show on devices running Android 4.1 or later, so you
                        // should ensure that the activity in this notification's
                        // content intent provides access to the same actions in
                        // another way.
                .setAutoCancel(false);

        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(NOTIFICATION_TAG, 0, builder.build());
    }

}
