package com.deparse.documentviewer;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;

import com.tencent.smtt.sdk.QbSdk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

/**
 * @author MartinKent
 * @time 2018/1/11
 */
@SuppressWarnings("ALL")
public class DocumentHelper {
    private static final String HTTP_PREFIX = "http://";
    private static final String HTTPS_PREFIX = "https://";
    private static final String FILE_PREFIX = "file:///";
    private static final String ASSETS_PREFIX = "file:///android_asset/";

    private static MessageProvider mMessageProvider = null;

    private static boolean isInited = false;

    private static final List<String> FORMATS_FOR_TBS_READER_VIEW = Arrays.asList(
            ".doc",
            ".docx",
            ".ppt",
            ".pptx",
            ".xls",
            ".xlsx",
            ".pdf",
            ".epub",
            ".chm"
    );
    private static final List<String> FORMATS_FOR_TBS_WEB_VIEW = Arrays.asList(
            ".txt",
            ".ini",
            ".log",
            ".bat",
            ".php",
            ".js",
            ".lrc",
            ".html",
            ".htm",
            ".xml",
            ".mht",
            ".gif",
            ".url"
    );
    private static final List<String> FORMATS_FOR_IMAGE_VIEW = Arrays.asList(
            ".jpg",
            ".jpeg",
            ".png",
            ".bmp"
    );

