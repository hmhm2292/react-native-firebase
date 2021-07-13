package io.invertase.firebase.messaging;

// For Aiqua sdk
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import androidx.core.app.JobIntentService;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.os.Bundle;
import android.util.Log;

import com.quantumgraph.sdk.NotificationJobIntentService;
import com.quantumgraph.sdk.QG;
//_______

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import io.invertase.firebase.common.ReactNativeFirebaseEventEmitter;

public class ReactNativeFirebaseMessagingService extends FirebaseMessagingService {
  @Override
  public void onSendError(String messageId, Exception sendError) {
    ReactNativeFirebaseEventEmitter emitter = ReactNativeFirebaseEventEmitter.getSharedInstance();
    emitter.sendEvent(ReactNativeFirebaseMessagingSerializer.messageSendErrorToEvent(messageId, sendError));
  }

  @Override
  public void onDeletedMessages() {
    ReactNativeFirebaseEventEmitter emitter = ReactNativeFirebaseEventEmitter.getSharedInstance();
    emitter.sendEvent(ReactNativeFirebaseMessagingSerializer.messagesDeletedToEvent());
  }

  @Override
  public void onMessageSent(String messageId) {
    ReactNativeFirebaseEventEmitter emitter = ReactNativeFirebaseEventEmitter.getSharedInstance();
    emitter.sendEvent(ReactNativeFirebaseMessagingSerializer.messageSentToEvent(messageId));
  }

  @Override
  public void onNewToken(String token) {
    Log.d("RNFirebaseMsgReceiver", token);
    QG.logFcmId(getApplicationContext());
    ReactNativeFirebaseEventEmitter emitter = ReactNativeFirebaseEventEmitter.getSharedInstance();
    emitter.sendEvent(ReactNativeFirebaseMessagingSerializer.newTokenToTokenEvent(token));
  }

  @Override
  public void onMessageReceived(RemoteMessage remoteMessage) {
    // noop - handled in receiver
  }
}
