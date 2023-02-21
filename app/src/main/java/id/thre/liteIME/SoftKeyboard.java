/*
 * Copyright (C) 2008-2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
 /*
 Saya mencoba membuat keyboard sederhana, yg mana tdk ada fitur spesial.
 Saya hanya menambahkan fitur Control Alt untuk memudahkan waktu edit text.
 Agar mudah dipelajari untuk pemula (seperti saya), saya ambil base dari keyboard aosp.
 
 
 
 "Al-Ilmi Kash-Shoif"                "ThRE_Team"  20/02/2023
 */

package id.thre.liteIME;

import android.inputmethodservice.*;
import android.inputmethodservice.KeyboardView.*;
import android.view.inputmethod.*;
import android.view.*;
import android.graphics.*;
import java.util.*;
import android.content.*;
import android.preference.*;
import android.inputmethodservice.Keyboard.*;
import android.os.*;
import android.media.*;
import android.graphics.drawable.*;
import android.app.*;
import java.io.*;
import android.content.res.*;
import android.widget.*;
import android.text.*;


public class SoftKeyboard extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {

    private InputMethodManager mInputMethodManager;

    private KeyboardView mInputView;

    private int mLastDisplayWidth;
    private boolean mCapsLock;
    private long mLastShiftTime;
    
    private LatinKeyboard mSymbolsKeyboard;
    private LatinKeyboard mSymbolsShiftedKeyboard;
    private LatinKeyboard mQwertyKeyboard;
    private LatinKeyboard mCurKeyboard;

	
	
    @Override public void onCreate() {
        super.onCreate();
        mInputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
    }

    Context getDisplayContext() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            // createDisplayContext is not available.
            return this;
        }
        // TODO (b/133825283): Non-activity components Resources / DisplayMetrics update when
        //  moving to external display.
        // An issue in Q that non-activity components Resources / DisplayMetrics in
        // Context doesn't well updated when the IME window moving to external display.
        // Currently we do a workaround is to create new display context directly and re-init
        // keyboard layout with this context.
        final WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        return createDisplayContext(wm.getDefaultDisplay());
    }

    @Override public void onInitializeInterface() {
        final Context displayContext = getDisplayContext();

        if (mQwertyKeyboard != null) {
            // Configuration changes can happen after the keyboard gets recreated,
            // so we need to be able to re-build the keyboards if the available
            // space has changed.
            int displayWidth = getMaxWidth();
            if (displayWidth == mLastDisplayWidth) return;
            mLastDisplayWidth = displayWidth;
        }
        mQwertyKeyboard = new LatinKeyboard(displayContext, R.xml.qwerty);
        mSymbolsKeyboard = new LatinKeyboard(displayContext, R.xml.symbols);
        mSymbolsShiftedKeyboard = new LatinKeyboard(displayContext, R.xml.symbols_shift);
    }

    @Override public View onCreateInputView() {
        mInputView = (KeyboardView) getLayoutInflater().inflate(R.layout.input, null);
        mInputView.setOnKeyboardActionListener(this);
        mInputView.setPreviewEnabled(false);
        setLatinKeyboard(mQwertyKeyboard);
		//Variables.setCtrlOff();
        return mInputView;
		
    }

    private void setLatinKeyboard(LatinKeyboard nextKeyboard) {
       // this is for activated switch to next language or to next keyboard
	   // jika script ini diaktifkan, maka tombol -101 harus ada pada keyboard layout (qwerty.xml)
		/* if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            final boolean shouldSupportLanguageSwitchKey = mInputMethodManager.shouldOfferSwitchingToNextInputMethod(getToken());
            nextKeyboard.setLanguageSwitchKeyVisibility(shouldSupportLanguageSwitchKey);
        } */
        mInputView.setKeyboard(nextKeyboard);
    }

    @Override public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        
        // We are now going to initialize our state based on the type of
        // text being edited.
        switch (attribute.inputType & InputType.TYPE_MASK_CLASS) {
            case InputType.TYPE_CLASS_NUMBER:
            case InputType.TYPE_CLASS_DATETIME:
            case InputType.TYPE_CLASS_PHONE:
                // Numbers and dates default to the symbols keyboard, with
                // no extra features.
                mCurKeyboard = mQwertyKeyboard;
                break;
                
            default:
                // For all unknown input types, default to the alphabetic
                // keyboard with no special features.
                mCurKeyboard = mQwertyKeyboard;
                updateShiftKeyState(attribute);
        }
		
        
        // Update the label on the enter key, depending on what the application
        // says it will do.
        mCurKeyboard.setImeOptions(getResources(), attribute.imeOptions);
    }

    @Override public void onFinishInput() {
        super.onFinishInput();
        
        mCurKeyboard = mQwertyKeyboard;
        if (mInputView != null) {
            mInputView.closing();
        }
		//Variables.setCtrlOff();
    }
    
    @Override public void onStartInputView(EditorInfo attribute, boolean restarting) {
        super.onStartInputView(attribute, restarting);
        // Apply the selected keyboard to the input view.
        setLatinKeyboard(mCurKeyboard);
        mInputView.closing();
		//Variables.setCtrlOff();
    }
	

    private void updateShiftKeyState(EditorInfo attr) {
        if (attr != null && mInputView != null && mQwertyKeyboard == mInputView.getKeyboard()) {
            int caps = 0;
            EditorInfo ei = getCurrentInputEditorInfo();
            if (ei != null && ei.inputType != InputType.TYPE_NULL) {
                caps = getCurrentInputConnection().getCursorCapsMode(attr.inputType);
            }
            mInputView.setShifted(mCapsLock || caps != 0);
        }
    }
