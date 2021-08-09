package com.binglen.androidxxime;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;

/**
 * Created by palance on 16/1/11.
 */
public class AndroidXXIME extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {

    private KeyboardView keyboardView; // 对应keyboard.xml中定义的KeyboardView
    private Keyboard keyboard;  // 对应qwerty.xml中定义的Keyboard
    private CandidateView candidateView;

    private InputMethodManager m_inputMethodManager;

    private StringBuilder m_composeString = new StringBuilder();

    @Override
    public void onCreate() {
        Log.d(this.getClass().toString(), "onCreate: ");
        super.onCreate();
        m_inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
    }

    @Override
    public void onInitializeInterface() {
        Log.d(this.getClass().toString(), "onInitializeInterface: ");
    }

    //输入窗是指用户通过按键或手写或手势直接产生的文本展示区域。当输入法首次展现时，系统调用onCreateInputView()回调函数。你需要在该方法中创建输入法界面布局，并将该布局返回给系统
    @Override
    public View onCreateInputView() {
        Log.d(this.getClass().toString(), "onCreateInputView: ");
        // keyboard被创建后，将调用onCreateInputView函数
        keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard, null);  // 此处使用了keyboard.xml
        keyboard = new Keyboard(this, R.xml.qwerty);  // 此处使用了qwerty.xml
        //在此视图上附加键盘。键盘可以随时切换,视图将重新布局本身，以适应键盘。
        keyboardView.setKeyboard(keyboard);
        keyboardView.setOnKeyboardActionListener(this);
        return keyboardView;
    }

    //候选窗用来展现输入法转换过的供用户选择的候选字串，系统将调用onCreateCandidatesView()使输入法创建并展现出候选窗。你需要实现该方法，返回一套布局来展现候选窗，当不需要展现候选窗时可以返回null。该方法默认就会返回null，因此如果你什么都不做就会什么都不展现
    @Override
    public View onCreateCandidatesView() {
        Log.d(this.getClass().toString(), "onCreateCandidatesView: ");
        candidateView = new CandidateView(this);
        return candidateView;
    }

    @Override
    public void onStartInput(EditorInfo editorInfo, boolean restarting) {
        super.onStartInput(editorInfo, restarting);
        Log.d(this.getClass().toString(), "onStartInput: ");

        m_composeString.setLength(0);
        updateCandidates();

    }

    private void updateCandidates() {
        Log.d(this.getClass().toString(), "updateCandidates: ");
        if (m_composeString.length() > 0) {
            setCandidatesViewShown(true);
        } else {
            setCandidatesViewShown(false);
        }
        if (candidateView != null) {
            ArrayList<String> list = new ArrayList<String>();
            list.add(m_composeString.toString());
            candidateView.setSuggestions(list);
        }
    }

    @Override
    public void onPress(int primaryCode) {
    }

    @Override
    public void onRelease(int primaryCode) {
    }

    @Override
    public void onText(CharSequence text) {
    }

    @Override
    public void swipeDown() {
    }

    @Override
    public void swipeLeft() {
    }

    @Override
    public void swipeRight() {
    }

    @Override
    public void swipeUp() {
    }


    private void playClick(int keyCode) {
        // 点击按键时播放声音，在onKey函数中被调用
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        switch (keyCode) {
            case 32:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case Keyboard.KEYCODE_DONE:
            case 10:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }
    }

    // 向InputConnection中也就是焦点view中传入字符串
    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection ic = getCurrentInputConnection();
        playClick(primaryCode);
        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE:
                ic.deleteSurroundingText(1, 0);
                break;
            case Keyboard.KEYCODE_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            default:
                char code = (char) primaryCode;
                if (code == ' ') {
                    if (m_composeString.length() > 0) {
                        ic.commitText(m_composeString, m_composeString.length());
                        m_composeString.setLength(0);
                    } else {
                        ic.commitText(" ", 1);
                    }
                } else {
                    m_composeString.append(code);
                    ic.setComposingText(m_composeString, 1);
                }
                updateCandidates();
        }
    }
}
