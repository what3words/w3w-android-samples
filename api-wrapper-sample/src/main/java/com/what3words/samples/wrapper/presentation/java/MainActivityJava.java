package com.what3words.samples.wrapper.presentation.java;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.what3words.core.datasource.text.W3WTextDataSource;
import com.what3words.core.datasource.voice.W3WVoiceDataSource;
import com.what3words.core.datasource.voice.audiostream.W3WAudioStream;
import com.what3words.core.datasource.voice.audiostream.W3WAudioStreamState;
import com.what3words.core.datasource.voice.audiostream.W3WMicrophone;
import com.what3words.core.types.common.W3WError;
import com.what3words.core.types.common.W3WResult;
import com.what3words.core.types.domain.W3WAddress;
import com.what3words.core.types.domain.W3WSuggestion;
import com.what3words.core.types.geometry.W3WCoordinates;
import com.what3words.core.types.language.W3WRFC5646Language;
import com.what3words.samples.wrapper.MyApplication;
import com.what3words.samples.wrapper.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.Unit;

public class MainActivityJava extends AppCompatActivity {

    private W3WVoiceDataSource voiceDataSource;

    private W3WTextDataSource textDataSource;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private MaterialButton buttonConvertTo3wa;

    private TextInputEditText textInputConvertTo3wa;

    private MaterialButton buttonConvertToCoordinates;

    private TextInputEditText textInputConvertToCoordinates;

    private TextInputEditText textInputAutoSuggest;

    private TextView resultConvertTo3wa;

    private TextView resultConvertToCoordinates;

    private MaterialButton buttonAutoSuggest;

    private TextView resultAutoSuggest;

    private W3WAudioStream audioStream;

    private boolean isRecording = false;

    MaterialButton buttonAutoSuggestVoice;
    TextView volumeAutoSuggestVoice;
    TextView resultAutoSuggestVoice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        voiceDataSource = MyApplication.getAppContainer().getW3WVoiceDataSource();
        textDataSource = MyApplication.getAppContainer().getW3wTextDataSource();

