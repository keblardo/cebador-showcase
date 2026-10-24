// © 2026 Cristian Luis Celie. All rights reserved. Portfolio excerpt from Cebador: shared for review only, not licensed for use, copying or redistribution.
package com.cebador.app;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Sesión de mate compartida entre el WebView (localStorage, clave cebador-session-v5)
 * y el widget nativo. Se guarda el mismo JSON que usa app.js; el campo updatedAt
 * decide quién tiene la versión más nueva.
 */
public final class SessionStore {
    private static final String PREFS = "cebador";
    private static final String K_SESSION = "session";
    private static final String K_KCAL = "kcalMap";
    private static final String K_SNACK = "snackLabel";
    private static final int SUGAR_KCAL = 16;

    private SessionStore() {}

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static String getSessionJson(Context c) {
        return prefs(c).getString(K_SESSION, null);
    }

    /** Lo llama la web cada vez que guarda la sesión. */
    public static void save(Context c, String sessionJson, String kcalMapJson, String snackLabel) {
        SharedPreferences.Editor e = prefs(c).edit().putString(K_SESSION, sessionJson);
        if (kcalMapJson != null) e.putString(K_KCAL, kcalMapJson);
        if (snackLabel != null && snackLabel.length() > 0) e.putString(K_SNACK, snackLabel);
        e.apply();
    }

    public static JSONObject load(Context c) {
        JSONObject s = null;
        String raw = getSessionJson(c);
        if (raw != null) {
            try { s = new JSONObject(raw); } catch (JSONException ex) { s = null; }
        }
        if (s == null) s = new JSONObject();
        long now = System.currentTimeMillis();
        try {
            if (!s.has("count")) s.put("count", 0);
            if (!s.has("startTime")) s.put("startTime", now);
            if (!s.has("lastCebada")) s.put("lastCebada", now);
            if (!s.has("lastWater")) s.put("lastWater", now);
            if (!s.has("cebarInterval")) s.put("cebarInterval", 4);
            if (!s.has("waterInterval")) s.put("waterInterval", 45);
            if (!s.has("mateType")) s.put("mateType", "amargo");
            if (!s.has("dulceCount")) s.put("dulceCount", 0);
            if (!s.has("selectedSnack")) s.put("selectedSnack", "galletitas-saladas");
            if (s.optJSONObject("snacks") == null) s.put("snacks", new JSONObject());
            if (!s.has("waterMl")) s.put("waterMl", 0);
            if (!s.has("pour")) s.put("pour", "M");
        } catch (JSONException ignored) {}
        return s;
    }

    private static void commit(Context c, JSONObject s) {
        try { s.put("updatedAt", System.currentTimeMillis()); } catch (JSONException ignored) {}
        prefs(c).edit().putString(K_SESSION, s.toString()).apply();
    }

    /** Misma regla que cebar() en app.js: +1, hora, dulce y agua según M/L. */
    public static void cebar(Context c) {
        JSONObject s = load(c);
        try {
            s.put("count", s.optInt("count") + 1);
            s.put("lastCebada", System.currentTimeMillis());
            if ("dulce".equals(s.optString("mateType"))) s.put("dulceCount", s.optInt("dulceCount") + 1);
            s.put("waterMl", s.optInt("waterMl") + ("L".equals(s.optString("pour")) ? 70 : 45));
        } catch (JSONException ignored) {}
        commit(c, s);
    }

    public static void setMateType(Context c, String type) {
        JSONObject s = load(c);
        try { s.put("mateType", "dulce".equals(type) ? "dulce" : "amargo"); } catch (JSONException ignored) {}
        commit(c, s);
    }

    public static void setSnack(Context c, String id) {
        JSONObject s = load(c);
        try { s.put("selectedSnack", id); } catch (JSONException ignored) {}
        commit(c, s);
    }

    public static void addSnack(Context c) {
        JSONObject s = load(c);
        try {
            String id = s.optString("selectedSnack", "galletitas-saladas");
            JSONObject snacks = s.optJSONObject("snacks");
            snacks.put(id, snacks.optInt(id) + 1);
        } catch (JSONException ignored) {}
        commit(c, s);
    }

    /** kcal de la sesión: azúcar de los dulces + galletitas (kcal/u del catálogo o de lo que mandó la web). */
    public static int kcal(Context c, JSONObject s) {
        double k = s.optInt("dulceCount") * SUGAR_KCAL;
        JSONObject map;
        try { map = new JSONObject(prefs(c).getString(K_KCAL, "{}")); } catch (JSONException ex) { map = new JSONObject(); }
        JSONObject snacks = s.optJSONObject("snacks");
        if (snacks != null) {
            Iterator<String> it = snacks.keys();
            while (it.hasNext()) {
                String id = it.next();
                Catalog.Item item = Catalog.find(c, id);
                double per = (item != null && item.kcal != null) ? item.kcal : map.optDouble(id, 0);
                k += snacks.optInt(id) * per;
            }
        }
        return (int) Math.round(k);
    }

    public static int snackCount(JSONObject s) {
        JSONObject snacks = s.optJSONObject("snacks");
        return snacks == null ? 0 : snacks.optInt(s.optString("selectedSnack"));
    }

    public static String snackName(Context c, JSONObject s) {
        Catalog.Item item = Catalog.find(c, s.optString("selectedSnack"));
        return item != null ? item.name : prefs(c).getString(K_SNACK, "Galletita");
    }
}
