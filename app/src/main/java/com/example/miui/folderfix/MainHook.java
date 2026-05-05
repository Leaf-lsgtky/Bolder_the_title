package com.example.miui.folderfix;

import android.content.res.Resources;
import android.content.res.TypedArray;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class MainHook implements IXposedHookLoadPackage {
    private static final String TARGET_PACKAGE = "com.miui.home";
    private static final String FOLDER_TITLE_STYLE = "FolderTitle";
    private static final String FONT_ANDROID_FROM = "sans-serif-light";
    private static final String FONT_ANDROID_TO = "sans-serif-bold";
    private static final String FONT_MIUI_FROM = "mipro-medium";
    private static final String FONT_MIUI_TO = "mipro-bold";

    private int folderTitleStyleId = 0;
    private boolean initLogged = false;

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(TARGET_PACKAGE)) return;

        XposedHelpers.findAndHookMethod("android.app.Instrumentation", lpparam.classLoader, "callApplicationOnCreate", "android.app.Application", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (folderTitleStyleId != 0) return;

                android.app.Application app = (android.app.Application) param.args[0];
                Resources res = app.getResources();

                folderTitleStyleId = res.getIdentifier(FOLDER_TITLE_STYLE, "style", TARGET_PACKAGE);
                if (!initLogged) {
                    initLogged = true;
                    XposedBridge.log("MIUI Folder Title Fix: StyleID=" + Integer.toHexString(folderTitleStyleId));
                }
            }
        });

        XposedHelpers.findAndHookMethod(TypedArray.class, "getString", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (folderTitleStyleId == 0) return;

                TypedArray ta = (TypedArray) param.thisObject;
                int index = (int) param.args[0];

                try {
                    int sourceId = ta.getSourceResourceId(index, 0);
                    if (sourceId != folderTitleStyleId) return;

                    Object result = param.getResult();
                    if (!(result instanceof String)) return;

                    String value = (String) result;
                    if (FONT_ANDROID_FROM.equals(value)) {
                        param.setResult(FONT_ANDROID_TO);
                    } else if (FONT_MIUI_FROM.equals(value)) {
                        param.setResult(FONT_MIUI_TO);
                    }
                } catch (Throwable t) {
                    XposedBridge.log("MIUI Folder Title Fix: getString hook failed: " + t);
                }
            }
        });
    }
}
