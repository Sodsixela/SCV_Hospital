package hospital;

public class Nurse {
	private Service service;
	public Nurse(Service service)
	{
		this.service=service;
		
	}
	
	public void process_paper()
	{
		System.out.println("a nurse is processing paper");
		Thread.currentThread();
		try {
			Thread.sleep((long)(Math.random() * 5000+500));
			System.out.println("a nurse is waiting a room,"+service.getName());
			service.room.acquire();
			service.nurse.release();
			System.out.println("She has finished,"+service.getName());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
