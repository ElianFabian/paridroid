
import java.lang.StringBuilder;

package org.libremath.paridroid.vos;

public class PolynomialVo {

    public int degree;
    
    public String[] coeffs = new String[degree];

    public PolynomialVo(int degree, String[] coeffs) {
	this.degree = degree;
	this.coeffs = coeffs;
    }

    public PolynomialVo(String[] coeffs) {
	
    }

    public int getDegree() {
	return this.degree;
    }
    
    public String[] getCoeffs() {
	return this.coeffs;
    } 

    public String toString() {

	StringBuilder sb = new StringBuilder();
	sb.append(coeffs[0]);

	for(int i=1; i < degree; i++) {
	    if(coeffs[i].equals("0")) {
		sb.append("");
	    } else {
		sb.append("+"+coeffs[i]+"*x^"+i);
	    }
	}
	
	String poly = sb.toString();
	return poly;
    }