package com.deparse.documentviewer;

/**
 * @author MartinKent
 * @time 2018/1/15
 */
public enum MessageType {

    COPY_FILE_FAILED(R.string.copy_file_failed),
    FILE_IS_DOWNLOADING(R.string.file_is_downloading),
    CREATE_TEMP_DIR_FAILED(R.string.create_temp_dir_failed),
    FILE_FORMAT_NOT_SUPPORTED(R.string.file_format_not_supported),
    FILE_DOWNLOAD_FAILED(R.string.file_download_failed),
    CAN_NOT_VIEW_THE_FILE_ON_MOBILE(R.string.can_not_view_the_file_on_mobile),
    FILE_LOAD_FILED(R.string.file_load_failed);

    MessageType(int resId) {
        this.resId = resId;
    }

    private int resId;

    int getResId() {
        return resId;
    }
}
