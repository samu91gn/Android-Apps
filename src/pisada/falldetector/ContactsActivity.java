package pisada.fallDetector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
/**
 * 
 * @author Pisada
 * Activity che gestisce i contatti cui mandare avvisi in caso di caduta,
 * permette di aggiungere contatti alla lista, rimuoverli, gestire che tipo di notifiche inviare ecc. 
 *
 */
public class ContactsActivity extends AppCompatActivity {


	private final String CONTACTS_KEY = "contacts";
	private ArrayList<String> contacts;
	private SharedPreferences sp;
	private ActionBar actionBar;
	private static final int CONTACT_PICKER_RESULT = 1021;
	private ArrayAdapter<String> adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts);
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		Set<String> numbers = sp.getStringSet(CONTACTS_KEY, null);
		contacts = numbers != null ? new ArrayList<String>(numbers) : new ArrayList<String>();
		actionBar = getSupportActionBar();
		ListView listView = (ListView) findViewById(R.id.listView1);
		adapter = new CustomAdapterContacts(this, R.layout.itemlistrow, contacts);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@SuppressLint("InflateParams")
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				//qui contacts non deve essere null
				String myCurrentContactString = contacts.get(position);
				final View textEntryView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.doubletextview, null); 
				final AlertDialog dialog = new AlertDialog.Builder(ContactsActivity.this)
				.setTitle(getResources().getString(R.string.insertNumber))
				.setMessage(getResources().getString(R.string.insertContactNumber))
				.setView(textEntryView)
				.setPositiveButton(getResources().getString(R.string.ok), null)
				.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// esci
					}
				}).create();

				final CheckBox checkBoxNumber = (CheckBox) textEntryView.findViewById(R.id.checkBoxNumber);
				final CheckBox checkBoxEmail = (CheckBox) textEntryView.findViewById(R.id.checkBoxEmail);
				final EditText inputName = (EditText) textEntryView.findViewById(R.id.contactName);
				final EditText inputNumber = (EditText) textEntryView.findViewById(R.id.contactNumber);
				final EditText inputEmail = (EditText) textEntryView.findViewById(R.id.contactEmail);

				Scanner scan = new Scanner(myCurrentContactString);
				inputName.setText(scan.nextLine());
				if(scan.hasNextLine())
					inputNumber.setText(scan.nextLine());
				else
					inputNumber.setText(getResources().getString(R.string.nonumber));
				if(scan.hasNextLine())
					inputEmail.setText(scan.nextLine());
				else					
					inputEmail.setText(getResources().getString(R.string.noemail));
				
				if(myCurrentContactString.contains("sendsm"))
					checkBoxNumber.setChecked(true);
				else
					checkBoxNumber.setChecked(false);
				if(myCurrentContactString.contains("sendem"))
					checkBoxEmail.setChecked(true);
				else
					checkBoxEmail.setChecked(false);
				scan.close();
				dialog.setOnShowListener(new DialogInterface.OnShowListener() {

					@Override
					public void onShow(DialogInterface d) {

						Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
						b.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View view) {
								String name = inputName.getText().toString();
								String number = inputNumber.getText().toString();
								String email = inputEmail.getText().toString();
								if(number.equals(""))
									number = getResources().getString(R.string.nonumber);
								if(email.equals(""))
									email = getResources().getString(R.string.noemail);
								
								if(name.equals("") || (number.equals(getResources().getString(R.string.nonumber)) && email.equals(getResources().getString(R.string.noemail))) || (checkBoxEmail.isChecked() && email.equals(getResources().getString(R.string.noemail))) || (checkBoxNumber.isChecked() && number.equals(getResources().getString(R.string.nonumber))))
									Toast.makeText(ContactsActivity.this, getResources().getString(R.string.complainInsertionContact), Toast.LENGTH_SHORT).show();
								else{
									String contact = name + "\n" + number + "\n" + email;
									contacts.remove(position);
									/*
									 * invio di email piuttosto che di sms gestito tramite parse della stringa -contatto- 
									 * le info relative al tipo di notifica vengono semplicemente aggiunte a seguito
									 * (per evitare di complicare il database)
									 */

									if(checkBoxEmail.isChecked() || checkBoxNumber.isChecked())
										contact += "\n";
									if(checkBoxEmail.isChecked())
										contact += "sendem ";
									if(checkBoxNumber.isChecked())
										contact += "sendsm";

									contacts.add(contact);


									Set<String> set = new HashSet<String>();
									set.addAll(contacts);
									sp.edit().putStringSet(CONTACTS_KEY, set).commit();
									adapter.notifyDataSetChanged();
									dialog.dismiss();
								}
							}
						});
					}
				});

				dialog.show();

			}
		});
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(final AdapterView<?> parent, View arg1,
					final int position, long id) {
				new AlertDialog.Builder(ContactsActivity.this)
				.setTitle(getResources().getString(R.string.deletecontact))
				.setMessage(getResources().getString(R.string.sure))
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						final String item = (String) parent.getItemAtPosition(position);
						contacts.remove(item);
						adapter.notifyDataSetChanged();
						Set<String> set = new HashSet<String>();
						set.addAll(contacts);
						sp.edit().putStringSet(CONTACTS_KEY, set).commit();

					}
				})
				.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				}).show();

				return true;
			}
		}
				);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
	}




	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.contacts, menu);
		return super.onCreateOptionsMenu(menu);
	}

	//motivo suppresslint: la textview del dialog non ha parent al momento della creazione: va messa nel dialog
	@SuppressLint("InflateParams")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case android.R.id.home:
			
			Intent toSettings = new Intent(this, SettingsActivity.class);
			toSettings.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); //per far si che risvegli l'activity se sta già runnando e non richiami oncreate
			startActivity(toSettings);
			return true;
		case R.id.action_addcontact:
			/*
			 * apri contacts picker
			 */
			Intent pickContactIntent = new Intent( Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
			startActivityForResult(pickContactIntent, CONTACT_PICKER_RESULT);	

			return true;
		case R.id.action_addnumber:
			/*
			 *  apri dialog [nome-numero] e salva numero nella lista poi salva la lista in sp
			 */
			final View textEntryView = LayoutInflater.from(this).inflate(R.layout.doubletextview, null); 
			final AlertDialog dialog = new AlertDialog.Builder(ContactsActivity.this)
			.setTitle(getResources().getString(R.string.insertNumber))
			.setMessage(getResources().getString(R.string.insertContactNumber))
			.setView(textEntryView)
			.setPositiveButton(getResources().getString(R.string.ok), null)
			.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// esci
				}
			}).create();

			final CheckBox checkBoxNumber = (CheckBox) textEntryView.findViewById(R.id.checkBoxNumber);
			final CheckBox checkBoxEmail = (CheckBox) textEntryView.findViewById(R.id.checkBoxEmail);
			final EditText inputName = (EditText) textEntryView.findViewById(R.id.contactName);
			final EditText inputNumber = (EditText) textEntryView.findViewById(R.id.contactNumber);
			final EditText inputEmail = (EditText) textEntryView.findViewById(R.id.contactEmail);
			inputNumber.setInputType(InputType.TYPE_CLASS_PHONE);

			
			//questo ha lo scopo di settare i tasti dopo aver inizializzato le textview:
			//se le textview non vengono inizializzate dopo averle già messe nel dialog non funziona
			//quindi imposto il comportamento dei pulsanti nell'onshowlistener
			dialog.setOnShowListener(new DialogInterface.OnShowListener() {

				@Override
				public void onShow(DialogInterface d) {

					Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
					b.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View view) {
							String name = inputName.getText().toString();
							String number = inputNumber.getText().toString();
							String email = inputEmail.getText().toString();
							if(number.equals(""))
								number = getResources().getString(R.string.nonumber);
							if(email.equals(""))
								email = getResources().getString(R.string.noemail);
							
							if(name.equals("") || (number.equals(getResources().getString(R.string.nonumber)) && email.equals(getResources().getString(R.string.noemail))) || (checkBoxEmail.isChecked() && email.equals(getResources().getString(R.string.noemail))) || (checkBoxNumber.isChecked() && number.equals(getResources().getString(R.string.nonumber))))
								Toast.makeText(ContactsActivity.this, getResources().getString(R.string.complainInsertionContact), Toast.LENGTH_SHORT).show();
							else{
								String contact = name + "\n" + number + "\n" + email;
								if(!contacts.contains(contact))
								{
									if(checkBoxEmail.isChecked() || checkBoxNumber.isChecked())
										contact += "\n";
									if(checkBoxEmail.isChecked())
										contact += "sendem ";
									if(checkBoxNumber.isChecked())
										contact += "sendsm";

									contacts.add(contact);

								}
								Set<String> set = new HashSet<String>();
								set.addAll(contacts);
								sp.edit().putStringSet(CONTACTS_KEY, set).commit();
								dialog.dismiss();
							}
						}
					});
				}
			});

			dialog.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override  
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case CONTACT_PICKER_RESULT:
				String phoneNo = null ;
				String name = null;
				String email = null;
				Uri uri = data.getData();
				
				
				Cursor cursor = getContentResolver().query(uri, null, null, null, null);
				cursor.moveToFirst();
				int  phoneIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
				int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
				int emailIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
				
				if(phoneIndex != -1)
					phoneNo = cursor.getString(phoneIndex);
				else 
					phoneNo = getResources().getString(R.string.nonumber);
				if(emailIndex != -1 && emailIndex != phoneIndex)
					email = cursor.getString(emailIndex);
				else 
					email = getResources().getString(R.string.noemail);
				name = cursor.getString(nameIndex);
			//	if emailindex è uguale a phoneindex allora metti la email come noemail
				
				String contact = ""+name+"\n"+phoneNo+"\n"+email;
				if(!contacts.contains(contact)){
					if(!(phoneNo.equals(getResources().getString(R.string.nonumber)) && email.equals(getResources().getString(R.string.noemail))))
						contact += "\n";
					if(!phoneNo.equals(getResources().getString(R.string.nonumber)))
						contact += "sendsm";
					if(!email.equals(getResources().getString(R.string.noemail)))
						contact += "sendem";
					contacts.add(contact);
				}
				adapter.notifyDataSetChanged();
				Set<String> set = new HashSet<String>();
				set.addAll(contacts);
				sp.edit().putStringSet(CONTACTS_KEY, set).commit();
			}
		}
	}


}
