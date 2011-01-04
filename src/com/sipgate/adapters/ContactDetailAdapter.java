
package com.sipgate.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sipgate.R;
import com.sipgate.db.ContactDataDBObject;
import com.sipgate.db.ContactNumberDBObject;
import com.sipgate.db.SipgateDBAdapter;
import com.sipgate.models.holder.ContactDetailViewHolder;

public class ContactDetailAdapter extends BaseAdapter
{
	private Activity activity = null;	
	private LayoutInflater mInflater = null;
	
	private ContactDataDBObject contactDataDBObject = null;
	private ContactDetailViewHolder holder = null;
	private ContactNumberDBObject currentContactNumberDBObject = null;
	
	private String displayName = "";
	
	public ContactDetailAdapter(Activity activity, String uuid, SipgateDBAdapter sipgateDBAdapter) 
	{
		this.activity = activity;
		
		mInflater = activity.getLayoutInflater();
		
		contactDataDBObject = sipgateDBAdapter.getContactDataDBObjectWithContactNumbersByUuid(uuid);
		
		TextView contactName = (TextView) activity.findViewById(R.id.ContactName);
	
		if (contactDataDBObject.getDisplayName() != null ||  contactDataDBObject.getDisplayName().length() > 0)
		{
			displayName = contactDataDBObject.getDisplayName();
		}
		else
		{
			displayName = contactDataDBObject.getFirstName();
			
			if (displayName.length() > 0)
			{
				displayName += " ";
			}
			
			displayName += contactDataDBObject.getLastName();
		}
		
		contactName.setText(displayName);
	}
		
	public boolean areAllItemsEnabled() 
	{
		return true;
	}
	
	public boolean isEnabled(int position) 
	{
		return true;
	}
		
	public Object getItem(int position) 
	{
		if (getCount() > position)
		{
			return contactDataDBObject.getContactNumberDBObjects().elementAt(position);
		}
	
		return null;
	}
	
	public long getItemId(int position) 
	{
		if (getCount() > position)
		{
			return contactDataDBObject.getContactNumberDBObjects().elementAt(position).getId();
		}
		
		return 0;
	}
	
	@Override
	public int getItemViewType(int position) 
	{
		return 0;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		if (convertView == null)
		{
			convertView = mInflater.inflate(R.layout.sipgate_contacts_detail_bit, null);
			
			holder = new ContactDetailViewHolder();
			holder.contactNumberType = (TextView) convertView.findViewById(R.id.ContactNumberType);
			holder.contactNumberValue = (TextView) convertView.findViewById(R.id.ContactNumberValue);

			convertView.setTag(holder);
		}
		else
		{
			holder = (ContactDetailViewHolder) convertView.getTag();
		}
		
		currentContactNumberDBObject = (ContactNumberDBObject) getItem(position);

		if (currentContactNumberDBObject != null)
		{
			String type = "";
			
			switch (currentContactNumberDBObject.getPhoneType())
			{
				case HOME:
					type = activity.getResources().getString(R.string.sipgate_phonetype_home);
					break;
				case CELL:
					type = activity.getResources().getString(R.string.sipgate_phonetype_mobile);
					break;
				case WORK:
					type = activity.getResources().getString(R.string.sipgate_phonetype_work);
					break;
				case WORK_FAX:
					type = activity.getResources().getString(R.string.sipgate_phonetype_work_fax);
					break;
				case HOME_FAX:
					type = activity.getResources().getString(R.string.sipgate_phonetype_home_fax);
					break;
				case PAGER:
					type = activity.getResources().getString(R.string.sipgate_phonetype_pager);
					break;
				case OTHER:
					type = activity.getResources().getString(R.string.sipgate_phonetype_other);
					break;
				case ASSISTANT:
					type = activity.getResources().getString(R.string.sipgate_phonetype_assistant);
					break;
				case CALLBACK:
					type = activity.getResources().getString(R.string.sipgate_phonetype_callback);
					break;
				case CAR:
					type = activity.getResources().getString(R.string.sipgate_phonetype_car);
					break;
				case COMPANY_MAIN:
					type = activity.getResources().getString(R.string.sipgate_phonetype_company_main);
					break;
				case ISDN:
					type = activity.getResources().getString(R.string.sipgate_phonetype_isdn);
					break;
				case MAIN:
					type = activity.getResources().getString(R.string.sipgate_phonetype_main);
					break;
				case MMS:
					type = activity.getResources().getString(R.string.sipgate_phonetype_mms);
					break;
				case OTHER_FAX:
					type = activity.getResources().getString(R.string.sipgate_phonetype_other_fax);
					break;
				case RADIO:
					type = activity.getResources().getString(R.string.sipgate_phonetype_radio);
					break;
				case TELEX:
					type = activity.getResources().getString(R.string.sipgate_phonetype_telex);
					break;
				case TTY_TDD:
					type = activity.getResources().getString(R.string.sipgate_phonetype_tty_tdd);
					break;
				case WORK_CELL:
					type = activity.getResources().getString(R.string.sipgate_phonetype_work_mobile);
					break;
				case WORK_PAGER:
					type = activity.getResources().getString(R.string.sipgate_phonetype_work_pager);
					break;
				default:
					type = activity.getResources().getString(R.string.sipgate_phonetype_unknown);
					break;
			}
			
			holder.contactNumberType.setText(type);
						
			holder.contactNumberValue.setText(currentContactNumberDBObject.getNumberPretty());
		}
		
		return convertView;
	}
	
	public boolean hasStableIds() 
	{
		return true;
	}

	@Override
	public boolean isEmpty()
	{
		return (contactDataDBObject.getContactNumberDBObjects().size() == 0);
	}

	@Override
	public int getCount()
	{
		return contactDataDBObject.getContactNumberDBObjects().size();
	}
}
