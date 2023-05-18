package com.nikol.sketchit;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nikol.sketchit.databinding.LayoutChatMessageBinding;

import java.util.LinkedList;
import java.util.List;

public class ChatLayoutAdapter extends RecyclerView.Adapter<ChatLayoutAdapter.ChatLayoutViewHolder> {
    private List<String> messages = new LinkedList<>();

    @NonNull
    @Override
    public ChatLayoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutChatMessageBinding binding = LayoutChatMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ChatLayoutViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatLayoutViewHolder holder, int position) {
        holder.setData(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void getUpdates(List<String> messages) {
        int newMessagesCount = messages.size();
        int lastMessagesCount = this.messages.size();
        if (newMessagesCount > lastMessagesCount) {
            this.messages = messages;
            if (newMessagesCount - lastMessagesCount > 1) {
                notifyItemRangeInserted(this.messages.size(), newMessagesCount - lastMessagesCount);
            } else {
                notifyItemInserted(newMessagesCount - 1);
            }
        }
    }

    public static class ChatLayoutViewHolder extends RecyclerView.ViewHolder {
        private final LayoutChatMessageBinding binding;

        public ChatLayoutViewHolder(LayoutChatMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(String message) {
            binding.textChatMessage.setText(message);
        }
    }
}
