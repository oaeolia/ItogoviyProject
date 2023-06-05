package com.nikol.sketchit;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class GameRoundEndMessageFragment extends DialogFragment {
    public static final String ARG_KEY_MESSAGE = "message";
    public static final String ARG_KEY_RIGHT_ANSWER = "rightAnswer";
    public static final String ARG_KEY_IS_ANSWER_RIGHT = "isVariantTrue";
    public static final String ARG_KEY_REMAINING_TIME = "remainingTime";

    public GameRoundEndMessageFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO: Set args to view
        Bundle args = getArguments();
        return inflater.inflate(R.layout.fragment_game_round_end_message, container, false);
    }
}