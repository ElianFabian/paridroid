package org.libremath.paridroid.vos;

import java.lang.StringBuilder;

public class PolynomialVo {

    public int degree;
    
    public String[] coeffs = new String[degree];

    public String variable;

    public PolynomialVo(int degree, String[] coeffs, String variable) {
	this.degree = degree;
	this.coeffs = coeffs;
	this.variable = variable;
    }

    public PolynomialVo(int degree, String[] coeffs) {
	this.degree = degree;
	this.coeffs = coeffs;
	this.variable = "x";
    }

    public PolynomialVo(String[] coeffs) {
	this.degree = coeffs.length;
	this.coeffs = coeffs;
	this.variable = "x";
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
		sb.append("+(" + coeffs[i] + "*" + variable + "^" + i + ")");
	    }
	}
	
	String poly = sb.toString();
	return poly;
    }
}