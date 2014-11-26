package com.esu.edu.recommendation.cf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SimilarBean {
	
	
	private int uId;
	private double sim;
	
	
	public SimilarBean(int uId,double sim){
		
		this.uId=uId;
		this.sim=sim;
	
	}

	
	

	public int getuId() {
		return uId;
	}


	@Override
	public String toString() {
		return "SimilarBean [uId=" + uId + ", sim=" + sim + "]";
	}




	public void setuId(int uId) {
		this.uId = uId;
	}


	public double getSim() {
		return sim;
	}


	public void setSim(double sim) {
		this.sim = sim;
	}
	
	
	public static void main(String[] args){
		
		
		List<SimilarBean> neighbor= new ArrayList<SimilarBean>();
		
		SimilarBean s1= new SimilarBean(1,0.998);
		SimilarBean s2= new SimilarBean(10,1.998);
		SimilarBean s3= new SimilarBean(8,0.01);
		SimilarBean s4= new SimilarBean(3,0.222);
		SimilarBean s5= new SimilarBean(16,2.998);
		
		neighbor.add(s1);
		neighbor.add(s2);
		neighbor.add(s3);
		neighbor.add(s4);
		neighbor.add(s5);
		
		
		for(int i=0;i<neighbor.size();i++)
			System.out.println(neighbor.get(i));
		
		
		
		Collections.sort(neighbor,new Comparator<SimilarBean>(){
			
			public int compare(SimilarBean arg0, SimilarBean arg1) {
		
				 return arg0.getSim() > arg1.getSim() ? 1 : -1;

			}
			
		});
		
		
		System.out.println("-----------------------------------------------");
		for(int i=0;i<neighbor.size();i++)
			System.out.println(neighbor.get(i));
		
		System.out.println("-----------------------------------------------");
		
		SimilarBean s6= new SimilarBean(100,0.19);
		neighbor.remove(0);
		neighbor.add(s6);
		for(int i=0;i<neighbor.size();i++)
			System.out.println(neighbor.get(i));
		
	}
	
	
}
