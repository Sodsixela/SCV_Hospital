package test;


import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import org.junit.Assert;

import hospital.Hospital;
import hospital.Service;
import hospital.Nurse;
import hospital.Room;
import hospital.Doctor;
import hospital.Patient;

public class JUnitTest {

	Hospital hospital;
	Nurse n;
	Doctor d;
	Room r;
	@Before
	public void setUp() throws Exception{
		hospital = new Hospital();
		hospital.addService(new Service("Cardiology",5,5,5,hospital));
		hospital.addService(new Service("Neurology",3,4,4,hospital));
		hospital.addService(new Service("Rheumatology",2,3,2,hospital));
	}
	
	@After
	public void tearDown() throws Exception {
		hospital = null;
		System.out.println("\n");
	}
	
	@Test
	public void test() throws InterruptedException{
		n= new Nurse(hospital.getServices().get(0));
		r= new Room(hospital.getServices().get(0));
		d=new Doctor(hospital.getServices().get(0));
		/* 0 patient before*/
		int oldnbPatient=hospital.getService("Cardiology").getPatient();
		/*we take 1 patient in the normal way*/
		Patient p=new Patient(false,hospital.getService("Cardiology"));
		/*now there is 1 patient*/
		Assert.assertEquals(oldnbPatient+1,hospital.getService("Cardiology").getPatient() );
		
		System.out.println("A patient is comming in Cardiology");
		/*true if check in ok*/
		Assert.assertTrue(p.goToEmergency());
		
		p.fillPaper();
		
		/*At least one nurse is in the service*/
		Assert.assertNotEquals(0,hospital.getServices().get(0).nurse.availablePermits() );
		
		/*A nurse take the patient*/
		hospital.getServices().get(0).nurse.acquire();
		
		/*one nurse is used, 4 free instead of 5 before*/
		Assert.assertEquals(4,hospital.getServices().get(0).nurse.availablePermits() );
		/*at least one room is free*/
		Assert.assertNotEquals(0,hospital.getServices().get(0).room.availablePermits() );
		n.process_paper();
		/*one of the 5 room is used*/
		Assert.assertEquals(4,hospital.getServices().get(0).room.availablePermits() );
		/*the nurse is now free*/
		Assert.assertEquals(5,hospital.getServices().get(0).nurse.availablePermits() );
		
		/*at least one doctor is free*/
		Assert.assertNotEquals(0,hospital.getServices().get(0).doctor.availablePermits() );
		r.waitDoctor();
		/*one of the 5 doctor is used*/
		Assert.assertEquals(4,hospital.getServices().get(0).doctor.availablePermits() );
		
		d.examine();
		
		/*Doctor and room are now free*/
		Assert.assertEquals(5,hospital.getServices().get(0).doctor.availablePermits() );
		Assert.assertEquals(5,hospital.getServices().get(0).room.availablePermits() );
		
		p.leave();
		/*the patient is not anymore in the hospital then there is 0 patient*/
		Assert.assertEquals(oldnbPatient,hospital.getService("Cardiology").getPatient() );
	}
	
	/*@Test
	public void realPatientTest()
	{
		//same test as before but all in a thread in patient
		Assert.assertEquals(0,hospital.getService("Cardiology").getPatient() );
		new Thread(new Patient(false,hospital.getService("Cardiology"))).start();
		Thread.currentThread();
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Assert.assertEquals(0,hospital.getService("Cardiology").getPatient() );
	}*/
	
	@Test 
	public void emergencyTest() throws InterruptedException
	{
		int oldnbPatient=hospital.getService("Neurology").getPatient();
		/*a patient comes in Neurology in emergency then directly in a room*/
		Patient p=new Patient(true,hospital.getService("Neurology"));
		r= new Room(hospital.getServices().get(1));
		d=new Doctor(hospital.getServices().get(1));
		
		/*There is one patient in more in Neurology */
		Assert.assertEquals(oldnbPatient+1,hospital.getService("Neurology").getPatient() );
		
		System.out.println("A patient is comming in Neurology in emergency");
		
		/*At least one room is free*/
		Assert.assertNotEquals(0,hospital.getServices().get(1).room.availablePermits() );
		p.emergency();
		/*one of the 4 room is now used*/
		Assert.assertEquals(3,hospital.getServices().get(1).room.availablePermits() );
		/*at least one doctor is free*/
		Assert.assertNotEquals(0,hospital.getServices().get(1).doctor.availablePermits() );
		r.waitDoctor();
		/*one doctor one the 3 is now used*/
		Assert.assertEquals(2,hospital.getServices().get(1).doctor.availablePermits() );
		
		d.examine();
		/*the room and doctor is now free*/
		Assert.assertEquals(3,hospital.getServices().get(1).doctor.availablePermits() );
		Assert.assertEquals(4,hospital.getServices().get(1).room.availablePermits() );
	
		p.leave();
		/*the patient left the hospital*/
		Assert.assertEquals(oldnbPatient,hospital.getService("Cardiology").getPatient() );
	}
	
