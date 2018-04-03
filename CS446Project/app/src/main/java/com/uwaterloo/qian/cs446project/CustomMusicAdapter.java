package com.uwaterloo.qian.cs446project;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import static com.uwaterloo.qian.cs446project.CS446Utils.formatTime;

/**
 * Created by Qian on 2018-02-15.
 */

// Ke Qiao Chen: I based this class on
// https://github.com/quocnguyenvan/media-player-demo/blob/master/app/src/main/java/com/quocnguyen/mediaplayerdemo/CustomMusicAdapter.java
// except for the following changes:
// - In the tutorial, play, pause, and stop buttons are available for each song. Since my
// implementation only allows the user to play, pause, and stop an entire playlist, I do not
// include these buttons in this class. Instead, I use a similar idea to show for each song a
// progress bar, the elapsed time, and the remaining time.
// - Because HostMusicPlayerActivity.java and ParticipantMusicPlayerActivity.java must update a
// song's progress bar, elapsed time, and remaining time as the song plays, as well as bold the
// metadata of the playing song, I store these values in instances of ViewHolder and have an
// ArrayList of these instances.
public class CustomMusicAdapter extends BaseAdapter {

    private Context context;
    private int layout;
    private Playlist playlist;
    private ArrayList<ViewHolder> songsInGUI = new ArrayList<>();
    private HashMap<Integer, View> displayedSongs = new HashMap<>();

    public CustomMusicAdapter(Context context, int layout, Playlist playlist) {
        this.context = context;
        this.layout = layout;
        this.playlist = playlist;
    }

    @Override
    public int getCount() {
        if(playlist == null) Log.d("getCount>","playlist is null");
        if(playlist.songs == null) Log.d("getCount>", "songs is null.");
        return playlist.songs.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public class ViewHolder {

        private TextView title, artist, album, elapsedTime, remainingTime;
        private SeekBar songProgressBar;

        public TextView getTitle() {
            return title;
        }

        public TextView getArtist() {
            return artist;
        }

        public TextView getAlbum() {
            return album;
        }

        public TextView getElapsedTime() {
            return elapsedTime;
        }

        public TextView getRemainingTime() {
            return remainingTime;
        }

        public SeekBar getSongProgressBar() {
            return songProgressBar;
        }

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        Song song = playlist.songs.get(position);
        if (!displayedSongs.containsKey(position)) {
            viewHolder = new ViewHolder();
            LayoutInflater layoutInflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(layout, null);
            viewHolder.title = convertView.findViewById(R.id.textViewTitle);
            viewHolder.artist = convertView.findViewById(R.id.textViewArtist);
            viewHolder.album = convertView.findViewById(R.id.textViewAlbum);
            viewHolder.elapsedTime = convertView.findViewById(R.id.textViewElapsedTime);
            viewHolder.remainingTime = convertView.findViewById(R.id.textViewRemainingTime);
            viewHolder.songProgressBar = convertView.findViewById(R.id.seekBarSongProgressBar);
            viewHolder.title.setText(viewHolder.title.getText() + song.getTitle());
            viewHolder.artist.setText(viewHolder.artist.getText() + song.getArtist());
            viewHolder.album.setText(viewHolder.album.getText() + song.getAlbum());
            viewHolder.remainingTime.setText("-" + formatTime(song.getDuration()));
            viewHolder.songProgressBar.setMax(song.getDuration());
            songsInGUI.add(viewHolder);
            convertView.setTag(viewHolder);
            displayedSongs.put(position, convertView);
        } else {
            convertView = displayedSongs.get(position);
        }
        return convertView;
    }

    public ArrayList<ViewHolder> getSongsInGUI() {
        return songsInGUI;
    }

}
