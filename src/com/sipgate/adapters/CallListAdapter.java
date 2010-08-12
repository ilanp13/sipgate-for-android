package com.sipgate.adapters;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.sipgate.models.SipgateCallData;
import com.sipgate.R;

public class CallListAdapter implements ListAdapter {
	public static final Integer VIEW_TYPE = 1;
	private ArrayList<SipgateCallData> inboxData = null;
	private LayoutInflater mInflater;
	private final Context context;

	public CallListAdapter(Context context, ArrayList<SipgateCallData> inboxData) {
		super();

		this.inboxData = inboxData;
		
		this.context = context;

		// Cache the LayoutInflate to avoid asking for a new one each time.
		mInflater = LayoutInflater.from(this.context);
	}

	
	public boolean areAllItemsEnabled() {
		return false;
	}

	
	public boolean isEnabled(int position) {
		return false; // TODO what does this do?
	}

	
	public int getCount() {
		// TODO Auto-generated method stub
		return inboxData.size();
	}

	
	public Object getItem(int position) {
		return null;
	}

	
	public long getItemId(int position) {
		return position;
	}

	
	public int getItemViewType(int position) {
		return VIEW_TYPE;
	}

	static class ViewHolder {
		public Context context;
		public ImageView callButton;
		public TextView callerName;
		public TextView callerNumberPretty;
		public TextView dateTime;
		public int position;
		public String callerNumberE164;
	}

	
	public View getView(int position, View convertView, ViewGroup parent) {
		// A ViewHolder keeps references to children views to avoid unneccessary
		// calls to findViewById() on each row.
		ViewHolder holder;

		// When convertView is not null, we can reuse it directly, there is no
		// need to reinflate it. We only inflate a new View when the convertView
		// supplied by ListView is null.
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.sipgate_call_list_bit, null);

			// Creates a ViewHolder and store references to the two children
			// views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.callerName = (TextView) convertView.findViewById(R.id.CallerNameTextView);
			holder.callerNumberPretty = (TextView) convertView.findViewById(R.id.CallerNumberTextView);
			holder.dateTime = (TextView) convertView.findViewById(R.id.DateTimeTextView);
			holder.callButton = (ImageView) convertView.findViewById(R.id.CallImageButton);

			// convertView.setLongClickable(true);
			// convertView.setOnLongClickListener(new
			// RecordListLongClickListener(this));

			convertView.setTag(holder);
		} else {
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}

		// Bind the data efficiently with the holder.
		SipgateCallData inboxItem = this.inboxData.get(position);

		if (inboxItem.getCallSourceNumberE164() != null && inboxItem.getCallSourceNumberE164().length() > 0) {
			holder.callButton.setImageResource(R.drawable.button_call);
			holder.callButton.setClickable(true);
		} else {
			holder.callButton.setClickable(false);
		}

		holder.callerNumberPretty.setText(inboxItem.getCallSourceNumberPretty());
		holder.callerNumberE164 = inboxItem.getCallSourceNumberE164();

		holder.dateTime.setText(this.dateIsoToPretty(inboxItem.getCallTime()));

		String name = inboxItem.getCallSourceName();
		if (name != null && name.length() > 0) {
			holder.callerName.setText(name);
		} else {
			holder.callerName.setText(holder.callerNumberPretty.getText());
		}

		holder.position = position;

		// "remember" reference to data via holder
		holder.callButton.setTag(holder);

		// register "call" event
		holder.callButton.setClickable(true);
		holder.callButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ViewHolder holder = (ViewHolder) v.getTag();
				String uri = holder.callerNumberE164;
				Intent intent = new Intent(Intent.ACTION_CALL);
				intent.setData(Uri.parse(uri));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				v.getContext().startActivity(intent);
			}
		});

		return convertView;
	}

	private CharSequence dateIsoToPretty(String dateTimeIso) {
		SimpleDateFormat dateformatterPretty = new SimpleDateFormat(context.getResources().getString(R.string.dateTimeFormat));
		SimpleDateFormat dateformatterIso = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss"); // TODO fixme!
		Date d = dateformatterIso.parse(dateTimeIso, new ParsePosition(0));
		return dateformatterPretty.format(d);
	}

	public int getViewTypeCount() {
		return 1 + this.getCount();
	}

	
	public boolean hasStableIds() {
		return false;
	}

	
	public boolean isEmpty() {
		return this.inboxData.isEmpty();
	}

	
	public void registerDataSetObserver(DataSetObserver observer) {
	}

	
	public void unregisterDataSetObserver(DataSetObserver observer) {
	}

}
