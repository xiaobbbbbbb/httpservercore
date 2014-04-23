package com.ecarinfo.frame.httpserver.core.http.bean;

public class ParamValue {
	String val;
	
	public ParamValue(String val) {
		this.val = val;
	}

	public String getVal() {
		return val;
	}

	public void setVal(String val) {
		this.val = val;
	}
	
	public int intValue(int defaultValue) {
		Integer value = defaultValue;
		if (val!=null) {
			try {
				value = Integer.parseInt(val);
			} catch (Exception e) {				
			}			
		}		
		return value;
	}
	
	public Integer integerValue(Integer defaultValue) {
		Integer value = defaultValue;
		if (val!=null) {
			try {
				value = Integer.parseInt(val);
			} catch (Exception e) {				
			}			
		}		
		return value;
	}
	
	public long longValue(long defaultValue) {
		Long value = defaultValue;
		if (val!=null) {
			try {
				value = Long.parseLong(val);
			} catch (Exception e) {				
			}			
		}		
		return value;
	}
	
	public Long LongValue(Long defaultValue) {
		Long value = defaultValue;
		if (val!=null) {
			try {
				value = Long.parseLong(val);
			} catch (Exception e) {				
			}			
		}		
		return value;
	}
	
	public Object getValue(Class<?> clazz) {
		if (clazz.equals(String.class)) {
			return  getVal();
		} else if (clazz.equals(int.class)) {
			return  intValue(-1);
		} else if (clazz.equals(Integer.class)) {
			return  integerValue(null);
		} else if (clazz.equals(long.class)) {
			return  longValue(-1l);
		}else if (clazz.equals(Long.class)) {
			return  LongValue(null);
		} else if (clazz.equals(float.class)) {
			return  floatValue(0.0f);
		} else if (clazz.equals(Float.class)) {
			return  FloatValue(null);
		}else if (clazz.equals(double.class)) {
			return  doubleValue(0.0);
		}else if (clazz.equals(Double.class)) {
			return  DoubleValue(null);
		} else if (clazz.equals(boolean.class)) {
			return  boolValue(false);
		} else if (clazz.equals(Boolean.class)) {
			return  BoolValue(null);
		} 
		return null;
	}
	
	public static Object getDefaultValue(Class<?> clazz) {
		if (clazz.equals(String.class)) {
			return  null;
		} else if (clazz.equals(int.class)) {
			return  -1;
		} else if (clazz.equals(Integer.class)) {
			return  null;
		} else if (clazz.equals(long.class)) {
			return  -1l;
		}else if (clazz.equals(Long.class)) {
			return  null;
		} else if (clazz.equals(float.class)) {
			return  0.0f;
		} else if (clazz.equals(Float.class)) {
			return  null;
		}else if (clazz.equals(double.class)) {
			return  0.0;
		}else if (clazz.equals(Double.class)) {
			return  null;
		} else if (clazz.equals(boolean.class)) {
			return  false;
		} else if (clazz.equals(Boolean.class)) {
			return  null;
		} 
		return null;
	}
	
	public boolean boolValue(boolean defaultValue) {
		Boolean value = defaultValue;
		if (val!=null) {
			try {
				value = Boolean.parseBoolean(val);
			} catch (Exception e) {				
			}			
		}		
		return value;
	}
	
	public Boolean BoolValue(Boolean defaultValue) {
		Boolean value = defaultValue;
		if (val!=null) {
			try {
				value = Boolean.parseBoolean(val);
			} catch (Exception e) {				
			}			
		}		
		return value;
	}
	
	public float floatValue(float defaultValue) {
		Float value = defaultValue;
		if (val!=null) {
			try {
				value = Float.parseFloat(val);
			} catch (Exception e) {				
			}			
		}		
		return value;
	}
	
	public Float FloatValue(Float defaultValue) {
		Float value = defaultValue;
		if (val!=null) {
			try {
				value = Float.parseFloat(val);
			} catch (Exception e) {				
			}			
		}		
		return value;
	}
	
	public double doubleValue(double defaultValue) {
		Double value = defaultValue;
		if (val!=null) {
			try {
				value = Double.parseDouble(val);
			} catch (Exception e) {				
			}			
		}		
		return value;
	}
	
	public Double DoubleValue(Double defaultValue) {
		Double value = defaultValue;
		if (val!=null) {
			try {
				value = Double.parseDouble(val);
			} catch (Exception e) {				
			}			
		}		
		return value;
	}
}
