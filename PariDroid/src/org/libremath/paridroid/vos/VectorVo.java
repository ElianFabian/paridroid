package org.libremath.paridroid.vos;

import java.lang.StringBuilder;

public class VectorVo {

    public int dimension;

    public String[] entries = new String[dimension];

    public VectorVo(int dimension, String[] entries) {
	this.dimension = dimension;
	this.entries = entries;
    }
    
    public String toString() {

	StringBuilder sb = new StringBuilder();
	sb.append("[");

	for(int i=0; i < dimension; i++) {
	    if (i == dimension-1) {
		sb.append(entries[i]);
	    } else {
		sb.append(entries[i]+",");
	    }
	}
	sb.append("]");
	return sb.toString();
    }
}