import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;
import java.lang.reflect.*;

/**
 * Rename a class file.
 *
 * @version 0.1, 01/30/2000
 * @author Peter Csizmadia
 */
public class Rename {
    private byte[][] cpBytes;

    public static void main(String[] args) throws Throwable {
	Rename ren = new Rename(args);
    }

    private Rename(String[] args) throws Throwable {
	if(args.length < 2) {
	    System.out.println("usage: java "+getClass().getName()+
			       " oldname.class newname.class");
	    return;
	}

	File oldFile = new File(args[0]);
	String oldName = oldFile.getName();
	if(oldName.endsWith(".class"))
	    oldName = oldName.substring(0, oldName.length()-6);
	else
	    oldFile = new File(args[0]+".class");

	File newFile = new File(args[1]);
	String newName = newFile.getName();
	if(newName.endsWith(".class"))
	    newName = newName.substring(0, newName.length()-6);
	else
	    newFile = new File(args[1]+".class");

	DataInput in = new DataInputStream(new FileInputStream(oldFile));
	byte[] header = new byte[8];
	in.readFully(header);
	cpBytes = new byte[in.readUnsignedShort()][];
	Object[] cpObjects = new Object[cpBytes.length];
	for(int i=1; i<cpBytes.length; ++i) {
	    int tag = in.readByte();
	    byte[] bdata;
	    int[] idata;
	    switch(tag) {
		case 1: // UTF8
		    bdata = new byte[in.readUnsignedShort() + 3];
		    in.readFully(bdata, 3, bdata.length-3);
		    bdata[1] = (byte)((bdata.length - 3) >> 8);
		    bdata[2] = (byte)((bdata.length - 3) & 0xff);
		    cpObjects[i] = new String(bdata, 3, bdata.length - 3);
		    break;
		case 3: // int
		case 4: // float
		case 9: // field
		case 10: // method
		case 11: // interface method
		case 12: // name and type
		    bdata = new byte[5];
		    in.readFully(bdata, 1, bdata.length-1);
		    break;
		case 5: // long
		case 6: // double
		    bdata = new byte[9];
		    in.readFully(bdata, 1, bdata.length-1);
		    break;
		case 7: // class
		case 8: // string
		    bdata = new byte[3];
		    in.readFully(bdata, 1, bdata.length-1);
		    break;
		default:
		    throw new ClassFormatError("bad tag "+tag);
	    }
	    bdata[0] = (byte)tag;
	    cpBytes[i] = bdata;
	    if(tag == 5 || tag == 6) {
		++i;
		cpBytes[i] = new byte[bdata.length];
	    }
	}
	byte[] tmpBytes = new byte[8];
	in.readFully(tmpBytes); // access_flags, this_class, super_class,
	int this_class = (tmpBytes[2] & 0xff)<<8 | (tmpBytes[3] & 0xff);
	int n = (tmpBytes[6] & 0xff)<<8 | (tmpBytes[7] & 0xff); // interfaces
	byte[] middle = new byte[8 + 2*n];
	System.arraycopy(tmpBytes, 0, middle, 0, 8);
	in.readFully(middle, 8, 2*n);
	int[][][] members = new int[2][][];
	int[][][] memberAttrNameI = new int[2][][];
	byte[][][][] memberAttrBytes = new byte[2][][][];
	for(int memberType=0; memberType<2; ++memberType) {
	    n = in.readUnsignedShort(); // number of fields/methods
	    members[memberType] = new int[n][];
	    memberAttrNameI[memberType] = new int[n][];
	    memberAttrBytes[memberType] = new byte[n][][];
	    for(int i=0; i<n; ++i) {
		int[] arr = members[memberType][i] = new int[3];
		arr[0] = in.readUnsignedShort(); // access_flags
		arr[1] = in.readUnsignedShort(); // name_index
		arr[2] = in.readUnsignedShort(); // descriptor_index
		int count = in.readUnsignedShort(); // attributes
		memberAttrNameI[memberType][i] = new int[count];
		memberAttrBytes[memberType][i] = new byte[count][];
		for(int j=0; j<count; ++j) {
		    memberAttrNameI[memberType][i][j] = in.readUnsignedShort();
		    byte[] data = new byte[in.readInt()];
		    memberAttrBytes[memberType][i][j] = data;
		    in.readFully(data);
		}
	    }
	}
	n = in.readUnsignedShort(); // attributes
	int[] attrNameI = new int[n];
	byte[][] attrBytes = new byte[n][];
	Class ucl = null;
	for(int i=0; i<n; ++i) {
	    attrNameI[i] = in.readUnsignedShort();
	    in.readFully(attrBytes[i] = new byte[in.readInt()]);
	}

	renameInCP(oldName, newName, cpObjects, this_class);

	// write file to disk
	try {
	    FileOutputStream os = new FileOutputStream(oldFile);
	    DataOutput out = new DataOutputStream(os);
	    out.write(header);

	    // constant pool
	    out.writeShort(cpBytes.length);
	    for(int i=1; i<cpBytes.length; ++i)
		out.write(cpBytes[i]);

	    // access_flags, this_class, super_class, interfaces
	    out.write(middle);

	    // fields, methods
	    for(int memberType = 0; memberType < 2; ++memberType) {
		out.writeShort(members[memberType].length);
		for(int i=0; i<members[memberType].length; ++i) {
		    int[] arr = members[memberType][i];
		    int[] nameI = memberAttrNameI[memberType][i];
		    out.writeShort(arr[0]);
		    out.writeShort(arr[1]);
		    out.writeShort(arr[2]);
		    out.writeShort(nameI.length);
		    for(int j=0; j<nameI.length; ++j) {
			out.writeShort(nameI[j]);
			byte[] data = memberAttrBytes[memberType][i][j];
			out.writeInt(data.length);
			out.write(data);
		    }
		}
	    }
	    out.writeShort(attrNameI.length);
	    for(int i=0; i<attrNameI.length; ++i) {
		out.writeShort(attrNameI[i]);
		out.writeInt(attrBytes[i].length);
		out.write(attrBytes[i]);
	    }
	    os.close();
	    if(!oldFile.renameTo(newFile)) {
		System.err.println(oldFile.getPath()+
				   (": could not rename file but "+
				   "successfully crapped it up"));
		if(oldFile.delete())
		    System.err.println(oldFile.getPath()+": deleted");
		System.exit(1);
	    }
	} catch(IOException ex) {
	    System.err.println(newFile.getPath()+": cannot write file");
	    System.exit(1);
	}
    }

