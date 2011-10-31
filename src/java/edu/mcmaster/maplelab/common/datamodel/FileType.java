package edu.mcmaster.maplelab.common.datamodel;

import java.util.HashMap;
import java.util.Map;

public abstract class FileType {
	private static Map<String, FileType> _fileTypes = new HashMap<String, FileType>();
	
	/**
	 * Create the submitted file type, if one with the given name has not already
	 * been created.
	 */
	public static FileType create(String name, String ext, String suffix, boolean agg, boolean bt) {
		FileType retval = _fileTypes.get(name);
		if (retval == null) {
			retval = new FileTypeImpl(name, ext, suffix, agg, bt);
			_fileTypes.put(name, retval);
		}
		return retval;
	}
	
	/**
	 * Get the file type of the given name, if it exists.
	 */
	public static FileType get(String name) {
		return _fileTypes.get(name);
	}
	
	/**
	 * Get the name of this file type.
	 */
	public abstract String getName();
	
	/**
	 * Get the file extension for this file type.
	 */
	public abstract String getExtension();
	
	/**
	 * Get the descriptive suffix for this file type.
	 */
	public abstract String getSuffix();
	
	/**
	 * Indicates if this file type is aggregated from other
	 * files or self-contained.
	 */
	public abstract boolean isAggregateType();
	
	/**
	 * Indicates if this file type includes block and trial 
	 * numbers in its name.
	 */
	public abstract boolean includesBlockTrialNums();
	
	private static class FileTypeImpl extends FileType {
		private final String _name;
		private final String _ext;
		private final String _suffix;
		private final boolean _aggregate;
		private final boolean _btName;
		
		private FileTypeImpl(String name, String ext, String suffix, boolean agg, boolean includeBTName) {
			_name = name;
			_ext = ext;
			_suffix = suffix;
			_aggregate = agg;
			_btName = includeBTName;
		}

		@Override
		public String getName() {
			return _name;
		}

		@Override
		public String getExtension() {
			return _ext;
		}

		@Override
		public String getSuffix() {
			return _suffix;
		}

		@Override
		public boolean isAggregateType() {
			return _aggregate;
		}

		@Override
		public boolean includesBlockTrialNums() {
			return _btName;
		}
		
	}
}
