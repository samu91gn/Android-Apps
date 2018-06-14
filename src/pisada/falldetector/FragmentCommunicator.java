package pisada.fallDetector;
/**
 * scopo di questa classe è generalizzare le activity che vorranno
 * utilizzare i fragment. ogni fragment è associato a una activity di tipo FragmentCommunicator e 
 * così può chiamare il metodo switchfragment su essa per poter agire direttamente sulla UI
 * principale.
 */
import android.content.Intent;

public interface FragmentCommunicator {

	public void switchFragment(Intent i);
}
