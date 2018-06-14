package pisada.fallDetector;
/**
 * classe principale che contiene i fragment holder e il navigation drawer.
 * 
 */
import java.util.ArrayList;
import java.util.List;

import pisada.database.SessionDataSource;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements FragmentCommunicator, android.text.TextWatcher{
	private List<NavDrawerItem> listItems;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private FallDetectorFragment fragment;
	private int currentUIIndex = 0;
	private SessionDataSource sessionData;
	private FragmentManager fm;
	private final int SESSION_DETAILS_ID = -1;
	public static boolean isPortrait = true, isForeground = true;
	private NavDrawListAdapter navAdapter;
	private int[] greenIcons;



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			// Restore value of members from saved state
			currentUIIndex = savedInstanceState.getInt("uindex");
		} 

		sessionData = new SessionDataSource(this); 


		setContentView(R.layout.activity_navigation_drawer);
		fixLandscape(true);
		

		fm = getSupportFragmentManager();
		
		fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {

			@Override
			public void onBackStackChanged() {
				fragment = (FallDetectorFragment)(fm.findFragmentById(R.id.content_frame));

				FallDetectorFragment f = (FallDetectorFragment)(fm.findFragmentById(R.id.content_frame));
				if (f instanceof CurrentSessionFragment) 
				{
					currentSessionFragmentLastIndex = f.getListPosition();
				}
				else if(f instanceof SessionDetailsFragment){
					sessionDetailsFragmentLastIndex = f.getListPosition();
				}
				else if(f instanceof SessionsListFragment){
					sessionsListFragmentLastIndex = f.getListPosition();
				}
				else if(f instanceof ArchiveFragment){
					archiveFragmentLastIndex = f.getListPosition();
				}
			}
		});
		if(savedInstanceState != null)
			fragment = (FallDetectorFragment)fm.getFragment(savedInstanceState, "fragmentState");


		String[] arr = (getResources().getStringArray(R.array.navigation_items));
		listItems = new ArrayList<NavDrawerItem>();
		listItems.add(new NavDrawerItem(arr[0], R.drawable.currentsession)); 
		listItems.add(new NavDrawerItem(arr[1], R.drawable.sessionlist));
		listItems.add(new NavDrawerItem(arr[2], R.drawable.archive));
		listItems.add(new NavDrawerItem(arr[3], R.drawable.settings));
		listItems.add(new NavDrawerItem(arr[4], R.drawable.info));
		greenIcons = new int[5];
		greenIcons[0]= (R.drawable.currentsessionpressed);
		greenIcons[1]= (R.drawable.sessionslistpressed);
		greenIcons[2]= (R.drawable.archivepressed);
		greenIcons[3]= (R.drawable.settingspressed);
		greenIcons[4]= (R.drawable.infopressed);
		setUpNavDrawer();

	}

	private void setUpNavDrawer(){
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// Set the adapter for the list view
		navAdapter = new NavDrawListAdapter(this,  listItems, greenIcons);
		mDrawerList.setAdapter(navAdapter);
		// Set the list's click listener
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		mTitle = mDrawerTitle = getTitle();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, android.R.drawable.ic_lock_idle_alarm, R.string.app_name) {

			//chiusura navigation drawer
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				getSupportActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // chiama onPrepareOptionsMenu()
				if(currentUIIndex > -1){
					mDrawerList.setItemChecked(currentUIIndex, true);
					navAdapter.selectItem(currentUIIndex);
				}

			}

			//apertura navigation drawer
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				getSupportActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); 
				if(currentUIIndex > -1){
					mDrawerList.setItemChecked(currentUIIndex, true);
					navAdapter.selectItem(currentUIIndex);
				}

			}
		};

		//setta drawertoggle come drawer listener
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		/*
		 * di default mettiamo la currentsessionactivity
		 */
		if(fragment == null){
			fixScrollIndexes();
			fragment = new CurrentSessionFragment();
			// Insert the fragment by replacing any existing fragment
			fm.beginTransaction()
			.replace(R.id.content_frame, (Fragment)fragment)
			.commit();
			
			}
	}


	/*
	 * metodi view che rimandano ai fragment ma vengono automaticamente chiamati qui
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {

		//Save the fragment's instance
		fm.putFragment(outState, "fragmentState", fragment);
		outState.putInt("uindex", currentUIIndex);
		super.onSaveInstanceState(outState);

	}



	public void addSession(View v)
	{
		fragment.addSession(v);
	}

	public void currentSessionDetails(View v)
	{
		fragment.currentSessionDetails(v);
	}


	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);
			((NavDrawListAdapter)parent.getAdapter()).selectItem(position);
		}
	}

	
	private void selectItem(int position) {
		/*
		 * chiamato quando viene selezionato un elemento dal navigation drawer
		 */

		currentUIIndex = position;

		fixScrollIndexes();

		fixLandscape(true);
		if(!isPortrait && currentUIIndex == 1){
			Intent toSessionDetails = new Intent(this, SessionDetailsFragment.class);
			ArrayList<SessionDataSource.Session> list = sessionData.notArchivedSessions();
			if(list.size()>0)
			{
				toSessionDetails.putExtra(Utility.SESSION_NAME_KEY, list.get(0).getName());
				this.switchFragment(toSessionDetails);
			}
		}

		/*svuoto back stack*/
		for(int j = 0; j < fm.getBackStackEntryCount(); ++j) {    
			fm.popBackStack();
		}
		switch(position)
		{
		case 0:
			fragment = new CurrentSessionFragment();
			break;
		case 1:
			fragment = new SessionsListFragment();
			break;
		case 2:
			fragment = new ArchiveFragment();
			break;
		case 3:
			Intent intent = new Intent(this, SettingsActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); //per far si che risvegli l'activity se sta già runnando e non richiami oncreate
			startActivity(intent);
			Intent intent2 = new Intent(MainActivity.this, CurrentSessionFragment.class);
			currentUIIndex = 0; //sto rimandando la schermata a currentsession
			switchFragment(intent2);

			break;
		case 4:
			fragment = new InfoFragment();
			break;
		default:

			break;
		}

		
		fm.beginTransaction()
		.replace(R.id.content_frame, (Fragment)fragment)
		.commit();
		


		// Insert the fragment by replacing any existing fragment

		// Highlight the selected item, update the title, and close the drawer
		mDrawerList.setItemChecked(position, true);
		navAdapter.selectItem(position);

		setTitle(listItems.get(position).getTitle());
		Handler handler=new Handler();
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				mDrawerLayout.closeDrawer(mDrawerList);
			}           
		}, 40);

	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}


	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content view
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		try{
			menu.findItem(R.id.rename_session).setVisible(!drawerOpen);
		}
		catch(NullPointerException e)
		{
			/*
			 * non faccio niente, significa che la view non c'è ancora
			 */
		}
		return super.onPrepareOptionsMenu(menu);
	}




	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		int id = item.getItemId();

		switch (id) {
		case R.id.rename_session:
		{

			if(currentUIIndex == 0){ 
				final EditText input = new EditText(this);;
				input.addTextChangedListener(this);
				new AlertDialog.Builder(this)
				.setTitle(getResources().getString(R.string.rename))
				.setMessage(getResources().getString(R.string.insertname))
				.setView(input)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String value = input.getText().toString(); 
						String tmp = fragment.getSessionName();

						if(!value.equals("") && !sessionData.existSession(value) && sessionData.existCurrentSession()){
							sessionData.renameSession(sessionData.currentSession(), value);
							setTitle(value);
							fragment.setSessionName(value);
						}
						else if(sessionData.existSession(value))
						{
							Toast.makeText(MainActivity.this, getResources().getString(R.string.samename), Toast.LENGTH_LONG).show();
							fragment.setSessionName(tmp);
							setTitle(fragment.getSessionName());
						}
						else if(value.equals("")){
							Toast.makeText(MainActivity.this, getResources().getString(R.string.emptyname), Toast.LENGTH_LONG).show();

						}
						else //cioè non esiste con valore nuovo ma non ci sono sessioni correnti: preparo per start
						{
							fragment.setSessionName(value);
							setTitle(value);
						}

					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Do nothing.
					}
				}).show();
				return true;
			}
			else if(currentUIIndex == this.SESSION_DETAILS_ID)//altrimenti passo il lavoro al fragment che sarebbe il sessionDetails
				return false;
		}
		return true;
		case R.id.action_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); //per far si che risvegli l'activity se sta già runnando e non richiami oncreate
			startActivity(intent);
			Intent intent2 = new Intent(this, CurrentSessionFragment.class);
			this.switchFragment(intent2);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}



	@Override
	public void onBackPressed()
	{
		if(currentUIIndex == 0){

			finish();
		}
		else if(currentUIIndex < 0)
		{

			currentUIIndex = 1;
			Intent toDaniel = new Intent(this, SessionsListFragment.class);
			this.switchFragment(toDaniel);
			
		}
		else
		{
			Intent toSamu = new Intent(this, CurrentSessionFragment.class);
			this.switchFragment(toSamu);
		}


		fixLandscape(false);
		if(!isPortrait && currentUIIndex == 1){
			Intent toSessionDetails = new Intent(this, SessionDetailsFragment.class);
			ArrayList<SessionDataSource.Session> list = sessionData.notArchivedSessions();
			if(list.size()>0)
			{
				toSessionDetails.putExtra(Utility.SESSION_NAME_KEY, list.get(0).getName());
				this.switchFragment(toSessionDetails);
			}
		}
		invalidateOptionsMenu();

	}


	public static int currentSessionFragmentLastIndex ,infoFragmentLastIndex, sessionsListFragmentLastIndex , sessionDetailsFragmentLastIndex, archiveFragmentLastIndex  ;

	@Override
	public void switchFragment(Intent i) {

		if(!isForeground) return ;

		fixScrollIndexes();
		
		if (i.getComponent().getClassName().contains("CurrentSessionFragment")){

			currentUIIndex = 0;
			/*svuoto back stack*/
			for(int j = 0; j < fm.getBackStackEntryCount(); ++j) {    
				fm.popBackStack();
			}
			fragment = new CurrentSessionFragment();
			fm.beginTransaction()
			.replace(R.id.content_frame, (Fragment)fragment)
			.commit();

		}
		else if (i.getComponent().getClassName().contains("SessionsListFragment")){
			currentUIIndex = 1;
			/*svuoto back stack*/
			for(int j = 0; j < fm.getBackStackEntryCount(); ++j) {    
				fm.popBackStack();
			}
			fragment = new SessionsListFragment();
			fm.beginTransaction()
			.replace(R.id.content_frame, (Fragment)fragment)
			.commit();

			/*
			 * richiamare metodo switchfragment per popolare  sessiondetailsfragment
			 */

			if(!isPortrait && currentUIIndex == 1){
				
				Intent toSessionDetails = new Intent(this, SessionDetailsFragment.class);
				ArrayList<SessionDataSource.Session> list = sessionData.notArchivedSessions();
				if(list.size()>0)
				{
					toSessionDetails.putExtra(Utility.SESSION_NAME_KEY, list.get(0).getName());
					this.switchFragment(toSessionDetails);
				}
			}

		}
		else if (i.getComponent().getClassName().contains("SessionDetailsFragment")){

			if(!isPortrait){
				if(currentUIIndex != 1){

					//manda a sessionlist 
					currentUIIndex = 1;
					fixLandscape(true);
					/*svuoto back stack*/
					for(int j = 0; j < fm.getBackStackEntryCount(); ++j) {    
						fm.popBackStack();
					}
					fragment = new SessionsListFragment();
					fm.beginTransaction()
					.replace(R.id.content_frame, (Fragment)fragment)
					.commit();
				}
				// in questo caso non aggiorniamo currentUIIndex perché teniamo come principale quello del fragment di SX
				unselectAllLines();
				Fragment fragment2 = new SessionDetailsFragment();
				Bundle args = new Bundle();
				SessionDetailsFragment.sessionName = i.getStringExtra(Utility.SESSION_NAME_KEY);
				fragment2.setArguments(args);
				fm.beginTransaction()
				.replace(R.id.content_right, (Fragment)fragment2)//.addToBackStack(null)
				.commit();
			}
			else{
				currentUIIndex = this.SESSION_DETAILS_ID;// non appare nel nav draw
				unselectAllLines();
				fragment = new SessionDetailsFragment();
				Bundle args = new Bundle();
				SessionDetailsFragment.sessionName = i.getStringExtra(Utility.SESSION_NAME_KEY);
				fragment.setArguments(args);
				fm.beginTransaction().remove(fragment)
				.replace(R.id.content_frame, (Fragment)fragment)//.addToBackStack(null)
				.commit();
			}
		}
		else if (i.getComponent().getClassName().contains("FallDetailsDialogFragment")){

			/*
			 * qui ci mettiamo il custom dialog
			 */
			FallDetailsDialogFragment dialog = new FallDetailsDialogFragment(i.getStringExtra(Utility.SESSION_NAME_KEY), i.getLongExtra(Utility.FALL_TIME_KEY, -1));

			dialog.show(fm, "");
		}
		else if(i.getComponent().getClassName().contains("ArchiveFragment")){
			currentUIIndex = 2;
			/*svuoto back stack*/
			for(int j = 0; j < fm.getBackStackEntryCount(); ++j) {    
				fm.popBackStack();
			}
			fragment = new ArchiveFragment();
			fm.beginTransaction()
			.replace(R.id.content_frame, (Fragment)fragment)
			.commit();

		}
		else if(i.getComponent().getClassName().contains("InfoFragment")){
			currentUIIndex = 4;
			/*svuoto back stack*/
			for(int j = 0; j < fm.getBackStackEntryCount(); ++j) {    
				fm.popBackStack();
			}
			fragment = new InfoFragment();
			fm.beginTransaction()
			.replace(R.id.content_frame, (Fragment)fragment)
			.commit();
		}
		if(currentUIIndex > -1){
			mDrawerList.setItemChecked(currentUIIndex, true);
			navAdapter.selectItem(currentUIIndex);

		}
		invalidateOptionsMenu();

		fixLandscape(true);
		if(currentUIIndex >= 0)
			setTitle(listItems.get(currentUIIndex).getTitle());

	}

	private void fixLandscape(boolean makeVisible)
	{
		View parent = this.findViewById(android.R.id.content);
		View right = null;
		try{
			right = (FrameLayout)(parent.findViewById(R.id.content_right));
		}
		catch(NullPointerException e){
			right = null;
		}
		if(right == null)
			isPortrait = true;
		else
			isPortrait = false;

		if(!isPortrait)
		{
			if(currentUIIndex != 1){
				right.setVisibility(View.GONE);
			}
			else if(makeVisible)
				right.setVisibility(View.VISIBLE);	
		}
	}

	private void unselectAllLines()
	{
		for(int i = 0; i < listItems.size(); i++){
			mDrawerList.setItemChecked(i, false);
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		//non usato

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		//non usato

	}

	@Override
	public void afterTextChanged(Editable s) {
		String text = s.toString();
		int length = text.length();

		if(!text.matches("[0-9a-zA-Z]+") && length > 0) {
			s.delete(length - 1, length);
			Toast.makeText(this, getResources().getString(R.string.notavalidchar), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();
		isForeground = false;
	}

	@Override
	public void onResume()
	{
		super.onResume();
		if(currentUIIndex<0){
			setTitle("");
		}
		else{
			setTitle(listItems.get(currentUIIndex).getTitle());
		}
		isForeground = true;

		if(fm != null){
			try{
				fixScrollIndexes();
				Fragment frg = null;
				frg = fm.findFragmentById(R.id.content_frame);
				final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.detach(frg);
				ft.attach(frg);
				ft.commit();
				
			}
			catch(IllegalStateException ex){
				//se onsavedinstancestate è già stato chiamato, non deve essere effettuato alcun cambio fragment
				//questo avviene se l'activity riceve una rotazione prima che il fragment sia stato attaccato
				//in questo caso non vogliamo effettuare "refresh" del fragment in quanto verrà già caricato
				//con il giusto layout
			}
		}


		this.fragment = (FallDetectorFragment)(fm.findFragmentById(R.id.content_frame));
		
		if(!isPortrait && currentUIIndex == 1){
			
			Intent toSessionDetails = new Intent(this, SessionDetailsFragment.class);
			ArrayList<SessionDataSource.Session> list = sessionData.notArchivedSessions();
			if(list.size()>0)
			{
				toSessionDetails.putExtra(Utility.SESSION_NAME_KEY, list.get(0).getName());
				this.switchFragment(toSessionDetails);
			}
		}
	
	}
	
	private void fixScrollIndexes(){
		FallDetectorFragment f = (FallDetectorFragment)(fm.findFragmentById(R.id.content_frame));
		if (f instanceof CurrentSessionFragment) 
		{
			currentSessionFragmentLastIndex = f.getListPosition();
		}
		else if(f instanceof SessionDetailsFragment){
			sessionDetailsFragmentLastIndex = f.getListPosition();
		}
		else if(f instanceof SessionsListFragment){
			sessionsListFragmentLastIndex = f.getListPosition();
		}
		else if(f instanceof ArchiveFragment){
			archiveFragmentLastIndex = f.getListPosition();
			
		}
	}


}
