package pisada.database;

import java.util.ArrayList;

import pisada.database.FallDataSource.Fall;
import fallDetectorException.AlreadyCloseSessionException;
import fallDetectorException.BoolNotBoolException;
import fallDetectorException.DublicateNameSessionException;
import fallDetectorException.InvalidSessionException;
import fallDetectorException.MoreThanOneOpenSessionException;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

//Gestione delle sessioni: per creare, ottenere e modificare sessioni � necessario usare un'istanza di questa classe
public class SessionDataSource { 
	private static SQLiteDatabase database;
	private FallSqlHelper databaseHelper;
	private Context context;
	private static ArrayList<Session> sessionList=new ArrayList<Session>(); //Unica lista sessioni, garantisce integrit� a fronte di modifiche di sessioni esistenti.


	//Classe interna Session
	public static class  Session {
		private String name;
		private long startTime;
		private long endTime;
		private Context context;
		private long stopTimePreference;
		private int close;
		private int pause;
		private int archived;
		private int id;
		private boolean isValid=true;
		private int falls=0;

		//COSTRUTTORE INTERNO NUOVA SESSIONE
		private Session(String name,long startTime,long endTime,long stopTimePreference, int close, int pause, int archived,int id, Context context) throws BoolNotBoolException{
			if((close!=0&&close!=1)||(pause!=0&&pause!=1)||(archived!=0&&archived!=1))throw new BoolNotBoolException();
			else{
				this.name=name;
				this.startTime=startTime;
				this.endTime=endTime;
				this.close=close;
				this.stopTimePreference=stopTimePreference;
				this.pause=pause;
				this.archived=archived;
				this.id=id;
				this.falls=SessionDataSource.getSessionFallsNumber(name);

			}

		}


		//PUBBLICO; RITORNA SESSIONE VUOTA PER ADAPTER. SESSION NON VALIDA
		public Session(){
			isValid=false;
		}

		//GETTER PUBBLICI
		public long getStartTime(){return startTime;}
		public long getEndTime(){return endTime;}
		public String getName(){return name;}
		public boolean booleanIsClose(){return close==1;}
		public long getStopTimePreference(){return stopTimePreference;}
		public int integerIsClose(){return close;}
		public boolean isValidSession() {return isValid;}
		public boolean hasStopTimePreference(){return stopTimePreference!=FallSqlHelper.NO_VALUE_FOR_TIME_COLUMN;}
		public boolean isOnPause(){return pause==FallSqlHelper.PAUSE;}
		public boolean isRunning(){return pause==FallSqlHelper.RUNNING;}
		public boolean isArchived(){return archived==FallSqlHelper.ARCHIVED;}
		public int getID(){return id;}
		public int getFallsNumber(){return falls;}
		public ArrayList<Fall> getFalls(){
			FallDataSource fallData=new FallDataSource(context);
			return fallData.sessionFalls(this);

		}


		//SETTER PRIVATI
		private void setName(String name){this.name=name;}
		@SuppressWarnings("unused")
		private void setEndTime(long endTime){this.endTime=endTime;}
		private void setClose(long endTime){close=FallSqlHelper.CLOSE;this.endTime=endTime;}
		private void setStopTimePreference(long t){stopTimePreference=t;}
		private void pause(){pause=FallSqlHelper.PAUSE;}
		private void resume(){pause=FallSqlHelper.RUNNING;}
		private void setArchived(boolean archived){
			int i=FallSqlHelper.NOTARCHIVED;
			if(archived) i=FallSqlHelper.ARCHIVED;
			this.archived=i;
		}
		protected int  addFall(){return ++falls;}
	}

	public SessionDataSource(Context context){
		synchronized(SessionDataSource.class)
		{		
			if(databaseHelper==null) databaseHelper=FallSqlHelper.getIstance(context);

			this.context=context;
			open();
			if(sessionList.size()==0){
				Cursor cursor= database.rawQuery("SELECT * FROM "+FallSqlHelper.SESSION_TABLE+" ORDER BY "+FallSqlHelper.SESSION_START_TIME+" DESC",null );
				if(cursor.getCount()!=0)
					while(cursor.moveToNext()){
						Session s=cursorToSession(cursor);
						sessionList.add(s);
					}
				cursor.close();
			}
		}
	}
	public void open() throws SQLException {
		database = databaseHelper.getWritableDatabase();
	}


