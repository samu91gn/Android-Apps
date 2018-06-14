package fallDetectorException;

@SuppressWarnings("serial") //(non serializziamo)
public class MoreThanOneOpenSessionException extends RuntimeException{
	
	private static final String message="Tentativo di aprire una nuova session con una sessione gi� aperta";

	public MoreThanOneOpenSessionException(){
		super(message);
	}
}
