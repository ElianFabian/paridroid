
package org.libremath.paridroid.vaos;

import java.lang.StringBuilder;

public class VectorVo {

    public int dimension;

    public String[] entries = new String[dimension];

    public MyVector(int dimension, String[] entries) {
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