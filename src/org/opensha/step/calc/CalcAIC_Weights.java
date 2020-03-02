package org.opensha.step.calc;

public class CalcAIC_Weights {
	
	//private double genAIC, seqAIC, spaAIC;
    private  double genWeight, seqWeight, spaWeight;
	
	public CalcAIC_Weights(){
	}	
	
	public CalcAIC_Weights(double genAIC, double seqAIC, double spaAIC) {		
		calcWeights(genAIC, seqAIC, spaAIC);
	}

	public  void calcWeights(double genAIC, double seqAIC, double spaAIC){
		double[] eDi = new double[3];
		double minAIC = genAIC;
		if (seqAIC < minAIC)
			minAIC = seqAIC;
		if (spaAIC < minAIC)
			minAIC = spaAIC;
		//TODO pls check
		//if an element is NaN, asign its weight to 0
		if(Double.isNaN(genAIC)){
			eDi[0] = 0;
		}else{
			eDi[0] = Math.exp(-0.5*(genAIC - minAIC));
		}
		if(Double.isNaN(seqAIC)){
			eDi[1] = 0;
		}else{
			eDi[1] = Math.exp(-0.5*(seqAIC - minAIC));
		}
		
		if(Double.isNaN(spaAIC)){
			eDi[2] = 0;
		}else{
			eDi[2] = Math.exp(-0.5*(spaAIC - minAIC));
		}
		
		double sum_eDi = eDi[0] + eDi[1] + eDi[2];
		genWeight = eDi[0]/sum_eDi;
		seqWeight = eDi[1]/sum_eDi;
		spaWeight = eDi[2]/sum_eDi;
	}
	
	public  double getGenWeight(){
		return genWeight;
	}
	
	public  double getSeqWeight(){
		return seqWeight;
	}
	
	public  double getSpaWeight(){
		return spaWeight;
	}

}