    /**
     * Rename the class in the constant pool.
     */
    private void renameInCP(String oldName, String newName,
			    Object[] cpObjects, int this_class)
    {
	String newFname = newName+".class";
	int nameI = ((cpBytes[this_class][1] & 0xff)<<8)
		  | (cpBytes[this_class][2] & 0xff);
	for(int i=1; i<cpBytes.length; ++i)
	    if(cpBytes[i] != null) {
		if(cpBytes[i][0] == 1) { // UTF8
		    if(cpObjects[i].equals(oldName+".class")) {
			int l = newFname.length();
			cpBytes[i] = new byte[l+3];
			cpBytes[i][0] = 1;
			cpBytes[i][1] = (byte)((l>>8) & 0xff);
			cpBytes[i][2] = (byte)(l & 0xff);
			for(int j=0; j<l; ++j)
			    cpBytes[i][j+3] = (byte)newFname.charAt(j);
			System.err.println("warning: "+cpObjects[i]+
					   " renamed to "+newFname+
					   " in constant pool");
		    }
		}
	    }
	boolean used_by_other_entry = false;
	for(int i=1; i<cpBytes.length; ++i)
	    if(cpBytes[i] != null) {
		int tag = cpBytes[i][0];
		if(tag == 8) {
		    int cli = ((cpBytes[i][1] & 0xff)<<8) |
			   (cpBytes[i][2] & 0xff);
		    if(cli == nameI) {
			used_by_other_entry = true;
			System.err.println("warning:"+
					   " constant pool index "+nameI+
			     		   " is also used used by entry "+i);
			break;
		    }
		}
	    }
	if(used_by_other_entry) {
	    byte[][] newCpBytes = new byte[cpBytes.length + 1][];
	    System.arraycopy(cpBytes, 0, newCpBytes, 0, cpBytes.length);
	    nameI = cpBytes.length;
	    cpBytes = newCpBytes;
	    cpBytes[this_class][1] = (byte)((nameI>>8) & 0xff);
	    cpBytes[this_class][2] = (byte)(nameI & 0xff);
	}
	cpBytes[nameI] = new byte[newName.length()+3];
	cpBytes[nameI][0] = 1; // UTF8
	cpBytes[nameI][1] = (byte)((newName.length()>>8) & 0xff);
	cpBytes[nameI][2] = (byte)(newName.length() & 0xff);
	for(int i=0; i<newName.length(); ++i)
	    cpBytes[nameI][i+3] = (byte)newName.charAt(i);
    }
}
