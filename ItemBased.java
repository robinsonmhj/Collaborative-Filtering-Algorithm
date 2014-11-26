package com.esu.edu.recommendation.cf;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.esu.edu.Util.FileUtil;

public class ItemBased {
	
	
	private HashMap<Integer,HashMap<Integer,Double>> rates= new HashMap<Integer,HashMap<Integer,Double>>();
	private final int K=50;
	private HashMap<Integer,Double> averages = new HashMap<Integer,Double>();
	private HashMap<Integer,List<SimilarBean>> neighbors = new HashMap<Integer,List<SimilarBean>>();
	
	
	public HashMap<Integer,HashMap<Integer,Double>> getRate(){
		
		return rates;
	
	}
	
	
	public void getNeighbor(){
		
		long start=System.currentTimeMillis();
		List<SimilarBean> neighbor;
		for(int business:rates.keySet()){
			
			neighbor=getNeighbor(business);
			neighbors.put(business, neighbor);
		}
		long end=System.currentTimeMillis();
		System.out.println("get neighbor using time"+(end-start));
		
		
	}
	
	public void readRatings(String filename){
		
		
		BufferedReader in=null;
		
		try{
			in= new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			String line=in.readLine();
			
			int uid,bid;
			double rate;
			
			while(line!=null){
				
				//System.out.println(line);
				HashMap<Integer,Double> bRate= new HashMap<Integer,Double>();//key:user,value:rate
				String[] lines=line.split("	");
				uid=Integer.valueOf(lines[0]);
				bid=Integer.valueOf(lines[1]);
				rate=Integer.valueOf(lines[2]);
		
				if(rates.containsKey(bid)){
					bRate=rates.get(bid);
					bRate.put(uid,rate);
					rates.put(bid, bRate);
		
				}else{
					
					bRate.put(uid,rate);
					rates.put(bid, bRate);
					
					
				}
				
				//bRate.clear();
				
				line=in.readLine();
				
			}
			
			
			// it is used to test if all the data in the file is read correctly
			/*for(int key:rates.keySet()){
				
				for(int key2:rates.get(key).keySet())
					
					System.out.println(key+"	"+key2+"	"+rates.get(key).get(key2));
				
				
			}*/
			
			
			System.out.println("reade file Finished");
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("read file Error");
		}finally{
			try{
				in.close();
			}catch(Exception e){
				//e.printStackTrace();
				System.out.println("close file error");
			}
			
		}
		
		System.out.println("Load data done");
		
	}
	
	
	
	public void getAverage(){
		
		Map<Integer,Double> brates=null;
		
		for(int business:rates.keySet()){
			double average=0;
			double sum=0;
			int count=0;
			brates=rates.get(business);
			for(int user:brates.keySet()){
				count++;
				sum+=brates.get(user);
			}
			
			average=sum/count;
			averages.put(business, average);
		}
	}
	
	
	
public double getSimilarity(HashMap<Integer,Double> businessX, HashMap<Integer,Double> businessY){
		
		double sim=0;
		double sumX=0;
		double sumY=0;
		double sumXY=0;
		double sumX2=0;
		double sumY2=0;
		
		double rateX=0;
		double rateY=0;
		
		int same=0;//the item number of the 2 users have in common  
		for(Integer key:businessX.keySet()){
			
			if(businessY.containsKey(key)){
				
				same++;
				rateX=businessX.get(key);
				rateY=businessY.get(key);
				sumXY+=rateX*rateY;
				sumX+=rateX;
				sumY+=rateY;
				sumX2+=rateX*rateX;
				sumY2+=rateY*rateY;
			
			}
			
			
		}
		
		
		if(same<=3)
			return -9999;
		
		
		
		double ex=sumX/same;
		double ey=sumY/same;
		double ex2=sumX2/same;
		double ey2=sumY2/same;
		double exy=sumXY/same;
		
		//double preSdx=Math.abs(ex2-ex*ex);
		//double preSdy=Math.abs(ey2-ey*ey);
		
		
		double preSdx=ex2-ex*ex;
		double preSdy=ey2-ey*ey;
		
		
		
		//System.out.println("ex"+ex+"ey"+ey+"ex2"+ex2+"ey2"+ey2+"exy"+exy);
		
		if(preSdx==0||preSdy==0)
			return 0;
		//else
			//
		
		double sdx=Math.sqrt(preSdx);
		double sdy=Math.sqrt(preSdy);
		double minus=exy-ex*ey;
		//double minus=exy-ex*ey;
		//System.out.println(minus);
		
		//try{
			sim=minus/(sdx*sdy);
		//}catch(Exception e){
			
			//e.printStackTrace();
			
	//	}
		
		//System.out.println(sim);
		return sim; // the larger the 2 businesses are more similar
		
		
		
	}
	
