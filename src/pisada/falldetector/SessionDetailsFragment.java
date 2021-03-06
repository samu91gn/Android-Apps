package pisada.fallDetector;

import java.util.ArrayList;

import pisada.database.FallDataSource;
import pisada.database.SessionDataSource;
import pisada.recycler.SessionDetailsCardAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

/**
 * fragment riguardante i dettagli di una sessione
 */
public class SessionDetailsFragment extends FallDetectorFragment {
	public static String sessionName;	
	SessionDetailsCardAdapter cardAdapter;
	Activity activity;
	LayoutManager mLayoutManager;
	FallDataSource fallDataSource;
	SessionDataSource sessionData;
	SessionDataSource.Session session;
	private int TYPE = -1;
	
	public SessionDetailsFragment()
	{
		setHasOptionsMenu(true);
	}
	public int getType()
	{
		return this.TYPE;
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_sessiondetails, container, false);  
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		activity.setTitle(sessionName);
		sessionData = new SessionDataSource(activity);
		session = sessionData.getSession(sessionName);
		rView=(RecyclerView) getView().findViewById(R.id.session_details_recycler);
		rView.setHasFixedSize(true);
		cardAdapter = new SessionDetailsCardAdapter(activity, new SessionDataSource(activity), sessionName);
		rView.setAdapter(cardAdapter);
		mLayoutManager = new LinearLayoutManager(activity);
		rView.setLayoutManager(mLayoutManager);
	
		if(fallDataSource == null)
			fallDataSource = new FallDataSource(activity);
		if(session == null){((MainActivity)activity).switchFragment(new Intent(activity,SessionsListFragment.class));return;}//viene chiamata se android chiude la classe per mancanza di memoria, in questo caso viene perso per strada il parametro name, questa riga fa ritornare alla home
		ArrayList<FallDataSource.Fall> falls = fallDataSource.sessionFalls(session);
		if(falls != null) 
			for(int i = falls.size()-1; i >= 0; i--){
				cardAdapter.addFall(falls.get(i));
			}
		this.scroll(MainActivity.sessionDetailsFragmentLastIndex);
	}

	
	@Override
	public void onAttach(Activity a){
		super.onAttach(a);
		activity = a;
	}
	

	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if(MainActivity.isPortrait){
		menu.clear();
        inflater.inflate(R.menu.session, menu);
        super.onCreateOptionsMenu(menu, inflater);
		}
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		

		int id = item.getItemId();

		switch (id) {
				case R.id.rename_session:
		{
			// Set an EditText view to get user input 
			final EditText input = new EditText(activity);
			input.addTextChangedListener((TextWatcher)activity);
			new AlertDialog.Builder(activity)
			.setTitle("Rename")
			.setMessage("Insert name")
			.setView(input)
			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					
					String value = input.getText().toString(); 
					

					if(!sessionData.existSession(value)){
						sessionData.renameSession(session, value);
						activity.setTitle(value);
						session = sessionData.getSession(value);
						sessionName = value;
						cardAdapter.updateSessionName(sessionName);
					}
					
					else
					{
						Toast.makeText(activity, "Can't add session with same name!", Toast.LENGTH_LONG).show();
						activity.setTitle(value);
					}

				}
			}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// Do nothing.
				}
			}).show();

		}
		return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
