package pisada.fallDetector;
/**
 * interfaccia usat per il bind tra activity-fragment e service:
 * se una activity o un fragment (o una classe in generale) implementa questa
 * interfaccia, è possibile connetterla tramite il metodo connect alla classe foregroundservice.
 * a quel punto inizierà a ricevere aggiornamenti su questi metodi implementati.
 */
import pisada.database.FallDataSource.Fall;


public interface ServiceReceiver {
	public void serviceUpdate(float x, float y, float z, long time);

	public void serviceUpdate(Fall fall, String sessionName);

	public void sessionTimeOut();
	
	public boolean equalsClass(ServiceReceiver obj);
	
	public void runOnUiThread(Runnable r);
}