	@Test
	public void GiveDoctorTest()
	{
		/*one per service*/
		int oldDocNb1=hospital.getServices().get(0).doctor.availablePermits();
		int oldDocNb2=hospital.getServices().get(1).doctor.availablePermits();
		/*Cardiology is not yet ok to give*/
		Assert.assertFalse(hospital.getServices().get(0).isGive());
		hospital.getServices().get(0).okToGive();
		/*now yes*/
		Assert.assertTrue(hospital.getServices().get(0).isGive());
		
		System.out.println("A patient is comming in Cardiology");
		Patient p=new Patient(false,hospital.getService("Cardiology"));
		/*A patient is here, then the service cannot give yet*/
		Assert.assertFalse(hospital.getServices().get(0).isGive());
		p.leave();
		
		hospital.getServices().get(1).askDoctor(true);
		/*Neurology ask a doctor, nobody gives then the number of doctor does nor change*/
		Assert.assertEquals(oldDocNb1,hospital.getServices().get(0).doctor.availablePermits() );
		Assert.assertEquals(oldDocNb2,hospital.getServices().get(1).doctor.availablePermits() );
		
		/*Cardiology is ok to give*/
		hospital.getServices().get(0).okToGive();
		
		/*askDoctor is a thread and takes a little time to work when it can finally take one then we make the main thread sleep */ 
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		/*Cardiology gave one doctor to Neurology*/
		Assert.assertEquals(oldDocNb1-1,hospital.getServices().get(0).doctor.availablePermits() );
		Assert.assertEquals(oldDocNb2+1,hospital.getServices().get(1).doctor.availablePermits() );
		
	}
	
	@Test
	public void askRoom() throws InterruptedException
	{
		int oldNbRoom=hospital.getServices().get(0).room.availablePermits();
		/*normal patient is coming*/
		Patient p=new Patient(false,hospital.getService("Cardiology"));
		n= new Nurse(hospital.getServices().get(0));
		r= new Room(hospital.getServices().get(0));
		d=new Doctor(hospital.getServices().get(0));
		
		/*Cardiology asks room*/
		hospital.getServices().get(0).askRoom(true);
		
		/*Assert not needed here: this is like the first test*/
		System.out.println("A patient is comming in Cardiology");
		p.fillPaper();
		hospital.getServices().get(0).nurse.acquire();
		n.process_paper();
		r.waitDoctor();
		
		Patient p2=new Patient(true,hospital.getService("Cardiology"));
		p2.emergency();
		new Room(hospital.getServices().get(0)).waitDoctor();
		
		/*Neurology is ok to give*/
		hospital.getServices().get(1).okToGive();
		/*2 rooms are used*/
		Assert.assertEquals(oldNbRoom, hospital.getServices().get(0).room.availablePermits()+2);
		/*to let the time to askRoom to work*/
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		/*the 2 patients finish their examinations*/
		d.examine();
		new Doctor(hospital.getServices().get(0)).examine();
		
		p.leave();
		p2.leave();
		/*After that patient left the hospital Cardiology has one room in more*/
		Assert.assertEquals(oldNbRoom, (hospital.getServices().get(0).room.availablePermits())-1);
	}
	
	@Test
	public void notEnoughRoom() throws InterruptedException
	{
		int oldNbPatient=hospital.getServices().get(2).getPatient();
		/*There is 2 room: 3 patients to make one wait*/
		/*They all come in emergency, to make the thing faster*/
		Patient p1=new Patient(true,hospital.getService("Rheumatology"));
		Patient p2=new Patient(true,hospital.getService("Rheumatology"));
		Patient p3=new Patient(true,hospital.getService("Rheumatology"));
		p1.emergency();
		p2.emergency();
		new Room(hospital.getServices().get(2)).waitDoctor();
		new Room(hospital.getServices().get(2)).waitDoctor();
		/*There is 3 patient in more than before*/
		Assert.assertEquals(oldNbPatient+3, hospital.getServices().get(2).getPatient());
		/*All rooms of the service are used*/
		Assert.assertEquals(0, hospital.getServices().get(2).room.availablePermits());
		/*The code use sempaphore, to show it we use a tryAcquire and not a acquire: to continue the test 
		 * All the rooms are used we will not be able to take a room
		 */
		Assert.assertFalse( hospital.getServices().get(2).room.tryAcquire(1,500, TimeUnit.MILLISECONDS));
		/*one patient finish*/
		new Doctor(hospital.getServices().get(2)).examine();
		p1.leave();
		/*then only 2 patients are still here and one room is now free*/
		Assert.assertEquals(oldNbPatient+2, hospital.getServices().get(2).getPatient());
		Assert.assertEquals(1, hospital.getServices().get(2).room.availablePermits());
		/*the 3rd patient can take it*/
		p3.emergency();
		new Room(hospital.getServices().get(2)).waitDoctor();
		new Doctor(hospital.getServices().get(2)).examine();
		new Doctor(hospital.getServices().get(2)).examine();
		p2.leave();
		p3.leave();
		/*The 2 other patients finished
		 * then the service get the same number of patient before those 3 came
		 * the 2 rooms are now free
		 */
		Assert.assertEquals(oldNbPatient, hospital.getServices().get(2).getPatient());
		Assert.assertEquals(2, hospital.getServices().get(2).room.availablePermits());
	}
}
