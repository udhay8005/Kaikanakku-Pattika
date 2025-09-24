package in.udhaya.kaikanakku.ui.history;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import in.udhaya.kaikanakku.R;
import in.udhaya.kaikanakku.data.db.HistoryEntry;

/**
 * Adapter for the history RecyclerView. It uses a ListAdapter with a DiffUtil callback
 * for efficient updates when the list of history entries changes.
 */
public class HistoryAdapter extends ListAdapter<HistoryEntry, HistoryAdapter.HistoryViewHolder> {

    private final OnHistoryItemInteractionListener listener;
    private int highlightedPosition = -1;

    // A payload object to indicate that only the highlight state has changed.
    private static final Object HIGHLIGHT_PAYLOAD = new Object();

    /**
     * Interface for handling interactions with items in the history list.
     */
    public interface OnHistoryItemInteractionListener {
        void onFavoriteClicked(HistoryEntry entry);
        void onItemCopied(HistoryEntry entry, int position);
        void onItemReused(HistoryEntry entry);
    }

    public HistoryAdapter(OnHistoryItemInteractionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(itemView);
    }

    /**
     * Called to bind a view holder to the data. This is the full bind version.
     */
    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryEntry entry = getItem(position);
        holder.bind(entry);
        // Set the background based on the highlighted state.
        holder.updateHighlight();
    }

    /**
     * A more efficient version of onBindViewHolder that is called when there are payloads.
     * If the payload is our HIGHLIGHT_PAYLOAD, we only update the background.
     * Otherwise, we fall back to a full bind.
     */
    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.contains(HIGHLIGHT_PAYLOAD)) {
            holder.updateHighlight();
        } else {
            // No specific payload, do a full re-bind.
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    /**
     * Temporarily highlights an item at a given position to provide visual feedback.
     * @param position The adapter position of the item to highlight.
     */
    public void showCopyHighlight(int position) {
        highlightedPosition = position;
        // Notify the adapter that the item at this position changed, with a specific payload.
        notifyItemChanged(position, HIGHLIGHT_PAYLOAD);
    }

    /**
     * Clears the highlight from the previously highlighted item.
     */
    public void clearCopyHighlight() {
        if (highlightedPosition != -1) {
            int oldPosition = highlightedPosition;
            highlightedPosition = -1;
            // Notify the adapter to remove the highlight from the old item.
            notifyItemChanged(oldPosition, HIGHLIGHT_PAYLOAD);
        }
    }

    /**
     * The ViewHolder for a single history item.
     */
    class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView inputText;
        private final TextView outputText;
        private final TextView timestampText;
        private final ImageButton favoriteButton;
        private final ImageButton reuseButton;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            inputText = itemView.findViewById(R.id.text_view_input);
            outputText = itemView.findViewById(R.id.text_view_output);
            timestampText = itemView.findViewById(R.id.text_view_timestamp);
            favoriteButton = itemView.findViewById(R.id.button_favorite);
            reuseButton = itemView.findViewById(R.id.button_reuse);
        }

        /**
         * Binds a HistoryEntry object to the views in the ViewHolder.
         * @param entry The HistoryEntry to display.
         */
        public void bind(final HistoryEntry entry) {
            inputText.setText(entry.getInputText());
            outputText.setText(entry.getOutputText());

            // Format and display the timestamp.
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault());
            timestampText.setText(sdf.format(new Date(entry.getTimestamp())));

            if (entry.isFavorite()) {
                favoriteButton.setImageResource(R.drawable.ic_star_filled);
            } else {
                favoriteButton.setImageResource(R.drawable.ic_star_outline);
            }

            // Set up click listeners if a listener is provided.
            if (listener != null) {
                favoriteButton.setOnClickListener(v -> listener.onFavoriteClicked(getItem(getAdapterPosition())));
                reuseButton.setOnClickListener(v -> listener.onItemReused(getItem(getAdapterPosition())));
                itemView.setOnClickListener(v -> listener.onItemCopied(getItem(getAdapterPosition()), getAdapterPosition()));
            }
        }

        /**
         * Updates the background of the item view based on whether it is highlighted.
         */
        public void updateHighlight() {
            if (getAdapterPosition() == highlightedPosition) {
                // Use a color from resources for better theme support.
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.highlight_color));
            } else {
                // Set back to transparent or default background.
                itemView.setBackground(null);
            }
        }
    }

    /**
     * The DiffUtil.ItemCallback used by the ListAdapter to calculate list differences.
     */
    private static final DiffUtil.ItemCallback<HistoryEntry> DIFF_CALLBACK = new DiffUtil.ItemCallback<HistoryEntry>() {
        @Override
        public boolean areItemsTheSame(@NonNull HistoryEntry oldItem, @NonNull HistoryEntry newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull HistoryEntry oldItem, @NonNull HistoryEntry newItem) {
            return oldItem.getInputText().equals(newItem.getInputText()) &&
                    oldItem.getOutputText().equals(newItem.getOutputText()) &&
                    oldItem.isFavorite() == newItem.isFavorite();
        }
    };
}