package com.deparse.documentviewer;

import android.content.Context;

/**
 * @author MartinKent
 * @time 2018/1/15
 */
public class DefaultMessageProvider implements MessageProvider {
    private final Context mContext;

    public DefaultMessageProvider(Context context) {
        this.mContext = context;
    }

    @Override
    public String getMsg(MessageType type, Object... formatArgs) {
        return mContext.getString(type.getResId(), formatArgs);
    }
}