	public List<SimilarBean> getNeighbor(int business){
	
	
	//long start=System.currentTimeMillis();
	double sim;
	List<SimilarBean> neighbor= new  ArrayList<SimilarBean>();
	HashMap<Integer,Double> userX=rates.get(business);
	HashMap<Integer,Double> userY;
	for(Integer key:rates.keySet()){
		
		if(key==business){
			continue;
	
		}
		
		userY=rates.get(key);
		sim=getSimilarity(userX,userY);
	
		if(sim<0)
			continue;
		
		
		SimilarBean similar= new SimilarBean(key,sim);
		
		
		
		if(neighbor.size()<K){
		
				neighbor.add(similar);
		
		}else{
			
			System.setProperty("java.util.Arrays.useLegacyMergeSort", "true"); 
			Collections.sort(neighbor,new Comparator<SimilarBean>(){
				
				public int compare(SimilarBean arg0, SimilarBean arg1) {
			
					 return arg0.getSim() >= arg1.getSim() ? 1 : -1;

				}
				
			});
			
			
			if(neighbor.get(0).getSim()<similar.getSim()){
				
				neighbor.remove(0);
				neighbor.add(similar);
				
			}
	
		}
	
	}
	
	//System.out.println("-------------------");
	//for(int i=0;i<neighbor.size();i++)
		//System.out.println(neighbor.get(i));
	
	//long end=System.currentTimeMillis();
	//System.out.println("find neighbor using time"+(end-start));
	return neighbor;
	
	
	}

	public HashMap<Integer,Double> getRecommendation(int user){
		
		//long start=System.currentTimeMillis();
		HashMap<Integer,Double> recommendation=new HashMap<Integer,Double>();
		
		double rate;
		
		for(int business:rates.keySet()){
			
			rate=getRecommendation(user,business);
			
			if(rate==0)
				continue;
			recommendation.put(business, rate);
	
		}
		//long end=System.currentTimeMillis();
		//System.out.println("get recommendation for user "+user+" using "+(end-start));
		return recommendation;
		
	}


	public double getRecommendation(int user,int business){
		
		List<SimilarBean> neighbor= neighbors.get(business);
		
		HashMap<Integer,Double> uRates;
        double sumTop=0;
        double sumBottom=0;
		double rate;
		double sim;
		int bId;
		for(SimilarBean bean:neighbor){
			
			bId=bean.getuId();
			sim=bean.getSim();
			uRates=rates.get(bId);
			
			if(uRates.containsKey(user)){
				rate=uRates.get(user);
				sumTop+=(rate*sim);
				sumBottom+=sim;
			}
		}
		
		if(sumBottom==0)
			return 0;
		
		return sumTop/sumBottom;
	
	}
	
	
	public static void main(String[] args){
		
		String traing="E:\\training.csv";
		String all="E:\\rates.csv";
		
		HashMap<Integer,HashMap<Integer,Double>> allMap;// used to store all the initial rate
		HashMap<Integer,HashMap<Integer,Double>> trainingMap;// used to store all the initial rate
		HashMap<Integer,HashMap<Integer,Double>> recommendations= new HashMap<Integer,HashMap<Integer,Double>>() ;//store the recommendation rates
		HashMap<Integer,Double> recommendation;//single recommendation
		HashMap<Integer,Double> init;//initial data
		
		int count=0;
		long start,end;
		
		double mae=0;
		int same=0;
		double observed;
		double predicted;
		double difference;
		
		ItemBased cf = new ItemBased();
		
		cf.readRatings(traing);
		
		cf.getAverage();
		
		cf.getNeighbor();
	
		trainingMap=cf.getRate();
		
		allMap=FileUtil.readRatings(all);
		start=System.currentTimeMillis();
		for(int user:allMap.keySet()){
			//System.out.println("user:"+user);
			recommendation=cf.getRecommendation(user);
			if(recommendation.size()==0)
				continue;
			init=allMap.get(user);
			//start=System.currentTimeMillis();
			for(int business:recommendation.keySet()){
				
				if(init.containsKey(business)){
					observed=init.get(business);
					predicted=recommendation.get(business);
					if(predicted>5||predicted<0){
						System.out.println("user "+user+",business "+business+",predicted "+predicted+",observed "+observed);
					}
					difference=Math.abs(observed-predicted);
					mae+=difference;
					same++;
					//System.out.println(business+":"+observed+","+predicted);
					//System.out.println("count"+same+",mae"+mae);
				}
	
			}
			//end=System.currentTimeMillis();
			//System.out.println("user:"+user+",Compared time:"+(end-start));
			count++;
			
			if(count%10000==0){
				end=System.currentTimeMillis();
				System.out.println(count+"done,using time"+(end-start));
				//start=end;
			}
				
			
			
			
		}
		
		
		System.out.println("MAE"+(mae/same)+",recommendations:"+same);
		
		
		
	}
	
	
}
