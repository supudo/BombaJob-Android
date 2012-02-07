package net.supudo.apps.aBombaJob.Misc;

import net.supudo.apps.aBombaJob.R;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;

public class BombaJobPreferences extends PreferenceActivity {
	
	CheckBoxPreference chkPrivateData;

	@Override
    public void onCreate(Bundle savedInstanceState) {        
        super.onCreate(savedInstanceState);        
        addPreferencesFromResource(R.layout.bombajob_preferences);
        chkPrivateData = (CheckBoxPreference)getPreferenceScreen().findPreference("StorePrivateData");
    }

}
