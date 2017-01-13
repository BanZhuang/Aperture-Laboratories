package com.cute.meido;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
/*
 未接来电监听模块
 */


public class MCCObserver extends ContentObserver {
    private Context mContext;
    private long mMissedCallDate;
    private static final String[] PROJECT = new String[]{
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.NEW,
            CallLog.Calls.DATE
    };

    public MCCObserver(Context context, Handler handler) {
        super(handler);
        mContext = context;
    }

    @Override
    public void onChange(boolean selfChange) {
        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Cursor cursor = mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                PROJECT, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
                switch (type) {
                    case CallLog.Calls.MISSED_TYPE:
                        if (cursor.getInt(cursor.getColumnIndex(CallLog.Calls.NEW)) == 1) {
                            String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                            long date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                            if (!checkSameItem(date)) {
                                Intent intent = new Intent("com.cute.meido.MISSED_CALL");
                                intent.putExtra("number",number);
                                mContext.sendBroadcast(intent);

                            }
                        }
                        break;
                }
            }
            cursor.close();
        }
    }
    private boolean checkSameItem(long date) {
        return mMissedCallDate == date;
    }
}
