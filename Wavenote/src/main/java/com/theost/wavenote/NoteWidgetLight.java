package com.theost.wavenote;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.content.ContextCompat;

import com.simperium.Simperium;
import com.simperium.client.Bucket;
import com.simperium.client.BucketObjectMissingException;
import com.simperium.client.User;
import com.theost.wavenote.models.Note;
import com.theost.wavenote.utils.ChecklistUtils;
import com.theost.wavenote.utils.PrefUtils;

import static com.theost.wavenote.models.Note.NEW_LINE;

public class NoteWidgetLight extends AppWidgetProvider {
    public static final String KEY_WIDGET_IDS_LIGHT = "key_widget_ids_light";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras() != null && intent.hasExtra(KEY_WIDGET_IDS_LIGHT)) {
            int[] ids = intent.getExtras().getIntArray(KEY_WIDGET_IDS_LIGHT);
            if (ids != null) {
                this.onUpdate(context, AppWidgetManager.getInstance(context), ids);
            } else {
                super.onReceive(context, intent);
            }
        } else {
            super.onReceive(context, intent);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            Bundle appWidgetOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);
            updateWidget(context, appWidgetManager, appWidgetId, appWidgetOptions);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.note_widget_light);
        resizeWidget(newOptions, views);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }

    private void resizeWidget(Bundle appWidgetOptions, RemoteViews views) {
        // Show/Hide larger title and content based on widget width
        if (appWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH) > 200) {
            views.setViewVisibility(R.id.widget_text, View.GONE);
            views.setViewVisibility(R.id.widget_text_title, View.VISIBLE);
            views.setViewVisibility(R.id.widget_text_content, View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.widget_text, View.VISIBLE);
            views.setViewVisibility(R.id.widget_text_title, View.GONE);
            views.setViewVisibility(R.id.widget_text_content, View.GONE);
        }
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle appWidgetOptions) {
        // Get widget views
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.note_widget_light);
        resizeWidget(appWidgetOptions, views);

        // Verify user authentication
        Wavenote currentApp = (Wavenote) context.getApplicationContext();
        Simperium simperium = currentApp.getSimperium();
        User user = simperium.getUser();

        // Get note id from SharedPreferences
        String key = PrefUtils.getStringPref(context, PrefUtils.PREF_NOTE_WIDGET_NOTE + appWidgetId);

        if (!key.isEmpty()) {
            // Get notes bucket
            Bucket<Note> notesBucket = currentApp.getNotesBucket();

            try {
                // Update note
                Note updatedNote = notesBucket.get(key);

                // Prepare bundle for NoteEditorActivity
                Bundle arguments = new Bundle();
                arguments.putBoolean(NoteEditorFragment.ARG_IS_FROM_WIDGET, true);
                arguments.putString(NoteEditorFragment.ARG_ITEM_ID, updatedNote.getSimperiumKey());
                arguments.putBoolean(NoteEditorFragment.ARG_MARKDOWN_ENABLED, updatedNote.isMarkdownEnabled());
                arguments.putBoolean(NoteEditorFragment.ARG_PREVIEW_ENABLED, updatedNote.isPreviewEnabled());

                // Create intent to navigate to selected note on widget click
                Intent intent = new Intent(context, NoteEditorActivity.class);
                intent.putExtras(arguments);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                // Remove title from content
                String title = updatedNote.getTitle();
                String contentWithoutTitle = updatedNote.getContent().toString().replace(title, "");
                int indexOfNewline = contentWithoutTitle.indexOf(NEW_LINE) + 1;
                String content = contentWithoutTitle.substring(indexOfNewline < contentWithoutTitle.length() ? indexOfNewline : 0);

                // Set widget content
                views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
                views.setTextViewText(R.id.widget_text, title);
                views.setTextColor(R.id.widget_text, ContextCompat.getColor(context, R.color.text_title_light));
                views.setTextViewText(R.id.widget_text_title, title);
                views.setTextColor(R.id.widget_text_title, ContextCompat.getColor(context, R.color.text_title_light));
                SpannableStringBuilder contentSpan = new SpannableStringBuilder(content);
                contentSpan = (SpannableStringBuilder) ChecklistUtils.addChecklistUnicodeSpansForRegex(
                        contentSpan,
                        ChecklistUtils.CHECKLIST_REGEX
                );
                views.setTextViewText(R.id.widget_text_content, contentSpan);
            } catch (BucketObjectMissingException e) {
                // Create intent to navigate to widget configure activity on widget click
                Intent intent = new Intent(context, NoteWidgetLightConfigureActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
                views.setTextViewText(R.id.widget_text, context.getString(R.string.note_not_found));
                views.setTextColor(R.id.widget_text, ContextCompat.getColor(context, R.color.text_title_light));
                views.setTextViewText(R.id.widget_text_title, context.getString(R.string.note_not_found));
                views.setTextColor(R.id.widget_text_title, ContextCompat.getColor(context, R.color.text_title_light));
                views.setViewVisibility(R.id.widget_text_content, View.GONE);
            }
        } else {
            views.setOnClickPendingIntent(R.id.widget_layout, null);
            views.setTextViewText(R.id.widget_text, context.getString(R.string.note_not_found));
            views.setTextColor(R.id.widget_text, ContextCompat.getColor(context, R.color.text_title_light));
            views.setTextViewText(R.id.widget_text_title, context.getString(R.string.note_not_found));
            views.setTextColor(R.id.widget_text_title, ContextCompat.getColor(context, R.color.text_title_light));
            views.setViewVisibility(R.id.widget_text_content, View.GONE);
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}