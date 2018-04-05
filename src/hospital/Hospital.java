package hospital;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Hospital {
	private static ArrayList<Service> services;
	public static Semaphore openService;
	public Hospital()
	{
		services= new ArrayList<>();
		openService= new Semaphore(0,true);
		services.add(new Service("Cardiology",5,5,5));
		services.add(new Service("Neurology",3,4,4));
		services.add(new Service("Rheumatology",2,3,2));
		
		/*System.out.println(openService.availablePermits());
		openService.release();
		System.out.println(openService.availablePermits());
		try {
			openService.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(openService.availablePermits());*/
		new Thread(new Patient(false,"Cardiology")).start();
		
		services.get(0).askDoctor(true);
		
		//new Thread(new Patient(false,"Cardiology")).start();
		//new Thread(new Patient(false,"Cardiology")).start();
		new Thread(new Patient(false,"Neurology")).start();
		Thread.currentThread();
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		services.get(1).okToGive();
		//while(true);
	}
	public static Service getService(String nameService) {
		for(Service service: services)
		{
			if(service.getName().equals(nameService))
				return service;
		}
		return null;
	}
	
	public static Service getDoctor() {
		for(Service service: services)
		{
			if(service.isGive()/* && !service.getName().equals(nameService) we don't need it, normally*/)
				if(service.doctor.availablePermits()>1)//we check it if there is at least 2 doctor, to keep at least one 
					return service;
		}
		return null;
	}
	
	public static Service getRoom() {
		for(Service service: services)
		{
			if(service.isGive() /*&& !service.getName().equals(nameService)*/)
				if(service.room.availablePermits()>1)//we check it if there is at least 2 room, to keep at least one 
					return service;
		}
		return null;
	}
	
}
