package commandline;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CommandLineInterface {
	
	private static CommandLine command;
		
	@SuppressWarnings("static-access")
	public CommandLineInterface(String[] args){
		// create the Options
		Options options  = new Options();
		// create the command line parser
		CommandLineParser parser = new GnuParser();
		
		Option port = OptionBuilder.withArgName("port")				 					 
				 					 .hasArg()
				 					 .isRequired()
				 					 .withDescription("Port Number")
				 					 .create("s");

		Option localWorker = OptionBuilder.withArgName("N_workers")				 					 
									 .hasArg()				 
									 .withDescription("Run local worker threads")
									 .create("lw");
		
		Option remoteWorker = OptionBuilder.withDescription("Run remote worker")
				 					 .create("rw");
		 		 
		options.addOption(port);
		options.addOption(localWorker);	
		options.addOption(remoteWorker);	

		try {
		       // parse the command line arguments
		       command = parser.parse( options, args );
		} catch( ParseException exp ) {
		       // oops, something went wrong
		       System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
		}
		 
	}
	
	public String getOptionValue(String option){
		
		if(command.hasOption(option)){
			return command.getOptionValue(option);
			
		}else{
			
			return null;
		}
				
	}
	
	public boolean hasOption(String option){
		
		return command.hasOption(option);
		
	}
}
