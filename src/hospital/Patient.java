package hospital;


public class Patient implements Runnable{
	private boolean emergency;
	private Service service;
	private long start;
	public Patient(boolean e, Service service)
	{
		this.service= service;
		this.emergency=e;
		this.start = System.nanoTime();
	}
	
	public void fillPaper()
	{
		Thread.currentThread();
		try {
			Thread.sleep((long)(Math.random() * 4000+1000));
			System.out.println("He filled the paper,"+service.getName());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	public void leave(boolean accepted)
	{
		System.out.println("The patient leave "+service.getName());
		if(accepted)
			service.setPatient(service.getPatient()-1);
	}
	
	public boolean goToEmergency()
	{
		boolean test =service.checkIn();
		if(test)
			this.service.setPatient((this.service.getPatient())+1);
		return test;
		
	}
	
	public void emergency()
	{

		this.service.setPatient((this.service.getPatient())+1);
		try {
			service.room.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void run() {
		System.out.println("A patient is comming in "+service.getName());
		boolean accepted=true;
		if(emergency==true)
		{
			System.out.println("emergency");
			emergency();
		}
		else 
		{
			accepted=goToEmergency();
			if(accepted==true)
			{
				System.out.println("check in ok, "+ service.getName());
				fillPaper();
				try {
					service.nurse.acquire();
					Nurse nurse= new Nurse(service);
					nurse.process_paper();
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		if(accepted==true)
		{
			Room room= new Room(service);
			room.waitDoctor();
			
			Doctor doctor=new Doctor(service);
			System.out.println("the doctor is here,"+service.getName());
			doctor.examine();
		}
		leave(accepted);
		long time = System.nanoTime() - start;
		System.out.println(time*0.000000001+ " ms");
	}

	public Service getService() {
		return service;
	}

	public boolean isEmergency() {
		return emergency;
	}
	
}