// untuk memudahkan keyEvent down / up
    private void keyDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }
	private void shiftDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(100, 100, KeyEvent.ACTION_DOWN, handleAZ(keyEventCode), 0, KeyEvent.META_SHIFT_ON));
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(100, 100, KeyEvent.ACTION_UP, handleAZ(keyEventCode), 0, KeyEvent.META_SHIFT_ON));
    }
	private void ctrlDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(100, 100, KeyEvent.ACTION_DOWN, handleAZ(keyEventCode), 0, KeyEvent.META_CTRL_ON));
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(100, 100, KeyEvent.ACTION_UP, handleAZ(keyEventCode), 0, KeyEvent.META_CTRL_ON));
    }
	private void altDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(100, 100, KeyEvent.ACTION_DOWN, handleAZ(keyEventCode), 0, KeyEvent.META_ALT_ON));
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(100, 100, KeyEvent.ACTION_UP, handleAZ(keyEventCode), 0, KeyEvent.META_ALT_ON));
    }
	private void ctrlAlt(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(100, 100, KeyEvent.ACTION_DOWN, handleAZ(keyEventCode), 0, KeyEvent.META_CTRL_ON | KeyEvent.META_ALT_ON));
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(100, 100, KeyEvent.ACTION_UP, handleAZ(keyEventCode), 0, KeyEvent.META_CTRL_ON | KeyEvent.META_ALT_ON));
    }
	private void shiftCtrlDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(100, 100, KeyEvent.ACTION_DOWN, handleAZ(keyEventCode), 0, KeyEvent.META_SHIFT_ON | KeyEvent.META_CTRL_ON));
		getCurrentInputConnection().sendKeyEvent(new KeyEvent(100, 100, KeyEvent.ACTION_UP, handleAZ(keyEventCode), 0, KeyEvent.META_SHIFT_ON | KeyEvent.META_CTRL_ON));
    }
	private void shiftAltDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(100, 100, KeyEvent.ACTION_DOWN, handleAZ(keyEventCode), 0, KeyEvent.META_SHIFT_ON | KeyEvent.META_ALT_ON));
		getCurrentInputConnection().sendKeyEvent(new KeyEvent(100, 100, KeyEvent.ACTION_UP, handleAZ(keyEventCode), 0, KeyEvent.META_SHIFT_ON | KeyEvent.META_ALT_ON));
    }
	private void shiftCtrlAltDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(100, 100, KeyEvent.ACTION_DOWN, handleAZ(keyEventCode), 0, KeyEvent.META_SHIFT_ON | KeyEvent.META_CTRL_ON | KeyEvent.META_ALT_ON));
		getCurrentInputConnection().sendKeyEvent(new KeyEvent(100, 100, KeyEvent.ACTION_UP, handleAZ(keyEventCode), 0, KeyEvent.META_SHIFT_ON | KeyEvent.META_CTRL_ON | KeyEvent.META_ALT_ON));
    }
	// menambahkan fungsi Arah untuk menandai text
	private void Arrows(int keyEventCode) {
		if (Variables.isCtrl() && mInputView.isShifted()) {
			shiftCtrlDownUp(keyEventCode);}
			else {
			   if (Variables.isCtrl()){
			      shiftDownUp(keyEventCode);
		       }else{
			      keyDownUp(keyEventCode);
		             }
		         }
		
    }
    // Implementation of KeyboardViewListener

