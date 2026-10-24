// © 2026 Cristian Luis Celie. All rights reserved. Portfolio excerpt from Cebador: shared for review only, not licensed for use, copying or redistribution.
package com.cebador.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import org.json.JSONObject;

/** Widget 4x1: cebadas · Amargo/Dulce · galletita (elegir y sumar) · Cebé. */
public class CebadorWidget extends AppWidgetProvider {
    public static final String ACTION_CEBAR = "com.cebador.app.CEBAR";
    public static final String ACTION_SNACK = "com.cebador.app.SNACK";
    public static final String ACTION_AMARGO = "com.cebador.app.AMARGO";
    public static final String ACTION_DULCE = "com.cebador.app.DULCE";

    private static final int COL_ON_OLIVE = 0xFFD6E0C4, COL_ON_TERRA = 0xFFFFF1DE, COL_OFF = 0xFF93A5B2;

    @Override
    public void onUpdate(Context context, AppWidgetManager mgr, int[] ids) {
        for (int id : ids) mgr.updateAppWidget(id, build(context));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String a = intent.getAction();
        if (ACTION_CEBAR.equals(a)) { SessionStore.cebar(context); updateAll(context); return; }
        if (ACTION_SNACK.equals(a)) { SessionStore.addSnack(context); updateAll(context); return; }
        if (ACTION_AMARGO.equals(a)) { SessionStore.setMateType(context, "amargo"); updateAll(context); return; }
        if (ACTION_DULCE.equals(a)) { SessionStore.setMateType(context, "dulce"); updateAll(context); return; }
        super.onReceive(context, intent);
    }

    /** Refresca todas las instancias del widget (lo usan también el puente JS y el selector). */
    public static void updateAll(Context context) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        int[] ids = mgr.getAppWidgetIds(new ComponentName(context, CebadorWidget.class));
        if (ids != null && ids.length > 0) mgr.updateAppWidget(ids, build(context));
    }

    private static PendingIntent action(Context c, String action, int code) {
        Intent i = new Intent(c, CebadorWidget.class).setAction(action);
        return PendingIntent.getBroadcast(c, code, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static PendingIntent activity(Context c, Class<?> cls, int code) {
        Intent i = new Intent(c, cls).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(c, code, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static RemoteViews build(Context c) {
        JSONObject s = SessionStore.load(c);
        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget_cebador);

        v.setTextViewText(R.id.count, String.valueOf(s.optInt("count")));

        boolean dulce = "dulce".equals(s.optString("mateType"));
        v.setInt(R.id.btnAmargo, "setBackgroundResource", dulce ? R.drawable.seg_off : R.drawable.seg_on_olive);
        v.setTextColor(R.id.btnAmargo, dulce ? COL_OFF : COL_ON_OLIVE);
        v.setInt(R.id.btnDulce, "setBackgroundResource", dulce ? R.drawable.seg_on_terra : R.drawable.seg_off);
        v.setTextColor(R.id.btnDulce, dulce ? COL_ON_TERRA : COL_OFF);

        Catalog.Item item = Catalog.find(c, s.optString("selectedSnack"));
        int n = SessionStore.snackCount(s);
        String kcal = (item != null && item.kcal != null) ? Math.round(item.kcal) + " kcal/u" : "sin dato";
        v.setTextViewText(R.id.snackName, SessionStore.snackName(c, s));
        v.setTextViewText(R.id.snackMeta, n > 0 ? n + " u  ·  " + kcal : kcal + "  ·  tocá para elegir");
        v.setContentDescription(R.id.btnSnackPlus, "Sumar una " + SessionStore.snackName(c, s));

        v.setOnClickPendingIntent(R.id.btnCebar, action(c, ACTION_CEBAR, 1));
        v.setOnClickPendingIntent(R.id.btnSnackPlus, action(c, ACTION_SNACK, 2));
        v.setOnClickPendingIntent(R.id.btnAmargo, action(c, ACTION_AMARGO, 3));
        v.setOnClickPendingIntent(R.id.btnDulce, action(c, ACTION_DULCE, 4));
        v.setOnClickPendingIntent(R.id.snackInfo, activity(c, SnackPickerActivity.class, 5));
        v.setOnClickPendingIntent(R.id.info, activity(c, MainActivity.class, 6));
        return v;
    }
}
