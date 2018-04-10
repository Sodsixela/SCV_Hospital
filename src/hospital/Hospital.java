package hospital;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Hospital {
	private ArrayList<Service> services;
	private  Semaphore openService;
	public Hospital()
	{
		services= new ArrayList<>();
		openService= new Semaphore(0,true);
		boolean test=false;
		if(test)
		{
			addService(new Service("Cardiology",5,5,5,this));
			addService(new Service("Neurology",3,4,4,this));
			addService(new Service("Rheumatology",2,3,2,this));
			
			new Thread(new Patient(false,getService("Cardiology"))).start();
			new Thread(new Patient(false,services.get(1))).start();
			new Thread(new Patient(false,services.get(1))).start();
			new Thread(new Patient(false,services.get(0))).start();
		}
	}
	public Service getService(String nameService) {
		for(Service service: services)
		{
			if(service.getName().equals(nameService))
				return service;
		}
		return null;
	}
	
	public Service getDoctor() {
		for(Service service: services)
		{
			if(service.isGive()/* && !service.getName().equals(nameService) we don't need it, normally*/)
				if(service.doctor.availablePermits()>1)//we check it if there is at least 2 doctor, to keep at least one 
					return service;
		}
		return null;
	}
	
	public Service getRoom() {
		for(Service service: services)
		{
			if(service.isGive() /*&& !service.getName().equals(nameService)*/)
				if(service.room.availablePermits()>1)//we check it if there is at least 2 room, to keep at least one 
					return service;
		}
		return null;
	}
	
	public void addService(Service service)
	{
		services.add(service);
	}
	public ArrayList<Service> getServices() {
		return services;
	}
	public void setServices(ArrayList<Service> services) {
		this.services = services;
	}
	public Semaphore getOpenService() {
		return openService;
	}
	
}
