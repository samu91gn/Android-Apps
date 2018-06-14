package pisada.fallDetector;

import java.util.List;
import java.util.Scanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomAdapterContacts extends ArrayAdapter<String> {

    public CustomAdapterContacts(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public CustomAdapterContacts(Context context, int resource, List<String> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.itemlistrow, parent, false);
        }

        String p = getItem(position);

        if (p != null) {
            TextView tt1 = (TextView) v.findViewById(R.id.contact);
            ImageView sms = (ImageView) v.findViewById(R.id.smsThumb);
            ImageView email = (ImageView) v.findViewById(R.id.emailThumb);
            if (tt1 != null) {
            	Scanner scan = new Scanner(p);
            	String text = scan.nextLine() + "\n" + scan.nextLine() + "\n" + scan.nextLine();
                tt1.setText(text);
                scan.close();
            }
            if(p.contains("sendsm"))
            	sms.setVisibility(View.VISIBLE);
            else
            	sms.setVisibility(View.INVISIBLE);
            if(p.contains("sendem"))
            	email.setVisibility(View.VISIBLE);
            else
            	email.setVisibility(View.INVISIBLE);

        }

        return v;
    }

}