    public static void init(Application app) {
        setMessageProvider(new DefaultMessageProvider(app.getApplicationContext()));
        QbSdk.initX5Environment(app.getApplicationContext(), new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {
                System.out.println("onCoreInitFinished");
            }

            @Override
            public void onViewInitFinished(boolean b) {
                System.out.println("onViewInitFinished " + b);
            }
        });
        isInited = true;
    }

    public static void setMessageProvider(MessageProvider provider) {
        DocumentHelper.mMessageProvider = provider;
    }

    static String getMsg(MessageType type, Object... formatArgs) {
        if (null == mMessageProvider) {
            throw new RuntimeException("DocumentHelper.init() is never called.");
        }
        return mMessageProvider.getMsg(type, formatArgs);
    }

    static void view(Context context, String filePath, String tmpPath, final DocumentView.Callback callback) {
        if (null == mMessageProvider || !isInited) {
            throw new RuntimeException("DocumentHelper.init() is never called.");
        }
        final String tempPath = ensureTempPath(context, tmpPath, callback);
        if (null == tempPath) {
            return;
        }
        if (filePath.toLowerCase().startsWith(ASSETS_PREFIX)) {
            String ext = DocumentHelper.getExtName(filePath);
            if (FORMATS_FOR_TBS_WEB_VIEW.contains(ext)) {
                callback.showWeb(filePath);
                return;
            }
            String path = copyAssetsToSDCard(context, filePath, tempPath);
            if (null == path) {
                callback.showError(DocumentHelper.getMsg(MessageType.COPY_FILE_FAILED));
                return;
            }
            showFile(path, tempPath, callback);
        } else if (filePath.toLowerCase().startsWith(FILE_PREFIX)) {
            showFile(filePath, tempPath, callback);
        } else if (filePath.toLowerCase().startsWith(HTTP_PREFIX) || filePath.toLowerCase().startsWith(HTTPS_PREFIX)) {
            String ext = DocumentHelper.getExtName(filePath);
            if (null != ext) {
                if (FORMATS_FOR_IMAGE_VIEW.contains(ext)) {
                    callback.showImage(filePath, true);
                    return;
                }
                if (FORMATS_FOR_TBS_WEB_VIEW.contains(ext)) {
                    callback.showWeb(filePath);
                    return;
                }
                downloadFile(filePath, tempPath, callback);
            } else {
                callback.showError(DocumentHelper.getMsg(MessageType.FILE_FORMAT_NOT_SUPPORTED));
            }
        } else {
            showFile(filePath, tempPath, callback);
        }
    }

    private static void showFile(String filePath, String tempPath, final DocumentView.Callback callback) {
        String extName = getExtName(filePath);
        if (TextUtils.isEmpty(extName)) {
            callback.showError(DocumentHelper.getMsg(MessageType.FILE_FORMAT_NOT_SUPPORTED));
            return;
        }
        if (FORMATS_FOR_TBS_READER_VIEW.contains(extName)) {
            callback.showDoc(filePath, tempPath);
            return;
        }
        if (FORMATS_FOR_IMAGE_VIEW.contains(extName)) {
            callback.showImage(filePath, false);
            return;
        }
        if (FORMATS_FOR_TBS_WEB_VIEW.contains(extName)) {
            callback.showWeb(filePath);
            return;
        }
        callback.showError(DocumentHelper.getMsg(MessageType.FILE_FORMAT_NOT_SUPPORTED));
    }

    private static String getCachePath(Context context) {
        try {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + context.getPackageName() + "/caches/files");
                if (dir.exists() && dir.isDirectory()) {
                    if (dir.isDirectory()) {
                        File testFile = new File(dir, context.getPackageName() + "_" + System.currentTimeMillis() + ".txt");
                        if (testFile.exists()) {
                            testFile.delete();
                        }
                        if (!testFile.exists() && testFile.createNewFile()) {
                            if (testFile.exists()) {
                                testFile.delete();
                            }
                            return dir.getAbsolutePath();
                        }
                    }
                }
                if (dir.mkdirs()) {
                    return dir.getAbsolutePath();
                }
            }
            File cacheDir = new File(context.getCacheDir().getAbsolutePath() + "/caches/files");
            if (!cacheDir.exists() || cacheDir.isDirectory()) {
                cacheDir.mkdirs();
            }
            return cacheDir.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return context.getCacheDir().getAbsolutePath();
    }

    private static String ensureTempPath(Context context, String tempPath, DocumentView.Callback callback) {
        if (TextUtils.isEmpty(tempPath)) {
            tempPath = getCachePath(context);
        }
        File tmpDir = new File(tempPath);
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }
        if (!tmpDir.isDirectory()) {
            callback.showError(DocumentHelper.getMsg(MessageType.CREATE_TEMP_DIR_FAILED, tempPath));
            return null;
        }
        return tmpDir.getPath();
    }

    private static String copyAssetsToSDCard(Context context, String assetPath, String tmpPath) {
        String asset = assetPath.substring(ASSETS_PREFIX.length());
        try {
            String id = DocumentHelper.md5(assetPath);
            String fullFileName = new File(asset).getName();
            String fileName = fullFileName.contains(".") ? fullFileName.substring(0, fullFileName.lastIndexOf(".")) : fullFileName;
            String ext = fullFileName.contains(".") ? fullFileName.substring(fullFileName.lastIndexOf(".")) : "";
            File outFile = new File(tmpPath, new File(tmpPath, fileName + "_" + id + ext).getName());
            if (outFile.exists()) {
                outFile.delete();
            }
            outFile.createNewFile();
            if (DocumentHelper.copyAssetFile(context, asset, outFile.getAbsolutePath())) {
                return outFile.getAbsolutePath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean copyAssetFile(Context context, String filename, String destinationPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = context.getAssets().open(filename);
            out = new FileOutputStream(destinationPath);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != in) {
                    in.close();
                }
                if (null != out) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static String getExtName(String path) {
        try {
            String lowerPath = path.toLowerCase();
            String ext = lowerPath.substring(lowerPath.lastIndexOf("."));
            if (FORMATS_FOR_TBS_READER_VIEW.contains(ext) || FORMATS_FOR_IMAGE_VIEW.contains(ext) || FORMATS_FOR_TBS_WEB_VIEW.contains(ext)) {
                return ext;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }

    private static void downloadFile(final String url, final String tempPath, final DocumentView.Callback callback) {
        new AsyncTask<String, Integer, String>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                callback.showMsg(DocumentHelper.getMsg(MessageType.FILE_IS_DOWNLOADING));
            }

            @Override
            protected String doInBackground(String... params) {
                InputStream is = null;
                OutputStream fos = null;
                try {
                    String urlStr = params[0];
                    String fileExtName = DocumentHelper.getExtName(url);
                    if (null == fileExtName) {
                        return null;
                    }
                    String tempPath = params[1];
                    String id = DocumentHelper.md5(url);
                    String fileName = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
                    File outFile = new File(tempPath, fileName + "_" + id + fileExtName);
                    if (outFile.exists()) {
//                        return outFile.getAbsolutePath();
                        outFile.delete();
                    }
                    outFile.createNewFile();
                    URL url = DocumentHelper.change(new URL(urlStr));
                    is = url.openStream();
                    fos = new FileOutputStream(outFile);
                    byte[] buffer = new byte[1024];
                    int byteCount;
                    while ((byteCount = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, byteCount);
                    }
                    fos.flush();
                    is.close();
                    fos.close();
                    return outFile.getAbsolutePath();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (null != is) {
                            is.close();
                        }
                        if (null != fos) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (TextUtils.isEmpty(s)) {
                    callback.showError(DocumentHelper.getMsg(MessageType.FILE_DOWNLOAD_FAILED));
                    return;
                }
                showFile(s, tempPath, callback);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url, tempPath);
    }

    public static URL change(URL srcUrl) {
        String srouce = null;
        try {
            srouce = URLEncoder.encode(srcUrl.toString(), "utf-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        srouce = srouce.replace("%2F", "/");
        srouce = srouce.replace("%3A", ":");
        URL tarUrl = null;
        try {
            tarUrl = new URL(srouce);
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return tarUrl;
    }
}
