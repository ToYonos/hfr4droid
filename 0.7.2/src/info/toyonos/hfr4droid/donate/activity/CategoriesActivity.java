package info.toyonos.hfr4droid.donate.activity;

import info.toyonos.hfr4droid.common.HFR4droidApplication;
import info.toyonos.hfr4droid.donate.R;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CategoriesActivity extends	info.toyonos.hfr4droid.common.activity.CategoriesActivity
{
	private int[] sequence = new int[] {0, 0, 0, 0 ,0 ,0};
	private AlertDialog promptDialog;
	private int menuCount = 0;
	private long lastMenuDate = 0;
	
	/** Les commandes */
	private enum Command
	{
		EXIT("exit"),
		LOGIN("login"),
		LOGOUT("logout"),
		SU("su"),
		PASSWD("passwd"),
		MAN("man"),
		TOP("top"),
		DATE("date"),
		DF("df"),
		APROPOS("apropos"),
		UNKNOWN("unknown");

		private final String key;

		private Command(String key)
		{
			this.key = key;
		}

		public String getKey()
		{
			return this.key;
		}
		
		public static Command fromString(String key) 
		{
			for (Command command : Command.values())
			{
				if (command.getKey().equals(key)) return command;
			}
			return UNKNOWN;
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.prompt, null);
		final EditText prompt = (EditText) layout.findViewById(R.id.PromptCommand);
		prompt.setOnKeyListener(new OnKeyListener()
		{
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)
				{
					processCommand(prompt);
					return true;
				}
				return false;
			}
		});
		
		promptDialog = new AlertDialog.Builder(CategoriesActivity.this)
		.setView(layout)
		.setOnCancelListener(new OnCancelListener()
		{
			public void onCancel(DialogInterface dialog)
			{
				hideKeyboard(prompt);
			}
		})
		.create();
	}

	private void hideKeyboard(EditText prompt)
	{
		InputMethodManager imm = (InputMethodManager) CategoriesActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(prompt.getWindowToken(), 0);
		prompt.setText("");
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		// Looking for the "prompt" sequence
		if (sequence[0] == 0 && keyCode == 44) sequence[0] = 44;
		else if (sequence[0] != 0 && sequence[1] == 0 && keyCode == 46) sequence[1] = 46;
		else if (sequence[0] != 0 && sequence[1] != 0 && sequence[2] == 0 && keyCode == 43) sequence[2] = 43;
		else if (sequence[0] != 0 && sequence[1] != 0 && sequence[2] != 0 && sequence[3] == 0 && keyCode == 41) sequence[3] = 41;
		else if (sequence[0] != 0 && sequence[1] != 0 && sequence[2] != 0 && sequence[3] != 0 && sequence[4] == 0 && keyCode == 44) sequence[4] = 44;
		else if (sequence[0] != 0 && sequence[1] != 0 && sequence[2] != 0 && sequence[3] != 0 && sequence[4] != 0 && sequence[5] == 0 && keyCode == 48)
		{
			promptDialog.show();
			TextView prefix = (TextView) promptDialog.findViewById(R.id.PromptCommandLeft);
			prefix.setText((isLoggedIn() ? getHFR4droidApplication().getAuthentication().getUser() : "anonymous") + "@HFR4droid:/$ ");
			
			Timer timer = new Timer();
			timer.schedule(new TimerTask()
			{
				public void run()
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							((EditText) promptDialog.findViewById(R.id.PromptCommand)).requestFocus();
							hideKeyboard((EditText) promptDialog.findViewById(R.id.PromptCommand));
						}
					});
				}
			}, 250);
		}
		else
		{
			sequence = new int[] {0, 0, 0, 0 ,0 ,0};
		}

		return super.onKeyDown(keyCode, event);
	}

	private void processCommand(EditText prompt)
	{
		Intent intent;
		DecimalFormat df = new DecimalFormat("#.##");
		switch (Command.fromString(prompt.getText().toString().toLowerCase().trim()))
		{
			case EXIT:
				hideKeyboard(prompt);
				promptDialog.dismiss();
				finish();
				break;
				
			case LOGIN:
				hideKeyboard(prompt);
				promptDialog.dismiss();
				if (!isLoggedIn()) showLoginDialog();
				break;
				
			case LOGOUT:
				hideKeyboard(prompt);
				promptDialog.dismiss();
				if (isLoggedIn()) 
				{
					logout();
					stopMpTimerCheckService();
					onLogout();
				}
				break;

			case SU:
				hideKeyboard(prompt);
				Toast.makeText(CategoriesActivity.this, getString("yesbutno"), Toast.LENGTH_SHORT).show();
				break;
				
			case PASSWD:
				hideKeyboard(prompt);
				promptDialog.dismiss();
				intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.hardware.fr/user/passwordchange.php?config=hfr.inc"));
				startActivity(intent);
				break;
				
			case MAN:
				hideKeyboard(prompt);
				promptDialog.dismiss();
				intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://code.google.com/p/hfr4droid/wiki/Documentation"));
				startActivity(intent);
				break;
				
			case TOP:
				hideKeyboard(prompt);
				Toast.makeText(CategoriesActivity.this, getString(R.string.cpu, df.format(getHFR4droidApplication().getCPUUsage())), Toast.LENGTH_LONG).show();
				break;
				
			case DATE:
				hideKeyboard(prompt);
				SimpleDateFormat sdf = new SimpleDateFormat("EEEE d MMMM yyyy HH:mm:ss Z");
				Toast.makeText(CategoriesActivity.this, sdf.format(new Date()), Toast.LENGTH_LONG).show();
				break;
				
			case DF:
				hideKeyboard(prompt);
				File sdcard = Environment.getExternalStorageDirectory();
				StatFs stat = new StatFs(sdcard.getAbsolutePath());
				float available = stat.getAvailableBlocks() * (long) stat.getBlockSize();
				float total = stat.getBlockCount() * (long) stat.getBlockSize();
				float availablePercent = (float) available / total * 100;
				Toast.makeText(CategoriesActivity.this, getString(R.string.availablespace, df.format(available / 1024 / 1024), df.format(availablePercent)), Toast.LENGTH_LONG).show();
				break;
				
			case APROPOS:
				hideKeyboard(prompt);
				promptDialog.dismiss();
				infoDialog.show();
				break;
			
			case UNKNOWN:
				Toast.makeText(CategoriesActivity.this, getString(R.string.unknowncommand, prompt.getText().toString()), Toast.LENGTH_SHORT).show();
				hideKeyboard(prompt);
				break;
		}
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		long now = System.currentTimeMillis();
		long diff = now - lastMenuDate;
		if (lastMenuDate == 0 || diff < 2000)
		{
			menuCount++;
			Log.d(HFR4droidApplication.TAG, "count = " + menuCount + ", diff " + diff);
			lastMenuDate = now;
		}
		else
		{
			menuCount = 0;
			lastMenuDate = 0;
		}
		
		if (menuCount == 3)
		{
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
			menuCount = 0;
			lastMenuDate = 0;
			return false;
		}
		else
		{
			return super.onPrepareOptionsMenu(menu);
		}
	}
}