	//NUOVA SESSIONE CON STOPTIME PREFERENCE
	public Session openNewSession(String name, long startTime,long stopTimePreference) throws BoolNotBoolException, MoreThanOneOpenSessionException, DublicateNameSessionException{

		if(existCurrentSession()) throw new MoreThanOneOpenSessionException();
		if(getSession(name)!=null) throw new DublicateNameSessionException();


		int id=0;
		if(!sessionList.isEmpty()) id=sessionList.get(0).id+1;
		ContentValues values=new ContentValues();
		values.put(FallSqlHelper.SESSION_NAME, name);
		values.put(FallSqlHelper.SESSION_START_TIME, startTime);
		values.put(FallSqlHelper.SESSION_STOP_TIME_PREFERENCE, stopTimePreference);
		values.put(FallSqlHelper.SESSION_ID,id);
		database.insert(FallSqlHelper.SESSION_TABLE, null,values);
		Session newSession= new Session(name,startTime,FallSqlHelper.NO_VALUE_FOR_TIME_COLUMN, stopTimePreference,FallSqlHelper.OPEN,FallSqlHelper.RUNNING, FallSqlHelper.NOTARCHIVED,id, context);
		sessionList.add(0,newSession);
		return newSession;

	}

	//NUOVA SESSIONE SENZA STOPTIMEPREFERENCE
	public Session openNewSession(String name, long startTime) throws  MoreThanOneOpenSessionException, DublicateNameSessionException{

		if(existCurrentSession()) throw new MoreThanOneOpenSessionException();
		if(getSession(name)!=null) throw new DublicateNameSessionException();

		int id=0;
		if(!sessionList.isEmpty()) id=sessionList.get(0).id+1;
		ContentValues values=new ContentValues();
		values.put(FallSqlHelper.SESSION_NAME, name);
		values.put(FallSqlHelper.SESSION_START_TIME, startTime);
		values.put(FallSqlHelper.SESSION_ID,id);
		database.insert(FallSqlHelper.SESSION_TABLE, null,values);
		Session newSession= new Session(name,startTime,FallSqlHelper.NO_VALUE_FOR_TIME_COLUMN,FallSqlHelper.NO_VALUE_FOR_TIME_COLUMN,FallSqlHelper.OPEN,FallSqlHelper.RUNNING,FallSqlHelper.ARCHIVED,id, context);
		sessionList.add(0,newSession);
		return newSession;

	}


	//RITORNA SESSIONE CORRENTE APERTA
	public Session currentSession(){
		if(!existCurrentSession())return null;
		return sessionList.get(0);

	}

	//RITORNA TRUE SE ESISTE SESSIONE APERTA CORRENTE
	public boolean existCurrentSession(){
		if(sessionList.size()==0)return false;
		return !sessionList.get(0).booleanIsClose();		
	}

	//RITORNA TUTTE LE SESSIONI
	public ArrayList<Session> sessions(){
		ArrayList<Session> list=new ArrayList<Session>();


		for(Session s: sessionList){
			list.add(s);
		}
		return list;

	}


	//CONVERTE TUPLA DATABASE IN SESSIONE
	private Session cursorToSession(Cursor cursor){
		if(cursor.getCount()==0)return null;
		String name=cursor.getString(cursor.getColumnIndex(FallSqlHelper.SESSION_NAME));
		long startTime=cursor.getLong(cursor.getColumnIndex(FallSqlHelper.SESSION_START_TIME));
		long endTime=cursor.getLong(cursor.getColumnIndex(FallSqlHelper.SESSION_END_TIME));
		long stopTime=cursor.getLong(cursor.getColumnIndex(FallSqlHelper.SESSION_STOP_TIME_PREFERENCE));
		int isClose=cursor.getInt(cursor.getColumnIndex(FallSqlHelper.SESSION_CLOSE_COLUMN));
		int isOnPause=cursor.getInt(cursor.getColumnIndex(FallSqlHelper.SESSION_PAUSE_COLUMN));
		int isArchived=cursor.getInt(cursor.getColumnIndex(FallSqlHelper.SESSION_ARCHIVED_COLUMN));
		int id=cursor.getInt(cursor.getColumnIndex(FallSqlHelper.SESSION_ID));
		Session session=null;

		session=new Session(name,startTime,endTime, stopTime, isClose,isOnPause, isArchived,id,context);



		return session;
	}

	//NUMERO DI TUTTE LE SESSIONI
	public int sessionCount(){
		return sessionList.size();
	}


