package com.example.music.Helpers;

import static androidx.core.app.NotificationManagerCompat.IMPORTANCE_DEFAULT;
import static androidx.core.app.NotificationManagerCompat.IMPORTANCE_HIGH;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.music.Home.HomeActivity;
import com.example.music.R;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class PlayerService extends Service {

    //members

    private MediaSessionCompat mediaSession;
    private MediaSessionConnector mediaSessionConnector;
    private final IBinder serviceBinder = new ServiceBinder();

    public ExoPlayer player;
    PlayerNotificationManager notificationManager;

    //class binder for clients
    public class ServiceBinder extends Binder{
        public PlayerService getPlayerService(){
            return PlayerService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
     return serviceBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //assign variables
        player = new ExoPlayer.Builder(getApplicationContext()).build();

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build();

        player.setAudioAttributes(audioAttributes,true);

        //notification manager
        final String chanelId = getResources().getString(R.string.app_name) + " Music chanel ";
        final int notificationId = 11111111;
        notificationManager = new PlayerNotificationManager.Builder(this,notificationId,chanelId)
                .setNotificationListener(notificationListener)
                .setMediaDescriptionAdapter(descriptionAdapter)
                .setChannelImportance(IMPORTANCE_DEFAULT)
                .setSmallIconResourceId(R.drawable.music)
                .setChannelDescriptionResourceId(R.string.app_name)
                .setNextActionIconResourceId(R.drawable.next_icon)
                .setPreviousActionIconResourceId(R.drawable.previous_icon)
                .setPlayActionIconResourceId(R.drawable.play_icon)
                .setPauseActionIconResourceId(R.drawable.pause_icon)
                .setChannelNameResourceId(R.string.app_name)
                .build();

        //set player to notification manger
        notificationManager.setPlayer(player);
        notificationManager.setPriority(NotificationCompat.PRIORITY_MAX);
        notificationManager.setUseRewindAction(false);
        notificationManager.setUseFastForwardAction(false);


        mediaSession = new MediaSessionCompat(this, "sample");
        mediaSessionConnector = new MediaSessionConnector(mediaSession);
        mediaSessionConnector.setPlayer(player);

        mediaSessionConnector.setQueueNavigator(new TimelineQueueNavigator(mediaSession) {
            @Override
            public MediaDescriptionCompat getMediaDescription(Player player, int windowIndex) {
                return new MediaDescriptionCompat.Builder()
                        .setTitle("MediaDescription title")
                        .setDescription("MediaDescription description for " + windowIndex)
                        .setSubtitle("MediaDescription subtitle")
                        .build();
            }
        });

        mediaSessionConnector.setEnabledPlaybackActions(
                PlaybackStateCompat.ACTION_PLAY_PAUSE
                        | PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PAUSE
                        | PlaybackStateCompat.ACTION_SEEK_TO
                        | PlaybackStateCompat.ACTION_FAST_FORWARD
                        | PlaybackStateCompat.ACTION_REWIND
                        | PlaybackStateCompat.ACTION_STOP
                        | PlaybackStateCompat.ACTION_SET_REPEAT_MODE
                        | PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE
        );

    }

    @Override
    public void onDestroy() {
        //release the player
        if (player.isPlaying()) player.stop();
        notificationManager.setPlayer(null);
        player.release();
        player = null;
        stopForeground(true);
        stopSelf();
        super.onDestroy();
    }

    //notification listener
    PlayerNotificationManager.NotificationListener notificationListener = new PlayerNotificationManager.NotificationListener() {
        @Override
        public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
            PlayerNotificationManager.NotificationListener.super.onNotificationCancelled(notificationId, dismissedByUser);
            stopForeground(true);
            if (player.isPlaying()){
                player.pause();

            }
        }

        @Override
        public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
            if (ongoing){
                PlayerNotificationManager.NotificationListener.super.onNotificationPosted(notificationId, notification, ongoing);
                startForeground(notificationId,notification);
            }else {
                stopForeground(false);
            }
        }
    };

    //notification adapter
    PlayerNotificationManager.MediaDescriptionAdapter descriptionAdapter = new PlayerNotificationManager.MediaDescriptionAdapter() {
        @Override
        public CharSequence getCurrentContentTitle(Player player) {
            return Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title;
        }

        @Nullable
        @Override
        public PendingIntent createCurrentContentIntent(Player player) {

            //intent open app when notification is clicked
            Intent openAppIntent = new Intent(getApplicationContext(), HomeActivity.class);

            return PendingIntent.getActivity(getApplicationContext(),0,openAppIntent,
                    PendingIntent.FLAG_IMMUTABLE
                    | PendingIntent.FLAG_CANCEL_CURRENT);
        }

        @Nullable
        @Override
        public CharSequence getCurrentContentText(Player player) {
            return null;
        }

        @Nullable
        @Override
        public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
            //image display
            CircleImageView view = new CircleImageView(getApplicationContext());
            view.setImageURI(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.artworkUri);

            //get drawable
            BitmapDrawable bitmapDrawable = (BitmapDrawable) view.getDrawable();

            if (bitmapDrawable == null){
                bitmapDrawable = (BitmapDrawable) ContextCompat.getDrawable(getApplicationContext(),R.drawable.music);
            }

            assert bitmapDrawable !=null;
            return bitmapDrawable.getBitmap();
        }
    };

}