        setContentView(R.layout.activity_main);
        initializeViews();
        setupConvertTo3wa();
        setupConvertToCoordinates();
        setupAutoSuggest();
        setupAutoSuggestVoice();
    }

    private void setupAutoSuggestVoice() {
        audioStream = new W3WMicrophone();
        audioStream.setEventsListener(new W3WAudioStream.EventsListener() {
            @Override
            public void onVolumeChange(float volume) {
                runOnUiThread(() -> volumeAutoSuggestVoice.setText(String.format("volume: %s", Math.round(volume * 100))));
            }

            @Override
            public void onError(@NonNull W3WError w3WError) {

            }

            @Override
            public void onAudioStreamStateChange(@NonNull W3WAudioStreamState w3WAudioStreamState) {
                runOnUiThread(() -> {
                    if (w3WAudioStreamState == W3WAudioStreamState.STOPPED) {
                        isRecording = false;
                        buttonAutoSuggestVoice.setIconResource(R.drawable.ic_record);
                    } else if (w3WAudioStreamState == W3WAudioStreamState.LISTENING) {
                        buttonAutoSuggestVoice.setIconResource(R.drawable.ic_stop);
                        isRecording = true;
                    }
                });
            }
        });

        buttonAutoSuggestVoice.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestRecordPermission.launch(Manifest.permission.RECORD_AUDIO);
            } else {
                autosuggestWithVoice();
            }
        });
    }

    private void setupAutoSuggest() {
        buttonAutoSuggest.setOnClickListener(view -> {
            String query = "";
            if (textInputAutoSuggest.getText() != null)
                query = textInputAutoSuggest.getText().toString();
            String finalQuery = query;
            compositeDisposable.add(
                    Observable.fromCallable(() -> textDataSource.autosuggest(finalQuery, null))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(result -> {
                                if (result instanceof W3WResult.Success) {
                                    List<String> suggestionsWords = new ArrayList<>();
                                    for (W3WSuggestion suggestion : ((W3WResult.Success<List<W3WSuggestion>>) result).getValue()) {
                                        suggestionsWords.add(suggestion.getW3wAddress().getAddress());
                                    }
                                    resultAutoSuggest.setText(String.format("Suggestions: %s", TextUtils.join(",", suggestionsWords)));
                                } else {
                                    resultAutoSuggest.setText(((W3WResult.Failure<List<W3WSuggestion>>) result).getMessage());
                                }
                            }));

        });
    }

    private void setupConvertToCoordinates() {
        buttonConvertToCoordinates.setOnClickListener(view -> {
            String address = "";
            if (textInputConvertToCoordinates.getText() != null)
                address = textInputConvertToCoordinates.getText().toString();
            String finalAddress = address;
            compositeDisposable.add(
                    Observable.fromCallable(() -> textDataSource.convertToCoordinates(finalAddress))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(result -> {
                                if (result instanceof W3WResult.Success) {
                                    resultConvertToCoordinates.setText(String.format("Coordinates: %s,%s", ((W3WResult.Success<W3WCoordinates>) result).getValue().getLat(), ((W3WResult.Success<W3WCoordinates>) result).getValue().getLng()));
                                } else {
                                    resultConvertToCoordinates.setText(((W3WResult.Failure<W3WCoordinates>) result).getMessage());
                                }
                            })
            );
        });
    }

    private void setupConvertTo3wa() {
        buttonConvertTo3wa.setOnClickListener(view -> {
            try {
                String[] latLong = Objects.requireNonNull(textInputConvertTo3wa.getText()).toString().replaceAll("\\s", "").split(",");
                double lat = Double.parseDouble(latLong[0]);
                double lng = Double.parseDouble(latLong[1]);
                compositeDisposable.add(
                        Observable.fromCallable(() -> textDataSource.convertTo3wa(new W3WCoordinates(lat, lng), W3WRFC5646Language.EN_GB))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(result -> {
                                    if (result instanceof W3WResult.Success) {
                                        resultConvertTo3wa.setText(String.format("3 word address: %s", ((W3WResult.Success<W3WAddress>) result).getValue().getAddress()));
                                    } else {
                                        resultConvertTo3wa.setText(((W3WResult.Failure<W3WAddress>) result).getMessage());
                                    }
                                })
                );
            } catch (Exception e) {
                resultConvertTo3wa.setText("invalid lat,long");
            }
        });
    }

    private void initializeViews() {
        buttonConvertTo3wa = findViewById(R.id.buttonConvertTo3wa);
        textInputConvertTo3wa = findViewById(R.id.textInputConvertTo3wa);
        resultConvertTo3wa = findViewById(R.id.resultConvertTo3wa);
        buttonConvertToCoordinates = findViewById(R.id.buttonConvertToCoordinates);
        textInputConvertToCoordinates = findViewById(R.id.textInputConvertToCoordinates);
        resultConvertToCoordinates = findViewById(R.id.resultConvertToCoordinates);
        buttonAutoSuggest = findViewById(R.id.buttonAutoSuggest);
        textInputAutoSuggest = findViewById(R.id.textInputAutoSuggest);
        resultAutoSuggest = findViewById(R.id.resultAutoSuggest);
        buttonAutoSuggestVoice = findViewById(R.id.buttonAutoSuggestVoice);
        volumeAutoSuggestVoice = findViewById(R.id.volumeAutoSuggestVoice);
        resultAutoSuggestVoice = findViewById(R.id.resultAutoSuggestVoice);
    }

    private final ActivityResultLauncher<String> requestRecordPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    autosuggestWithVoice();
                } else {
                    resultAutoSuggestVoice.setText("record audio permission required");
                }
            }
    );

    private void autosuggestWithVoice() {
        if (isRecording) {
            voiceDataSource.terminate();
        } else {
            voiceDataSource.autosuggest(audioStream, W3WRFC5646Language.EN_GB, null, null, onResult -> {
                if (onResult instanceof W3WResult.Success) {
                    List<String> suggestionsWords = new ArrayList<>();
                    for (W3WSuggestion suggestion : ((W3WResult.Success<List<W3WSuggestion>>) onResult).getValue()) {
                        suggestionsWords.add(suggestion.getW3wAddress().getAddress());
                    }
                    runOnUiThread(() -> resultAutoSuggestVoice.setText(String.format("Suggestions: %s", TextUtils.join(",", suggestionsWords))));
                } else {
                    runOnUiThread(() -> resultAutoSuggestVoice.setText(((W3WResult.Failure<List<W3WSuggestion>>) onResult).getMessage()));
                }
                return Unit.INSTANCE;

            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}