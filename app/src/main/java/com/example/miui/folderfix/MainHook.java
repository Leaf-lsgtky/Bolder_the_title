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
    private int folderTitleStyleId = 0;
    private int customFontFamilyAttrId = 0;
    private final int androidFontFamilyAttrId = android.R.attr.fontFamily;

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(TARGET_PACKAGE)) return;

        XposedHelpers.findAndHookMethod("android.app.Instrumentation", lpparam.classLoader, "callApplicationOnCreate", "android.app.Application", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                android.app.Application app = (android.app.Application) param.args[0];
                Resources res = app.getResources();
                
                // 获取样式的 ID
                folderTitleStyleId = res.getIdentifier("FolderTitle", "style", TARGET_PACKAGE);
                // 获取自定义属性 fontFamily 的 ID (注意不是 android:fontFamily)
                customFontFamilyAttrId = res.getIdentifier("fontFamily", "attr", TARGET_PACKAGE);
                
                if (folderTitleStyleId != 0) {
                    XposedBridge.log("MIUI Folder Title Fix: Initialized with StyleID=" + Integer.toHexString(folderTitleStyleId) + 
                                     ", CustomAttrID=" + Integer.toHexString(customFontFamilyAttrId));
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
                    // 1. 确认该属性是否来自 FolderTitle 样式
                    int sourceId = ta.getSourceResourceId(index, 0);
                    if (sourceId != folderTitleStyleId) return;

                    // 2. 获取当前 index 对应的属性 ID
                    int attrId = ta.getAttributeId(index, 0);

                    // 3. 根据属性 ID 精准替换，不判断原始值
                    if (attrId == androidFontFamilyAttrId) {
                        // 针对 android:fontFamily
                        param.setResult("sans-serif-bold");
                    } else if (attrId != 0 && attrId == customFontFamilyAttrId) {
                        // 针对 fontFamily (MIUI 自定义字体属性)
                        param.setResult("mipro-bold");
                    }
                } catch (Throwable t) {
                    // 容错
                }
            }
        });
    }
}
