package fallDetectorException;

@SuppressWarnings("serial") //(non vogliamo serializzare)
public class InvalidSessionException extends RuntimeException{
	private static final String message="Sessione non valida";

	public InvalidSessionException(){
		super(message); 
	}
	
	public InvalidSessionException(String message){
		super(message); 
	}
}
