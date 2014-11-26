package com.esu.edu.recommendation.cf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.esu.edu.Util.FileUtil;

public class UserBased {
	
	private Logger log=Logger.getLogger(UserBased.class);
	
	private HashMap<Integer,HashMap<Integer,Double>> rates= new HashMap<Integer,HashMap<Integer,Double>>() ;
	private HashMap<Integer,HashMap<Integer,Double>> trainUserRates;
	private HashMap<Integer,Double> averages =new HashMap<Integer,Double>();
	private HashMap<Integer,List<SimilarBean>> neighbors= new HashMap<Integer,List<SimilarBean>>();
	
	//private final int  K=10;// how many neighbors we use
	private final int alpha=5;
	private List<HashMap<Integer,HashMap<Integer,Double>>> folderList;
	
	
	UserBased(){
	
		File directory = new File("");
		String absoluatePath=directory.getAbsolutePath();
		System.out.println("absoluatePath"+absoluatePath);
		folderList= new ArrayList<HashMap<Integer,HashMap<Integer,Double>>>();
		readRatings(absoluatePath+"//rates.csv");
		
		divideDataSet();
		
		
	
	
	}
	
	
public void generateTrainingSet(){
		
		Map<Integer,Double> itemMap;
		int reminder;
		int size;
		int trainNo;
		int half;
		double rate;
		for(int user:rates.keySet()){
			itemMap=rates.get(user);
			size=itemMap.size();
			if(size>1){
				
				reminder=size%2;
				half=size/2;
				
				if(reminder==0){
					trainNo=half;
				}else{
					trainNo=half+1;
				}
				
				HashMap<Integer,Double> trainMap= new HashMap<Integer,Double>();
				for(int item:itemMap.keySet()){
					rate=itemMap.get(item);
					
					trainMap.put(item, rate);
					trainNo--;
					
					if(trainNo==0)
						break;
				}
				trainUserRates.put(user, trainMap);
			}
		}
		
		//testing if all the data is loaded into trainItemRate
				int total=0;
				for(int business:trainUserRates.keySet()){
					total+=trainUserRates.get(business).size();
				}
				System.out.println("Total Items:"+total);
			
		
	}
	
	
	
	
	public HashMap<Integer,List<SimilarBean>> getNeighbor(int K){
		
		System.out.println("Finding the nearest neighbors........");
		neighbors.clear();
		long start=System.currentTimeMillis();
		
		List<SimilarBean> neighbor;
		
		int count=0;
		for(int user:trainUserRates.keySet()){
			
			neighbor=getNeighbor(user,K);
			
			neighbors.put(user, neighbor);
			
			count++;
			if(count%10000==0)
				System.out.println(count+" users processed");
			
		}
		
		long end=System.currentTimeMillis();
		System.out.println("get neighbor using time"+(end-start));
		return neighbors;
		
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
				HashMap<Integer,Double> bRate= new HashMap<Integer,Double>();//key:business,value:rate
				String[] lines=line.split("	");
				uid=Integer.valueOf(lines[0]);
				bid=Integer.valueOf(lines[1]);
				rate=Integer.valueOf(lines[2]);
		
				if(rates.containsKey(uid)){
					bRate=rates.get(uid);
					if(bRate.containsKey(bid)){
						
						log.error("user "+uid+"has more than one review in business "+ bid);		
					}
					
					bRate.put(bid,rate);
					rates.put(uid, bRate);
		
				}else{
					
					bRate.put(bid,rate);
					rates.put(uid, bRate);
					
					
				}
				
				//bRate.clear();
				
				line=in.readLine();
				
			}
			
			
			// it is used to test if all the data in the file is read correctly
			/*for(int key:rates.keySet()){
				
				for(int key2:rates.get(key).keySet())
					
					System.out.println(key+"	"+key2+"	"+rates.get(key).get(key2));
				
				
			}*/
			
			
			log.info("reade file Finished");
		}catch(Exception e){
			e.printStackTrace();
			log.info("read file Error",e);
		}finally{
			try{
				in.close();
			}catch(Exception e){
				//e.printStackTrace();
				log.error("close file error",e);
			}
			
		}
		
		System.out.println("Load data done");
		
	}
	
	
	public List<SimilarBean> getNeighbor(int user,int K){
		//long start=System.currentTimeMillis();
		double sim;
		List<Integer> commonList;
		//List<HashMap<Integer,Double>> neighbor=new ArrayList<HashMap<Integer,Double>>();
		List<SimilarBean> neighbor= new  ArrayList<SimilarBean>();
		HashMap<Integer,Double> userX=trainUserRates.get(user);
		HashMap<Integer,Double> userY;
		for(Integer key:trainUserRates.keySet()){
			
			if(key==user){
				continue;
		
			}
			
			userY=trainUserRates.get(key);
			commonList=getCommonItems(user,key);
			if(commonList.size()<alpha)
				continue;
			
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
				
						 return Math.abs(arg0.getSim()) >= Math.abs(arg1.getSim()) ? 1 : -1;

					}
					
				});
				
				
				if(neighbor.get(0).getSim()<similar.getSim()){
					
					neighbor.remove(0);
					neighbor.add(similar);
					
				}
		
			}
		
		}
		
		
		//long end=System.currentTimeMillis();
		//System.out.println("get neighbor using "+(end-start));
		return neighbor;
		
		
	}
	
	public void getAverage(){
		
		Map<Integer,Double> brates=null;
		averages.clear();
		for(int key:trainUserRates.keySet()){
			double average=0;
			double sum=0;
			int count=0;
			brates=trainUserRates.get(key);
			for(int business:brates.keySet()){
				count++;
				
				sum+=brates.get(business);
				
				
			}
			
			average=sum/count;
			averages.put(key, average);
		}
		
		
		//for(int key:averages.keySet()){
			
			//System.out.println(key+":"+averages.get(key));
			
			
		//}
		
	}
	
	
	public int ratesInTesting(){
		
		int testNo,trainNo=0,total=0;
		
		
		for(int user:rates.keySet()){
			
			total+=rates.get(user).size();
		}
		
		

		for(int user:trainUserRates.keySet()){
			
			trainNo+=trainUserRates.get(user).size();
		}
		
		testNo=total-trainNo;
		
		return testNo;
		
		
	}
	
	
	public double[] getRecommendation(){
		
		HashMap<Integer,Double> itemRate;
		double[] result= new double[2];
		
		double predicted,observed,difference,sum=0,mse,same=0;
		
		for(int user:rates.keySet()){
			itemRate=getRecommendation(user);
			
			if(itemRate==null)
				continue;
			
			for(int business:itemRate.keySet()){
				
				if(rates.get(user).get(business)!=null){
					
					predicted=itemRate.get(business);
					observed=rates.get(user).get(business);
					difference=Math.pow(predicted-observed,2);
					sum+=difference;
					same++;
				}

			}

		}
		
		mse=sum/same;
		int testNo=ratesInTesting();
		double coverage=(double)same/(double)testNo;
		System.out.println("MAE:"+mse+",total recommendation:"+same);
		System.out.println("Total rates in the testing dataset:"+testNo);
		System.out.println("Coverage rate:"+coverage);
		
		result[0]=mse;
		result[1]=coverage;
		
		return result;
		
	}
	
	
	public HashMap<Integer,Double> getRecommendation(int user){
		
		int neighborId;
		int neighbor2Id;
		double sim;
		Map<Integer,Double> nbrate=null;// neighbor rates
		Map<Integer,Double> nbrate2=null;// neighbor rates
		Map<Integer,Double> brate=trainUserRates.get(user);
		List<SimilarBean> neighbor= neighbors.get(user);
		HashMap<Integer,Double> recommendation= new HashMap<Integer,Double>();
		double initAverage=averages.get(user); //the average value of the user you want to recommend
		//System.out.println("average"+initAverage);
		for(int i=0;i<neighbor.size();i++){
			
			neighborId=neighbor.get(i).getuId();
			
			nbrate=trainUserRates.get(neighborId);
			
			for(int business:nbrate.keySet()){
				
				if(!brate.containsKey(business)&&!recommendation.containsKey(business)){
					
					double sum1=0; // 
					double sum2=0; //the sum of sim of all the neighbors 
					double rate=0;// recommendation rate
					
					for(int j=0;j<neighbor.size();j++){
						
						neighbor2Id=neighbor.get(j).getuId();
						sim=neighbor.get(j).getSim();
						if(sim<0)
							continue;
						nbrate2=trainUserRates.get(neighbor2Id);
						
						if(nbrate2.containsKey(business)){
							//System.out.println("Average"+neighbor2Id+"is"+averages.get(neighbor2Id));
							if(sim>0){
								sum1+=sim*(nbrate2.get(business)-averages.get(neighbor2Id));
							}else{
								sum1+=sim*(nbrate2.get(business)+averages.get(neighbor2Id));
							}
							
							sum2+=Math.abs(sim);
							//sum1+=sim*(nbrate2.get(business));
							///sum2+=sim;
						
						}
					
					}
					
					if(sum2==0)
						continue;
					rate=initAverage+sum1/sum2;
					//rate=sum1/sum2;
					if(rate<1)
						rate=1;
					else if(rate>5)
						rate=5;
						
						
					recommendation.put(business, rate);
				}
				
				
				
				
			}
			
		
		}
		
		return recommendation;
		
		/*//System.out.println("Recommendation-------------------------------");
		System.out.println("User "+user);
		for(int key:recommendation.keySet()){
			
			System.out.print(key+":"+recommendation.get(key)+",");
			
			
		}
		System.out.println("\n");*/
		
	}
	
	
	
	public double getSimilarity(HashMap<Integer,Double> userX, HashMap<Integer,Double> userY){
		//long start=System.currentTimeMillis();
		double sim=0;
		double sumX=0;
		double sumY=0;
		double sumXY=0;
		double sumX2=0;
		double sumY2=0;
		
		double rateX=0;
		double rateY=0;
		
		int same=0;//the item number of the 2 users have in common  
		for(Integer key:userX.keySet()){
			
			if(userY.containsKey(key)){
				
				same++;
				rateX=userX.get(key);
				rateY=userY.get(key);
				sumXY+=rateX*rateY;
				sumX+=rateX;
				sumY+=rateY;
				sumX2+=rateX*rateX;
				sumY2+=rateY*rateY;
			
			}
			
			
		}
		
		
		/*if(same<=3)
			return -9999;*/
		
		
		
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
			return 1;
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
			
		//long end=System.currentTimeMillis();
		//System.out.println("get similarity using time"+(end-start));
		return sim; // the less the 2 users are more similar
		
		
		
	}
	
	public List<Integer> getCommonItems(int user1,int user2){
		
		//int count=0;
		List<Integer> list= new ArrayList<Integer>();
		HashMap<Integer,Double> rateMap1=trainUserRates.get(user1);
		HashMap<Integer,Double> rateMap2=trainUserRates.get(user2);
		
		if(rateMap1!=null&&rateMap2!=null){
			for(int business:rateMap1.keySet()){
				
				
				if(rateMap2.containsKey(business))
					list.add(business);
			
			}
			
			
		}
		
		return list;
	}
	
	
	
	
	public static void main(String[] args){
		
		
	
		
		UserBased ub= new UserBased();
		
		ub.crossValidation();
		
		
		
	
		
		
	}
	
	
	public void divideDataSet(){
		
		Map<Integer,Double> itemMap;
		int reminder;
		int size,count;
		int trainNo;
		int half;
		double rate;
		
		for(int user:rates.keySet()){
			itemMap=rates.get(user);
			size=itemMap.size();
			List<HashMap<Integer,Double>> businessMapList= new ArrayList<HashMap<Integer,Double>>();
			
			count=0;
			for(int business:itemMap.keySet()){
				
				rate=itemMap.get(business);
				reminder=count%10;
				if(businessMapList.size()>reminder){
					businessMapList.get(reminder).put(business, rate);
				}else{
					HashMap<Integer,Double> bMap= new HashMap<Integer,Double>();
					bMap.put(business, rate);
					businessMapList.add(reminder, bMap);
				}
				
				count++;
			}
			
			for(int i=0;i<businessMapList.size();i++){
				if(folderList.size()>i){
					folderList.get(i).put(user, businessMapList.get(i));
				}else{
					HashMap<Integer,HashMap<Integer,Double>> uMap= new HashMap<Integer,HashMap<Integer,Double>>();
					uMap.put(user, businessMapList.get(i));
					folderList.add(i,uMap);
				}
				
			}
			
			
		
	}
	
	
	
	}
	
