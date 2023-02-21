package id.thre.liteIME;

// thanks for Vlad make it so fun

import android.view.*;
import android.view.inputmethod.*;

public  class Variables
 {
    private static boolean IS_CTRL = false;
    private static boolean IS_ALT = false;
    private static boolean IS_SHIFT = false;
    private static boolean IS_FN = false;
    private static boolean IS_CAPS = false;
    private static boolean IS_THRE = false;
	
    public static boolean isCtrlAlt() {
        return IS_CTRL || IS_ALT;}
    public static boolean isCtrl() {
        return IS_CTRL;}
    public static boolean isAlt() {
        return IS_ALT;}
    public static boolean isShift() {
        return IS_SHIFT;}
    public static boolean isFn() {
        return IS_FN;}
	public static boolean isCaps() {
        return IS_CAPS;}
    public static void setIsCtrl(boolean on) {
        IS_CTRL = on;}
    public static void setIsAlt(boolean on) {
        IS_ALT = on;}
    public static void setIsFn(boolean on) {
        IS_FN = on;}
	public static void setIsCaps(boolean on) {
        IS_CAPS = on;}
    public static void setAltOn() {
        IS_ALT = true;}
    public static void setAltOff() {
        IS_ALT = false;}
    public static void setCtrlOn() {
        IS_CTRL = true;}
    public static void setCtrlOff() {
        IS_CTRL = false;}
    public static void setShiftOn() {
        IS_SHIFT = true;}
    public static void setShiftOff() {
        IS_SHIFT = false;}
    public static void setFNOn() {
        IS_FN = true;}
    public static void setFNOff() {
        IS_FN = false;}
	public static void setCapsOn() {
        IS_CAPS = true;}
    public static void setCapsOff() {
        IS_CAPS = false;}

}