	//RITORNA TRUE SE ESISTE UNA SESSIONE CON QUEL NOME, FALSO ALTRIMENTI
	public boolean existSession(String name){

		Session s=getSession(name);
		if(s==null)return false;
		return true;
	}

	
	public Session getSession(String name){
		for(Session s: sessionList){
			if(s.getName().equalsIgnoreCase(name)) return s;
		}
		return null;
	}


	//CHIUDE SESSIONE DATO IL NOME
	public void closeSession(String name){
		Session s=getSession(name);
		if(s==null) return;
		if(s.booleanIsClose())throw new AlreadyCloseSessionException();

		long sEndTime=System.currentTimeMillis();

		ContentValues values=new ContentValues();
		values.put(FallSqlHelper.SESSION_CLOSE_COLUMN, FallSqlHelper.CLOSE);
		values.put(FallSqlHelper.SESSION_END_TIME,sEndTime);
		values.put(FallSqlHelper.SESSION_PAUSE_COLUMN,FallSqlHelper.PAUSE);
		database.update(FallSqlHelper.SESSION_TABLE, values, FallSqlHelper.SESSION_NAME+" = '"+ name+"'", null);
		s.setClose(sEndTime);
	}


	//CHIUDE SESSIONE DATA LA SESSIONE
	public void closeSession(Session s){

		if(!s.isValidSession())throw new InvalidSessionException();
		if(s.booleanIsClose())throw new AlreadyCloseSessionException();

		String name=s.getName();
		long sEndTime=System.currentTimeMillis();
		if(s.booleanIsClose())return;

		ContentValues values=new ContentValues();
		values.put(FallSqlHelper.SESSION_CLOSE_COLUMN, FallSqlHelper.CLOSE);
		values.put(FallSqlHelper.SESSION_END_TIME,sEndTime);
		values.put(FallSqlHelper.SESSION_PAUSE_COLUMN,FallSqlHelper.PAUSE);
		database.update(FallSqlHelper.SESSION_TABLE, values, FallSqlHelper.SESSION_NAME+" = '"+ name+"'", null);
		s.setClose(sEndTime);

	}

	//AGGIORNA DURATA E CHIUDE SESSIONE. SIA OGGETTO CHE DATABASE. RITORNA NUOVA DURATA

	public long closeAfterUpdateSession(Session s, long addDuration){
		if(!s.isValidSession()) throw new InvalidSessionException();
		if(s.booleanIsClose())throw new AlreadyCloseSessionException();

		long newDuration=updateSessionDuration(s,addDuration);
		closeSession(s);
		return newDuration;
	}


	//AGGIORNA DURATA NEL DATABASE E RITORNA LA DURATA DELLA SESSIONE, SOMMANDO LA DURATA DA AGGIUNGERE. 
	public long updateSessionDuration(Session s, long addDuration){

		if(!s.isValidSession()) throw new InvalidSessionException();
		if(s.booleanIsClose())throw new  AlreadyCloseSessionException();

		long oldDuration=sessionDuration(s), newDuration=oldDuration+addDuration;

		database.execSQL("UPDATE "+FallSqlHelper.SESSION_TABLE
				+ " SET "+ FallSqlHelper.SESSION_DURATION+" = "+newDuration+
				" WHERE "+FallSqlHelper.SESSION_NAME+" = '"+s.getName()+"';");

		return newDuration;
	}

	//RITORNA L'ULTIMA DURATA STORATA NEL DATABASE DELLA LA SESSIONE PASSATA
	public long sessionDuration(Session s){

		if(!s.isValidSession()) throw new InvalidSessionException();

		String[] column={FallSqlHelper.SESSION_DURATION};
		String where=FallSqlHelper.SESSION_NAME+" = '"+s.getName()+"'";
		Cursor cursor=database.query(FallSqlHelper.SESSION_TABLE, column,where,null,null,null,null);
		if(cursor.getCount()==0){
			cursor.close();
			return -1;
		}
		cursor.moveToFirst();
		long duration=cursor.getLong(0);
		cursor.close();
		return duration;
	}





	//CAMBIA STOPTIMEPREFERENCE DELL'OGGETTO SESSIONE E AGGIORNA IL DATABASE
	public void changeStopTimePreference(Session s, long newStopTime){
		if(!s.isValidSession()) throw new InvalidSessionException();
		if(s.booleanIsClose()) throw new AlreadyCloseSessionException();

		database.execSQL("UPDATE "+FallSqlHelper.SESSION_TABLE
				+ " SET "+ FallSqlHelper.SESSION_STOP_TIME_PREFERENCE+" = "+newStopTime+
				" WHERE "+FallSqlHelper.SESSION_NAME+" = '"+s.getName()+"';");

		s.setStopTimePreference(newStopTime);
	}


