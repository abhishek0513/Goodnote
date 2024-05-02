package com.example.mynotes.adapters;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mynotes.R;
import com.example.mynotes.entities.Note;
import com.example.mynotes.listeners.NoteListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder>{
    private List<Note>notes;
    private NoteListener noteListener;
    private Timer timer;
    private List<Note> noteSource;
    public NotesAdapter(List<Note> notes, NoteListener noteListener)
    {
        this.notes = notes;
        this.noteListener= noteListener;
        noteSource = notes;

    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(LayoutInflater.from(parent.getContext()).inflate(

                R.layout.item_container_note, parent, false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, @SuppressLint("RecyclerView") int position) {
            holder.setNote(notes.get(position));
            holder.layoutNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    noteListener.onNoteClicked(notes.get(position),position );
                }
            });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }


    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder{

        TextView texttitle, textsubtitle, textDateTime;
        LinearLayout layoutNote;
        RoundedImageView imageNote;
         NoteViewHolder(@NonNull View itemView) {
               super(itemView);
            texttitle = itemView.findViewById(R.id.textitle);
            textsubtitle = itemView.findViewById(R.id.textSubtitle);
            textDateTime = itemView.findViewById(R.id.textDateTime);
            layoutNote = itemView.findViewById(R.id.layoutNote);
            imageNote = itemView.findViewById(R.id.imageNote);
        }

        void setNote(Note note){
             texttitle.setText(note.getTitle());
             if (note.getSubtitle().trim().isEmpty()){
                 textsubtitle.setVisibility(View.GONE);
             }
             else{
                 textsubtitle.setText(note.getSubtitle());
             }
             textDateTime.setText(note.getDateTime());

            GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
            if (note.getColor()!=null){
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            }
            else{
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }
            if (note.getImagePath() != null){
                imageNote.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                imageNote.setVisibility(View.VISIBLE);
            }
            else{
                imageNote.setVisibility(View.GONE);
            }
        }
    }
    public void searchNotes(final String searchKeyword){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (searchKeyword.trim().isEmpty()){
                    notes = noteSource;
                }
                else{
                    ArrayList<Note>temp = new ArrayList<>();
                    for (Note note: noteSource ){
                        if (note.getTitle().toLowerCase().contains(searchKeyword.toLowerCase()) || note.getSubtitle().toLowerCase().contains(searchKeyword.toLowerCase())
                                ||note.getNotetext().toLowerCase().contains(searchKeyword.toLowerCase())
                        ) {
                                temp.add(note);
                        }
                    }
                    notes = temp;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }, 500);
    }
    public void cancelTimer(){
        if (timer != null){
            timer.cancel();
        }
    }
}
