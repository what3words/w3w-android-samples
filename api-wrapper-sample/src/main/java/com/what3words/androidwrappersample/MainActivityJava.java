package com.what3words.androidwrappersample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.what3words.androidwrapper.What3WordsV3;
import com.what3words.androidwrapper.voice.Microphone;
import com.what3words.androidwrapper.voice.VoiceBuilder;
import com.what3words.javawrapper.request.Coordinates;
import com.what3words.javawrapper.response.Suggestion;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivityJava extends AppCompatActivity {

    private VoiceBuilder voiceBuilder = null;
    MaterialButton buttonAutoSuggestVoice;
    TextView volumeAutoSuggestVoice;
    TextView resultAutoSuggestVoice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        What3WordsV3 wrapper = new What3WordsV3(BuildConfig.W3W_API_KEY, this);

        MaterialButton buttonConvertTo3wa = findViewById(R.id.buttonConvertTo3wa);
        TextInputEditText textInputConvertTo3wa = findViewById(R.id.textInputConvertTo3wa);
        TextView resultConvertTo3wa = findViewById(R.id.resultConvertTo3wa);

        //convert-to-3wa sample
        buttonConvertTo3wa.setOnClickListener(view -> {
            try {
                String[] latLong = textInputConvertTo3wa.getText().toString().replaceAll("\\s", "").split(",");
                Double lat = Double.parseDouble(latLong[0]);
                Double lng = Double.parseDouble(latLong[1]);
                if (lat != null && lng != null) {
                    Observable.fromCallable(() -> wrapper.convertTo3wa(new Coordinates(lat, lng)).execute())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(result -> {
                                if (result.isSuccessful()) {
                                    resultConvertTo3wa.setText(String.format("3 word address: %s", result.getWords()));
                                } else {
                                    resultConvertTo3wa.setText(result.getError().getMessage());
                                }
                            });
                } else {
                    resultConvertTo3wa.setText("invalid lat,long");
                }
            } catch (Exception e) {
                resultConvertTo3wa.setText("invalid lat,long");
            }
        });

        MaterialButton buttonConvertToCoordinates = findViewById(R.id.buttonConvertToCoordinates);
        TextInputEditText textInputConvertToCoordinates = findViewById(R.id.textInputConvertToCoordinates);
        TextView resultConvertToCoordinates = findViewById(R.id.resultConvertToCoordinates);

        //convert-to-coordinates sample
        buttonConvertToCoordinates.setOnClickListener(view -> {
            String address = "";
            if (textInputConvertToCoordinates.getText() != null)
                address = textInputConvertToCoordinates.getText().toString();
            String finalAddress = address;
            Observable.fromCallable(() -> wrapper.convertToCoordinates(finalAddress).execute())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                        if (result.isSuccessful()) {
                            resultConvertToCoordinates.setText(String.format("Coordinates: %s,%s", result.getCoordinates().getLat(), result.getCoordinates().getLng()));
                        } else {
                            resultConvertToCoordinates.setText(result.getError().getMessage());
                        }
                    });

        });

        MaterialButton buttonAutoSuggest = findViewById(R.id.buttonAutoSuggest);
        TextInputEditText textInputAutoSuggest = findViewById(R.id.textInputAutoSuggest);
        TextView resultAutoSuggest = findViewById(R.id.resultAutoSuggest);

        //autosuggest sample
        buttonAutoSuggest.setOnClickListener(view -> {
            String query = "";
            if (textInputAutoSuggest.getText() != null)
                query = textInputAutoSuggest.getText().toString();
            String finalQuery = query;
            Observable.fromCallable(() -> wrapper.autosuggest(finalQuery).execute())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                        if (result.isSuccessful()) {
                            List<String> suggestionsWords = new ArrayList<>();
                            for (Suggestion suggestion : result.getSuggestions()) {
                                suggestionsWords.add(suggestion.getWords());
                            }
                            resultAutoSuggest.setText(String.format("Suggestions: %s", TextUtils.join(",", suggestionsWords)));
                        } else {
                            resultAutoSuggest.setText(result.getError().getMessage());
                        }
                    });

        });

        buttonAutoSuggestVoice = findViewById(R.id.buttonAutoSuggestVoice);
        volumeAutoSuggestVoice = findViewById(R.id.volumeAutoSuggestVoice);
        resultAutoSuggestVoice = findViewById(R.id.resultAutoSuggestVoice);

        //voice autosuggest sample
        Microphone microphone = new Microphone().onListening(volume -> {
            if (volume != null) {
                volumeAutoSuggestVoice.setText(String.format("volume: %s", Math.round(volume * 100)));
            }
        });

        voiceBuilder = wrapper.autosuggest(microphone, "en")
                .onSuggestions(suggestions -> {
                    List<String> suggestionsWords = new ArrayList<>();
                    for (Suggestion suggestion : suggestions) {
                        suggestionsWords.add(suggestion.getWords());
                    }
                    resultAutoSuggestVoice.setText(String.format("Suggestions: %s", TextUtils.join(",", suggestionsWords)));
                    buttonAutoSuggestVoice.setIconResource(R.drawable.ic_record);
                })
                .onError(error -> {
                    resultAutoSuggestVoice.setText(error.getMessage());
                    buttonAutoSuggestVoice.setIconResource(R.drawable.ic_record);
                });

        buttonAutoSuggestVoice.setOnClickListener(view -> {
            if (voiceBuilder != null && voiceBuilder.isListening()) {
                buttonAutoSuggestVoice.setIconResource(R.drawable.ic_record);
                voiceBuilder.stopListening();
            } else if (voiceBuilder != null && !voiceBuilder.isListening()) {
                int permission = PermissionChecker.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
                if (permission == PermissionChecker.PERMISSION_GRANTED) {
                    buttonAutoSuggestVoice.setIconResource(R.drawable.ic_stop);
                    voiceBuilder.startListening();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            buttonAutoSuggestVoice.setIconResource(R.drawable.ic_stop);
            voiceBuilder.startListening();
        } else {
            resultAutoSuggestVoice.setText("record audio permission required");
        }
    }
}