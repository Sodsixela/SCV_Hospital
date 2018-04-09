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
	private int nbDoctor;
	private int nbNurse;
	private int nbRoom;
	public Semaphore doctor;
	public Semaphore nurse;
	public Semaphore room;
	public Semaphore reception;
	private Hospital hospital;
	
	public Service(String name,int doctor, int nurse, int room,Hospital hospital)
	{
		this.hospital=hospital;
		
		this.name=name;
		//this.available=true;
		this.nbDoctor=doctor;
		this.nbNurse=nurse;
		this.nbRoom=room;
		this.doctor=new Semaphore(doctor, true);
		this.nurse= new Semaphore(nurse, true);
		this.room = new Semaphore(room, true);
		this.give=false;
		this.need=0;
		this.reception = new Semaphore(1,true);
		this.patient=0;
		System.out.println(name +" is ready");
	}
	public boolean checkIn()
	{
		boolean enter=true;
		try {
			this.reception.acquire();
			int busyNurse;
			/*nurse and room can be used in the same time by one patient, we can take only the busiest*/
			if(nbNurse<nbRoom)
				busyNurse=nbNurse;
			else
				busyNurse=nbRoom;
			int busyDoctor;
			/*same with doctor and room*/
			if(nbDoctor<nbRoom)
				busyDoctor=nbDoctor;
			else
				busyDoctor=nbRoom;
			/*Doctor and nurse can work simultaneously + patient has to fill paper: 3 tasks in the same time, patient/3
			 * we give twice what they can as limits to know if they are too busy
			 * then again/2
			 * */
			if((float)busyDoctor<(float)(patient/3)/2 || (float)busyNurse<(float)(patient/3)/2)
				enter= false;
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
			hospital.getOpenService().release();
		}
		else give=false;
	}
	
	public void stopToGive()
	{
		give=false;
		System.out.println(this.name+" stop to give");
		try {
			hospital.getOpenService().acquire();
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
			Thread t = new askThread(this,2);
			t.start();
			
		}
		else
		{
			if(need ==3)
				need=1;
			else need=0;
		}
	}
	
	
	public int getNbDoctor() {
		return nbDoctor;
	}
	public void setNbDoctor(int nbDoctor) {
		this.nbDoctor = nbDoctor;
	}
	public int getNbRoom() {
		return nbRoom;
	}
	public void setNbRoom(int nbRoom) {
		this.nbRoom = nbRoom;
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
					get=hospital.getOpenService().tryAcquire(1,1000, TimeUnit.MILLISECONDS);
					if(get)
					{
						if(resource==1)
						{
							Service giver=hospital.getDoctor();
							if(giver!=null)
							{
								System.out.println(giver.getName() +" gives a doctor to "+ service.name);
								try {
									giver.doctor.acquire();
									giver.setNbDoctor(giver.getNbDoctor()-1);
									service.doctor.release();
									service.setNbDoctor(service.getNbDoctor()+1);
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
							Service giver=hospital.getRoom();
							if(giver!=null)
							{
								System.out.println(giver.getName() +" gives a room to "+ service.name);
								try {
									giver.room.acquire();
									giver.setNbRoom(giver.getNbRoom()-1);
									service.room.release();
									service.setNbRoom(service.getNbRoom()+1);
									service.askRoom(false);
								}
								catch (InterruptedException e) {
									e.printStackTrace();
								}
								
							}
							else get=false;
						}
						hospital.getOpenService().release();
					}
				}while(((need==1 && resource==1) || (need==2 && resource==2) || (need==3)) && !get);
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