// onKey switch mode
	// saya merubah metode dasar onKey if/Else jadi switch mode
	// untuk memudahkan mengatur tombol
	@Override
	public void onKey(int primaryCode, int[] keyCodes) {
		playClick(primaryCode);		
		switch(primaryCode){
			case Keyboard.KEYCODE_DELETE : // code -5
				handleBackspace();
				break;
			case -254: //left
				Arrows(KeyEvent.KEYCODE_DPAD_LEFT);
				break;
			case -256: //right
				Arrows(KeyEvent.KEYCODE_DPAD_RIGHT);
				break;
			case LatinKeyboard.KEYCODE_LANGUAGE_SWITCH: // -101 / Function
                if (Variables.isCtrl()) {
                    Variables.setCtrlOff();
                } else {
                    Variables.setCtrlOn();
                }
				//voidKeyLabelOnCtrlOn();		
				//mInputView.draw(new Canvas());
						
				break;
			case 32:
				keyDownUp(KeyEvent.KEYCODE_SPACE);
				break;
			case 46:
				if (Variables.isCtrl()) {
				Variables.setCtrlOff();
				
				setInputView(onCreateInputView());}
				else {
				keyDownUp(KeyEvent.KEYCODE_PERIOD);}
				break;
			case Keyboard.KEYCODE_ALT: //code -6
				break;
			case Keyboard.KEYCODE_MODE_CHANGE: //code -2
				Keyboard current = mInputView.getKeyboard();
				if (current == mSymbolsKeyboard || current == mSymbolsShiftedKeyboard) {
					setLatinKeyboard(mQwertyKeyboard);
					//Variables.setCtrlOff();
					//mInputView.draw(new Canvas());
				} else {
					setLatinKeyboard(mSymbolsKeyboard);
					mSymbolsKeyboard.setShifted(false);
					//Variables.setCtrlOff();
					//mInputView.draw(new Canvas());
				}
				//mInputView.draw(new Canvas());
				break;
			case Keyboard.KEYCODE_CANCEL: //code -3
				break;
			case Keyboard.KEYCODE_SHIFT: //code -1
				handleShift();
				break;
			case Keyboard.KEYCODE_DONE: //code -4
				keyDownUp(KeyEvent.KEYCODE_ENTER);
				break;
			default:			
				//voidKeyLabelOnCtrlOn();
				//mInputView.draw(new Canvas());
				if (Variables.isCtrl()) {
					ctrlDownUp(primaryCode);
				} else {
				    handleCharacter(primaryCode);
                       }
		}
	}