public void crossValidation(){
		
	for(int K=55;K<=100;K+=5){
		System.out.println("================================================");
		System.out.println("K="+K);

		double mseSum=0,coverageSum=0;
		int count=0;
		HashMap<Integer,Double> businessMap;
		HashMap<Integer,HashMap<Integer,Double>> tmpRates;
		double[] results;
		for(int i=0;i<folderList.size();i++){
			System.out.println("The "+i+" round!");
			trainUserRates= new HashMap<Integer,HashMap<Integer,Double>>();
			for(int j=0;j<folderList.size();j++){
				
				if(j!=i){	
					tmpRates=folderList.get(j);
					for(int user:tmpRates.keySet()){
						if(trainUserRates.containsKey(user)){
							businessMap=trainUserRates.get(user);
							for(int business:tmpRates.get(user).keySet()){
								double rate=tmpRates.get(user).get(business);
								businessMap.put(business, rate);
							}
							//trainUserRates.put(user, businessMap);
						}else{
							for(int u1:tmpRates.keySet()){
								// attention if you want put something in a new place,
								// new a hashmap and put all the thing into it
								businessMap= new HashMap<Integer,Double>();
								businessMap.putAll(tmpRates.get(u1));
								trainUserRates.put(u1, businessMap);
							}
						}
					}
					
					
				}
			}
			// do the testing
			getAverage();
			
			getNeighbor(K);
			
			results=getRecommendation();
			mseSum+=results[0];
			coverageSum+=results[1];
			count++;
		}
		
		System.out.println("Average MSE:"+mseSum/count);
		System.out.println("Average Coverage:"+coverageSum/count);
		
	}
	
	
}
	

}
