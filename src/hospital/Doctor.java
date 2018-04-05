package hospital;

public class Doctor  {
	private Service service;
	public Doctor(Service service)
	{
		this.service=service;
		
	}
	public void examine()
	{
		Thread.currentThread();
		try {
			Thread.sleep((long)(Math.random() * 6000+1500));
			System.out.println("He finished to examine,"+service.getName());
			service.room.release();
			service.doctor.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
