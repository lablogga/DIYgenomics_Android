package com.melanieswan.pg.utils;

import java.util.Comparator;
import com.melanieswan.pg.MappingItem;
import com.melanieswan.pg.Variant;


public class ConditionVariantTableRowComparator implements
		Comparator<MappingItem> {

	@Override
	public int compare(MappingItem row1,
					   MappingItem row2) 
	{

		Variant variant1 = row1.getVariant();
		Variant variant2 = row2.getVariant();
		
		if (variant1 == null || variant2 == null)	return 0;
		
		return variant1.compareTo(variant2);
	}

}
