package pisada.fallDetector;

import java.util.concurrent.ConcurrentLinkedQueue;

import pisada.database.Acquisition;


public class ExpiringList {

	private final int EXPIRING_SIZE = ForegroundService.FALL_DURATION / ForegroundService.MAX_SENSOR_UPDATE_RATE;
	private ConcurrentLinkedQueue<Acquisition> timerAcquisitionList ; 
	
	public ExpiringList()
	{
		timerAcquisitionList = new ConcurrentLinkedQueue<Acquisition>();
	}
	
	public void enqueue(Acquisition a)
	{
		
		timerAcquisitionList.add(a);

		if(size() >= EXPIRING_SIZE)
			timerAcquisitionList.poll();
		
		
	}

	public Acquisition peek()
	{
		return timerAcquisitionList.peek();
	}
	
	
	public int size()
	{
		return this.timerAcquisitionList.size();
	}
	
	public Object[] getArray()
	{
		return this.timerAcquisitionList.toArray();
	}
	
	public ConcurrentLinkedQueue<Acquisition> getQueue(){return timerAcquisitionList;}
	
}

