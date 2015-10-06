package com.sensor.barcode;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONObject;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.atiia.automation.sensors.NetFTRDTPacket;
import com.atiia.automation.sensors.NetFTSensor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Repeatable;
import java.net.DatagramSocket;

@Path("/readdata")
public class ReadData	 
{
	private DatagramSocket slowDataSocket = null;
	private NetFTRDTPacket packet = null;
	private NetFTRDTPacket[] packetsArray = null;
	private String result = null;
	private int m_iRDTSampleRate; 
	private double[] m_daftCountsPerUnit = {1, 1, 1, 1, 1, 1};
	private FileWriter fileWriter;
	private int readFrequency = 0;
	private int readCount = 0;
	private int readPackets = 0;
	private String readPath = null;
	private String fileName = null;
	private File folder = null;
	@GET
	@Path("/csvfile")
	@Produces(MediaType.TEXT_XML)
	public String readDataCSV(@QueryParam("ip") String ip, @QueryParam("port") String port,@QueryParam("packets") String packets,@QueryParam("frequency") String frequency,@QueryParam("count") String count,@QueryParam("path") String path)
	{
		try 
		{
			readFrequency = Integer.parseInt(frequency);
			readCount = Integer.parseInt(count);
			readPackets=Integer.parseInt(packets);
			if(path.equals(""))
				readPath = "C:\temp";
			else 
				readPath = path;
			folder = new File(path);
			if(!folder.exists())
			{
				folder.mkdirs();
			}
			fileName = folder.getAbsolutePath() + "\\output_"+getDateTimeWithZone()+".csv";
			//return getEmptyXML(readFrequency+""+readCount+""+folder.getAbsolutePath()+""+fileName);
			WriteData("RDTSequence,FTSequence,Status,Fx,Fy,Fz,Tx,Ty,Tz", "TimeStamp");
			if(readConfigurationInfo(ip))
			{
				NetFTSensor sensor = new NetFTSensor(InetAddress.getByName(ip), Integer.parseInt(port));
				if(sensor!=null)
				{
						int i = 0;
						while(i<readCount)
						{
							slowDataSocket = sensor.startHighSpeedDataCollection(0);
							if(slowDataSocket!=null)
							{
								packetsArray = new NetFTRDTPacket[readPackets];
								packetsArray = sensor.readHighSpeedData(slowDataSocket, readPackets);
								if(packetsArray != null)
								{
									for (int j = 0; j < readPackets; j++) 
									{
										String data = getString(packetsArray[j]);
										WriteData(data);
									}
									CloseData();
								}
							}
							Thread.sleep(readFrequency);
						}
						result = getXMLFile(fileName,getDateTimeWithZone());
				}
			}
			else
			{
				throw new Exception();
			}
		}
		catch (UnknownHostException e) 
		{
			result = getEmptyXML(e.toString());
		}
		catch(IOException e)
		{
			result = getEmptyXML(e.toString());
		}
		catch(Exception e)
		{
			result = getEmptyXML("Cannot Read Sensor Configuration");
		}
		return result;
	}
	@SuppressWarnings("unused")
	@GET
	@Path("/jsonfile")
	@Produces(MediaType.TEXT_XML)
	public String readDataJSON(@QueryParam("ip") String ip, @QueryParam("port") String port,@QueryParam("packets") String packets,@QueryParam("frequency") String frequency,@QueryParam("count") String count,@QueryParam("path") String path)
	{
		try 
		{
			readFrequency = Integer.parseInt(frequency);
			readCount = Integer.parseInt(count);
			readPackets=Integer.parseInt(packets);
			if(path.equals(""))
				readPath = "C:\temp";
			else 
				readPath = path;
			folder = new File(path);
			if(!folder.exists())
			{
				folder.mkdirs();
			}
			fileName = folder.getAbsolutePath() + "\\output_"+getDateTimeWithZone()+".json";
			//return getEmptyXML(readFrequency+""+readCount+""+folder.getAbsolutePath()+""+fileName);
			if(readConfigurationInfo(ip))
			{
				NetFTSensor sensor = new NetFTSensor(InetAddress.getByName(ip), Integer.parseInt(port));
				if(sensor!=null)
				{
						int i = 0;
						while(i<readCount)
						{
							slowDataSocket = sensor.startHighSpeedDataCollection(0);
							if(slowDataSocket!=null)
							{
								packetsArray = new NetFTRDTPacket[readPackets];
								packetsArray = sensor.readHighSpeedData(slowDataSocket, readPackets);
								if(packetsArray != null)
								{
									for (int j = 0; j < readPackets; j++) 
									{
										String data = getStringJSON(packetsArray[j]);
										WriteData(data);
									}
									CloseData();
								}
							}
							Thread.sleep(readFrequency);
						}
						result = getXMLFile(fileName,getDateTimeWithZone());
				}
			}
			else
			{
				throw new Exception();
			}
		}
		catch (UnknownHostException e) 
		{
			result = getEmptyXML(e.toString());
		}
		catch(IOException e)
		{
			result = getEmptyXML(e.toString());
		}
		catch(Exception e)
		{
			result = getEmptyXML("Cannot Read Sensor Configuration");
		}
		return result;
	}
	
	
	
