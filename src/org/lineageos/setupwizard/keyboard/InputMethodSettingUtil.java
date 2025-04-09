package org.lineageos.setupwizard.keyboard;

import android.content.ContentResolver;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;

public class InputMethodSettingUtil {
    private static final String TAG = "InputMethodSettingUtil";

    private static final boolean DEBUG = true;
    private static final String SUBTYPE_MODE_KEYBOARD = "keyboard";
    private static final char INPUT_METHOD_SEPARATER = ':';
    private static final char INPUT_METHOD_SUBTYPE_SEPARATER = ';';
    private static final int NOT_A_SUBTYPE_ID = -1;

    private static final TextUtils.SimpleStringSplitter sStringInputMethodSplitter
            = new TextUtils.SimpleStringSplitter(INPUT_METHOD_SEPARATER);

    private static final TextUtils.SimpleStringSplitter sStringInputMethodSubtypeSplitter
            = new TextUtils.SimpleStringSplitter(INPUT_METHOD_SUBTYPE_SEPARATER);


    public static String buildInputMethodsAndSubtypesString(
            final HashMap<String, HashSet<String>> imeToSubtypesMap) {
        final StringBuilder builder = new StringBuilder();
        for (final String imi : imeToSubtypesMap.keySet()) {
            if (builder.length() > 0) {
                builder.append(INPUT_METHOD_SEPARATER);
            }
            final HashSet<String> subtypeIdSet = imeToSubtypesMap.get(imi);
            builder.append(imi);
            for (final String subtypeId : subtypeIdSet) {
                builder.append(INPUT_METHOD_SUBTYPE_SEPARATER).append(subtypeId);
            }
        }
        return builder.toString();
    }

    private static HashMap<String, HashSet<String>> getEnabledInputMethodsAndSubtypeList(
            ContentResolver resolver) {
        final String enabledInputMethodsStr = Settings.Secure.getString(
                resolver, Settings.Secure.ENABLED_INPUT_METHODS);
        if (DEBUG) {
            Log.d(TAG, "--- Load enabled input methods: " + enabledInputMethodsStr);
        }
        return parseInputMethodsAndSubtypesString(enabledInputMethodsStr);
    }

    public static HashMap<String, HashSet<String>> parseInputMethodsAndSubtypesString(
            final String inputMethodsAndSubtypesString) {
        final HashMap<String, HashSet<String>> subtypesMap = new HashMap<>();
        if (TextUtils.isEmpty(inputMethodsAndSubtypesString)) {
            return subtypesMap;
        }
        sStringInputMethodSplitter.setString(inputMethodsAndSubtypesString);
        while (sStringInputMethodSplitter.hasNext()) {
            final String nextImsStr = sStringInputMethodSplitter.next();
            sStringInputMethodSubtypeSplitter.setString(nextImsStr);
            if (sStringInputMethodSubtypeSplitter.hasNext()) {
                final HashSet<String> subtypeIdSet = new HashSet<>();
                // The first element is {@link InputMethodInfoId}.
                final String imiId = sStringInputMethodSubtypeSplitter.next();
                while (sStringInputMethodSubtypeSplitter.hasNext()) {
                    subtypeIdSet.add(sStringInputMethodSubtypeSplitter.next());
                }

                subtypesMap.put(imiId, subtypeIdSet);
            }
        }
        return subtypesMap;
    }


    private static int getInputMethodSubtypeSelected(ContentResolver resolver) {
        try {
            return Settings.Secure.getInt(resolver,
                    Settings.Secure.SELECTED_INPUT_METHOD_SUBTYPE);
        } catch (Settings.SettingNotFoundException e) {
            return -1;
        }
    }
}
