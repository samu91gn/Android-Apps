package pisada.fallDetector;
/**
 * activity-dialog per stoppare l'invio di notifiche se non si desidera avvisare i contatti (in caso non ce ne sia bisogno)
 */
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;

import pisada.database.FallDataSource;
import pisada.fallDetector.smSender.Sender;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;

public class StoBeneActivity extends Activity {

	private int mProgressStatus = 0;
	private Handler mHandler;
	private ProgressBar pb;
	private Button cancel;
	private boolean cancelClicked = false;
	private final static String CONTACTS_KEY = "contacts";
	private static SharedPreferences sp;
	private FallDataSource fds;

	private String position;
	private FallDataSource.Fall fall;
	private boolean stopped = false; //variabile di istanza per fermare
									 //l'istanza del dialog dal mandare notifica nel
									 //caso avvenga rotazione dispositivo

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		 if (savedInstanceState != null) {
		        // Restore value of members from saved state
		        mProgressStatus = savedInstanceState.getInt("progress");
		    } 
		fds = new FallDataSource(this);
		setFinishOnTouchOutside(false);
		fall = fds.getFall(getIntent().getLongExtra("time", 0), getIntent().getStringExtra("sessionName"));
		position = getIntent().getStringExtra("position");
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_sto_bene);
		cancel = (Button) findViewById(R.id.cancelButton);
		cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				cancelClicked = true;
				finish();
			}
		});
		pb = (ProgressBar) findViewById(R.id.progressBarHoriz);
		mHandler = new Handler();

		new Thread(new Runnable() {
			public void run() {
				while (mProgressStatus < 100) {
					try {
						Thread.sleep(100); //diamogli 10 secondi di tempo
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					mProgressStatus += 1;
					
					
					// Update the progress bar
					mHandler.post(new Runnable() {
						public void run() {
							pb.setProgress(mProgressStatus);
						}
					});
				}
				if(!cancelClicked)
				{
					manageFallOccured();
					finish();
				}
			}
		}).start();
	}

	

	private  void manageFallOccured()
	{
		//gestisce la caduta avvenuta: si occupa di avvisare via sms le persone nella lista

		Scanner scan;

		String message = getResources().getString(R.string.message);
		
		
		/*
		 * qui mandare sms ai contatti che contengono sendsm e email a quelli che contengono sendem
		 */
		
		message += position;
		if(fall.getLat() != -1 && fall.getLng() != -1)
		{
			message += "\n" + Utility.getMapsLink(fall.getLat(), fall.getLng());
		}
		Set<String> numbers = sp.getStringSet(CONTACTS_KEY, null);
		ArrayList<String> contacts = numbers != null ? new ArrayList<String>(numbers) : new ArrayList<String>();
		ArrayList<String> numbersList = new ArrayList<String>();
		ArrayList<String> emailList = new ArrayList<String>();
		for(int i = 0; i < contacts.size(); i++)
		{
			String fullContact = contacts.get(i);
			scan = new Scanner(fullContact); scan.nextLine();
			
			String number = scan.nextLine();
			String email = scan.nextLine();
			
			if(fullContact.contains("sendsm"))
				numbersList.add(number);
			if(fullContact.contains("sendem"))
				emailList.add(email);
		}
		
		if(!stopped){
		new Sender().sendSMSToList(numbersList, getApplicationContext(), message, fall);
		new Sender().sendEmailToList(emailList, this, message, fall);
		}
	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sto_bene, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed()
	{
		//non usciamo
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putInt("progress", mProgressStatus);
	    super.onSaveInstanceState(savedInstanceState);
	}
	
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		stopped = true;
	}
}