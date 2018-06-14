package pisada.fallDetector;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import pisada.database.Acquisition;

public class DetectorAlgorithm {
	/***********Parametri da testare bene, anche in funzione del rate (high funziona bene), non mi sento più spalle, braccia e gambe, mi fermo qua.*******************/
	
	static double fallPercentFirst;//percentuale di lista minima in caduta  per metodo con "orientamento"
	static double notFallPercentFirst;	//percentuale di lista  massima  di non caduta consecutiva prima di azzeramento conteggio per metodo con "orientameto"
	static double fallPercentSecond;//percentuale minima di lista in caduta  per metodo con "caduta libera"
	static double notFallPercentSecond;//percentuale  massima di lista di non caduta consecutiva prima di azzeramento conteggio per metodo con "caduta libera"

	/******************************************************************************************************************************/

	
	/*L'algoritmo è composto di due parti:
	 * - Prima parte cerca una caduta con telefono tenuto in tasca o in uno zaino ad esempio, quindi discesa meno regolare con modulo anche vicino 9.81 (gravità)
	 *   e con orientamento del telefono ruotato di "circa" 90 gradi(in realtà margine grande) tra il vetore prima della caduta e il "vettore di impatto"/"vettore medio dopo la caduta". 
	 * - Seconda parte nel caso in cui il primo metodo non dia esito positivo, opera nell'ipotesi che il telefono sia tenuto in mano e lasciato cadere 
	 *	 all'inizio della caduta. Modulo richiesto durante la discesa più basso (<4) e modulo di impatto più alto. 
	 */
	
