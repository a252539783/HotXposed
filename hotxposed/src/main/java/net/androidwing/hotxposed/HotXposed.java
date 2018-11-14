package net.androidwing.hotxposed;

import android.app.AndroidAppHelper;
import android.util.Log;
import dalvik.system.PathClassLoader;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.io.File;
import java.lang.reflect.Method;

/**
 * Created  on 2018/3/30.
 */
public class HotXposed {
  public static void hook(Class clazz, XC_LoadPackage.LoadPackageParam lpparam)
      throws Exception {
    String packageName = clazz.getName().replace("."+clazz.getSimpleName(),"");
    File apkFile = getApkFile(packageName);

    if (!apkFile.exists()) {
      Log.e("error", "apk file not found");
      return;
    }

    filterNotify(lpparam);

    PathClassLoader classLoader =
        new PathClassLoader(apkFile.getAbsolutePath(), lpparam.getClass().getClassLoader());

    Class cls = classLoader.loadClass(clazz.getName());
    if (cls != null) {
      Method method = cls.getDeclaredMethod("dispatch", XC_LoadPackage.LoadPackageParam.class);
      method.setAccessible(true);
      method.invoke(cls.newInstance(), lpparam);
    }
  }

  private static void filterNotify(XC_LoadPackage.LoadPackageParam lpparam)
      throws ClassNotFoundException {
    if("de.robv.android.xposed.installer".equals(lpparam.packageName)){
      XposedHelpers.findAndHookMethod(lpparam.classLoader.loadClass("de.robv.android.xposed.installer.util.NotificationUtil"),
          "showModulesUpdatedNotification", new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
              param.setResult(new Object());
            }

            @Override protected void afterHookedMethod(MethodHookParam param) throws Throwable {
              super.afterHookedMethod(param);
            }
          });
    }
  }


  private static File getApkFile(String packageName) {
    String filePath = String.format("/data/app/%s-%s/base.apk", packageName, 1);
    File apkFile = new File(filePath);
    if (!apkFile.exists()) {
      filePath = String.format("/data/app/%s-%s/base.apk", packageName, 2);
      apkFile = new File(filePath);
    }
    return apkFile;
  }
}
