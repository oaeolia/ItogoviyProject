package com.nikol.sketchit;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nikol.sketchit.databinding.FragmentGameRoundEndMessageBinding;

/**
 * Класс фрагмента сообщения после раунда. Отвечает за отображение сообщения после каждого игрового раунда.
 */
public class GameRoundEndMessageFragment extends DialogFragment {
    public static final String ARG_KEY_MESSAGE = "message";
    public static final String ARG_KEY_RIGHT_ANSWER = "rightAnswer";
    public static final String ARG_KEY_IS_ANSWER_RIGHT = "isVariantTrue";
    public static final String ARG_KEY_REMAINING_TIME = "remainingTime";

    private FragmentGameRoundEndMessageBinding binding;

    public GameRoundEndMessageFragment() {
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        binding = FragmentGameRoundEndMessageBinding.inflate(inflater, container, false);

        assert args != null;
        if (args.getBoolean(ARG_KEY_IS_ANSWER_RIGHT, false)) {
            binding.textViewMessage.setText(R.string.message_your_varint_right);
        } else {
            binding.textViewMessage.setText(args.getString(ARG_KEY_MESSAGE));
        }
        binding.textViewTimeLeft.setText(args.getString(ARG_KEY_REMAINING_TIME));
        String mainText = getResources().getString(R.string.message_right_answer);
        binding.textViewRightAnswer.setText(mainText + " " + args.getString(ARG_KEY_RIGHT_ANSWER));

        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    public void updateTime(String time) {
        binding.textViewTimeLeft.setText(time);
    }
}