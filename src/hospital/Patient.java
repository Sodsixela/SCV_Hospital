package hospital;


public class Patient implements Runnable{
	private boolean emergency;
	private Service service;
	private long start;
	public Patient(boolean e, String service)
	{
		this.service= Hospital.getService(service);
		this.service.setPatient((this.service.getPatient())+1);
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
	public void leave()
	{
		System.out.println("The patient leave "+service.getName());
		service.setPatient(service.getPatient()-1);
	}
	
	private void goToEmergency()
	{
		boolean enter = service.checkIn();
		if(enter)
		{
			System.out.println("check in ok, "+ service.getName());
			fillPaper();
			try {
				service.nurse.acquire();
				Nurse nurse= new Nurse(service);
				nurse.process_paper();
				Room room= new Room(service);
				room.waitDoctor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		leave();
	}
	
	private void emergency()
	{
		try {
			service.room.acquire();
			Room room= new Room(service);
			room.waitDoctor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void run() {
		System.out.println("A patient is comming in "+service.getName());
		if(emergency==true)
		{
			System.out.println("emergency");
			emergency();
		}
		else 
		{
			goToEmergency();
		}
		long time = System.nanoTime() - start;
		System.out.println(time*0.000000001+ " ms");
	}
}
