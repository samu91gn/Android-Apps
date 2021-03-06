package pisada.fallDetector;
/**
 * utility - metoti di appoggio vari
 */
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.location.LocationManager;
import android.provider.Settings;
import android.widget.Toast;
@SuppressLint("SimpleDateFormat")
public class Utility {

	public final static String FALL_TIME_KEY = "fall_time";
	public final static String SESSION_NAME_KEY = "session_name";
	private static ArrayList<Entry> mapp=new ArrayList<Utility.Entry>();

	public static String checkLocationServices(final Context context, boolean showDialog)
	{


		LocationManager lm = null;
		boolean gps_enabled = false,network_enabled = false;
		if(lm==null)
			lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		try{	
			gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		}catch(IllegalArgumentException ex){
			Toast.makeText(context, "Can't get location from GPS", Toast.LENGTH_SHORT).show();
		}
		try{
			network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		}catch(IllegalArgumentException ex){
			Toast.makeText(context, "Can't get location from network provider", Toast.LENGTH_SHORT).show();

		}

		if(!gps_enabled && !network_enabled && showDialog){
			AlertDialog.Builder dialog = new AlertDialog.Builder(context);
			dialog.setMessage("GPS is not enabled");
			dialog.setPositiveButton("Open settings", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface paramDialogInterface, int paramInt) {
					Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					context.startActivity(myIntent);
				}
			});
			dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface paramDialogInterface, int paramInt) {

				}
			});

			dialog.show();

		}
		if(gps_enabled)
			return LocationManager.GPS_PROVIDER;
		if(network_enabled)
			return LocationManager.NETWORK_PROVIDER;
		return null;
	}


	public static int randInt(int min, int max) {
		Random rand = new Random();
		int randomNum = rand.nextInt((max - min) + 1) + min;
		return randomNum;
	}

	public static int randomizeToColor(double d)
	{
		if(d <= 255)
			return randInt(0, (int)d);
		else
		{
			return randomizeToColor(d/randInt(1, 3));
		}
	}

	public static String getStringTime(long timeMillis)
	{
		final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy_hh:mm:ss");
		// milliseconds to date 
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeMillis);
		Date date = calendar.getTime();
		return formatter.format(date);

	}

	public static String getStringHour(long timeMillis)
	{
		final SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss");
		// milliseconds to date 
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeMillis);
		Date date = calendar.getTime();
		return formatter.format(date);

	}
	public static String getStringDate(long timeMillis)
	{
		final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		// milliseconds to date 
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeMillis);
		Date date = calendar.getTime();
		return formatter.format(date);

	}

	public static String getMapsLink(double lat, double lng){
		if(lat == -1 || lng == -1)
			return null;
		return "https://www.google.com/maps/@" + lat+"," + lng + ",13z";
	}

	
	//creazione icona sessione
	public static Bitmap createImage(int sessionNumber){
		sessionNumber=sessionNumber%5000; //5000 icone diverse
		sessionNumber+=6;
	
		
		ArrayList<int[]>  primes=getPrimesList(sessionNumber);//prendo la fattorizzazione in primi
		Bitmap icon=Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
		icon.eraseColor(Color.TRANSPARENT);
		Canvas canvas=new Canvas(icon);
		Paint paint=new Paint();
		paint.setAntiAlias(true); //aggiunta per depixellare e rendere pi� "smooth" le forme
		paint.setStyle(Style.FILL);
		int[] colors={Color.CYAN,Color.GREEN,Color.MAGENTA,Color.YELLOW,Color.BLUE,Color.RED,Color.BLACK, Color.DKGRAY};

		for(int i=0;i<primes.size();i++){
			int prime=primes.get(i)[0];//numero primo
			int exp=primes.get(i)[1];//esponente del primo
			paint.setColor(colors[(int)((Math.pow(prime, exp)*5+7)%11)%8]);//hashing sull'array di colori
			canvas.drawPath(getPolygon(prime*exp,new Point(100,100),Math.max(50,100/exp), prime*exp%2==0&&prime*exp>7), paint);//disegno il poligono

		}

		//se � primo, otterrei un solo poligono. Per evitare questo, disegno sopra anche l'icona di sessionnumber+15 (pari, non primo), scalato di 0,66
		if(isPrime(sessionNumber)){
			primes=getPrimesList(sessionNumber+15);
			for(int i=0;i<primes.size();i++){
				int prime=primes.get(i)[0];
				int exp=primes.get(i)[1];
				paint.setColor(colors[(int)((Math.pow(prime, exp)*5+7)%11)%8]);
				canvas.drawPath(getPolygon(prime*exp,new Point(100,100),(float)(Math.max(50,100/exp)/1.5), prime*exp%2==0&&prime*exp>7), paint);

			}
		}
		return icon;
	}

	//ritorna la fattorizzazione in primi dell'input ordinandoli per un risultato migliore dell'immagine
	private static ArrayList<int[]> getPrimesList(int sessionNumber){
		boolean keyFound=false;
	
		ArrayList<int[]> primes=new ArrayList<int[]>();
		int  size=0;
		for(Entry e: mapp){ //cerco nella "mappa"
			if(e.key==sessionNumber){
				primes=e.primes;
				size=primes.size();
				keyFound=true;
				break;
			}
		}

		 //Non � mai stato calcolato:calcolo la fattorizzazione e la inserisco nella mappa per accessi futuri
		if(!keyFound){
			primes=getPrimes(sessionNumber);
			size=primes.size();
			Collections.sort(primes, new Comparator<int[]>(){

				@Override
				public int compare(int[] prime1, int[] prime2) {
					double res=-(double)1/(double)prime2[1]-(double)1/(double)prime1[1];
					if(res<0) return -1;
					if(res>0)return 1;
					else return 0;
				}

			});


			ArrayList<int[]> tmp=new ArrayList<int[]>();

			for(int firstIndex=0,lastIndex=0;lastIndex<primes.size();lastIndex++){
				int[] prime=primes.get(lastIndex);
				tmp.add(prime);

				if(lastIndex+1==size||prime[1]!=primes.get(lastIndex+1)[1]){
					Collections.sort(tmp, new Comparator<int[]>(){

						@Override
						public int compare(int[] prime1, int[] prime2) {
							// TODO Auto-generated method stub
							return prime2[1]*prime2[0]- prime1[1]*prime1[0];
						}

					});

					for(int[] p: tmp){
						primes.set(firstIndex++, p);
					}
					tmp=null;
					tmp=new ArrayList<int[]>();
				}
			}
			mapp.add(new Entry(sessionNumber, primes));
		}
		return primes;

	}
	
	//ritorna fattorizzazione in primi dell'input, non ordinati
	private static ArrayList<int[]> getPrimes(int sNumber){


		ArrayList<int[]> factors=new ArrayList<int[]>();
		if(sNumber==1){
			int[] tmp={1,1};
			factors.add(tmp);
			return factors;
		}

		for(int i=1;i<=sNumber;i++){
			if(!isPrime(i)||sNumber%i!=0)continue;
			int j=1;
			int pow=i;
			while(sNumber%pow==0){
				pow*=i;
				if(sNumber%pow!=0) break;
				else j++;
			}
			int[] fact={i,j};
			factors.add(fact);
		}

		return factors;


	}


	private static boolean isPrime(int n) {

		boolean prime = true;
		if(n==1)return false;
		if(n==2)return true;
		double  sqrt=Math.sqrt(n);
		if(n%2==0)return false;
		for (long i = 3; i <= sqrt; i += 2)

			if (n % i == 0) {

				prime = false;

				break;

			}
		return prime;
	}

	//ritorna un polygono in un Path
	private static Path getPolygon(int n, Point startPoint, float dimension, boolean star){

		float startX=startPoint.x;
		float startY=startPoint.y;
		float deg=(float) (2*Math.PI/((float) n));
		ArrayList<float[]> points=new ArrayList<float[]>();
		for(int i=0; i<n;i++){

			float[] p=new float[2];
			if(star){
				if(i%2==0){
					p[0]=((float)(startX+ Math.sin(deg*i)*dimension));
					p[1]=(float)(startY-Math.cos(deg*i)*dimension);
				}
				else{
					p[0]=((float)(startX+ Math.sin(deg*i)*dimension/2.0));
					p[1]=(float)(startY-Math.cos(deg*i)*dimension/2.0);
				}
			}
			else{
				p[0]=((float)(startX+ Math.sin(deg*i)*dimension));
				p[1]=(float)(startY-Math.cos(deg*i)*dimension);
			}
			points.add(p);
		}
		Path path=new Path();
		path.moveTo(points.get(0)[0],points.get(0)[1]);
		for(int i=1;i<n;i++){
			path.lineTo(points.get(i)[0],points.get(i)[1]);
		}

		return path;
	}

	public static String longToDuration(long l)
	{
		String duration = "";
		int days = (int)(TimeUnit.MILLISECONDS.toDays(l));
		int hours = (int)(TimeUnit.MILLISECONDS.toHours(l) - TimeUnit.DAYS.toHours(days));
		int minutes = (int)(TimeUnit.MILLISECONDS.toMinutes(l) - TimeUnit.HOURS.toMinutes(hours) - TimeUnit.DAYS.toMinutes(days));
		int seconds = (int)(TimeUnit.MILLISECONDS.toSeconds(l)- TimeUnit.MINUTES.toSeconds(minutes) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.DAYS.toSeconds(days));

		if(days != 0)
			duration += days + " days,";
		if(hours != 0)
			duration += hours + " hrs, ";
		duration += minutes + " min, " + seconds + " sec";
		return duration;
	}



//Gestione fattorizzazioni gi� calcolate almeno una volta
	private  static  class Entry{
		int key;
		ArrayList<int[]> primes;

		public Entry(int key, ArrayList<int[]> primes){
			this.key=key;
			this.primes=primes;
		}


	}

}
