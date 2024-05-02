package com.example.mynotes.database;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.mynotes.dao.NoteDao;
import com.example.mynotes.entities.Note;

import kotlin.jvm.Synchronized;

@Database(entities = Note.class, version = 1, exportSchema = false)
public abstract class NotesDatabase extends RoomDatabase {


    public static NotesDatabase notesDatabase;
    public static synchronized NotesDatabase getDatabase(Context context){

        if (notesDatabase == null){
            notesDatabase= Room.databaseBuilder(context, NotesDatabase.class, "notes_db").build();
        }
        return notesDatabase;
    }


    public abstract NoteDao noteDao();
}
