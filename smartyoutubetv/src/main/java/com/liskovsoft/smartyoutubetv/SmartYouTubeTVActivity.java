package com.liskovsoft.smartyoutubetv;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import com.liskovsoft.browser.BrowserActivity;
import com.liskovsoft.browser.Controller;
import com.liskovsoft.browser.util.PageData;
import com.liskovsoft.browser.util.SimpleUIController;
import com.liskovsoft.browser.util.PageLoadHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmartYouTubeTVActivity extends BrowserActivity {
    private Controller mController;
    private final String mYouTubeTVUrl = "https://youtube.com/tv";
    private final String mLGSmartTVUserAgent = "Mozilla/5.0 (Unknown; Linux armv7l) AppleWebKit/537.1+ (KHTML, like Gecko) Safari/537.1+ LG Browser/6.00.00(+mouse+3D+SCREEN+TUNER; LGE; 42LA660S-ZA; 04.25.05; 0x00000001;); LG NetCast.TV-2013 /04.25.05 (LG, 42LA660S-ZA, wired)";
    private Map<String, String> mHeaders;
    private PageLoadHandler mPageLoadHandler;
    private PageData mPageDefaults;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    }

    @Override
    protected Controller createAndInitController(Bundle icicle) {
        mHeaders = new HashMap<>();
        mPageLoadHandler = new MyPageLoadHandler(this);
        mHeaders.put("user-agent", mLGSmartTVUserAgent);

        mController = new SimpleUIController(this);
        Intent intent = (icicle == null) ? transformIntent(getIntent()) : null;
        mPageDefaults = new PageData(mYouTubeTVUrl, mHeaders, mPageLoadHandler);
        mController.start(intent, mPageDefaults);
        //mController.load(mPageDefaults);
        return mController;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        event = doTranslateKeys(event);
        return super.onKeyDown(event.getKeyCode(), event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        event = doTranslateKeys(event);
        return super.onKeyUp(event.getKeyCode(), event);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(transformIntent(intent));
    }


    private KeyEvent doTranslateKeys(KeyEvent event) {
        event = translateBackToEscape(event);
        event = translateMenuToGuide(event);
        return event;
    }

    private KeyEvent translateBackToEscape(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            // pay attention, you must pass action_up instead of action_down
            event = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ESCAPE);
        }
        return event;
    }

    private KeyEvent translateMenuToGuide(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
            // pay attention, you must pass action_up instead of action_down
            event = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_G);
        }
        return event;
    }

    ///////////////////////// Begin Youtube filter /////////////////////


    private Intent transformIntent(Intent intent) {
        if (intent == null)
            return null;
        Uri data = intent.getData();
        intent.setData(transformUri(data));
        return intent;
    }

    private String runMultiMatcher(String input, String[] patterns) {
        Pattern regex;
        Matcher matcher;
        String result = null;
        for (String pattern : patterns) {
            regex = Pattern.compile(pattern);
            matcher = regex.matcher(input);

            if (matcher.find()) {
                result = matcher.group(1);
                break;
            }
        }

        return result;
    }

    private String extractVideoIdFromUrl(String url) {
        // https://www.youtube.com/watch?v=xtx33RuFCik
        String[] patterns = {"v=(\\w*)", "/(\\w*)$"};
        return runMultiMatcher(url, patterns);
    }

    private Uri transformUri(Uri uri) {
        if (uri == null)
            return null;
        String url = uri.toString();
        String videoId = extractVideoIdFromUrl(url);
        String videoUrlTemplate = "https://www.youtube.com/tv#/watch/video/control?v=%s";
        String format = String.format(videoUrlTemplate, videoId);
        return Uri.parse(format);
    }

    ///////////////////////// End Youtube filter /////////////////////
}