package net.majorsfamily;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class Logger 
{
	
	// class variables
	private String 	m_strFilename;
	private File	m_File;
		
	private FileWriter fileWriterOutput;
	private BufferedWriter streamOutput;
	private PrintWriter printOutput;
		
	private String m_strOutputLine;		// for building the output message
	// private Date m_Date;
	// private Calendar m_Calendar;

	public Logger() 
	{
		m_strFilename = "logfile.txt";
	}
	
	public Logger (String strFilename)
	{
		m_strFilename = strFilename;
	}
	
	
	
	public void Log (String strMessage)
	{
		m_strOutputLine = GetDateTimeStamp();
		m_strOutputLine += ": ";
		m_strOutputLine += strMessage;
		printOutput.println(m_strOutputLine);
	}
	
	public void Open()
	{
		boolean bRC = false;
		
		try
		{
			m_File = new File(m_strFilename);
			bRC = m_File.createNewFile();	// true if file is created
			fileWriterOutput = new FileWriter(m_File,false);	// overwrite mode
			streamOutput = new BufferedWriter(fileWriterOutput);
			printOutput = new PrintWriter(streamOutput,true); // flush output
			
			
			m_strOutputLine = GetDateTimeStamp();
			// add the first message
			m_strOutputLine += ": opening log file" ;
			printOutput.println(m_strOutputLine);
		}
		catch (FileNotFoundException e)
		{
			System.out.println("file not found");
		}
		catch (IOException e)
		{
			System.out.println("I/O Error");
		}
	}
	
	public void Close()
	{
		// get the date/time stamp
		m_strOutputLine = GetDateTimeStamp();
		// final message
		m_strOutputLine += ": closing log file" ;
		printOutput.println(m_strOutputLine);
		// flush and close
		printOutput.flush();
		printOutput.close();
	}
	
	private String GetDateTimeStamp()
	{
		// String currentDateTime = new String();
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSS'Z'");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date currentDate = new Date();
		return(dateFormat.format(currentDate)); 
	}
	


}
