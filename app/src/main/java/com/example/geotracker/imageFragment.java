package com.example.geotracker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class imageFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAPTURE_IMAGE_REQUEST = 2;

    private RecyclerView recyclerViewImages;

    private ImageAdapter imageAdapter;

    private int journeyId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);

        recyclerViewImages = view.findViewById(R.id.recyclerView);
        recyclerViewImages.setLayoutManager(new LinearLayoutManager(getContext()));
        imageAdapter = new ImageAdapter(getContext(), new ArrayList<>());
        recyclerViewImages.setAdapter(imageAdapter);


        journeyId = requireArguments().getInt("JOURNEY_ID");



        Button button = view.findViewById(R.id.imageButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        displayImages();
        return view;
    }

    private void selectImage() {
        // Open gallery
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("ImageFragment", "Getting image");
        Log.d("ImageFragment", String.valueOf(requestCode));
        Log.d("ImageFragment", String.valueOf(resultCode));
        Log.d("ImageFragment", String.valueOf(data));
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {

            Uri selectedImage = data.getData();
            try {
                // Correctly extract the flags for URI permission
                int takeFlags = data.getFlags();
                takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                // Check for and request persistable permissions
                if (takeFlags != 0) {
                    getActivity().getContentResolver().takePersistableUriPermission(selectedImage, takeFlags);
                }
            } catch (SecurityException e) {
                e.printStackTrace();
                // Handle exception
            }
            saveImageUriToDatabase(selectedImage);
            displayImages();

        }
    }

    private void saveImageUriToDatabase(Uri selectedImage) {
        JourneyImage journeyImage = new JourneyImage();
        journeyImage.imageUri = selectedImage.toString();
        journeyImage.journeyId = journeyId;


        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(requireContext(),
                    AppDatabase.class, "Journey-Database").build();
            db.journeyImageDao().insert(journeyImage);
        }).start();
    }

    private void displayImages() {
        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(getContext(), AppDatabase.class, "Journey-Database").build();
            List<JourneyImage> images = db.journeyDao().getJourneyImagesForJourney(journeyId);
            List<Uri> uris = new ArrayList<>();

            for (JourneyImage journeyImage : images) {
                if (journeyImage.imageUri != null && !journeyImage.imageUri.isEmpty()) {
                    uris.add(Uri.parse(journeyImage.imageUri));
                }
            }
            Log.d("ImageFragment", "Displaying images: " + uris.toString());


            getActivity().runOnUiThread(() -> {
                imageAdapter = new ImageAdapter(getContext(), uris);
                recyclerViewImages.setAdapter(imageAdapter);
            });
        }).start();
    }
}