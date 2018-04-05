package hospital;

public class Room {
	private Service service;
	public Room(Service service)
	{
		this.service=service;
		
	}
	
	public void waitDoctor()
	{
		System.out.println("The patient wait the doctor in the room,"+ service.getName());
			
		try {
			service.doctor.acquire();
			Doctor doctor=new Doctor(service);
			System.out.println("the doctor is here,"+service.getName());
			doctor.examine();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
