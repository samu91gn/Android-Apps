package pisada.fallDetector;
/**
 * scopo di questa classe � generalizzare le activity che vorranno
 * utilizzare i fragment. ogni fragment � associato a una activity di tipo FragmentCommunicator e 
 * cos� pu� chiamare il metodo switchfragment su essa per poter agire direttamente sulla UI
 * principale.
 */
import android.content.Intent;

public interface FragmentCommunicator {

	public void switchFragment(Intent i);
}
