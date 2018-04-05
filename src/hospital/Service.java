package hospital;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Service{
	private String name;
	private boolean give;
	public int patient;
	/*0= no need,1=need doctor, 2=need room, 3=both*/
	private int need;
	//private boolean available;
	public Semaphore doctor;
	public Semaphore nurse;
	public Semaphore room;
	public Semaphore reception;
	
	public Service(String name,int doctor, int nurse, int room)
	{
		Hospital.openService.release();
		try {
			Hospital.openService.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.name=name;
		//this.available=true;
		this.doctor=new Semaphore(doctor, true);
		this.nurse= new Semaphore(nurse, true);
		this.room = new Semaphore(room, true);
		this.give=false;
		this.need=0;
		this.reception = new Semaphore(1,true);
		this.patient=0;
		System.out.println(name +" is ready");
	}
	
	public void sendDoctor()
	{
		
	}
	public void sendRoom()
	{
		
	}
	
	public boolean checkIn()
	{
		boolean enter=false;
		try {
			this.reception.acquire();
			int i=10;
			
			if(i<=100)
				enter= true;
			this.reception.release();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return enter;
		
	}
	
	public String getName() {
		return name;
	}

	public int getPatient() {
		return patient;
	}

	public void setPatient(int patient) {
		if(patient>this.patient && give==true)
		{
			stopToGive();
		}
		this.patient = patient;
	}

	public boolean isGive() {
		return give;
	}

	public void setGive(boolean give) {
		this.give = give;
	}

	public void okToGive()
	{
		System.out.println(this.name+" can give");
		if(patient==0) 
		{
			give=true;
			Hospital.openService.release();
		}
		else give=false;
	}
	
	public void stopToGive()
	{
		give=false;
		System.out.println(this.name+" stop to give");
		try {
			Hospital.openService.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void askDoctor(boolean action)
	{
		if(action)
		{
			
			if(need ==0)
			{
				need=1;
				if(give==true)
				{
					give=false;
				}
			}
				
			else need=3;
			Thread t = new askThread(this,1);
			t.start();
		}
		else
		{
			if(need ==3)
				need=2;
			else need=0;
		}
		
	}
	
	public void askRoom(boolean action)
	{
		if(action)
		{
			if(need ==0)
			{
				need=2;
				if(give==true)
				{
					give=false;
				}
			}
			else need=3;
			Thread t = new askThread(this,1);
			t.start();
			
		}
		else
		{
			if(need ==3)
				need=1;
			else need=0;
		}
	}
	private class askThread extends Thread{
		private Service service;
		private int resource;
		public askThread(Service service,int i)
		{
			this.resource=i;
			this.service=service;
		}
		public void run() {
			System.out.println(service.getName()+" want "+resource);
			boolean get=false;
			try {
				do
				{
					get=Hospital.openService.tryAcquire(1,1000, TimeUnit.MILLISECONDS);
					if(get)
					{
						if(resource==1)
						{
							Service giver=Hospital.getDoctor();
							if(giver!=null)
							{
								System.out.println(giver.getName() +" gives a doctor to "+ service.name);
								try {
									giver.doctor.acquire();
									service.doctor.release();
									service.askDoctor(false);
								}
								catch (InterruptedException e) {
									e.printStackTrace();
								}
								
							}
							else get=false;
						}
						else if(resource==2)
						{
							Service giver=Hospital.getRoom();
							if(giver!=null)
							{
								System.out.println(giver.getName() +" gives a room to "+ service.name);
								try {
									giver.room.acquire();
									service.room.release();
									service.askRoom(false);
								}
								catch (InterruptedException e) {
									e.printStackTrace();
								}
								
							}
							else get=false;
						}
					}
				}while(((need==1 && resource==1) || (need==2 && resource==2) || (need==3)) && !get);
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
