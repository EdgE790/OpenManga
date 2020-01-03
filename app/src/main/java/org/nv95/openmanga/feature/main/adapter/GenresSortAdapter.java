package org.nv95.openmanga.feature.main.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.providers.FavouritesProvider;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.staff.MangaProviderManager;

import java.util.ArrayList;

/**
 * Created by nv95 on 28.11.16.
 */

public class GenresSortAdapter extends RecyclerView.Adapter<GenresSortAdapter.TextViewHolder> implements View.OnClickListener {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_SORT = 1;
    private static final int TYPE_GENRE = 2;

    private final ArrayList<TypedString> mDataset;
    private final Callback mCallback;
    private int mSelGenre;
    private int mSelSort;

    public GenresSortAdapter(Callback callback) {
        mDataset = new ArrayList<>();
        mCallback = callback;
        mSelGenre = 0;
        mSelSort = 0;
    }

    /**
     * Loads settings from MangaProvider
     * @param context context
     * @param provider provider that will be used to load sorts
     */
    public void fromProvider(Context context, MangaProvider provider) {
        mDataset.clear();
        processSortFromProvider(context, provider);
        processGenreFromProvider(context, provider);
        mSelGenre = 0;
        if (mDataset.isEmpty()) {
            mDataset.add(new TypedString(context.getString(R.string.no_options_available), TYPE_HEADER));
        }
        notifyDataSetChanged();
    }

    private void processSortFromProvider(Context context, MangaProvider provider) {
        if (provider.hasSort()) {
            String[] data = provider.getSortTitles(context);
            if (data != null) {
                mDataset.add(new TypedString(context.getString(R.string.action_sort), TYPE_HEADER));
                for (int i = 0; i < data.length; i++) {
                    mDataset.add(new TypedString(data[i], TYPE_SORT, i));
                }
            }
            mSelSort = MangaProviderManager.restoreSortOrder(context, provider);
        } else {
            mSelSort = 0;
        }
    }

    private void processGenreFromProvider(Context context, MangaProvider provider) {
        if (provider.hasGenres()) {
            String[] data = provider.getGenresTitles(context);
            if (data != null) {
                mDataset.add(new TypedString(context.getString(
                        provider instanceof FavouritesProvider
                                ? R.string.action_category : R.string.action_genre
                ), TYPE_HEADER));
                for (int i = 0; i < data.length; i++) {
                    mDataset.add(new TypedString(data[i], TYPE_GENRE, i));
                }
            }
        }
    }

    public int getSelectedGenre() {
        return mSelGenre;
    }

    public int getSelectedSort() {
        return mSelSort;
    }

    @NonNull
    @Override
    public TextViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_SORT:
            case TYPE_GENRE:
                RadioCheckHolder holder = new RadioCheckHolder(inflater.inflate(R.layout.item_radiocheck, parent, false));
                holder.itemView.setOnClickListener(this);
                return holder;
            default:
                return new TextViewHolder(inflater.inflate(R.layout.header_group, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(TextViewHolder holder, int position) {
        TypedString item = mDataset.get(position);
        holder.getTextView().setText(item.data);
        if (holder instanceof RadioCheckHolder) {
            ((RadioCheckHolder) holder).setChecked(
                    item.type == TYPE_SORT && item.subPosition == mSelSort
                            || item.type == TYPE_GENRE && item.subPosition == mSelGenre
            );
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mDataset.get(position).type;
    }

    private int getAbsolutePosition(int type, int subPos) {
        TypedString o;
        for (int i = 0; i < mDataset.size(); i++) {
            o = mDataset.get(i);
            if (o.type == type && o.subPosition == subPos) {
                return i;
            }
        }
        return -1;
    }

    @Nullable
    private String getSelectedGenreName() {
        int pos = getAbsolutePosition(TYPE_GENRE, mSelGenre);
        return pos == -1 ? null : mDataset.get(pos).data;
    }

    @Nullable
    private String getSelectedSortName() {
        int pos = getAbsolutePosition(TYPE_SORT, mSelSort);
        return pos == -1 ? null : mDataset.get(pos).data;
    }

    @Override
    public void onClick(View view) {
        Object tag = view.getTag();
        if (tag instanceof RadioCheckHolder) {
            int pos = ((RadioCheckHolder) tag).getAdapterPosition();
            TypedString item = mDataset.get(pos);
            if (item.type == TYPE_SORT) {
                mSelSort = item.subPosition;
                mCallback.onApply(mSelGenre, mSelSort, getSelectedGenreName(), item.data);

                notifyDataSetChanged();
            } else if (item.type == TYPE_GENRE) {
                mSelGenre = item.subPosition;
                mCallback.onApply(mSelGenre, mSelSort, item.data, getSelectedSortName());
                notifyDataSetChanged();
            }
        }
    }

    private static class TypedString {
        final String data;
        final int type;
        int subPosition;

        public TypedString(String data, int type) {
            this.data = data;
            this.type = type;
            this.subPosition = 0;
        }

        public TypedString(String data, int type, int subpos) {
            this.data = data;
            this.type = type;
            this.subPosition = subpos;
        }

        @NonNull
        @Override
        public String toString() {
            return data;
        }
    }

    private static class RadioCheckHolder extends TextViewHolder implements Checkable {

        RadioCheckHolder(View itemView) {
            super(itemView);
            itemView.setTag(this);
        }

        @Override
        public void setChecked(boolean b) {
            ((Checkable) itemView).setChecked(b);
        }

        @Override
        public boolean isChecked() {
            return ((Checkable) itemView).isChecked();
        }

        @Override
        public void toggle() {
            ((Checkable) itemView).toggle();
        }
    }

    static class TextViewHolder extends RecyclerView.ViewHolder {

        TextViewHolder(View itemView) {
            super(itemView);
        }

        TextView getTextView() {
            return (TextView) itemView;
        }
    }

    public interface Callback {
        void onApply(int genre, int sort, @Nullable String genreName, @Nullable String sortName);
    }
}
