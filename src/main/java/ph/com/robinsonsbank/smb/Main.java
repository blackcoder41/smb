package ph.com.robinsonsbank.smb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileAccessInformation;
import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.msfscc.fileinformation.FileBasicInformation;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.msfscc.fileinformation.FileStandardInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;


public class Main {

	public static void main(String[] args) throws IOException {
		
		String PATH = "";
		String DELIMETER = "\\";
		
		String USERNAME = "";
		char[] PASSWORD = "".toCharArray();
		String DOMAIN   = "";
		
		String[] PATH_ARGS = PATH.split(Pattern.quote(DELIMETER));
		String HOST = null;
		String SHARENAME = "";
		String DIR = "";
		
		for (String s : PATH_ARGS) {
			if (!s.isEmpty()) {
				if (HOST == null) {;
					HOST = s;
				} else if (SHARENAME.isEmpty()) {
					SHARENAME = s;
				} else {
					DIR += DELIMETER + s;
				}
			}
		}
		

		String format = "%-20s %-40s \n";
		System.out.printf(format, "HOST", HOST);
		System.out.printf(format, "SHARENAME", SHARENAME);
		System.out.printf(format, "DIR", DIR);

	    SMBClient client = new SMBClient();

	    try (Connection connection = client.connect(HOST)) {
	        AuthenticationContext ac = new AuthenticationContext(USERNAME.toString(), PASSWORD, DOMAIN.toString());
	        Session session = connection.authenticate(ac);

	        // Connect to Share
	        try (DiskShare share = (DiskShare) session.connectShare(SHARENAME)) {


	            
	            FileAllInformation pathInformation = share.getFileInformation(DIR);
	            FileAccessInformation accessInformation = pathInformation.getAccessInformation();
	            FileStandardInformation standardInformation = pathInformation.getStandardInformation();
	            FileBasicInformation basicInformation = pathInformation.getBasicInformation();
	            
 
	            if (standardInformation.isDirectory()) {
	            	
	            	for (FileIdBothDirectoryInformation f : share.list(DIR)) {
						System.out.printf("%20d %10s %40s\n",
								f.getAllocationSize(),
								f.getFileAttributes(),
								f.getFileName());
		            	
		            }
	            	
	            } else {
	            	
	            	long accessFlags = accessInformation.getAccessFlags();
					Set<AccessMask> accessMask = Set.of(AccessMask.MAXIMUM_ALLOWED  );
					Set<FileAttributes> attributes = Set.of(FileAttributes.FILE_ATTRIBUTE_NORMAL);
					Set<SMB2ShareAccess> shareAccesses = SMB2ShareAccess.ALL;
					SMB2CreateDisposition createDisposition = SMB2CreateDisposition.FILE_OPEN;
					Set<SMB2CreateOptions> createOptions = Set.of(SMB2CreateOptions.FILE_RANDOM_ACCESS);
					
					
					com.hierynomus.smbj.share.File file = share.openFile(DIR, accessMask, attributes, shareAccesses, createDisposition, createOptions);
	            
					String filename = PATH_ARGS[PATH_ARGS.length-1];
					File copyFile = new File(filename);
					FileOutputStream copyStream = new FileOutputStream(copyFile);
					
					
					System.out.println("Copy started");
					
					//System.out.println( filename );
					
					file.read(copyStream);
					
					System.out.println("Done");
					
	            }
				
	        }
		
	    }
	}
}
