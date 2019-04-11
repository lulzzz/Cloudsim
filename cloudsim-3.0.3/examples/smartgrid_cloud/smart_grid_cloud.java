package smartgrid_cloud;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class smart_grid_cloud{
	
	private static List<Cloudlet> cloudletList; // store all cloudlets 
    private static List<Vm> vmlist; // store all VM
	private static int VM_number =50;
	private static int cloudlet_number = 355; 
	private static CloudletSchedulerTimeShared cloudlet_scheduler =  new CloudletSchedulerTimeShared();
	private static List<Vm> Create_VM(int user_ID, int numbers_of_VM){
		LinkedList<Vm> list = new LinkedList<Vm>();
		//configure VM 
		long size = 1000; // image size of VM (MB)
		int  ram = 512; //  VM memory (MB)
		int  mips = 250; //  computing power of VM (million instruction per second)
		long bw = 1024; // bandwidth 1Gbps
		int pesNumber = 4; // number of CPU 
		String VM_name = "Xen"; // VM_name
		Vm[] vm = new Vm[numbers_of_VM];
		Log.printLine("Creating VM ......");
		// creating VM  with cloudlet scheduler 
		for (int i =0 ; i < numbers_of_VM;i++) {
			// assign each VM to specific users 
			vm[i] = new Vm(i, user_ID, mips, pesNumber, ram, bw, size, VM_name,  cloudlet_scheduler);
			list.add(vm[i]);
		     }
		return list;
		 }
    // create Cloudlets 	
	private static List<Cloudlet> Create_Cloudlet(int user_ID, int number_of_cloudlets){
		LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();
		// cloudlet configuration
		long length = 500; // mips 
		long fileSize = 300;   // MB
		long outputSize = 300; // MB
		int pesNumber = 1; // no. of CPU
		UtilizationModel Model = new UtilizationModelFull();
        Cloudlet[] cloudlet = new Cloudlet[number_of_cloudlets];
      
        for (int i = 0; i < number_of_cloudlets; i++) {
        	 // creating cloudlets  	
        	cloudlet[i] = new Cloudlet(i, length, pesNumber, fileSize, outputSize, Model,Model,Model);
        	// assign cloudlets with specific user ID
        	cloudlet[i].setUserId(user_ID);
			list.add(cloudlet[i]);
        	
        }
		
		return list;
	}
	// create datacenter with 2 hosts 
	private static Datacenter Create_Datacenter(String name){
		// creating list of host machine 
		List<Host> hostList = new ArrayList<Host>();
		// creating list of CPU for each host machine, In our simulation with choose 1 core per machine 
		List<Pe> peList1 = new ArrayList<Pe>();
		int mips = 1000; // computing power of each core
		// add  cores to each host machine 
		for (int i =0; i < 100; i ++ ) {
		peList1.add(new Pe(i, new PeProvisionerSimple(mips)));
		}
		// configuring host
		int hostId=0;
		int ram = 51200; //host memory 50 GB
		long storage = 5120000; //host storage 10000 GB
		int bw = 102400; // bandwidth 100 Gbps
		// create first host machine with 4 cores 
		hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList1,
    				new VmSchedulerTimeShared(peList1)
    			)
    		);
		// create another host machine with 1 cores
		List<Pe> peList2 = new ArrayList<Pe>();
		// add 1 core to each host machine 
		for (int i =0; i < 100; i ++ ) {
			peList2.add(new Pe(i, new PeProvisionerSimple(mips)));
		}
		hostId++;

		hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList2,
    				new VmSchedulerTimeShared(peList2)
    			)
    		);
		
	  // configuring datacenter 
		String arch = "x86";      // system architecture
		String os = "Linux";          // operating system
		String vmm = "Xen";
		double time_zone = 10.0;         // time zone this resource located
		double cost = 3.0;              // the cost of using processing in this resource
		double costPerMem = 0.05;		// the cost of using memory in this resource
		double costPerStorage = 0.001;	// the cost of using storage in this resource
		double costPerBw = 0.2;			// the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>();
		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

		// creating data center 
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return datacenter;
		
	}
	// create data center brokers 
	private static DatacenterBroker createBroker(){

		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}
    // print out all runtime results of cloudlets
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;
        List<Double> CPUtime = new ArrayList<Double>();
        List <Double>  cputime = new ArrayList<Double>();
        List <Double>  start_time = new ArrayList<Double>();
        List<Double>   starttime = new ArrayList<Double>();
        List<Double>   endtime = new ArrayList<Double>();
		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
		    if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
				cputime.add(cloudlet.getActualCPUTime());
				start_time.add(cloudlet.getExecStartTime());
			}
		}
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
		          if(!CPUtime.contains(cloudlet.getActualCPUTime())) {
		        	  CPUtime.add(cloudlet.getActualCPUTime());
		        	 }
		          if(!starttime.contains(cloudlet.getExecStartTime())) {
		        	  starttime.add(cloudlet.getExecStartTime());
		        	}
		          if(!endtime.contains(cloudlet.getFinishTime())) {
		        	  endtime.add(cloudlet.getFinishTime());
		        	 }
			}
		}
		
         int n=0;
		for (int i=0; i< CPUtime.size();i++) {
			
               n= Collections.frequency(cputime,CPUtime.get(i));
                Log.printLine(dft.format(n)+" "+"Cloudlets successfully finish in "+ dft.format(CPUtime.get(i))+"s" );
		}
		Log.printLine();
		for (int i=0; i< starttime.size();i++) {
			 n= Collections.frequency(start_time,starttime.get(i));
			 Log.printLine(dft.format(n)+" "+"Cloudlets executes in time "+ dft.format(starttime.get(i))+"~" + dft.format(endtime.get(i))+"s");
		 }
	}	
	public static void main(String[] args) {

	    Log.printLine("Start Smart Grid Cloud Simulation.....");	
	      try {

			int number_of_user = 40000;   // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;  // mean trace events

			CloudSim.init(number_of_user, calendar, trace_flag);

			// creating datacenter 
			@SuppressWarnings("unused")
			Datacenter smart_grid_cloud_data_center = Create_Datacenter("Datacenter_0");



			DatacenterBroker broker = createBroker();
			int brokerId = broker.getId();

			
			vmlist = Create_VM(brokerId,VM_number);
			cloudletList = Create_Cloudlet(brokerId,cloudlet_number);

			broker.submitVmList(vmlist);
			broker.submitCloudletList(cloudletList);

			// start simulation 
			CloudSim.startSimulation();
            // print result 
			List<Cloudlet> cloudletlist = broker.getCloudletReceivedList();

			CloudSim.stopSimulation();

			printCloudletList(cloudletlist);
			Log.printLine("Smart Grid Cloud finished!");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
	}
}