	public void renameSession(Session s,String name){

		if(!s.isValidSession())throw new InvalidSessionException();

		database.execSQL("UPDATE "+FallSqlHelper.SESSION_TABLE
				+ " SET "+ FallSqlHelper.SESSION_NAME+" = '"+name+
				"' WHERE "+FallSqlHelper.SESSION_NAME+" = '"+s.getName()+"';");

		database.execSQL("UPDATE "+FallSqlHelper.FALL_TABLE
				+ " SET "+ FallSqlHelper.FALL_FSESSION+" = '"+name+
				"' WHERE "+FallSqlHelper.FALL_FSESSION+" = '"+s.getName()+"';");

		database.execSQL("UPDATE "+FallSqlHelper.ACQUISITION_TABLE
				+ " SET "+ FallSqlHelper.ACQUISITION_ASESSION+" = '"+name+
				"' WHERE "+FallSqlHelper.ACQUISITION_ASESSION+" = '"+s.getName()+"';");
		s.setName(name);
	}

	
	public void setSessionOnPause(Session s){

		if(!s.isValidSession()) throw new InvalidSessionException();
		if(s.booleanIsClose()) throw new AlreadyCloseSessionException();

		database.execSQL("UPDATE "+FallSqlHelper.SESSION_TABLE
				+ " SET "+ FallSqlHelper.SESSION_PAUSE_COLUMN+" = "+FallSqlHelper.PAUSE+
				" WHERE "+FallSqlHelper.SESSION_NAME+" = '"+s.getName()+"';");
		s.pause();

	}

	public void resumeSession(Session s){
		if(!s.isValidSession()) throw new InvalidSessionException();
		if(s.booleanIsClose()) throw new AlreadyCloseSessionException();

		database.execSQL("UPDATE "+FallSqlHelper.SESSION_TABLE
				+ " SET "+ FallSqlHelper.SESSION_PAUSE_COLUMN+" = "+FallSqlHelper.RUNNING+
				" WHERE "+FallSqlHelper.SESSION_NAME+" = '"+s.getName()+"';");
		s.resume();
	}

	//RITORNA LISTA SESSIONI ARCHIVIATE
	public ArrayList<Session> archivedSessions(){
		ArrayList<Session> list=new ArrayList<Session>();
		for(Session s: sessionList){
			if(s.isArchived()) list.add(s);
		}

		return list;
	}


	//RITORNA LISTA SESSIONI NON ARCHIVIATE
	public ArrayList<Session> notArchivedSessions(){
		ArrayList<Session> list=new ArrayList<Session>();
		for(Session s: sessionList){
			if(!s.isArchived())list.add(s);
		}

		return list;
	}

	public void setSessionArchived(Session s,boolean boolArchived){
		if(!s.isValidSession()) throw new InvalidSessionException();

		int archived=FallSqlHelper.NOTARCHIVED;
		if(boolArchived) 
			archived=FallSqlHelper.ARCHIVED; //TODO ATTENZIONE MODIFICA SAMU

		ContentValues values=new ContentValues();
		values.put(FallSqlHelper.SESSION_ARCHIVED_COLUMN,archived);
		@SuppressWarnings("unused")
		String[] whereArgs={s.getName()};
		database.update(FallSqlHelper.SESSION_TABLE, values, FallSqlHelper.SESSION_NAME+" = ?",new String[]{s.name});
		s.setArchived(boolArchived);

	}

	public void deleteSession(Session s){
		if(!s.isValid)throw new InvalidSessionException();

		database.delete(FallSqlHelper.SESSION_TABLE, FallSqlHelper.SESSION_NAME+" = ?", new String[]{s.name});
		int index=0;
		for(Session session:sessionList){
			if(s==session){
				sessionList.remove(index);
				s.isValid=false;
				break;
			}
			index++;
		}

		database.delete(FallSqlHelper.FALL_TABLE, FallSqlHelper.FALL_FSESSION+" = ?", new String[]{s.name});
		database.delete(FallSqlHelper.ACQUISITION_TABLE, FallSqlHelper.ACQUISITION_ASESSION+" = ?", new String[]{s.name});

	}

	private static int getSessionFallsNumber(String sessionName){return  database.rawQuery("SELECT * FROM "+FallSqlHelper.FALL_TABLE+" WHERE "+FallSqlHelper.FALL_FSESSION+" = '"+sessionName+"' ORDER BY "+FallSqlHelper.FALL_TIME+" DESC",null ).getCount();}


}

