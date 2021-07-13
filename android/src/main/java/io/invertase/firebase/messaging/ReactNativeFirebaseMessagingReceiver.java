package io.invertase.firebase.messaging;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

// For Aiqua sdk
import androidx.core.app.JobIntentService;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.os.Bundle;
import com.quantumgraph.sdk.NotificationJobIntentService;
import com.quantumgraph.sdk.QG;
import java.util.Map;
//___________

import com.facebook.react.HeadlessJsTaskService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;

import io.invertase.firebase.app.ReactNativeFirebaseApp;
import io.invertase.firebase.common.ReactNativeFirebaseEventEmitter;
import io.invertase.firebase.common.SharedUtils;

public class ReactNativeFirebaseMessagingReceiver extends BroadcastReceiver {
  private static final String TAG = "RNFirebaseMsgReceiver";
  static HashMap<String, RemoteMessage> notifications = new HashMap<>();


  @Override
  public void onReceive(Context context, Intent intent) {
    Log.d(TAG, "broadcast received for message");
    if (ReactNativeFirebaseApp.getApplicationContext() == null) {
      ReactNativeFirebaseApp.setApplicationContext(context.getApplicationContext());
    }
    RemoteMessage remoteMessage = new RemoteMessage(intent.getExtras());
    ReactNativeFirebaseEventEmitter emitter = ReactNativeFirebaseEventEmitter.getSharedInstance();

    String from = remoteMessage.getFrom();
    Map data = remoteMessage.getData();

    Log.d(TAG, from);
    Log.d(TAG, data.toString());

    // only send notifications from AIQUA to AIQUA Sdk
    if (data.containsKey("message") && QG.isQGMessage(data.get("message").toString())) {
      Bundle qgData = new Bundle();
      qgData.putString("message", data.get("message").toString());
      if (from == null || context == null) {
          return;
      }
      intent.setAction("QG");
      intent.putExtras(qgData);
      JobIntentService.enqueueWork(context, NotificationJobIntentService.class, 1000, intent);
      return;
    } else {
      // handle fcm message from other services
      // Add a RemoteMessage if the message contains a notification payload
      if (remoteMessage.getNotification() != null) {
        notifications.put(remoteMessage.getMessageId(), remoteMessage);
        ReactNativeFirebaseMessagingStoreHelper.getInstance().getMessagingStore().storeFirebaseMessage(remoteMessage);
      }
      //  |-> ---------------------
      //      App in Foreground
      //   ------------------------
      if (SharedUtils.isAppInForeground(context)) {
        emitter.sendEvent(ReactNativeFirebaseMessagingSerializer.remoteMessageToEvent(remoteMessage, false));
        return;
      }
      //  |-> ---------------------
      //    App in Background/Quit
      //   ------------------------
      try {
        Intent backgroundIntent = new Intent(context, ReactNativeFirebaseMessagingHeadlessService.class);
        backgroundIntent.putExtra("message", remoteMessage);
        ComponentName name = context.startService(backgroundIntent);
        if (name != null) {
          HeadlessJsTaskService.acquireWakeLockNow(context);
        }
      } catch (IllegalStateException ex) {
        // By default, data only messages are "default" priority and cannot trigger Headless tasks
        Log.e(
          TAG,
          "Background messages only work if the message priority is set to 'high'",
          ex
        );
      }
    }
  }
}
