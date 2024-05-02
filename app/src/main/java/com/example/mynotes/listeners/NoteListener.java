package com.example.mynotes.listeners;

import com.example.mynotes.entities.Note;

public interface NoteListener {

    void onNoteClicked(Note note, int position);

}