//berfungsi menghandle keyCode A-Z saat tombol Function diaktifkan
	private int handleAZ(int keycode) {
        char code = (char) keycode;
        switch (String.valueOf(code)) {
            case "a":
                return KeyEvent.KEYCODE_A;
            case "b":
                return KeyEvent.KEYCODE_B;
            case "c":
                return KeyEvent.KEYCODE_C;
            case "d":
                return KeyEvent.KEYCODE_D;
            case "e":
                return KeyEvent.KEYCODE_E;
            case "f":
                return KeyEvent.KEYCODE_F;
            case "g":
                return KeyEvent.KEYCODE_G;
            case "h":
                return KeyEvent.KEYCODE_H;
            case "i":
                return KeyEvent.KEYCODE_I;
            case "j":
                return KeyEvent.KEYCODE_J;
            case "k":
                return KeyEvent.KEYCODE_K;
            case "l":
                return KeyEvent.KEYCODE_L;
            case "m":
                return KeyEvent.KEYCODE_M;
            case "n":
                return KeyEvent.KEYCODE_N;
            case "o":
                return KeyEvent.KEYCODE_O;
            case "p":
                return KeyEvent.KEYCODE_P;
            case "q":
                return KeyEvent.KEYCODE_Q;
            case "r":
                return KeyEvent.KEYCODE_R;
            case "s":
                return KeyEvent.KEYCODE_S;
            case "t":
                return KeyEvent.KEYCODE_T;
            case "u":
                return KeyEvent.KEYCODE_U;
            case "v":
                return KeyEvent.KEYCODE_V;
            case "w":
                return KeyEvent.KEYCODE_W;
            case "x":
                return KeyEvent.KEYCODE_X;
            case "y":
                return KeyEvent.KEYCODE_Y;
            case "z":
                return KeyEvent.KEYCODE_Z;
            default:
                return keycode;
        }}
	private void playClick(int keyCode){
		AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
		switch(keyCode){
			case 32: 
				am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
				break;
			case Keyboard.KEYCODE_DONE:
			case 10: 
				am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
				break;	
			case Keyboard.KEYCODE_SHIFT:
			case Keyboard.KEYCODE_MODE_CHANGE:
			case LatinKeyboard.KEYCODE_LANGUAGE_SWITCH:				
			case Keyboard.KEYCODE_DELETE:
				am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
				break;		
			case -252:
			case -254:
			case -256:
			case -258:
				am.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_UP);
				break;
			default: am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
		}		
	}

    public void onText(CharSequence text) {
    }
    
    private void handleBackspace() {
		if (Variables.isCtrl()) {
			keyDownUp(KeyEvent.KEYCODE_FORWARD_DEL);
		} 
		else {		
        keyDownUp(KeyEvent.KEYCODE_DEL);
        updateShiftKeyState(getCurrentInputEditorInfo());
		}
    }

    private void handleShift() {
        if (mInputView == null) {
            return;
        }
        
        Keyboard currentKeyboard = mInputView.getKeyboard();
        if (mQwertyKeyboard == currentKeyboard) {
            // Alphabet keyboard
            checkToggleCapsLock();
            mInputView.setShifted(mCapsLock || !mInputView.isShifted());
        } else if (currentKeyboard == mSymbolsKeyboard) {
            mSymbolsKeyboard.setShifted(true);
            setLatinKeyboard(mSymbolsShiftedKeyboard);
            mSymbolsShiftedKeyboard.setShifted(true);
        } else if (currentKeyboard == mSymbolsShiftedKeyboard) {
            mSymbolsShiftedKeyboard.setShifted(false);
            setLatinKeyboard(mSymbolsKeyboard);
            mSymbolsKeyboard.setShifted(false);
        }
    }
    
    private void handleCharacter(int primaryCode) {
        if (isInputViewShown()) {
            if (mInputView.isShifted()) {
                primaryCode = Character.toUpperCase(primaryCode);
            }
        }
        getCurrentInputConnection().commitText(String.valueOf((char) primaryCode), 1);
        updateShiftKeyState(getCurrentInputEditorInfo());
    }
    private IBinder getToken() {
        final Dialog dialog = getWindow();
        if (dialog == null) {
            return null;
        }
        final Window window = dialog.getWindow();
        if (window == null) {
            return null;
        }
        return window.getAttributes().token;
    }

    private void handleLanguageSwitch() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            mInputMethodManager.switchToNextInputMethod(getToken(), false /* onlyCurrentIme */);
        }
    }

    private void checkToggleCapsLock() {
        long now = System.currentTimeMillis();
        if (mLastShiftTime + 200 > now) {
            mCapsLock = !mCapsLock;
            mLastShiftTime = 0;
        } else {
            mLastShiftTime = now;
        }
    }
    
    public void swipeRight() {
    }
    
    public void swipeLeft() {
    }

    public void swipeDown() {
    }

    public void swipeUp() {
    }
    
    public void onPress(int primaryCode) {
    }
    
    public void onRelease(int primaryCode) {
    }
	private void setKeyLabelOnCtrlOn(String setOldLabel, String setNewLabel)
	{
		int i;
		List<Keyboard.Key> keys = mCurKeyboard.getKeys();
		for (i = 0; i < keys.size(); i++){
			if (Variables.isCtrl()){
				if (keys.get(i).label.equals(setOldLabel))
				{keys.get(i).label = setNewLabel;}}
			else{
				if(keys.get(i).label.equals(setNewLabel)){
					keys.get(i).label = setOldLabel;}
			}}
		mInputView.invalidateKey(i);}

	private void voidKeyLabelOnCtrlOn(){
//voidSetKeyFN();
		//rfLabel();
		//setKeyLabelOnCtrlOn(".","×");
		//setKeyLabelOnCtrlOn("Del","f.del");
		setKeyLabelOnCtrlOn("Fn","CTRL");
	}
	
	
}