	@GET
	@Path("/json")
	@Produces({MediaType.TEXT_PLAIN,MediaType.TEXT_PLAIN})
	public String readDataJSONString(@QueryParam("ip") String ip, @QueryParam("port") String port)
	{
		try 
		{
			if(readConfigurationInfo(ip))
			{
				NetFTSensor sensor = new NetFTSensor(InetAddress.getByName(ip), Integer.parseInt(port));
				if(sensor!=null)
				{
					slowDataSocket = sensor.initLowSpeedData();
					if(slowDataSocket!=null)
					{
						packet = sensor.readLowSpeedData(slowDataSocket);
						if(packet != null)
						{
							result = getStringJSON(packet);
						}
					}
				}
			}
			else
			{
				throw new Exception();
			}
		}
		catch (UnknownHostException e) 
		{
			result = getEmptyXML(e.toString());
		}
		catch(IOException e)
		{
			result = getEmptyXML(e.toString());
		}
		catch(Exception e)
		{
			result = getEmptyXML("Cannot Read Sensor Configuration");
		}
		return result;
	}
	
	@GET
	@Produces(MediaType.TEXT_XML)
	public String readData(@QueryParam("ip") String ip, @QueryParam("port") String port)
	{
		try 
		{
			if(readConfigurationInfo(ip))
			{
				NetFTSensor sensor = new NetFTSensor(InetAddress.getByName(ip), Integer.parseInt(port));
				if(sensor!=null)
				{
					slowDataSocket = sensor.initLowSpeedData();
					if(slowDataSocket!=null)
					{
						packet = sensor.readLowSpeedData(slowDataSocket);
						if(packet != null)
						{
							result = getXML(packet);
						}
						else
						{
							result = getEmptyXML("No Data Found");
						}
					}
				}
			}
			else
			{
				throw new Exception();
			}
		}
		catch (UnknownHostException e) 
		{
			result = getEmptyXML(e.toString());
		}
		catch(IOException e)
		{
			result = getEmptyXML(e.toString());
		}
		catch(Exception e)
		{
			result = getEmptyXML("Cannot Read Sensor Configuration");
		}
		return result;
	}
	
	
	private FileWriter getWriter(String filePath) {
		if (fileWriter == null) {
			try {
				fileWriter = new FileWriter(filePath, true);
			} catch (IOException e) {
				
			}
		}
		return fileWriter;
	}
	
	
	private void WriteData(String data, String date) {
		try {
			getWriter(fileName).write(data + "," + date + "\n");
			getWriter(fileName).flush();
			CloseData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void WriteData(String data) {
		try {
			getWriter(fileName).write(data);
			getWriter(fileName).flush();
			CloseData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void CloseData() {
		try {
			getWriter(fileName).close();
			fileWriter = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private String getString(NetFTRDTPacket packet)
	{
		StringBuilder s = new StringBuilder();
		s.append(packet.getRDTSequence()+",");
		s.append(packet.getFTSequence()+",");
		s.append(packet.getStatus()+",");
		s.append(packet.getFx()/m_daftCountsPerUnit[0]+",");
		s.append(packet.getFy()/m_daftCountsPerUnit[1]+",");
		s.append(packet.getFz()/m_daftCountsPerUnit[2]+",");
		s.append(packet.getTx()/m_daftCountsPerUnit[3]+",");
		s.append(packet.getTy()/m_daftCountsPerUnit[4]+",");
		s.append(packet.getTz()/m_daftCountsPerUnit[5]+",");
		return s.toString();
	}
	
	private String getStringJSON(NetFTRDTPacket packet)
	{
		JSONObject object = new JSONObject();
		object.put("rdtsequence", packet.getRDTSequence());
		object.put("ftsequence",packet.getFTSequence());
		object.put("status",packet.getStatus());
		object.put("fx",packet.getFx()/m_daftCountsPerUnit[0]);
		object.put("fy",packet.getFy()/m_daftCountsPerUnit[1]);
		object.put("fz",packet.getFz()/m_daftCountsPerUnit[2]);
		object.put("tx",packet.getTx()/m_daftCountsPerUnit[3]);
		object.put("ty",packet.getTy()/m_daftCountsPerUnit[4]);
		object.put("tz",packet.getTz()/m_daftCountsPerUnit[5]);
		return object.toJSONString();
	}
	
	private String getXMLFile(String filePath, String date)
	{
		StringBuilder s = new StringBuilder();
		s.append("<?xml version = \"1.0\"?>");
		s.append("<ForceTorque>");
		s.append("<FilePath>"+filePath+"</Tz>");
		s.append("<DateTime>"+date+"</DateTime>");
		s.append("</ForceTorque>");
		return s.toString();
	}


	private String getXML(NetFTRDTPacket packet)
	{
		StringBuilder s = new StringBuilder();
		s.append("<?xml version = \"1.0\"?>");
		s.append("<ForceTorque>");
		s.append("<RDTSequence>"+packet.getRDTSequence()+"</RDTSequence>");
		s.append("<FTSequence>"+packet.getFTSequence()+"</FTSequence>");
		s.append("<Status>"+packet.getStatus()+"</Status>");
		s.append("<Fx>"+packet.getFx()/m_daftCountsPerUnit[0]+"</Fx>");
		s.append("<Fy>"+packet.getFy()/m_daftCountsPerUnit[1]+"</Fy>");
		s.append("<Fz>"+packet.getFz()/m_daftCountsPerUnit[2]+"</Fz>");
		s.append("<Tx>"+packet.getTx()/m_daftCountsPerUnit[3]+"</Tx>");
		s.append("<Ty>"+packet.getTy()/m_daftCountsPerUnit[4]+"</Ty>");
		s.append("<Tz>"+packet.getTz()/m_daftCountsPerUnit[5]+"</Tz>");
		s.append("<DateTime>"+getDateTimeWithZone()+"</DateTime>");
		s.append("</ForceTorque>");
		return s.toString();
	}
	private String getCurrentTimezoneOffset() {

		TimeZone tz = TimeZone.getDefault();
		Calendar cal = GregorianCalendar.getInstance(tz);
		int offsetInMillis = tz.getOffset(cal.getTimeInMillis());
		int hour = Integer.parseInt(String.format("%02d", Math.abs(offsetInMillis / 3600000)));
		int minute = Integer.parseInt(String.format("%02d", Math.abs((offsetInMillis / 60000) % 60)));
		String offset = (minute < 1) ? (hour + "") : (hour + "" + minute + "");
		offset = (offsetInMillis >= 0 ? "" : "-") + offset;
		return offset;
	}
	private String getDateTimeWithZone() {
		return getDateTime() + "Tz" + getCurrentTimezoneOffset();
	}
	private String getDateTime()
	{
		SimpleDateFormat dateTime = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		dateTime.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateTime.format(new Date());
	}
	private String getEmptyXML(String status)
	{
		StringBuilder s = new StringBuilder();
		s.append("<?xml version = \"1.0\"?>");
		s.append("<ForceTorque>");
		s.append("<ExceptionString>"+status+"</ExceptionString>");
		s.append("<ExceptionDateTime>"+getDateTimeWithZone()+"</ExceptionDateTime>");
		s.append("</ForceTorque>");
		return s.toString();
	}
	private String readNetFTAPI(int index,String ipAddress)
    {
        try{
        String strXML = readWebPageText("netftapi2.xml?index="+index,ipAddress);
        return strXML;
        }catch(Exception e)
        {
            return "";
        }
    }
	private String readWebPageText( String strUrlSuffix , String m_strSensorAddress) throws 
    MalformedURLException, IOException
	{
		  /*Reads the HTML from the web server.*/
		  BufferedReader cBufferedReader;
		  /*The url of the configuration page.*/
		  String strURL = "http://" + m_strSensorAddress + "/" +
		          strUrlSuffix;
		  cBufferedReader = new BufferedReader ( new InputStreamReader ( new
		          URL(strURL).openConnection().getInputStream()));        
		  /*The text of the page.*/
		  String strPageText = "";
		  /*The last line read from the web stream.*/
		  String strCurLine;
		  /*Precondition: cBufferedReader is at the beginning of the page.
		   *Postcondition: cBufferedReader is finished, strPageText =
		   *the text of the page, strCurLine = last line read from the 
		   *page.
		   */
		   while ( null != ( strCurLine = cBufferedReader.readLine() ) ) {            
		      strPageText += strCurLine;
		   }     
		   return strPageText;
}
	
	//Read NetFT Cal API
	private String readNetFTCalAPI(int index, String ipAddress)
    {
        try{
        String strXML = readWebPageText("netftcalapi.xml?index="+index,ipAddress);
        return strXML;
        }catch(Exception e)
        {
            return "";
        }
    }
	private int findActiveCFG(String xmlText)
    {
       String[] strret = xmlText.split("<setcfgsel>");
       String[] strret2 = strret[1].split("</setcfgsel>");
       int activeConfig = Integer.parseInt(strret2[0]);
       return activeConfig;       
    }
	
	private void setCountsPerForce( double counts )
    {
        double dCountsPerForce = counts;
        if ( 0 == dCountsPerForce ){
            dCountsPerForce = 1;
        }
        int i;
        for ( i = 0; i < 3; i++ )
        {
            m_daftCountsPerUnit[i] = dCountsPerForce;
        }
    }
    
    private void setCountsPerTorque( double counts )
    {
        double dCountsPerTorque = counts;
        if ( 0 == dCountsPerTorque ) {
            dCountsPerTorque = 1;
        }
        int i;
        for ( i = 0; i < 3; i++ )
        {
            m_daftCountsPerUnit[i+3] = dCountsPerTorque;
        }
    }
    private boolean readConfigurationInfo(String ipAddress)
    { 
        try
        {
	        String mDoc = readNetFTAPI(0,ipAddress);
	        int activeConfig = findActiveCFG(mDoc);
	        mDoc = readNetFTAPI(activeConfig,ipAddress);
	        String[] parseStep1 = mDoc.split("<cfgcalsel>");
	        String[] parseStep2 = parseStep1[1].split("</cfgcalsel>");
	        String mCal = readNetFTCalAPI(Integer.parseInt(parseStep2[0]),ipAddress);
	        mDoc = readNetFTAPI(activeConfig,ipAddress);
	        parseStep1 = mDoc.split("<cfgcpf>");
	        parseStep2 = parseStep1[1].split("</cfgcpf>");        
	        setCountsPerForce(Double.parseDouble(parseStep2[0]));
	        parseStep1 = mDoc.split("<cfgcpt>");
	        parseStep2 = parseStep1[1].split("</cfgcpt>");       
	        setCountsPerTorque(Double.parseDouble(parseStep2[0]));
	        parseStep1 = mDoc.split("<comrdtrate>");
	        parseStep2 = parseStep1[1].split("</comrdtrate>");  
	        m_iRDTSampleRate = (Integer.parseInt(parseStep2[0]));
        }
        catch(Exception e)
        {
            return false;            
        }
        return true;
    }
}

