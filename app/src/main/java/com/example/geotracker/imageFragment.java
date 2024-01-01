package com.example.geotracker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;
import java.util.Objects;


public class imageFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAPTURE_IMAGE_REQUEST = 2;

    private int journeyId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);

        journeyId = requireArguments().getInt("JOURNEY_ID");



        Button button = view.findViewById(R.id.imageButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        return view;
    }

    private void selectImage() {
        // Open gallery
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            saveImageUriToDatabase(selectedImage);
        }
    }

    private void saveImageUriToDatabase(Uri selectedImage) {
        JourneyImage journeyImage = new JourneyImage();
        journeyImage.imageUri = selectedImage.toString();

        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(requireContext(),
                    AppDatabase.class, "Journey-Database").build();
            db.journeyImageDao().insert(journeyImage);
        }).start();
    }

    private void displayImages() {
        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(getContext(), AppDatabase.class, "database-name").build();
            List<JourneyImage> images = db.journeyDao().getJourneyImagesForJourney(journeyId);

            getActivity().runOnUiThread(() -> {
                for (JourneyImage image : images) {
                    Uri imageUri = Uri.parse(image.imageUri);
                    // Use Glide or Picasso to display the image
                }
            });
        }).start();
    }
}