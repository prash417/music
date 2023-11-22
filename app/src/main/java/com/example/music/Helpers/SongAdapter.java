package com.example.music.Helpers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.Home.HomeActivity;
import com.example.music.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    //members
    Context context;
    List<Song> songs;
    ExoPlayer player;
    RelativeLayout playerView;

    int current_pos = 0;

    private  onItemClickListener listener;
    //interface
    public interface onItemClickListener{
        void onItemClick(Song position);
    }

    //method
    public void setOnItemClickListener(onItemClickListener clickListener){
        listener = clickListener;
    }

    private onItemClickRingListener ringListener;
    //interface for ringtone click
    public interface onItemClickRingListener{
        void onItemRingClick(Song position);
    }

    public void setOnItemRingClickListener(onItemClickRingListener clickRingListener){
        ringListener = clickRingListener;
    }


    //constructor
    public SongAdapter(Context context, List<Song> songs, ExoPlayer player, RelativeLayout playerView) {
        this.context = context;
        this.songs = songs;
        this.player = player;
        this.playerView = playerView;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflate song row item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_rows,parent,false);
        return new SongViewHolder(view,listener,ringListener);
    }
    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), android.R.anim.slide_in_left);
        holder.itemView.startAnimation(animation);

        Song song = songs.get(position);
        SongViewHolder viewHolder = (SongViewHolder) holder;


        //set values
        viewHolder.titleHolder.setText(song.getTitle());
        viewHolder.durationHolder.setText(getDuration(song.getDuration()));
        viewHolder.sizeHolder.setText(getSize(song.getSize()));



        //art work uri
        Uri artworkUri = song.getArtworkUri();

        if (artworkUri != null){
            viewHolder.artworkHolder.setImageURI(artworkUri);

            //make sure uri has an artwork
            if (viewHolder.artworkHolder.getDrawable() == null){
                viewHolder.artworkHolder.setImageResource(R.drawable.music);
            }
        }

        //play song on item click
        viewHolder.itemView.setOnClickListener(View ->{


            //start the player service
            context.startService(new Intent(context.getApplicationContext(), PlayerService.class));


            if (player.isPlaying()){
                player.setMediaItems(getMediaItems(),position,0);
            }


            if (!player.isPlaying()){
                player.setMediaItems(getMediaItems(),position,0);
            }
            else {
                player.pause();
                player.seekTo(position,0);
            }

            //prepare and play
            player.prepare();
            player.play();



            //check if record audio permission is given or not
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
                //request audio permission
                ((HomeActivity)context).recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            }
        });



        viewHolder.More_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                current_pos = position;
                PopupMenu popupMenu = new PopupMenu(context,view);
                popupMenu.getMenuInflater().inflate(R.menu.more_option,popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    if (menuItem.getItemId() == R.id.play_option){
                        player.setMediaItems(getMediaItems(),current_pos,0);
                        player.prepare();
                        player.play();

                    }if (menuItem.getItemId() == R.id.share_option){
                        Uri sharePath = song.getUri();
                        Intent share = new Intent(Intent.ACTION_SEND);
                        share.setType("audio/*");
                        share.putExtra(Intent.EXTRA_STREAM, sharePath);
                        context.startActivity(Intent.createChooser(share, "Share Sound File"));
                    }if (menuItem.getItemId() == R.id.delete_option){
                        listener.onItemClick(songs.get(position));
                    }if (menuItem.getItemId() == R.id.set_as_ringtone_option){
                        ringListener.onItemRingClick(songs.get(position));
                    }if (menuItem.getItemId() == R.id.Details_option){
                        String path = songs.get(position).getPath();
                        String size = getSize(songs.get(position).getSize());
                        String duration =getDuration(songs.get(position).getDuration());
                        String name = songs.get(position).getTitle();

                        showAlertBox(path,size,duration,name);
                    }

                    return true;
                });
            }
        });


    }

    //show song details
    private void showAlertBox(String path, String size, String duration, String name) {
        View view = LayoutInflater.from(context).inflate(R.layout.song_information,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);

        TextView Music_Details_Name = view.findViewById(R.id.Music_Details_Name);
        Music_Details_Name.setText(name);
        Music_Details_Name.setSelected(true);

         TextView file_path_display = view.findViewById(R.id.file_path_display);
        file_path_display.setText(path);

        TextView song_size_display = view.findViewById(R.id.song_size_display);
        song_size_display.setText(size);


        TextView song_duration_display = view.findViewById(R.id.song_duration_display);
        song_duration_display.setText(duration);


        TextView ok_button = view.findViewById(R.id.ok_button);

        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        alertDialog.show();
    }

    //get player song list
    public List<MediaItem> getMediaItems() {
        //difine list of media items
        List<MediaItem> mediaItems = new ArrayList<>();

        for (Song song: songs) {
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(song.getUri())
                    .setMediaMetadata(getMetaData(song))
                    .build();

            //add media item to media item list
            mediaItems.add(mediaItem);
        }
        return mediaItems;
    }

    //create media metaData
    private MediaMetadata getMetaData(Song song) {
        return new MediaMetadata.Builder()
                .setTitle(song.getTitle())
                .setArtworkUri(song.getArtworkUri())
                .build();
    }

    //view holder class
    public static class SongViewHolder extends RecyclerView.ViewHolder{

        //members
        ImageView artworkHolder,More_icon;
        TextView titleHolder,durationHolder,sizeHolder;

        public SongViewHolder(@NonNull View itemView, onItemClickListener listener, onItemClickRingListener ringListener) {
            super(itemView);

            artworkHolder = itemView.findViewById(R.id.songs_view);
            More_icon = itemView.findViewById(R.id.More_icon);

            titleHolder = itemView.findViewById(R.id.song_name);
            durationHolder = itemView.findViewById(R.id.song_Duration);
            sizeHolder = itemView.findViewById(R.id.song_Size);



        }
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    //filter song
    @SuppressLint("NotifyDataSetChanged")
    public void filterSong(List<Song> filteredList){
            songs = filteredList;
            notifyDataSetChanged();
            notifyItemChanged(current_pos);
    }

    //get actual duration of song
    private String getDuration(int totalDuration){
        String totalDurationText;

        int hrs = totalDuration/(1000*60*60);
        int min = (totalDuration%(1000*60*60))/(1000*60);
        int secs = (((totalDuration%(1000*60*60))%(1000*60*60))%(1000*60))/1000;

        if (hrs < 1){
            totalDurationText = String.format("%02d:%02d",min,secs);
        }else {
            totalDurationText = String.format("%1d:%02d:%02d",hrs,min,secs);
        }

        return totalDurationText;
    }

//    get actual size of song
    private String getSize(long bytes){
        String hrSize;

        double k = bytes/1024.0;
        double m = ((bytes/1024.0)/1024.0);
        double g = (((bytes/1024.0)/1024.0)/1024.0);
        double t = ((((bytes/1024.0)/1024.0)/1024.0)/1024.0);

        //the format
        DecimalFormat dec = new DecimalFormat("0.00");

        if (t>1){
            hrSize = dec.format(t).concat(" TB");
        } else if (g>1) {
            hrSize = dec.format(g).concat(" GB");
        } else if (m>1) {
            hrSize = dec.format(m).concat(" MB");
        } else if (k>1) {
            hrSize = dec.format(k).concat(" KB");
        }else {
            hrSize = dec.format(g).concat(" Bytes");
        }
        return hrSize;
    }
}
