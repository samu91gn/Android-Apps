package pisada.recycler;


import java.util.ArrayList;

import pisada.database.SessionDataSource;
import pisada.database.SessionDataSource.Session;
import pisada.fallDetector.ArchiveFragment;
import pisada.fallDetector.R;
import pisada.fallDetector.Utility;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

//modificato1
public class ArchiveCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

	private ArrayList<Session> sessionList;
	private static Activity activity;
	private static SessionDataSource sessionData;
	private ArchiveFragment frag;

	public static class SessionHolder extends RecyclerView.ViewHolder
	{
		private TextView name;
		private TextView falls;
		private TextView date;
		private ImageView img;
		private Button deleteBtn;
		private Button unarchiveBtn;

		public SessionHolder(View v) {
			super(v);
			img=(ImageView) v.findViewById(R.id.archive_old_session_icon);
			name=(TextView) v.findViewById(R.id.archive_old_name_name);
			falls =(TextView)v.findViewById(R.id.archive_old_falls_description);
			date =(TextView)v.findViewById(R.id.archive_old_start_description);
			deleteBtn=(Button) v.findViewById(R.id.archive_old_delete_button);
			unarchiveBtn=(Button) v.findViewById(R.id.archive_old_archive_button);
			//	deleteBtn=(Button) v.findViewById(R.id.)
		}
	}


	public ArchiveCardAdapter(final Activity activity, RecyclerView rView, ArchiveFragment frag) {
		this.frag=frag;
		ArchiveCardAdapter.activity=activity;
		sessionData=new SessionDataSource(activity);

		this.sessionList=sessionData.archivedSessions();

	}

	@Override
	public int getItemCount() {
		return sessionList.size();
	}

	@Override
	public void onBindViewHolder(ViewHolder holder,  final int i) {

		SessionHolder Oholder=(SessionHolder) holder;
		final Session session = sessionList.get(i);

		Oholder.name.setText(session.getName());
		Oholder.falls.setText(String.valueOf(session.getFallsNumber()));
		Oholder.date.setText(Utility.getStringDate(session.getStartTime()));
		BitmapManager.loadBitmap(session.getID(), Oholder.img, activity);
		Oholder.unarchiveBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				sessionData.setSessionArchived(session, false);
				sessionList.remove(i);
				notifyItemRemoved(i);
				notifyItemRangeChanged(i, sessionList.size()-i);
				frag.updarteActionBar();
			}
		});

		Oholder.deleteBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(activity)
				.setTitle(activity.getResources().getString(R.string.delete)+" "+session.getName()+" ?" )

				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						sessionData.deleteSession(session);
						sessionList.remove(i);
						notifyItemRemoved(i);
						notifyItemRangeChanged(i, sessionList.size()-i);
						frag.updarteActionBar();

					}
				}).setNegativeButton(activity.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Do nothing.
					}
				}).show();

			}
		});




	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {

		return new SessionHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.archive_old_session_card, viewGroup, false));

	}

	public void deleteAllSession(){

		new AlertDialog.Builder(activity)
		.setTitle(activity.getResources().getString(R.string.deleteall))
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				int size=sessionList.size();
				for(int i =0;i<size;i++){

					sessionData.deleteSession(sessionList.remove(size-i-1));
					notifyItemRemoved(size-i-1);
				}
				frag.updarteActionBar();
			}
		}).setNegativeButton(activity.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Do nothing.
			}
		}).show();

	}

	public void archiveAllSession(){

		new AlertDialog.Builder(activity)
		.setTitle(activity.getResources().getString(R.string.unarchiveall))
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				int size=sessionList.size();
				for(int i =0;i<size;i++){
					sessionData.setSessionArchived((sessionList.remove(size-i-1)),false);
					notifyItemRemoved(size-i-1);
				}
				frag.updarteActionBar();
			}
		}).setNegativeButton(activity.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Do nothing.
			}
		}).show();


	}



}