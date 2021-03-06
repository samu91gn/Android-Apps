package pisada.recycler;


import java.util.ArrayList;

import pisada.database.FallDataSource;
import pisada.database.FallDataSource.Fall;
import pisada.database.SessionDataSource;
import pisada.database.SessionDataSource.Session;
import pisada.fallDetector.CurrentSessionFragment;
import pisada.fallDetector.FragmentCommunicator;
import pisada.fallDetector.R;
import pisada.fallDetector.SessionDetailsFragment;
import pisada.fallDetector.SessionsListFragment;
import pisada.fallDetector.Utility;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import fallDetectorException.DublicateNameSessionException;



public class SessionListCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

	private ArrayList<Session> sessionList;
	private static Activity activity;
	private static SessionDataSource sessionData;
	private static FallDataSource fallData;
	private ArrayList<Boolean> expandedArray=new ArrayList<Boolean>(); //supporto a card espanse
	static private ArrayList<Boolean> selectedArray=new ArrayList<Boolean>(); //supporto a card selezionate
	static private int existExp=-1;
	protected boolean oneSelected=false;
	static private int selectedCard=0;
	private SessionsListFragment frag;





//Elemento lista: sessione corrente, se esiste
	public static class CurrentSessionHolder extends RecyclerView.ViewHolder {
		private TextView sessionNameText;
		private TextView fallsText;
		private TextView timeText;
		private TextView dateText;
		private TextView sentText;
		private ImageView img;
		private RelativeLayout fieldLay;
		private View card;
		private TextView stateText;
		
		
		public CurrentSessionHolder(View v) {
			super(v);
			card=v;
			sessionNameText=(TextView) v.findViewById(R.id.first_curr_name);
			fallsText=(TextView) v.findViewById(R.id.fist_curr_num_cadute);
			timeText=(TextView) v.findViewById(R.id.first_curr_ora_inizio);
			dateText=(TextView) v.findViewById(R.id.first_curr_date);
			sentText=(TextView) v.findViewById(R.id.first_curr_sent_field);
			fieldLay=(RelativeLayout) v.findViewById(R.id.first_curr_field_layout);
			img=(ImageView) v.findViewById(R.id.current_session_icon);
			stateText=(TextView) v.findViewById(R.id.state_field);
			}

	}

	
	//Elementi lista: vecchie sessioni
	public static class OldSessionHolder extends RecyclerView.ViewHolder
	{
		private TextView vName;
		private TextView durationText;
		private TextView startTimeTextView;
		private TextView fallsTextView;


		private ImageView sessionIcon;
		private ImageButton expandButton;

		private RelativeLayout buttonsLayout;
		private Button deleteBtn;
		private Button archiveBtn;
		private Button renameBtn;
		private CardView oldCard;
		public OldSessionHolder(View v) {
			super(v);
			oldCard=(CardView) v;
			vName =  (TextView) v.findViewById(R.id.old_name_name);
			renameBtn=(Button) v.findViewById(R.id.old_rename_button);
			deleteBtn=(Button) v.findViewById(R.id.old_delete_button);
			archiveBtn =(Button)v.findViewById(R.id.old_archive_button);
			sessionIcon=(ImageView) v.findViewById(R.id.archive_old_session_icon);
			buttonsLayout= (RelativeLayout) v.findViewById(R.id.buttons_layout);
			expandButton=(ImageButton) v.findViewById(R.id.expand_button);
			startTimeTextView=(TextView) v.findViewById(R.id.old_start_description);
			fallsTextView= (TextView) v.findViewById(R.id.old_falls_description);
			durationText=(TextView) v.findViewById(R.id.old_duration_description);

		}


	}


	public SessionListCardAdapter(final Activity activity, RecyclerView rView, SessionsListFragment frag) {

		SessionListCardAdapter.activity=activity;
		sessionData=new SessionDataSource(activity);
		fallData=new FallDataSource(activity);
		this.frag=frag;
		this.sessionList=sessionData.notArchivedSessions();

		if(!sessionData.existCurrentSession()){
			sessionList.add(0,new Session()); //se non esiste una sessione corrente, aggiungo una sessione vuota come primo elemento della lista
		}

		for(int i=0;i<sessionList.size();i++){
			expandedArray.add(false);
			selectedArray.add(false);
		}
	}

	@Override
	public int getItemCount() {
		return sessionList.size();
	}

	@Override
	public void onBindViewHolder(ViewHolder holder,  final int i) {

		Session currSession=sessionData.currentSession();
		switch(i) {
		case 0:
			CurrentSessionHolder cHolder=(CurrentSessionHolder) holder;

			if(currSession==null){

				cHolder.fieldLay.setVisibility(View.GONE);
				cHolder.img.setVisibility(View.GONE);
				cHolder.sessionNameText.setText(activity.getResources().getString(R.string.no_current_session));

			}
			else{
				cHolder.fieldLay.setVisibility(View.VISIBLE);
				cHolder.img.setVisibility(View.VISIBLE);
				cHolder.sessionNameText.setText(currSession.getName());
				cHolder.timeText.setText(String.valueOf(currSession.getStartTime()).toString());
				cHolder.dateText.setText(Utility.getStringDate(currSession.getStartTime()));
				cHolder.timeText.setText(Utility.getStringHour(currSession.getStartTime()));
				cHolder.fallsText.setText(String.valueOf(currSession.getFallsNumber()));
				cHolder.stateText.setText(currSession.isOnPause()? activity.getResources().getString(R.string.pausedmin):  activity.getResources().getString(R.string.running));
				ArrayList<Fall> falls=currSession.getFalls();

				View.OnClickListener currListener=new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent=new Intent(activity,CurrentSessionFragment.class);
						((FragmentCommunicator)activity).switchFragment(intent);
					}
				};
		
				cHolder.card.setOnClickListener(currListener);
				if(falls.size()!=0){
					cHolder.sentText.setVisibility(View.VISIBLE);
					cHolder.sentText.setText(activity.getResources().getString(R.string.falls_notified));
					cHolder.sentText.setTextColor(Color.GREEN);
					for(Fall f:falls){
						if(!f.wasNotified()){
							cHolder.sentText.setText(activity.getResources().getString(R.string.falls_unnotified));
							cHolder.sentText.setTextColor(Color.RED);
							break;
						}

					}
				}
				else  cHolder.sentText.setVisibility(View.INVISIBLE);
				BitmapManager.loadBitmap(currSession.getID(), cHolder.img, activity);
			}

			return;
		}

		final OldSessionHolder Oholder=(OldSessionHolder) holder;
		final Session session = sessionList.get(i);
		if(selectedArray.get(i))Oholder.oldCard.setBackgroundColor(Color.CYAN);
		else Oholder.oldCard.setBackgroundColor(Color.WHITE);

		Oholder.oldCard.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(final View v) {
				if(existExp!=-1){
					expandedArray.set(existExp,false);
					notifyItemChanged(existExp);
					existExp=-1;
				}
				else{
					if(selectedCard>0){
						Integer colorFrom =null;
						Integer colorTo =null;

						if(selectedArray.get(i)==false){
							selectedArray.set(i, true);
							colorFrom =Color.WHITE;
							colorTo =Color.CYAN;
							selectedCard++;
						}
						else{
							colorFrom =Color.CYAN;
							colorTo =Color.WHITE;
							selectedArray.set(i,false);
							selectedCard--;
						}

						frag.existSelectedItem(selectedCard>0); 
						ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
						colorAnimation.addUpdateListener(new AnimatorUpdateListener() {

							@Override
							public void onAnimationUpdate(ValueAnimator animator) {
								v.setBackgroundColor((Integer)animator.getAnimatedValue());
							}

						});
						colorAnimation.setDuration(250).start();
					}
					else{
						Intent intent=new Intent(activity,SessionDetailsFragment.class);
						intent.putExtra(Utility.SESSION_NAME_KEY, session.getName());
						((FragmentCommunicator)activity).switchFragment(intent);
					}
				}


			}
		});
		Oholder.oldCard.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(final View v) {

				Integer colorFrom =null;
				Integer colorTo =null;
				if(existExp!=-1){
					expandedArray.set(existExp, false);
					notifyItemChanged(existExp);
					existExp=-1;
				}

				if(selectedArray.get(i)==false){
					selectedArray.set(i, true);
					colorFrom =Color.WHITE;
					colorTo =Color.CYAN;
					selectedCard++;

					frag.existSelectedItem(selectedCard>0);
					ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
					colorAnimation.addUpdateListener(new AnimatorUpdateListener() {

						@Override
						public void onAnimationUpdate(ValueAnimator animator) {
							v.setBackgroundColor((Integer)animator.getAnimatedValue());
						}

					});

					colorAnimation.setDuration(250).start();
				}
				return true;

			}
		});




		if(fallData.sessionFalls(session)!=null)
			String.valueOf(fallData.sessionFalls(session).size());

		Oholder.vName.setText(session.getName());
		Oholder.durationText.setText(Utility.longToDuration(sessionData.sessionDuration(session)));
		Oholder.fallsTextView.setText(String.valueOf(session.getFallsNumber()));
		Oholder.startTimeTextView.setText(Utility.getStringDate(session.getStartTime())+", "+Utility.getStringHour(session.getStartTime()));
		BitmapManager.loadBitmap(session.getID(), Oholder.sessionIcon, activity);
		Oholder.renameBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(final View v) {
				final EditText input = new EditText(activity);
				input.setText( session.getName());
				input.addTextChangedListener((TextWatcher) activity);
				new AlertDialog.Builder(activity)
				.setTitle(v.getContext().getResources().getString(R.string.rename))
				.setMessage(v.getContext().getResources().getString(R.string.insertname))
				.setView(input)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						String value = input.getText().toString();
						Session s=sessionData.getSession(value);
						if(s!=null&&!s.equals(session)){
							Toast.makeText(activity, v.getContext().getResources().getString(R.string.samename), Toast.LENGTH_SHORT).show();

						}
						else{
							if(value==null||value.equalsIgnoreCase("")){
								Toast.makeText(activity, v.getContext().getResources().getString(R.string.insertnamesess), Toast.LENGTH_SHORT).show();
							}
							else{
								sessionData.renameSession(session, value);
								notifyItemChanged(i);
							}
						}

					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Do nothing.
					}
				}).show();

				expandedArray.set(i, false);
				notifyItemChanged(i);

			}


		});


		if(expandedArray.get(i))Oholder.buttonsLayout.setVisibility(View.VISIBLE);
		else Oholder.buttonsLayout.setVisibility(View.GONE);
		Oholder.expandButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(selectedCard==0){
					for(int k=0;k<expandedArray.size();k++){

						if(k!=i&&expandedArray.get(k)){
							expandedArray.set(k,false);
							notifyItemChanged(k);
						}
					}

					if(expandedArray.get(i)==false){
						expandedArray.set(i, true);
						existExp=i;
						Oholder.buttonsLayout.setVisibility(View.VISIBLE);
						Animation animation = AnimationUtils.loadAnimation(activity.getApplicationContext(), R.anim.expandanimation);
						Oholder.buttonsLayout.startAnimation(animation);
					}
					else{
						expandedArray.set(i, false);
						existExp=-1;
						notifyItemChanged(i);
					}


				}
			}
		});

		sessionList.size();
		Oholder.deleteBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {

				new AlertDialog.Builder(activity)
				.setTitle("Delete "+session.getName()+" ?" )

				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						sessionData.deleteSession(session);
						sessionList.remove(i);
						expandedArray.remove(i);
						notifyItemRemoved(i);
						notifyItemRangeChanged(i, i<sessionList.size()-15 ? i+15: sessionList.size()-1);

					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					//nulla
					}
				}).show();
				expandedArray.set(i, false);
				notifyItemChanged(i);



			}
		});

		Oholder.archiveBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {				
				
				sessionData.setSessionArchived(session, true);
				sessionList.remove(i);
				expandedArray.remove(i);
				notifyItemRemoved(i);
				notifyItemRangeChanged(i,i<sessionList.size()-15 ? i+15: sessionList.size()-1);
			}
		});


	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {

		if(type==0){
			return new CurrentSessionHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.first_current_session_sessions_list_card, viewGroup, false));

		}

		return new OldSessionHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.old_session_card, viewGroup, false));

	}

	//AGGIUNGE NUOVA SESSIONE ALL'ADAPTER, SENZA STORE NEL DATABASE. STORE DA FARE FUORI PRIMA
	public void addNewSession(String name,long startTime) throws DublicateNameSessionException {

		if(sessionData.existCurrentSession()){
			sessionData.closeSession(sessionData.currentSession());
			sessionList.add(1,sessionData.openNewSession(name, startTime));
		}

		else{
			sessionList.set(1,sessionData.openNewSession(name, startTime));
			notifyItemChanged(0);
		}


	}

	//CHIUDE SESSIONE CORRENTE APPOGGIANDOSI AL METODO DI SESSIONDATASOURCE
	public void closeCurrentSession(){
		Session currSession=sessionList.get(1);
		if(currSession.isValidSession()) {
			sessionData.closeSession(currSession);
			sessionList.add(1,new Session());
		}

	}

	public void check(){
		sessionList=sessionData.notArchivedSessions();
		if(!sessionData.existCurrentSession()){
			sessionList.add(0, new Session());
		}
	}

	@Override
	public int getItemViewType(int position) {

		switch(position){
		case 0: return 0;
		}
		return 3;

	}


	public void closeAllDetails(){


		for(int k=0;k<expandedArray.size();k++){

			if(expandedArray.get(k)){
				expandedArray.set(k,false);
				notifyItemChanged(k);
				return;
			}
		}
	}

	public void deleteSelectedSession(){

		new AlertDialog.Builder(activity)
		.setTitle(selectedCard==1 ? "Delete "+selectedCard+" session?" :"Delete "+selectedCard+" sessions?" )

		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				int i=0;
				while(i<selectedArray.size()){

					if(selectedArray.get(i)){			
						Session s=sessionList.remove(i);
						selectedArray.remove(i);
						sessionData.deleteSession(s);
						notifyItemRemoved(i);
						notifyItemRangeChanged(i, i<sessionList.size()-15 ? i+15: sessionList.size()-1);
					}
					else i++;


				}
				selectedCard=0;
				frag.existSelectedItem(false);

			}
		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Do nothing.
			}
		}).show();




	}

	public void archiveSelectedSession(){

		new AlertDialog.Builder(activity)
		.setTitle(selectedCard==1 ? "Archive "+selectedCard+" session?" :"Archive "+selectedCard+" sessions?" )
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				int i=0;
				while(i<selectedArray.size()){

					if(selectedArray.get(i)){			
						Session s=sessionList.remove(i);
						selectedArray.remove(i);
						sessionData.setSessionArchived(s, true);
						notifyItemRemoved(i);
						notifyItemRangeChanged(i, i<sessionList.size()-15 ? i+15: sessionList.size()-1);
					}
					else i++;


				}
				selectedCard=0;
				frag.existSelectedItem(false);

			}
		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Do nothing.
			}
		}).show();


	}


	public  int  itemSelectedNumber(){return selectedCard;}
	public void deselectAll(){
		for(int i=0;i<selectedArray.size();i++) selectedArray.set(i, false);
		selectedCard=0;
	}


}















