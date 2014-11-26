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
		
		Option socket = OptionBuilder.withArgName("socket")				 					 
				 					 .hasArg()
				 					 .isRequired()
				 					 .withDescription("IP_ADDRESS:PORT")
				 					 .create("s");
		 
		Option workload = OptionBuilder.withArgName("workload")					
									 .hasArg()
									 .isRequired()
									 .withDescription("WORKLOAD_FILE")
									 .create("w");
		 
		options.addOption(socket);
		options.addOption(workload);

		try {
		       // parse the command line arguments
		       command = parser.parse( options, args );
		} catch( ParseException exp ) {
		       // oops, something went wrong
		       System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
		}
		 
	}
	
	public String getOptionValue(String option){
		
		String value = command.getOptionValue(option);
		
		return value;
		
	}
}
