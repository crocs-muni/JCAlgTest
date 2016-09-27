/*
 * To the extent possible under law, contributors have waived all
 * copyright and related or neighboring rights to work.
 */
package jnasmartcardio;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class WinscardConstants {
	private WinscardConstants(){}
	// Starting from javadoc at http://pcsclite.alioth.debian.org/api/pcsclite_8h.html
	// find all #define 	([\w_]+)   \(\(LONG\)0x([0-9A-F]+)\)\s*\n\s+(.*)
	// replace with public static final int $1 = 0x$2;
	public static final int SCARD_S_SUCCESS = 0x00000000;
	public static final int SCARD_F_INTERNAL_ERROR = 0x80100001;
	public static final int SCARD_E_CANCELLED = 0x80100002;
	public static final int SCARD_E_INVALID_HANDLE = 0x80100003;
	public static final int SCARD_E_INVALID_PARAMETER = 0x80100004;
	public static final int SCARD_E_INVALID_TARGET = 0x80100005;
	public static final int SCARD_E_NO_MEMORY = 0x80100006;
	public static final int SCARD_F_WAITED_TOO_LONG = 0x80100007;
	public static final int SCARD_E_INSUFFICIENT_BUFFER = 0x80100008;
	public static final int SCARD_E_UNKNOWN_READER = 0x80100009;
	public static final int SCARD_E_TIMEOUT = 0x8010000A;
	public static final int SCARD_E_SHARING_VIOLATION = 0x8010000B;
	public static final int SCARD_E_NO_SMARTCARD = 0x8010000C;
	public static final int SCARD_E_UNKNOWN_CARD = 0x8010000D;
	public static final int SCARD_E_CANT_DISPOSE = 0x8010000E;
	public static final int SCARD_E_PROTO_MISMATCH = 0x8010000F;
	public static final int SCARD_E_NOT_READY = 0x80100010;
	public static final int SCARD_E_INVALID_VALUE = 0x80100011;
	public static final int SCARD_E_SYSTEM_CANCELLED = 0x80100012;
	public static final int SCARD_F_COMM_ERROR = 0x80100013;
	public static final int SCARD_F_UNKNOWN_ERROR = 0x80100014;
	public static final int SCARD_E_INVALID_ATR = 0x80100015;
	public static final int SCARD_E_NOT_TRANSACTED = 0x80100016;
	public static final int SCARD_E_READER_UNAVAILABLE = 0x80100017;
	public static final int SCARD_P_SHUTDOWN = 0x80100018;
	public static final int SCARD_E_PCI_TOO_SMALL = 0x80100019;
	public static final int SCARD_E_READER_UNSUPPORTED = 0x8010001A;
	public static final int SCARD_E_DUPLICATE_READER = 0x8010001B;
	public static final int SCARD_E_CARD_UNSUPPORTED = 0x8010001C;
	public static final int SCARD_E_NO_SERVICE = 0x8010001D;
	public static final int SCARD_E_SERVICE_STOPPED = 0x8010001E;
	public static final int SCARD_E_UNEXPECTED = 0x8010001F;
	public static final int SCARD_E_UNSUPPORTED_FEATURE = 0x8010001F;
	public static final int SCARD_E_ICC_INSTALLATION = 0x80100020;
	public static final int SCARD_E_ICC_CREATEORDER = 0x80100021;
	public static final int SCARD_E_DIR_NOT_FOUND = 0x80100023;
	public static final int SCARD_E_FILE_NOT_FOUND = 0x80100024;
	public static final int SCARD_E_NO_DIR = 0x80100025;
	public static final int SCARD_E_NO_FILE = 0x80100026;
	public static final int SCARD_E_NO_ACCESS = 0x80100027;
	public static final int SCARD_E_WRITE_TOO_MANY = 0x80100028;
	public static final int SCARD_E_BAD_SEEK = 0x80100029;
	public static final int SCARD_E_INVALID_CHV = 0x8010002A;
	public static final int SCARD_E_UNKNOWN_RES_MNG = 0x8010002B;
	public static final int SCARD_E_NO_SUCH_CERTIFICATE = 0x8010002C;
	public static final int SCARD_E_CERTIFICATE_UNAVAILABLE = 0x8010002D;
	public static final int SCARD_E_NO_READERS_AVAILABLE = 0x8010002E;
	public static final int SCARD_E_COMM_DATA_LOST = 0x8010002F;
	public static final int SCARD_E_NO_KEY_CONTAINER = 0x80100030;
	public static final int SCARD_E_SERVER_TOO_BUSY = 0x80100031;
	public static final int SCARD_W_UNSUPPORTED_CARD = 0x80100065;
	public static final int SCARD_W_UNRESPONSIVE_CARD = 0x80100066;
	public static final int SCARD_W_UNPOWERED_CARD = 0x80100067;
	public static final int SCARD_W_RESET_CARD = 0x80100068;
	public static final int SCARD_W_REMOVED_CARD = 0x80100069;
	public static final int SCARD_W_SECURITY_VIOLATION = 0x8010006A;
	public static final int SCARD_W_WRONG_CHV = 0x8010006B;
	public static final int SCARD_W_CHV_BLOCKED = 0x8010006C;
	public static final int SCARD_W_EOF = 0x8010006D;
	public static final int SCARD_W_CANCELLED_BY_USER = 0x8010006E;
	public static final int SCARD_W_CARD_NOT_AUTHENTICATED = 0x8010006F;

	// Starting from javadoc at http://pcsclite.alioth.debian.org/api/pcsclite_8h.html
	// find all #define 	([\w_]+)   \(\(LONG\)0x([0-9A-F]+)\)\s*\n\s+(.*)
	// replace with a.put($1, "$1"); b.put($1, "$3");
	public static final Map<Integer, String> ERROR_TO_DESCRIPTION;
	public static final Map<Integer, String> ERROR_TO_VARIABLE_NAME;
	static {
		Map<Integer, String> a = new HashMap<Integer, String>(), b = new HashMap<Integer, String>();
		a.put(SCARD_S_SUCCESS, "SCARD_S_SUCCESS"); b.put(SCARD_S_SUCCESS, "error codes from http://msdn.microsoft.com/en-us/library/aa924526.aspx");
		a.put(SCARD_F_INTERNAL_ERROR, "SCARD_F_INTERNAL_ERROR"); b.put(SCARD_F_INTERNAL_ERROR, "An internal consistency check failed.");
		a.put(SCARD_E_CANCELLED, "SCARD_E_CANCELLED"); b.put(SCARD_E_CANCELLED, "The action was cancelled by an SCardCancel request.");
		a.put(SCARD_E_INVALID_HANDLE, "SCARD_E_INVALID_HANDLE"); b.put(SCARD_E_INVALID_HANDLE, "The supplied handle was invalid.");
		a.put(SCARD_E_INVALID_PARAMETER, "SCARD_E_INVALID_PARAMETER"); b.put(SCARD_E_INVALID_PARAMETER, "One or more of the supplied parameters could not be properly interpreted.");
		a.put(SCARD_E_INVALID_TARGET, "SCARD_E_INVALID_TARGET"); b.put(SCARD_E_INVALID_TARGET, "Registry startup information is missing or invalid.");
		a.put(SCARD_E_NO_MEMORY, "SCARD_E_NO_MEMORY"); b.put(SCARD_E_NO_MEMORY, "Not enough memory available to complete this command.");
		a.put(SCARD_F_WAITED_TOO_LONG, "SCARD_F_WAITED_TOO_LONG"); b.put(SCARD_F_WAITED_TOO_LONG, "An internal consistency timer has expired.");
		a.put(SCARD_E_INSUFFICIENT_BUFFER, "SCARD_E_INSUFFICIENT_BUFFER"); b.put(SCARD_E_INSUFFICIENT_BUFFER, "The data buffer to receive returned data is too small for the returned data.");
		a.put(SCARD_E_UNKNOWN_READER, "SCARD_E_UNKNOWN_READER"); b.put(SCARD_E_UNKNOWN_READER, "The specified reader name is not recognized.");
		a.put(SCARD_E_TIMEOUT, "SCARD_E_TIMEOUT"); b.put(SCARD_E_TIMEOUT, "The user-specified timeout value has expired.");
		a.put(SCARD_E_SHARING_VIOLATION, "SCARD_E_SHARING_VIOLATION"); b.put(SCARD_E_SHARING_VIOLATION, "The smart card cannot be accessed because of other connections outstanding.");
		a.put(SCARD_E_NO_SMARTCARD, "SCARD_E_NO_SMARTCARD"); b.put(SCARD_E_NO_SMARTCARD, "The operation requires a Smart Card, but no Smart Card is currently in the device.");
		a.put(SCARD_E_UNKNOWN_CARD, "SCARD_E_UNKNOWN_CARD"); b.put(SCARD_E_UNKNOWN_CARD, "The specified smart card name is not recognized.");
		a.put(SCARD_E_CANT_DISPOSE, "SCARD_E_CANT_DISPOSE"); b.put(SCARD_E_CANT_DISPOSE, "The system could not dispose of the media in the requested manner.");
		a.put(SCARD_E_PROTO_MISMATCH, "SCARD_E_PROTO_MISMATCH"); b.put(SCARD_E_PROTO_MISMATCH, "The requested protocols are incompatible with the protocol currently in use with the smart card.");
		a.put(SCARD_E_NOT_READY, "SCARD_E_NOT_READY"); b.put(SCARD_E_NOT_READY, "The reader or smart card is not ready to accept commands.");
		a.put(SCARD_E_INVALID_VALUE, "SCARD_E_INVALID_VALUE"); b.put(SCARD_E_INVALID_VALUE, "One or more of the supplied parameters values could not be properly interpreted.");
		a.put(SCARD_E_SYSTEM_CANCELLED, "SCARD_E_SYSTEM_CANCELLED"); b.put(SCARD_E_SYSTEM_CANCELLED, "The action was cancelled by the system, presumably to log off or shut down.");
		a.put(SCARD_F_COMM_ERROR, "SCARD_F_COMM_ERROR"); b.put(SCARD_F_COMM_ERROR, "An internal communications error has been detected.");
		a.put(SCARD_F_UNKNOWN_ERROR, "SCARD_F_UNKNOWN_ERROR"); b.put(SCARD_F_UNKNOWN_ERROR, "An internal error has been detected, but the source is unknown.");
		a.put(SCARD_E_INVALID_ATR, "SCARD_E_INVALID_ATR"); b.put(SCARD_E_INVALID_ATR, "An ATR obtained from the registry is not a valid ATR string.");
		a.put(SCARD_E_NOT_TRANSACTED, "SCARD_E_NOT_TRANSACTED"); b.put(SCARD_E_NOT_TRANSACTED, "An attempt was made to end a non-existent transaction.");
		a.put(SCARD_E_READER_UNAVAILABLE, "SCARD_E_READER_UNAVAILABLE"); b.put(SCARD_E_READER_UNAVAILABLE, "The specified reader is not currently available for use.");
		a.put(SCARD_P_SHUTDOWN, "SCARD_P_SHUTDOWN"); b.put(SCARD_P_SHUTDOWN, "The operation has been aborted to allow the server application to exit.");
		a.put(SCARD_E_PCI_TOO_SMALL, "SCARD_E_PCI_TOO_SMALL"); b.put(SCARD_E_PCI_TOO_SMALL, "The PCI Receive buffer was too small.");
		a.put(SCARD_E_READER_UNSUPPORTED, "SCARD_E_READER_UNSUPPORTED"); b.put(SCARD_E_READER_UNSUPPORTED, "The reader driver does not meet minimal requirements for support.");
		a.put(SCARD_E_DUPLICATE_READER, "SCARD_E_DUPLICATE_READER"); b.put(SCARD_E_DUPLICATE_READER, "The reader driver did not produce a unique reader name.");
		a.put(SCARD_E_CARD_UNSUPPORTED, "SCARD_E_CARD_UNSUPPORTED"); b.put(SCARD_E_CARD_UNSUPPORTED, "The smart card does not meet minimal requirements for support.");
		a.put(SCARD_E_NO_SERVICE, "SCARD_E_NO_SERVICE"); b.put(SCARD_E_NO_SERVICE, "The Smart card resource manager is not running.");
		a.put(SCARD_E_SERVICE_STOPPED, "SCARD_E_SERVICE_STOPPED"); b.put(SCARD_E_SERVICE_STOPPED, "The Smart card resource manager has shut down.");
		a.put(SCARD_E_UNEXPECTED, "SCARD_E_UNEXPECTED"); b.put(SCARD_E_UNEXPECTED, "An unexpected card error has occurred.");
		a.put(SCARD_E_UNSUPPORTED_FEATURE, "SCARD_E_UNSUPPORTED_FEATURE"); b.put(SCARD_E_UNSUPPORTED_FEATURE, "This smart card does not support the requested feature.");
		a.put(SCARD_E_ICC_INSTALLATION, "SCARD_E_ICC_INSTALLATION"); b.put(SCARD_E_ICC_INSTALLATION, "No primary provider can be found for the smart card.");
		a.put(SCARD_E_ICC_CREATEORDER, "SCARD_E_ICC_CREATEORDER"); b.put(SCARD_E_ICC_CREATEORDER, "The requested order of object creation is not supported.");
		a.put(SCARD_E_DIR_NOT_FOUND, "SCARD_E_DIR_NOT_FOUND"); b.put(SCARD_E_DIR_NOT_FOUND, "The identified directory does not exist in the smart card.");
		a.put(SCARD_E_FILE_NOT_FOUND, "SCARD_E_FILE_NOT_FOUND"); b.put(SCARD_E_FILE_NOT_FOUND, "The identified file does not exist in the smart card.");
		a.put(SCARD_E_NO_DIR, "SCARD_E_NO_DIR"); b.put(SCARD_E_NO_DIR, "The supplied path does not represent a smart card directory.");
		a.put(SCARD_E_NO_FILE, "SCARD_E_NO_FILE"); b.put(SCARD_E_NO_FILE, "The supplied path does not represent a smart card file.");
		a.put(SCARD_E_NO_ACCESS, "SCARD_E_NO_ACCESS"); b.put(SCARD_E_NO_ACCESS, "Access is denied to this file.");
		a.put(SCARD_E_WRITE_TOO_MANY, "SCARD_E_WRITE_TOO_MANY"); b.put(SCARD_E_WRITE_TOO_MANY, "The smart card does not have enough memory to store the information.");
		a.put(SCARD_E_BAD_SEEK, "SCARD_E_BAD_SEEK"); b.put(SCARD_E_BAD_SEEK, "There was an error trying to set the smart card file object pointer.");
		a.put(SCARD_E_INVALID_CHV, "SCARD_E_INVALID_CHV"); b.put(SCARD_E_INVALID_CHV, "The supplied PIN is incorrect.");
		a.put(SCARD_E_UNKNOWN_RES_MNG, "SCARD_E_UNKNOWN_RES_MNG"); b.put(SCARD_E_UNKNOWN_RES_MNG, "An unrecognized error code was returned from a layered component.");
		a.put(SCARD_E_NO_SUCH_CERTIFICATE, "SCARD_E_NO_SUCH_CERTIFICATE"); b.put(SCARD_E_NO_SUCH_CERTIFICATE, "The requested certificate does not exist.");
		a.put(SCARD_E_CERTIFICATE_UNAVAILABLE, "SCARD_E_CERTIFICATE_UNAVAILABLE"); b.put(SCARD_E_CERTIFICATE_UNAVAILABLE, "The requested certificate could not be obtained.");
		a.put(SCARD_E_NO_READERS_AVAILABLE, "SCARD_E_NO_READERS_AVAILABLE"); b.put(SCARD_E_NO_READERS_AVAILABLE, "Cannot find a smart card reader.");
		a.put(SCARD_E_COMM_DATA_LOST, "SCARD_E_COMM_DATA_LOST"); b.put(SCARD_E_COMM_DATA_LOST, "A communications error with the smart card has been detected.");
		a.put(SCARD_E_NO_KEY_CONTAINER, "SCARD_E_NO_KEY_CONTAINER"); b.put(SCARD_E_NO_KEY_CONTAINER, "The requested key container does not exist on the smart card.");
		a.put(SCARD_E_SERVER_TOO_BUSY, "SCARD_E_SERVER_TOO_BUSY"); b.put(SCARD_E_SERVER_TOO_BUSY, "The Smart Card Resource Manager is too busy to complete this operation.");
		a.put(SCARD_W_UNSUPPORTED_CARD, "SCARD_W_UNSUPPORTED_CARD"); b.put(SCARD_W_UNSUPPORTED_CARD, "The reader cannot communicate with the card, due to ATR string configuration conflicts.");
		a.put(SCARD_W_UNRESPONSIVE_CARD, "SCARD_W_UNRESPONSIVE_CARD"); b.put(SCARD_W_UNRESPONSIVE_CARD, "The smart card is not responding to a reset.");
		a.put(SCARD_W_UNPOWERED_CARD, "SCARD_W_UNPOWERED_CARD"); b.put(SCARD_W_UNPOWERED_CARD, "Power has been removed from the smart card, so that further communication is not possible.");
		a.put(SCARD_W_RESET_CARD, "SCARD_W_RESET_CARD"); b.put(SCARD_W_RESET_CARD, "The smart card has been reset, so any shared state information is invalid.");
		a.put(SCARD_W_REMOVED_CARD, "SCARD_W_REMOVED_CARD"); b.put(SCARD_W_REMOVED_CARD, "The smart card has been removed, so further communication is not possible.");
		a.put(SCARD_W_SECURITY_VIOLATION, "SCARD_W_SECURITY_VIOLATION"); b.put(SCARD_W_SECURITY_VIOLATION, "Access was denied because of a security violation.");
		a.put(SCARD_W_WRONG_CHV, "SCARD_W_WRONG_CHV"); b.put(SCARD_W_WRONG_CHV, "The card cannot be accessed because the wrong PIN was presented.");
		a.put(SCARD_W_CHV_BLOCKED, "SCARD_W_CHV_BLOCKED"); b.put(SCARD_W_CHV_BLOCKED, "The card cannot be accessed because the maximum number of PIN entry attempts has been reached.");
		a.put(SCARD_W_EOF, "SCARD_W_EOF"); b.put(SCARD_W_EOF, "The end of the smart card file has been reached.");
		a.put(SCARD_W_CANCELLED_BY_USER, "SCARD_W_CANCELLED_BY_USER"); b.put(SCARD_W_CANCELLED_BY_USER, "The user pressed \"Cancel\" on a Smart Card Selection Dialog.");
		a.put(SCARD_W_CARD_NOT_AUTHENTICATED, "SCARD_W_CARD_NOT_AUTHENTICATED"); b.put(SCARD_W_CARD_NOT_AUTHENTICATED, "No PIN was presented to the smart card.");
		ERROR_TO_VARIABLE_NAME = Collections.unmodifiableMap(a);
		ERROR_TO_DESCRIPTION = Collections.unmodifiableMap(b);
	}

	// Bit masks used by waitFor* methods for SCardGetStatusChange
	public static final int SCARD_STATE_UNAWARE = 0x0000;
	public static final int SCARD_STATE_IGNORE = 0x0001;
	public static final int SCARD_STATE_CHANGED = 0x0002;
	public static final int SCARD_STATE_UNKNOWN = 0x0004;
	public static final int SCARD_STATE_UNAVAILABLE = 0x0008;
	public static final int SCARD_STATE_EMPTY = 0x0010;
	public static final int SCARD_STATE_PRESENT = 0x0020;
	public static final int SCARD_STATE_ATRMATCH = 0x0040;
	public static final int SCARD_STATE_EXCLUSIVE = 0x0080;
	public static final int SCARD_STATE_INUSE = 0x0100;
	public static final int SCARD_STATE_MUTE = 0x0200;
	public static final int SCARD_STATE_UNPOWERED = 0x0400;
	public static List<String> stateToStrings(int scardState) {
		if (0 == scardState) return Arrays.asList("unaware");
		List<String> r = new ArrayList<String>();
		if (0 != (scardState & SCARD_STATE_IGNORE)) r.add("ignore");
		if (0 != (scardState & SCARD_STATE_CHANGED)) r.add("changed");
		if (0 != (scardState & SCARD_STATE_UNKNOWN)) r.add("unknown");
		if (0 != (scardState & SCARD_STATE_UNAVAILABLE)) r.add("unavailable");
		if (0 != (scardState & SCARD_STATE_EMPTY)) r.add("empty");
		if (0 != (scardState & SCARD_STATE_PRESENT)) r.add("present");
		if (0 != (scardState & SCARD_STATE_ATRMATCH)) r.add("atrmatch");
		if (0 != (scardState & SCARD_STATE_EXCLUSIVE)) r.add("exclusive");
		if (0 != (scardState & SCARD_STATE_INUSE)) r.add("inuse");
		if (0 != (scardState & SCARD_STATE_MUTE)) r.add("mute");
		if (0 != (scardState & SCARD_STATE_UNPOWERED)) r.add("unpowered");
		return r;
	}
	/** Infinite timeout for SCardGetStatusChange */
	public static final int INFINITE = 0xffffffff;
	public static final int MAX_ATR_SIZE = 33;
	public static final String PNP_READER_ID = "\\\\?PnP?\\Notification";
}
