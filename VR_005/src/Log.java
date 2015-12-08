import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class Log implements Map<Integer,Request>{

	private Map<Integer,Request> log;
	
	public Log(){
		log = new HashMap<>();
	}

	public Map<Integer, Request> getLog() {
		return log;
	}

	public void setLog(Map<Integer, Request> log) {
		this.log = log;
	}	
	
	@Override
	public String toString(){
		String string = "";
		return string;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsKey(Object key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Request get(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Request put(Integer key, Request value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Request remove(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putAll(Map<? extends Integer, ? extends Request> m) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<Integer> keySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Request> values() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<java.util.Map.Entry<Integer, Request>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}
}
