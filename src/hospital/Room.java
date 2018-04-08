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
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
