import java.io.Serializable;

public class Message implements Serializable{

	private static final long serialVersionUID = -931297275588424657L;

	private TypeMessage typemsg;

	public Message(TypeMessage typemsg) {
		this.typemsg = typemsg;
	}
	
	public TypeMessage getTypeMessage(){
		return typemsg;
	}

	@Override
	public String toString() {
		return super.toString();
	}
}