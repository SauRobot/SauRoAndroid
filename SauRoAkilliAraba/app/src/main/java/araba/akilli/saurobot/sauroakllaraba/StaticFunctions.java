package araba.akilli.saurobot.sauroakllaraba;

import android.app.Activity;
import android.view.WindowManager;

/**
 * Created by Abdullah on 09.03.2016.
 */
public class StaticFunctions {
    static void onCreateSetups(Activity activityReference){
        // ekran kararmasını engeller.
        activityReference.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
