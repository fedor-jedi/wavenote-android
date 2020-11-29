package com.theost.wavenote.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.ParcelableSpan;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.TypefaceSpan;
import android.text.style.UnderlineSpan;

import androidx.core.content.ContextCompat;

import com.theost.wavenote.R;
import com.theost.wavenote.models.Note;
import com.theost.wavenote.widgets.WavenoteEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.theost.wavenote.models.Note.NEW_LINE;
import static com.theost.wavenote.models.Note.SPACE;
import static com.theost.wavenote.widgets.TypefaceCache.TYPEFACE_NAME_ROBOTO_REGULAR;
import static com.theost.wavenote.widgets.WavenoteEditText.DISABLE_TEXTWATCHER;

public class HighlightUtils {

    private static boolean isHighlightChanged = false;
    private static final int TITLE_SPACE_LENGTH = 24;

    private static ArrayList<String> noteChordsList;
    private static ArrayList<String> noteWordsList;
    private static ArrayList<Integer> noteWordsIndexes;

    private static List<String> keyTitles;
    private static List<String> keyWords;

    private static int backTextColor;
    private static int frontTextColor;

    @SuppressLint("ResourceAsColor")
    public static void updateSyntaxHighlight(Context context, WavenoteEditText mContentEditText, String foregroundColor, boolean detectChords) {
        mContentEditText.setTag(DISABLE_TEXTWATCHER);

        if (Note.isIsNeedResourceUpdate()) updateResources(context);

        Spannable noteContent = mContentEditText.getText();
        frontTextColor = Color.parseColor(foregroundColor);

        backTextColor = ContextCompat.getColor(context, R.color.syntax_title);
        noteContent = addSyntaxHighlight(keyTitles, noteContent, true, false);
        frontTextColor = ContextCompat.getColor(context, R.color.syntax_word);
        noteContent = addSyntaxHighlight(keyWords, noteContent, false, true);

        if (detectChords) {
            frontTextColor = ContextCompat.getColor(context, R.color.syntax_chord);
            List<String> keyChords = Arrays.asList(context.getResources().getStringArray(R.array.array_musical_chords));
            noteContent = addSyntaxHighlight(keyChords, noteContent, false, true);
        }

        if (isHighlightChanged) {
            isHighlightChanged = false;
            int[] selectionIndexes = mContentEditText.getSelectionIndexes();
            mContentEditText.setText(noteContent);
            mContentEditText.restoreSelection(selectionIndexes);
        }

        mContentEditText.setTag(null);
    }