	public static int danielAlgorithm(ExpiringList list,float[] oldVector) //Input: lista acquisizioni e vettore medio prima della caduta
	{

		
		switch(ForegroundService.MAX_SENSOR_UPDATE_RATE){
		case 10:
			fallPercentFirst= 12;
			notFallPercentFirst=5;	
			fallPercentSecond=12;
			notFallPercentSecond=2;
		case 20:
			fallPercentFirst= 8;
			notFallPercentFirst=5;	
			fallPercentSecond=7;
			notFallPercentSecond=3;
		case 30://Dubbio
			fallPercentFirst= 9;
			notFallPercentFirst=5;	
			fallPercentSecond=7;
			notFallPercentSecond=3;
		}
		
		
		boolean mFall=false;
		boolean fall=false;
		int metodo=-1;
		int i=0;
		int j=0;
		int  size= list.size();
		double maxiFirst=(size*fallPercentFirst)/100; //numero minimo di acquisizioni in caduta per metodo con "orientamento"
		double maxjFirst=(size*notFallPercentFirst)/100; //numero massimo di acquisizioni di non caduta consecutiva prima di azzeramento conteggio per metodo con "orientameto"
		double maxiSecond=(size*fallPercentSecond)/100;//numero di acquisizioni minimo in caduta  per metodo con "caduta libera"
		double maxjSecond=(size*notFallPercentSecond)/100; //numero di acquisizioni massimo di non caduta consecutiva prima di azzeramento conteggio per metodo con "caduta libera"



		ConcurrentLinkedQueue<Acquisition> queue=list.getQueue();
		/*****************METODO CON ORIENTAMENTO: discesa+impatto+orientamento ******************/
		int impactAcquisitionIndex=0; //indice lista in cui viene rilevato l'impatto per metodo con orientamento

		for(Acquisition a :queue){ //Ricerca discesa+impatto
			double module=module(a);

			if(i>maxiFirst){//superato numero minimo di acquisizioni in discesa, si cerca un impatto
				
				if(module>25){ //impatto rilevato

					mFall=true; //possibile caduta, si deve verificare orientamento
					break;


				}
			}
			else{

				if(module<9){
					i++; //acquisizione in discesa;


				}
				else{
					j++; //discesa "temporaneamente" interrotta

				}
				
				if(j>maxjFirst){ //superato numero di acquisizioni consecutive senza discesa: azzero contatori;
					j=0;
					i=0;
				}


			}
			impactAcquisitionIndex++; 

		}


		if(mFall){ //Verifica orientamento con vettore di impatto dopo impatto rilevato

			float[] impactVector=new float[3];
			int k=0;
			for(Acquisition a: queue){
				if(k==impactAcquisitionIndex){
					impactVector=acquisitionToVector(a);
					break;
				}
				k++;
			}

			double cosBetween=CosBetweenVectors( oldVector,impactVector);//Calcolo coseno angolo tra vettore prima della caduta (input metodo) e vettore impatto della caduta
			double cosRange=0.5; //forse un po' troppo grande
			metodo=2;
			if(-cosRange<cosBetween&&cosBetween<cosRange) { //Se è "vicino" a 0 (vettori perpendicolari, prodotto scalare nullo (telefono in tasca ad esempio): caduta rilevata
				fall=true;
				metodo=0;
			}


			if(!fall){//se il vettore di impatto non da esisto positivo, provo con una media dei vettori alla fine della lista (fine caduta)

				ArrayList<float[]> landingVectors=new ArrayList<float[]>();
				float[] mediumLandingVector=new float[3];
				k=0;


				for(Acquisition a: queue){
					if(size*95/100<=k){
						landingVectors.add(acquisitionToVector(a)); //Ultimo 5% della lista delle acquisizioni
					}
					k++;
				}
				mediumLandingVector=mediumVector(landingVectors); //vettore medio negli instanti finali della lista 

				cosBetween=CosBetweenVectors( oldVector,mediumLandingVector);//Calcolo coseno angolo tra vettore prima della caduta (input metodo) e media dei vettori dopo la caduta
				metodo=2;
				if(-cosRange<cosBetween&&cosBetween<cosRange) { //Se è "vicino" a 0 (vettori perpendicolari, prodotto scalare nullo: telefono in tasca ad esempio): caduta rilevata
					fall=true;
					metodo=0;
				}
			}
		}
		/****************FINE METODO ORIENTAMENTO****************/


		/****************METODO CADUTA LIBERA: discesa+impatto*******************/
		if(!fall){ //Se fallisce metodo orientamento (telefono tenuto in mano ad esempio: vettori paralleli), si ricerca una caduta libera
			i=0;j=0;
			for(Acquisition a :queue){
				double module=module(a);
				
				if(i>maxiSecond){ 
					if(module>30){//come prima, più stringente del metodo con orientamento
						fall=true; //Impatto: caduta rilevata
						metodo=1;
						break;
					}
				}
				else{
					if(module<4){//discesa, molto più stringente del metodo con orientamento
						i++; 

					}
					else{ 
						j++;

					}

					if(j>maxjSecond){  //superato numero di acquisizioni consecutive senza discesa: azzero contatori;
						j=0;
						i=0;
					}

				}


			}
		}

		/***********************FINE METODO CADUTA LIBERA***********************/


		return metodo;

	}
	
	//Vecchio tentativo di usare solo versori, problema: piccoli disturbi pesano come segnale reale. Non utilizzato
	public static float[] getVersor(float[] v){
		float[] versor=new float[3];
		float module=(float) module(v);
		versor[0]= v[0]/module;
		versor[1]= v[1]/module;
		versor[2]= v[2]/module;
		return versor;

	}

	public static float[] acquisitionToVector(Acquisition a){ 
		float[] vector={a.getXaxis(),a.getYaxis(),a.getZaxis()};
		return vector;
	}

	public static double CosBetweenVectors(float[] v1, float[] v2){
		float dotProduct= v1[0]*v2[0]+v1[1]*v2[1]+v1[2]*v2[2];
		double moduleProduct=module(v1)*module(v2);
		return dotProduct/moduleProduct;

	}


	public static float[] mediumVector(ArrayList<float[]> list){

		float xSum=0;
		float ySum=0;
		float zSum=0;

		for(float[] vector:list){

			xSum+=vector[0];
			ySum+=vector[1];
			zSum+=vector[2];
		}
		float[] vector={xSum/list.size(),ySum/list.size(),zSum/list.size()};
		return vector;
	}

	public static double module(float[] v){
		return Math.sqrt(v[0]*v[0]+v[1]*v[1]+v[2]*v[2]);
	}
	public static double module(Acquisition a){

		return	Math.sqrt(a.getXaxis()*a.getXaxis()+a.getYaxis()*a.getYaxis()+a.getZaxis()*a.getZaxis());
	}
}