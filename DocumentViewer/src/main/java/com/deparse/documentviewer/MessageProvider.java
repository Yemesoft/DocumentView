package com.deparse.documentviewer;

/**
 * @author MartinKent
 * @time 2018/1/15
 */
public interface MessageProvider {
    String getMsg(MessageType type, Object... formatArgs);
}
