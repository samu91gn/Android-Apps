package pisada.fallDetector;

/*
 * classe archive semplicissima, inizializza solo l'adapter e la recyclerview
 */

import pisada.recycler.ArchiveCardAdapter;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
//modificato1
public class ArchiveFragment extends FallDetectorFragment  {

	private Activity activity;
//	private final boolean HASOPTIONSMENU = false;
	private final int TYPE = 2;
	

	public int getType()
	{
		return this.TYPE;
	}

	public ArchiveFragment()
	{
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.archive_menu, menu);
		if(rView.getAdapter().getItemCount()>0){
			menu.findItem(R.id.delete_archive_bar).setVisible(true);
			menu.findItem(R.id.archive_archive_bar).setVisible(true);
		}
		else {
			menu.findItem(R.id.delete_archive_bar).setVisible(false);
			menu.findItem(R.id.archive_archive_bar).setVisible(false);
		}
		
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_archive, container, false);  
	}

	@Override
	public void onAttach(Activity a)
	{
		super.onAttach(a);
		activity = a;
	}

	@Override
	public void onActivityCreated(Bundle savedInstance)
	{
		super.onActivityCreated(savedInstance);		
		rView=(RecyclerView) getView().findViewById(R.id.archive_recycler);
		ArchiveCardAdapter cardAdapter=new ArchiveCardAdapter(activity, rView, this);
		rView.setAdapter(cardAdapter);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(activity);
		rView.setLayoutManager(mLayoutManager);
		rView.setItemAnimator(new DefaultItemAnimator());
		this.scroll(MainActivity.archiveFragmentLastIndex);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	public void onResume()
	{
		super.onResume();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	public void updarteActionBar(){
	getActivity().invalidateOptionsMenu();
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.delete_archive_bar:
			((ArchiveCardAdapter) rView.getAdapter()).deleteAllSession();
			return true;
		case R.id.archive_archive_bar:
			((ArchiveCardAdapter) rView.getAdapter()).archiveAllSession();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