    public static Spannable addSyntaxHighlight(List<String> words, Spannable contentSpan, boolean isTitle, boolean isForeground) {
        int titleEnd = getTitleEnd(contentSpan);
        String contentStr = contentSpan.toString().replaceAll("[\\t\\r\\n\\u202F\\u00A0]", SPACE);
        if (!isForeground) contentStr = contentStr.toLowerCase();
        StringBuilder contentBuffer = new StringBuilder(contentStr);
        for (String i : words) {
            int index = titleEnd;
            while (index != -1) {
                if (!isForeground) i = i.toLowerCase();
                index = contentBuffer.indexOf(SPACE + i + SPACE, index);
                if (index != -1) {
                    ForegroundColorSpan[] markedSpans = contentSpan.getSpans(index + 1, index + 1 + i.length(), ForegroundColorSpan.class);
                    index += SPACE.length();
                    if (markedSpans.length > 0) {
                        index++;
                        continue;
                    }
                    isHighlightChanged = true;
                    int lastindex = index + i.length();
                    if (isTitle) {
                        String extraSpace = StrUtils.repeat(SPACE, (TITLE_SPACE_LENGTH - i.length()) / 2);
                        ((Editable) contentSpan).insert(lastindex, extraSpace);
                        ((Editable) contentSpan).insert(index, extraSpace);
                        contentBuffer.insert(lastindex, extraSpace);
                        contentBuffer.insert(index, extraSpace);
                        lastindex += extraSpace.length() * 2;
                    }
                    if (index < lastindex) {
                        if (!isForeground)
                            contentSpan.setSpan(new BackgroundColorSpan(backTextColor), index, lastindex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        contentSpan.setSpan(new ForegroundColorSpan(frontTextColor), index, lastindex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    index++;
                }
            }
        }
        return contentSpan;
    }

    public static int getTitleEnd(Spannable content) {
        int titleEnd = content.toString().indexOf(NEW_LINE);
        if (titleEnd == -1) titleEnd = content.length();
        return titleEnd;
    }

    public static Map<ArrayList<String>, HashMap<Integer, String>> getChordsData(Context context, String content) {
        String[] resourceChords = context.getResources().getStringArray(R.array.array_musical_chords);
        content = content.replaceAll("\\u00a0", SPACE).replaceAll("[ ]{2,}", SPACE).replaceAll("\\n+", "\n");
        String contentInline = content.replaceAll("\\n+", SPACE);

        Map<Integer, String> chordsSortedMap = new TreeMap<>();
        Map<Integer, Integer> chordsIndexesMap = new TreeMap<>();
        ArrayList<Integer> wordsInsertIndexes = new ArrayList<>();

        // Parsing chords and indexes
        for (String chord : resourceChords) {
            String splitter = " " + chord + " ";
            int index = 0;
            while (index != -1) {
                index = contentInline.indexOf(splitter, index);
                if (index != -1) {
                    chordsSortedMap.put(index, chord);
                    chordsIndexesMap.put(index, index + splitter.length());
                    index++;
                }
            }
        }

        // Don't waste time if there's no chords
        if (chordsSortedMap.size() == 0) return new HashMap<>();

        // Checking indexes for repeat and getting strings insert indexes
        int count = 0;
        if (!(chordsIndexesMap.containsKey(0))) {
            wordsInsertIndexes.add(0, 0);
            count++;
        }

        for (int i = 0; i < chordsIndexesMap.size() - 1; i++) {
            ArrayList<Integer> startIndexes = new ArrayList<>(chordsIndexesMap.keySet());
            ArrayList<Integer> endIndexes = new ArrayList<>(chordsIndexesMap.values());
            int start = startIndexes.get(i);
            int end = endIndexes.get(i);
            int nextStart = startIndexes.get(i + 1);
            int nextEnd = endIndexes.get(i + 1);
            if (end >= nextStart) {
                chordsIndexesMap.remove(start);
                chordsIndexesMap.remove(nextStart);
                chordsIndexesMap.put(start, nextEnd);
                count++;
                i--;
            } else {
                int arraySize = getArraySize(wordsInsertIndexes);
                wordsInsertIndexes.add(arraySize + 1 + count);
                count = 1;
            }
        }

        if (!(chordsIndexesMap.containsValue(content.length()))) {
            int arraySize = getArraySize(wordsInsertIndexes);
            wordsInsertIndexes.add(arraySize + 1 + count);
        }

        // Generating result
        ArrayList<String> chordsList = new ArrayList<>(chordsSortedMap.values());
        HashMap<Integer, String> wordsMap = new HashMap<>();

        ArrayList<String> wordsList = new ArrayList<>();

        int index = 0;
        if (chordsIndexesMap.containsKey(0)) index = chordsIndexesMap.get(0);
        for (Integer j : chordsIndexesMap.keySet()) {
            if (index < j) {
                wordsList.add(content.substring(index, j));
                index = chordsIndexesMap.get(j);
            }
        }
        if (index < content.length()) wordsList.add(content.substring(index));

        int offset = 0;
        for (int k = 0; k < wordsList.size(); k++) {
            String word = wordsList.get(k);
            word = word.trim();
            if (word.equals("") || word.equals(NEW_LINE)) {
                offset++;
                continue;
            }
            wordsMap.put(wordsInsertIndexes.get(k) - offset, word);
        }

        Map<ArrayList<String>, HashMap<Integer, String>> chordsData = new HashMap<>();
        chordsData.put(chordsList, wordsMap);

        return chordsData;
    }

    private static int getArraySize(ArrayList<Integer> arrayList) {
        if (arrayList.size() > 0) {
            return arrayList.get(arrayList.size() - 1);
        } else {
            return 0;
        }
    }

    public static List<String> getChordsSet(List<String> chordsList) {
        return new ArrayList<>(new LinkedHashSet<>(chordsList));
    }

    public static ArrayList<String> getAllChords(Context context) {
        String[] resourceChords = context.getResources().getStringArray(R.array.array_musical_chords);
        return new ArrayList<>(Arrays.asList(resourceChords));
    }

    public static void changeTextStyle(WavenoteEditText mContentEditText, int selectedColor, String backgroundColor) {

        // Getting resources and checking selection
        int[] cursorPositions = mContentEditText.getSelectionIndexes();
        if (cursorPositions[0] == cursorPositions[1]) return;
        int titleEnd = getTitleEnd(mContentEditText.getText());
        if (cursorPositions[0] < titleEnd) {
            if (cursorPositions[1] > titleEnd) {
                cursorPositions[0] = titleEnd;
            } else {
                return;
            }
        }

        mContentEditText.setTag(DISABLE_TEXTWATCHER);

        // Clear selected text style

        Editable noteContent = mContentEditText.getText();
        ParcelableSpan[] spans = noteContent.getSpans(cursorPositions[0], cursorPositions[1], ParcelableSpan.class);
        if (spans.length > 0) {
            ArrayList<Integer> startIndexes = new ArrayList<>();
            ArrayList<Integer> endIndexes = new ArrayList<>();
            for (ParcelableSpan span : spans) {
                ParcelableSpan newSpanStart = null;
                ParcelableSpan newSpanEnd = null;
                if (span instanceof TypefaceSpan) {
                    newSpanStart = new TypefaceSpan(((TypefaceSpan) span).getFamily());
                    newSpanEnd = new TypefaceSpan(((TypefaceSpan) span).getFamily());
                } else if (span instanceof StyleSpan) {
                    newSpanStart = new StyleSpan(((StyleSpan) span).getStyle());
                    newSpanEnd = new StyleSpan(((StyleSpan) span).getStyle());
                } else if (span instanceof RelativeSizeSpan) {
                    newSpanStart = new RelativeSizeSpan(((RelativeSizeSpan) span).getSizeChange());
                    newSpanEnd = new RelativeSizeSpan(((RelativeSizeSpan) span).getSizeChange());
                } else if (span instanceof SuperscriptSpan) {
                    newSpanStart = new SuperscriptSpan();
                    newSpanEnd = new SuperscriptSpan();
                } else if (span instanceof UnderlineSpan) {
                    newSpanStart = new UnderlineSpan();
                    newSpanEnd = new UnderlineSpan();
                } else if (span instanceof StrikethroughSpan) {
                    newSpanStart = new StrikethroughSpan();
                    newSpanEnd = new StrikethroughSpan();
                } else if (span instanceof ForegroundColorSpan) {
                    newSpanStart = new ForegroundColorSpan(((ForegroundColorSpan) span).getForegroundColor());
                    newSpanEnd = new ForegroundColorSpan(((ForegroundColorSpan) span).getForegroundColor());
                } else if (span instanceof BackgroundColorSpan) {
                    newSpanStart = new BackgroundColorSpan(((BackgroundColorSpan) span).getBackgroundColor());
                    newSpanEnd = new BackgroundColorSpan(((BackgroundColorSpan) span).getBackgroundColor());
                }
                if (newSpanStart != null) {
                    int start = noteContent.getSpanStart(span);
                    int end = noteContent.getSpanEnd(span);
                    if (start < cursorPositions[0]) {
                        noteContent.setSpan(newSpanStart, start, cursorPositions[0], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    if (end > cursorPositions[1]) {
                        noteContent.setSpan(newSpanEnd, cursorPositions[1], end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                noteContent.removeSpan(span);
            }
        }

        // Setting new style

        String font = Note.getActiveTextFont();
        if (font.equals("")) {
            noteContent.setSpan(new TypefaceSpan(TYPEFACE_NAME_ROBOTO_REGULAR), cursorPositions[0], cursorPositions[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            noteContent.setSpan(new TypefaceSpan(Note.getActiveTextFont()), cursorPositions[0], cursorPositions[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (Note.isIsTextStyleBold()) {
            noteContent.setSpan(new StyleSpan(Typeface.BOLD), cursorPositions[0], cursorPositions[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (Note.isIsTextStyleItalic()) {
            noteContent.setSpan(new StyleSpan(Typeface.ITALIC), cursorPositions[0], cursorPositions[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (Note.isIsTextStyleUpper()) {
            noteContent.setSpan(new RelativeSizeSpan(0.8f), cursorPositions[0], cursorPositions[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            noteContent.setSpan(new SuperscriptSpan(), cursorPositions[0], cursorPositions[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (Note.isIsTextStyleUnderline()) {
            noteContent.setSpan(new UnderlineSpan(), cursorPositions[0], cursorPositions[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (Note.isIsTextStyleStrikethrough()) {
            noteContent.setSpan(new StrikethroughSpan(), cursorPositions[0], cursorPositions[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (selectedColor != -1) {
            if (Note.isIsTextStyleStroke()) {
                noteContent.setSpan(new ForegroundColorSpan(Color.parseColor(backgroundColor)), cursorPositions[0], cursorPositions[1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                noteContent.setSpan(new BackgroundColorSpan(selectedColor), cursorPositions[0], cursorPositions[1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                noteContent.setSpan(new ForegroundColorSpan(selectedColor), cursorPositions[0], cursorPositions[1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        mContentEditText.setSelection(cursorPositions[1]);
        mContentEditText.setTag(null);
    }

    public static void updateResources(Context context) {
        DatabaseHelper localDatabase = new DatabaseHelper(context);
        Cursor titles = localDatabase.getDictionaryData("Title");
        Cursor words = localDatabase.getDictionaryData("Word");

        keyTitles = new ArrayList<>();
        while (titles.moveToNext()) {
            keyTitles.add(titles.getString(0));
        }

        keyWords = new ArrayList<>();
        while (words.moveToNext()) {
            keyWords.add(words.getString(0));
        }

        Note.setIsNeedResourceUpdate(false);
        localDatabase.close();
    }

}