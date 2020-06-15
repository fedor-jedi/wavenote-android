package com.theost.wavenote;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.theost.wavenote.models.Note;
import com.theost.wavenote.adapters.ChordAdapter;
import com.theost.wavenote.utils.ThemeUtils;
import com.theost.wavenote.utils.ViewUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChordsActivity extends ThemedAppCompatActivity {

    public static final String ARG_INSTRUMENT = "instrument";
    public static final String ARG_ALL_CHORDS = "all_chords";
    public static final String ARG_CHORDS = "chords";

    private int DEFAULT_COLUMN = 4;

    private List<Drawable> mChordsDrawable;
    private List<String> mChordsReplace;
    private List<String> mNotesList;
    private List<String> mChordsList;

    private String[] mChordReplacement;
    private String[] mNotesOrder;
    private String[] mInstrumentList;
    private String[] mColumnList;
    private String activeInstrument;

    private boolean isAllChords;

    private final int DEFAULT_INSTRUMENT = 0;
    private final int COLUMN_COUNT = 6;

    private int itemsInline;
    private int itemWidth;

    private AutoCompleteTextView mInstrumentInputView;
    private AutoCompleteTextView mColumnsInputView;
    private RecyclerView mChordsRecyclerView;
    private ChordAdapter adapter;

    @SuppressLint({"ResourceType", "SetTextI18n"})

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ThemeUtils.setTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chords);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        setTitle(R.string.chords);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        LinearLayout emptyView = findViewById(android.R.id.empty);
        ImageView mEmptyViewImage = emptyView.findViewById(R.id.image);
        TextView mEmptyViewText = emptyView.findViewById(R.id.text);
        mEmptyViewImage.setImageResource(R.drawable.m_audiotrack_black_24dp);
        mEmptyViewText.setText(R.string.empty_chords);

        isAllChords = getIntent().getBooleanExtra(ARG_ALL_CHORDS, false);
        mChordsList = getIntent().getStringArrayListExtra(ARG_CHORDS);

        mInstrumentList = getResources().getStringArray(R.array.array_musical_instruments);
        mColumnList = getResources().getStringArray(R.array.array_musical_columns);

        mChordReplacement = getResources().getStringArray(R.array.array_musical_chords_replace);
        mNotesOrder = getResources().getStringArray(R.array.array_musical_notes_order);

        activeInstrument = getIntent().getStringExtra(ARG_INSTRUMENT);
        if (activeInstrument == null) {
            String savedInstrument = Note.getNoteActiveInstrument();
            if (savedInstrument == null) {
                activeInstrument = mInstrumentList[DEFAULT_INSTRUMENT];
            } else {
                activeInstrument = savedInstrument;
            }
        }

        itemsInline = Note.getNoteActiveColumns();
        if (itemsInline == 0) itemsInline = DEFAULT_COLUMN;

        mChordsReplace = Arrays.asList(mChordReplacement);
        mNotesList = Arrays.asList(mNotesOrder);

        mChordsRecyclerView = findViewById(R.id.chord_view);
        mChordsRecyclerView.setHasFixedSize(true);
        mChordsRecyclerView.setNestedScrollingEnabled(false);
        mChordsRecyclerView.setDrawingCacheEnabled(true);
        mChordsRecyclerView.setItemViewCacheSize(20);
        mChordsRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        if (mChordsList.size() == 0) {
            mChordsRecyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            return;
        }

        mInstrumentInputView = findViewById(R.id.instrument);
        ViewUtils.removeFocus(mInstrumentInputView);
        mInstrumentInputView.setText(activeInstrument);
        ViewUtils.disbaleInput(mInstrumentInputView);
        ViewUtils.updateDropdown(this, mInstrumentInputView, mInstrumentList);
        ViewUtils.restoreFocus(mInstrumentInputView);

        if (isAllChords) {
            TextInputLayout mInstrumentLayout = findViewById(R.id.instrument_layout);
            mInstrumentLayout.setEnabled(false);
        }

        if (mChordsList.size() < itemsInline) itemsInline = mChordsList.size();

        mColumnsInputView = findViewById(R.id.columns);
        ViewUtils.removeFocus(mColumnsInputView);
        mColumnsInputView.setText(Integer.toString(itemsInline));
        ViewUtils.disbaleInput(mColumnsInputView);
        ViewUtils.updateDropdown(this, mColumnsInputView, mColumnList);
        ViewUtils.restoreFocus(mColumnsInputView);

        mColumnsInputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mColumnsInputView.clearFocus();
                int tmpItemsInline = Integer.parseInt(s.toString());
                if (tmpItemsInline != itemsInline) {
                    itemsInline = tmpItemsInline;
                    Note.setNoteActiveColumns(itemsInline);
                    updateItemSize();
                }
            }
        });

        mInstrumentInputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mInstrumentInputView.clearFocus();
                String tmpInstrument = s.toString();
                if (!(tmpInstrument.equals(activeInstrument))) {
                    activeInstrument = tmpInstrument;
                    Note.setNoteActiveInstrument(activeInstrument);
                    updateDrawables();
                }
            }
        });

        updateItemSize();
        updateDrawables();
        adapter = new ChordAdapter(this, mChordsDrawable, itemWidth);
        adapter.hasStableIds();
        mChordsRecyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if ((isAllChords) || (mChordsList.size() == 0)) return false;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chords_list, menu);
        MenuCompat.setGroupDividerEnabled(menu, true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_transposeup:
                transposeChords(1);
                return true;
            case R.id.menu_transposedown:
                transposeChords(-1);
                return true;
            case android.R.id.home:
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateItemSize();
    }

    private void updateDrawables() {
        mChordsDrawable = new ArrayList<>();
        for (String i : mChordsList) {
            if ((i.length() > 1) && ((i.substring(1, 2).equals("#")) || (i.substring(1, 2).equals("b")))) {
                String key = i.substring(0, 2);
                String end = "";
                if (mChordsReplace.indexOf(key) % 2 == 0) {
                    if (isAllChords) continue;
                    if (i.length() > 2) end = i.substring(2);
                    i = mChordsReplace.get(mChordsReplace.indexOf(key) + 1) + end;
                }
            }
            int id = getResources().getIdentifier(("mu_" + i.replace("#", "s") + "_" + activeInstrument).toLowerCase(), "drawable", this.getPackageName());
            mChordsDrawable.add(ContextCompat.getDrawable(this, id));
        }
        if (adapter != null) adapter.updateItemDrawable(mChordsDrawable);
    }

    private void updateItemSize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        itemWidth = displayMetrics.widthPixels / itemsInline;
        mChordsRecyclerView.setLayoutManager(new GridLayoutManager(this, itemsInline));
        if (adapter != null) adapter.updateItemSize(itemWidth);
    }

    private void transposeChords(int direction) {
        for (int i = 0; i < mChordsList.size(); i++) {
            String chord = mChordsList.get(i);
            String end = "";
            int chordEnd = 1;
            if (chord.length() > 1) {
                if ((chord.substring(1, 2).equals("#")) || (chord.substring(1, 2).equals("b"))) {
                    end = chord.substring(2);
                    chordEnd++;
                } else {
                    end = chord.substring(1);
                }
            }
            int index = mNotesList.indexOf(chord.substring(0, chordEnd)) + direction;
            if (index < 0) {
                index += mNotesList.size();
            } else if (index >= mNotesList.size()) {
                index -= mNotesList.size();
            }
            mChordsList.set(i, mNotesList.get(index) + end);
        }
        updateDrawables();
    }

}
