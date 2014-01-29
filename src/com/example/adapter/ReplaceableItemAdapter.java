package com.example.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.example.pricer.R;

public class ReplaceableItemAdapter extends ArrayAdapter<Item> {

	// declaring our ArrayList of items
	private List<ReplaceableItem> record;

	/* here we must override the constructor for ArrayAdapter
	* the only variable we care about now is ArrayList<Item> objects,
	* because it is the list of objects we want to display.
	*/
	public ReplaceableItemAdapter(Context context, int textViewResourceId, List<ReplaceableItem> objects) {
		super(context, textViewResourceId);
		this.record = objects;
	}

	/*
	 * we are overriding the getView method here - this is what defines how each
	 * list item will look.
	 */
	public View getView(int position, View convertView, ViewGroup parent){

		// assign the view we are converting to a local variable
		View v = convertView;

		// first check to see if the view is null. if so, we have to inflate it.
		// to inflate it basically means to render, or show, the view.
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.simple_list_item, null);
		}

		/*
		 * Recall that the variable position is sent in as an argument to this method.
		 * The variable simply refers to the position of the current object in the list. (The ArrayAdapter
		 * iterates through the list we sent it)
		 * 
		 * Therefore, i refers to the current Item object.
		 */
		ReplaceableItem i = record.get(position);
		
		if (i != null) {

			// This is how you obtain a reference to the TextViews.
			// These TextViews are created in the XML files we defined.

			TextView name = (TextView) v.findViewById(R.id.firstLine);
			TextView price = (TextView) v.findViewById(R.id.secondLine);

			// check to see if each individual textview is null.
			// if not, assign some text!
			if (name != null){
				name.setText(i.getName());
			}
			if (price != null){
				price.setText(i.getAttributes().get(2).getValue().toString());
			}
		}
		// the view must be returned to our activity
		return v;

	}

}