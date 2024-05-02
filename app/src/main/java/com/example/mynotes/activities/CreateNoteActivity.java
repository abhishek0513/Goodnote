package com.example.mynotes.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mynotes.R;
import com.example.mynotes.database.NotesDatabase;
import com.example.mynotes.entities.Note;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {

    private EditText inputNotetitle, inputNotesubtitle, inputNoteText;
    private TextView textDateTime;
    private View viewsubtitleIndicator;
    private ImageView imageNote;

    public String selectedNoteColor;
    private String selectedImagePath;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE= 2;

    private Note alreadyAvailable ;

    private AlertDialog dialogdeleteNote;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);
        ImageView imageBack = findViewById(R.id.imageBack);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        inputNotetitle = findViewById(R.id.inputNotetitle);
        inputNotesubtitle = findViewById(R.id.inputNotesubtitle);
        inputNoteText = findViewById(R.id.inputNote);
        textDateTime = findViewById(R.id.textDateTime);
        viewsubtitleIndicator = findViewById(R.id.viewSubtitleIndicator);
        imageNote = findViewById(R.id.imageNote);

        textDateTime.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy  HH:mm a", Locale.getDefault())
                        .format(new Date())
        );
        ImageView imageSave = findViewById(R.id.imageSave);
        imageSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });

        ImageView imageShare = findViewById(R.id.imageShare);
        imageShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShareNotesAsPdf();
            }
        });

        selectedNoteColor = "#333333";
        selectedImagePath = "";

        if (getIntent().getBooleanExtra("isViewOrUpdate", false)){
            alreadyAvailable = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }

        intMiscellaneous();
        setSubtitleIndicatorColor();

    }

    private void setViewOrUpdateNote(){
            inputNotetitle.setText(alreadyAvailable.getTitle());
            inputNotesubtitle.setText(alreadyAvailable.getSubtitle());
        inputNoteText.setText(alreadyAvailable.getNotetext());
        textDateTime.setText(alreadyAvailable.getDateTime());
        if (alreadyAvailable.getImagePath()!=null && !alreadyAvailable.getImagePath().trim().isEmpty()){
            imageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailable.getImagePath()));
            imageNote.setVisibility(View.VISIBLE);
            selectedImagePath = alreadyAvailable.getImagePath();
        }

    }

    private void saveNote(){
            if (inputNotetitle.getText().toString().trim().isEmpty()){
                Toast.makeText(this, "Title can't be empty", Toast.LENGTH_SHORT).show();
                return;
            } else if (inputNotesubtitle.getText().toString().trim().isEmpty() && inputNoteText.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Note can't be empty", Toast.LENGTH_SHORT).show();
                return;
            }

        final Note note = new Note();
            note.setTitle(inputNotetitle.getText().toString());
            note.setSubtitle(inputNotesubtitle.getText().toString());
            note.setNotetext(inputNoteText.getText().toString());
            note.setDateTime(textDateTime.getText().toString());
            note.setColor(selectedNoteColor);
            note.setImagePath(selectedImagePath);

            if (alreadyAvailable!=null){
                note.setId(alreadyAvailable.getId());
            }
            @SuppressLint("StaticFieldLeak")
            class SaveNoteTask extends AsyncTask<Void, Void, Void>{
                @Override
                protected Void doInBackground(Void... voids) {
                    NotesDatabase.getDatabase(getApplicationContext()).noteDao().inserNote(note);
                    return null;
                }

                @Override
                protected void onPostExecute(Void unused) {
                    super.onPostExecute(unused);
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
            new SaveNoteTask().execute();

    }

    private void intMiscellaneous(){
        final LinearLayout layoutMiscellaneous = findViewById(R.id.layoutMiscellaneous);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);
        layoutMiscellaneous.findViewById(R.id.textMiscellaneous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                else{
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });
        final  ImageView imageColor1 = layoutMiscellaneous.findViewById(R.id.imageColor1);
        final  ImageView imageColor2= layoutMiscellaneous.findViewById(R.id.imageColor2);
        final  ImageView imageColor3= layoutMiscellaneous.findViewById(R.id.imageColor3);
        final  ImageView imageColor4 = layoutMiscellaneous.findViewById(R.id.imageColor4);
        final ImageView imageColor5 = layoutMiscellaneous.findViewById(R.id.imageColor5);
        layoutMiscellaneous.findViewById(R.id.viewColor1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#333333";
                imageColor1.setImageResource(R.drawable.icon_tick);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });
        layoutMiscellaneous.findViewById(R.id.viewColor2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#FDBE3B";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(R.drawable.icon_tick);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });
        layoutMiscellaneous.findViewById(R.id.viewColor3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#FF4842";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(R.drawable.icon_tick);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });
        layoutMiscellaneous.findViewById(R.id.viewColor4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#3A52FC";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(R.drawable.icon_tick);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });
        layoutMiscellaneous.findViewById(R.id.viewColor5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#000000";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(R.drawable.icon_tick);
                setSubtitleIndicatorColor();
            }
        });

        if (alreadyAvailable != null && alreadyAvailable.getColor()!=null && !alreadyAvailable.getColor().trim().isEmpty()){
            switch (alreadyAvailable.getColor()){
                case "#FDBE3B" :
                    layoutMiscellaneous.findViewById(R.id.viewColor2).performClick();
                    break;
                case "#FF4842" :
                    layoutMiscellaneous.findViewById(R.id.viewColor3).performClick();
                    break;
                case "#3A52FC" :
                    layoutMiscellaneous.findViewById(R.id.viewColor4).performClick();
                    break;
                case "#000000" :
                    layoutMiscellaneous.findViewById(R.id.viewColor5).performClick();
                    break;


            }
        }

        layoutMiscellaneous.findViewById(R.id.layoutAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//              if (ContextCompat.checkSelfPermission(
//                      getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
//
//              ) != PackageManager.PERMISSION_GRANTED){
//                  ActivityCompat.requestPermissions(
//
//                          CreateNoteActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION
//                  );
//              }
//              else{
//              }
                  selectImage();


            }
        });
        if (alreadyAvailable != null){
            layoutMiscellaneous.findViewById(R.id.layoutDeleteNote).setVisibility(View.VISIBLE);
            layoutMiscellaneous.findViewById(R.id.layoutDeleteNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        showDeleteNoteDialog();
                }
            });
        }

    }

    private void showDeleteNoteDialog(){
        if (dialogdeleteNote == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_delete_note,
                    (ViewGroup) findViewById(R.id.layoutDeleteNoteContainer)

            );
            builder.setView(view);
            dialogdeleteNote = builder.create();
            if (dialogdeleteNote.getWindow()!=null){
                dialogdeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            view.findViewById(R.id.textDeleteNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    class DeleteNoteTask extends AsyncTask<Void, Void, Void>{

                        @Override
                        protected Void doInBackground(Void... voids) {
                            NotesDatabase.getDatabase(getApplicationContext()).noteDao().deleteNote(alreadyAvailable);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void unused) {
                            super.onPostExecute(unused);
                            Intent intent  = new Intent();
                            intent.putExtra("isNoteDeleted", true);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                    new DeleteNoteTask().execute();
                }
            });
            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogdeleteNote.dismiss();
                }
            });
        }
        dialogdeleteNote.show();
    }
    private void setSubtitleIndicatorColor(){
        GradientDrawable gradientDrawable = (GradientDrawable) viewsubtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
    }


    @SuppressLint("QueryPermissionsNeeded")
    private void selectImage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        if (intent.resolveActivity(getPackageManager())!= null){
//            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
//        }
//        else {
//            // Handle the case where no gallery app is available
//            Toast.makeText(this, "No gallery app found", Toast.LENGTH_SHORT).show();
//        }

            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage();
            }
            else{
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if (data != null){
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null){
                    try {
                        InputStream inputStream  = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        imageNote.setImageBitmap(bitmap);
                        imageNote.setVisibility(View.VISIBLE);
                        selectedImagePath = getPathFromUri(selectedImageUri);
                    }catch (Exception exception)
                    {
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
    private String getPathFromUri(Uri contentUri){
        String filePath;
        Cursor cursor  = getContentResolver()
                .query(contentUri, null, null, null, null);

        if (cursor == null){
            filePath = contentUri.getPath();
        }
        else{
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }
    public void onShareNotesAsPdf() {
        // Generate PDF content dynamically from note data
        String pdfContent = generatePdfContentFromNotes();

        // Share the PDF content
        if (pdfContent != null && !pdfContent.isEmpty()) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("application/pdf");
            share.putExtra(Intent.EXTRA_TEXT, pdfContent);
            startActivity(Intent.createChooser(share, "Share PDF"));
        } else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    private String generatePdfContentFromNotes() {
        // Generate PDF content from your note data
        // You can format the note data as per your requirements
        // For simplicity, let's assume you have a single note text
        String noteText = inputNoteText.getText().toString();
        return noteText;
    }
}